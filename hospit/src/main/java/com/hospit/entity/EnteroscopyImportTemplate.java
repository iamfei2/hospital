package com.hospit.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;

@ContentRowHeight(20)
@ColumnWidth(20)
public class EnteroscopyImportTemplate {

    @ExcelProperty(value = "患者ID", index = 0)
    private String patientId;

    @ExcelProperty(value = "检查编号", index = 1)
    private String examinationNo;

    @ExcelProperty(value = "检查时间", index = 2)
    private String examinationTime;

    @ExcelProperty(value = "肠镜类型", index = 3)
    private String enteroscopyType;

    @ExcelProperty(value = "操作医生", index = 4)
    private String examineDoctor;

    @ExcelProperty(value = "检查科室", index = 5)
    private String examineDept;

    @ExcelProperty(value = "报告结论", index = 6)
    private String reportConclusion;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getExaminationNo() { return examinationNo; }
    public void setExaminationNo(String examinationNo) { this.examinationNo = examinationNo; }
    public String getExaminationTime() { return examinationTime; }
    public void setExaminationTime(String examinationTime) { this.examinationTime = examinationTime; }
    public String getEnteroscopyType() { return enteroscopyType; }
    public void setEnteroscopyType(String enteroscopyType) { this.enteroscopyType = enteroscopyType; }
    public String getExamineDoctor() { return examineDoctor; }
    public void setExamineDoctor(String examineDoctor) { this.examineDoctor = examineDoctor; }
    public String getExamineDept() { return examineDept; }
    public void setExamineDept(String examineDept) { this.examineDept = examineDept; }
    public String getReportConclusion() { return reportConclusion; }
    public void setReportConclusion(String reportConclusion) { this.reportConclusion = reportConclusion; }
}
