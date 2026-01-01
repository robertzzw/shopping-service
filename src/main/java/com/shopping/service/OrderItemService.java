package com.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.entity.OrderItem;
import java.util.List;

/** 订单明细服务接口 */
public interface OrderItemService extends IService<OrderItem> {

    /** 根据订单ID查询明细 */
    List<OrderItem> findByOrderId(Long orderId);
    
    /** 根据SKU ID查询订单明细 */
    List<OrderItem> findBySkuId(Long skuId);
    
    /** 统计商品销量 */
    Integer countSalesBySkuId(Long skuId);
}