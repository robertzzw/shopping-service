package com.shopping.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** 日期工具类 */
public class DateUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 获取当天开始时间（00:00:00） */
    public static LocalDateTime getDayStart(LocalDate date) {
        return date.atStartOfDay();
    }

    /** 获取当天结束时间（23:59:59） */
    public static LocalDateTime getDayEnd(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /** 格式化日期 */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /** 格式化日期时间 */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /** 解析日期 */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /** 解析日期时间 */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }
}