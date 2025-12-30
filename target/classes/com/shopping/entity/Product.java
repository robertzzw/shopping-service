package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品编码 */
    private String productCode;

    /** 商家ID */
    private Long merchantId;

    /** 商品分类 */
    private String category;

    /** 商品描述 */
    private String description;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}