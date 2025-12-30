package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/** 订单退款请求DTO */
@Data
@ApiModel(description = "订单退款请求")
public class OrderRefundRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款原因", required = true)
    @NotNull(message = "退款原因不能为空")
    private String reason;

    @ApiModelProperty(value = "备注")
    private String remark;
}