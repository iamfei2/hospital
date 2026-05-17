package com.hospit.service;

import com.hospit.entity.LabItemStatistics;
import com.hospit.entity.LabResult;
import com.hospit.entity.WarningRecord;
import com.hospit.entity.WarningRule;
import com.hospit.rules.LabResultFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预警规则评估服务 - 支持多种条件类型的规则评估
 * 包括：阈值判断(ABOVE/BELOW/RANGE)、趋势判断(TREND_UP/DOWN)、
 * Z-Score异常判断(ZSCORE_ABOVE/BELOW)、连续趋势判断
 */
@Service
public class EasyRulesService {

    private static final Logger log = LoggerFactory.getLogger(EasyRulesService.class);
    private static final String GLOBAL_DEPT = "GLOBAL";
    private static final double DEFAULT_ZSCORE_THRESHOLD = 2.0;

    private final Map<Long, WarningRule> ruleCache = new ConcurrentHashMap<>();

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    public List<WarningRecord> evaluateLabResult(LabResult result, List<WarningRule> rules, 
                                                 Map<String, BigDecimal> recentValues,
                                                 Map<String, BigDecimal> previousValues) {
        List<WarningRecord> triggeredRecords = new ArrayList<>();

        LabResultFacts facts = buildFacts(result, recentValues, previousValues);

        for (WarningRule rule : rules) {
            if (rule.getEnabled() == null || !rule.getEnabled()) {
                continue;
            }

            if (rule.getItemId() != null && !rule.getItemId().equals(result.getItemId())) {
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

    public List<WarningRecord> evaluateKeywordRule(String patientId, String patientName, 
                                                   String examinationType, Long examinationId,
                                                   String reportConclusion,
                                                   List<WarningRule> keywordRules) {
        List<WarningRecord> triggeredRecords = new ArrayList<>();

        LabResultFacts facts = new LabResultFacts();
        facts.setPatientId(patientId);
        facts.setPatientName(patientName);
        facts.setReportConclusion(reportConclusion);
        facts.setExaminationType(examinationType);
        facts.setExaminationId(examinationId);

        for (WarningRule rule : keywordRules) {
            if (rule.getEnabled() == null || !rule.getEnabled()) {
                continue;
            }

            if (!"KEYWORD".equals(rule.getRuleType())) {
                continue;
            }

            boolean triggered = evaluateKeywordRule(facts, rule);
            if (triggered) {
                WarningRecord record = createKeywordWarningRecord(facts, rule);
                triggeredRecords.add(record);
            }
        }

        return triggeredRecords;
    }

    private LabResultFacts buildFacts(LabResult result, Map<String, BigDecimal> recentValues,
                                      Map<String, BigDecimal> previousValues) {
        LabResultFacts facts = new LabResultFacts();
        facts.setPatientId(result.getPatientId());
        facts.setResultId(result.getResultId());
        facts.setItemId(result.getItemId());
        facts.setResultValue(result.getResultValue());
        facts.setResultUnit(result.getResultUnit());
        facts.setReportTime(result.getReportTime());
        facts.setExecuteDept(result.getExecuteDept());
        facts.setExecuteDoc(result.getExecuteDoc());

        if (recentValues != null) {
            facts.setRecentItemValues(recentValues);
        }
        if (previousValues != null) {
            facts.setPreviousItemValues(previousValues);
        }

        return facts;
    }

    private boolean evaluateThresholdRule(LabResultFacts facts, WarningRule rule) {
        ruleCache.put(rule.getRuleId(), rule);

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
            case "ZSCORE_ABOVE" -> evaluateZScoreAbove(facts, rule);
            case "ZSCORE_BELOW" -> evaluateZScoreBelow(facts, rule);
            default -> false;
        };
    }

    private boolean evaluateAbove(BigDecimal value, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdHigh();
        return threshold != null && value.compareTo(threshold) > 0;
    }

    private boolean evaluateBelow(BigDecimal value, WarningRule rule) {
        BigDecimal threshold = rule.getThresholdLow();
        return threshold != null && value.compareTo(threshold) < 0;
    }

    private boolean evaluateRange(BigDecimal value, WarningRule rule) {
        BigDecimal high = rule.getThresholdHigh();
        BigDecimal low = rule.getThresholdLow();
        if (high != null && value.compareTo(high) > 0) {
            return true;
        }
        if (low != null && value.compareTo(low) < 0) {
            return true;
        }
        return false;
    }

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

    private boolean evaluateZScoreAbove(LabResultFacts facts, WarningRule rule) {
        BigDecimal value = facts.getResultValue();
        if (value == null) {
            return false;
        }
        String deptCode = facts.getExecuteDept() != null ? facts.getExecuteDept() : GLOBAL_DEPT;
        LabItemStatistics stats = statisticsComputeService.getStatistics(facts.getItemId(), deptCode);
        if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
            stats = statisticsComputeService.getStatistics(facts.getItemId(), GLOBAL_DEPT);
        }
        if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
            return false;
        }
        double threshold = rule.getThresholdHigh() != null ? rule.getThresholdHigh().doubleValue() : DEFAULT_ZSCORE_THRESHOLD;
        return statisticsComputeService.isAnomaly(value, stats.getMeanValue(), stats.getStdDeviation(), threshold)
                && value.compareTo(stats.getMeanValue()) > 0;
    }

