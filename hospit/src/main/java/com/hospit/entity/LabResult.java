package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 患者检验结果表
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("lab_result")
@Schema(description = "患者检验结果表")
public class LabResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检验结果ID（主键）
     */
    @Schema(description = "检验结果ID（主键）")
    @TableId(value = "result_id", type = IdType.AUTO)
    private Long resultId;

    /**
     * 患者ID（关联patient.patient_id）
     */
    @Schema(description = "患者ID（关联patient.patient_id）")
    private String patientId;

    /**
     * 检验项目ID（关联lab_item_dict.item_id）
     */
    @Schema(description = "检验项目ID（关联lab_item_dict.item_id）")
    private Integer itemId;

    /**
     * 检验结果值（如16.0、31.0）
     */
    @Schema(description = "检验结果值（如16.0、31.0）")
    private BigDecimal resultValue;

    /**
     * 结果单位（兼容特殊情况，优先与字典表一致）
     */
    @Schema(description = "结果单位（兼容特殊情况，优先与字典表一致）")
    private String resultUnit;

    /**
     * 报告时间（即检查日期，核心必填字段）
     */
    @Schema(description = "报告时间（即检查日期，核心必填字段）")
    private LocalDateTime reportTime;

    /**
     * 执行科室（核心必填字段）
     */
    @Schema(description = "执行科室（核心必填字段）")
    private String executeDept;

    /**
     * 执行医生（核心必填字段）
     */
    @Schema(description = "执行医生（核心必填字段）")
    private String executeDoc;

    /**
     * 逻辑删除标识：0=有效，1=作废
     */
    @Schema(description = "逻辑删除标识：0=有效，1=作废")
    private Boolean isInvalid;

    /**
     * 检验报告PDF链接
     */
    @Schema(description = "检验报告PDF链接")
    private String reportUrl;

    /**
     * 数据创建时间
     */
    @Schema(description = "数据创建时间")
    private LocalDateTime createTime;

    /**
     * 数据更新时间
     */
    @Schema(description = "数据更新时间")
    private LocalDateTime updateTime;

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getResultValue() {
        return resultValue;
    }

    public void setResultValue(BigDecimal resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultUnit() {
        return resultUnit;
    }

    public void setResultUnit(String resultUnit) {
        this.resultUnit = resultUnit;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public String getExecuteDept() {
        return executeDept;
    }

    public void setExecuteDept(String executeDept) {
        this.executeDept = executeDept;
    }

    public String getExecuteDoc() {
        return executeDoc;
    }

    public void setExecuteDoc(String executeDoc) {
        this.executeDoc = executeDoc;
    }

    public Boolean getIsInvalid() {
        return isInvalid;
    }

    public void setIsInvalid(Boolean isInvalid) {
        this.isInvalid = isInvalid;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "LabResult{" +
            "resultId = " + resultId +
            ", patientId = " + patientId +
            ", itemId = " + itemId +
            ", resultValue = " + resultValue +
            ", resultUnit = " + resultUnit +
            ", reportTime = " + reportTime +
            ", executeDept = " + executeDept +
            ", executeDoc = " + executeDoc +
            ", isInvalid = " + isInvalid +
            ", reportUrl = " + reportUrl +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
