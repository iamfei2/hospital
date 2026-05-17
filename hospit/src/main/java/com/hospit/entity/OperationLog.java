package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志表（全量操作留痕）
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("operation_log")
@Schema(description = "操作日志表（全量操作留痕）")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID（主键）
     */
    @Schema(description = "日志ID（主键）")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 操作人ID（关联t_user.user_id，当前登录用户）
     */
    @Schema(description = "操作人ID（关联t_user.user_id，当前登录用户）")
    private Integer userId;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    /**
     * 操作类型：新增/修改/作废
     */
    @Schema(description = "操作类型：新增/修改/作废")
    private String operationType;

    /**
     * 操作的表名（如"t_lab_result""t_patient"）
     */
    @Schema(description = "操作的表名（如\"t_lab_result\"\"t_patient\"）")
    private String operatedTable;

    /**
     * 关联的记录ID（如result_id、patient_id）
     */
    @Schema(description = "关联的记录ID（如result_id、patient_id）")
    private String relatedRecordId;

    /**
     * 修改前内容（JSON格式，如{"result_value":"15.0"}）
     */
    @Schema(description = "修改前内容（JSON格式，如{\"result_value\":\"15.0\"}）")
    private String beforeContent;

    /**
     * 修改后内容（JSON格式）
     */
    @Schema(description = "修改后内容（JSON格式）")
    private String afterContent;

    /**
     * 操作备注（如"修正检验结果录入错误"）
     */
    @Schema(description = "操作备注（如\"修正检验结果录入错误\"）")
    private String remark;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperatedTable() {
        return operatedTable;
    }

    public void setOperatedTable(String operatedTable) {
        this.operatedTable = operatedTable;
    }

    public String getRelatedRecordId() {
        return relatedRecordId;
    }

    public void setRelatedRecordId(String relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public String getBeforeContent() {
        return beforeContent;
    }

    public void setBeforeContent(String beforeContent) {
        this.beforeContent = beforeContent;
    }

    public String getAfterContent() {
        return afterContent;
    }

    public void setAfterContent(String afterContent) {
        this.afterContent = afterContent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
            "logId = " + logId +
            ", userId = " + userId +
            ", operationTime = " + operationTime +
            ", operationType = " + operationType +
            ", operatedTable = " + operatedTable +
            ", relatedRecordId = " + relatedRecordId +
            ", beforeContent = " + beforeContent +
            ", afterContent = " + afterContent +
            ", remark = " + remark +
            "}";
    }
}
