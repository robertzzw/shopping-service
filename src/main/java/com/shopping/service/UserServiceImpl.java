package com.shopping.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.AccountType;
import com.shopping.constant.StatusEnum;
import com.shopping.constant.TransactionType;
import com.shopping.dto.request.UserRechargeRequest;
import com.shopping.dto.response.UserResponse;
import com.shopping.entity.AccountTransaction;
import com.shopping.entity.ProductSku;
import com.shopping.entity.User;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.UserMapper;
import com.shopping.utils.MoneyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private AccountTransactionService accountTransactionService;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        User user = this.getById(userId);
//        User user = userMapper.selectUserForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }

        // 验证用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }

        // 设置默认值
        if (user.getAccountBalance() == null) {
            user.setAccountBalance(new BigDecimal("0.00"));
        }
        if (user.getUserType() == null) {
            user.setUserType(1); // 默认为普通用户
        }
        if (user.getStatus() == null) {
            user.setStatus(StatusEnum.ENABLED.getCode());
        }

        boolean success = this.save(user);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建用户失败");
        }

        log.info("创建用户成功，用户ID: {}", user.getUserId());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse recharge(UserRechargeRequest request) {
        // 参数验证
        if (request == null || request.getUserId() == null || request.getAmount() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值参数不能为空");
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(new BigDecimal("0.00")) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值金额必须大于0");
        }
        // 获取用户
        User user = this.getUserById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 验证用户状态
        validateUserStatus(user);
        // 原余额
        BigDecimal oldBalance = user.getAccountBalance();
        // 计算新余额
        BigDecimal newBalance = oldBalance.add(amount);
        
        // 更新用户余额
        //todo 添加余额,后续可以优化, 可以使用乐观锁/for update加锁, 或Redis锁等, 防止并发更新覆盖
        user.setAccountBalance(newBalance);
        boolean updateSuccess = this.updateById(user);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户余额失败");
        }

        // 记录交易流水
        AccountTransaction transaction = new AccountTransaction();
        transaction.setTransactionNo("TRX" + IdUtil.getSnowflakeNextId());
        transaction.setAccountType(AccountType.USER_ACCOUNT.getCode());
        transaction.setAccountId(user.getUserId());
        transaction.setTransactionType(TransactionType.RECHARGE.getCode());
        transaction.setAmount(amount);
        transaction.setBalanceBefore(oldBalance);
        transaction.setBalanceAfter(newBalance);
        transaction.setRelatedType("RECHARGE");
        transaction.setRemark("用户充值");
        transaction.setCreatedTime(LocalDateTime.now());

        boolean saveSuccess = accountTransactionService.saveTransaction(transaction);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "记录交易流水失败");
        }

        log.info("用户充值成功，用户ID: {}, 充值金额: {}, 原余额: {}, 新余额: {}",
                user.getUserId(), amount, oldBalance, newBalance);

        // 构建响应
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setRechargeAmount(amount);
        response.setOldBalance(oldBalance);
        response.setNewBalance(newBalance);

        return response;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        User user = this.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user.getAccountBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUserBalance(Long userId, BigDecimal amount, String operationType,
                                     Long relatedId, Integer transactionType, String remark) {
        if (userId == null || amount == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (amount.compareTo(new BigDecimal("0.00")) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "金额不能为负数");
        }
        if ((operationType != "decrease" && operationType != "increase")){
            log.info("更新操作类型需要是 decrease 或 increase");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误, 更新操作类型需要是 decrease 或 increase");
        }
        // 验证用户
        User user = this.getUserById(userId);
        this.validateUserStatus(user);
        // 验证用户余额
        if (!MoneyUtil.isEnough(user.getAccountBalance(), amount)) {
            throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH, "用户余额不足");
        }

        user = userMapper.updateBalanceAndReturn(userId, amount, operationType);
        if (user == null) {
            log.info("扣减用户余额失败, user为null");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "扣减用户余额失败, user为null");
        }
        BigDecimal currentBalance = user.getAccountBalance();
        if ("failure".equals(user.getResultMessage())) {
            if (amount.compareTo(currentBalance) > 0) {
                log.info("用户余额不足, userId:{}, 当前余额:{}, 需要金额:{}", userId, currentBalance, amount);
            }
            throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH,
                    "扣减用户余额失败,当前余额:" + currentBalance + ",需要金额:" + amount);
        }
        if ("success".equals(user.getResultMessage())) {
            //记录交易流水时,如果是扣减操作, 获取金额的负数来保存
            if ("decrease".equals(operationType)){
                amount = amount.negate();
            }
            // 记录用户交易流水
            AccountTransaction userTransaction = accountTransactionService.createTransaction(
                    AccountType.USER_ACCOUNT.getCode(),
                    user.getUserId(),
                    transactionType,
                    currentBalance.add(amount.negate()),
                    amount,
                    currentBalance,
                    relatedId,
                    "order_pay",
                    remark
            );
            accountTransactionService.saveTransaction(userTransaction);
            log.info("更新用户余额成功，用户ID: {}, 操作: {}, 金额: {}, 原余额: {}, 新余额: {}",
                    userId, operationType, amount, currentBalance.add(amount.negate()), currentBalance);
        }
        return user;
    }

    @Override
    public void validateUserStatus(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (!StatusEnum.ENABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被禁用");
        }
    }
}