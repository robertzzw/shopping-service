package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 每日结算实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("daily_settlement")
public class DailySettlement implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 结算ID */
    @TableId(value = "settlement_id", type = IdType.AUTO)
    private Long settlementId;

    /** 结算单号 */
    private String settlementNo;

    /** 商家ID */
    private Long merchantId;

    /** 结算日期 */
    private LocalDate settlementDate;

    /** 当日已支付订单金额 */
    private BigDecimal paidAmount;

    /** 当日已退款订单金额 */
    private BigDecimal refundAmount;

    /** 当日净销售额 */
    private BigDecimal soldAmount;

    /** 商家账户当日净收入 */
    private BigDecimal merchantNetIncome;

    /** 是否匹配: 0-不匹配, 1-匹配 */
    private Integer isMatched;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdTime;
}