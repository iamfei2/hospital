package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 检验项目字典表
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@TableName("lab_item_dict")
@Schema(description = "检验项目字典表")
public class LabItemDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检验项目ID（主键）
     */
    @Schema(description = "检验项目ID（主键）")
    @TableId(value = "item_id", type = IdType.AUTO)
    private Integer itemId;

    /**
     * 检验项目名称（如"γ-谷氨酰转移酶"，唯一）
     */
    @Schema(description = "检验项目名称（如\"γ-谷氨酰转移酶\"，唯一）")
    private String itemName;

    /**
     * 检验项目编码（如"GAMMA_GT"，唯一，便于程序识别）
     */
    @Schema(description = "检验项目编码（如\"GAMMA_GT\"，唯一，便于程序识别）")
    private String itemCode;

    /**
     * 默认单位（如"U/L""mmol/L"）
     */
    @Schema(description = "默认单位（如\"U/L\"\"mmol/L\"）")
    private String defaultUnit;

    /**
     * 正常参考范围（如"0-40"，用于患病判断）
     */
    @Schema(description = "正常参考范围（如\"0-40\"，用于患病判断）")
    private String normalRange;

    /**
     * 备注（如"★标记为核心指标"）
     */
    @Schema(description = "备注（如\"★标记为核心指标\"）")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
        return "LabItemDict{" +
            "itemId = " + itemId +
            ", itemName = " + itemName +
            ", itemCode = " + itemCode +
            ", defaultUnit = " + defaultUnit +
            ", normalRange = " + normalRange +
            ", remark = " + remark +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
