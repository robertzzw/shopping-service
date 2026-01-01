package com.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.OrderStatus;
import com.shopping.entity.OrderItem;
import com.shopping.entity.Order;
import com.shopping.mapper.OrderItemMapper;
import com.shopping.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** 订单明细服务实现 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    @Autowired
    private OrderService orderService;
    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        Validator.notNull(orderId, "订单ID不能为空");
        
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.orderByDesc("created_time");
        
        return this.list(queryWrapper);
    }

    @Override
    public List<OrderItem> findBySkuId(Long skuId) {
        Validator.notNull(skuId, "SKU ID不能为空");
        
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        queryWrapper.orderByDesc("created_time");
        
        return this.list(queryWrapper);
    }

    @Override
    public Integer countSalesBySkuId(Long skuId) {
        Validator.notNull(skuId, "SKU ID不能为空");
        
        // 查询所有已支付、已完成、已发货的订单ID
        QueryWrapper<Order> orderQuery = new QueryWrapper<>();
        orderQuery.in("order_status", 
            OrderStatus.PAID.getCode(), 
            OrderStatus.SHIPPED.getCode(), 
            OrderStatus.COMPLETED.getCode());
        List<Order> paidOrders = orderService.list(orderQuery);
        
        if (paidOrders.isEmpty()) {
            return 0;
        }
        
        // 提取订单ID
        List<Long> orderIds = paidOrders.stream()
            .map(Order::getOrderId)
            .collect(java.util.stream.Collectors.toList());
        
        // 统计销量
        QueryWrapper<OrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("sku_id", skuId);
        itemQuery.in("order_id", orderIds);
        
        List<OrderItem> items = this.list(itemQuery);
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
}