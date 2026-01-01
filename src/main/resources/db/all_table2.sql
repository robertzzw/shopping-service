-- 创建数据库
drop database if exists `shopping_db`;
create database if not exists `shopping_db` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `shopping_db`;

-- 1. 用户表
drop table if exists `user`;
create table `user` (
  `user_id` bigint(20) not null auto_increment comment '用户id',
  `username` varchar(50) not null comment '用户名',
  `email` varchar(50) default null comment '邮箱',
  `phone` varchar(20) default null comment '手机号',
  `account_balance` decimal(10,2) not null default '0.00' comment '账户余额',
  `balance_version` bigint(20) not null default '0' comment '余额版本',
  `user_type` tinyint(1) not null default '1' comment '用户类型: 1-普通用户, 2-商家用户',
  `status` tinyint(1) not null default '1' comment '状态: 0-禁用, 1-正常',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  `updated_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (`user_id`),
  unique key `uk_username` (`username`),
  key `idx_email` (`email`),
  key `idx_phone` (`phone`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='用户表';

-- 2. 商家表
drop table if exists `merchant`;
create table `merchant` (
  `merchant_id` bigint(20) not null auto_increment comment '商家id',
  `user_id` bigint(20) default null comment '关联用户id, 可以为null,即商家可以不开通用户账号',
  `merchant_name` varchar(100) not null comment '商家名称',
  `merchant_code` varchar(50) not null comment '商家编码',
  `contact_person` varchar(50) default null comment '联系人',
  `contact_phone` varchar(20) default null comment '联系电话',
  `account_balance` decimal(10,2) not null default '0.00' comment '商家账户余额',
  `balance_version` bigint(20) not null default '0' comment '余额版本',
  `status` tinyint(1) not null default '1' comment '状态: 0-停用, 1-正常',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  `updated_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (`merchant_id`),
  unique key `uk_merchant_code` (`merchant_code`),
  key `idx_user_id` (`user_id`),
  key `idx_merchant_name` (`merchant_name`)
#   constraint `fk_merchant_user` foreign key (`user_id`) references `user` (`user_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='商家表';

-- 3. 商品表
drop table if exists `product`;
create table `product` (
  `product_id` bigint(20) not null auto_increment comment '商品id',
  `product_name` varchar(200) not null comment '商品名称',
  `product_code` varchar(50) not null comment '商品编码',
  `merchant_id` bigint(20) not null comment '商家id',
  `category` varchar(50) default null comment '商品分类',
  `description` text comment '商品描述',
  `status` tinyint(1) not null default '1' comment '状态: 0-下架, 1-上架',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  `updated_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (`product_id`),
  unique key `uk_product_code` (`product_code`),
  key `idx_merchant_id` (`merchant_id`),
  key `idx_product_name` (`product_name`)
#   constraint `fk_product_merchant` foreign key (`merchant_id`) references `merchant` (`merchant_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='商品表';

-- 4. 商品sku表, attributes sku属性字段 后期扩展可创建一个新表来存储
drop table if exists `product_sku`;
create table `product_sku` (
  `sku_id` bigint(20) not null auto_increment comment 'sku id',
  `sku_code` varchar(50) not null comment 'sku编码',
  `product_id` bigint(20) not null comment '商品id',
  `sku_name` varchar(200) not null comment 'sku名称',
  `price` decimal(10,2) not null comment '单价',
  `stock_quantity` int(11) not null default '0' comment '库存数量',
  `stock_version` bigint(20) not null default '0' comment '库存版本',
  `attributes` varchar(500) default null comment 'sku属性(如: 颜色:红色,尺寸:m)',
  `status` tinyint(1) not null default '1' comment '状态: 0-不可用, 1-可用',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  `updated_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (`sku_id`),
  unique key `uk_sku_code` (`sku_code`),
  key `idx_product_id` (`product_id`),
  key `idx_sku_stock` (`stock_quantity`);
  key `idx_sku_name` (`sku_name`)
#   constraint `fk_product_sku_product` foreign key (`product_id`) references `product` (`product_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='商品sku表';

-- 5. 订单表
drop table if exists `orders`;
create table `orders` (
  `order_id` bigint(20) not null auto_increment comment '订单id',
  `order_no` varchar(50) not null comment '订单号',
  `user_id` bigint(20) not null comment '用户id',
  `merchant_id` bigint(20) not null comment '商家id',
  `total_amount` decimal(10,2) not null comment '订单总金额',
  `order_status` tinyint(1) not null default '0' comment '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消, 5-已退款',
  `payment_time` datetime default null comment '支付时间',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  `updated_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (`order_id`),
  unique key `uk_order_no` (`order_no`),
  key `idx_user_id` (`user_id`),
  key `idx_merchant_id` (`merchant_id`),
  key `idx_created_time` (`created_time`)
#   constraint `fk_orders_user` foreign key (`user_id`) references `user` (`user_id`) on delete cascade,
#   constraint `fk_orders_merchant` foreign key (`merchant_id`) references `merchant` (`merchant_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='订单表';

-- 6. 订单明细表
drop table if exists `order_item`;
create table `order_item` (
  `item_id` bigint(20) not null auto_increment comment '订单明细id',
  `order_id` bigint(20) not null comment '订单id',
  `sku_id` bigint(20) not null comment 'sku id',
  `quantity` int(11) not null comment '购买数量',
  `unit_price` decimal(10,2) not null comment '商品单价',
  `total_price` decimal(10,2) not null comment '商品总价',
  `remark` varchar(50) default null comment '订单备注',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  primary key (`item_id`),
  key `idx_order_id` (`order_id`),
  key `idx_sku_id` (`sku_id`)
#   constraint `fk_order_item_order` foreign key (`order_id`) references `orders` (`order_id`) on delete cascade,
#   constraint `fk_order_item_sku` foreign key (`sku_id`) references `product_sku` (`sku_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='订单明细表';

-- 7. 账户交易记录表
drop table if exists `account_transaction`;
create table `account_transaction` (
  `transaction_id` bigint(20) not null auto_increment comment '交易记录id',
  `transaction_no` varchar(50) not null comment '交易流水号',
  `account_type` tinyint(1) not null comment '账户类型: 1-用户账户, 2-商家账户',
  `account_id` bigint(20) not null comment '账户id(用户id或商家id)',
  `transaction_type` tinyint(1) not null comment '交易类型: 1-充值, 2-消费, 3-收入, 4-退款',
  `balance_before` decimal(10,2) not null comment '交易前余额',
  `amount` decimal(10,2) not null comment '交易金额',
  `balance_after` decimal(10,2) not null comment '交易后余额',
  `related_id` bigint(20) default null comment '关联id(订单id、充值id等)',
  `related_type` varchar(50) default null comment '关联类型',
  `remark` varchar(50) default null comment '备注',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  primary key (`transaction_id`),
  unique key `uk_transaction_no` (`transaction_no`),
  key `idx_account_settlement` (`account_id`, `created_time`,`transaction_type`),
  key `idx_created_time` (`created_time`),
  key `idx_related` (`related_type`, `related_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='账户交易记录表';

-- 8. 库存变更记录表
drop table if exists `inventory_change`;
create table `inventory_change` (
  `change_id` bigint(20) not null auto_increment comment '库存变更id',
  `sku_id` bigint(20) not null comment 'sku id',
  `change_type` tinyint(1) not null comment '变更类型: 1-增加库存, 2-减少库存(销售), 3-调整库存',
  `stock_before` int(11) not null comment '变更前库存',
  `change_quantity` int(11) not null comment '变更数量',
  `stock_after` int(11) not null comment '变更后库存',
  `related_id` bigint(20) default null comment '关联id(订单id、调整单id等)',
  `related_type` varchar(50) default null comment '关联类型',
  `remark` varchar(500) default null comment '备注',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  primary key (`change_id`),
  key `idx_sku_id` (`sku_id`),
  key `idx_created_time` (`created_time`),
  key `idx_related` (`related_type`, `related_id`)
#   constraint `fk_inventory_change_sku` foreign key (`sku_id`) references `product_sku` (`sku_id`) on delete cascade
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='库存变更记录表';

-- 9. 每日结算表
drop table if exists `daily_settlement`;
create table `daily_settlement` (
  `settlement_id` bigint(20) not null auto_increment comment '结算id',
  `settlement_no` varchar(50) not null comment '结算单号',
  `merchant_id` bigint(20) not null comment '商家id',
  `settlement_date` date not null comment '结算日期',
  `paid_amount` decimal(10,2) not null default '0.00' comment '已支付订单金额',
  `refund_amount` decimal(10,2) not null default '0.00' comment '已退款订单金额',
  `sold_amount` decimal(10,2) not null default '0.00' comment '销售额(已支付的订单+已退款的)',
  `merchant_net_income` decimal(10,2) not null comment '商家账户当日净收入',
  `is_matched` tinyint(1) not null default '0' comment '是否匹配: 0-不匹配, 1-匹配',
  `remark` varchar(500) default null comment '备注',
  `created_time` datetime not null default current_timestamp comment '创建时间',
  primary key (`settlement_id`),
  unique key `uk_settlement_no` (`settlement_no`),
  unique key `uk_merchant_settlement_date` (`merchant_id`, `settlement_date`)
#   constraint `fk_daily_settlement_merchant` foreign key (`merchant_id`) references `merchant` (`merchant_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='每日结算表';

-- 创建索引
create index idx_order_status on orders(order_status);