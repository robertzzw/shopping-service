package com.shopping.controller;

import com.shopping.dto.request.UserRechargeRequest;
import com.shopping.dto.response.ApiResponse;
import com.shopping.dto.response.UserResponse;
import com.shopping.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@Api(tags = "用户管理")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/recharge")
    @ApiOperation("用户充值")
    public ApiResponse<UserResponse> recharge(@Valid @RequestBody UserRechargeRequest request) {
        log.info("用户充值请求: {}", request);
        UserResponse response = userService.recharge(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}/balance")
    @ApiOperation("查询用户余额")
    public ApiResponse<Object> getBalance(@PathVariable Long userId) {
        log.info("查询用户余额: userId={}", userId);
        return ApiResponse.success(userService.getBalance(userId));
    }
}