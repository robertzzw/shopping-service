package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

/** 商品SKU创建请求DTO */
@Data
@ApiModel(description = "商品SKU创建请求")
public class ProductSkuCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "SKU编码", required = true)
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @ApiModelProperty(value = "SKU名称", required = true)
    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    @ApiModelProperty(value = "单价", required = true)
    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于0")
    private BigDecimal price;

    @ApiModelProperty(value = "库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "SKU属性")
    private String attributes;
}