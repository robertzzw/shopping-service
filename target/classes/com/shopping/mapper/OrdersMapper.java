package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 订单Mapper接口 */
@Repository
public interface OrdersMapper extends BaseMapper<Order> {
    /** 将订单状态由未支付0, 改为已支付1 */
    Integer updateOrderPaid(@Param("orderId") Long orderId);

    /** 统计商家指定日期的销售额 */
    BigDecimal sumMerchantSalesByDate(@Param("merchantId") Long merchantId, @Param("date") LocalDate date);

    /** 统计商家指定日期的已支付订单金额 */
    BigDecimal sumMerchantPaidSalesByDate(@Param("merchantId") Long merchantId, @Param("date") LocalDate date);

    /** 统计商家指定日期的已退款订单金额 */
    BigDecimal sumMerchantRefundSalesByDate(@Param("merchantId") Long merchantId, @Param("date") LocalDate date);

    /** 查询商家指定日期的订单 */
    List<Order> findMerchantOrdersByDate(@Param("merchantId") Long merchantId, @Param("date") LocalDate date);
}