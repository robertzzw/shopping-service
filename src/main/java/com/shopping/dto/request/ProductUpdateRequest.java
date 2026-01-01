package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/** 商品更新请求DTO */
@Data
@ApiModel(description = "商品更新请求")
public class ProductUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品分类")
    private String category;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "状态: 0-下架, 1-上架")
    private Integer status;
}