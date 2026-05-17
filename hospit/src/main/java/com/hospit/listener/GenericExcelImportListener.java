package com.hospit.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.hospit.util.DateFormatResolver;
import com.hospit.util.HeaderMappingResolver;
import com.hospit.validation.ValidationEngine;
import com.hospit.validation.ValidationResult;
import com.hospit.vo.HeaderMapping;
import com.hospit.vo.ImportContext;

import java.time.LocalDateTime;
import java.util.*;

public class GenericExcelImportListener extends AnalysisEventListener<Map<Integer, String>> {

    private final ImportContext context;
    private final String type;
    private final ValidationEngine validationEngine;
    private final Map<String, Integer> intraFileDuplicate = new HashMap<>();
    private int currentRowIndex = 0;
    private boolean isFirstRow = true;
    private HeaderMapping mapping;

    public GenericExcelImportListener(ImportContext context, String type) {
        this.context = context;
        this.type = type;
        this.validationEngine = createValidationEngine(type);
    }

    private ValidationEngine createValidationEngine(String type) {
        switch (type) {
            case "ct":
                return ValidationEngine.forCtExamination();
            case "mri":
                return ValidationEngine.forMriExamination();
            case "pathology":
                return ValidationEngine.forPathologyExamination();
            case "enteroscopy":
                return ValidationEngine.forEnteroscopyExamination();
            default:
                return new ValidationEngine();
        }
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        List<String> headers = new ArrayList<>();
        int columnCount = headMap.size();
        for (int i = 0; i < columnCount; i++) {
            ReadCellData<?> cellData = headMap.get(i);
            String value = cellData != null ? cellData.getStringValue() : "";
            headers.add(value);
        }
        mapping = resolveMapping(headers);
    }

