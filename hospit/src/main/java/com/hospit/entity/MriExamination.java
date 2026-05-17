package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 核磁(MRI)检查专项表
 * </p>
 *
 * @author iamfei2
 * @since 2026-03-01
 */
@TableName("mri_examination")
@Schema(description = "核磁(MRI)检查专项表")
public class MriExamination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 核磁检查ID（主键）
     */
    @Schema(description = "核磁检查ID（主键）")
    @TableId(value = "mri_id", type = IdType.AUTO)
    private Long mriId;

    /**
     * 患者ID（关联patient.patient_id）
     */
    @Schema(description = "患者ID（关联patient.patient_id）")
    private String patientId;

    /**
     * 检查编号（唯一，对接院内检查系统）
     */
    @Schema(description = "检查编号（唯一，对接院内检查系统）")
    private String examinationNo;

    /**
     * 检查时间
     */
    @Schema(description = "检查时间")
    private LocalDateTime examinationTime;

    /**
     * 检查部位（如"颅脑弥散加权"）
     */
    @Schema(description = "检查部位（如\"颅脑弥散加权\"）")
    private String examinationPart;

    /**
     * 检查医生
     */
    @Schema(description = "检查医生")
    private String examineDoctor;

    /**
     * 检查科室
     */
    @Schema(description = "检查科室")
    private String examineDept;

    /**
     * 核磁报告PDF文件访问链接
     */
    @Schema(description = "核磁报告PDF文件访问链接")
    private String reportUrl;

    /**
     * 核磁影像DICOM/图片文件访问链接（多文件用逗号分隔）
     */
    @Schema(description = "核磁影像DICOM/图片文件访问链接（多文件用逗号分隔）")
    private String imageUrl;

    /**
     * 检查报告结论（可选录入）
     */
    @Schema(description = "检查报告结论（可选录入）")
    private String reportConclusion;

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

    public Long getMriId() {
        return mriId;
    }

    public void setMriId(Long mriId) {
        this.mriId = mriId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getExaminationNo() {
        return examinationNo;
    }

    public void setExaminationNo(String examinationNo) {
        this.examinationNo = examinationNo;
    }

    public LocalDateTime getExaminationTime() {
        return examinationTime;
    }

    public void setExaminationTime(LocalDateTime examinationTime) {
        this.examinationTime = examinationTime;
    }

    public String getExaminationPart() {
        return examinationPart;
    }

    public void setExaminationPart(String examinationPart) {
        this.examinationPart = examinationPart;
    }

    public String getExamineDoctor() {
        return examineDoctor;
    }

    public void setExamineDoctor(String examineDoctor) {
        this.examineDoctor = examineDoctor;
    }

    public String getExamineDept() {
        return examineDept;
    }

    public void setExamineDept(String examineDept) {
        this.examineDept = examineDept;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReportConclusion() {
        return reportConclusion;
    }

    public void setReportConclusion(String reportConclusion) {
        this.reportConclusion = reportConclusion;
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
        return "MriExamination{" +
            "mriId = " + mriId +
            ", patientId = " + patientId +
            ", examinationNo = " + examinationNo +
            ", examinationTime = " + examinationTime +
            ", examinationPart = " + examinationPart +
            ", examineDoctor = " + examineDoctor +
            ", examineDept = " + examineDept +
            ", reportUrl = " + reportUrl +
            ", imageUrl = " + imageUrl +
            ", reportConclusion = " + reportConclusion +
            ", userId = " + userId +
            ", uploadTime = " + uploadTime +
            ", isInvalid = " + isInvalid +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
