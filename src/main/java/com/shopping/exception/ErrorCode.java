package com.shopping.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    SUCCESS(0, "成功"),
    
    // 通用错误码
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND_ERROR(40004, "请求数据不存在"),
    FORBIDDEN_ERROR(40003, "禁止访问"),
    UNAUTHORIZED_ERROR(40001, "未授权"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_FAILED(50001, "操作失败"),
    
    // 业务错误码
    USER_DISABLED(41001, "用户已被禁用"),
    MERCHANT_DISABLED(41002, "商家已被停用"),
    PRODUCT_DISABLED(41003, "商品已下架"),
    SKU_NOT_AVAILABLE(41004, "商品SKU不可用"),
    BALANCE_NOT_ENOUGH(41005, "余额不足"),
    STOCK_NOT_ENOUGH(41006, "库存不足"),
    ORDER_STATUS_ERROR(41007, "订单状态异常"),
    SETTLEMENT_FAILED(41008, "结算失败"),
    
    // 重复操作
    DUPLICATE_OPERATION(42001, "重复操作");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}