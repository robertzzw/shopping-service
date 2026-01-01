package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/** 库存增加请求DTO */
@Data
@ApiModel(description = "库存增加请求")
public class InventoryAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "SKU ID", required = true)
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @ApiModelProperty(value = "增加数量", required = true)
    @NotNull(message = "增加数量不能为空")
    @Positive(message = "增加数量必须大于0")
    private Integer quantity;

    @ApiModelProperty(value = "备注")
    private String remark;
}