package com.shopping.constant;

/**
 * 交易类型枚举
 */
public enum TransactionType {
    RECHARGE(1, "充值"),
    CONSUME(2, "消费"),
    INCOME(3, "收入"),
    REFUND(4, "退款");

    private final Integer code;
    private final String desc;

    TransactionType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TransactionType fromCode(Integer code) {
        for (TransactionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的交易类型编码: " + code);
    }
}