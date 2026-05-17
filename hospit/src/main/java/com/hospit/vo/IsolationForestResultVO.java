package com.hospit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "孤立森林联合检测结果")
public class IsolationForestResultVO {

    @Schema(description = "孤立森林得分（0-1，越大越异常）")
    private Double isolationScore;

    @Schema(description = "综合评分")
    private Double combinedScore;

    @Schema(description = "异常阈值")
    private BigDecimal threshold;

    @Schema(description = "异常等级：NORMAL/SUSPICIOUS/ANOMALY")
    private String anomalyLevel;

    @Schema(description = "异常指标列表")
    private List<AnomalyItemVO> anomalyItems;

    @Schema(description = "预警消息")
    private String alertMessage;

    @Schema(description = "是否触发预警")
    private Boolean triggered;

    public Double getIsolationScore() { return isolationScore; }
    public void setIsolationScore(Double isolationScore) { this.isolationScore = isolationScore; }
    public Double getCombinedScore() { return combinedScore; }
    public void setCombinedScore(Double combinedScore) { this.combinedScore = combinedScore; }
    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public String getAnomalyLevel() { return anomalyLevel; }
    public void setAnomalyLevel(String anomalyLevel) { this.anomalyLevel = anomalyLevel; }
    public List<AnomalyItemVO> getAnomalyItems() { return anomalyItems; }
    public void setAnomalyItems(List<AnomalyItemVO> anomalyItems) { this.anomalyItems = anomalyItems; }
    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
    public Boolean getTriggered() { return triggered; }
    public void setTriggered(Boolean triggered) { this.triggered = triggered; }

    @Schema(description = "异常指标详情")
    public static class AnomalyItemVO {
        @Schema(description = "指标ID")
        private Integer itemId;

        @Schema(description = "指标名称")
        private String itemName;

        @Schema(description = "Z-Score值")
        private Double zscore;

        @Schema(description = "实际值")
        private BigDecimal resultValue;

        @Schema(description = "均值")
        private BigDecimal meanValue;

        @Schema(description = "标准差")
        private BigDecimal stdDeviation;

        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public Double getZscore() { return zscore; }
        public void setZscore(Double zscore) { this.zscore = zscore; }
        public BigDecimal getResultValue() { return resultValue; }
        public void setResultValue(BigDecimal resultValue) { this.resultValue = resultValue; }
        public BigDecimal getMeanValue() { return meanValue; }
        public void setMeanValue(BigDecimal meanValue) { this.meanValue = meanValue; }
        public BigDecimal getStdDeviation() { return stdDeviation; }
        public void setStdDeviation(BigDecimal stdDeviation) { this.stdDeviation = stdDeviation; }
    }
}
