package com.shopping.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 */
public class MoneyUtil {

    /**
     * 默认精度（2位小数）
     */
    private static final int DEFAULT_SCALE = 2;

    /**
     * 格式化金额（保留2位小数）
     */
    public static BigDecimal format(BigDecimal amount) {
        if (amount == null) {
            return new BigDecimal("0.00");
        }
        return amount.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 验证金额是否有效（大于0）
     */
    public static boolean isValid(BigDecimal amount) {
        return amount != null && amount.compareTo(new BigDecimal("0.00")) > 0;
    }

    /**
     * 验证金额是否足够
     */
    public static boolean isEnough(BigDecimal balance, BigDecimal required) {
        if (balance == null || required == null) {
            return false;
        }
        return balance.compareTo(required) >= 0;
    }

    /**
     * 计算总价（单价 * 数量）
     */
    public static BigDecimal calculateTotal(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null || quantity <= 0) {
            return new BigDecimal("0.00");
        }
        return format(unitPrice.multiply(new BigDecimal(quantity)));
    }

    /**
     * 比较金额是否相等（考虑精度）
     */
    public static boolean equals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return format(a).compareTo(format(b)) == 0;
    }
}