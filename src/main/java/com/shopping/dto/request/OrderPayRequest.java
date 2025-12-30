package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 订单支付请求DTO */
@Data
@ApiModel(description = "订单支付请求")
public class OrderPayRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "支付方式")
    private String paymentMethod;

    @ApiModelProperty(value = "支付备注")
    private String remark;
}