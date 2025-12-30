package com.shopping.constant;

/**
 * 库存变更类型枚举
 */
public enum InventoryChangeType {
    ADD(1, "增加库存"),
    REDUCE(2, "减少库存"),
    ADJUST(3, "调整库存");

    private final Integer code;
    private final String desc;

    InventoryChangeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InventoryChangeType fromCode(Integer code) {
        for (InventoryChangeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的库存变更类型编码: " + code);
    }
}