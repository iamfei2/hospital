package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospit.entity.*;
import com.hospit.service.*;
import com.hospit.vo.RuleExpression;
import com.hospit.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 预警引擎服务 - 核心预警规则评估
 * 支持检验指标预警、关键词预警、数据缺失预警、操作异常预警
 * 评估完成后通过WebSocket推送预警消息
 */
@Service
public class WarningEngineServiceImpl implements IWarningEngineService {

    private static final Logger log = LoggerFactory.getLogger(WarningEngineServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final long WARNING_DEDUP_MINUTES = 30;

    @Autowired
    private IWarningRuleService warningRuleService;

    @Autowired
    private IWarningRecordService warningRecordService;

    @Autowired
    private ILabItemDictService labItemDictService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private ILabResultService labResultService;

    @Autowired
    private ICtExaminationService ctExaminationService;

    @Autowired
    private IMriExaminationService mriExaminationService;

    @Autowired
    private IPathologyExaminationService pathologyExaminationService;

    @Autowired
    private IEnteroscopyExaminationService enteroscopyExaminationService;

    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    @Override
    @Async
    public void evaluate(LabResult result) {
        try {
            List<WarningRule> rules = warningRuleService.getEnabledRules();
            for (WarningRule rule : rules) {
                if (!"LAB".equals(rule.getRuleType())) {
                    continue;
                }
                if (rule.getItemId() != null && !rule.getItemId().equals(result.getItemId())) {
                    continue;
                }

                boolean triggered = evaluateRule(result, rule);
                if (triggered) {
                    WarningRecord record = createWarningRecord(result, rule);
                    if (isDuplicateWarning(record)) {
                        continue;
                    }
                    warningRecordService.save(record);
                    webSocketSessionManager.broadcast(buildWarningMessage(record));
                    log.info("预警触发: rule={}, patient={}, item={}, value={}",
                            rule.getRuleName(), result.getPatientId(), result.getItemId(), result.getResultValue());
                }
            }
        } catch (Exception e) {
            log.error("预警评估异常", e);
        }
    }

    @Override
    @Async
    public void evaluateBatch(List<LabResult> results) {
        for (LabResult result : results) {
            evaluate(result);
        }
    }

    @Override
    @Async
    public void evaluateExamination(ExaminationContext context) {
        try {
            if (context.getReportConclusion() == null || context.getReportConclusion().trim().isEmpty()) {
                return;
            }
            
            List<WarningRule> rules = warningRuleService.getEnabledRules();
            for (WarningRule rule : rules) {
                if (!"KEYWORD".equals(rule.getRuleType())) {
                    continue;
                }
                
                boolean triggered = evaluateKeywordRule(context, rule);
                if (triggered) {
                    WarningRecord record = createExaminationWarningRecord(context, rule);
                    warningRecordService.save(record);
                    webSocketSessionManager.broadcast(buildWarningMessage(record));
                    log.info("关键词预警触发: rule={}, patient={}, type={}, conclusion={}",
                            rule.getRuleName(), context.getPatientId(), context.getExaminationType(), context.getReportConclusion());
                }
            }
        } catch (Exception e) {
            log.error("检查结果预警评估异常", e);
        }
    }

    @Override
    public int rescanAllLabResults() {
        int triggeredCount = 0;
        List<WarningRecord> batchRecords = new ArrayList<>();
        List<String> batchMessages = new ArrayList<>();
        Set<String> existingKeys = new HashSet<>();
        
        try {
            List<WarningRule> thresholdRules = warningRuleService.lambdaQuery()
                    .eq(WarningRule::getRuleType, "THRESHOLD")
                    .eq(WarningRule::getEnabled, true)
                    .list();
            
            if (thresholdRules.isEmpty()) {
                log.info("没有启用的指标异常预警规则，跳过扫描");
                return 0;
            }
            
            QueryWrapper<LabResult> baseWrapper = new QueryWrapper<>();
            baseWrapper.eq("is_invalid", false).isNotNull("result_value");
            long total = labResultService.count(baseWrapper);
            
            int batchSize = 500;
            int pageCount = (int) Math.ceil((double) total / batchSize);
            
            log.info("开始扫描历史检验结果，共 {} 条，分 {} 批处理", total, pageCount);
            
            for (int page = 1; page <= pageCount; page++) {
                Page<LabResult> pageResult = new Page<>(page, batchSize);
                labResultService.page(pageResult, baseWrapper);
                
                for (LabResult result : pageResult.getRecords()) {
                    for (WarningRule rule : thresholdRules) {
                        if (rule.getItemId() != null && !rule.getItemId().equals(result.getItemId())) {
                            continue;
                        }
                        
                        String recordKey = buildRecordKey(result, rule);
                        if (existingKeys.contains(recordKey)) {
                            continue;
                        }
                        
                        boolean triggered = evaluateRule(result, rule);
                        if (triggered) {
                            WarningRecord record = createWarningRecord(result, rule);
                            batchRecords.add(record);
                            batchMessages.add(buildWarningMessage(record));
                            existingKeys.add(recordKey);
                            triggeredCount++;
                        }
                    }
                }
                
                if (page % 10 == 0) {
                    log.info("检验结果扫描进度: {}/{} 页", page, pageCount);
                }
            }
            
            if (!batchRecords.isEmpty()) {
                warningRecordService.saveBatch(batchRecords);
                webSocketSessionManager.broadcast(buildBatchWarningMessage(batchMessages));
                log.info("批量保存 {} 条预警记录", batchRecords.size());
            }
            
            log.info("历史检验结果扫描完成，共触发 {} 条预警", triggeredCount);
        } catch (Exception e) {
            log.error("历史检验结果扫描异常", e);
        }
        return triggeredCount;
    }

    @Override
    public int rescanAllExaminations() {
        int triggeredCount = 0;
        List<WarningRecord> batchRecords = new ArrayList<>();
        List<String> batchMessages = new ArrayList<>();
        
        try {
            List<WarningRule> keywordRules = warningRuleService.lambdaQuery()
                    .eq(WarningRule::getRuleType, "KEYWORD")
                    .eq(WarningRule::getEnabled, true)
                    .list();
            
            if (keywordRules.isEmpty()) {
                log.info("没有启用的关键词预警规则，跳过扫描");
                return 0;
            }
            
            log.info("开始扫描历史检查结果...");
            
            for (WarningRule rule : keywordRules) {
                triggeredCount += scanCtExaminations(rule, batchRecords, batchMessages);
                triggeredCount += scanMriExaminations(rule, batchRecords, batchMessages);
                triggeredCount += scanPathologyExaminations(rule, batchRecords, batchMessages);
                triggeredCount += scanEnteroscopyExaminations(rule, batchRecords, batchMessages);
            }
            
            if (!batchRecords.isEmpty()) {
                warningRecordService.saveBatch(batchRecords);
                webSocketSessionManager.broadcast(buildBatchWarningMessage(batchMessages));
                log.info("批量保存 {} 条预警记录", batchRecords.size());
            }
            
            log.info("历史检查数据扫描完成，共触发 {} 条预警", triggeredCount);
        } catch (Exception e) {
            log.error("历史检查数据扫描异常", e);
        }
        return triggeredCount;
    }

    private String buildRecordKey(LabResult result, WarningRule rule) {
        return String.format("LAB:%d:%d:%s", result.getResultId(), rule.getRuleId(), rule.getConditionType());
    }

    private String buildRecordKey(String examType, Long examId, WarningRule rule) {
        return String.format("%s:%d:%d", examType, examId, rule.getRuleId());
    }

    private String buildBatchWarningMessage(List<String> messages) {
        if (messages.isEmpty()) return "";
        if (messages.size() == 1) return messages.get(0);
        return String.format("【批量预警】共触发 %d 条预警，请到预警记录页面查看", messages.size());
    }

    private int scanCtExaminations(WarningRule rule, List<WarningRecord> batchRecords, List<String> batchMessages) {
        int count = 0;
        List<CtExamination> list = ctExaminationService.lambdaQuery()
                .eq(CtExamination::getIsInvalid, false)
                .isNotNull(CtExamination::getReportConclusion)
                .list();
        for (CtExamination ct : list) {
            ExaminationContext context = new ExaminationContext();
            context.setPatientId(ct.getPatientId());
            context.setExaminationType("CT");
            context.setExaminationId(ct.getCtId());
            context.setReportConclusion(ct.getReportConclusion());
            if (evaluateKeywordRule(context, rule)) {
                WarningRecord record = createExaminationWarningRecord(context, rule);
                batchRecords.add(record);
                batchMessages.add(buildWarningMessage(record));
                count++;
            }
        }
        return count;
    }

    private int scanMriExaminations(WarningRule rule, List<WarningRecord> batchRecords, List<String> batchMessages) {
        int count = 0;
        List<MriExamination> list = mriExaminationService.lambdaQuery()
                .eq(MriExamination::getIsInvalid, false)
                .isNotNull(MriExamination::getReportConclusion)
                .list();
        for (MriExamination mri : list) {
            ExaminationContext context = new ExaminationContext();
            context.setPatientId(mri.getPatientId());
            context.setExaminationType("MRI");
            context.setExaminationId(mri.getMriId());
            context.setReportConclusion(mri.getReportConclusion());
            if (evaluateKeywordRule(context, rule)) {
                WarningRecord record = createExaminationWarningRecord(context, rule);
                batchRecords.add(record);
                batchMessages.add(buildWarningMessage(record));
                count++;
            }
        }
        return count;
    }

    private int scanPathologyExaminations(WarningRule rule, List<WarningRecord> batchRecords, List<String> batchMessages) {
        int count = 0;
        List<PathologyExamination> list = pathologyExaminationService.lambdaQuery()
                .eq(PathologyExamination::getIsInvalid, false)
                .isNotNull(PathologyExamination::getPathologyDiagnosis)
                .list();
        for (PathologyExamination pathology : list) {
            ExaminationContext context = new ExaminationContext();
            context.setPatientId(pathology.getPatientId());
            context.setExaminationType("PATHOLOGY");
            context.setExaminationId(pathology.getPathologyId());
            context.setReportConclusion(pathology.getPathologyDiagnosis());
            if (evaluateKeywordRule(context, rule)) {
                WarningRecord record = createExaminationWarningRecord(context, rule);
                batchRecords.add(record);
                batchMessages.add(buildWarningMessage(record));
                count++;
            }
        }
        return count;
    }

    private int scanEnteroscopyExaminations(WarningRule rule, List<WarningRecord> batchRecords, List<String> batchMessages) {
        int count = 0;
        List<EnteroscopyExamination> list = enteroscopyExaminationService.lambdaQuery()
                .eq(EnteroscopyExamination::getIsInvalid, false)
                .isNotNull(EnteroscopyExamination::getReportConclusion)
                .list();
        for (EnteroscopyExamination ent : list) {
            ExaminationContext context = new ExaminationContext();
            context.setPatientId(ent.getPatientId());
            context.setExaminationType("ENTEROSCOPY");
            context.setExaminationId(ent.getEnteroscopyId());
            context.setReportConclusion(ent.getReportConclusion());
            if (evaluateKeywordRule(context, rule)) {
                WarningRecord record = createExaminationWarningRecord(context, rule);
                batchRecords.add(record);
                batchMessages.add(buildWarningMessage(record));
                count++;
            }
        }
        return count;
    }

    private boolean evaluateKeywordRule(ExaminationContext context, WarningRule rule) {
        String conclusion = context.getReportConclusion();
        if (conclusion == null || conclusion.trim().isEmpty()) {
            return false;
        }
        
        String keywords = rule.getDescription();
        if (keywords == null || keywords.trim().isEmpty()) {
            return false;
        }
        
        String[] keywordArray = keywords.split("[,，]");
        for (String keyword : keywordArray) {
            keyword = keyword.trim();
            if (!keyword.isEmpty() && conclusion.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private WarningRecord createExaminationWarningRecord(ExaminationContext context, WarningRule rule) {
        WarningRecord record = new WarningRecord();
        record.setPatientId(context.getPatientId());
        
        Patient patient = patientService.getById(context.getPatientId());
        record.setPatientName(patient != null ? patient.getPatientName() : "");
        
        record.setExaminationType(context.getExaminationType());
        record.setExaminationId(context.getExaminationId());
        record.setRuleId(rule.getRuleId());
        record.setRuleType(rule.getRuleType());
        record.setSeverity(rule.getSeverity());
        record.setIsRead(false);
        
        String message = buildKeywordWarningMessage(context, rule);
        record.setMessage(message);
        record.setCreateTime(LocalDateTime.now());
        
        return record;
    }

    private String buildKeywordWarningMessage(ExaminationContext context, WarningRule rule) {
        Patient patient = patientService.getById(context.getPatientId());
        String patientName = patient != null ? patient.getPatientName() : context.getPatientId();
        
        String severityLabel = switch (rule.getSeverity()) {
            case "EMERGENCY" -> "紧急";
            case "CRITICAL" -> "危急";
            case "WARNING" -> "警告";
            default -> "提示";
        };
        
        String examTypeName = switch (context.getExaminationType()) {
            case "CT" -> "CT检查";
            case "MRI" -> "MRI检查";
            case "PATHOLOGY" -> "病理检查";
            case "ENTEROSCOPY" -> "肠镜检查";
            default -> context.getExaminationType();
        };
        
        return String.format("[%s] 患者%s的%s报告发现关键词: %s",
                severityLabel, patientName, examTypeName, rule.getDescription());
    }

    private boolean evaluateRule(LabResult result, WarningRule rule) {
        if (rule.getRuleExpression() != null && !rule.getRuleExpression().trim().isEmpty()) {
            return evaluateExpressionRule(result, rule);
        }

        if (result.getResultValue() == null) {
            return false;
        }

        BigDecimal value = result.getResultValue();
        String conditionType = rule.getConditionType();

        switch (conditionType) {
            case "ABOVE":
                return rule.getThresholdHigh() != null && value.compareTo(rule.getThresholdHigh()) > 0;

            case "BELOW":
                return rule.getThresholdLow() != null && value.compareTo(rule.getThresholdLow()) < 0;

            case "RANGE":
                boolean aboveHigh = rule.getThresholdHigh() != null && value.compareTo(rule.getThresholdHigh()) > 0;
                boolean belowLow = rule.getThresholdLow() != null && value.compareTo(rule.getThresholdLow()) < 0;
                return aboveHigh || belowLow;

            case "TREND_UP":
                return checkTrendUp(result, rule.getThresholdHigh());

            case "TREND_DOWN":
                return checkTrendDown(result, rule.getThresholdHigh());

            case "CONTINUE_UP":
                return checkContinueUp(result, rule.getThresholdHigh());

            case "CONTINUE_DOWN":
                return checkContinueDown(result, rule.getThresholdHigh());

            default:
                return false;
        }
    }

    private boolean evaluateExpressionRule(LabResult result, WarningRule rule) {
        try {
            RuleExpression expression = objectMapper.readValue(rule.getRuleExpression(), RuleExpression.class);
            Map<Integer, BigDecimal> itemValues = fetchPatientItemValues(result.getPatientId(), expression);
            return evaluateExpression(expression, itemValues);
        } catch (Exception e) {
            log.error("解析规则表达式失败: ruleId={}, expression={}", rule.getRuleId(), rule.getRuleExpression(), e);
            return false;
        }
    }

    private Map<Integer, BigDecimal> fetchPatientItemValues(String patientId, RuleExpression expression) {
        Map<Integer, BigDecimal> itemValues = new HashMap<>();
        if (expression.getConditions() != null) {
            for (RuleExpression.RuleCondition condition : expression.getConditions()) {
                if (condition.getItemId() != null) {
                    BigDecimal value = fetchLatestItemValue(patientId, condition.getItemId());
                    itemValues.put(condition.getItemId(), value);
                }
            }
        }
        return itemValues;
    }

    private BigDecimal fetchLatestItemValue(String patientId, Integer itemId) {
        try {
            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId)
                   .eq("item_id", itemId)
                   .eq("is_invalid", false)
                   .isNotNull("result_value")
                   .orderByDesc("report_time")
                   .last("LIMIT 1");
            LabResult result = labResultService.getOne(wrapper);
            return result != null ? result.getResultValue() : null;
        } catch (Exception e) {
            log.error("获取患者检验项目值失败: patientId={}, itemId={}", patientId, itemId, e);
            return null;
        }
    }

    private boolean evaluateExpression(RuleExpression expression, Map<Integer, BigDecimal> itemValues) {
        if (expression.getConditions() != null && !expression.getConditions().isEmpty()) {
            List<Boolean> results = new ArrayList<>();
            for (RuleExpression.RuleCondition condition : expression.getConditions()) {
                results.add(evaluateCondition(condition, itemValues));
            }
            return combineResults(expression.getLogic(), results);
        }

        if (expression.getExpressions() != null && !expression.getExpressions().isEmpty()) {
            List<Boolean> results = new ArrayList<>();
            for (RuleExpression subExpr : expression.getExpressions()) {
                results.add(evaluateExpression(subExpr, itemValues));
            }
            return combineResults(expression.getLogic(), results);
        }

        return false;
    }

    private boolean evaluateCondition(RuleExpression.RuleCondition condition, Map<Integer, BigDecimal> itemValues) {
        BigDecimal value = itemValues.get(condition.getItemId());
        if (value == null) {
            return false;
        }

        String operator = condition.getOperator();
        BigDecimal threshold = condition.getValue();

        if (threshold == null) {
            return false;
        }

        return switch (operator) {
            case ">" -> value.compareTo(threshold) > 0;
            case ">=" -> value.compareTo(threshold) >= 0;
            case "<" -> value.compareTo(threshold) < 0;
            case "<=" -> value.compareTo(threshold) <= 0;
            case "==" -> value.compareTo(threshold) == 0;
            case "!=" -> value.compareTo(threshold) != 0;
            default -> false;
        };
    }

    private boolean combineResults(String logic, List<Boolean> results) {
        if (results.isEmpty()) {
            return false;
        }
        if ("AND".equalsIgnoreCase(logic)) {
            return results.stream().allMatch(r -> r);
        } else {
            return results.stream().anyMatch(r -> r);
        }
    }

    private boolean checkTrendUp(LabResult result, BigDecimal thresholdPercent) {
        if (thresholdPercent == null || result.getResultValue() == null) {
            return false;
        }
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", result.getPatientId())
               .eq("item_id", result.getItemId())
               .eq("is_invalid", false)
               .ne("result_id", result.getResultId())
               .orderByDesc("report_time")
               .last("LIMIT 1");
        List<LabResult> history = labResultService.list(wrapper);
        if (history.isEmpty() || history.get(0).getResultValue() == null) {
            return false;
        }
        BigDecimal prevValue = history.get(0).getResultValue();
        if (prevValue.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal changePercent = result.getResultValue().subtract(prevValue)
                .abs().multiply(new BigDecimal("100")).divide(prevValue.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return changePercent.compareTo(thresholdPercent) >= 0 && result.getResultValue().compareTo(prevValue) > 0;
    }

    private boolean checkTrendDown(LabResult result, BigDecimal thresholdPercent) {
        if (thresholdPercent == null || result.getResultValue() == null) {
            return false;
        }
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", result.getPatientId())
               .eq("item_id", result.getItemId())
               .eq("is_invalid", false)
               .ne("result_id", result.getResultId())
               .orderByDesc("report_time")
               .last("LIMIT 1");
        List<LabResult> history = labResultService.list(wrapper);
        if (history.isEmpty() || history.get(0).getResultValue() == null) {
            return false;
        }
        BigDecimal prevValue = history.get(0).getResultValue();
        if (prevValue.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal changePercent = result.getResultValue().subtract(prevValue)
                .abs().multiply(new BigDecimal("100")).divide(prevValue.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return changePercent.compareTo(thresholdPercent) >= 0 && result.getResultValue().compareTo(prevValue) < 0;
    }

    private boolean checkContinueUp(LabResult result, BigDecimal thresholdPercent) {
        if (thresholdPercent == null || result.getResultValue() == null) {
            return false;
        }
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", result.getPatientId())
               .eq("item_id", result.getItemId())
               .eq("is_invalid", false)
               .ne("result_id", result.getResultId())
               .orderByDesc("report_time")
               .last("LIMIT 2");
        List<LabResult> history = labResultService.list(wrapper);
        if (history.size() < 2 || history.get(0).getResultValue() == null || history.get(1).getResultValue() == null) {
            return false;
        }
        BigDecimal latestValue = history.get(0).getResultValue();
        BigDecimal previousValue = history.get(1).getResultValue();
        if (latestValue.compareTo(BigDecimal.ZERO) == 0 || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal changePercent = latestValue.subtract(previousValue)
                .abs().multiply(new BigDecimal("100")).divide(previousValue.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return changePercent.compareTo(thresholdPercent) >= 0 && latestValue.compareTo(previousValue) > 0;
    }

    private boolean checkContinueDown(LabResult result, BigDecimal thresholdPercent) {
        if (thresholdPercent == null || result.getResultValue() == null) {
            return false;
        }
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", result.getPatientId())
               .eq("item_id", result.getItemId())
               .eq("is_invalid", false)
               .ne("result_id", result.getResultId())
               .orderByDesc("report_time")
               .last("LIMIT 2");
        List<LabResult> history = labResultService.list(wrapper);
        if (history.size() < 2 || history.get(0).getResultValue() == null || history.get(1).getResultValue() == null) {
            return false;
        }
        BigDecimal latestValue = history.get(0).getResultValue();
        BigDecimal previousValue = history.get(1).getResultValue();
        if (latestValue.compareTo(BigDecimal.ZERO) == 0 || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal changePercent = latestValue.subtract(previousValue)
                .abs().multiply(new BigDecimal("100")).divide(previousValue.abs(), 2, BigDecimal.ROUND_HALF_UP);
        return changePercent.compareTo(thresholdPercent) >= 0 && latestValue.compareTo(previousValue) < 0;
    }

    private WarningRecord createWarningRecord(LabResult result, WarningRule rule) {
        WarningRecord record = new WarningRecord();
        record.setPatientId(result.getPatientId());

        Patient patient = patientService.getById(result.getPatientId());
        record.setPatientName(patient != null ? patient.getPatientName() : "");

        record.setItemId(result.getItemId());
        if (result.getItemId() != null) {
            LabItemDict item = labItemDictService.getById(result.getItemId());
            record.setItemName(item != null ? item.getItemName() : "");
        }

        record.setResultId(result.getResultId());
        record.setRuleId(rule.getRuleId());
        record.setRuleType(rule.getRuleType());
        record.setSeverity(rule.getSeverity());
        record.setResultValue(result.getResultValue());
        record.setIsRead(false);

        String message = buildWarningMessage(result, rule);
        record.setMessage(message);
        record.setCreateTime(LocalDateTime.now());

        return record;
    }

    private String buildWarningMessage(LabResult result, WarningRule rule) {
        String itemName = "";
        if (result.getItemId() != null) {
            LabItemDict item = labItemDictService.getById(result.getItemId());
            itemName = item != null ? item.getItemName() : "";
        }
        Patient patient = patientService.getById(result.getPatientId());
        String patientName = patient != null ? patient.getPatientName() : result.getPatientId();

        String severityLabel = switch (rule.getSeverity()) {
            case "EMERGENCY" -> "紧急";
            case "CRITICAL" -> "危急";
            case "WARNING" -> "警告";
            default -> "提示";
        };

        switch (rule.getConditionType()) {
            case "ABOVE":
                return String.format("[%s] 患者%s的%s(%.2f)超过上限阈值(%.2f)",
                        severityLabel, patientName, itemName,
                        result.getResultValue(), rule.getThresholdHigh());
            case "BELOW":
                return String.format("[%s] 患者%s的%s(%.2f)低于下限阈值(%.2f)",
                        severityLabel, patientName, itemName,
                        result.getResultValue(), rule.getThresholdLow());
            case "RANGE":
                if (result.getResultValue().compareTo(rule.getThresholdHigh()) > 0) {
                    return String.format("[%s] 患者%s的%s(%.2f)超过危急值上限(%.2f)",
                            severityLabel, patientName, itemName,
                            result.getResultValue(), rule.getThresholdHigh());
                } else {
                    return String.format("[%s] 患者%s的%s(%.2f)低于危急值下限(%.2f)",
                            severityLabel, patientName, itemName,
                            result.getResultValue(), rule.getThresholdLow());
                }
            case "TREND_UP":
                return String.format("[%s] 患者%s的%s较上次骤升(当前值: %.2f)",
                        severityLabel, patientName, itemName, result.getResultValue());
            case "TREND_DOWN":
                return String.format("[%s] 患者%s的%s较上次骤降(当前值: %.2f)",
                        severityLabel, patientName, itemName, result.getResultValue());
            default:
                return String.format("[%s] 患者%s的%s(%.2f)触发预警规则: %s",
                        severityLabel, patientName, itemName,
                        result.getResultValue(), rule.getRuleName());
        }
    }

    private String buildWarningMessage(WarningRecord record) {
        return record.getMessage();
    }

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
}
