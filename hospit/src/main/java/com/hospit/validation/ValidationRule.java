package com.hospit.validation;

import java.util.List;

// 验证规则接口
public interface ValidationRule {
    
    // 获取字段名称
    String getFieldName();
    
    // 执行验证
    List<String> validate(Object value);
    
    // 获取错误代码
    String getErrorCode();
    
    // 获取错误消息
    String getErrorMessage();
}