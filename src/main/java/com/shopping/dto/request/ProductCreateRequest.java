package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 商品创建请求DTO */
@Data
@ApiModel(description = "商品创建请求")
public class ProductCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @ApiModelProperty(value = "商品编码", required = true)
    @NotBlank(message = "商品编码不能为空")
    private String productCode;

    @ApiModelProperty(value = "商家ID", required = true)
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @ApiModelProperty(value = "商品分类")
    private String category;

    @ApiModelProperty(value = "商品描述")
    private String description;
}