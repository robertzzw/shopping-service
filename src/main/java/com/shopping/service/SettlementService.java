package com.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.dto.response.SettlementDetail;
import com.shopping.dto.response.SettlementResult;
import com.shopping.entity.DailySettlement;
import com.shopping.entity.ProductSku;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 结算服务接口 */
public interface SettlementService extends IService<DailySettlement> {
    
    /** 执行每日结算 */
    SettlementResult executeDailySettlement(LocalDate settlementDate);
    
    /** 根据ID查询结算记录 */
    DailySettlement findById(Long settlementId);
    /** 重新结算 */
    SettlementDetail resettlement(Long merchantId, LocalDate settlementDate);
    
    /** 查询商家结算记录 */
    List<DailySettlement> findByMerchantId(Long merchantId);
    
    /** 查询指定日期的结算记录 */
    DailySettlement findByMerchantAndDate(Long merchantId, LocalDate settlementDate);
    
    /** 分页查询结算记录 */
    Page<DailySettlement> findPage(int pageNum, int pageSize, Long merchantId, LocalDate startDate, LocalDate endDate);

    /** 查询结算详情 */
    SettlementDetail findDetailById(Long settlementId);
    
    /** 计算指定日期的销售额 */
    BigDecimal calculateSalesAmount(Long merchantId, LocalDate settlementDate);

}