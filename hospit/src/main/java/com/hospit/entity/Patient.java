package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 患者信息表
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("patient")
@Schema(description = "患者信息表")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 患者ID（病案号，与Excel一致）
     */
    @TableId("patient_id")
    @Schema(description = "患者ID（病案号，与Excel一致）")
    private String patientId;

    /**
     * 患者姓名（核心必填字段）
     */
    @Schema(description = "患者姓名（核心必填字段）")
    private String patientName;

    /**
     * 性别（男/女）
     */
    @Schema(description = "性别（男/女）")
    private String gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCard;

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

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
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
        return "Patient{" +
            "patientId = " + patientId +
            ", patientName = " + patientName +
            ", gender = " + gender +
            ", age = " + age +
            ", phone = " + phone +
            ", isInvalid = " + isInvalid +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
