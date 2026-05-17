package com.hospit.statistics;

import java.util.List;
import java.util.Map;

// 维度统计策略接口
public interface DimensionStrategy {
    // 获取维度名称
    String getDimensionName();

    // 获取分组字段
    String getGroupByField(String tableName);

    // 获取时间格式
    String getTimeFormat();

    // 执行聚合统计
    List<Map<String, Object>> aggregate(String tableName, String startTime, String endTime, Map<String, String> filters);
}
