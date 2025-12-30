package com.shopping.utils;

import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;

import java.util.Collection;

/**
 * 验证工具类
 */
public class Validator {

    /**
     * 验证对象不为空
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }

    /**
     * 验证字符串不为空
     */
    public static void notBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }

    /**
     * 验证集合不为空
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }

    /**
     * 验证数字大于0
     */
    public static void positive(Number number, String message) {
        if (number == null || number.doubleValue() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }

    /**
     * 验证条件为真
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }

    /**
     * 验证条件为假
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
        }
    }
}