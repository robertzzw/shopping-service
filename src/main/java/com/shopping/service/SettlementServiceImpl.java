package com.shopping.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.OrderStatus;
import com.shopping.dto.response.SettlementDetail;
import com.shopping.dto.response.SettlementResult;
import com.shopping.entity.DailySettlement;
import com.shopping.entity.Merchant;
import com.shopping.entity.Order;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.AccountTransactionMapper;
import com.shopping.mapper.DailySettlementMapper;
import com.shopping.mapper.OrdersMapper;
import com.shopping.utils.DateUtil;
import com.shopping.utils.IdGenerator;
import com.shopping.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 结算服务实现 */
@Slf4j
@Service
public class SettlementServiceImpl extends ServiceImpl<DailySettlementMapper, DailySettlement> implements SettlementService {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private AccountTransactionMapper transactionMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettlementResult executeDailySettlement(LocalDate settlementDate) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("执行每日结算: settlementDate={}", settlementDate);
        Validator.notNull(settlementDate, "结算日期不能为空");
        // 获取所有商家
        List<Merchant> merchants = merchantService.findAll();
        SettlementResult result = new SettlementResult();
        List<SettlementDetail> successDetails = new ArrayList<>();
        List<SettlementDetail> failDetails = new ArrayList<>();
        boolean allSuccess = true;
        int failCount = 0;
        int successCount = 0;
        for (Merchant merchant : merchants) {
            SettlementDetail detail = null;
            try {
                detail = settleMerchant(merchant.getMerchantId(), settlementDate);
                if (detail != null && detail.getIsSuccess()) {
                    successCount++;
                    successDetails.add(detail);
                }else {
                    failCount++;
                    failDetails.add(detail);
                    log.error("商家结算失败，商家ID: {}, 结算日期: {}", merchant.getMerchantId(), settlementDate);
                }
            } catch (Exception e) {
                failCount++;
                failDetails.add(detail);
                log.error("商家结算异常，商家ID: {}, 结算日期: {}, 异常: {}", merchant.getMerchantId(), settlementDate, e.getMessage(), e);
            }
        }
        if (successCount != merchants.size()){
            allSuccess = false;
        }
        result.setSettlementDate(settlementDate);
        result.setAllSuccess(allSuccess);
        result.setTotalMerchants(merchants.size());
        result.setSuccessCount(successCount);
        result.setFailedCount(failCount);
        result.setSuccessDetails(successDetails);
        result.setFailDetails(failDetails);

        result.setStartTime(startTime);
        LocalDateTime endTime = LocalDateTime.now();
        result.setEndTime(endTime);
        Duration duration = Duration.between(startTime, endTime);
        long millisDiff = duration.toMillis();  // 总毫秒数
        double secondsDiff = millisDiff / 1000.0;  // 转换为秒
        String costTime = String.format("%.3f", secondsDiff) + "秒"; //保留3位小数
        result.setExecutionTime(costTime);

