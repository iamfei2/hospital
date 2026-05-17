package com.hospit.validation.rule;

import com.hospit.validation.ValidationRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumRule implements ValidationRule {
    
    private final String fieldName;
    private final List<String> allowedValues;
    private final boolean caseSensitive;
    
    public EnumRule(String fieldName, List<String> allowedValues) {
        this(fieldName, allowedValues, false);
    }
    
    public EnumRule(String fieldName, List<String> allowedValues, boolean caseSensitive) {
        this.fieldName = fieldName;
        this.allowedValues = allowedValues;
        this.caseSensitive = caseSensitive;
    }
    
    @SafeVarargs
    public static <T> EnumRule of(String fieldName, T... allowedValues) {
        List<String> values = Arrays.stream(allowedValues)
                .map(Object::toString)
                .collect(Collectors.toList());
        return new EnumRule(fieldName, values);
    }
    
    @Override
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<>();
        if (value == null || value.toString().trim().isEmpty()) {
            return errors;
        }
        String strValue = value.toString();
        boolean found = caseSensitive 
                ? allowedValues.contains(strValue)
                : allowedValues.stream().anyMatch(v -> v.equalsIgnoreCase(strValue));
        if (!found) {
            String allowed = String.join(", ", allowedValues);
            errors.add(fieldName + "必须是以下值之一: " + allowed);
        }
        return errors;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String getErrorCode() {
        return "ENUM";
    }
    
    @Override
    public String getErrorMessage() {
        return fieldName + "值不在允许的范围内";
    }
}