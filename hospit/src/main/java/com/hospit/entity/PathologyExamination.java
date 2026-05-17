package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 病理检查专项表
 * </p>
 *
 * @author iamfei2
 * @since 2026-03-01
 */
@TableName("pathology_examination")
@Schema(description = "病理检查专项表")
public class PathologyExamination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 病理检查ID（主键）
     */
    @Schema(description = "病理检查ID（主键）")
    @TableId(value = "pathology_id", type = IdType.AUTO)
    private Long pathologyId;

    /**
     * 患者ID（关联patient.patient_id）
     */
    @Schema(description = "患者ID（关联patient.patient_id）")
    private String patientId;

    /**
     * 病理号（唯一，院内病理系统核心编号）
     */
    @Schema(description = "病理号（唯一，院内病理系统核心编号）")
    private String pathologyNo;

    /**
     * 标本类型（如"结肠活检组织"）
     */
    @Schema(description = "标本类型（如\"结肠活检组织\"）")
    private String specimenType;

    /**
     * 标本取样时间
     */
    @Schema(description = "标本取样时间")
    private LocalDateTime samplingTime;

    /**
     * 病理报告出具时间
     */
    @Schema(description = "病理报告出具时间")
    private LocalDateTime reportTime;

    /**
     * 病理诊断医生
     */
    @Schema(description = "病理诊断医生")
    private String pathologyDoctor;

    /**
     * 病理科
     */
    @Schema(description = "病理科")
    private String pathologyDept;

    /**
     * 病理报告PDF文件访问链接
     */
    @Schema(description = "病理报告PDF文件访问链接")
    private String reportUrl;

    /**
     * 病理切片图片文件访问链接（多文件用逗号分隔）
     */
    @Schema(description = "病理切片图片文件访问链接（多文件用逗号分隔）")
    private String slideImageUrl;

    /**
     * 病理诊断结论（可选录入）
     */
    @Schema(description = "病理诊断结论（可选录入）")
    private String pathologyDiagnosis;

    /**
     * 上传人ID（关联user.user_id，当前登录用户）
     */
    @Schema(description = "上传人ID（关联user.user_id，当前登录用户）")
    private Integer userId;

    /**
     * 文件上传时间
     */
    @Schema(description = "文件上传时间")
    private LocalDateTime uploadTime;

    /**
     * 逻辑删除标识：0=有效，1=作废
     */
    @Schema(description = "逻辑删除标识：0=有效，1=作废")
    private Boolean isInvalid;

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

    public Long getPathologyId() {
        return pathologyId;
    }

    public void setPathologyId(Long pathologyId) {
        this.pathologyId = pathologyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPathologyNo() {
        return pathologyNo;
    }

    public void setPathologyNo(String pathologyNo) {
        this.pathologyNo = pathologyNo;
    }

    public String getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(String specimenType) {
        this.specimenType = specimenType;
    }

    public LocalDateTime getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(LocalDateTime samplingTime) {
        this.samplingTime = samplingTime;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public String getPathologyDoctor() {
        return pathologyDoctor;
    }

    public void setPathologyDoctor(String pathologyDoctor) {
        this.pathologyDoctor = pathologyDoctor;
    }

    public String getPathologyDept() {
        return pathologyDept;
    }

    public void setPathologyDept(String pathologyDept) {
        this.pathologyDept = pathologyDept;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getSlideImageUrl() {
        return slideImageUrl;
    }

    public void setSlideImageUrl(String slideImageUrl) {
        this.slideImageUrl = slideImageUrl;
    }

    public String getPathologyDiagnosis() {
        return pathologyDiagnosis;
    }

    public void setPathologyDiagnosis(String pathologyDiagnosis) {
        this.pathologyDiagnosis = pathologyDiagnosis;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Boolean getIsInvalid() {
        return isInvalid;
    }

    public void setIsInvalid(Boolean isInvalid) {
        this.isInvalid = isInvalid;
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
        return "PathologyExamination{" +
            "pathologyId = " + pathologyId +
            ", patientId = " + patientId +
            ", pathologyNo = " + pathologyNo +
            ", specimenType = " + specimenType +
            ", samplingTime = " + samplingTime +
            ", reportTime = " + reportTime +
            ", pathologyDoctor = " + pathologyDoctor +
            ", pathologyDept = " + pathologyDept +
            ", reportUrl = " + reportUrl +
            ", slideImageUrl = " + slideImageUrl +
            ", pathologyDiagnosis = " + pathologyDiagnosis +
            ", userId = " + userId +
            ", uploadTime = " + uploadTime +
            ", isInvalid = " + isInvalid +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
