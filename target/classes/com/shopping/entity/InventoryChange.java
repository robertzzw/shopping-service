package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 库存变更记录实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_change")
public class InventoryChange implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库存变更ID */
    @TableId(value = "change_id", type = IdType.AUTO)
    private Long changeId;

    /** SKU ID */
    private Long skuId;

    /** 变更类型: 1-增加库存, 2-减少库存(销售), 3-调整库存 */
    private Integer changeType;

    /** 变更前库存 */
    private Integer stockBefore;

    /** 变更数量 */
    private Integer changeQuantity;

    /** 变更后库存 */
    private Integer stockAfter;

    /** 关联ID(订单ID、调整单ID等) */
    private Long relatedId;

    /** 关联类型 */
    private String relatedType;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdTime;
}