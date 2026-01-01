package com.shopping.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ID生成器工具类
 */
@Component
public class IdGenerator {

    @Value("${app.order.prefix:ORD}")
    private String orderPrefix;

    @Value("${app.transaction.prefix:TRX}")
    private String transactionPrefix;

    @Value("${app.settlement.prefix:SET}")
    private String settlementPrefix;

    @Value("${app.order.refund-prefix:REF}")
    private String refundPrefix;

    /**
     * 生成订单号
     */
    public String generateOrderNo() {
        DateTime now = DateUtil.date();
        String dateStr = now.toString(DatePattern.PURE_DATETIME_PATTERN);
//        return orderPrefix + dateStr + IdUtil.getSnowflakeNextIdStr().substring(0, 6);
        String orderNo = orderPrefix + dateStr + IdUtil.getSnowflakeNextIdStr();
        return orderNo;
    }

    /**
     * 生成交易流水号
     */
    public String generateTransactionNo() {
        return transactionPrefix + IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成结算单号
     */
    public String generateSettlementNo() {
        DateTime now = DateUtil.date();
        String dateStr = now.toString(DatePattern.PURE_DATE_PATTERN);
//        return settlementPrefix + dateStr + IdUtil.getSnowflakeNextIdStr().substring(0, 8);
        String res = settlementPrefix + dateStr + IdUtil.getSnowflakeNextIdStr();
        return res;
    }

    /**
     * 生成退款单号
     */
    public String generateRefundNo() {
        DateTime now = DateUtil.date();
        String dateStr = now.toString(DatePattern.PURE_DATETIME_PATTERN);
//        return refundPrefix + dateStr + IdUtil.getSnowflakeNextIdStr().substring(0, 6);
        String res = refundPrefix + dateStr + IdUtil.getSnowflakeNextIdStr();
        return res;
    }
}