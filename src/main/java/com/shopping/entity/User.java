package com.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 用户实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 用户ID */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 账户余额 */
    private BigDecimal accountBalance;

    /** 余额版本 */
    private Long balanceVersion;

    /** 用户类型: 1-普通用户, 2-商家用户 */
    private Integer userType;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
    /** 用于存储过程返回错误信息, 插入和更新操作会自动忽略该字段, 查询结果可以正常接收 */
    @TableField(exist = false)
    private String resultMessage;
}