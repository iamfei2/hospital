package com.hospit.validation.rule;

import com.hospit.validation.ValidationResult;
import com.hospit.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// 正则表达式验证规则
public class PatternRule implements ValidationRule {
    
    private final String fieldName;
    private final String regex;
    private final String errorMessage;
    
    public PatternRule(String fieldName, String regex) {
        this(fieldName, regex, fieldName + "格式不正确");
    }
    
    public PatternRule(String fieldName, String regex, String errorMessage) {
        this.fieldName = fieldName;
        this.regex = regex;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<>();
        if (value == null || value.toString().trim().isEmpty()) {
            return errors;
        }
        if (!Pattern.matches(regex, value.toString())) {
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
        return "PATTERN";
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}