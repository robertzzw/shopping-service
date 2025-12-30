package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消, 5-已退款 */
    private Integer orderStatus;

    /** 支付时间 */
    private LocalDateTime paymentTime;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}