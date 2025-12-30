package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品响应DTO */
@Data
@ApiModel(description = "商品响应")
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID")
    private Long productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品编码")
    private String productCode;

    @ApiModelProperty(value = "商家ID")
    private Long merchantId;

    @ApiModelProperty(value = "商家名称")
    private String merchantName;

    @ApiModelProperty(value = "商品分类")
    private String category;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedTime;

    @ApiModelProperty(value = "SKU数量")
    private Long skuCount;
}