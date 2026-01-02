package com.shopping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.dto.request.ProductCreateRequest;
import com.shopping.dto.request.ProductSkuCreateRequest;
import com.shopping.dto.request.ProductSkuUpdateRequest;
import com.shopping.dto.request.ProductUpdateRequest;
import com.shopping.dto.response.ApiResponse;
import com.shopping.dto.response.PageResponse;
import com.shopping.dto.response.ProductResponse;
import com.shopping.dto.response.ProductSkuResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductSku;
import com.shopping.service.ProductService;
import com.shopping.service.ProductSkuService;
import com.shopping.service.ProductSkuServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

/** 商品控制器 */
@Slf4j
@Validated
@RestController
@RequestMapping("/products")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductSkuServiceImpl productSkuService;

    @PostMapping
    @ApiOperation("创建商品")
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("创建商品请求: {}", request);
        ProductResponse response = productService.createProduct(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商品")
    public ApiResponse<ProductResponse> findById(@PathVariable Long id) {
        log.info("查询商品: id={}", id);
        ProductResponse response = productService.findDetailById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/code/{productCode}")
    @ApiOperation("根据编码查询商品")
    public ApiResponse<Product> findByCode(@PathVariable String productCode) {
        log.info("根据编码查询商品: productCode={}", productCode);
        Product product = productService.findByProductCode(productCode);
        if (product == null) {
            return ApiResponse.error(404, "商品不存在");
        }
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新商品")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("更新商品: id={}, request={}", id, request);
        ProductResponse response = productService.updateProduct(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    public ApiResponse<Boolean> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: id={}", id);
        boolean success = productService.deleteProduct(id);
        return ApiResponse.success(success);
    }

    @GetMapping("/merchant/{merchantId}")
    @ApiOperation("查询商家所有商品")
    public ApiResponse<List<Product>> findByMerchantId(@PathVariable Long merchantId) {
        log.info("查询商家所有商品: merchantId={}", merchantId);
        List<Product> products = productService.findByMerchantId(merchantId);
        return ApiResponse.success(products);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询商品")
    public ApiResponse<PageResponse<Product>> findPage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) Long merchantId) {
        log.info("分页查询商品: pageNum={}, pageSize={}, merchantId={}", pageNum, pageSize, merchantId);
        Page<Product> page = productService.findPage(pageNum, pageSize, merchantId);
        
        PageResponse<Product> response = PageResponse.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        
        return ApiResponse.success(response);
    }

    @PostMapping("/{productId}/enable")
    @ApiOperation("上架商品")
    public ApiResponse<Boolean> enableProduct(@PathVariable Long productId) {
        log.info("上架商品: productId={}", productId);
        boolean success = productService.enableProduct(productId);
        return ApiResponse.success(success);
    }

    @PostMapping("/{productId}/disable")
    @ApiOperation("下架商品")
    public ApiResponse<Boolean> disableProduct(@PathVariable Long productId) {
        log.info("下架商品: productId={}", productId);
        boolean success = productService.disableProduct(productId);
        return ApiResponse.success(success);
    }

    @PostMapping("/skus")
    @ApiOperation("创建SKU")
    public ApiResponse<ProductSkuResponse> createSku(@Valid @RequestBody ProductSkuCreateRequest request) {
        log.info("创建SKU请求: {}", request);
        ProductSkuResponse response = productSkuService.createSku(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/skus/{skuId}")
    @ApiOperation("根据ID查询SKU")
    public ApiResponse<ProductSkuResponse> findSkuById(@PathVariable Long skuId) {
        log.info("查询SKU: skuId={}", skuId);
        ProductSkuResponse response = productSkuService.findDetailById(skuId);
        return ApiResponse.success(response);
    }

    @GetMapping("/skus/code/{skuCode}")
    @ApiOperation("根据编码查询SKU")
    public ApiResponse<ProductSku> findSkuByCode(@PathVariable String skuCode) {
        log.info("根据编码查询SKU: skuCode={}", skuCode);
        ProductSku sku = productSkuService.findBySkuCode(skuCode);
        if (sku == null) {
            return ApiResponse.error(404, "SKU不存在");
        }
        return ApiResponse.success(sku);
    }

    @PostMapping("/skus/{skuId}/stock/adjust")
    @ApiOperation("调整库存")
    public ApiResponse<Boolean> adjustStock(
            @PathVariable Long skuId,
            @RequestParam @Min(value = 0, message = "库存数量不能为负数") Integer quantity,
            @RequestParam(required = false) String remark) {
        log.info("调整库存: skuId={}, quantity={}", skuId, quantity);
        boolean success = productSkuService.adjustStock(skuId, quantity, remark);
        return ApiResponse.success(success);
    }

    @PutMapping("/skus/{skuId}")
    @ApiOperation("更新SKU")
    public ApiResponse<ProductSkuResponse> updateSku(
            @PathVariable Long skuId, 
            @Valid @RequestBody ProductSkuUpdateRequest request) {
        log.info("更新SKU: skuId={}, request={}", skuId, request);
        ProductSkuResponse response = productSkuService.updateSku(skuId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/skus/{skuId}")
    @ApiOperation("删除SKU")
    public ApiResponse<Boolean> deleteSku(@PathVariable Long skuId) {
        log.info("删除SKU: skuId={}", skuId);
        boolean success = productSkuService.deleteSku(skuId);
        return ApiResponse.success(success);
    }

    @GetMapping("/{productId}/skus")
    @ApiOperation("查询商品所有SKU")
    public ApiResponse<List<ProductSku>> findSkusByProductId(@PathVariable Long productId) {
        log.info("查询商品所有SKU: productId={}", productId);
        List<ProductSku> skus = productSkuService.findByProductId(productId);
        return ApiResponse.success(skus);
    }

    @GetMapping("/skus/page")
    @ApiOperation("分页查询SKU")
    public ApiResponse<PageResponse<ProductSku>> findSkuPage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) Long productId) {
        log.info("分页查询SKU: pageNum={}, pageSize={}, productId={}", pageNum, pageSize, productId);
        Page<ProductSku> page = productSkuService.findPage(pageNum, pageSize, productId);
        
        PageResponse<ProductSku> response = PageResponse.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        
        return ApiResponse.success(response);
    }

    @PostMapping("/skus/{skuId}/enable")
    @ApiOperation("启用SKU")
    public ApiResponse<Boolean> enableSku(@PathVariable Long skuId) {
        log.info("启用SKU: skuId={}", skuId);
        boolean success = productSkuService.enableSku(skuId);
        return ApiResponse.success(success);
    }

    @PostMapping("/skus/{skuId}/disable")
    @ApiOperation("禁用SKU")
    public ApiResponse<Boolean> disableSku(@PathVariable Long skuId) {
        log.info("禁用SKU: skuId={}", skuId);
        boolean success = productSkuService.disableSku(skuId);
        return ApiResponse.success(success);
    }

    @GetMapping("/skus/{skuId}/stock/validate")
    @ApiOperation("验证库存")
    public ApiResponse<Boolean> validateStock(
            @PathVariable Long skuId,
            @RequestParam @Min(value = 1, message = "数量必须大于0") Integer quantity) {
        log.info("验证库存: skuId={}, quantity={}", skuId, quantity);
        try {
            productSkuService.validateStock(skuId, quantity);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/skus/{skuId}/price/calculate")
    @ApiOperation("计算总价")
    public ApiResponse<Object> calculatePrice(
            @PathVariable Long skuId,
            @RequestParam @Min(value = 1, message = "数量必须大于0") Integer quantity) {
        log.info("计算总价: skuId={}, quantity={}", skuId, quantity);
        java.math.BigDecimal totalPrice = productSkuService.calculateTotalPrice(skuId, quantity);
        return ApiResponse.success(totalPrice);
    }
}