    private HeaderMapping resolveMapping(List<String> headers) {
        switch (type) {
            case "ct":
                return HeaderMappingResolver.resolveCtHeaders(headers);
            case "mri":
                return HeaderMappingResolver.resolveMriHeaders(headers);
            case "pathology":
                return HeaderMappingResolver.resolvePathologyHeaders(headers);
            case "enteroscopy":
                return HeaderMappingResolver.resolveEnteroscopyHeaders(headers);
            default:
                return new HeaderMapping();
        }
    }

    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext analysisContext) {
        if (isFirstRow) {
            isFirstRow = false;
            return;
        }

        currentRowIndex = analysisContext.readRowHolder().getRowIndex() + 1;
        context.setTotalRows(context.getTotalRows() + 1);

        List<String> errors = new ArrayList<>();
        String rawData = buildRawData(rowData);

        if (mapping == null || mapping.size() == 0) {
            errors.add("无法解析表头，请确保Excel表头包含标准字段名或别名");
            context.addFailedRow(currentRowIndex, rawData, errors);
            return;
        }

        String validationError = validateRow(rowData);
        if (validationError != null) {
            errors.add(validationError);
        }

        String noField = "examinationNo";
        if ("pathology".equals(type)) {
            noField = "pathologyNo";
        }
        Integer noColumn = mapping.getColumn(noField);
        if (noColumn != null) {
            String no = rowData.get(noColumn);
            if (no != null && !no.isEmpty()) {
                Integer firstRow = intraFileDuplicate.get(no);
                if (firstRow != null) {
                    errors.add(noField + "已在Excel第" + firstRow + "行出现");
                } else {
                    intraFileDuplicate.put(no, currentRowIndex);
                }
            }
        }

        if (!errors.isEmpty()) {
            context.addFailedRow(currentRowIndex, rawData, errors);
            return;
        }

        try {
            Object entity = convertToEntity(rowData);
            context.getDataList().add(entity);
        } catch (Exception e) {
            context.addFailedRow(currentRowIndex, rawData, List.of("数据转换异常: " + e.getMessage()));
        }
    }

    private String buildRawData(Map<Integer, String> rowData) {
        StringBuilder sb = new StringBuilder();
        if (mapping != null) {
            for (Map.Entry<Integer, String> entry : mapping.getColumnToField().entrySet()) {
                String value = rowData.get(entry.getKey());
                if (value != null) {
                    sb.append(entry.getValue()).append(":").append(value).append(",");
                }
            }
        }
        return sb.toString();
    }

    private String validateRow(Map<Integer, String> rowData) {
        if (mapping == null || mapping.size() == 0) {
            return "无法解析表头，请确保Excel表头包含标准字段名或别名";
        }
        Integer patientIdColumn = mapping.getColumn("patientId");
        if (patientIdColumn == null) {
            return "无法找到患者ID列，请检查表头";
        }

        Map<String, Object> dataMap = rowDataToMap(rowData);
        ValidationResult result = validationEngine.validate(dataMap);
        
        if (!result.isValid() && result.getErrors() != null && !result.getErrors().isEmpty()) {
            return String.join("; ", result.getErrors());
        }
        
        return null;
    }
    
    private Map<String, Object> rowDataToMap(Map<Integer, String> rowData) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
            String fieldName = mapping.getField(entry.getKey());
            if (fieldName != null) {
                map.put(fieldName, entry.getValue());
            }
        }
        return map;
    }

    private Object convertToEntity(Map<Integer, String> rowData) {
        switch (type) {
            case "ct":
                return createCtExamination(rowData);
            case "mri":
                return createMriExamination(rowData);
            case "pathology":
                return createPathologyExamination(rowData);
            case "enteroscopy":
                return createEnteroscopyExamination(rowData);
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private com.hospit.entity.CtExamination createCtExamination(Map<Integer, String> rowData) {
        com.hospit.entity.CtExamination entity = new com.hospit.entity.CtExamination();
        setCommonFields(entity, rowData);
        entity.setExaminationTime(parseTime(rowData, "examinationTime"));
        setCommonEntityFields(entity);
        return entity;
    }

    private com.hospit.entity.MriExamination createMriExamination(Map<Integer, String> rowData) {
        com.hospit.entity.MriExamination entity = new com.hospit.entity.MriExamination();
        setCommonFields(entity, rowData);
        entity.setExaminationTime(parseTime(rowData, "examinationTime"));
        setCommonEntityFields(entity);
        return entity;
    }

    private com.hospit.entity.PathologyExamination createPathologyExamination(Map<Integer, String> rowData) {
        com.hospit.entity.PathologyExamination entity = new com.hospit.entity.PathologyExamination();
        entity.setPatientId(getValue(rowData, "patientId").trim());
        entity.setPathologyNo(getValue(rowData, "pathologyNo"));
        entity.setSpecimenType(getValue(rowData, "specimenType"));
        entity.setPathologyDoctor(getValue(rowData, "pathologyDoctor"));
        entity.setPathologyDept(getValue(rowData, "pathologyDept"));
        entity.setPathologyDiagnosis(getValue(rowData, "pathologyDiagnosis"));
        entity.setSamplingTime(parseTime(rowData, "samplingTime"));
        setCommonEntityFields(entity);
        return entity;
    }

    private com.hospit.entity.EnteroscopyExamination createEnteroscopyExamination(Map<Integer, String> rowData) {
        com.hospit.entity.EnteroscopyExamination entity = new com.hospit.entity.EnteroscopyExamination();
        entity.setPatientId(getValue(rowData, "patientId").trim());
        entity.setExaminationNo(getValue(rowData, "examinationNo"));
        entity.setEnteroscopyType(getValue(rowData, "enteroscopyType"));
        entity.setExamineDoctor(getValue(rowData, "examineDoctor"));
        entity.setExamineDept(getValue(rowData, "examineDept"));
        entity.setReportConclusion(getValue(rowData, "reportConclusion"));
        entity.setExaminationTime(parseTime(rowData, "examinationTime"));
        setCommonEntityFields(entity);
        return entity;
    }

    private void setCommonFields(Object entity, Map<Integer, String> rowData) {
        if (entity instanceof com.hospit.entity.CtExamination) {
            com.hospit.entity.CtExamination e = (com.hospit.entity.CtExamination) entity;
            e.setPatientId(getValue(rowData, "patientId").trim());
            e.setExaminationNo(getValue(rowData, "examinationNo"));
            e.setExaminationPart(getValue(rowData, "examinationPart"));
            e.setExamineDoctor(getValue(rowData, "examineDoctor"));
            e.setExamineDept(getValue(rowData, "examineDept"));
            e.setReportConclusion(getValue(rowData, "reportConclusion"));
        } else if (entity instanceof com.hospit.entity.MriExamination) {
            com.hospit.entity.MriExamination e = (com.hospit.entity.MriExamination) entity;
            e.setPatientId(getValue(rowData, "patientId").trim());
            e.setExaminationNo(getValue(rowData, "examinationNo"));
            e.setExaminationPart(getValue(rowData, "examinationPart"));
            e.setExamineDoctor(getValue(rowData, "examineDoctor"));
            e.setExamineDept(getValue(rowData, "examineDept"));
            e.setReportConclusion(getValue(rowData, "reportConclusion"));
        }
    }

    private void setCommonEntityFields(Object entity) {
        if (entity instanceof com.hospit.entity.CtExamination) {
            ((com.hospit.entity.CtExamination) entity).setUserId(1);
            ((com.hospit.entity.CtExamination) entity).setUploadTime(LocalDateTime.now());
            ((com.hospit.entity.CtExamination) entity).setIsInvalid(false);
            ((com.hospit.entity.CtExamination) entity).setCreateTime(LocalDateTime.now());
        } else if (entity instanceof com.hospit.entity.MriExamination) {
            ((com.hospit.entity.MriExamination) entity).setUserId(1);
            ((com.hospit.entity.MriExamination) entity).setUploadTime(LocalDateTime.now());
            ((com.hospit.entity.MriExamination) entity).setIsInvalid(false);
            ((com.hospit.entity.MriExamination) entity).setCreateTime(LocalDateTime.now());
        } else if (entity instanceof com.hospit.entity.PathologyExamination) {
            ((com.hospit.entity.PathologyExamination) entity).setUserId(1);
            ((com.hospit.entity.PathologyExamination) entity).setUploadTime(LocalDateTime.now());
            ((com.hospit.entity.PathologyExamination) entity).setIsInvalid(false);
            ((com.hospit.entity.PathologyExamination) entity).setCreateTime(LocalDateTime.now());
        } else if (entity instanceof com.hospit.entity.EnteroscopyExamination) {
            ((com.hospit.entity.EnteroscopyExamination) entity).setUserId(1);
            ((com.hospit.entity.EnteroscopyExamination) entity).setUploadTime(LocalDateTime.now());
            ((com.hospit.entity.EnteroscopyExamination) entity).setIsInvalid(false);
            ((com.hospit.entity.EnteroscopyExamination) entity).setCreateTime(LocalDateTime.now());
        }
    }

    private String getValue(Map<Integer, String> rowData, String fieldName) {
        Integer column = mapping.getColumn(fieldName);
        if (column == null) {
            return null;
        }
        String value = rowData.get(column);
        return value != null ? value.trim() : null;
    }

    private LocalDateTime parseTime(Map<Integer, String> rowData, String fieldName) {
        String timeStr = getValue(rowData, fieldName);
        return DateFormatResolver.parse(timeStr);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    public HeaderMapping getMapping() {
        return mapping;
    }

    public List<Object> getDataList() {
        return this.context.getDataList();
    }
}