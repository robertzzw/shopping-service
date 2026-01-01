package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 订单商品明细请求DTO */
@Data
@ApiModel(description = "订单商品明细请求")
public class OrderItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "SKU ID", required = true)
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @ApiModelProperty(value = "购买数量", required = true)
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

    @ApiModelProperty(value = "备注")
    private String remark;
}