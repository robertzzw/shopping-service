package com.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.dto.request.ProductCreateRequest;
import com.shopping.dto.request.ProductUpdateRequest;
import com.shopping.dto.response.ProductResponse;
import com.shopping.entity.Product;
import com.shopping.entity.User;

import java.util.List;

/** 商品服务接口 */
public interface ProductService extends IService<Product> {
    
    /** 创建商品 */
    ProductResponse createProduct(ProductCreateRequest request);
    
    /** 根据ID查询商品 */
    Product findById(Long productId);
    
    /** 根据编码查询商品 */
    Product findByProductCode(String productCode);
    
    /** 更新商品 */
    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);
    
    /** 删除商品 */
    boolean deleteProduct(Long productId);
    
    /** 查询商家所有商品 */
    List<Product> findByMerchantId(Long merchantId);
    
    /** 分页查询商品 */
    Page<Product> findPage(int pageNum, int pageSize, Long merchantId);
    
    /** 查询商品详情（包含SKU数量） */
    ProductResponse findDetailById(Long productId);
    
    /** 上架商品 */
    boolean enableProduct(Long productId);
    
    /** 下架商品 */
    boolean disableProduct(Long productId);
}