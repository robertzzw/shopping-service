package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品SKU响应DTO */
@Data
@ApiModel(description = "商品SKU响应")
public class ProductSkuResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "商品ID")
    private Long productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "库存版本")
    private Long stockVersion;

    @ApiModelProperty(value = "SKU属性")
    private String attributes;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedTime;
}