package com.hospit.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.time.LocalDateTime;

@ColumnWidth(20)
public class PathologyExaminationExportVO {

    @ExcelProperty("患者ID")
    private String patientId;

    @ExcelProperty("病理号")
    private String pathologyNo;

    @ExcelProperty("标本类型")
    private String specimenType;

    @ExcelProperty("标本取样时间")
    private LocalDateTime samplingTime;

    @ExcelProperty("报告时间")
    private LocalDateTime reportTime;

    @ExcelProperty("病理诊断医生")
    private String pathologyDoctor;

    @ExcelProperty("病理科")
    private String pathologyDept;

    @ExcelProperty("病理诊断结论")
    private String pathologyDiagnosis;

    @ExcelProperty("上传时间")
    private LocalDateTime uploadTime;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPathologyNo() { return pathologyNo; }
    public void setPathologyNo(String pathologyNo) { this.pathologyNo = pathologyNo; }
    public String getSpecimenType() { return specimenType; }
    public void setSpecimenType(String specimenType) { this.specimenType = specimenType; }
    public LocalDateTime getSamplingTime() { return samplingTime; }
    public void setSamplingTime(LocalDateTime samplingTime) { this.samplingTime = samplingTime; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public String getPathologyDoctor() { return pathologyDoctor; }
    public void setPathologyDoctor(String pathologyDoctor) { this.pathologyDoctor = pathologyDoctor; }
    public String getPathologyDept() { return pathologyDept; }
    public void setPathologyDept(String pathologyDept) { this.pathologyDept = pathologyDept; }
    public String getPathologyDiagnosis() { return pathologyDiagnosis; }
    public void setPathologyDiagnosis(String pathologyDiagnosis) { this.pathologyDiagnosis = pathologyDiagnosis; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
