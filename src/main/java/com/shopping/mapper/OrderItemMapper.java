package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.OrderItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 订单明细Mapper接口 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /** 根据订单ID查询明细 */
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /** 根据SKU ID查询已支付的订单明细 */
    List<OrderItem> findPaidItemsBySkuId(@Param("skuId") Long skuId);
}