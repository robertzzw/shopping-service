package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品SKU实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_sku")
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SKU ID */
    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    /** SKU编码 */
    private String skuCode;

    /** 商品ID */
    private Long productId;

    /** SKU名称 */
    private String skuName;

    /** 单价 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stockQuantity;

    /** 库存版本 */
    private Long stockVersion;

    /** SKU属性(如: 颜色:红色,尺寸:M) */
    private String attributes;

    /** 状态: 0-不可用, 1-可用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 用于存储过程返回错误信息, 插入和更新操作会自动忽略该字段, 查询结果可以正常接收 */
    @TableField(exist = false)
    private String resultMessage;
}