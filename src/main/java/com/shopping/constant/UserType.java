package com.shopping.constant;

/**
 * 用户类型枚举
 */
public enum UserType {
    NORMAL(1, "普通用户"),
    MERCHANT(2, "商家用户");

    private final Integer code;
    private final String desc;

    UserType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static UserType fromCode(Integer code) {
        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的用户类型编码: " + code);
    }
}