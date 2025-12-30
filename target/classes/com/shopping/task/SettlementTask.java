package com.shopping.task;

import com.shopping.dto.response.SettlementResult;
import com.shopping.service.SettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/** 结算定时任务 */
@Slf4j
@Component
public class SettlementTask {

    @Autowired
    private SettlementService settlementService;
    
    @Value("${settlement.enabled:true}")
    private boolean settlementEnabled;
    
    @Value("${settlement.cron:0 0 0 * * ?}")
    private String cronExpression;
    
    /** 每日结算任务 */
    @Scheduled(cron = "${settlement.cron:0 0 0 * * ?}")
    public void executeDailySettlement() {
        if (!settlementEnabled) {
            log.info("结算定时任务已禁用");
            return;
        }
        
        try {
            log.info("开始执行每日结算定时任务");
            LocalDate settlementDate = LocalDate.now().minusDays(1); // 结算昨天
            SettlementResult result = settlementService.executeDailySettlement(settlementDate);
            if (result.getAllSuccess()) {
                log.info("每日结算定时任务执行成功，结算日期: {}", settlementDate);
            } else {
                log.error("每日结算定时任务执行失败，结算日期: {}", settlementDate);
            }
        } catch (Exception e) {
            log.error("每日结算定时任务执行异常", e);
        }
    }
    
    /** 测试结算任务 */
    public void testSettlement(LocalDate settlementDate) {
        log.info("执行测试结算，日期: {}", settlementDate);
        try {
            SettlementResult result = settlementService.executeDailySettlement(settlementDate);
            log.info("测试结算结果: {}", result.getAllSuccess() ? "成功" : "失败");
        } catch (Exception e) {
            log.error("测试结算异常", e);
        }
    }
}