package com.hospit.validation;

import com.hospit.validation.rule.*;

import java.time.LocalDateTime;
import java.util.*;

// 数据验证引擎
public class ValidationEngine {
    
    private final Map<String, List<ValidationRule>> rules = new HashMap<>();
    
    // 添加验证规则
    public ValidationEngine addRule(String fieldName, ValidationRule rule) {
        rules.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(rule);
        return this;
    }
    
    // 添加必填验证
    public ValidationEngine required(String fieldName) {
        return addRule(fieldName, new RequiredRule(fieldName));
    }
    
    // 添加必填验证（自定义错误消息）
    public ValidationEngine required(String fieldName, String errorMessage) {
        return addRule(fieldName, new RequiredRule(fieldName, errorMessage));
    }
    
    // 添加长度验证
    public ValidationEngine length(String fieldName, Integer minLength, Integer maxLength) {
        return addRule(fieldName, new LengthRule(fieldName, minLength, maxLength));
    }
    
    // 添加最大长度验证
    public ValidationEngine maxLength(String fieldName, int maxLength) {
        return addRule(fieldName, new LengthRule(fieldName, null, maxLength));
    }
    
    // 添加最小长度验证
    public ValidationEngine minLength(String fieldName, int minLength) {
        return addRule(fieldName, new LengthRule(fieldName, minLength, null));
    }
    
    // 添加正则表达式验证
    public ValidationEngine pattern(String fieldName, String regex) {
        return addRule(fieldName, new PatternRule(fieldName, regex));
    }
    
    // 添加正则表达式验证（自定义错误消息）
    public ValidationEngine pattern(String fieldName, String regex, String errorMessage) {
        return addRule(fieldName, new PatternRule(fieldName, regex, errorMessage));
    }
    
    // 添加枚举值验证
    public ValidationEngine enumValues(String fieldName, List<String> allowedValues) {
        return addRule(fieldName, new EnumRule(fieldName, allowedValues));
    }
    
    // 添加日期不能为未来验证
    public ValidationEngine dateNotFuture(String fieldName) {
        return addRule(fieldName, new DateRangeRule(fieldName, false));
    }
    
    // 添加日期范围验证
    public ValidationEngine dateRange(String fieldName, LocalDateTime minDate, LocalDateTime maxDate) {
        return addRule(fieldName, new DateRangeRule(fieldName, minDate, maxDate));
    }
    
    // 执行验证
    public ValidationResult validate(Map<String, Object> data) {
        ValidationResult result = ValidationResult.builder().valid(true).build();
        for (Map.Entry<String, List<ValidationRule>> entry : rules.entrySet()) {
            String fieldName = entry.getKey();
            Object value = data.get(fieldName);
            for (ValidationRule rule : entry.getValue()) {
                List<String> errors = rule.validate(value);
                for (String error : errors) {
                    result.addError(error);
                }
            }
        }
        if (!result.getErrors().isEmpty()) {
            result.setValid(false);
        }
        return result;
    }
    
    // CT检查数据验证引擎
    public static ValidationEngine forCtExamination() {
        ValidationEngine engine = new ValidationEngine();
        engine.required("patientId", "患者ID不能为空")
               .maxLength("patientId", 50)
               .maxLength("examinationNo", 50)
               .maxLength("reportConclusion", 500)
               .dateNotFuture("examinationTime");
        return engine;
    }
    
    // MRI检查数据验证引擎
    public static ValidationEngine forMriExamination() {
        ValidationEngine engine = new ValidationEngine();
        engine.required("patientId", "患者ID不能为空")
               .maxLength("patientId", 50)
               .maxLength("examinationNo", 50)
               .maxLength("reportConclusion", 500)
               .dateNotFuture("examinationTime");
        return engine;
    }
    
    // 病理检查数据验证引擎
    public static ValidationEngine forPathologyExamination() {
        ValidationEngine engine = new ValidationEngine();
        engine.required("patientId", "患者ID不能为空")
               .maxLength("patientId", 50)
               .maxLength("pathologyNo", 50)
               .maxLength("pathologyDiagnosis", 500)
               .dateNotFuture("samplingTime");
        return engine;
    }
    
    // 肠镜检查数据验证引擎
    public static ValidationEngine forEnteroscopyExamination() {
        ValidationEngine engine = new ValidationEngine();
        engine.required("patientId", "患者ID不能为空")
               .maxLength("patientId", 50)
               .maxLength("examinationNo", 50)
               .maxLength("reportConclusion", 500)
               .dateNotFuture("examinationTime");
        return engine;
    }
}