package com.shopping.constant;

/**
 * 账户类型枚举
 */
public enum AccountType {
    USER_ACCOUNT(1, "用户账户"),
    MERCHANT_ACCOUNT(2, "商家账户");

    private final Integer code;
    private final String desc;

    AccountType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AccountType fromCode(Integer code) {
        for (AccountType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的账户类型编码: " + code);
    }
}