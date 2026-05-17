package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("isolation_forest_rule")
@Schema(description = "孤立森林多指标组合规则表")
public class IsolationForestRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rule_id", type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "指标ID列表，用逗号分隔")
    private String itemIds;

    @Schema(description = "污染率（异常比例估计）")
    private BigDecimal contamination;

    @Schema(description = "异常阈值（0-1之间）")
    private BigDecimal thresholdScore;

    @Schema(description = "严重级别")
    private String severity;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getItemIds() { return itemIds; }
    public void setItemIds(String itemIds) { this.itemIds = itemIds; }
    public BigDecimal getContamination() { return contamination; }
    public void setContamination(BigDecimal contamination) { this.contamination = contamination; }
    public BigDecimal getThresholdScore() { return thresholdScore; }
    public void setThresholdScore(BigDecimal thresholdScore) { this.thresholdScore = thresholdScore; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
