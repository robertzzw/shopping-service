package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 结算响应DTO */
@Data
@ApiModel(description = "结算响应")
public class SettlementDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "结算ID")
    private Long settlementId;

    @ApiModelProperty(value = "结算单号")
    private String settlementNo;

    @ApiModelProperty(value = "商家ID")
    private Long merchantId;

    @ApiModelProperty(value = "商家名称")
    private String merchantName;

    @ApiModelProperty(value = "结算日期")
    private LocalDate settlementDate;

    @ApiModelProperty(value = "当日已支付订单金额")
    private BigDecimal paidAmount;

    @ApiModelProperty(value = "当日已退款订单金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "当日净销售额")
    private BigDecimal soldAmount;

    @ApiModelProperty(value = "商家账户当日净收入")
    private BigDecimal merchantNetIncome;

    @ApiModelProperty(value = "结果匹配,并且保存成功才是成功, 不匹配或异常都算失败")
    private Boolean isSuccess;

    @ApiModelProperty(value = "是否匹配: 0-不匹配, 1-匹配")
    private Integer isMatched;

    @ApiModelProperty(value = "匹配结果描述")
    private String matchDescription;

    @ApiModelProperty(value = "差异金额")
    private BigDecimal diffAmount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;
}