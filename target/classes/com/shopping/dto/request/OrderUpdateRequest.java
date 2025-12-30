package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/** 订单更新请求DTO */
@Data
@ApiModel(description = "订单更新请求")
public class OrderUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消, 5-已退款")
    private Integer orderStatus;

    @ApiModelProperty(value = "备注")
    private String remark;
}