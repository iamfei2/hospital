package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 医嘱信息表
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("medical_order")
@Schema(description = "医嘱信息表")
public class MedicalOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 医嘱ID（主键）
     */
    @Schema(description = "医嘱ID（主键）")
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 住院次
     */
    @Schema(description = "住院次")
    private Integer hospitalizationTimes;

    /**
     * 患者ID（关联t_patient.patient_id）
     */
    @Schema(description = "患者ID（关联t_patient.patient_id）")
    private String patientId;

    /**
     * 医嘱项名称（如"胰岛素注射液"）
     */
    @Schema(description = "医嘱项名称（如\"胰岛素注射液\"）")
    private String orderName;

    /**
     * 医嘱开始时间
     */
    @Schema(description = "医嘱开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 医嘱结束时间
     */
    @Schema(description = "医嘱结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 医嘱状态（如"停止"）
     */
    @Schema(description = "医嘱状态（如\"停止\"）")
    private String orderStatus;

    /**
     * 医嘱频率（如"一次"）
     */
    @Schema(description = "医嘱频率（如\"一次\"）")
    private String orderFrequency;

    /**
     * 执行科室（可选补充）
     */
    @Schema(description = "执行科室（可选补充）")
    private String executeDept;

    /**
     * 执行医生（可选补充）
     */
    @Schema(description = "执行医生（可选补充）")
    private String executeDoc;

    /**
     * 逻辑删除标识：0=有效，1=作废
     */
    @Schema(description = "逻辑删除标识：0=有效，1=作废")
    private int isInvalid;

    /**
     * 数据创建时间
     */
    @Schema(description = "数据创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 数据更新时间
     */
    @Schema(description = "数据更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getHospitalizationTimes() {
        return hospitalizationTimes;
    }

    public void setHospitalizationTimes(Integer hospitalizationTimes) {
        this.hospitalizationTimes = hospitalizationTimes;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderFrequency() {
        return orderFrequency;
    }

    public void setOrderFrequency(String orderFrequency) {
        this.orderFrequency = orderFrequency;
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

    public int getIsInvalid() {
        return isInvalid;
    }

    public void setIsInvalid(int isInvalid) {
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
        return "MedicalOrder{" +
            "orderId = " + orderId +
            ", hospitalizationTimes = " + hospitalizationTimes +
            ", patientId = " + patientId +
            ", orderName = " + orderName +
            ", startTime = " + startTime +
            ", endTime = " + endTime +
            ", orderStatus = " + orderStatus +
            ", orderFrequency = " + orderFrequency +
            ", executeDept = " + executeDept +
            ", executeDoc = " + executeDoc +
            ", isInvalid = " + isInvalid +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
