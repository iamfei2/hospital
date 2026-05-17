package com.hospit.service.impl;

import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.LabResult;
import com.hospit.entity.Patient;
import com.hospit.entity.LabItemDict;
import com.hospit.entity.LabItemStatistics;
import com.hospit.mapper.LabResultMapper;
import com.hospit.service.ILabResultService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hospit.service.IPatientService;
import com.hospit.service.ILabItemDictService;
import com.hospit.service.IWarningRuleService;
import com.hospit.service.IWarningRecordService;
import com.hospit.service.EasyRulesService;
import com.hospit.service.IIsolationForestService;
import com.hospit.service.IStatisticsComputeService;
import com.hospit.websocket.WebSocketSessionManager;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.vo.CursorPageResult;
import com.hospit.entity.WarningRecord;
import com.hospit.entity.WarningRule;
import com.hospit.rules.LabResultFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 患者检验结果表 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class LabResultServiceImpl extends ServiceImpl<LabResultMapper, LabResult> implements ILabResultService {
    private static final Logger log = LoggerFactory.getLogger(LabResultServiceImpl.class);

    @Autowired
    private IPatientService patientService;

    @Autowired
    private ILabItemDictService labItemDictService;

    @Autowired
    private IWarningRuleService warningRuleService;

    @Autowired
    private IWarningRecordService warningRecordService;

    @Autowired
    private EasyRulesService easyRulesService;

    @Autowired
    private IIsolationForestService isolationForestService;

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    private static final int RECENT_COUNT = 5;
    private static final long WARNING_DEDUP_MINUTES = 30;



    // 分页查询检验结果
    @Override
    public Result getLabResultPage(QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            String patientId = param != null && param.get("patientId") != null ? (String) param.get("patientId") : null;
            String patientName = param != null && param.get("patientName") != null ? (String) param.get("patientName") : null;
            String testName = param != null && param.get("testName") != null ? (String) param.get("testName") : null;
            String resultValue = param != null && param.get("resultValue") != null ? (String) param.get("resultValue") : null;
            String executeDoc = param != null && param.get("executeDoc") != null ? (String) param.get("executeDoc") : null;
            String executeDept = param != null && param.get("executeDept") != null ? (String) param.get("executeDept") : null;
            String startTime = param != null && param.get("startTime") != null ? (String) param.get("startTime") : null;
            String endTime = param != null && param.get("endTime") != null ? (String) param.get("endTime") : null;

            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
            IPage<Map<String, Object>> resultPage = baseMapper.selectGroupedPage(page, patientId, patientName, testName, resultValue, executeDoc, executeDept, startTime, endTime);

            List<Map<String, Object>> records = resultPage.getRecords();
            if (records == null || records.isEmpty()) {
                return Result.success(new ArrayList<>(), resultPage.getTotal());
            }

            Set<String> patientIds = records.stream()
                    .map(r -> r.get("patientId") != null ? r.get("patientId").toString() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            final Map<String, Patient> patientMap;
            if (patientIds.isEmpty()) {
                patientMap = new HashMap<>();
            } else {
                List<Patient> patients = patientService.listByIds(patientIds);
                patientMap = patients.stream().collect(Collectors.toMap(Patient::getPatientId, p -> p, (a, b) -> a));
            }

            List<LabResultGroupVO> voList = records.stream().map(map -> {
                LabResultGroupVO vo = new LabResultGroupVO();
                Object reportTimeObj = map.get("reportTime");
                if (reportTimeObj instanceof LocalDateTime) {
                    vo.setReportTime((LocalDateTime) reportTimeObj);
                } else if (reportTimeObj instanceof java.sql.Timestamp) {
                    vo.setReportTime(((java.sql.Timestamp) reportTimeObj).toLocalDateTime());
                }
                vo.setPatientId(map.get("patientId") != null ? map.get("patientId").toString() : null);
                vo.setTestDept(map.get("executeDept") != null ? map.get("executeDept").toString() : null);
                vo.setTestDoctor(map.get("executeDoc") != null ? map.get("executeDoc").toString() : null);
                vo.setReportUrl(map.get("reportUrl") != null ? map.get("reportUrl").toString() : null);
                vo.setItemCount(map.get("itemCount") != null ? ((Number) map.get("itemCount")).intValue() : 0);
                vo.setAbnormalCount(map.get("abnormalCount") != null ? ((Number) map.get("abnormalCount")).intValue() : 0);

                final String pid = vo.getPatientId();
                if (pid != null && patientMap.containsKey(pid)) {
                    vo.setPatientName(patientMap.get(pid).getPatientName());
                } else {
                    vo.setPatientName("未知");
                }
                return vo;
            }).collect(Collectors.toList());

            return Result.success(voList, resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 游标分页查询检验结果
    @Override
    public Result getLabResultPageWithCursor(QueryPageParam queryPageParam, String cursor) {
        try {
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            String patientId = param != null && param.get("patientId") != null ? (String) param.get("patientId") : null;
            String patientName = param != null && param.get("patientName") != null ? (String) param.get("patientName") : null;
            String testName = param != null && param.get("testName") != null ? (String) param.get("testName") : null;
            String resultValue = param != null && param.get("resultValue") != null ? (String) param.get("resultValue") : null;
            String executeDoc = param != null && param.get("executeDoc") != null ? (String) param.get("executeDoc") : null;
            String executeDept = param != null && param.get("executeDept") != null ? (String) param.get("executeDept") : null;
            String startTime = param != null && param.get("startTime") != null ? (String) param.get("startTime") : null;
            String endTime = param != null && param.get("endTime") != null ? (String) param.get("endTime") : null;

            List<Map<String, Object>> records = baseMapper.selectGroupedPageWithCursor(
                    patientId, patientName, testName, resultValue, executeDoc, executeDept, startTime, endTime, cursor, pageSize);

            if (records == null || records.isEmpty()) {
                return Result.success(new CursorPageResult<>(new ArrayList<>(), null, false, pageSize));
            }

            Set<String> patientIds = records.stream()
                    .map(r -> r.get("patientId") != null ? r.get("patientId").toString() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            final Map<String, Patient> patientMap;
            if (patientIds.isEmpty()) {
                patientMap = new HashMap<>();
            } else {
                List<Patient> patients = patientService.listByIds(patientIds);
                patientMap = patients.stream().collect(Collectors.toMap(Patient::getPatientId, p -> p, (a, b) -> a));
            }

            List<LabResultGroupVO> voList = records.stream().map(map -> {
                LabResultGroupVO vo = new LabResultGroupVO();
                Object reportTimeObj = map.get("reportTime");
                if (reportTimeObj instanceof LocalDateTime) {
                    vo.setReportTime((LocalDateTime) reportTimeObj);
                } else if (reportTimeObj instanceof java.sql.Timestamp) {
                    vo.setReportTime(((java.sql.Timestamp) reportTimeObj).toLocalDateTime());
                }
                vo.setPatientId(map.get("patientId") != null ? map.get("patientId").toString() : null);
                vo.setTestDept(map.get("executeDept") != null ? map.get("executeDept").toString() : null);
                vo.setTestDoctor(map.get("executeDoc") != null ? map.get("executeDoc").toString() : null);
                vo.setReportUrl(map.get("reportUrl") != null ? map.get("reportUrl").toString() : null);
                vo.setItemCount(map.get("itemCount") != null ? ((Number) map.get("itemCount")).intValue() : 0);
                vo.setAbnormalCount(map.get("abnormalCount") != null ? ((Number) map.get("abnormalCount")).intValue() : 0);

                final String pid = vo.getPatientId();
                if (pid != null && patientMap.containsKey(pid)) {
                    vo.setPatientName(patientMap.get(pid).getPatientName());
                } else {
                    vo.setPatientName("未知");
                }
                return vo;
            }).collect(Collectors.toList());

            Map<String, Object> lastRecord = records.get(records.size() - 1);
            Object lastReportTimeObj = lastRecord.get("reportTime");
            String nextCursor = null;
            if (lastReportTimeObj != null) {
                if (lastReportTimeObj instanceof LocalDateTime) {
                    nextCursor = ((LocalDateTime) lastReportTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else if (lastReportTimeObj instanceof java.sql.Timestamp) {
                    nextCursor = ((java.sql.Timestamp) lastReportTimeObj).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }

            Long remainingCount = baseMapper.countGroupedPageWithCursor(
                    patientId, patientName, testName, resultValue, executeDoc, executeDept, startTime, endTime, nextCursor);
            boolean hasMore = remainingCount != null && remainingCount > 0;

            CursorPageResult<LabResultGroupVO> pageResult = new CursorPageResult<>(voList, nextCursor, hasMore, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 获取检验结果详情
    @Override
    public Result getLabResultDetail(Long resultId) {
        try {
            LabResult labResult = baseMapper.selectById(resultId);
            if (labResult == null) {
                return Result.fail("检验结果不存在");
            }
            LabResultVO vo = convertToVO(labResult);
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询详情失败");
        }
    }

    // 根据时间查询检验结果
    @Override
    public Result getLabResultsByTime(QueryPageParam queryPageParam) {
        try {
            HashMap param = queryPageParam.getParam();
            if (param == null || param.get("patientId") == null || param.get("reportTime") == null) {
                return Result.fail("参数不完整");
            }

            String patientId = (String) param.get("patientId");
            String reportTimeStr = (String) param.get("reportTime");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime reportTime = LocalDateTime.parse(reportTimeStr, formatter);

            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId)
                    .eq("report_time", reportTime)
                    .orderByDesc("report_time");

            List<LabResult> results = baseMapper.selectList(wrapper);

            List<LabResultVO> voList = new ArrayList<>();
            for (LabResult labResult : results) {
                LabResultVO vo = convertToVO(labResult);
                voList.add(vo);
            }

            return Result.success(voList, (long) voList.size());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 创建分组VO
    private LabResultGroupVO createGroupVO(LocalDateTime reportTime, List<LabResult> timeResults) {
        LabResultGroupVO groupVO = new LabResultGroupVO();

        // 取第一条记录的基本信息
        LabResult firstResult = timeResults.get(0);
        Patient patient = patientService.getById(firstResult.getPatientId());

        groupVO.setReportTime(reportTime);
        groupVO.setPatientId(firstResult.getPatientId());
        groupVO.setPatientName(patient != null ? patient.getPatientName() : "未知");
        groupVO.setTestDept(firstResult.getExecuteDept());
        groupVO.setTestDoctor(firstResult.getExecuteDoc());
        groupVO.setItemCount(timeResults.size());

        // 计算异常项目数量
        long abnormalCount = timeResults.stream()
                .filter(result -> {
                    LabItemDict itemDict = labItemDictService.getById(result.getItemId());
                    String status = calculateResultStatus(result.getResultValue(),
                            itemDict != null ? itemDict.getNormalRange() : null);
                    return "异常".equals(status) || "危急".equals(status);
                })
                .count();

        groupVO.setAbnormalCount((int) abnormalCount);

        // 设置报告URL（从第一条记录获取）
        groupVO.setReportUrl(firstResult.getReportUrl());

        return groupVO;
    }

    // 转换为VO对象
    private LabResultVO convertToVO(LabResult labResult) {
        LabResultVO vo = new LabResultVO();
        vo.setResultId(labResult.getResultId());
        vo.setPatientId(labResult.getPatientId());
        vo.setItemId(labResult.getItemId());
        vo.setTestResult(labResult.getResultValue());

        // 查询检验项目字典信息
        LabItemDict itemDict = labItemDictService.getById(labResult.getItemId());

        // 调试日志 - 检查单位值
        System.out.println("Debug - LabResult Unit: " + labResult.getResultUnit());
        System.out.println("Debug - ItemDict DefaultUnit: " + (itemDict != null ? itemDict.getDefaultUnit() : "null"));

        // 设置单位：如果检验结果中的单位为空或为"-"，则使用字典中的默认单位
        String resultUnit = labResult.getResultUnit();
        if (resultUnit == null || resultUnit.trim().isEmpty() || "-".equals(resultUnit.trim())) {
            // 确保从字典中获取默认单位
            String defaultUnit = itemDict != null ? itemDict.getDefaultUnit() : null;
            if (defaultUnit != null && !defaultUnit.trim().isEmpty() && !"-".equals(defaultUnit.trim())) {
                vo.setUnit(defaultUnit);
            } else {
                // 如果字典中的默认单位也为空，则尝试从项目名称中提取单位
                String unitFromName = extractUnitFromItemName(itemDict != null ? itemDict.getItemName() : null);
                vo.setUnit(unitFromName != null ? unitFromName : "未知");
            }
        } else {
            vo.setUnit(resultUnit);
        }

        vo.setTestTime(labResult.getReportTime());
        vo.setTestDept(labResult.getExecuteDept());
        vo.setTestDoctor(labResult.getExecuteDoc());

        // 查询患者名称
        Patient patient = patientService.getById(labResult.getPatientId());
        vo.setPatientName(patient != null ? patient.getPatientName() : "未知");

        // 查询检验项目名称和参考范围
        vo.setTestName(itemDict != null ? itemDict.getItemName() : "未知");
        vo.setReferenceRange(itemDict != null ? itemDict.getNormalRange() : "未知");

        // 计算结果状态
        vo.setResultStatus(calculateResultStatus(labResult.getResultValue(),
                itemDict != null ? itemDict.getNormalRange() : null));

        // 设置报告URL
        vo.setReportUrl(labResult.getReportUrl());

        return vo;
    }

    // 从项目名称中提取单位
    private String extractUnitFromItemName(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return null;
        }

        // 常见的单位模式匹配
        if (itemName.contains("白细胞") || itemName.contains("红细胞") || itemName.contains("血小板")) {
            return "×10^9/L";
        } else if (itemName.contains("血红蛋白")) {
            return "g/L";
        } else if (itemName.contains("谷丙转氨酶") || itemName.contains("谷草转氨酶") ||
                itemName.contains("碱性磷酸酶") || itemName.contains("γ-谷氨酰转移酶")) {
            return "U/L";
        } else if (itemName.contains("总蛋白") || itemName.contains("白蛋白") || itemName.contains("球蛋白")) {
            return "g/L";
        } else if (itemName.contains("总胆红素") || itemName.contains("直接胆红素") || itemName.contains("间接胆红素")) {
            return "μmol/L";
        } else if (itemName.contains("肌酐") || itemName.contains("尿素") || itemName.contains("尿酸")) {
            return "μmol/L";
        } else if (itemName.contains("葡萄糖") || itemName.contains("血糖")) {
            return "mmol/L";
        } else if (itemName.contains("胆固醇") || itemName.contains("甘油三酯")) {
            return "mmol/L";
        } else if (itemName.contains("钠") || itemName.contains("钾") || itemName.contains("氯") ||
                itemName.contains("钙") || itemName.contains("磷")) {
            return "mmol/L";
        }

        return null;
    }

    // 计算检验结果状态
    private String calculateResultStatus(BigDecimal resultValue, String normalRange) {
        if (normalRange == null || normalRange.isEmpty()) {
            return "未知";
        }
        try {
            String[] parts = normalRange.split("-");
            if (parts.length == 2) {
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                double value = resultValue.doubleValue();
                if (value < min) {
                    return "异常";
                } else if (value > max) {
                    return "异常";
                } else {
                    return "正常";
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return "未知";
    }

    // 单条记录VO类
    public static class LabResultVO {
        private Long resultId;
        private String patientId;
        private String patientName;
        private Integer itemId;
        private String testName;
        private BigDecimal testResult;
        private String referenceRange;
        private String unit;
        private LocalDateTime testTime;
        private String testDept;
        private String testDoctor;
        private String resultStatus;
        private String reportUrl;

        // Getters and Setters
        public Long getResultId() { return resultId; }
        public void setResultId(Long resultId) { this.resultId = resultId; }
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        public BigDecimal getTestResult() { return testResult; }
        public void setTestResult(BigDecimal testResult) { this.testResult = testResult; }
        public String getReferenceRange() { return referenceRange; }
        public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public LocalDateTime getTestTime() { return testTime; }
        public void setTestTime(LocalDateTime testTime) { this.testTime = testTime; }
        public String getTestDept() { return testDept; }
        public void setTestDept(String testDept) { this.testDept = testDept; }
        public String getTestDoctor() { return testDoctor; }
        public void setTestDoctor(String testDoctor) { this.testDoctor = testDoctor; }
        public String getResultStatus() { return resultStatus; }
        public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
        public String getReportUrl() { return reportUrl; }
        public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    }

    // 分组VO类
    public static class LabResultGroupVO {
        private LocalDateTime reportTime;
        private String patientId;
        private String patientName;
        private String testDept;
        private String testDoctor;
        private int itemCount;
        private int abnormalCount;
        private String reportUrl;

        // Getters and Setters
        public LocalDateTime getReportTime() { return reportTime; }
        public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getTestDept() { return testDept; }
        public void setTestDept(String testDept) { this.testDept = testDept; }
        public String getTestDoctor() { return testDoctor; }
        public void setTestDoctor(String testDoctor) { this.testDoctor = testDoctor; }
        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
        public int getAbnormalCount() { return abnormalCount; }
        public void setAbnormalCount(int abnormalCount) { this.abnormalCount = abnormalCount; }
        public String getReportUrl() { return reportUrl; }
        public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    }
    // 根据患者ID获取检验结果
    @Override
    public Result getLabResultsByPatient(String patientId) {
        try {
            QueryWrapper<LabResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("patient_id", patientId)
                    .eq("is_invalid", 0)
                    .orderByDesc("report_time");

            List<LabResult> results = this.list(queryWrapper);

            // 按报告时间分组
            Map<LocalDateTime, List<LabResult>> groupedByTime = results.stream()
                    .collect(Collectors.groupingBy(LabResult::getReportTime));

            List<Map<String, Object>> groupedResults = new ArrayList<>();

            for (Map.Entry<LocalDateTime, List<LabResult>> entry : groupedByTime.entrySet()) {
                Map<String, Object> group = new HashMap<>();
                LocalDateTime reportTime = entry.getKey();
                List<LabResult> timeResults = entry.getValue();

                // 取第一条记录的基本信息
                LabResult firstResult = timeResults.get(0);

                // 设置分组信息
                group.put("reportTime", reportTime);
                group.put("patientId", firstResult.getPatientId());
                group.put("testDept", firstResult.getExecuteDept());
                group.put("testDoctor", firstResult.getExecuteDoc());
                group.put("itemCount", timeResults.size());

                // 计算异常项目数量
                long abnormalCount = timeResults.stream()
                        .filter(result -> {
                            LabItemDict itemDict = labItemDictService.getById(result.getItemId());
                            String status = calculateResultStatus(result.getResultValue(),
                                    itemDict != null ? itemDict.getNormalRange() : null);
                            return "异常".equals(status) || "危急".equals(status);
                        })
                        .count();
                group.put("abnormalCount", (int) abnormalCount);

                // 查询患者名称
                Patient patient = patientService.getById(firstResult.getPatientId());
                group.put("patientName", patient != null ? patient.getPatientName() : "未知");

                // 添加报告URL（从第一条记录获取）
                group.put("reportUrl", firstResult.getReportUrl());

                // 添加该分组下的所有检验项目详情
                List<Map<String, Object>> itemDetails = new ArrayList<>();
                for (LabResult result : timeResults) {
                    itemDetails.add(formatLabResult(result));
                }
                group.put("labItems", itemDetails);

                groupedResults.add(group);
            }

            // 按时间倒序排序
            groupedResults.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("reportTime");
                LocalDateTime timeB = (LocalDateTime) b.get("reportTime");
                return timeB.compareTo(timeA);
            });

            return Result.success(groupedResults);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取检验结果失败");
        }
    }

    // 格式化检验结果
    private Map<String, Object> formatLabResult(LabResult labResult) {
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("resultId", labResult.getResultId());
        resultMap.put("patientId", labResult.getPatientId());
        resultMap.put("testResult", labResult.getResultValue());
        resultMap.put("testTime", labResult.getReportTime());
        resultMap.put("testDept", labResult.getExecuteDept());
        resultMap.put("testDoctor", labResult.getExecuteDoc());

        // 查询检验项目字典信息
        LabItemDict itemDict = labItemDictService.getById(labResult.getItemId());

        // 设置单位：如果检验结果中的单位为空或为"-"，则使用字典中的默认单位
        String resultUnit = labResult.getResultUnit();
        if (resultUnit == null || resultUnit.trim().isEmpty() || "-".equals(resultUnit.trim())) {
            String defaultUnit = itemDict != null ? itemDict.getDefaultUnit() : null;
            if (defaultUnit != null && !defaultUnit.trim().isEmpty() && !"-".equals(defaultUnit.trim())) {
                resultMap.put("unit", defaultUnit);
            } else {
                // 如果字典中的默认单位也为空，则尝试从项目名称中提取单位
                String unitFromName = extractUnitFromItemName(itemDict != null ? itemDict.getItemName() : null);
                resultMap.put("unit", unitFromName != null ? unitFromName : "未知");
            }
        } else {
            resultMap.put("unit", resultUnit);
        }

        // 查询患者名称
        Patient patient = patientService.getById(labResult.getPatientId());
        resultMap.put("patientName", patient != null ? patient.getPatientName() : "未知");

        // 查询检验项目名称和参考范围
        resultMap.put("testName", itemDict != null ? itemDict.getItemName() : "未知");
        resultMap.put("referenceRange", itemDict != null ? itemDict.getNormalRange() : "未知");

        // 计算结果状态
        resultMap.put("resultStatus", calculateResultStatus(labResult.getResultValue(),
                itemDict != null ? itemDict.getNormalRange() : null));

        return resultMap;
    }

    // 获取检验项目趋势
    @Override
    public Result getTrend(String patientId, Integer itemId) {
        try {
            // 查询该患者该项目的所有历史记录
            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId)
                    .eq("item_id", itemId)
                    .eq("is_invalid", 0)
                    .orderByAsc("report_time");

            List<LabResult> results = baseMapper.selectList(wrapper);

            // 获取检验项目信息
            LabItemDict itemDict = labItemDictService.getById(itemId);
            String itemName = itemDict != null ? itemDict.getItemName() : "未知项目";
            String unit = itemDict != null ? itemDict.getDefaultUnit() : "";
            String normalRange = itemDict != null ? itemDict.getNormalRange() : "";

            // 构建趋势数据
            List<Map<String, Object>> trendData = new ArrayList<>();
            for (LabResult result : results) {
                Map<String, Object> point = new HashMap<>();
                point.put("reportTime", result.getReportTime());
                point.put("resultValue", result.getResultValue());
                point.put("unit", result.getResultUnit() != null ? result.getResultUnit() : unit);
                trendData.add(point);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("itemName", itemName);
            data.put("unit", unit);
            data.put("normalRange", normalRange);
            data.put("trendData", trendData);

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取趋势数据失败");
        }
    }

    // 评估并保存预警
    @Override
    public void evaluateAndSaveWarnings(LabResult result) {
        if (result == null || result.getPatientId() == null || result.getItemId() == null) {
            return;
        }
        try {
            List<WarningRule> rules = warningRuleService.lambdaQuery()
                    .eq(WarningRule::getRuleType, "LAB")
                    .eq(WarningRule::getEnabled, true)
                    .list();

            if (rules.isEmpty()) {
                return;
            }

            Patient patient = patientService.getById(result.getPatientId());
            LabItemDict itemDict = labItemDictService.getById(result.getItemId());

            Map<String, BigDecimal> recentValues = new HashMap<>();
            Map<String, BigDecimal> previousValues = new HashMap<>();

            List<LabResult> recentResults = getRecentLabResults(result.getPatientId(), result.getItemId(), RECENT_COUNT);
            for (int i = 0; i < recentResults.size(); i++) {
                LabResult r = recentResults.get(i);
                String key = String.valueOf(r.getItemId());
                if (i < recentResults.size() - 1) {
                    previousValues.put(key, recentResults.get(i + 1).getResultValue());
                }
                recentValues.put(key, r.getResultValue());
            }

            LabResultFacts facts = buildFacts(result, patient, itemDict, recentValues, previousValues);
            List<WarningRecord> triggeredRecords = evaluateRules(facts, rules);

            for (WarningRecord record : triggeredRecords) {
                if (!isDuplicateWarning(record)) {
                    warningRecordService.save(record);
                    webSocketSessionManager.broadcast(buildWarningMessage(record));
                    log.info("检验指标预警触发: patientId={}, itemId={}, message={}",
                            record.getPatientId(), record.getItemId(), record.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("评估预警异常: resultId={}", result.getResultId(), e);
        }
    }

    // 批量评估并保存预警
    @Override
    public void evaluateAndSaveWarningsBatch(List<LabResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        for (LabResult result : results) {
            evaluateAndSaveWarnings(result);
        }
    }

    // 获取最近检验结果
    private List<LabResult> getRecentLabResults(String patientId, Integer itemId, int limit) {
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId)
                .eq("item_id", itemId)
                .eq("is_invalid", false)
                .orderByDesc("report_time")
                .last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }

    // 构建事实数据
    private LabResultFacts buildFacts(LabResult result, Patient patient, LabItemDict itemDict,
                                     Map<String, BigDecimal> recentValues, Map<String, BigDecimal> previousValues) {
        LabResultFacts facts = new LabResultFacts();
        facts.setPatientId(result.getPatientId());
        facts.setPatientName(patient != null ? patient.getPatientName() : null);
        facts.setResultId(result.getResultId());
        facts.setItemId(result.getItemId());
        facts.setItemName(itemDict != null ? itemDict.getItemName() : null);
        facts.setResultValue(result.getResultValue());
        facts.setResultUnit(result.getResultUnit());
        facts.setReportTime(result.getReportTime());
        facts.setExecuteDept(result.getExecuteDept());
        facts.setExecuteDoc(result.getExecuteDoc());
        facts.setRecentItemValues(recentValues);
        facts.setPreviousItemValues(previousValues);
        return facts;
    }

    // 评估预警规则
    private List<WarningRecord> evaluateRules(LabResultFacts facts, List<WarningRule> rules) {
        List<WarningRecord> triggeredRecords = new ArrayList<>();
        for (WarningRule rule : rules) {
            if (rule.getItemId() != null && !rule.getItemId().equals(facts.getItemId())) {
                continue;
            }
            boolean triggered = evaluateThresholdRule(facts, rule);
            if (triggered) {
                WarningRecord record = createWarningRecord(facts, rule);
                triggeredRecords.add(record);
            }
        }
        return triggeredRecords;
    }

    // 评估阈值规则
    private boolean evaluateThresholdRule(LabResultFacts facts, WarningRule rule) {
        BigDecimal value = facts.getResultValue();
        if (value == null) {
            return false;
        }
        String conditionType = rule.getConditionType();
        if (conditionType == null) {
            return false;
        }
        return switch (conditionType) {
            case "ABOVE" -> evaluateAbove(value, rule);
            case "BELOW" -> evaluateBelow(value, rule);
            case "RANGE" -> evaluateRange(value, rule);
            case "TREND_UP" -> evaluateTrendUp(facts, rule);
            case "TREND_DOWN" -> evaluateTrendDown(facts, rule);
            case "CONTINUE_UP" -> evaluateContinueUp(facts, rule);
            case "CONTINUE_DOWN" -> evaluateContinueDown(facts, rule);
            default -> false;
        };
    }

    // 评估高于阈值
    private boolean evaluateAbove(BigDecimal value, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        return threshold != null && value.compareTo(threshold) > 0;
    }

    // 评估低于阈值
    private boolean evaluateBelow(BigDecimal value, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdLow();
        return threshold != null && value.compareTo(threshold) < 0;
    }

    // 评估超出范围
    private boolean evaluateRange(BigDecimal value, WarningRule rule) {
        BigDecimal high = rule.getThresholdHigh();
        BigDecimal low = rule.getThresholdLow();
        if (high != null && value.compareTo(high) > 0) {
            return true;
        }
        return low != null && value.compareTo(low) < 0;
    }

    // 评估上升趋势
    private boolean evaluateTrendUp(LabResultFacts facts, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        if (threshold == null) {
            return false;
        }
        BigDecimal changePercent = facts.getChangePercent(rule.getItemId());
        if (changePercent == null) {
            return false;
        }
        BigDecimal previousValue = facts.getPreviousValueForItem(rule.getItemId());
        return changePercent.compareTo(threshold) >= 0 && facts.getResultValue().compareTo(previousValue) > 0;
    }

    // 评估下降趋势
    private boolean evaluateTrendDown(LabResultFacts facts, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        if (threshold == null) {
            return false;
        }
        BigDecimal changePercent = facts.getChangePercent(rule.getItemId());
        if (changePercent == null) {
            return false;
        }
        BigDecimal previousValue = facts.getPreviousValueForItem(rule.getItemId());
        return changePercent.compareTo(threshold) >= 0 && facts.getResultValue().compareTo(previousValue) < 0;
    }

    // 评估持续上升
    private boolean evaluateContinueUp(LabResultFacts facts, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        if (threshold == null) {
            return false;
        }
        BigDecimal previous = facts.getPreviousValueForItem(rule.getItemId());
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal current = facts.getResultValue();
        BigDecimal currentChange = current.subtract(previous)
                .abs()
                .multiply(new BigDecimal("100"))
                .divide(previous.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return currentChange.compareTo(threshold) >= 0 && current.compareTo(previous) > 0;
    }

    // 评估持续下降
    private boolean evaluateContinueDown(LabResultFacts facts, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        if (threshold == null) {
            return false;
        }
        BigDecimal previous = facts.getPreviousValueForItem(rule.getItemId());
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal current = facts.getResultValue();
        BigDecimal currentChange = current.subtract(previous)
                .abs()
                .multiply(new BigDecimal("100"))
                .divide(previous.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return currentChange.compareTo(threshold) >= 0 && current.compareTo(previous) < 0;
    }

    // 创建预警记录
    private WarningRecord createWarningRecord(LabResultFacts facts, WarningRule rule) {
        WarningRecord record = new WarningRecord();
        record.setPatientId(facts.getPatientId());
        record.setPatientName(facts.getPatientName());
        record.setItemId(facts.getItemId());
        record.setItemName(facts.getItemName());
        record.setResultId(facts.getResultId());
        record.setRuleId(rule.getRuleId());
        record.setRuleType(rule.getRuleType());
        record.setSeverity(rule.getSeverity());
        record.setResultValue(facts.getResultValue());
        record.setIsRead(false);
        record.setMessage(buildWarningMessage(facts, rule));
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    // 构建预警消息
    private String buildWarningMessage(LabResultFacts facts, WarningRule rule) {
        String severityLabel = switch (rule.getSeverity()) {
            case "EMERGENCY" -> "紧急";
            case "CRITICAL" -> "危急";
            case "WARNING" -> "警告";
            default -> "提示";
        };
        String itemName = facts.getItemName() != null ? facts.getItemName() : String.valueOf(facts.getItemId());
        String patientName = facts.getPatientName() != null ? facts.getPatientName() : facts.getPatientId();
        return switch (rule.getConditionType()) {
            case "ABOVE" -> String.format("[%s] 患者%s的%s(%.2f)超过上限阈值(%.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue(), rule.getThresholdHigh());
            case "BELOW" -> String.format("[%s] 患者%s的%s(%.2f)低于下限阈值(%.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue(), rule.getThresholdLow());
            case "RANGE" -> facts.getResultValue().compareTo(rule.getThresholdHigh()) > 0
                    ? String.format("[%s] 患者%s的%s(%.2f)超过危急值上限(%.2f)", severityLabel, patientName, itemName, facts.getResultValue(), rule.getThresholdHigh())
                    : String.format("[%s] 患者%s的%s(%.2f)低于危急值下限(%.2f)", severityLabel, patientName, itemName, facts.getResultValue(), rule.getThresholdLow());
            case "TREND_UP" -> String.format("[%s] 患者%s的%s较上次骤升(当前值: %.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue());
            case "TREND_DOWN" -> String.format("[%s] 患者%s的%s较上次骤降(当前值: %.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue());
            default -> String.format("[%s] 患者%s的%s(%.2f)触发预警规则: %s",
                    severityLabel, patientName, itemName, facts.getResultValue(), rule.getRuleName());
        };
    }

    // 检查重复预警
    private boolean isDuplicateWarning(WarningRecord newRecord) {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(WARNING_DEDUP_MINUTES);
        QueryWrapper<WarningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", newRecord.getPatientId())
                .eq("item_id", newRecord.getItemId())
                .eq("rule_id", newRecord.getRuleId())
                .eq("is_read", false)
                .ge("create_time", thresholdTime)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        WarningRecord existing = warningRecordService.getOne(wrapper);
        return existing != null;
    }

    // 构建WebSocket预警消息
    private Map<String, Object> buildWarningMessage(WarningRecord record) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "WARNING");
        msg.put("warningId", record.getWarningId());
        msg.put("severity", record.getSeverity());
        msg.put("patientName", record.getPatientName());
        msg.put("itemName", record.getItemName());
        msg.put("message", record.getMessage());
        msg.put("resultValue", record.getResultValue());
        msg.put("createTime", record.getCreateTime().toString());
        return msg;
    }

    // 联合孤立森林检测
    @Override
    public com.hospit.vo.IsolationForestResultVO jointIsolationForestDetect(String patientId, List<Integer> itemIds) {
        com.hospit.vo.IsolationForestResultVO resultVO = new com.hospit.vo.IsolationForestResultVO();
        
        try {
            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId);
            wrapper.in("item_id", itemIds);
            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("report_time");
            wrapper.last("LIMIT 100");
            
            List<LabResult> results = this.list(wrapper);
            
            if (results == null || results.isEmpty()) {
                resultVO.setTriggered(false);
                resultVO.setAnomalyLevel("NORMAL");
                resultVO.setAlertMessage("没有找到相关检验结果");
                return resultVO;
            }

            Map<Integer, LabResult> latestByItem = new LinkedHashMap<>();
            for (LabResult r : results) {
                if (!latestByItem.containsKey(r.getItemId())) {
                    latestByItem.put(r.getItemId(), r);
                }
            }
            
            List<LabResult> latestResults = new ArrayList<>(latestByItem.values());

            List<com.hospit.entity.IsolationForestRule> rules = isolationForestService.getAllEnabledRules();
            
            com.hospit.entity.IsolationForestRule matchedRule = null;
            String targetItemIds = String.join(",", itemIds.stream()
                    .map(String::valueOf)
                    .sorted()
                    .toList());
            
            for (com.hospit.entity.IsolationForestRule rule : rules) {
                String ruleItemIds = rule.getItemIds();
                List<String> ruleItems = Arrays.asList(ruleItemIds.split(","))
                        .stream()
                        .map(String::trim)
                        .sorted()
                        .toList();
                List<String> targetItems = itemIds.stream()
                        .map(String::valueOf)
                        .sorted()
                        .toList();
                
                if (ruleItems.equals(targetItems)) {
                    matchedRule = rule;
                    break;
                }
            }
            
            IIsolationForestService.IsolationResult isolationResult = 
                    isolationForestService.jointDetect(latestResults, matchedRule);
            
            resultVO.setIsolationScore(isolationResult.getIsolationScore());
            resultVO.setCombinedScore(isolationResult.getCombinedScore());
            resultVO.setThreshold(matchedRule != null ? matchedRule.getThresholdScore() : BigDecimal.valueOf(0.5));
            resultVO.setAnomalyLevel(isolationResult.getAnomalyLevel());
            resultVO.setAlertMessage(isolationResult.getAlertMessage());
            resultVO.setTriggered("ANOMALY".equals(isolationResult.getAnomalyLevel()) || 
                                   "SUSPICIOUS".equals(isolationResult.getAnomalyLevel()));
            
            List<com.hospit.vo.IsolationForestResultVO.AnomalyItemVO> anomalyItems = new ArrayList<>();
            if (isolationResult.getZscoreAnomalies() != null) {
                for (Map.Entry<Integer, Double> entry : isolationResult.getZscoreAnomalies().entrySet()) {
                    com.hospit.vo.IsolationForestResultVO.AnomalyItemVO itemVO = 
                            new com.hospit.vo.IsolationForestResultVO.AnomalyItemVO();
                    itemVO.setItemId(entry.getKey());
                    LabItemDict item = labItemDictService.getById(entry.getKey());
                    itemVO.setItemName(item != null ? item.getItemName() : "指标" + entry.getKey());
                    itemVO.setZscore(entry.getValue());
                    
                    LabResult result = latestByItem.get(entry.getKey());
                    if (result != null) {
                        itemVO.setResultValue(result.getResultValue());
                    }
                    
                    LabItemStatistics stats = statisticsComputeService.getStatistics(entry.getKey(), "GLOBAL");
                    if (stats != null) {
                        itemVO.setMeanValue(stats.getMeanValue());
                        itemVO.setStdDeviation(stats.getStdDeviation());
                    }
                    
                    anomalyItems.add(itemVO);
                }
            }
            resultVO.setAnomalyItems(anomalyItems);
            
        } catch (Exception e) {
            log.error("孤立森林联合检测失败: patientId={}", patientId, e);
            resultVO.setTriggered(false);
            resultVO.setAnomalyLevel("ERROR");
            resultVO.setAlertMessage("检测失败: " + e.getMessage());
        }
        
        return resultVO;
    }

}