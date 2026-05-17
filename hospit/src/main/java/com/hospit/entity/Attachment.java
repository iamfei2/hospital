package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 附件存储表（CT影像/病历原图等）
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("attachment")
@Schema(description = "附件存储表（CT影像/病历原图等）")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件ID（主键）
     */
    @Schema(description = "附件ID（主键）")
    @TableId(value = "attachment_id", type = IdType.AUTO)
    private Long attachmentId;

    /**
     * 患者ID（关联t_patient.patient_id）
     */
    @Schema(description = "患者ID（关联t_patient.patient_id）")
    private String patientId;

    /**
     * 附件名称（如"20230917CT影像.png"）
     */
    @Schema(description = "附件名称（如\"20230917CT影像.png\"）")
    private String attachmentName;

    /**
     * 附件类型（如"CT影像""病历原图"）
     */
    @Schema(description = "附件类型（如\"CT影像\"\"病历原图\"）")
    private String attachmentType;

    /**
     * 附件存储路径/访问链接
     */
    @Schema(description = "附件存储路径/访问链接")
    private String storageUrl;

    /**
     * 上传人ID（关联t_user.user_id，当前登录用户）
     */
    @Schema(description = "上传人ID（关联t_user.user_id，当前登录用户）")
    private Integer userId;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 关联检验结果ID（可选，如附件属于某检验）
     */
    @Schema(description = "关联检验结果ID（可选，如附件属于某检验）")
    private Long relatedResultId;

    /**
     * 逻辑删除标识：0=有效，1=作废
     */
    @Schema(description = "逻辑删除标识：0=有效，1=作废")
    private Boolean isInvalid;

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
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

    public Long getRelatedResultId() {
        return relatedResultId;
    }

    public void setRelatedResultId(Long relatedResultId) {
        this.relatedResultId = relatedResultId;
    }

    public Boolean getIsInvalid() {
        return isInvalid;
    }

    public void setIsInvalid(Boolean isInvalid) {
        this.isInvalid = isInvalid;
    }

    @Override
    public String toString() {
        return "Attachment{" +
            "attachmentId = " + attachmentId +
            ", patientId = " + patientId +
            ", attachmentName = " + attachmentName +
            ", attachmentType = " + attachmentType +
            ", storageUrl = " + storageUrl +
            ", userId = " + userId +
            ", uploadTime = " + uploadTime +
            ", relatedResultId = " + relatedResultId +
            ", isInvalid = " + isInvalid +
            "}";
    }
}
