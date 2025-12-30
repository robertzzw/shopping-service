package com.shopping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.dto.request.OrderCreateRequest;
import com.shopping.dto.request.OrderPayRequest;
import com.shopping.dto.request.OrderRefundRequest;
import com.shopping.dto.request.OrderUpdateRequest;
import com.shopping.dto.response.ApiResponse;
import com.shopping.dto.response.OrderResponse;
import com.shopping.dto.response.OrderSimpleResponse;
import com.shopping.dto.response.PageResponse;
import com.shopping.entity.Order;
import com.shopping.service.OrderItemService;
import com.shopping.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** 订单控制器 */
@Slf4j
@Validated
@RestController
@RequestMapping("/orders")
@Api(tags = "订单管理")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping
    @ApiOperation("创建订单")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        log.info("创建订单请求: {}", request);
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.success(response);
    }
    @PostMapping("/pay")
    @ApiOperation("支付订单")
    public ApiResponse<OrderResponse> payOrder(@Valid @RequestBody OrderPayRequest request) {
        log.info("支付订单请求: {}", request);
        OrderResponse response = orderService.payOrder(request);
        return ApiResponse.success(response);
    }
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询订单")
    public ApiResponse<OrderResponse> findById(@PathVariable Long id) {
        log.info("查询订单: id={}", id);
        OrderResponse response = orderService.findDetailById(id);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public ApiResponse<Boolean> deleteOrder(@PathVariable Long id) {
        log.info("删除订单: id={}", id);
        boolean success = orderService.deleteOrder(id);
        return ApiResponse.success(success);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询订单")
    public ApiResponse<PageResponse<OrderSimpleResponse>> findPage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询订单: pageNum={}, pageSize={}, userId={}, merchantId={}, status={}", 
                pageNum, pageSize, userId, merchantId, status);
        
        Page<Order> page = orderService.findPage(pageNum, pageSize, userId, merchantId, status);
        
        // 转换为简化响应
        List<OrderSimpleResponse> simpleResponses = page.getRecords().stream().map(order -> {
            OrderItemService orderItemService = null;
            List<com.shopping.entity.OrderItem> items = orderItemService.findByOrderId(order.getOrderId());
            return buildSimpleOrderResponse(order, items.size());
        }).collect(Collectors.toList());
        
        PageResponse<OrderSimpleResponse> response = PageResponse.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                simpleResponses
        );
        
        return ApiResponse.success(response);
    }

    /** 构建简化订单响应 */
    private OrderSimpleResponse buildSimpleOrderResponse(Order order, int itemCount) {
        OrderSimpleResponse response = new OrderSimpleResponse();
        org.springframework.beans.BeanUtils.copyProperties(order, response);
        
        // 设置订单状态描述
        com.shopping.constant.OrderStatus orderStatus = com.shopping.constant.OrderStatus.fromCode(order.getOrderStatus());
        response.setOrderStatusDesc(orderStatus.getDesc());
        response.setItemCount(itemCount);
        return response;
    }
}