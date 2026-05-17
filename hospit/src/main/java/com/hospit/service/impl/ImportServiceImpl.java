package com.hospit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.hospit.entity.*;
import com.hospit.listener.*;
import com.hospit.mapper.*;
import com.hospit.service.*;
import com.hospit.vo.FailedRowDetail;
import com.hospit.vo.ImportContext;
import com.hospit.vo.ImportResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportServiceImpl implements IImportService {

    @Autowired
    private ICtExaminationService ctExaminationService;
    @Autowired
    private CtExaminationMapper ctExaminationMapper;
    @Autowired
    private IMriExaminationService mriExaminationService;
    @Autowired
    private MriExaminationMapper mriExaminationMapper;
    @Autowired
    private IEnteroscopyExaminationService enteroscopyExaminationService;
    @Autowired
    private EnteroscopyExaminationMapper enteroscopyExaminationMapper;
    @Autowired
    private IPathologyExaminationService pathologyExaminationService;
    @Autowired
    private PathologyExaminationMapper pathologyExaminationMapper;
    @Autowired
    private IPatientService patientService;

    // 导入CT检查数据
    @Override
    public ImportResultVO importCtExamination(MultipartFile file) {
        return importWithMode(file, "ct", "strict");
    }

    // 导入MRI检查数据
    @Override
    public ImportResultVO importMrtExamination(MultipartFile file) {
        return importWithMode(file, "mri", "strict");
    }

    // 导入病理检查数据
    @Override
    public ImportResultVO importPathologyExamination(MultipartFile file) {
        return importWithMode(file, "pathology", "strict");
    }

    // 导入肠镜检查数据
    @Override
    public ImportResultVO importEnteroscopyExamination(MultipartFile file) {
        return importWithMode(file, "enteroscopy", "strict");
    }

    // 根据模式导入数据
    @Override
    public ImportResultVO importWithMode(MultipartFile file, String type, String mode) {
        long startTime = System.currentTimeMillis();
        ImportContext context = ImportContext.create(mode, type);

        try {
            switch (type) {
                case "ct" -> readCtData(file, context);
                case "mri" -> readMriData(file, context);
                case "pathology" -> readPathologyData(file, context);
                case "enteroscopy" -> readEnteroscopyData(file, context);
            }

            if (context.getFailedRows().isEmpty()) {
                if ("strict".equals(mode)) {
                    return saveWithStrict(context, startTime);
                } else {
                    return saveWithLenient(context, startTime);
                }
            } else {
                if ("strict".equals(mode)) {
                    return buildStrictFailureResult(context, startTime);
                } else {
                    return saveWithLenient(context, startTime);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            context.addFailedRow(-1, null, List.of("导入异常: " + e.getMessage()));
            return buildFailureResult(context, startTime);
        }
    }

    // 读取CT数据
    private void readCtData(MultipartFile file, ImportContext context) throws Exception {
        GenericExcelImportListener listener = new GenericExcelImportListener(context, "ct");
        EasyExcel.read(file.getInputStream())
                .registerReadListener(listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
    }

    // 读取MRI数据
    private void readMriData(MultipartFile file, ImportContext context) throws Exception {
        GenericExcelImportListener listener = new GenericExcelImportListener(context, "mri");
        EasyExcel.read(file.getInputStream())
                .registerReadListener(listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
    }

    // 读取病理数据
    private void readPathologyData(MultipartFile file, ImportContext context) throws Exception {
        GenericExcelImportListener listener = new GenericExcelImportListener(context, "pathology");
        EasyExcel.read(file.getInputStream())
                .registerReadListener(listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
    }

    // 读取肠镜数据
    private void readEnteroscopyData(MultipartFile file, ImportContext context) throws Exception {
        GenericExcelImportListener listener = new GenericExcelImportListener(context, "enteroscopy");
        EasyExcel.read(file.getInputStream())
                .registerReadListener(listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
    }

    // 严格模式保存
    private ImportResultVO saveWithStrict(ImportContext context, long startTime) {
        List<String> errors = buildErrorMessages(context, 5);
        return ImportResultVO.builder()
                .total(context.getTotalRows())
                .success(context.getTotalRows())
                .fail(0)
                .errors(errors)
                .failedRows(context.getFailedRows())
                .errorSummary(context.getErrorSummary())
                .importMode("strict")
                .importToken(context.getImportToken())
                .costTime(System.currentTimeMillis() - startTime)
                .build();
    }

    // 构建严格模式失败结果
    private ImportResultVO buildStrictFailureResult(ImportContext context, long startTime) {
        List<String> displayErrors = buildErrorMessages(context, 5);
        List<String> allErrors = buildErrorMessages(context, Integer.MAX_VALUE);

        ImportResultVO result = ImportResultVO.builder()
                .total(context.getTotalRows())
                .success(0)
                .fail(context.getFailedRows().size())
                .errors(displayErrors)
                .failedRows(allErrors.size() > 5 ? null : context.getFailedRows())
                .errorSummary(context.getErrorSummary())
                .importMode("strict")
                .importToken(context.getImportToken())
                .costTime(System.currentTimeMillis() - startTime)
                .build();

        if (allErrors.size() > 5) {
            List<String> remaining = new ArrayList<>(displayErrors);
            remaining.add("...等" + allErrors.size() + "条错误，请到「导入错误列表」查看全部");
            result.setErrors(remaining);
            result.setFailedRows(context.getFailedRows());
        }

        return result;
    }

    // 宽松模式保存
    private ImportResultVO saveWithLenient(ImportContext context, long startTime) {
        List<?> dataList = getDataList(context);
        String type = context.getImportType();

        Set<String> allPatientIds = dataList.stream()
                .map(obj -> getPatientId(obj))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> validPatientIds = new HashSet<>();
        if (!allPatientIds.isEmpty()) {
            List<Patient> patients = patientService.listByIds(allPatientIds);
            validPatientIds = patients.stream().map(Patient::getPatientId).collect(Collectors.toSet());
        }

        List<Object> validBatch = new ArrayList<>();
        int successCount = 0;
        final int BATCH_SIZE = 500;

        List<FailedRowDetail> originalFailedRows = new ArrayList<>(context.getFailedRows());
        context.getFailedRows().clear();

        for (int i = 0; i < dataList.size(); i++) {
            Object obj = dataList.get(i);
            String patientId = getPatientId(obj);
            int rowIndex = i + 2;

            if (patientId != null && !validPatientIds.contains(patientId)) {
                context.addFailedRow(rowIndex, getRawData(obj), List.of("患者ID[" + patientId + "]不存在于系统中"));
            }
        }

        for (Object obj : dataList) {
            String patientId = getPatientId(obj);
            if (patientId == null || !validPatientIds.contains(patientId)) {
                continue;
            }

            if (checkDbDuplicate(context.getImportType(), obj)) {
                String no = getExaminationNo(obj);
                context.addFailedRow(findRowIndex(dataList, obj) + 2, getRawData(obj),
                        List.of("检查编号[" + no + "]已存在于系统中"));
                continue;
            }

            saveOneEntity(context.getImportType(), obj);
            validBatch.add(obj);
            context.incrementSuccess();
            successCount++;

            if (validBatch.size() >= BATCH_SIZE) {
                batchSave(context.getImportType(), validBatch);
                validBatch.clear();
            }
        }

        if (!validBatch.isEmpty()) {
            batchSave(context.getImportType(), validBatch);
        }

        List<String> displayErrors = buildErrorMessages(context, 5);

        return ImportResultVO.builder()
                .total(context.getTotalRows())
                .success(successCount)
                .fail(context.getFailedRows().size())
                .errors(displayErrors)
                .failedRows(context.getFailedRows())
                .errorSummary(context.getErrorSummary())
                .importMode("lenient")
                .importToken(context.getImportToken())
                .costTime(System.currentTimeMillis() - startTime)
                .build();
    }

    // 检查数据库重复
    private boolean checkDbDuplicate(String type, Object obj) {
        String no = getExaminationNo(obj);
        if (no == null || no.isEmpty()) return false;
        switch (type) {
            case "ct" -> {
                List<String> exist = ctExaminationMapper.selectExistExaminationNos(List.of(no));
                return exist != null && !exist.isEmpty();
            }
            case "mri" -> {
                List<String> exist = mriExaminationMapper.selectExistExaminationNos(List.of(no));
                return exist != null && !exist.isEmpty();
            }
            case "pathology" -> {
                List<String> exist = pathologyExaminationMapper.selectExistPathologyNos(List.of(no));
                return exist != null && !exist.isEmpty();
            }
            case "enteroscopy" -> {
                List<String> exist = enteroscopyExaminationMapper.selectExistExaminationNos(List.of(no));
                return exist != null && !exist.isEmpty();
            }
        }
        return false;
    }

    // 批量保存
    private void batchSave(String type, List<Object> batch) {
        switch (type) {
            case "ct" -> {
                List<CtExamination> list = batch.stream().map(o -> (CtExamination) o).collect(Collectors.toList());
                ctExaminationService.saveBatch(list, 500);
            }
            case "mri" -> {
                List<MriExamination> list = batch.stream().map(o -> (MriExamination) o).collect(Collectors.toList());
                mriExaminationService.saveBatch(list, 500);
            }
            case "pathology" -> {
                List<PathologyExamination> list = batch.stream().map(o -> (PathologyExamination) o).collect(Collectors.toList());
                pathologyExaminationService.saveBatch(list, 500);
            }
            case "enteroscopy" -> {
                List<EnteroscopyExamination> list = batch.stream().map(o -> (EnteroscopyExamination) o).collect(Collectors.toList());
                enteroscopyExaminationService.saveBatch(list, 500);
            }
        }
    }

    // 保存单个实体
    private void saveOneEntity(String type, Object obj) {
        switch (type) {
            case "ct" -> ctExaminationService.save((CtExamination) obj);
            case "mri" -> mriExaminationService.save((MriExamination) obj);
            case "pathology" -> pathologyExaminationService.save((PathologyExamination) obj);
            case "enteroscopy" -> enteroscopyExaminationService.save((EnteroscopyExamination) obj);
        }
    }

    // 获取数据列表
    private List<?> getDataList(ImportContext context) {
        return context.getDataList();
    }

    // 获取患者ID
    private String getPatientId(Object obj) {
        if (obj instanceof CtExamination) return ((CtExamination) obj).getPatientId();
        if (obj instanceof MriExamination) return ((MriExamination) obj).getPatientId();
        if (obj instanceof PathologyExamination) return ((PathologyExamination) obj).getPatientId();
        if (obj instanceof EnteroscopyExamination) return ((EnteroscopyExamination) obj).getPatientId();
        return null;
    }

    // 获取检查编号
    private String getExaminationNo(Object obj) {
        if (obj instanceof CtExamination) return ((CtExamination) obj).getExaminationNo();
        if (obj instanceof MriExamination) return ((MriExamination) obj).getExaminationNo();
        if (obj instanceof PathologyExamination) return ((PathologyExamination) obj).getPathologyNo();
        if (obj instanceof EnteroscopyExamination) return ((EnteroscopyExamination) obj).getExaminationNo();
        return null;
    }

    // 获取原始数据
    private String getRawData(Object obj) {
        return obj.toString();
    }

    // 查找行索引
    private int findRowIndex(List<?> list, Object obj) {
        return list.indexOf(obj);
    }

    // 构建错误消息
    private List<String> buildErrorMessages(ImportContext context, int limit) {
        List<String> errors = new ArrayList<>();
        for (FailedRowDetail row : context.getFailedRows()) {
            if (errors.size() >= limit) break;
            for (String err : row.getErrors()) {
                if (errors.size() >= limit) break;
                errors.add("第" + row.getRowIndex() + "行: " + err);
            }
        }
        return errors;
    }

    // 构建失败结果
    private ImportResultVO buildFailureResult(ImportContext context, long startTime) {
        List<String> errors = buildErrorMessages(context, 5);
        return ImportResultVO.builder()
                .total(context.getTotalRows())
                .success(context.getSuccessCount())
                .fail(context.getFailedRows().size())
                .errors(errors)
                .failedRows(context.getFailedRows())
                .errorSummary(context.getErrorSummary())
                .importToken(context.getImportToken())
                .costTime(System.currentTimeMillis() - startTime)
                .build();
    }
}
