package com.hospit.validation.rule;

import com.hospit.validation.ValidationResult;
import com.hospit.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

// 长度验证规则
public class LengthRule implements ValidationRule {
    
    private final String fieldName;
    private final Integer minLength;
    private final Integer maxLength;
    
    public LengthRule(String fieldName, Integer minLength, Integer maxLength) {
        this.fieldName = fieldName;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    @Override
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<>();
        if (value == null) {
            return errors;
        }
        String str = value.toString();
        if (minLength != null && str.length() < minLength) {
            errors.add(fieldName + "长度不能小于" + minLength + "个字符");
        }
        if (maxLength != null && str.length() > maxLength) {
            errors.add(fieldName + "长度不能超过" + maxLength + "个字符");
        }
        return errors;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String getErrorCode() {
        return "LENGTH";
    }
    
    @Override
    public String getErrorMessage() {
        return fieldName + "长度超出允许范围";
    }
}