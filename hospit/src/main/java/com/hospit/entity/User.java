package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 科室用户登录信息表
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("user")
@Schema(description = "科室用户登录信息表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键）
     */
    @Schema(description = "用户ID（主键）")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 登录账号（唯一，如工号）
     */
    @Schema(description = "登录账号（唯一，如工号）")
    private String loginAccount;

    /**
     * 加密后的登录密码（建议bcrypt/MD5加密）
     */
    @Schema(description = "加密后的登录密码（建议bcrypt/MD5加密）")
    private String loginPassword;

    /**
     * 用户姓名（科室人员名称）
     */
    @Schema(description = "用户姓名（科室人员名称）")
    private String userName;

    /**
     * 账号状态：1=启用，0=禁用
     */
    @Schema(description = "账号状态：1=启用，0=禁用")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Boolean status;

    /**
     * 用户角色：admin=超级管理员，user=普通用户
     */
    @Schema(description = "用户角色：admin=超级管理员，user=普通用户")
    private String role;

    /**
     * 账号创建时间
     */
    @Schema(description = "账号创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 账号更新时间
     */
    @Schema(description = "账号更新时间")
    private LocalDateTime updateTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
        return "User{" +
            "userId = " + userId +
            ", loginAccount = " + loginAccount +
            ", loginPassword = " + loginPassword +
            ", userName = " + userName +
            ", status = " + status +
                ", createTime = " + createTime +
                ", updateTime = " + updateTime +
            "}";
    }
}
