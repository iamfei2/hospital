package com.hospit.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.time.LocalDateTime;

@ColumnWidth(20)
public class MriExaminationExportVO {

    @ExcelProperty("患者ID")
    private String patientId;

    @ExcelProperty("检查编号")
    private String examinationNo;

    @ExcelProperty("检查时间")
    private LocalDateTime examinationTime;

    @ExcelProperty("检查部位")
    private String examinationPart;

    @ExcelProperty("检查医生")
    private String examineDoctor;

    @ExcelProperty("检查科室")
    private String examineDept;

    @ExcelProperty("报告结论")
    private String reportConclusion;

    @ExcelProperty("上传时间")
    private LocalDateTime uploadTime;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getExaminationNo() { return examinationNo; }
    public void setExaminationNo(String examinationNo) { this.examinationNo = examinationNo; }
    public LocalDateTime getExaminationTime() { return examinationTime; }
    public void setExaminationTime(LocalDateTime examinationTime) { this.examinationTime = examinationTime; }
    public String getExaminationPart() { return examinationPart; }
    public void setExaminationPart(String examinationPart) { this.examinationPart = examinationPart; }
    public String getExamineDoctor() { return examineDoctor; }
    public void setExamineDoctor(String examineDoctor) { this.examineDoctor = examineDoctor; }
    public String getExamineDept() { return examineDept; }
    public void setExamineDept(String examineDept) { this.examineDept = examineDept; }
    public String getReportConclusion() { return reportConclusion; }
    public void setReportConclusion(String reportConclusion) { this.reportConclusion = reportConclusion; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
