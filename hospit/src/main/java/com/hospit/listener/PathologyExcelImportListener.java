package com.hospit.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.hospit.entity.PathologyExamination;
import com.hospit.entity.PathologyImportTemplate;
import com.hospit.util.DateFormatResolver;
import com.hospit.vo.ImportContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathologyExcelImportListener extends AnalysisEventListener<PathologyImportTemplate> {

    private final ImportContext context;
    private final Map<String, Integer> intraFileDuplicate = new HashMap<>();

    private int currentRowIndex = 0;
    private boolean isFirstRow = true;

    public PathologyExcelImportListener(ImportContext context) {
        this.context = context;
    }

    @Override
    public void invoke(PathologyImportTemplate template, AnalysisContext analysisContext) {
        if (isFirstRow) {
            isFirstRow = false;
            return;
        }

        currentRowIndex = analysisContext.readRowHolder().getRowIndex() + 1;
        context.setTotalRows(context.getTotalRows() + 1);

        List<String> errors = new ArrayList<>();
        String rawData = buildRawData(template);

        String validationError = validateRow(template);
        if (validationError != null) {
            errors.add(validationError);
        }

        if (template.getPathologyNo() != null && !template.getPathologyNo().isEmpty()) {
            Integer firstRow = intraFileDuplicate.get(template.getPathologyNo());
            if (firstRow != null) {
                errors.add("病理号已在Excel第" + firstRow + "行出现");
            } else {
                intraFileDuplicate.put(template.getPathologyNo(), currentRowIndex);
            }
        }

        if (!errors.isEmpty()) {
            context.addFailedRow(currentRowIndex, rawData, errors);
            return;
        }

        try {
            PathologyExamination entity = convertToEntity(template);
            if (entity.getSamplingTime() == null && template.getSamplingTime() != null && !template.getSamplingTime().isEmpty()) {
                context.addFailedRow(currentRowIndex, rawData, List.of("日期格式错误，无法解析"));
                return;
            }
            context.getDataList().add(entity);
        } catch (Exception e) {
            context.addFailedRow(currentRowIndex, rawData, List.of("数据转换异常: " + e.getMessage()));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    private String validateRow(PathologyImportTemplate template) {
        if (template.getPatientId() == null || template.getPatientId().trim().isEmpty()) {
            return "患者ID不能为空";
        }
        if (template.getPatientId() != null && template.getPatientId().trim().length() > 50) {
            return "患者ID长度不能超过50个字符";
        }
        if (template.getPathologyNo() != null && template.getPathologyNo().length() > 50) {
            return "病理号长度不能超过50个字符";
        }
        if (template.getPathologyDiagnosis() != null && template.getPathologyDiagnosis().length() > 500) {
            return "病理诊断长度不能超过500个字符";
        }
        return null;
    }

    private String buildRawData(PathologyImportTemplate template) {
        StringBuilder sb = new StringBuilder();
        if (template.getPatientId() != null) sb.append("患者ID:").append(template.getPatientId()).append(",");
        if (template.getPathologyNo() != null) sb.append("病理号:").append(template.getPathologyNo()).append(",");
        if (template.getSpecimenType() != null) sb.append("标本类型:").append(template.getSpecimenType()).append(",");
        if (template.getPathologyDoctor() != null) sb.append("病理医生:").append(template.getPathologyDoctor()).append(",");
        if (template.getPathologyDept() != null) sb.append("病理科室:").append(template.getPathologyDept()).append(",");
        if (template.getSamplingTime() != null) sb.append("采样时间:").append(template.getSamplingTime()).append(",");
        if (template.getPathologyDiagnosis() != null) sb.append("病理诊断:").append(template.getPathologyDiagnosis());
        return sb.toString();
    }

    private PathologyExamination convertToEntity(PathologyImportTemplate template) {
        PathologyExamination entity = new PathologyExamination();
        entity.setPatientId(template.getPatientId().trim());
        entity.setPathologyNo(template.getPathologyNo());
        entity.setSpecimenType(template.getSpecimenType());
        entity.setPathologyDoctor(template.getPathologyDoctor());
        entity.setPathologyDept(template.getPathologyDept());
        entity.setPathologyDiagnosis(template.getPathologyDiagnosis());

        entity.setSamplingTime(DateFormatResolver.parse(template.getSamplingTime()));

        entity.setUserId(1);
        entity.setUploadTime(LocalDateTime.now());
        entity.setIsInvalid(false);
        entity.setCreateTime(LocalDateTime.now());

        return entity;
    }

    public List<Object> getDataList() {
        return context.getDataList();
    }
}
