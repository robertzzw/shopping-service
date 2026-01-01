package com.shopping.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class UpdateBalanceRequest {
    private Long userId;
    private Long merchantId;
    @Min(value = 1, message = "金额必须大于0")
    private BigDecimal amount;
    private Boolean isAdd;
    private Long relatedId;
    private Integer transactionType;
    private String remark;
}
