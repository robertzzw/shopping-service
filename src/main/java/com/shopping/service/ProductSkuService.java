package com.shopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.entity.ProductSku;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/** 商品SKU服务接口 */
public interface ProductSkuService extends IService<ProductSku> {
    
    /** 根据ID查询SKU */
    ProductSku findById(Long skuId);

    /** todo 检查库存是否足够时,使用for update加锁来查询, 防止后面扣减库存时,出现并发更新覆盖,后期可以优化 */
    ProductSku selectSkuForUpdate(@Param("id") Long id);
    
    /** 根据SKU编码查询 */
    ProductSku findBySkuCode(String skuCode);
    
    /** 验证SKU库存是否足够 */
    ProductSku validateStock(Long skuId, Integer quantity);
    
    /** 更新库存 */
    boolean subtractStock(Long skuId, Integer quantity, Long stockVersion,
                          Long relatedId, String relatedType, String remark);
    
    /** 增加库存 */
    boolean increaseStock(Long skuId, Integer quantity, Long relatedId, String relatedType, String remark);
    
    /** 计算总价 */
    BigDecimal calculateTotalPrice(Long skuId, Integer quantity);
}