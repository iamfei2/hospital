package com.hospit.util;

import java.util.Arrays;
import java.util.List;

public class FieldAliasMapping {

    public static final List<String> PATIENT_ID_ALIASES = Arrays.asList(
            "患者ID", "patient_id", "PID", "病人ID", "patientid", "patientId", "患者编号", "病人编号"
    );

    public static final List<String> EXAMINATION_NO_ALIASES = Arrays.asList(
            "检查编号", "examination_no", "检查号", "单号", "编号", "examinationNo"
    );

    public static final List<String> PATHOLOGY_NO_ALIASES = Arrays.asList(
            "病理号", "pathology_no", "病理编号", "pathologyNo"
    );

    public static final List<String> EXAMINATION_TIME_ALIASES = Arrays.asList(
            "检查时间", "examination_time", "检查日期", "日期", "检查日期时间", "examinationTime"
    );

    public static final List<String> SAMPLING_TIME_ALIASES = Arrays.asList(
            "采样时间", "sampling_time", "采样日期", "samplingTime"
    );

    public static final List<String> EXAMINATION_PART_ALIASES = Arrays.asList(
            "检查部位", "examination_part", "部位", "检查位置", "examinationPart"
    );

    public static final List<String> EXAMINE_DOCTOR_ALIASES = Arrays.asList(
            "检查医生", "examine_doctor", "医生", "检查医师", "examineDoctor"
    );

    public static final List<String> EXAMINE_DEPT_ALIASES = Arrays.asList(
            "检查科室", "examine_dept", "科室", "部门", "examineDept"
    );

    public static final List<String> REPORT_CONCLUSION_ALIASES = Arrays.asList(
            "报告结论", "report_conclusion", "结论", "检查结论", "报告", "reportConclusion"
    );

    public static final List<String> SPECIMEN_TYPE_ALIASES = Arrays.asList(
            "标本类型", "specimen_type", "标本", "specimenType"
    );

    public static final List<String> PATHOLOGY_DOCTOR_ALIASES = Arrays.asList(
            "病理医生", "pathology_doctor", "病理医师", "pathologyDoctor"
    );

    public static final List<String> PATHOLOGY_DEPT_ALIASES = Arrays.asList(
            "病理科室", "pathology_dept", "病理科", "pathologyDept"
    );

    public static final List<String> PATHOLOGY_DIAGNOSIS_ALIASES = Arrays.asList(
            "病理诊断", "pathology_diagnosis", "诊断", "病理结论", "pathologyDiagnosis"
    );

    public static final List<String> ENTEROSCOPY_TYPE_ALIASES = Arrays.asList(
            "肠镜类型", "enteroscopy_type", "类型", "enteroscopyType"
    );

    // 匹配标准字段名
    public static String matchStandardField(String headerName, List<String> standardFieldNames) {
        if (headerName == null || headerName.trim().isEmpty()) {
            return null;
        }
        String normalized = headerName.trim().toLowerCase().replace(" ", "");

        for (String fieldName : standardFieldNames) {
            if (fieldName.equalsIgnoreCase(headerName) || fieldName.equalsIgnoreCase(headerName.trim())) {
                return fieldName;
            }
            String fieldNormalized = fieldName.toLowerCase().replace(" ", "");
            if (normalized.equals(fieldNormalized)) {
                return fieldName;
            }
        }
        return null;
    }

    // 匹配别名
    public static String matchAlias(String headerName, List<String> aliases) {
        if (headerName == null || headerName.trim().isEmpty()) {
            return null;
        }
        String normalized = headerName.trim().toLowerCase().replace(" ", "");

        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(headerName) || alias.equalsIgnoreCase(headerName.trim())) {
                return alias;
            }
            String aliasNormalized = alias.toLowerCase().replace(" ", "");
            if (normalized.equals(aliasNormalized)) {
                return alias;
            }
        }
        return null;
    }
}