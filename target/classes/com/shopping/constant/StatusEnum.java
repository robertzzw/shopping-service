package com.shopping.constant;

/**
 * 通用状态枚举
 */
public enum StatusEnum {
    DISABLED(0, "禁用/停用/下架"),
    ENABLED(1, "启用/正常/上架");

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static StatusEnum fromCode(Integer code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态编码: " + code);
    }
}