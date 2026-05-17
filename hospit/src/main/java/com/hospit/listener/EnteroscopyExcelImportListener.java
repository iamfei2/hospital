package com.hospit.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.hospit.entity.EnteroscopyExamination;
import com.hospit.entity.EnteroscopyImportTemplate;
import com.hospit.util.DateFormatResolver;
import com.hospit.vo.ImportContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnteroscopyExcelImportListener extends AnalysisEventListener<EnteroscopyImportTemplate> {

    private final ImportContext context;
    private final Map<String, Integer> intraFileDuplicate = new HashMap<>();

    private int currentRowIndex = 0;
    private boolean isFirstRow = true;

    public EnteroscopyExcelImportListener(ImportContext context) {
        this.context = context;
    }

    @Override
    public void invoke(EnteroscopyImportTemplate template, AnalysisContext analysisContext) {
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

        if (template.getExaminationNo() != null && !template.getExaminationNo().isEmpty()) {
            Integer firstRow = intraFileDuplicate.get(template.getExaminationNo());
            if (firstRow != null) {
                errors.add("检查编号已在Excel第" + firstRow + "行出现");
            } else {
                intraFileDuplicate.put(template.getExaminationNo(), currentRowIndex);
            }
        }

        if (!errors.isEmpty()) {
            context.addFailedRow(currentRowIndex, rawData, errors);
            return;
        }

        try {
            EnteroscopyExamination entity = convertToEntity(template);
            if (entity.getExaminationTime() == null && template.getExaminationTime() != null && !template.getExaminationTime().isEmpty()) {
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

    private String validateRow(EnteroscopyImportTemplate template) {
        if (template.getPatientId() == null || template.getPatientId().trim().isEmpty()) {
            return "患者ID不能为空";
        }
        if (template.getPatientId() != null && template.getPatientId().trim().length() > 50) {
            return "患者ID长度不能超过50个字符";
        }
        if (template.getExaminationNo() != null && template.getExaminationNo().length() > 50) {
            return "检查编号长度不能超过50个字符";
        }
        if (template.getReportConclusion() != null && template.getReportConclusion().length() > 500) {
            return "报告结论长度不能超过500个字符";
        }
        return null;
    }

    private String buildRawData(EnteroscopyImportTemplate template) {
        StringBuilder sb = new StringBuilder();
        if (template.getPatientId() != null) sb.append("患者ID:").append(template.getPatientId()).append(",");
        if (template.getExaminationNo() != null) sb.append("检查编号:").append(template.getExaminationNo()).append(",");
        if (template.getEnteroscopyType() != null) sb.append("肠镜类型:").append(template.getEnteroscopyType()).append(",");
        if (template.getExamineDoctor() != null) sb.append("检查医生:").append(template.getExamineDoctor()).append(",");
        if (template.getExamineDept() != null) sb.append("检查科室:").append(template.getExamineDept()).append(",");
        if (template.getExaminationTime() != null) sb.append("检查时间:").append(template.getExaminationTime()).append(",");
        if (template.getReportConclusion() != null) sb.append("报告结论:").append(template.getReportConclusion());
        return sb.toString();
    }

    private EnteroscopyExamination convertToEntity(EnteroscopyImportTemplate template) {
        EnteroscopyExamination entity = new EnteroscopyExamination();
        entity.setPatientId(template.getPatientId().trim());
        entity.setExaminationNo(template.getExaminationNo());
        entity.setEnteroscopyType(template.getEnteroscopyType());
        entity.setExamineDoctor(template.getExamineDoctor());
        entity.setExamineDept(template.getExamineDept());
        entity.setReportConclusion(template.getReportConclusion());

        entity.setExaminationTime(DateFormatResolver.parse(template.getExaminationTime()));

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
