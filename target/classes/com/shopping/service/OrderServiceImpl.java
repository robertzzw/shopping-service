package com.shopping.service;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.*;
import com.shopping.dto.request.*;
import com.shopping.dto.response.OrderItemResponse;
import com.shopping.dto.response.OrderResponse;
import com.shopping.dto.response.OrderSimpleResponse;
import com.shopping.entity.*;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.OrdersMapper;
import com.shopping.utils.IdGenerator;
import com.shopping.utils.MoneyUtil;
import com.shopping.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/** 订单服务实现 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Order> implements OrderService {
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductSkuService productSkuService;
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("创建订单: {}", request);
        // 验证参数
        Validator.notNull(request.getUserId(), "用户ID不能为空");
        Validator.notNull(request.getMerchantId(), "商家ID不能为空");
        Validator.notEmpty(request.getOrderItems(), "订单商品不能为空");

        // 验证用户
        User user = userService.getUserById(request.getUserId());
        userService.validateUserStatus(user);
        // 验证商家
        Merchant merchant = merchantService.findById(request.getMerchantId());
        merchantService.validateMerchantStatus(merchant);

        // 验证商品SKU和库存
        List<OrderItemRequest> reqItems = request.getOrderItems();
        BigDecimal totalAmount = new BigDecimal("0.00");

        List<OrderItem> orderItems = new ArrayList<>();
        Map<Long, Long> skuVersionMap = new HashMap<>();
        for (OrderItemRequest item : reqItems) {
            //todo 检查库存是否足够时,使用for update加锁来查询, 防止后面扣减库存时,出现超额/并发更新覆盖
            //验证SKU和库存
            ProductSku sku = productSkuService.validateStock(item.getSkuId(), item.getQuantity());
            // 验证SKU是否属于该商家
            Product product = productService.findById(sku.getProductId());
            if (!merchant.getMerchantId().equals(product.getMerchantId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品不属于该商家");
            }
            skuVersionMap.put(sku.getSkuId(), sku.getStockVersion());
            // 计算商品总价
            BigDecimal itemTotal = MoneyUtil.calculateTotal(sku.getPrice(), item.getQuantity());
            totalAmount = totalAmount.add(itemTotal);

            // 创建订单明细
            OrderItem orderItem = new OrderItem();
            //订单明细是否应该要带上商品id/productId?
            orderItem.setSkuId(sku.getSkuId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(sku.getPrice());
            orderItem.setTotalPrice(MoneyUtil.calculateTotal(sku.getPrice(), item.getQuantity()));
            orderItem.setRemark(item.getRemark());
            orderItem.setCreatedTime(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        // 生成订单号
        String orderNo = idGenerator.generateOrderNo();
        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setMerchantId(request.getMerchantId());
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTotalAmount(totalAmount);
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        boolean saveOrderSuccess = this.save(order);
        if (!saveOrderSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "保存订单失败");
        }
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getOrderId());
            Long skuId = orderItem.getSkuId();
            Long skuVersion = skuVersionMap.get(skuId);
            // 扣减库存
            boolean reduceStockSuccess = productSkuService.subtractStock(
                    skuId,
                    orderItem.getQuantity(),
                    skuVersion,
                    order.getOrderId(),
                    "createOrder",
                    "下单时扣减库存"
            );
            if (!reduceStockSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "扣减库存失败");
            }
        }

        boolean saveItemsSuccess = orderItemService.saveBatch(orderItems);
        if (!saveItemsSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "保存订单明细失败");
        }
        // 构建响应
        OrderResponse response = buildOrderResponse(order, orderItems);
        response.setUsername(user.getUsername());
        response.setMerchantName(merchant.getMerchantName());
        log.info("创建订单成功，订单ID: {}, 订单号: {}, 总金额: {}", order.getOrderId(), orderNo, totalAmount);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse payOrder(OrderPayRequest request) {
        log.info("支付订单: {}", request);
        Validator.notNull(request.getOrderId(), "订单ID不能为空");
        Order order = this.findById(request.getOrderId());
        // 验证订单状态
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单不是待支付状态");
        }
        // 验证商家
        Merchant merchant = merchantService.findById(order.getMerchantId());
        merchantService.validateMerchantStatus(merchant);

        // 扣减用户余额,并记录交易流水
        User user = userService.updateUserBalance(order.getUserId(),
                order.getTotalAmount(), "decrease", order.getOrderId(), 2, "用户消费");

        // 增加商家余额,并记录交易流水
        boolean merchantBalanceSuccess = merchantService.updateBalance(merchant.getMerchantId(),
                order.getTotalAmount(), true, order.getOrderId(), 3, "商家收入");
        if (!merchantBalanceSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "增加商家余额失败");
        }

        // 更新订单状态
        Integer integer = ordersMapper.updateOrderPaid(order.getOrderId());
        if (integer != 1) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新订单失败");
        }
        // 构建响应
        order.setOrderStatus(1);
        order.setPaymentTime(LocalDateTime.now());
        List<OrderItem> orderItems = orderItemService.findByOrderId(order.getOrderId());
        OrderResponse response = buildOrderResponse(order, orderItems);
        response.setUsername(user.getUsername());
        response.setMerchantName(merchant.getMerchantName());
        log.info("支付订单成功，订单ID: {}, 订单号: {}, 支付金额: {}", order.getOrderId(), order.getOrderNo(), order.getTotalAmount());
        return response;
    }

    @Override
    public Order findById(Long orderId) {
        Validator.notNull(orderId, "订单ID不能为空");
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        return order;
    }

    @Override
    public OrderResponse findDetailById(Long orderId) {
        Order order = this.findById(orderId);
        // 查询订单明细
        List<OrderItem> orderItems = orderItemService.findByOrderId(orderId);
        // 查询用户和商家信息
        User user = userService.getUserById(order.getUserId());
        Merchant merchant = merchantService.findById(order.getMerchantId());
        // 构建响应
        OrderResponse response = buildOrderResponse(order, orderItems);
        response.setUsername(user.getUsername());
        response.setMerchantName(merchant.getMerchantName());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long orderId) {
        log.info("删除订单: orderId={}", orderId);
        Order order = this.findById(orderId);
        // 检查订单状态，只有待支付和已取消的订单可以删除
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getOrderStatus()) &&
                !OrderStatus.CANCELLED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有待支付和已取消的订单可以删除");
        }
        // 删除订单明细
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        orderItemService.remove(queryWrapper);
        // 删除订单
        boolean deleteSuccess = this.removeById(orderId);
        if (deleteSuccess) {
            log.info("删除订单成功，订单ID: {}", orderId);
        }
        return deleteSuccess;
    }

    @Override
    public Page<Order> findPage(int pageNum, int pageSize, Long userId, Long merchantId, Integer status) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (merchantId != null) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        if (status != null) {
            queryWrapper.eq("order_status", status);
        }
        queryWrapper.orderByDesc("created_time");
        Page<Order> page = new Page<>(pageNum, pageSize);
        return this.page(page, queryWrapper);
    }

    @Override
    public BigDecimal calculateOrderAmount(Long orderId) {
        Order order = this.findById(orderId);
        return order.getTotalAmount();
    }

    /**
     * 构建订单响应
     */
    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems) {
        OrderResponse response = new OrderResponse();
        BeanUtil.copyProperties(order, response);
        // 设置订单状态描述
        OrderStatus orderStatus = OrderStatus.fromCode(order.getOrderStatus());
        response.setOrderStatusDesc(orderStatus.getDesc());

        // 构建订单明细响应
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            BeanUtil.copyProperties(item, itemResponse);
            // 查询SKU信息
            ProductSku sku = productSkuService.findById(item.getSkuId());
            if (sku != null) {
                itemResponse.setSkuCode(sku.getSkuCode());
                itemResponse.setSkuName(sku.getSkuName());
            }
            return itemResponse;
        }).collect(Collectors.toList());
        response.setItems(itemResponses);
        return response;
    }

    /**
     * 构建简化订单响应
     */
    private OrderSimpleResponse buildSimpleOrderResponse(Order order, int itemCount) {
        OrderSimpleResponse response = new OrderSimpleResponse();
        BeanUtil.copyProperties(order, response);
        // 设置订单状态描述
        OrderStatus orderStatus = OrderStatus.fromCode(order.getOrderStatus());
        response.setOrderStatusDesc(orderStatus.getDesc());
        response.setItemCount(itemCount);
        return response;
    }
}