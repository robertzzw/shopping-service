package com.shopping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopping.dto.request.MerchantCreateRequest;
import com.shopping.dto.response.MerchantResponse;
import com.shopping.entity.Merchant;
import com.shopping.entity.User;

import java.math.BigDecimal;
import java.util.List;

/** 商家服务接口 */
public interface MerchantService extends IService<Merchant> {
    
    /** 创建商家 */
    MerchantResponse createMerchant(MerchantCreateRequest request);
    
    /** 根据ID查询商家 */
    Merchant findById(Long merchantId);
    
    /** 根据用户ID查询商家 */
    Merchant findByUserId(Long userId);
    
    /** 根据商家编码查询 */
    Merchant findByMerchantCode(String merchantCode);
    
    /** 查询所有商家 */
    List<Merchant> findAll();
    
    /** 分页查询商家 */
    Page<Merchant> findPage(int pageNum, int pageSize);
    
    /** 更新商家余额 */
    boolean updateBalance(Long merchantId, BigDecimal amount,
                          boolean isAdd, Long relatedId, Integer transactionType, String remark);
    
    /** 获取商家余额 */
    BigDecimal getBalance(Long merchantId);
    
    /** 验证商家状态 */
    void validateMerchantStatus(Merchant merchant);
    
    /** 增加库存 */
    boolean addInventory(Long skuId, Integer quantity, String remark);
}