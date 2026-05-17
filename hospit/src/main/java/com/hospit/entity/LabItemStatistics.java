package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检验项目统计基准表
 * 存储每个检验项目的历史均值、标准差等统计信息
 * 用于Z-Score异常检测的动态基准计算
 */
@TableName("lab_item_statistics")
@Schema(description = "检验项目统计表")
public class LabItemStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "stat_id", type = IdType.AUTO)
    @Schema(description = "统计ID")
    private Long statId;

    @Schema(description = "检验项目ID")
    private Integer itemId;

    @Schema(description = "科室代码（GLOBAL表示全院）")
    private String deptCode;

    @Schema(description = "样本数量")
    private Integer sampleCount;

    @Schema(description = "历史均值")
    private BigDecimal meanValue;

    @Schema(description = "历史标准差")
    private BigDecimal stdDeviation;

    @Schema(description = "最小值")
    private BigDecimal minValue;

    @Schema(description = "最大值")
    private BigDecimal maxValue;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public BigDecimal getMeanValue() { return meanValue; }
    public void setMeanValue(BigDecimal meanValue) { this.meanValue = meanValue; }
    public BigDecimal getStdDeviation() { return stdDeviation; }
    public void setStdDeviation(BigDecimal stdDeviation) { this.stdDeviation = stdDeviation; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
