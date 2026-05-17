package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 肠镜检查专项表
 * </p>
 *
 * @author iamfei2
 * @since 2026-03-02
 */
@TableName("enteroscopy_examination")
@Schema(description = "肠镜检查专项表")
public class EnteroscopyExamination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 肠镜检查ID（主键）
     */
    @Schema(description = "肠镜检查ID（主键）")
    @TableId(value = "enteroscopy_id", type = IdType.AUTO)
    private Long enteroscopyId;

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
     * 肠镜类型（如"普通肠镜""无痛肠镜"）
     */
    @Schema(description = "肠镜类型（如\"普通肠镜\"\"无痛肠镜\"）")
    private String enteroscopyType;

    /**
     * 操作医生
     */
    @Schema(description = "操作医生")
    private String examineDoctor;

    /**
     * 检查科室
     */
    @Schema(description = "检查科室")
    private String examineDept;

    /**
     * 肠镜报告PDF文件访问链接
     */
    @Schema(description = "肠镜报告PDF文件访问链接")
    private String reportUrl;

    /**
     * 肠镜镜下图片文件访问链接（多文件用逗号分隔）
     */
    @Schema(description = "肠镜镜下图片文件访问链接（多文件用逗号分隔）")
    private String imageUrl;

    /**
     * 镜下诊断与结论（可选录入）
     */
    @Schema(description = "镜下诊断与结论（可选录入）")
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

    public Long getEnteroscopyId() {
        return enteroscopyId;
    }

    public void setEnteroscopyId(Long enteroscopyId) {
        this.enteroscopyId = enteroscopyId;
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

    public String getEnteroscopyType() {
        return enteroscopyType;
    }

    public void setEnteroscopyType(String enteroscopyType) {
        this.enteroscopyType = enteroscopyType;
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
        return "EnteroscopyExamination{" +
            "enteroscopyId = " + enteroscopyId +
            ", patientId = " + patientId +
            ", examinationNo = " + examinationNo +
            ", examinationTime = " + examinationTime +
            ", enteroscopyType = " + enteroscopyType +
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
