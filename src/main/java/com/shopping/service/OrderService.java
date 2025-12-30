package com.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.dto.request.OrderCreateRequest;
import com.shopping.dto.request.OrderPayRequest;
import com.shopping.dto.request.OrderRefundRequest;
import com.shopping.dto.request.OrderUpdateRequest;
import com.shopping.dto.response.OrderResponse;
import com.shopping.entity.Order;

import java.time.LocalDate;
import java.util.List;

/** 订单服务接口 */
public interface OrderService extends IService<Order> {
    /** 创建订单 */
    OrderResponse createOrder(OrderCreateRequest request);

    /** 支付订单 */
    OrderResponse payOrder(OrderPayRequest request);
    
    /** 根据ID查询订单 */
    Order findById(Long orderId);

    /** 查询订单详情 */
    OrderResponse findDetailById(Long orderId);

    /** 删除订单 */
    boolean deleteOrder(Long orderId);
    
    /** 分页查询订单 */
    Page<Order> findPage(int pageNum, int pageSize, Long userId, Long merchantId, Integer status);

    /** 计算订单金额 */
    java.math.BigDecimal calculateOrderAmount(Long orderId);
}