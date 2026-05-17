package com.hospit.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;

@ContentRowHeight(20)
@ColumnWidth(20)
public class PathologyImportTemplate {

    @ExcelProperty(value = "患者ID", index = 0)
    private String patientId;

    @ExcelProperty(value = "病理号", index = 1)
    private String pathologyNo;

    @ExcelProperty(value = "标本类型", index = 2)
    private String specimenType;

    @ExcelProperty(value = "标本取样时间", index = 3)
    private String samplingTime;

    @ExcelProperty(value = "病理诊断医生", index = 4)
    private String pathologyDoctor;

    @ExcelProperty(value = "病理科", index = 5)
    private String pathologyDept;

    @ExcelProperty(value = "病理诊断结论", index = 6)
    private String pathologyDiagnosis;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPathologyNo() { return pathologyNo; }
    public void setPathologyNo(String pathologyNo) { this.pathologyNo = pathologyNo; }
    public String getSpecimenType() { return specimenType; }
    public void setSpecimenType(String specimenType) { this.specimenType = specimenType; }
    public String getSamplingTime() { return samplingTime; }
    public void setSamplingTime(String samplingTime) { this.samplingTime = samplingTime; }
    public String getPathologyDoctor() { return pathologyDoctor; }
    public void setPathologyDoctor(String pathologyDoctor) { this.pathologyDoctor = pathologyDoctor; }
    public String getPathologyDept() { return pathologyDept; }
    public void setPathologyDept(String pathologyDept) { this.pathologyDept = pathologyDept; }
    public String getPathologyDiagnosis() { return pathologyDiagnosis; }
    public void setPathologyDiagnosis(String pathologyDiagnosis) { this.pathologyDiagnosis = pathologyDiagnosis; }
}
