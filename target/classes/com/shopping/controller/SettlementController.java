package com.shopping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.dto.response.ApiResponse;
import com.shopping.dto.response.PageResponse;
import com.shopping.dto.response.SettlementDetail;
import com.shopping.dto.response.SettlementResult;
import com.shopping.entity.DailySettlement;
import com.shopping.service.SettlementService;
import com.shopping.task.SettlementTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 结算控制器 */
@Slf4j
@Validated
@RestController
@RequestMapping("/settlements")
@Api(tags = "结算管理")
public class SettlementController {
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementTask settlementTask;
    @PostMapping("/daily")
    @ApiOperation("执行每日结算(所有商家)")
    public ApiResponse<SettlementResult> executeDailySettlement(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate settlementDate) {
        log.info("执行每日结算: settlementDate={}", settlementDate);
        if (settlementDate == null) {
            settlementDate = LocalDate.now().minusDays(1);
        }
        SettlementResult result = settlementService.executeDailySettlement(settlementDate);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询结算记录")
    public ApiResponse<SettlementDetail> findById(@PathVariable Long id) {
        log.info("查询结算记录: id={}", id);
        SettlementDetail response = settlementService.findDetailById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/merchant/resettle")
    @ApiOperation("重新结算(单个商家)")
    public ApiResponse<SettlementDetail> resettlement(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate settlementDate) {
        log.info("重新结算: merchantId={}", merchantId);
        if (settlementDate == null) {
            settlementDate = LocalDate.now().minusDays(1);
        }
        SettlementDetail detail = settlementService.resettlement(merchantId, settlementDate);
        return ApiResponse.success(detail);
    }

    @GetMapping("/merchant/{merchantId}")
    @ApiOperation("查询商家结算记录")
    public ApiResponse<List<DailySettlement>> findByMerchantId(@PathVariable Long merchantId) {
        log.info("查询商家结算记录: merchantId={}", merchantId);
        List<DailySettlement> settlements = settlementService.findByMerchantId(merchantId);
        return ApiResponse.success(settlements);
    }

    @GetMapping("/merchant/{merchantId}/date/{date}")
    @ApiOperation("查询商家指定日期的结算记录")
    public ApiResponse<DailySettlement> findByMerchantAndDate(
            @PathVariable Long merchantId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("查询商家指定日期的结算记录: merchantId={}, date={}", merchantId, date);
        DailySettlement settlement = settlementService.findByMerchantAndDate(merchantId, date);
        if (settlement == null) {
            return ApiResponse.error(404, "结算记录不存在");
        }
        return ApiResponse.success(settlement);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询结算记录")
    public ApiResponse<PageResponse<DailySettlement>> findPage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("分页查询结算记录: pageNum={}, pageSize={}, merchantId={}, startDate={}, endDate={}", 
                pageNum, pageSize, merchantId, startDate, endDate);
        
        Page<DailySettlement> page = settlementService.findPage(pageNum, pageSize, merchantId, startDate, endDate);
        
        PageResponse<DailySettlement> response = PageResponse.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        
        return ApiResponse.success(response);
    }

    @GetMapping("/merchant/{merchantId}/date/{date}/sales")
    @ApiOperation("计算指定日期的销售额")
    public ApiResponse<BigDecimal> calculateSalesAmount(
            @PathVariable Long merchantId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("计算销售额: merchantId={}, date={}", merchantId, date);
        BigDecimal amount = settlementService.calculateSalesAmount(merchantId, date);
        return ApiResponse.success(amount);
    }


    @PostMapping("/test")
    @ApiOperation("测试结算任务")
    public ApiResponse<String> testSettlementTask(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate settlementDate) {
        log.info("测试结算任务: settlementDate={}", settlementDate);
        
        if (settlementDate == null) {
            settlementDate = LocalDate.now().minusDays(1);
        }
        
        try {
            settlementTask.testSettlement(settlementDate);
            return ApiResponse.success("测试结算任务已执行");
        } catch (Exception e) {
            log.error("测试结算任务异常", e);
            return ApiResponse.error(500, "测试结算任务异常: " + e.getMessage());
        }
    }
}