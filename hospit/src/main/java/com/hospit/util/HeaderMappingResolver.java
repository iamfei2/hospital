package com.hospit.util;

import com.hospit.vo.HeaderMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeaderMappingResolver {

    // 解析Excel表头映射关系
    public static HeaderMapping resolve(List<String> headers, Map<String, List<String>> fieldToAliases) {
        HeaderMapping mapping = new HeaderMapping();

        for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
            String header = headers.get(colIndex);
            String matchedField = matchField(header, fieldToAliases);
            if (matchedField != null) {
                mapping.put(colIndex, matchedField);
            }
        }

        return mapping;
    }

    // 匹配表头字段
    private static String matchField(String header, Map<String, List<String>> fieldToAliases) {
        if (header == null || header.trim().isEmpty()) {
            return null;
        }

        for (Map.Entry<String, List<String>> entry : fieldToAliases.entrySet()) {
            String standardField = entry.getKey();
            List<String> aliases = entry.getValue();

            if (aliases.contains(header)) {
                return standardField;
            }

            String matchedAlias = FieldAliasMapping.matchAlias(header, aliases);
            if (matchedAlias != null) {
                return standardField;
            }
        }

        String normalizedHeader = header.trim().toLowerCase().replace(" ", "").replace("_", "");
        for (Map.Entry<String, List<String>> entry : fieldToAliases.entrySet()) {
            String standardField = entry.getKey();
            if (standardField.equalsIgnoreCase(header) || 
                standardField.toLowerCase().replace("_", "").equals(normalizedHeader)) {
                return standardField;
            }
        }

        return null;
    }

    // 解析CT检查表头
    public static HeaderMapping resolveCtHeaders(List<String> headers) {
        return resolve(headers, getCtFieldToAliases());
    }

    // 解析MRI检查表头
    public static HeaderMapping resolveMriHeaders(List<String> headers) {
        return resolve(headers, getMriFieldToAliases());
    }

    // 解析病理检查表头
    public static HeaderMapping resolvePathologyHeaders(List<String> headers) {
        return resolve(headers, getPathologyFieldToAliases());
    }

    // 解析肠镜检查表头
    public static HeaderMapping resolveEnteroscopyHeaders(List<String> headers) {
        return resolve(headers, getEnteroscopyFieldToAliases());
    }

    private static Map<String, List<String>> getCtFieldToAliases() {
        return Map.of(
                "patientId", FieldAliasMapping.PATIENT_ID_ALIASES,
                "examinationNo", FieldAliasMapping.EXAMINATION_NO_ALIASES,
                "examinationTime", FieldAliasMapping.EXAMINATION_TIME_ALIASES,
                "examinationPart", FieldAliasMapping.EXAMINATION_PART_ALIASES,
                "examineDoctor", FieldAliasMapping.EXAMINE_DOCTOR_ALIASES,
                "examineDept", FieldAliasMapping.EXAMINE_DEPT_ALIASES,
                "reportConclusion", FieldAliasMapping.REPORT_CONCLUSION_ALIASES
        );
    }

    private static Map<String, List<String>> getMriFieldToAliases() {
        return Map.of(
                "patientId", FieldAliasMapping.PATIENT_ID_ALIASES,
                "examinationNo", FieldAliasMapping.EXAMINATION_NO_ALIASES,
                "examinationTime", FieldAliasMapping.EXAMINATION_TIME_ALIASES,
                "examinationPart", FieldAliasMapping.EXAMINATION_PART_ALIASES,
                "examineDoctor", FieldAliasMapping.EXAMINE_DOCTOR_ALIASES,
                "examineDept", FieldAliasMapping.EXAMINE_DEPT_ALIASES,
                "reportConclusion", FieldAliasMapping.REPORT_CONCLUSION_ALIASES
        );
    }

    private static Map<String, List<String>> getPathologyFieldToAliases() {
        return Map.of(
                "patientId", FieldAliasMapping.PATIENT_ID_ALIASES,
                "pathologyNo", FieldAliasMapping.PATHOLOGY_NO_ALIASES,
                "samplingTime", FieldAliasMapping.SAMPLING_TIME_ALIASES,
                "specimenType", FieldAliasMapping.SPECIMEN_TYPE_ALIASES,
                "pathologyDoctor", FieldAliasMapping.PATHOLOGY_DOCTOR_ALIASES,
                "pathologyDept", FieldAliasMapping.PATHOLOGY_DEPT_ALIASES,
                "pathologyDiagnosis", FieldAliasMapping.PATHOLOGY_DIAGNOSIS_ALIASES
        );
    }

    private static Map<String, List<String>> getEnteroscopyFieldToAliases() {
        return Map.of(
                "patientId", FieldAliasMapping.PATIENT_ID_ALIASES,
                "examinationNo", FieldAliasMapping.EXAMINATION_NO_ALIASES,
                "examinationTime", FieldAliasMapping.EXAMINATION_TIME_ALIASES,
                "enteroscopyType", FieldAliasMapping.ENTEROSCOPY_TYPE_ALIASES,
                "examineDoctor", FieldAliasMapping.EXAMINE_DOCTOR_ALIASES,
                "examineDept", FieldAliasMapping.EXAMINE_DEPT_ALIASES,
                "reportConclusion", FieldAliasMapping.REPORT_CONCLUSION_ALIASES
        );
    }
}