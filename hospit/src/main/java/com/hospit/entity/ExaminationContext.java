package com.hospit.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "检查结果上下文（用于预警引擎）")
public class ExaminationContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "患者ID")
    private String patientId;

    @Schema(description = "检查类型: CT/MRI/PATHOLOGY/ENTEROSCOPY")
    private String examinationType;

    @Schema(description = "检查记录ID")
    private Long examinationId;

    @Schema(description = "报告结论文本")
    private String reportConclusion;

    @Schema(description = "报告时间")
    private LocalDateTime reportTime;

    @Schema(description = "报告URL")
    private String reportUrl;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getExaminationType() { return examinationType; }
    public void setExaminationType(String examinationType) { this.examinationType = examinationType; }
    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
    public String getReportConclusion() { return reportConclusion; }
    public void setReportConclusion(String reportConclusion) { this.reportConclusion = reportConclusion; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
}
