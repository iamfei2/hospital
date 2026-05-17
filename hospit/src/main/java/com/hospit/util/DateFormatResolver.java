package com.hospit.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateFormatResolver {

    private static final List<DateTimeFormatter> FORMATTERS = new ArrayList<>();

    static {
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // 解析日期字符串
    public static LocalDateTime parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    // 获取支持的日期格式列表
    public static String getSupportedFormats() {
        return "yyyy-MM-dd HH:mm:ss, yyyy/MM/dd HH:mm:ss, yyyy-MM-dd, yyyy/MM/dd, yyyyMMdd, dd-MM-yyyy, dd/MM/yyyy 等";
    }
}