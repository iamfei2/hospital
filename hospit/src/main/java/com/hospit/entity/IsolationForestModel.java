package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("isolation_forest_model")
@Schema(description = "孤立森林模型参数表")
public class IsolationForestModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "model_id", type = IdType.AUTO)
    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "关联规则ID")
    private Long ruleId;

    @Schema(description = "指标ID列表")
    private String itemIds;

    @Schema(description = "模型参数JSON")
    private String modelParams;

    @Schema(description = "训练时间")
    private LocalDateTime trainedAt;

    @Schema(description = "训练样本数量")
    private Integer sampleCount;

    @Schema(description = "训练集平均得分")
    private BigDecimal meanScore;

    @Schema(description = "训练集得分标准差")
    private BigDecimal stdScore;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getItemIds() { return itemIds; }
    public void setItemIds(String itemIds) { this.itemIds = itemIds; }
    public String getModelParams() { return modelParams; }
    public void setModelParams(String modelParams) { this.modelParams = modelParams; }
    public LocalDateTime getTrainedAt() { return trainedAt; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public BigDecimal getMeanScore() { return meanScore; }
    public void setMeanScore(BigDecimal meanScore) { this.meanScore = meanScore; }
    public BigDecimal getStdScore() { return stdScore; }
    public void setStdScore(BigDecimal stdScore) { this.stdScore = stdScore; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
