package com.hospit.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private String fieldName;
    private Object rejectedValue;

    public static ValidationResultBuilder builder() { return new ValidationResultBuilder(); }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public Object getRejectedValue() { return rejectedValue; }
    public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }

    public static ValidationResult success() {
        return ValidationResult.builder().valid(true).build();
    }

    public static ValidationResult failure(String fieldName, String error, Object rejectedValue) {
        ValidationResult result = ValidationResult.builder()
                .valid(false)
                .fieldName(fieldName)
                .rejectedValue(rejectedValue)
                .build();
        result.addError(error);
        return result;
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public void merge(ValidationResult other) {
        if (!other.isValid()) {
            this.valid = false;
            if (other.getErrors() != null) {
                if (this.errors == null) {
                    this.errors = new ArrayList<>();
                }
                this.errors.addAll(other.getErrors());
            }
        }
    }

    public static class ValidationResultBuilder {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private String fieldName;
        private Object rejectedValue;

        public ValidationResultBuilder valid(boolean valid) { this.valid = valid; return this; }
        public ValidationResultBuilder errors(List<String> errors) { this.errors = errors; return this; }
        public ValidationResultBuilder fieldName(String fieldName) { this.fieldName = fieldName; return this; }
        public ValidationResultBuilder rejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; return this; }
        public ValidationResult build() {
            ValidationResult r = new ValidationResult();
            r.setValid(valid);
            r.setErrors(errors);
            r.setFieldName(fieldName);
            r.setRejectedValue(rejectedValue);
            return r;
        }
    }
}