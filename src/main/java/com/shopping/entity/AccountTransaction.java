package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 账户交易记录实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("account_transaction")
public class AccountTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 交易记录ID */
    @TableId(value = "transaction_id", type = IdType.AUTO)
    private Long transactionId;

    /** 交易流水号 */
    private String transactionNo;

    /** 账户类型: 1-用户账户, 2-商家账户 */
    private Integer accountType;

    /** 账户ID(用户ID或商家ID) */
    private Long accountId;

    /** 交易类型: 1-充值, 2-消费, 3-收入, 4-退款 */
    private Integer transactionType;

    /** 交易前余额 */
    private BigDecimal balanceBefore;

    /** 交易金额 */
    private BigDecimal amount;

    /** 交易后余额 */
    private BigDecimal balanceAfter;

    /** 关联ID(订单ID、充值ID等) */
    private Long relatedId;

    /** 关联类型 */
    private String relatedType;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdTime;
}