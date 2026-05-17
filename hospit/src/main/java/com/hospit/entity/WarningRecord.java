package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录表 - 存储所有触发的预警信息
 * 可查询、可标记已读、支持WebSocket实时推送
 */
@TableName("warning_record")
@Schema(description = "预警记录表")
public class WarningRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "warning_id", type = IdType.AUTO)
    @Schema(description = "预警记录ID")
    private Long warningId;

    @Schema(description = "患者ID")
    private String patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "检验项目ID")
    private Integer itemId;

    @Schema(description = "检验项目名称")
    private String itemName;

    @Schema(description = "关联的检验结果ID")
    private Long resultId;

    @Schema(description = "检查类型(CT/MRI/PATHOLOGY/ENTEROSCOPY)")
    private String examinationType;

    @Schema(description = "关联的检查记录ID")
    private Long examinationId;

    @Schema(description = "触发预警的规则ID")
    private Long ruleId;

    /** 规则类型：LAB/KEYWORD/MISSING/OPERATION */
    @Schema(description = "规则类型")
    private String ruleType;

    /** 严重程度：CRITICAL/WARNING/INFO */
    @Schema(description = "严重程度")
    private String severity;

    @Schema(description = "预警消息")
    private String message;

    @Schema(description = "检验结果值")
    private BigDecimal resultValue;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public Long getWarningId() { return warningId; }
    public void setWarningId(Long warningId) { this.warningId = warningId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public String getExaminationType() { return examinationType; }
    public void setExaminationType(String examinationType) { this.examinationType = examinationType; }
    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getResultValue() { return resultValue; }
    public void setResultValue(BigDecimal resultValue) { this.resultValue = resultValue; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
