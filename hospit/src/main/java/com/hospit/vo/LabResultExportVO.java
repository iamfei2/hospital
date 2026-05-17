package com.hospit.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ColumnWidth(20)
public class LabResultExportVO {

    @ExcelProperty("患者ID")
    private String patientId;

    @ExcelProperty("患者姓名")
    private String patientName;

    @ExcelProperty("检验项目")
    private String itemName;

    @ExcelProperty("检验结果值")
    private BigDecimal resultValue;

    @ExcelProperty("结果单位")
    private String resultUnit;

    @ExcelProperty("报告时间")
    private LocalDateTime reportTime;

    @ExcelProperty("执行科室")
    private String executeDept;

    @ExcelProperty("执行医生")
    private String executeDoc;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getResultValue() { return resultValue; }
    public void setResultValue(BigDecimal resultValue) { this.resultValue = resultValue; }
    public String getResultUnit() { return resultUnit; }
    public void setResultUnit(String resultUnit) { this.resultUnit = resultUnit; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public String getExecuteDept() { return executeDept; }
    public void setExecuteDept(String executeDept) { this.executeDept = executeDept; }
    public String getExecuteDoc() { return executeDoc; }
    public void setExecuteDoc(String executeDoc) { this.executeDoc = executeDoc; }
}
