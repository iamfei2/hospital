package com.hospit.rules;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class LabResultFacts {
    private String patientId;
    private String patientName;
    private Long resultId;
    private Integer itemId;
    private String itemName;
    private BigDecimal resultValue;
    private String resultUnit;
    private String normalRange;
    private LocalDateTime reportTime;
    private String executeDept;
    private String executeDoc;
    private String reportConclusion;
    private String examinationType;
    private Long examinationId;
    private Map<String, BigDecimal> recentItemValues;
    private Map<String, BigDecimal> previousItemValues;

    public LabResultFacts() {
        this.recentItemValues = new java.util.HashMap<>();
        this.previousItemValues = new java.util.HashMap<>();
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getResultValue() { return resultValue; }
    public void setResultValue(BigDecimal resultValue) { this.resultValue = resultValue; }
    public String getResultUnit() { return resultUnit; }
    public void setResultUnit(String resultUnit) { this.resultUnit = resultUnit; }
    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public String getExecuteDept() { return executeDept; }
    public void setExecuteDept(String executeDept) { this.executeDept = executeDept; }
    public String getExecuteDoc() { return executeDoc; }
    public void setExecuteDoc(String executeDoc) { this.executeDoc = executeDoc; }
    public String getReportConclusion() { return reportConclusion; }
    public void setReportConclusion(String reportConclusion) { this.reportConclusion = reportConclusion; }
    public String getExaminationType() { return examinationType; }
    public void setExaminationType(String examinationType) { this.examinationType = examinationType; }
    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
    public Map<String, BigDecimal> getRecentItemValues() { return recentItemValues; }
    public void setRecentItemValues(Map<String, BigDecimal> recentItemValues) { this.recentItemValues = recentItemValues; }
    public Map<String, BigDecimal> getPreviousItemValues() { return previousItemValues; }
    public void setPreviousItemValues(Map<String, BigDecimal> previousItemValues) { this.previousItemValues = previousItemValues; }

    // 获取项目的前一个值
    public BigDecimal getPreviousValueForItem(Integer itemId) {
        return previousItemValues.get(String.valueOf(itemId));
    }

    // 获取变化百分比
    public BigDecimal getChangePercent(Integer itemId) {
        BigDecimal previous = getPreviousValueForItem(itemId);
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal current = recentItemValues.get(String.valueOf(itemId));
        if (current == null) {
            return null;
        }
        return current.subtract(previous)
                .abs()
                .multiply(new BigDecimal("100"))
                .divide(previous.abs(), 2, BigDecimal.ROUND_HALF_UP);
    }

    // 检查值是否超出正常范围上限
    public boolean isValueAboveRange(BigDecimal value, String normalRange) {
        if (value == null || normalRange == null || normalRange.isEmpty()) {
            return false;
        }
        try {
            String[] parts = normalRange.split("-");
            if (parts.length == 2) {
                BigDecimal high = new BigDecimal(parts[1].trim());
                return value.compareTo(high) > 0;
            }
        } catch (Exception e) {
        }
        return false;
    }

    // 检查值是否超出正常范围下限
    public boolean isValueBelowRange(BigDecimal value, String normalRange) {
        if (value == null || normalRange == null || normalRange.isEmpty()) {
            return false;
        }
        try {
            String[] parts = normalRange.split("-");
            if (parts.length == 2) {
                BigDecimal low = new BigDecimal(parts[0].trim());
                return value.compareTo(low) < 0;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
