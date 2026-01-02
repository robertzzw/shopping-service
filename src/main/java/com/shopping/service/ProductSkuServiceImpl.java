package com.shopping.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.InventoryChangeType;
import com.shopping.constant.StatusEnum;
import com.shopping.dto.request.ProductSkuCreateRequest;
import com.shopping.dto.request.ProductSkuUpdateRequest;
import com.shopping.dto.response.ProductSkuResponse;
import com.shopping.entity.InventoryChange;
import com.shopping.entity.Product;
import com.shopping.entity.ProductSku;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.ProductSkuMapper;
import com.shopping.utils.MoneyUtil;
import com.shopping.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 商品SKU服务实现
 */
@Slf4j
@Service
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {
    @Autowired
    private InventoryChangeService inventoryChangeService;
    @Autowired
    private ProductSkuMapper skuMapper;
    @Override
    public ProductSku findById(Long skuId) {
        Validator.notNull(skuId, "SKU ID不能为空");
        return this.getById(skuId);
    }
    @Override
    public ProductSku selectSkuForUpdate(Long id) {
        return skuMapper.selectSkuForUpdate(id);
    }

    @Override
    public ProductSku findBySkuCode(String skuCode) {
        Validator.notBlank(skuCode, "SKU编码不能为空");
        QueryWrapper<ProductSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_code", skuCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public ProductSku validateStock(Long skuId, Integer quantity) {
        Validator.notNull(skuId, "SKU ID不能为空");
        Validator.notNull(quantity, "数量不能为空");
        Validator.isTrue(quantity > 0, "数量必须大于0");
        //todo 检查库存是否足够时,使用for update加锁来查询, 防止后面扣减库存时,出现并发更新覆盖
//        ProductSku sku = skuMapper.selectSkuForUpdate(skuId);
        ProductSku sku = this.findById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "SKU不存在");
        }
        if (!StatusEnum.ENABLED.getCode().equals(sku.getStatus())) {
            throw new BusinessException(ErrorCode.SKU_NOT_AVAILABLE, "SKU不可用");
        }
        if (sku.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH,
                    String.format("库存不足，可用库存: %d，需要数量: %d", sku.getStockQuantity(), quantity));
        }
        return sku;
    }

    /** 乐观锁扣减库存 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean subtractStock(Long skuId, Integer quantity, Long stockVersion,
                                 Long relatedId, String relatedType, String remark) {
        ProductSku sku = null;
        for (int i=1; i<4; i++){
            sku = skuMapper.subtractStockAndReturn(skuId, quantity, stockVersion);
            if (sku != null && "success".equals(sku.getResultMessage())){
                break;
            }
            if (sku == null) {
                log.info("第{}次扣减库存失败, sku为null", i);
                return false;
            }else {
                log.info("第{}次扣减库存失败, skuId:{}, 当前库存:{}, 需要库存:{}",
                        i, skuId, sku.getStockQuantity(), quantity);
                if (i == 3){
                    return false;
                }
            }
            //随机等待一段时间，避免同时重试50-150ms
            try {
                Thread.sleep((50 + new Random().nextInt(100)) * i);
            } catch (InterruptedException e) {
                log.info("重试被中断", e);
                return false;
            }
        }
        Integer currentStock = sku.getStockQuantity();
        // 记录库存变更
        InventoryChange inventoryChange = new InventoryChange();
        inventoryChange.setSkuId(skuId);
        inventoryChange.setChangeType(InventoryChangeType.REDUCE.getCode());
        inventoryChange.setStockBefore(currentStock + quantity);
        inventoryChange.setChangeQuantity(-quantity);
        inventoryChange.setStockAfter(currentStock);
        inventoryChange.setRelatedId(relatedId);
        inventoryChange.setRelatedType(relatedType);
        inventoryChange.setRemark(remark != null ? remark : "销售扣减库存");
        inventoryChange.setCreatedTime(sku.getUpdatedTime());

        inventoryChangeService.save(inventoryChange);
        log.info("扣减库存成功，SKU ID: {}, 扣减数量: {}, 原库存: {}, 新库存: {}",
                skuId, -quantity, currentStock + quantity, currentStock);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long skuId, Integer quantity, Long relatedId, String relatedType, String remark) {
        Validator.notNull(skuId, "SKU ID不能为空");
        Validator.notNull(quantity, "数量不能为空");
        Validator.isTrue(quantity > 0, "数量必须大于0");

        ProductSku sku = this.findById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "SKU不存在");
        }

        Integer oldStock = sku.getStockQuantity();
        Integer newStock = oldStock + quantity;

        // 更新库存
        sku.setStockQuantity(newStock);
        boolean updateSuccess = this.updateById(sku);

        if (updateSuccess) {
            // 记录库存变更
            InventoryChange inventoryChange = new InventoryChange();
            inventoryChange.setSkuId(skuId);
            inventoryChange.setChangeType(InventoryChangeType.ADD.getCode());
            inventoryChange.setChangeQuantity(quantity);
            inventoryChange.setStockBefore(oldStock);
            inventoryChange.setStockAfter(newStock);
            inventoryChange.setRelatedId(relatedId);
            inventoryChange.setRelatedType(relatedType);
            inventoryChange.setRemark(remark != null ? remark : "增加库存");
            inventoryChange.setCreatedTime(LocalDateTime.now());

            inventoryChangeService.save(inventoryChange);

            log.info("增加库存成功，SKU ID: {}, 增加数量: {}, 原库存: {}, 新库存: {}",
                    skuId, quantity, oldStock, newStock);
        }

        return updateSuccess;
    }

    @Override
    public BigDecimal calculateTotalPrice(Long skuId, Integer quantity) {
        Validator.notNull(skuId, "SKU ID不能为空");
        Validator.notNull(quantity, "数量不能为空");
        Validator.isTrue(quantity > 0, "数量必须大于0");

        ProductSku sku = this.findById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "SKU不存在");
        }

        return MoneyUtil.calculateTotal(sku.getPrice(), quantity);
    }

    @Autowired
    private ProductService productService;

    /**
     * 创建SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductSkuResponse createSku(ProductSkuCreateRequest request) {
        log.info("创建SKU: {}", request);

        // 验证参数
        Validator.notBlank(request.getSkuCode(), "SKU编码不能为空");
        Validator.notNull(request.getProductId(), "商品ID不能为空");
        Validator.notBlank(request.getSkuName(), "SKU名称不能为空");
        Validator.notNull(request.getPrice(), "单价不能为空");
        Validator.isTrue(MoneyUtil.isValid(request.getPrice()), "单价必须大于0");

        // 验证商品
        Product product = productService.findById(request.getProductId());
        if (!StatusEnum.ENABLED.getCode().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_DISABLED, "商品已下架");
        }

        // 检查SKU编码是否已存在
        QueryWrapper<ProductSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_code", request.getSkuCode());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "SKU编码已存在");
        }

        // 创建SKU
        ProductSku sku = new ProductSku();
        sku.setSkuCode(request.getSkuCode());
        sku.setProductId(request.getProductId());
        sku.setSkuName(request.getSkuName());
        sku.setPrice(request.getPrice());
        sku.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        sku.setAttributes(request.getAttributes());
        sku.setStatus(StatusEnum.ENABLED.getCode());

        boolean saveSuccess = this.save(sku);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建SKU失败");
        }

        // 构建响应
        ProductSkuResponse response = new ProductSkuResponse();
        BeanUtil.copyProperties(sku, response);
        response.setProductName(product.getProductName());

        log.info("创建SKU成功，SKU ID: {}, SKU名称: {}", sku.getSkuId(), sku.getSkuName());
        return response;
    }

    /**
     * 调整库存
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustStock(Long skuId, Integer quantity, String remark) {
        Validator.notNull(skuId, "SKU ID不能为空");
        Validator.notNull(quantity, "数量不能为空");

        ProductSku sku = this.findById(skuId);
        Integer oldStock = sku.getStockQuantity();
        Integer newStock = quantity; // 设置为指定数量

        if (newStock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存数量不能为负数");
        }

        sku.setStockQuantity(newStock);
        boolean updateSuccess = this.updateById(sku);

        if (updateSuccess) {
            // 记录库存变更
            InventoryChange inventoryChange = new InventoryChange();
            inventoryChange.setSkuId(skuId);
            inventoryChange.setChangeType(3); // 调整库存
            inventoryChange.setChangeQuantity(newStock - oldStock);
            inventoryChange.setStockBefore(oldStock);
            inventoryChange.setStockAfter(newStock);
            inventoryChange.setRelatedType("STOCK_ADJUST");
            inventoryChange.setRemark(remark != null ? remark : "调整库存");
            inventoryChange.setCreatedTime(java.time.LocalDateTime.now());

            inventoryChangeService.save(inventoryChange);

            log.info("调整库存成功，SKU ID: {}, 原库存: {}, 新库存: {}, 变更数量: {}",
                    skuId, oldStock, newStock, newStock - oldStock);
        }

        return updateSuccess;
    }

    /**
     * 更新SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductSkuResponse updateSku(Long skuId, ProductSkuUpdateRequest request) {
        log.info("更新SKU: skuId={}, request={}", skuId, request);
        ProductSku sku = this.findById(skuId);
        // 更新字段
        if (request.getSkuName() != null) {
            sku.setSkuName(request.getSkuName());
        }
        if (request.getPrice() != null) {
            Validator.isTrue(MoneyUtil.isValid(request.getPrice()), "单价必须大于0");
            sku.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            sku.setStockQuantity(request.getStockQuantity());
        }
        if (request.getAttributes() != null) {
            sku.setAttributes(request.getAttributes());
        }
        if (request.getStatus() != null) {
            // 验证状态值
            if (!StatusEnum.DISABLED.getCode().equals(request.getStatus()) &&
                    !StatusEnum.ENABLED.getCode().equals(request.getStatus())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值无效");
            }
            sku.setStatus(request.getStatus());
        }

        boolean updateSuccess = this.updateById(sku);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新SKU失败");
        }

        // 获取商品信息
        Product product = productService.findById(sku.getProductId());

        // 构建响应
        ProductSkuResponse response = new ProductSkuResponse();
        BeanUtil.copyProperties(sku, response);
        response.setProductName(product.getProductName());

        log.info("更新SKU成功，SKU ID: {}", skuId);
        return response;
    }

    /**
     * 删除SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSku(Long skuId) {
        log.info("删除SKU: skuId={}", skuId);

        ProductSku sku = this.findById(skuId);

        // 检查库存是否为0
        if (sku.getStockQuantity() > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "SKU库存不为0，无法删除");
        }

        boolean deleteSuccess = this.removeById(skuId);
        if (deleteSuccess) {
            log.info("删除SKU成功，SKU ID: {}", skuId);
        }

        return deleteSuccess;
    }

    /**
     * 查询商品的所有SKU
     */
    public List<ProductSku> findByProductId(Long productId) {
        Validator.notNull(productId, "商品ID不能为空");

        QueryWrapper<ProductSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.orderByDesc("created_time");

        return this.list(queryWrapper);
    }

    /**
     * 分页查询SKU
     */
    public Page<ProductSku> findPage(int pageNum, int pageSize, Long productId) {
        QueryWrapper<ProductSku> queryWrapper = new QueryWrapper<>();

        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }

        queryWrapper.orderByDesc("created_time");
        Page<ProductSku> page = new Page<>(pageNum, pageSize);

        return this.page(page, queryWrapper);
    }

    /**
     * 查询SKU详情
     */
    public ProductSkuResponse findDetailById(Long skuId) {
        ProductSku sku = this.findById(skuId);

        // 获取商品信息
        Product product = productService.findById(sku.getProductId());

        // 构建响应
        ProductSkuResponse response = new ProductSkuResponse();
        BeanUtil.copyProperties(sku, response);
        response.setProductName(product.getProductName());

        return response;
    }

    /**
     * 启用SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean enableSku(Long skuId) {
        log.info("启用SKU: skuId={}", skuId);
        ProductSku sku = this.findById(skuId);
        sku.setStatus(StatusEnum.ENABLED.getCode());

        boolean updateSuccess = this.updateById(sku);
        if (updateSuccess) {
            log.info("启用SKU成功，SKU ID: {}", skuId);
        }
        return updateSuccess;
    }

    /**
     * 禁用SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean disableSku(Long skuId) {
        log.info("禁用SKU: skuId={}", skuId);
        ProductSku sku = this.findById(skuId);
        sku.setStatus(StatusEnum.DISABLED.getCode());

        boolean updateSuccess = this.updateById(sku);
        if (updateSuccess) {
            log.info("禁用SKU成功，SKU ID: {}", skuId);
        }
        return updateSuccess;
    }
}