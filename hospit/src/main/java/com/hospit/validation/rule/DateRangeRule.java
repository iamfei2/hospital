package com.hospit.validation.rule;

import com.hospit.validation.ValidationRule;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateRangeRule implements ValidationRule {
    
    private final String fieldName;
    private final LocalDateTime minDate;
    private final LocalDateTime maxDate;
    private final boolean allowFuture;
    
    public DateRangeRule(String fieldName) {
        this(fieldName, null, null, false);
    }
    
    public DateRangeRule(String fieldName, boolean allowFuture) {
        this(fieldName, null, null, allowFuture);
    }
    
    public DateRangeRule(String fieldName, LocalDateTime minDate, LocalDateTime maxDate) {
        this(fieldName, minDate, maxDate, false);
    }
    
    public DateRangeRule(String fieldName, LocalDateTime minDate, LocalDateTime maxDate, boolean allowFuture) {
        this.fieldName = fieldName;
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.allowFuture = allowFuture;
    }
    
    @Override
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<>();
        if (value == null || value.toString().trim().isEmpty()) {
            return errors;
        }
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(value.toString());
        } catch (DateTimeParseException e) {
            errors.add(fieldName + "日期格式错误");
            return errors;
        }
        if (!allowFuture && dateTime.isAfter(LocalDateTime.now())) {
            errors.add(fieldName + "不能晚于当前时间");
        }
        if (minDate != null && dateTime.isBefore(minDate)) {
            errors.add(fieldName + "不能早于" + minDate);
        }
        if (maxDate != null && dateTime.isAfter(maxDate)) {
            errors.add(fieldName + "不能晚于" + maxDate);
        }
        return errors;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String getErrorCode() {
        return "DATE_RANGE";
    }
    
    @Override
    public String getErrorMessage() {
        return fieldName + "日期超出允许范围";
    }
}