package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则配置表
 * 支持4种规则类型：LAB（检验指标）、KEYWORD（关键词）、MISSING（数据缺失）、OPERATION（操作异常）
 * 支持多种条件类型：ABOVE/BELOW/RANGE/TREND_UP/TREND_DOWN/ZSCORE_ABOVE/ZSCORE_BELOW
 */
@TableName("warning_rule")
@Schema(description = "预警规则表")
public class WarningRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rule_id", type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    /** 规则类型：LAB/KEYWORD/MISSING/OPERATION */
    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "检验项目ID")
    private Integer itemId;

    /** 条件类型：ABOVE/BELOW/RANGE/TREND_UP/TREND_DOWN/ZSCORE_ABOVE/ZSCORE_BELOW */
    @Schema(description = "条件类型")
    private String conditionType;

    @Schema(description = "阈值下限")
    private BigDecimal thresholdLow;

    @Schema(description = "阈值上限")
    private BigDecimal thresholdHigh;

    @Schema(description = "缺失天数")
    private Integer missingDays;

    /** 严重程度：CRITICAL/WARNING/INFO */
    @Schema(description = "严重程度")
    private String severity;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "下班时间（小时，0-23，默认22）")
    private Integer offHourStart;

    @Schema(description = "上班时间（小时，0-23，默认6）")
    private Integer offHourEnd;

    @Schema(description = "休息日，逗号分隔，如0,6表示周日和周六")
    private String offDays;

    @Schema(description = "组合条件表达式(JSON)，支持AND/OR组合")
    private String ruleExpression;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public BigDecimal getThresholdLow() { return thresholdLow; }
    public void setThresholdLow(BigDecimal thresholdLow) { this.thresholdLow = thresholdLow; }
    public BigDecimal getThresholdHigh() { return thresholdHigh; }
    public void setThresholdHigh(BigDecimal thresholdHigh) { this.thresholdHigh = thresholdHigh; }
    public Integer getMissingDays() { return missingDays; }
    public void setMissingDays(Integer missingDays) { this.missingDays = missingDays; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getOffHourStart() { return offHourStart; }
    public void setOffHourStart(Integer offHourStart) { this.offHourStart = offHourStart; }
    public Integer getOffHourEnd() { return offHourEnd; }
    public void setOffHourEnd(Integer offHourEnd) { this.offHourEnd = offHourEnd; }
    public String getOffDays() { return offDays; }
    public void setOffDays(String offDays) { this.offDays = offDays; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getRuleExpression() { return ruleExpression; }
    public void setRuleExpression(String ruleExpression) { this.ruleExpression = ruleExpression; }
}
