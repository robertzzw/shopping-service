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

/** 订单明细实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单明细ID */
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /** 订单ID */
    private Long orderId;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer quantity;

    /** 商品单价 */
    private BigDecimal unitPrice;

    /** 商品总价 */
    private BigDecimal totalPrice;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 备注 */
    private String remark;
}