package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.ProductSku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 商品SKU Mapper接口
 */
@Repository
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    ProductSku selectSkuForUpdate(@Param("skuId") Long skuId);
    Integer updateStockById(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    ProductSku subtractStockAndReturn(@Param("skuId") Long skuId,
                                      @Param("quantity") Integer quantity,
                                      @Param("version") Long version);

}