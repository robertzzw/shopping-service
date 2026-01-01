package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.ProductSku;
import com.shopping.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 用户Mapper接口
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
    User selectUserForUpdate(@Param("userId") Long userId);
    /** 扣减用户账户余额 */
    Integer deductUserBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /** 使用存储过程更新用户余额, 并返回更新后的余额 */
    User updateBalanceAndReturn(Long userId, BigDecimal amount, String operationType);
}
