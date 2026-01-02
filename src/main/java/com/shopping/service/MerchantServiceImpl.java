package com.shopping.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.constant.AccountType;
import com.shopping.constant.InventoryChangeType;
import com.shopping.constant.StatusEnum;
import com.shopping.constant.UserType;
import com.shopping.dto.request.MerchantCreateRequest;
import com.shopping.dto.response.MerchantResponse;
import com.shopping.entity.*;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.MerchantMapper;
import com.shopping.utils.MoneyUtil;
import com.shopping.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 商家服务实现 */
@Slf4j
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {
    @Autowired
    private UserService userService;
    @Autowired
    private ProductSkuService productSkuService;
    @Autowired
    private AccountTransactionService accountTransactionService;
    @Autowired
    private InventoryChangeService inventoryChangeService;
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantResponse createMerchant(MerchantCreateRequest request) {
        log.info("创建商家: {}", request);
        
        // 验证参数
        Validator.notBlank(request.getUsername(), "用户名不能为空");
        Validator.notBlank(request.getMerchantName(), "商家名称不能为空");
        Validator.notBlank(request.getMerchantCode(), "商家编码不能为空");
        
        // 检查商家编码是否已存在
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_code", request.getMerchantCode());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商家编码已存在");
        }
        
        // 检查用户名是否已存在
        User existingUser = userService.getUserByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUserType(UserType.MERCHANT.getCode());
        user.setStatus(StatusEnum.ENABLED.getCode());
        user.setAccountBalance(new BigDecimal("0.00"));
        userService.createUser(user);
        
        // 创建商家
        Merchant merchant = new Merchant();
        merchant.setUserId(user.getUserId());
        merchant.setMerchantName(request.getMerchantName());
        merchant.setMerchantCode(request.getMerchantCode());
        merchant.setContactPerson(request.getContactPerson());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setAccountBalance(request.getInitialBalance() != null ? request.getInitialBalance() : new BigDecimal("0.00"));
        merchant.setStatus(StatusEnum.ENABLED.getCode());
        
        boolean saveSuccess = this.save(merchant);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建商家失败");
        }
        
        // 如果有初始余额，更新用户余额
        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(new BigDecimal("0.00")) > 0) {
            userService.updateUserBalance(user.getUserId(), request.getInitialBalance(),
                    "increase", 0L, 1, "用户充值");
        }
        
        // 构建响应
        MerchantResponse response = new MerchantResponse();
        BeanUtil.copyProperties(merchant, response);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        
        log.info("创建商家成功，商家ID: {}, 用户ID: {}", merchant.getMerchantId(), user.getUserId());
        return response;
    }

    @Override
    public Merchant findById(Long merchantId) {
        Validator.notNull(merchantId, "商家ID不能为空");
        Merchant merchant = this.getById(merchantId);
//        Merchant merchant = merchantMapper.selectMerchantForUpdate(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商家不存在");
        }
        return merchant;
    }

    @Override
    public Merchant findByUserId(Long userId) {
        Validator.notNull(userId, "用户ID不能为空");
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public Merchant findByMerchantCode(String merchantCode) {
        Validator.notBlank(merchantCode, "商家编码不能为空");
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_code", merchantCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<Merchant> findAll() {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", StatusEnum.ENABLED.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public Page<Merchant> findPage(int pageNum, int pageSize) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_time");
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBalance(Long merchantId, BigDecimal amount, boolean isAdd,
                                 Long relatedId, Integer transactionType, String remark) {
        Validator.notNull(merchantId, "商家ID不能为空");
        Validator.notNull(amount, "金额不能为空");
        Validator.isTrue(MoneyUtil.isValid(amount), "金额必须大于0");
        
        //Merchant merchant = this.findById(merchantId);
        //todo 检查库存是否足够时,使用for update加锁来查询, 防止出现超额/并发更新覆盖
        Merchant merchant = merchantMapper.selectMerchantForUpdate(merchantId);
        validateMerchantStatus(merchant);
        
        BigDecimal oldBalance = merchant.getAccountBalance();
        BigDecimal newBalance;
        if (isAdd) {
            newBalance = oldBalance.add(amount);
        } else {
            if (!MoneyUtil.isEnough(oldBalance, amount)) {
                throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH, "商家余额不足");
            }
            newBalance = oldBalance.subtract(amount);
            //记录交易流水时,如果是扣减操作, 获取金额的负数来保存
            amount = amount.negate();
        }
        merchant.setAccountBalance(newBalance);
        boolean updateSuccess = this.updateById(merchant);
        if (updateSuccess) {
            // 记录交易流水
            AccountTransaction transaction = accountTransactionService.createTransaction(
                AccountType.MERCHANT_ACCOUNT.getCode(),
                merchantId,
                transactionType, //交易类型: 1-充值, 2-消费, 3-收入, 4-退款
                oldBalance,
                amount,
                newBalance,
                relatedId,
               "merchant_balance_update",
                remark
            );
            accountTransactionService.saveTransaction(transaction);
            log.info("更新商家余额成功，商家ID: {}, 操作: {}, 金额: {}, 原余额: {}, 新余额: {}",
                    merchantId, isAdd ? "增加" : "减少", amount, oldBalance, newBalance);
        }
        return updateSuccess;
    }

    @Override
    public void validateMerchantStatus(Merchant merchant) {
        Validator.notNull(merchant, "商家不能为空");
        if (!StatusEnum.ENABLED.getCode().equals(merchant.getStatus())) {
            throw new BusinessException(ErrorCode.MERCHANT_DISABLED, "商家已被停用");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addInventory(Long skuId, Integer quantity, String remark) {
        Validator.notNull(skuId, "SKU ID不能为空");
        Validator.notNull(quantity, "数量不能为空");
        Validator.isTrue(quantity > 0, "数量必须大于0");
        
        // 获取SKU信息
        ProductSku sku = productSkuService.findById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "SKU不存在");
        }
        
        // 验证SKU状态
        if (!StatusEnum.ENABLED.getCode().equals(sku.getStatus())) {
            throw new BusinessException(ErrorCode.SKU_NOT_AVAILABLE, "SKU不可用");
        }
        
        // 记录变更前库存
        Integer oldStock = sku.getStockQuantity();
        Integer newStock = oldStock + quantity;
        
        // 更新库存
        sku.setStockQuantity(newStock);
        boolean updateSuccess = productSkuService.updateById(sku);
        
        if (updateSuccess) {
            // 记录库存变更
            InventoryChange inventoryChange = new InventoryChange();
            inventoryChange.setSkuId(skuId);
            inventoryChange.setChangeType(InventoryChangeType.ADD.getCode());
            inventoryChange.setChangeQuantity(quantity);
            inventoryChange.setStockBefore(oldStock);
            inventoryChange.setStockAfter(newStock);
            inventoryChange.setRelatedType("INVENTORY_ADD");
            inventoryChange.setRemark(remark != null ? remark : "商家增加库存");
            inventoryChange.setCreatedTime(LocalDateTime.now());
            
            inventoryChangeService.save(inventoryChange);
            
            log.info("增加库存成功，SKU ID: {}, 增加数量: {}, 原库存: {}, 新库存: {}",
                    skuId, quantity, oldStock, newStock);
        }
        
        return updateSuccess;
    }
}