package com.shopping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.dto.request.InventoryAddRequest;
import com.shopping.dto.request.MerchantCreateRequest;
import com.shopping.dto.request.UpdateBalanceRequest;
import com.shopping.dto.response.ApiResponse;
import com.shopping.dto.response.MerchantResponse;
import com.shopping.dto.response.PageResponse;
import com.shopping.entity.Merchant;
import com.shopping.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/** 商家控制器 */
@Slf4j
@Validated
@RestController
@RequestMapping("/merchants")
@Api(tags = "商家管理")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @PostMapping
    @ApiOperation("创建商家")
    public ApiResponse<MerchantResponse> createMerchant(@Valid @RequestBody MerchantCreateRequest request) {
        log.info("创建商家请求: {}", request);
        MerchantResponse response = merchantService.createMerchant(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商家")
    public ApiResponse<MerchantResponse> findById(@PathVariable Long id) {
        log.info("查询商家: id={}", id);
        Merchant merchant = merchantService.findById(id);
        
        MerchantResponse response = new MerchantResponse();
        response.setMerchantId(merchant.getMerchantId());
        response.setUserId(merchant.getUserId());
        response.setMerchantName(merchant.getMerchantName());
        response.setMerchantCode(merchant.getMerchantCode());
        response.setContactPerson(merchant.getContactPerson());
        response.setContactPhone(merchant.getContactPhone());
        response.setAccountBalance(merchant.getAccountBalance());
        response.setStatus(merchant.getStatus());
        response.setCreatedTime(merchant.getCreatedTime());
        response.setUpdatedTime(merchant.getUpdatedTime());
        
        return ApiResponse.success(response);
    }

    @GetMapping("/code/{merchantCode}")
    @ApiOperation("根据编码查询商家")
    public ApiResponse<Merchant> findByCode(@PathVariable String merchantCode) {
        log.info("根据编码查询商家: merchantCode={}", merchantCode);
        Merchant merchant = merchantService.findByMerchantCode(merchantCode);
        if (merchant == null) {
            return ApiResponse.error(404, "商家不存在");
        }
        return ApiResponse.success(merchant);
    }

    @GetMapping
    @ApiOperation("查询所有商家")
    public ApiResponse<List<Merchant>> findAll() {
        log.info("查询所有商家");
        List<Merchant> merchants = merchantService.findAll();
        return ApiResponse.success(merchants);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询商家")
    public ApiResponse<PageResponse<Merchant>> findPage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        log.info("分页查询商家: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<Merchant> page = merchantService.findPage(pageNum, pageSize);
        
        PageResponse<Merchant> response = PageResponse.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        
        return ApiResponse.success(response);
    }

    @GetMapping("/{merchantId}/balance")
    @ApiOperation("查询商家余额")
    public ApiResponse<BigDecimal> getBalance(@PathVariable Long merchantId) {
        log.info("查询商家余额: merchantId={}", merchantId);
        BigDecimal balance = merchantService.getBalance(merchantId);
        return ApiResponse.success(balance);
    }

    @PostMapping("/balance/add")
    @ApiOperation("更新商家余额")
    public ApiResponse<Boolean> addBalance(@RequestBody UpdateBalanceRequest req) {
        log.info("增加商家余额: merchantId={}, amount={}", req.getMerchantId(), req.getAmount());

        boolean success = merchantService.updateBalance(req.getMerchantId(), req.getAmount(),
                req.getIsAdd(), req.getRelatedId(), req.getTransactionType(), req.getRemark());
        return ApiResponse.success(success);
    }

    @PostMapping("/inventory/add")
    @ApiOperation("增加库存")
    public ApiResponse<Boolean> addInventory(@Valid @RequestBody InventoryAddRequest request) {
        log.info("增加库存请求: {}", request);
        boolean success = merchantService.addInventory(request.getSkuId(), request.getQuantity(), request.getRemark());
        return ApiResponse.success(success);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("根据用户ID查询商家")
    public ApiResponse<Merchant> findByUserId(@PathVariable Long userId) {
        log.info("根据用户ID查询商家: userId={}", userId);
        Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            return ApiResponse.error(404, "商家不存在");
        }
        return ApiResponse.success(merchant);
    }
}