package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 每日结算结果汇总 */
@Data
@ApiModel(description = "每日结算结果汇总")
public class SettlementResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "结算日期")
    private LocalDate settlementDate;

    @ApiModelProperty(value = "总商家数")
    private Integer totalMerchants;

    @ApiModelProperty(value = "成功结算商家数")
    private Integer successCount;

    @ApiModelProperty(value = "失败结算商家数")
    private Integer failedCount;

    @ApiModelProperty(value = "成功详情列表")
    private List<SettlementDetail> successDetails;

    @ApiModelProperty(value = "失败详情列表")
    private List<SettlementDetail> failDetails;

    @ApiModelProperty(value = "是否全部成功")
    private Boolean allSuccess;

    @ApiModelProperty(value = "执行总耗时(秒)")
    private String executionTime;

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;
}