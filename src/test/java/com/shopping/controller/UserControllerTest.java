package com.shopping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shopping.dto.request.UserRechargeRequest;
import com.shopping.dto.response.UserResponse;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private UserService userService;
    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化验证器
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        // 确保MockMvc正确配置
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print()) // 打印请求/响应详情，便于调试
                .build();
    }

    @Test
    @DisplayName("用户充值 - 成功")
    void recharge_Success() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("100.00"));
        request.setRemark("测试充值");
        // 准备响应数据
        UserResponse response = new UserResponse();
        response.setUserId(1L);
        response.setUsername("testUser");
        response.setAccountBalance(new BigDecimal("200.00"));
        response.setRechargeAmount(new BigDecimal("100.00"));
        response.setOldBalance(new BigDecimal("100.00"));
        response.setNewBalance(new BigDecimal("200.00"));

        // Mock服务层调用
        when(userService.recharge(any(UserRechargeRequest.class))).thenReturn(response);

        // 执行请求并验证
        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data.userId", is(1)))
                .andExpect(jsonPath("$.data.rechargeAmount", is(100.00)))
                .andExpect(jsonPath("$.data.newBalance", is(200.00)));

        // 验证服务层调用
        verify(userService, times(1)).recharge(any(UserRechargeRequest.class));
    }

    @Test
    @DisplayName("用户充值 - 参数验证失败 - 用户ID为空")
    void recharge_Fail_UserIdNull() throws Exception {
        // 准备请求数据（用户ID为空）
        UserRechargeRequest request = new UserRechargeRequest();
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())))
                .andExpect(jsonPath("$.message", containsString("用户ID不能为空")));
    }

    @Test
    @DisplayName("用户充值 - 参数验证失败 - 金额为空")
    void recharge_Fail_AmountNull() throws Exception {
        // 准备请求数据（金额为空）
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())))
                .andExpect(jsonPath("$.message", containsString("充值金额不能为空")));
    }

    @Test
    @DisplayName("用户充值 - 参数验证失败 - 金额小于等于0")
    void recharge_Fail_AmountInvalid() throws Exception {
        // 测试金额为0
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("0.00"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())))
                .andExpect(jsonPath("$.message", containsString("充值金额必须大于0")));
    }

    @Test
    @DisplayName("用户充值 - 业务异常 - 用户不存在")
    void recharge_Fail_UserNotFound() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(999L);
        request.setAmount(new BigDecimal("100.00"));

        // Mock业务异常 - 修正为返回ApiResponse.error
        when(userService.recharge(any(UserRechargeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // 业务异常返回200
                .andExpect(jsonPath("$.code", is(ErrorCode.NOT_FOUND_ERROR.getCode())))
                .andExpect(jsonPath("$.message", is("用户不存在")));
    }

    @Test
    @DisplayName("用户充值 - 业务异常 - 用户被禁用")
    void recharge_Fail_UserDisabled() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(2L);
        request.setAmount(new BigDecimal("100.00"));

        // Mock业务异常
        when(userService.recharge(any(UserRechargeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.USER_DISABLED));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_DISABLED.getCode())))
                .andExpect(jsonPath("$.message", is(ErrorCode.USER_DISABLED.getMessage())));
    }

    @Test
    @DisplayName("用户充值 - 业务异常 - 操作失败")
    void recharge_Fail_OperationFailed() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("100.00"));

        // Mock业务异常
        when(userService.recharge(any(UserRechargeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户余额失败"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ErrorCode.OPERATION_FAILED.getCode())))
                .andExpect(jsonPath("$.message", is("更新用户余额失败")));
    }

    @Test
    @DisplayName("查询用户余额 - 成功")
    void getBalance_Success() throws Exception {
        Long userId = 1L;
        BigDecimal balance = new BigDecimal("500.00");

        // Mock服务层调用
        when(userService.getBalance(userId)).thenReturn(balance);

        mockMvc.perform(get("/users/{userId}/balance", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data", is(500.00)));

        verify(userService, times(1)).getBalance(userId);
    }

    @Test
    @DisplayName("查询用户余额 - 业务异常 - 用户不存在")
    void getBalance_Fail_UserNotFound() throws Exception {
        Long userId = 999L;

        // Mock业务异常
        when(userService.getBalance(userId))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在"));

        mockMvc.perform(get("/users/{userId}/balance", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ErrorCode.NOT_FOUND_ERROR.getCode())))
                .andExpect(jsonPath("$.message", is("用户不存在")));
    }

    @Test
    @DisplayName("查询用户余额 - 路径变量格式错误 - 应该返回系统异常")
    void getBalance_Fail_InvalidPathVariable() throws Exception {
        // 测试非数字用户ID - 会抛出MethodArgumentTypeMismatchException，被全局异常处理器捕获为系统异常
        mockMvc.perform(get("/users/{userId}/balance", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // 500错误，系统异常
                .andExpect(jsonPath("$.code", is(ErrorCode.SYSTEM_ERROR.getCode())));
    }

    @Test
    @DisplayName("用户充值 - 边界值测试 - 最小充值金额0.01")
    void recharge_Boundary_MinAmount() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("0.01")); // 最小充值金额
        request.setRemark("最小充值测试");

        // 准备响应数据
        UserResponse response = new UserResponse();
        response.setUserId(1L);
        response.setAccountBalance(new BigDecimal("0.01"));
        response.setRechargeAmount(new BigDecimal("0.01"));
        response.setOldBalance(new BigDecimal("0.00"));
        response.setNewBalance(new BigDecimal("0.01"));

        // Mock服务层调用
        when(userService.recharge(any(UserRechargeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.rechargeAmount", is(0.01)))
                .andExpect(jsonPath("$.data.newBalance", is(0.01)));
    }

    @Test
    @DisplayName("用户充值 - 异常情况 - 服务层抛出运行时异常")
    void recharge_Fail_RuntimeException() throws Exception {
        // 准备请求数据
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("100.00"));

        // Mock服务层抛出运行时异常
        when(userService.recharge(any(UserRechargeRequest.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()) // 500错误
                .andExpect(jsonPath("$.code", is(ErrorCode.SYSTEM_ERROR.getCode())))
                .andExpect(jsonPath("$.message", is("系统异常，请稍后重试")));
    }

    @Test
    @DisplayName("用户充值 - 金额为负数")
    void recharge_Fail_NegativeAmount() throws Exception {
        // 测试金额为负数
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())))
                .andExpect(jsonPath("$.message", containsString("充值金额必须大于0")));
    }

    @Test
    @DisplayName("用户充值 - 金额太大（超出精度）")
    void recharge_Fail_TooLargeAmount() throws Exception {
        // 测试非常大的金额
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("999999999999999.99"));

        // 假设服务层可以处理，正常响应
        UserResponse response = new UserResponse();
        response.setUserId(1L);
        response.setAccountBalance(new BigDecimal("999999999999999.99"));
        response.setRechargeAmount(new BigDecimal("999999999999999.99"));
        response.setOldBalance(new BigDecimal("0.00"));
        response.setNewBalance(new BigDecimal("999999999999999.99"));

        when(userService.recharge(any(UserRechargeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("用户充值 - 验证异常处理器")
    void recharge_ValidationExceptionHandler() throws Exception {
        // 准备请求数据，故意不设置任何字段
        UserRechargeRequest request = new UserRechargeRequest();

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())));
    }

    @Test
    @DisplayName("用户充值 - 空请求体")
    void recharge_Fail_EmptyBody() throws Exception {
        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户充值 - 测试JSON解析异常,抛出HttpMessageNotReadableException")
    void recharge_Fail_InvalidJson() throws Exception {
        // 发送无效的JSON数据
        String invalidJson = "{ invalid json }";
        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertThat(exception)
                            .isInstanceOf(HttpMessageNotReadableException.class)
                            .hasMessageContaining("JSON parse error");
                });
    }

    @Test
    @DisplayName("用户充值 - 测试HttpMediaTypeNotSupportedException")
    void recharge_Fail_InvalidContentType() throws Exception {
        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("100.00"));
        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.TEXT_PLAIN) // 错误的内容类型
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertThat(exception)
                            .isInstanceOf(HttpMediaTypeNotSupportedException.class)
                            .hasMessageContaining("Content type 'text/plain' not supported");
                });
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRechargeRequests")
    @DisplayName("用户充值 - 参数化测试 - 各种无效参数")
    void recharge_ParameterizedInvalidRequests(
            String testName,
            Long userId,
            BigDecimal amount) throws Exception {

        UserRechargeRequest request = new UserRechargeRequest();
        request.setUserId(userId);
        request.setAmount(amount);

        mockMvc.perform(post("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.PARAMS_ERROR.getCode())));
    }

    private static Stream<Object[]> provideInvalidRechargeRequests() {
        return Stream.of(
                new Object[]{"用户ID为null", null, new BigDecimal("100.00")},
                new Object[]{"金额为null", 1L, null},
                new Object[]{"金额为0", 1L, new BigDecimal("0.00")},
                new Object[]{"金额为负数", 1L, new BigDecimal("-50.00")}
        );
    }

    @Test
    @DisplayName("查询用户余额 - 用户ID为0")
    void getBalance_UserIdZero() throws Exception {
        Long userId = 0L;
        BigDecimal balance = new BigDecimal("0.00");

        // Mock服务层调用
        when(userService.getBalance(userId)).thenReturn(balance);

        mockMvc.perform(get("/users/{userId}/balance", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data", is(0.00)));

        verify(userService, times(1)).getBalance(userId);
    }

    @Test
    @DisplayName("查询用户余额 - 用户ID为负数")
    void getBalance_UserIdNegative() throws Exception {
        Long userId = -1L;

        // Mock业务异常 - 负数ID用户不存在
        when(userService.getBalance(userId))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在"));

        mockMvc.perform(get("/users/{userId}/balance", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ErrorCode.NOT_FOUND_ERROR.getCode())));
    }

    @Test
    @DisplayName("测试404路径")
    void testNotFoundPath() throws Exception {
        mockMvc.perform(get("/users/nonexistent/path")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("测试http请求方法不支持异常, HttpRequestMethodNotSupportedException")
    void testMethodNotAllowed() throws Exception {
        // 尝试使用PUT方法访问充值接口
        mockMvc.perform(put("/users/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(put("/users/recharge")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString("{}")))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertThat(exception)
                            .isInstanceOf(HttpRequestMethodNotSupportedException.class)
                            .hasMessageContaining("Request method 'PUT' not supported");
                });
    }

    @Test
    @DisplayName("测试Controller的健康检查")
    void testControllerHealth() throws Exception {
        // 简单的健康检查，确保Controller能够响应
        mockMvc.perform(get("/users/1/balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn(); // 不验证具体结果，只确保不抛异常
    }
}