package com.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.dto.request.UserRechargeRequest;
import com.shopping.dto.response.UserResponse;
import com.shopping.entity.ProductSku;
import com.shopping.entity.User;

import java.math.BigDecimal;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据ID获取用户
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 用户充值
     */
    UserResponse recharge(UserRechargeRequest request);

    /**
     * 获取用户余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 更新用户余额
     */
    User updateUserBalance(Long userId, BigDecimal amount, String operationType,
                              Long relatedId, Integer transactionType, String remark);


    /**
     * 验证用户状态
     */
    void validateUserStatus(User user);
}