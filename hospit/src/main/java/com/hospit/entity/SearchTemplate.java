package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("search_template")
@Schema(description = "查询模板表")
public class SearchTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.AUTO)
    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型：ct/mri/pathology/enteroscopy/lab")
    private String templateType;

    @Schema(description = "查询条件JSON")
    private String queryConditions;

    @Schema(description = "创建用户ID")
    private Integer userId;

    @Schema(description = "是否共享：0-私有，1-共享")
    private Boolean isShared;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getQueryConditions() {
        return queryConditions;
    }

    public void setQueryConditions(String queryConditions) {
        this.queryConditions = queryConditions;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Boolean getIsShared() {
        return isShared;
    }

    public void setIsShared(Boolean isShared) {
        this.isShared = isShared;
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
}
