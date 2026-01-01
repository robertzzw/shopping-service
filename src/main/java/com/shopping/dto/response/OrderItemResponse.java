package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单明细响应DTO */
@Data
@ApiModel(description = "订单明细响应")
public class OrderItemResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单明细ID")
    private Long itemId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "商品单价")
    private BigDecimal unitPrice;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;
}