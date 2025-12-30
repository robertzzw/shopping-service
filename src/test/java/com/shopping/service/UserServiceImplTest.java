package com.shopping.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shopping.constant.AccountType;
import com.shopping.constant.StatusEnum;
import com.shopping.constant.TransactionType;
import com.shopping.dto.request.UserRechargeRequest;
import com.shopping.dto.response.UserResponse;
import com.shopping.entity.AccountTransaction;
import com.shopping.entity.User;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.UserMapper;
import com.shopping.utils.MoneyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private AccountTransactionService accountTransactionService;
    @InjectMocks
    private UserServiceImpl userService;
    private User validUser;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setUserId(1L);
        validUser.setUsername("testuser");
        validUser.setEmail("test@example.com");
        validUser.setPhone("13800138000");
        validUser.setAccountBalance(new BigDecimal("100.00"));
        validUser.setUserType(1);
        validUser.setStatus(StatusEnum.ENABLED.getCode());
        validUser.setCreatedTime(LocalDateTime.now());
        validUser.setUpdatedTime(LocalDateTime.now());

        disabledUser = new User();
        disabledUser.setUserId(2L);
        disabledUser.setUsername("disableduser");
        disabledUser.setAccountBalance(new BigDecimal("50.00"));
        disabledUser.setStatus(StatusEnum.DISABLED.getCode());
    }

    @Nested
    @DisplayName("getUserById tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when user exists")
        void getUserById_success() {
            when(userMapper.selectById(1L)).thenReturn(validUser);

            User result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("testuser", result.getUsername());
            verify(userMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("should throw exception when user id is null")
        void getUserById_nullUserId() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.getUserById(null);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("用户ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserById_userNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.getUserById(999L);
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
            verify(userMapper, times(1)).selectById(999L);
        }
    }

    @Nested
    @DisplayName("getUserByUsername tests")
    class GetUserByUsernameTests {

        @Test
        @DisplayName("should return user when username exists")
        void getUserByUsername_success() {
            when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(validUser);

            User result = userService.getUserByUsername("testuser");

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
        }

        @Test
        @DisplayName("should throw exception when username is null")
        void getUserByUsername_nullUsername() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.getUserByUsername(null);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("用户名不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when username is empty")
        void getUserByUsername_emptyUsername() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.getUserByUsername("");
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("用户名不能为空", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("createUser tests")
    class CreateUserTests {

        @Test
        @DisplayName("should create user successfully with default values")
        void createUser_successWithDefaults() {
            User newUser = new User();
            newUser.setUsername("newuser");
            newUser.setEmail("new@example.com");
            newUser.setPhone("13900139000");

            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            User createdUser = userService.createUser(newUser);

            assertNotNull(createdUser);
            assertEquals("newuser", createdUser.getUsername());
            assertEquals(new BigDecimal("0.00"), createdUser.getAccountBalance());
            assertEquals(1, createdUser.getUserType());
            assertEquals(StatusEnum.ENABLED.getCode(), createdUser.getStatus());
            verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
            verify(userMapper, times(1)).insert(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when user info is null")
        void createUser_nullUser() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(null);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("用户信息不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void createUser_usernameExists() {
            User existingUser = new User();
            existingUser.setUsername("existinguser");

            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(existingUser);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("用户名已存在", exception.getMessage());
            verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        }

        @Test
        @DisplayName("should throw exception when save fails")
        void createUser_saveFailed() {
            User newUser = new User();
            newUser.setUsername("newuser");

            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(userMapper.insert(any(User.class))).thenReturn(0);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.createUser(newUser);
            });

            assertEquals(ErrorCode.OPERATION_FAILED.getCode(), exception.getCode());
            assertEquals("创建用户失败", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("recharge tests")
    class RechargeTests {

        @Test
        @DisplayName("should recharge successfully")
        void recharge_success() throws Exception {
            try (MockedStatic<IdUtil> mockedIdUtil = mockStatic(IdUtil.class)) {
                UserRechargeRequest request = new UserRechargeRequest();
                request.setUserId(1L);
                request.setAmount(new BigDecimal("50.00"));

                User updatedUser = new User();
                updatedUser.setUserId(1L);
                updatedUser.setAccountBalance(new BigDecimal("150.00"));

                when(userMapper.selectById(1L)).thenReturn(validUser);
                when(userMapper.updateById(any(User.class))).thenReturn(1);
                when(accountTransactionService.saveTransaction(any(AccountTransaction.class))).thenReturn(true);
                mockedIdUtil.when(IdUtil::getSnowflakeNextId).thenReturn(12345L);

                UserResponse response = userService.recharge(request);

                assertNotNull(response);
                assertEquals(new BigDecimal("150.00"), response.getNewBalance());
                assertEquals(new BigDecimal("100.00"), response.getOldBalance());
                assertEquals(new BigDecimal("50.00"), response.getRechargeAmount());

                verify(userMapper, times(1)).updateById(any(User.class));
                verify(accountTransactionService, times(1)).saveTransaction(any(AccountTransaction.class));
            }
        }

        @Test
        @DisplayName("should throw exception when request is null")
        void recharge_nullRequest() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(null);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("充值参数不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user id is null")
        void recharge_nullUserId() {
            UserRechargeRequest request = new UserRechargeRequest();
            request.setAmount(new BigDecimal("50.00"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(request);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("充值参数不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when amount is null")
        void recharge_nullAmount() {
            UserRechargeRequest request = new UserRechargeRequest();
            request.setUserId(1L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(request);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("充值参数不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when amount is zero or negative")
        void recharge_invalidAmount() {
            UserRechargeRequest request = new UserRechargeRequest();
            request.setUserId(1L);
            request.setAmount(new BigDecimal("-10.00"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(request);
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("充值金额必须大于0", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void recharge_userNotFound() {
            UserRechargeRequest request = new UserRechargeRequest();
            request.setUserId(999L);
            request.setAmount(new BigDecimal("50.00"));

            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(request);
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user is disabled")
        void recharge_userDisabled() {
            UserRechargeRequest request = new UserRechargeRequest();
            request.setUserId(2L);
            request.setAmount(new BigDecimal("50.00"));

            when(userMapper.selectById(2L)).thenReturn(disabledUser);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.recharge(request);
            });

            assertEquals(ErrorCode.USER_DISABLED.getCode(), exception.getCode());
            assertEquals("用户已被禁用", exception.getMessage());
        }


        @Test
        @DisplayName("should throw exception when update balance fails")
        void recharge_updateBalanceFailed() throws Exception {
            try (MockedStatic<IdUtil> mockedIdUtil = mockStatic(IdUtil.class)) {
                UserRechargeRequest request = new UserRechargeRequest();
                request.setUserId(1L);
                request.setAmount(new BigDecimal("50.00"));

                when(userMapper.selectById(1L)).thenReturn(validUser);
                when(userMapper.updateById(any(User.class))).thenReturn(0); // 更新失败，返回0
                mockedIdUtil.when(IdUtil::getSnowflakeNextId).thenReturn(12345L);

                BusinessException exception = assertThrows(BusinessException.class, () -> {
                    userService.recharge(request);
                });

                assertEquals(ErrorCode.OPERATION_FAILED.getCode(), exception.getCode());
                assertEquals("更新用户余额失败", exception.getMessage());
                verify(userMapper, times(1)).updateById(any(User.class));
            }
        }

        @Test
        @DisplayName("should throw exception when save transaction fails")
        void recharge_saveTransactionFailed() throws Exception {
            try (MockedStatic<IdUtil> mockedIdUtil = mockStatic(IdUtil.class)) {
                UserRechargeRequest request = new UserRechargeRequest();
                request.setUserId(1L);
                request.setAmount(new BigDecimal("50.00"));

                User updatedUser = new User();
                updatedUser.setUserId(1L);
                updatedUser.setAccountBalance(new BigDecimal("150.00"));

                when(userMapper.selectById(1L)).thenReturn(validUser);
                when(userMapper.updateById(any(User.class))).thenReturn(1);
                when(accountTransactionService.saveTransaction(any(AccountTransaction.class))).thenReturn(false);
                mockedIdUtil.when(IdUtil::getSnowflakeNextId).thenReturn(12345L);

                BusinessException exception = assertThrows(BusinessException.class, () -> {
                    userService.recharge(request);
                });

                assertEquals(ErrorCode.OPERATION_FAILED.getCode(), exception.getCode());
                assertEquals("记录交易流水失败", exception.getMessage());
                verify(accountTransactionService, times(1)).saveTransaction(any(AccountTransaction.class));
            }
        }
    }

    @Nested
    @DisplayName("getBalance tests")
    class GetBalanceTests {

        @Test
        @DisplayName("should get balance successfully")
        void getBalance_success() {
            when(userMapper.selectById(1L)).thenReturn(validUser);

            BigDecimal balance = userService.getBalance(1L);

            assertEquals(new BigDecimal("100.00"), balance);
            verify(userMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getBalance_userNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.getBalance(999L);
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateUserBalance tests")
    class UpdateUserBalanceTests {

        @Test
        @DisplayName("should increase balance successfully")
        void updateUserBalance_increaseSuccess() {
            User updatedUser = new User();
            updatedUser.setUserId(1L);
            updatedUser.setAccountBalance(new BigDecimal("150.00"));
            updatedUser.setResultMessage("success");

            when(userMapper.selectById(1L)).thenReturn(validUser);
            when(userMapper.updateBalanceAndReturn(eq(1L), eq(new BigDecimal("50.00")), eq("increase"))).thenReturn(updatedUser);
            when(accountTransactionService.createTransaction(anyInt(), anyLong(), anyInt(), any(), any(), any(), anyLong(), anyString(), anyString()))
                    .thenReturn(new AccountTransaction());
            when(accountTransactionService.saveTransaction(any(AccountTransaction.class))).thenReturn(true);

            User result = userService.updateUserBalance(
                    1L,
                    new BigDecimal("50.00"),
                    "increase",
                    1001L,
                    TransactionType.CONSUME.getCode(),
                    "测试增加余额"
            );

            assertNotNull(result);
            assertEquals(new BigDecimal("150.00"), result.getAccountBalance());
            verify(userMapper, times(1)).updateBalanceAndReturn(eq(1L), eq(new BigDecimal("50.00")), eq("increase"));
            verify(accountTransactionService, times(1)).createTransaction(anyInt(), anyLong(), anyInt(), any(), any(), any(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("should decrease balance successfully")
        void updateUserBalance_decreaseSuccess() {
            User updatedUser = new User();
            updatedUser.setUserId(1L);
            updatedUser.setAccountBalance(new BigDecimal("50.00"));
            updatedUser.setResultMessage("success");

            when(userMapper.selectById(1L)).thenReturn(validUser);
            when(userMapper.updateBalanceAndReturn(eq(1L), eq(new BigDecimal("50.00")), eq("decrease"))).thenReturn(updatedUser);
            when(accountTransactionService.createTransaction(anyInt(), anyLong(), anyInt(), any(), any(), any(), anyLong(), anyString(), anyString()))
                    .thenReturn(new AccountTransaction());
            when(accountTransactionService.saveTransaction(any(AccountTransaction.class))).thenReturn(true);

            User result = userService.updateUserBalance(
                    1L,
                    new BigDecimal("50.00"),
                    "decrease",
                    1001L,
                    TransactionType.CONSUME.getCode(),
                    "测试减少余额"
            );

            assertNotNull(result);
            assertEquals(new BigDecimal("50.00"), result.getAccountBalance());
            verify(userMapper, times(1)).updateBalanceAndReturn(eq(1L), eq(new BigDecimal("50.00")), eq("decrease"));
            verify(accountTransactionService, times(1)).createTransaction(anyInt(), anyLong(), anyInt(), any(), any(), any(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw exception when user id is null")
        void updateUserBalance_nullUserId() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        null,
                        new BigDecimal("50.00"),
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("参数不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when amount is null")
        void updateUserBalance_nullAmount() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        1L,
                        null,
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("参数不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when amount is negative")
        void updateUserBalance_negativeAmount() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        1L,
                        new BigDecimal("-10.00"),
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("金额不能为负数", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when operation type is invalid")
        void updateUserBalance_invalidOperationType() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        1L,
                        new BigDecimal("50.00"),
                        "invalid",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
            assertEquals("参数错误, 更新操作类型需要是 decrease 或 increase", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void updateUserBalance_userNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        999L,
                        new BigDecimal("50.00"),
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user is disabled")
        void updateUserBalance_userDisabled() {
            when(userMapper.selectById(2L)).thenReturn(disabledUser);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        2L,
                        new BigDecimal("50.00"),
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.USER_DISABLED.getCode(), exception.getCode());
            assertEquals("用户已被禁用", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when balance is not enough")
        void updateUserBalance_notEnoughBalance() {
            try (MockedStatic<MoneyUtil> mockedMoneyUtil = mockStatic(MoneyUtil.class)) {
                when(userMapper.selectById(1L)).thenReturn(validUser);
                mockedMoneyUtil.when(() -> MoneyUtil.isEnough(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(false);

                BusinessException exception = assertThrows(BusinessException.class, () -> {
                    userService.updateUserBalance(
                            1L,
                            new BigDecimal("150.00"),
                            "decrease",
                            1001L,
                            TransactionType.CONSUME.getCode(),
                            "测试"
                    );
                });

                assertEquals(ErrorCode.BALANCE_NOT_ENOUGH.getCode(), exception.getCode());
                assertEquals("用户余额不足", exception.getMessage());
            }
        }

        @Test
        @DisplayName("should throw exception when update fails")
        void updateUserBalance_updateFailed() {
            when(userMapper.selectById(1L)).thenReturn(validUser);
            when(userMapper.updateBalanceAndReturn(eq(1L), eq(new BigDecimal("50.00")), eq("increase"))).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        1L,
                        new BigDecimal("50.00"),
                        "increase",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("扣减用户余额失败, user为null", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when balance not enough after update")
        void updateUserBalance_notEnoughAfterUpdate() {
            User updatedUser = new User();
            updatedUser.setUserId(1L);
            updatedUser.setAccountBalance(new BigDecimal("40.00"));
            updatedUser.setResultMessage("failure");

            when(userMapper.selectById(1L)).thenReturn(validUser);
            when(userMapper.updateBalanceAndReturn(eq(1L), eq(new BigDecimal("60.00")), eq("decrease"))).thenReturn(updatedUser);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUserBalance(
                        1L,
                        new BigDecimal("60.00"),
                        "decrease",
                        1001L,
                        TransactionType.CONSUME.getCode(),
                        "测试"
                );
            });

            assertEquals(ErrorCode.BALANCE_NOT_ENOUGH.getCode(), exception.getCode());
            assertTrue(exception.getMessage().startsWith("扣减用户余额失败,当前余额:"));
        }
    }

    @Nested
    @DisplayName("validateUserStatus tests")
    class ValidateUserStatusTests {

        @Test
        @DisplayName("should validate successfully for enabled user")
        void validateUserStatus_enabledUser() {
            assertDoesNotThrow(() -> userService.validateUserStatus(validUser));
        }

        @Test
        @DisplayName("should throw exception when user is null")
        void validateUserStatus_nullUser() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.validateUserStatus(null);
            });

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception when user is disabled")
        void validateUserStatus_disabledUser() {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.validateUserStatus(disabledUser);
            });

            assertEquals(ErrorCode.USER_DISABLED.getCode(), exception.getCode());
            assertEquals("用户已被禁用", exception.getMessage());
        }
    }
}