    private boolean evaluateZScoreBelow(LabResultFacts facts, WarningRule rule) {
        BigDecimal value = facts.getResultValue();
        if (value == null) {
            return false;
        }
        String deptCode = facts.getExecuteDept() != null ? facts.getExecuteDept() : GLOBAL_DEPT;
        LabItemStatistics stats = statisticsComputeService.getStatistics(facts.getItemId(), deptCode);
        if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
            stats = statisticsComputeService.getStatistics(facts.getItemId(), GLOBAL_DEPT);
        }
        if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
            return false;
        }
        double threshold = rule.getThresholdLow() != null ? rule.getThresholdLow().doubleValue() : DEFAULT_ZSCORE_THRESHOLD;
        return statisticsComputeService.isAnomaly(value, stats.getMeanValue(), stats.getStdDeviation(), threshold)
                && value.compareTo(stats.getMeanValue()) < 0;
    }

    private boolean evaluateKeywordRule(LabResultFacts facts, WarningRule rule) {
        String conclusion = facts.getReportConclusion();
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

    private WarningRecord createWarningRecord(LabResultFacts facts, WarningRule rule) {
        WarningRecord record = new WarningRecord();
        record.setPatientId(facts.getPatientId());
        record.setItemId(facts.getItemId());
        record.setResultId(facts.getResultId());
        record.setRuleId(rule.getRuleId());
        record.setRuleType(rule.getRuleType());
        record.setSeverity(rule.getSeverity());
        record.setResultValue(facts.getResultValue());
        record.setIsRead(false);
        record.setMessage(buildWarningMessage(facts, rule));
        record.setCreateTime(java.time.LocalDateTime.now());
        return record;
    }

    private WarningRecord createKeywordWarningRecord(LabResultFacts facts, WarningRule rule) {
        WarningRecord record = new WarningRecord();
        record.setPatientId(facts.getPatientId());
        record.setPatientName(facts.getPatientName());
        record.setExaminationType(facts.getExaminationType());
        record.setExaminationId(facts.getExaminationId());
        record.setRuleId(rule.getRuleId());
        record.setRuleType(rule.getRuleType());
        record.setSeverity(rule.getSeverity());
        record.setIsRead(false);
        record.setMessage(buildKeywordWarningMessage(facts, rule));
        record.setCreateTime(java.time.LocalDateTime.now());
        return record;
    }

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
                    severityLabel, patientName, itemName,
                    facts.getResultValue(), rule.getThresholdHigh());
            case "BELOW" -> String.format("[%s] 患者%s的%s(%.2f)低于下限阈值(%.2f)",
                    severityLabel, patientName, itemName,
                    facts.getResultValue(), rule.getThresholdLow());
            case "RANGE" -> {
                if (facts.getResultValue().compareTo(rule.getThresholdHigh()) > 0) {
                    yield String.format("[%s] 患者%s的%s(%.2f)超过危急值上限(%.2f)",
                            severityLabel, patientName, itemName,
                            facts.getResultValue(), rule.getThresholdHigh());
                } else {
                    yield String.format("[%s] 患者%s的%s(%.2f)低于危急值下限(%.2f)",
                            severityLabel, patientName, itemName,
                            facts.getResultValue(), rule.getThresholdLow());
                }
            }
            case "TREND_UP" -> String.format("[%s] 患者%s的%s较上次骤升(当前值: %.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue());
            case "TREND_DOWN" -> String.format("[%s] 患者%s的%s较上次骤降(当前值: %.2f)",
                    severityLabel, patientName, itemName, facts.getResultValue());
            case "ZSCORE_ABOVE", "ZSCORE_BELOW" -> {
                String deptCode = facts.getExecuteDept() != null ? facts.getExecuteDept() : GLOBAL_DEPT;
                LabItemStatistics stats = statisticsComputeService.getStatistics(facts.getItemId(), deptCode);
                if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
                    stats = statisticsComputeService.getStatistics(facts.getItemId(), GLOBAL_DEPT);
                }
                if (stats != null && stats.getMeanValue() != null && stats.getStdDeviation() != null) {
                    BigDecimal zScore = statisticsComputeService.calculateZScore(
                            facts.getResultValue(), stats.getMeanValue(), stats.getStdDeviation());
                    yield String.format("[%s] 患者%s的%s(%.2f)Z-Score异常(%.2f，阈值%.1f，均值%.2f，标准差%.2f)",
                            severityLabel, patientName, itemName,
                            facts.getResultValue(), zScore,
                            rule.getThresholdHigh() != null ? rule.getThresholdHigh().doubleValue() : DEFAULT_ZSCORE_THRESHOLD,
                            stats.getMeanValue(), stats.getStdDeviation());
                }
                yield String.format("[%s] 患者%s的%s(%.2f)Z-Score异常(统计信息不可用)",
                        severityLabel, patientName, itemName, facts.getResultValue());
            }
            default -> String.format("[%s] 患者%s的%s(%.2f)触发预警规则: %s",
                    severityLabel, patientName, itemName,
                    facts.getResultValue(), rule.getRuleName());
        };
    }

    private String buildKeywordWarningMessage(LabResultFacts facts, WarningRule rule) {
        String severityLabel = switch (rule.getSeverity()) {
            case "EMERGENCY" -> "紧急";
            case "CRITICAL" -> "危急";
            case "WARNING" -> "警告";
            default -> "提示";
        };

        String examTypeName = switch (facts.getExaminationType()) {
            case "CT" -> "CT检查";
            case "MRI" -> "MRI检查";
            case "PATHOLOGY" -> "病理检查";
            case "ENTEROSCOPY" -> "肠镜检查";
            default -> "检查";
        };

        String patientName = facts.getPatientName() != null ? facts.getPatientName() : facts.getPatientId();

        return String.format("[%s] 患者%s的%s报告发现关键词: %s",
                severityLabel, patientName, examTypeName, rule.getDescription());
    }

    public void clearCache() {
        ruleCache.clear();
        log.info("Easy Rules 缓存已清除");
    }
}
