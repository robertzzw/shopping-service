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

/** 商家实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant")
public class Merchant implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 商家ID */
    @TableId(value = "merchant_id", type = IdType.AUTO)
    private Long merchantId;

    /** 关联用户ID */
    private Long userId;

    /** 商家名称 */
    private String merchantName;

    /** 商家编码 */
    private String merchantCode;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 商家账户余额 */
    private BigDecimal accountBalance;

    /** 余额版本 */
    private Long balanceVersion;

    /** 状态: 0-停用, 1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}