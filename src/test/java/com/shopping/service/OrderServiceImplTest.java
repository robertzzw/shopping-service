package com.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.constant.OrderStatus;
import com.shopping.dto.request.OrderCreateRequest;
import com.shopping.dto.request.OrderItemRequest;
import com.shopping.dto.request.OrderPayRequest;
import com.shopping.dto.response.OrderItemResponse;
import com.shopping.dto.response.OrderResponse;
import com.shopping.entity.*;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.OrdersMapper;
import com.shopping.utils.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private UserService userService;
    @Mock
    private MerchantService merchantService;
    @Mock
    private ProductService productService;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private IdGenerator idGenerator;

    // === 被测类：真实实例，注入 mocks ===
    @InjectMocks
    private OrderServiceImpl orderService; // 继承了 MyBatis-Plus ServiceImpl
    // === 测试数据 ===
    private User user;
    private Merchant merchant;
    private Product product;
    private ProductSku sku;
    private Order order;
    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testUser");
        user.setStatus(1);

        merchant = new Merchant();
        merchant.setMerchantId(100L);
        merchant.setUserId(2L);
        merchant.setStatus(1);

        product = new Product();
        product.setProductId(10L);
        product.setMerchantId(100L);
        product.setStatus(1);

        sku = new ProductSku();
        sku.setSkuId(1000L);
        sku.setProductId(10L);
        sku.setPrice(new BigDecimal("100.00"));
        sku.setStockQuantity(10);
        sku.setStockVersion(1L);
        sku.setStatus(1);

        order = new Order();
        order.setOrderId(10000L);
        order.setOrderNo("ORD20251231001");
        order.setUserId(1L);
        order.setMerchantId(100L);
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setCreatedTime(LocalDateTime.now());
    }
    @Test @DisplayName("测试_下单成功")
    void createOrder_success() {
        // 构建请求（分步赋值）
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.findById(100L)).thenReturn(merchant);
        when(productSkuService.validateStock(1000L, 2)).thenReturn(sku);
        when(productService.findById(10L)).thenReturn(product);
        when(idGenerator.generateOrderNo()).thenReturn("ORD20251231001");

        doAnswer(invocation -> {
            Order arg = invocation.getArgument(0);
            arg.setOrderId(10000L);
            return 1;
        }).when(ordersMapper).insert(any(Order.class));

        when(productSkuService.subtractStock(eq(1000L), eq(2), eq(1L),
                eq(10000L), eq("createOrder"), anyString()))
                .thenReturn(true);

        when(orderItemService.saveBatch(anyList())).thenReturn(true);

        // 调用被测方法（真实逻辑）
        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("ORD20251231001", response.getOrderNo());
        assertEquals(new BigDecimal("200.00"), response.getTotalAmount());

        verify(ordersMapper, times(1)).insert(any(Order.class));
        verify(orderItemService, times(1)).saveBatch(anyList());
    }

    @Test @DisplayName("测试_库存不足")
    void createOrder_insufficientStock_throwsException() {
        // 准备请求：购买数量超过库存
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(15); // 库存只有10，设置购买15会库存不足
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        // 模拟用户和商家正常
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(userService).validateUserStatus(any(User.class));
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟库存不足：validateStock 抛出异常
        when(productSkuService.validateStock(1000L, 15))
                .thenThrow(new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足"));

        // 执行并验证：应该抛出库存不足的异常
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request));

        // 验证异常信息
        assertEquals(ErrorCode.STOCK_NOT_ENOUGH.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("库存不足"));
        // 验证没有创建订单
        verify(ordersMapper, never()).insert(any(Order.class));
        verify(productSkuService, never()).subtractStock(anyLong(), anyInt(), anyLong(), anyLong(), anyString(), anyString());
        verify(orderItemService, never()).saveBatch(anyList());
    }
    @Test @DisplayName("测试_SKU不存在")
    void createOrder_stockValidationFails_throwsBusinessException() {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(5);
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        // 模拟正常用户和商家
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(userService).validateUserStatus(any(User.class));
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟库存验证失败（SKU不存在）
        when(productSkuService.validateStock(1000L, 5))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND_ERROR, "SKU不存在"));

        // 执行并验证
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request));
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), ex.getCode());
    }

    @Test @DisplayName("测试_扣减库存失败")
    void createOrder_concurrentStockReduction_failsAfterStockValidation() {
        // 测试场景：通过库存验证，但在实际扣减时失败（可能因为并发）
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(2);
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        // 模拟正常用户和商家
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(userService).validateUserStatus(any(User.class));
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟库存验证通过
        when(productSkuService.validateStock(1000L, 2)).thenReturn(sku);
        when(productService.findById(10L)).thenReturn(product);
        when(idGenerator.generateOrderNo()).thenReturn("ORD20251231001");

        // 模拟保存订单成功
        doAnswer(invocation -> {
            Order arg = invocation.getArgument(0);
            arg.setOrderId(10000L);
            return 1;
        }).when(ordersMapper).insert(any(Order.class));

        // 模拟扣减库存失败（例如版本号不一致，乐观锁失败）
        when(productSkuService.subtractStock(eq(1000L), eq(2), eq(1L),
                eq(10000L), eq("createOrder"), anyString()))
                .thenReturn(false);

        // 执行并验证：应该抛出扣减库存失败的异常
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request));

        assertEquals(ErrorCode.OPERATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("扣减库存失败"));

        // 验证订单虽然创建了，但事务应该回滚
        verify(ordersMapper, times(1)).insert(any(Order.class));
        verify(orderItemService, never()).saveBatch(anyList());
    }
    @Test @DisplayName("测试_多个商品,其中一个库存不足")
    void createOrder_multipleItems_oneItemInsufficientStock() {
        // 测试场景：多个商品，其中一个库存不足
        OrderItemRequest itemReq1 = new OrderItemRequest();
        itemReq1.setSkuId(1000L);
        itemReq1.setQuantity(5); // 库存10，这个可以
        OrderItemRequest itemReq2 = new OrderItemRequest();
        itemReq2.setSkuId(2000L); // 另一个SKU
        itemReq2.setQuantity(20); // 库存不足

        List<OrderItemRequest> items = Arrays.asList(itemReq1, itemReq2);
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(items);

        // 模拟正常用户和商家
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(userService).validateUserStatus(any(User.class));
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟第一个SKU库存验证通过
        when(productSkuService.validateStock(1000L, 5)).thenReturn(sku);
        // 模拟第二个SKU
        ProductSku sku2 = new ProductSku();
        sku2.setSkuId(2000L);
        sku2.setProductId(20L);
        sku2.setPrice(new BigDecimal("50.00"));
        sku2.setStockQuantity(10); // 库存只有10
        sku2.setStockVersion(1L);

        // 模拟第二个SKU库存不足
        when(productSkuService.validateStock(2000L, 20))
                .thenThrow(new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "商品[2000]库存不足"));

        // 模拟第一个SKU对应的商品
        Product product1 = new Product();
        product1.setProductId(10L);
        product1.setMerchantId(100L);
        when(productService.findById(10L)).thenReturn(product1);

        // 执行并验证
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request));

        assertEquals(ErrorCode.STOCK_NOT_ENOUGH.getCode(), ex.getCode());

        // 验证第一个SKU的库存验证被调用，但第二个失败后就停止了
        verify(productSkuService, times(1)).validateStock(1000L, 5);
        verify(productSkuService, times(1)).validateStock(2000L, 20);

        // 验证没有进行后续操作
        verify(ordersMapper, never()).insert(any(Order.class));
        verify(productSkuService, never()).subtractStock(anyLong(), anyInt(), anyLong(), anyLong(), anyString(), anyString());
    }

    @Test @DisplayName("测试_买数量为0或负数")
    void createOrder_zeroQuantity_throwsException() {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(0); // 数量为0
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        // 模拟正常用户和商家
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(userService).validateUserStatus(any(User.class));
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟库存验证：数量为0应该抛出参数错误
        when(productSkuService.validateStock(1000L, 0))
                .thenThrow(new BusinessException(ErrorCode.PARAMS_ERROR, "购买数量必须大于0"));

        // 执行并验证
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    @Test @DisplayName("测试_用户被禁用")
    void createOrder_userDisabled_throwsException() {
        user.setStatus(0);
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(3);
        request.setOrderItems(Collections.singletonList(itemReq));

        when(userService.getUserById(1L)).thenReturn(user);
//        when(() -> userService.validateUserStatus(any(User.class)))
//                .thenThrow(new BusinessException(ErrorCode.USER_DISABLED));

        // 使用doThrow来模拟void方法抛出异常
        doThrow(new BusinessException(ErrorCode.USER_DISABLED))
                .when(userService)
                .validateUserStatus(any(User.class));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.createOrder(request));
        assertEquals(ErrorCode.USER_DISABLED.getCode(), ex.getCode());
    }

    @Test @DisplayName("测试_支付订单成功")
    void payOrder_success() {
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        when(merchantService.findById(100L)).thenReturn(merchant);

        when(userService.updateUserBalance(eq(1L), eq(new BigDecimal("200.00")),
                eq("decrease"), eq(10000L), eq(2), anyString()))
                .thenReturn(new User());

        when(merchantService.updateBalance(eq(100L), eq(new BigDecimal("200.00")),
                eq(true), eq(10000L), eq(3), anyString()))
                .thenReturn(true);

        when(ordersMapper.updateOrderPaid(10000L)).thenReturn(1);
        OrderPayRequest request = new OrderPayRequest();
        request.setOrderId(10000L);

        OrderResponse response = orderService.payOrder(request);
        assertNotNull(response);
        assertEquals(OrderStatus.PAID.getCode(), response.getOrderStatus());
        assertNotNull(response.getPaymentTime());
    }

    @Test @DisplayName("测试_支付订单 用户余额不足")
    void payOrder_insufficientUserBalance_throwsException() {
        OrderPayRequest request = new OrderPayRequest();
        request.setOrderId(10000L);
        // 模拟订单存在且为待支付状态
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        // 模拟商家正常
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟用户余额不足：updateUserBalance 抛出余额不足异常
        when(userService.updateUserBalance(eq(1L), eq(new BigDecimal("200.00")),
                eq("decrease"), eq(10000L), eq(2), anyString()))
                .thenThrow(new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH, "用户余额不足"));

        // 执行并验证：应该抛出余额不足的异常
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.payOrder(request));

        // 验证异常信息
        assertEquals(ErrorCode.BALANCE_NOT_ENOUGH.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("用户余额不足"));

        // 验证没有更新订单状态为已支付
        verify(ordersMapper, never()).updateOrderPaid(anyLong());
        // 验证没有增加商家余额
        verify(merchantService, never()).updateBalance(anyLong(), any(BigDecimal.class), anyBoolean(),
                anyLong(), anyInt(), anyString());
    }

    @Test @DisplayName("测试_支付订单 扣减用户余额失败")
    void payOrder_userBalanceUpdateFails_throwsException() {
        OrderPayRequest request = new OrderPayRequest();
        request.setOrderId(10000L);
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        when(merchantService.findById(100L)).thenReturn(merchant);
        doNothing().when(merchantService).validateMerchantStatus(any(Merchant.class));

        // 模拟用户余额扣减失败（例如数据库连接失败、乐观锁冲突等）
        when(userService.updateUserBalance(eq(1L), eq(new BigDecimal("200.00")),
                eq("decrease"), eq(10000L), eq(2), anyString()))
                .thenThrow(new BusinessException(ErrorCode.OPERATION_FAILED, "扣减用户余额失败"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.payOrder(request));

        // 验证异常信息
        assertEquals(ErrorCode.OPERATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("扣减用户余额失败"));
        // 验证没有进行后续操作
        verify(ordersMapper, never()).updateOrderPaid(anyLong());
        verify(merchantService, never()).updateBalance(anyLong(), any(BigDecimal.class), anyBoolean(),
                anyLong(), anyInt(), anyString());
    }

    @Test @DisplayName("测试_查询订单详情")
    void findDetailById_success() {
        OrderItem item = new OrderItem();
        item.setItemId(1L);
        item.setOrderId(10000L);
        item.setSkuId(1000L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setTotalPrice(new BigDecimal("200.00"));

        when(ordersMapper.selectById(10000L)).thenReturn(order);
        when(orderItemService.findByOrderId(10000L)).thenReturn(Collections.singletonList(item));
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.findById(100L)).thenReturn(merchant);
        when(productSkuService.findById(1000L)).thenReturn(sku);

        OrderResponse response = orderService.findDetailById(10000L);
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        OrderItemResponse itemResp = response.getItems().get(0);
        assertEquals(1000L, itemResp.getSkuId());
        assertEquals(2, itemResp.getQuantity());
    }

    @Test @DisplayName("测试_删除订单成功")
    void deleteOrder_pending_success() {
        // 让 getById 返回 pending 订单
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        when(ordersMapper.selectById(10000L)).thenReturn(order);

        when(orderItemService.remove(any(QueryWrapper.class))).thenReturn(true);
        when(ordersMapper.deleteById(10000L)).thenReturn(1); // 返回影响行数

        boolean result = orderService.deleteOrder(10000L);
        assertTrue(result);
        verify(ordersMapper, times(1)).deleteById(10000L);
        verify(orderItemService, times(1)).remove(any(QueryWrapper.class));
    }

    @Test @DisplayName("测试_删除未支付订单/已取消订单")
    void deleteOrder_paid_cannotDelete() {
        order.setOrderStatus(OrderStatus.PAID.getCode());
        when(ordersMapper.selectById(10000L)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.deleteOrder(10000L));
        assertEquals(ErrorCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test @DisplayName("测试_分页查询订单")
    void findPage_success() {
        Page<Order> mockPage = new Page<>(1, 10);
        order.setOrderStatus(OrderStatus.PAID.getCode());
        mockPage.setRecords(Collections.singletonList(order));
        mockPage.setTotal(1);

        when(ordersMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        Page<Order> page = orderService.findPage(1, 10, 1L,
                100L, null);
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());

        Page<Order> page2 = orderService.findPage(1, 10, null,
                null, 1);
        assertEquals(1, page2.getTotal());
        assertEquals(1, page2.getRecords().size());
    }

    @Test
    void calculateOrderAmount_success() {
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        BigDecimal amount = orderService.calculateOrderAmount(10000L);
        assertEquals(new BigDecimal("200.00"), amount);
    }

    @Test
    void calculateOrderAmount_notFound() {
        when(ordersMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.calculateOrderAmount(999L));
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), ex.getCode());
    }
}