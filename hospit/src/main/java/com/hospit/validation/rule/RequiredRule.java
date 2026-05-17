package com.hospit.validation.rule;

import com.hospit.validation.ValidationResult;
import com.hospit.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

// 必填验证规则
public class RequiredRule implements ValidationRule {
    
    private final String fieldName;
    private final String errorMessage;
    
    public RequiredRule(String fieldName) {
        this(fieldName, fieldName + "不能为空");
    }
    
    public RequiredRule(String fieldName, String errorMessage) {
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<>();
        if (value == null) {
            errors.add(errorMessage);
            return errors;
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            errors.add(errorMessage);
        }
        return errors;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String getErrorCode() {
        return "REQUIRED";
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}