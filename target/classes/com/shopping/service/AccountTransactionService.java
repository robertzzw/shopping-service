package com.shopping.service;

import com.shopping.entity.AccountTransaction;

import java.math.BigDecimal;

/**
 * 账户交易记录服务接口
 */
public interface AccountTransactionService {

    /**
     * 保存交易记录
     */
    boolean saveTransaction(AccountTransaction transaction);

    /**
     * 创建交易记录
     */
    AccountTransaction createTransaction(Integer accountType, Long accountId, 
                                        Integer transactionType, BigDecimal balanceBefore,
                                         BigDecimal amount, BigDecimal balanceAfter,
                                        Long relatedId, String relatedType, String remark);
}