package com.shopping.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.entity.AccountTransaction;
import com.shopping.mapper.AccountTransactionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户交易记录服务实现
 */
@Service
public class AccountTransactionServiceImpl extends ServiceImpl<AccountTransactionMapper, AccountTransaction> 
    implements AccountTransactionService {

    @Override
    public boolean saveTransaction(AccountTransaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        // 设置创建时间
        if (transaction.getCreatedTime() == null) {
            transaction.setCreatedTime(LocalDateTime.now());
        }
        
        return this.save(transaction);
    }

    @Override
    public AccountTransaction createTransaction(Integer accountType, Long accountId, 
                                              Integer transactionType, BigDecimal balanceBefore,
                                              BigDecimal amount, BigDecimal balanceAfter,
                                              Long relatedId, String relatedType, String remark) {
        
        AccountTransaction transaction = new AccountTransaction();
        transaction.setTransactionNo("TRX" + IdUtil.getSnowflakeNextId());
        transaction.setAccountType(accountType);
        transaction.setAccountId(accountId);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedId(relatedId);
        transaction.setRelatedType(relatedType);
        transaction.setRemark(remark);
        transaction.setCreatedTime(LocalDateTime.now());
        
        return transaction;
    }
}