package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.AccountTransaction;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账户交易记录Mapper接口
 */
@Repository
public interface AccountTransactionMapper extends BaseMapper<AccountTransaction> {
    BigDecimal calculateMerchantNetIncome(
            @Param("accountId") Long accountId,
            @Param("date") LocalDate date
    );
}