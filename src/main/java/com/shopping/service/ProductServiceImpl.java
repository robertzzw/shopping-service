package com.shopping.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.StatusEnum;
import com.shopping.dto.request.ProductCreateRequest;
import com.shopping.dto.request.ProductUpdateRequest;
import com.shopping.dto.response.ProductResponse;
import com.shopping.entity.Merchant;
import com.shopping.entity.Product;
import com.shopping.entity.ProductSku;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.ProductMapper;
import com.shopping.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** 商品服务实现 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductSkuService productSkuService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("创建商品: {}", request);
        
        // 验证参数
        Validator.notBlank(request.getProductName(), "商品名称不能为空");
        Validator.notBlank(request.getProductCode(), "商品编码不能为空");
        Validator.notNull(request.getMerchantId(), "商家ID不能为空");
        
        // 验证商家
        Merchant merchant = merchantService.findById(request.getMerchantId());
        merchantService.validateMerchantStatus(merchant);
        
        // 检查商品编码是否已存在
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_code", request.getProductCode());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品编码已存在");
        }
        
        // 创建商品
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setProductCode(request.getProductCode());
        product.setMerchantId(request.getMerchantId());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setStatus(StatusEnum.ENABLED.getCode());
        
        boolean saveSuccess = this.save(product);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建商品失败");
        }
        
        // 构建响应
        ProductResponse response = new ProductResponse();
        BeanUtil.copyProperties(product, response);
        response.setMerchantName(merchant.getMerchantName());
        response.setSkuCount(0L);
        
        log.info("创建商品成功，商品ID: {}, 商品名称: {}", product.getProductId(), product.getProductName());
        return response;
    }

    @Override
    public Product findById(Long productId) {
        Validator.notNull(productId, "商品ID不能为空");
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        return product;
    }

    @Override
    public Product findByProductCode(String productCode) {
        Validator.notBlank(productCode, "商品编码不能为空");
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_code", productCode);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("更新商品: productId={}, request={}", productId, request);
        
        Product product = this.findById(productId);
        
        // 更新字段
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            // 验证状态值
            if (!StatusEnum.DISABLED.getCode().equals(request.getStatus()) && 
                !StatusEnum.ENABLED.getCode().equals(request.getStatus())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值无效");
            }
            product.setStatus(request.getStatus());
        }
        
        boolean updateSuccess = this.updateById(product);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新商品失败");
        }
        
        // 获取商家信息
        Merchant merchant = merchantService.findById(product.getMerchantId());
        
        // 获取SKU数量
        QueryWrapper<ProductSku> skuQuery = new QueryWrapper<>();
        skuQuery.eq("product_id", productId);
        long skuCount = productSkuService.count(skuQuery);
        
        // 构建响应
        ProductResponse response = new ProductResponse();
        BeanUtil.copyProperties(product, response);
        response.setMerchantName(merchant.getMerchantName());
        response.setSkuCount(skuCount);
        
        log.info("更新商品成功，商品ID: {}", productId);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long productId) {
        log.info("删除商品: productId={}", productId);
        
        Product product = this.findById(productId);
        
        // 检查商品是否有SKU
        QueryWrapper<ProductSku> skuQuery = new QueryWrapper<>();
        skuQuery.eq("product_id", productId);
        long skuCount = productSkuService.count(skuQuery);
        if (skuCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "商品下存在SKU，无法删除");
        }
        
        boolean deleteSuccess = this.removeById(productId);
        if (deleteSuccess) {
            log.info("删除商品成功，商品ID: {}", productId);
        }
        
        return deleteSuccess;
    }

    @Override
    public List<Product> findByMerchantId(Long merchantId) {
        Validator.notNull(merchantId, "商家ID不能为空");
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId);
        queryWrapper.orderByDesc("created_time");
        
        return this.list(queryWrapper);
    }

    @Override
    public Page<Product> findPage(int pageNum, int pageSize, Long merchantId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        
        if (merchantId != null) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        
        queryWrapper.orderByDesc("created_time");
        Page<Product> page = new Page<>(pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public ProductResponse findDetailById(Long productId) {
        Product product = this.findById(productId);
        
        // 获取商家信息
        Merchant merchant = merchantService.findById(product.getMerchantId());
        
        // 获取SKU数量
        QueryWrapper<ProductSku> skuQuery = new QueryWrapper<>();
        skuQuery.eq("product_id", productId);
        long skuCount = productSkuService.count(skuQuery);
        
        // 构建响应
        ProductResponse response = new ProductResponse();
        BeanUtil.copyProperties(product, response);
        response.setMerchantName(merchant.getMerchantName());
        response.setSkuCount(skuCount);
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableProduct(Long productId) {
        log.info("上架商品: productId={}", productId);
        
        Product product = this.findById(productId);
        product.setStatus(StatusEnum.ENABLED.getCode());
        
        boolean updateSuccess = this.updateById(product);
        if (updateSuccess) {
            log.info("上架商品成功，商品ID: {}", productId);
        }
        
        return updateSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableProduct(Long productId) {
        log.info("下架商品: productId={}", productId);
        Product product = this.findById(productId);
        product.setStatus(StatusEnum.DISABLED.getCode());
        
        boolean updateSuccess = this.updateById(product);
        if (updateSuccess) {
            log.info("下架商品成功，商品ID: {}", productId);
        }
        return updateSuccess;
    }
}