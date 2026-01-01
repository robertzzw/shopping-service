package com.shopping.exception;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API错误信息
 */
@Data
public class ApiError {
    private int code;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    public ApiError(int code, String message, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}