        log.info("每日结算执行完成，成功商家数: {}, 失败商家数: {}", successCount, failCount);
        log.info("耗时: " + costTime);
        return result;
    }

    /** 结算单个商家, 结果匹配,并且保存成功才是成功, 不匹配或异常都算失败 */
    @Transactional(rollbackFor = Exception.class)
    public SettlementDetail settleMerchant(Long merchantId, LocalDate settlementDate) {
        // 检查是否已结算
        DailySettlement existing = findByMerchantAndDate(merchantId, settlementDate);
        SettlementDetail detail = null;
        if (existing != null) {
            log.warn("商家已结算，商家ID: {}, 结算日期: {}, 结算单号: {}", 
                    merchantId, settlementDate, existing.getSettlementNo());
            detail = new SettlementDetail();
            BeanUtil.copyProperties(existing, detail);
            detail.setMatchDescription("已结算过");
            detail.setIsSuccess(existing.getIsMatched() == 1);
            return detail;
        }
        // 从订单记录表里,计算指定日期的订单净销售额
        BigDecimal paidAmount = calculatePaidAmount(merchantId, settlementDate);
        BigDecimal refundAmount = calculateRefundAmount(merchantId, settlementDate);
        BigDecimal soldAmount = paidAmount.subtract(refundAmount);
        
        // 从商家余额流水记录表里, 统计商家同一日期内的订单净收入,交易类型为3和4的总金额(即订单收入和订单退款)
        BigDecimal netIncome = transactionMapper.calculateMerchantNetIncome(merchantId, settlementDate);
        // 验证是否匹配
        boolean isMatched = false;
        String matchDescription = "不匹配";
        BigDecimal diffAmount = new BigDecimal("0.00");
        
        if (soldAmount.compareTo(netIncome) == 0) {
            isMatched = true;
            matchDescription = "匹配（完全相等）";
        } else if (soldAmount.compareTo(new BigDecimal("0.00")) > 0 && netIncome.compareTo(new BigDecimal("0.00")) > 0) {
            // 计算差异（允许0.01以内的差异）
            diffAmount = netIncome.subtract(soldAmount).abs();
            if (diffAmount.compareTo(new BigDecimal("0.01")) <= 0) {
                isMatched = true;
                matchDescription = "匹配(差异金额<=0.01)";
            } else {
                matchDescription = "不匹配，差异金额: " + diffAmount;
            }
        }
        // 生成结算单号
        String settlementNo = idGenerator.generateSettlementNo();
        // 创建结算记录
        DailySettlement settlement = new DailySettlement();
        settlement.setSettlementNo(settlementNo);
        settlement.setMerchantId(merchantId);
        settlement.setSettlementDate(settlementDate);
        settlement.setPaidAmount(paidAmount);
        settlement.setRefundAmount(refundAmount);
        settlement.setSoldAmount(soldAmount);
        settlement.setMerchantNetIncome(netIncome);
        settlement.setIsMatched(isMatched ? 1 : 0);
        settlement.setRemark(matchDescription);
        boolean saveSuccess = this.save(settlement);

        detail = new SettlementDetail();
        BeanUtil.copyProperties(settlement, detail);
        if (saveSuccess) {
            if (isMatched){
                log.info("商家结算成功，商家ID: {}, 结算日期: {}, 结算单号: {}, 净销售额: {}, 账户净收入: {}, 匹配: {}",
                        merchantId, settlementDate, settlementNo, soldAmount, netIncome, isMatched);
                detail.setIsSuccess(true);
            }
        }else {
            detail.setIsSuccess(false);
            if (isMatched){
                matchDescription = matchDescription + "; 但保存结算记录失败";
            }
        }
        detail.setMatchDescription(matchDescription);
        detail.setDiffAmount(diffAmount);
        detail.setCreatedTime(LocalDateTime.now());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettlementDetail resettlement(Long merchantId, LocalDate settlementDate){
        log.info("重新结算: merchantId={}", merchantId);
        DailySettlement settlement = this.findByMerchantAndDate(merchantId, settlementDate);
        if (settlement != null){
            // 删除原结算记录
            boolean deleteSuccess = this.removeById(settlement.getSettlementId());
            if (!deleteSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除原结算记录失败");
            }
        }
        // 重新结算
        SettlementDetail detail = settleMerchant(merchantId, settlementDate);
        if (detail.getIsSuccess()) {
            log.info("重新结算成功，结算ID: {}, 商家ID: {}, 结算日期: {}",
                    detail.getSettlementId(), merchantId, settlement.getSettlementDate());

        }
        return detail;
    }


    /** 计算已支付订单金额 */
    private BigDecimal calculatePaidAmount(Long merchantId, LocalDate settlementDate) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.eq("order_status", OrderStatus.PAID.getCode());
        queryWrapper.between("payment_time", 
                DateUtil.getDayStart(settlementDate), 
                DateUtil.getDayEnd(settlementDate));
        
        List<Order> paidOrders = ordersMapper.selectList(queryWrapper);
        return paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    /** 计算已退款订单金额 */
    private BigDecimal calculateRefundAmount(Long merchantId, LocalDate settlementDate) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.eq("order_status", OrderStatus.REFUNDED.getCode());
        queryWrapper.between("updated_time", 
                DateUtil.getDayStart(settlementDate), 
                DateUtil.getDayEnd(settlementDate));
        
        List<Order> refundOrders = ordersMapper.selectList(queryWrapper);
        return refundOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    @Override
    public DailySettlement findById(Long settlementId) {
        Validator.notNull(settlementId, "结算ID不能为空");
        DailySettlement settlement = this.getById(settlementId);
        if (settlement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "结算记录不存在");
        }
        return settlement;
    }


    @Override
    public List<DailySettlement> findByMerchantId(Long merchantId) {
        Validator.notNull(merchantId, "商家ID不能为空");
        
        QueryWrapper<DailySettlement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.orderByDesc("settlement_date");
        
        return this.list(queryWrapper);
    }

    @Override
    public DailySettlement findByMerchantAndDate(Long merchantId, LocalDate settlementDate) {
        Validator.notNull(merchantId, "商家ID不能为空");
        Validator.notNull(settlementDate, "结算日期不能为空");
        
        QueryWrapper<DailySettlement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.eq("settlement_date", settlementDate);
        
        return this.getOne(queryWrapper);
    }

    @Override
    public Page<DailySettlement> findPage(int pageNum, int pageSize, Long merchantId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<DailySettlement> queryWrapper = new QueryWrapper<>();
        
        if (merchantId != null) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        
        if (startDate != null) {
            queryWrapper.ge("settlement_date", startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le("settlement_date", endDate);
        }
        
        queryWrapper.orderByDesc("settlement_date");
        Page<DailySettlement> page = new Page<>(pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public SettlementDetail findDetailById(Long settlementId) {
        DailySettlement settlement = this.findById(settlementId);
        Merchant merchant = merchantService.findById(settlement.getMerchantId());
        
        // 计算详细的金额
        BigDecimal paidAmount = calculatePaidAmount(merchant.getMerchantId(), settlement.getSettlementDate());
        BigDecimal refundAmount = calculateRefundAmount(merchant.getMerchantId(), settlement.getSettlementDate());
        BigDecimal soldAmount = paidAmount.subtract(refundAmount);
        
        // 计算差异金额
        BigDecimal diffAmount = settlement.getMerchantNetIncome().subtract(soldAmount).abs();
        
        // 构建响应
        SettlementDetail response = new SettlementDetail();
        BeanUtil.copyProperties(settlement, response);
        response.setMerchantName(merchant.getMerchantName());
        response.setPaidAmount(paidAmount);
        response.setRefundAmount(refundAmount);
        response.setSoldAmount(soldAmount);
        response.setIsMatched(settlement.getIsMatched());
        response.setDiffAmount(diffAmount);
        
        if (settlement.getIsMatched() == 1) {
            response.setMatchDescription("匹配");
        } else {
            response.setMatchDescription(String.format("不匹配，差异金额: %s", diffAmount));
        }
        
        return response;
    }

    @Override
    public BigDecimal calculateSalesAmount(Long merchantId, LocalDate settlementDate) {
        Validator.notNull(merchantId, "商家ID不能为空");
        Validator.notNull(settlementDate, "结算日期不能为空");
        
        BigDecimal paidAmount = calculatePaidAmount(merchantId, settlementDate);
        BigDecimal refundAmount = calculateRefundAmount(merchantId, settlementDate);
        
        return paidAmount.subtract(refundAmount);
    }
}