package com.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.entity.Merchant;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 商家Mapper接口
 */
@Repository
public interface MerchantMapper extends BaseMapper<Merchant> {
    Merchant selectMerchantForUpdate(@Param("merchantId") Long merchantId);
}