package com.shopping.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/** 商家创建请求DTO */
@Data
@ApiModel(description = "商家创建请求")
public class MerchantCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "商家名称", required = true)
    @NotBlank(message = "商家名称不能为空")
    private String merchantName;

    @ApiModelProperty(value = "商家编码", required = true)
    @NotBlank(message = "商家编码不能为空")
    private String merchantCode;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "联系人")
    private String contactPerson;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "初始余额")
    private BigDecimal initialBalance;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}