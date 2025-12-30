package com.shopping.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopping.entity.InventoryChange;
import com.shopping.mapper.InventoryChangeMapper;
import org.springframework.stereotype.Service;

/** 库存变更服务实现 */
@Service
public class InventoryChangeServiceImpl extends ServiceImpl<InventoryChangeMapper, InventoryChange> 
    implements InventoryChangeService {
}