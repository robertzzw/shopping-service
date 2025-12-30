package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 订单创建请求DTO */
@Data
@ApiModel(description = "订单创建请求")
public class OrderCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "商家ID", required = true)
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @ApiModelProperty(value = "商品id", required = true)
    @NotNull(message = "商品id不能为空")
    private Long productId;

    @ApiModelProperty(value = "订单商品明细", required = true)
    @Valid
    @NotNull(message = "订单商品明细不能为空")
    @Size(min = 1, message = "至少需要一件商品型号/明细")
    private List<OrderItemRequest> orderItems;
}

