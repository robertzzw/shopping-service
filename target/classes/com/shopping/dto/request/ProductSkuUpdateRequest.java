package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/** 商品SKU更新请求DTO */
@Data
@ApiModel(description = "商品SKU更新请求")
public class ProductSkuUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "SKU属性")
    private String attributes;

    @ApiModelProperty(value = "状态: 0-不可用, 1-可用")
    private Integer status;
}