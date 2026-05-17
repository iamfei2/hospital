package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospit.algorithm.IsolationForest;
import com.hospit.entity.IsolationForestModel;
import com.hospit.entity.IsolationForestRule;
import com.hospit.entity.LabItemDict;
import com.hospit.entity.LabItemStatistics;
import com.hospit.entity.LabResult;
import com.hospit.mapper.IsolationForestMapper;
import com.hospit.mapper.IsolationForestModelMapper;
import com.hospit.mapper.LabResultMapper;
import com.hospit.mapper.LabItemDictMapper;
import com.hospit.service.IIsolationForestService;
import com.hospit.service.IStatisticsComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 孤立森林异常检测服务 - 多指标联合异常检测
 * 结合Z-Score预筛和孤立森林算法，综合评分判定异常
 * 支持模型训练、历史数据回扫、实时联合检测
 */
@Service
public class IsolationForestServiceImpl implements IIsolationForestService {

    private static final Logger log = LoggerFactory.getLogger(IsolationForestServiceImpl.class);
    private static final int MIN_SAMPLES = 5;
    private static final double DEFAULT_THRESHOLD = 0.5;
    private static final int RECENT_DAYS = 36500;

    @Autowired
    private IsolationForestMapper isolationForestMapper;

    @Autowired
    private IsolationForestModelMapper isolationForestModelMapper;

    @Autowired
    private LabResultMapper labResultMapper;

    @Autowired
    private LabItemDictMapper labItemDictMapper;

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    private final Map<Long, IsolationForest> modelCache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public IsolationResult jointDetect(List<LabResult> results, IsolationForestRule rule) {
        IsolationResult result = new IsolationResult();
        
        if (results == null || results.isEmpty()) {
            result.setAnomalyLevel("NORMAL");
            result.setAlertMessage("没有找到相关检验结果");
            return result;
        }
        
        List<Integer> itemIds;
        boolean hasRule = rule != null && rule.getItemIds() != null && !rule.getItemIds().isEmpty();
        
        if (hasRule) {
            String[] itemIdStrs = rule.getItemIds().split(",");
            itemIds = Arrays.stream(itemIdStrs)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } else {
            itemIds = results.stream()
                    .map(LabResult::getItemId)
                    .distinct()
                    .collect(Collectors.toList());
            result.setAlertMessage("该指标组合未配置Isolation Forest模型，仅基于Z-score提供参考分析");
        }

        double[] featureVector = buildFeatureVector(results, itemIds);
        if (featureVector == null || featureVector.length == 0) {
            result.setAnomalyLevel("NORMAL");
            return result;
        }

        Map<Integer, Double> zscoreAnomalies = zscoreDetect(results, itemIds);
        result.setZscoreAnomalies(zscoreAnomalies);

        double isolationScore = -1.0;
        double threshold = DEFAULT_THRESHOLD;
        
        if (hasRule) {
            IsolationForest forest = getOrBuildModel(rule);
            if (forest != null) {
                isolationScore = forest.predict(featureVector);
            }
            threshold = rule.getThresholdScore() != null ? 
                    rule.getThresholdScore().doubleValue() : DEFAULT_THRESHOLD;
        }
        result.setIsolationScore(isolationScore);

        double combinedScore = hasRule ? combinedScoreMethod(zscoreAnomalies, isolationScore) : calculateZscoreOnlyScore(zscoreAnomalies);
        result.setCombinedScore(combinedScore);
        result.setThreshold(threshold);

        String anomalyLevel = determineAnomalyLevel(combinedScore, zscoreAnomalies);
        result.setAnomalyLevel(anomalyLevel);

        buildAlertMessages(result, results, itemIds, zscoreAnomalies);

        return result;
    }
    
    private double calculateZscoreOnlyScore(Map<Integer, Double> zscoreAnomalies) {
        if (zscoreAnomalies == null || zscoreAnomalies.isEmpty()) {
            return 0.0;
        }
        double maxAbsZscore = zscoreAnomalies.values().stream()
                .mapToDouble(Math::abs)
                .max()
                .orElse(0.0);
        return Math.min(maxAbsZscore / 3.0, 1.0);
    }

    @Override
    public void buildModel(IsolationForestRule rule) {
        if (rule == null || rule.getItemIds() == null) {
            return;
        }

        String[] itemIdStrs = rule.getItemIds().split(",");
        List<Integer> itemIds = Arrays.stream(itemIdStrs)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<double[]> trainingData = collectTrainingData(itemIds);
        
        if (trainingData.size() < MIN_SAMPLES) {
            log.warn("样本数量不足，跳过模型训练: ruleId={}, sampleCount={}", 
                    rule.getRuleId(), trainingData.size());
            return;
        }

        IsolationForest forest = new IsolationForest(100, 256, 10, 
                rule.getContamination() != null ? rule.getContamination().doubleValue() : 0.1);
        forest.buildForest(trainingData);

        modelCache.put(rule.getRuleId(), forest);

        System.out.println("DEBUG: Training complete, about to save model. Sample count: " + trainingData.size());
        saveModel(rule, forest, trainingData.size());
        System.out.println("DEBUG: saveModel called successfully");
    }

    @Override
    public void trainModel(Long ruleId) {
        IsolationForestRule rule = getRuleById(ruleId);
        if (rule != null) {
            buildModel(rule);
        }
    }

    @Override
    public IsolationForestRule getRuleById(Long ruleId) {
        return isolationForestMapper.selectById(ruleId);
    }

    @Override
    public List<IsolationForestRule> getAllEnabledRules() {
        QueryWrapper<IsolationForestRule> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", true);
        return isolationForestMapper.selectList(wrapper);
    }

    @Override
    public void rebuildAllModels() {
        List<IsolationForestRule> rules = getAllEnabledRules();
        for (IsolationForestRule rule : rules) {
            try {
                buildModel(rule);
                log.info("孤立森林模型构建完成: ruleId={}, ruleName={}", 
                        rule.getRuleId(), rule.getRuleName());
            } catch (Exception e) {
                log.error("孤立森林模型构建失败: ruleId={}", rule.getRuleId(), e);
            }
        }
    }

    private double[] buildFeatureVector(List<LabResult> results, List<Integer> itemIds) {
        Map<Integer, BigDecimal> resultMap = new HashMap<>();
        for (LabResult r : results) {
            if (r.getResultValue() != null) {
                resultMap.put(r.getItemId(), r.getResultValue());
            }
        }

        double[] vector = new double[itemIds.size()];
        boolean hasAnyValue = false;
        
        for (int i = 0; i < itemIds.size(); i++) {
            Integer itemId = itemIds.get(i);
            BigDecimal value = resultMap.get(itemId);
            
            if (value != null) {
                LabItemStatistics stats = statisticsComputeService.getStatistics(itemId, "GLOBAL");
                if (stats != null && stats.getMeanValue() != null && stats.getStdDeviation() != null
                        && stats.getStdDeviation().compareTo(BigDecimal.ZERO) > 0) {
                    vector[i] = value.subtract(stats.getMeanValue())
                            .divide(stats.getStdDeviation(), 4, RoundingMode.HALF_UP)
                            .doubleValue();
                    hasAnyValue = true;
                } else {
                    vector[i] = value.doubleValue();
                    hasAnyValue = true;
                }
            } else {
                vector[i] = 0.0;
            }
        }

        return hasAnyValue ? vector : null;
    }

    private Map<Integer, Double> zscoreDetect(List<LabResult> results, List<Integer> itemIds) {
        Map<Integer, Double> anomalies = new HashMap<>();
        
        for (LabResult r : results) {
            if (r.getResultValue() == null) continue;
            
            LabItemStatistics stats = statisticsComputeService.getStatistics(r.getItemId(), "GLOBAL");
            if (stats == null || stats.getMeanValue() == null || stats.getStdDeviation() == null) {
                stats = statisticsComputeService.getStatistics(r.getItemId(), 
                        r.getExecuteDept() != null ? r.getExecuteDept() : "GLOBAL");
            }
            
            if (stats != null && stats.getMeanValue() != null && stats.getStdDeviation() != null
                    && stats.getStdDeviation().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal zscore = statisticsComputeService.calculateZScore(
                        r.getResultValue(), stats.getMeanValue(), stats.getStdDeviation());
                double absZscore = Math.abs(zscore.doubleValue());
                
                if (absZscore > 2.0) {
                    anomalies.put(r.getItemId(), zscore.doubleValue());
                }
            }
        }
        
        return anomalies;
    }

    private double combinedScoreMethod(Map<Integer, Double> zscoreAnomalies, double isolationScore) {
        if (zscoreAnomalies.isEmpty()) {
            return isolationScore;
        }
        
        double avgAbsZscore = zscoreAnomalies.values().stream()
                .mapToDouble(Math::abs)
                .average()
                .orElse(0.0);
        
        double normalizedZscore = Math.min(avgAbsZscore / 4.0, 1.0);
        
        return 0.4 * normalizedZscore + 0.6 * isolationScore;
    }

    private String determineAnomalyLevel(double combinedScore, Map<Integer, Double> zscoreAnomalies) {
        if (combinedScore >= 0.6 || (!zscoreAnomalies.isEmpty() && combinedScore >= 0.4)) {
            return "ANOMALY";
        } else if (combinedScore >= 0.4 || (!zscoreAnomalies.isEmpty() && combinedScore >= 0.3)) {
            return "SUSPICIOUS";
        }
        return "NORMAL";
    }

    private void buildAlertMessages(IsolationResult result, List<LabResult> results, 
                                   List<Integer> itemIds, Map<Integer, Double> zscoreAnomalies) {
        List<String> messages = new ArrayList<>();
        
        messages.add(String.format("孤立森林得分：%.2f（阈值%.2f）", 
                result.getIsolationScore(), result.getThreshold()));
        messages.add(String.format("综合评分：%.2f", result.getCombinedScore()));
        
        if (!zscoreAnomalies.isEmpty()) {
            List<String> anomalyItems = new ArrayList<>();
            for (Map.Entry<Integer, Double> entry : zscoreAnomalies.entrySet()) {
                LabItemDict item = labItemDictMapper.selectById(entry.getKey());
                String itemName = item != null ? item.getItemName() : "指标" + entry.getKey();
                anomalyItems.add(String.format("%s(Z=%.2f)", itemName, entry.getValue()));
            }
            messages.add("异常指标：" + String.join(", ", anomalyItems));
        }
        
        String anomalyLevelText;
        switch (result.getAnomalyLevel()) {
            case "ANOMALY": anomalyLevelText = "异常"; break;
            case "SUSPICIOUS": anomalyLevelText = "可疑"; break;
            case "NORMAL": anomalyLevelText = "正常"; break;
            default: anomalyLevelText = result.getAnomalyLevel();
        }
        messages.add("异常等级：" + anomalyLevelText);
        result.setAlertMessage(String.join("\n", messages));
    }

    private List<double[]> collectTrainingData(List<Integer> itemIds) {
        List<double[]> data = new ArrayList<>();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(RECENT_DAYS);
        
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.in("item_id", itemIds);
        wrapper.eq("is_invalid", false);
        wrapper.ge("report_time", cutoffTime);
        wrapper.isNotNull("result_value");
        wrapper.orderByDesc("report_time");
        wrapper.last("LIMIT 10000");
        
        List<LabResult> results = labResultMapper.selectList(wrapper);
        
        Map<String, Map<Integer, LabResult>> patientResultsMap = new LinkedHashMap<>();
        for (LabResult r : results) {
            String patientId = r.getPatientId();
            patientResultsMap.computeIfAbsent(patientId, k -> new LinkedHashMap<>());
            Map<Integer, LabResult> patientResults = patientResultsMap.get(patientId);
            if (!patientResults.containsKey(r.getItemId())) {
                patientResults.put(r.getItemId(), r);
            }
        }
        
        for (Map<Integer, LabResult> patientResults : patientResultsMap.values()) {
            double[] vector = new double[itemIds.size()];
            boolean hasAnyValue = false;
            
            for (int i = 0; i < itemIds.size(); i++) {
                Integer itemId = itemIds.get(i);
                LabResult r = patientResults.get(itemId);
                
                if (r != null && r.getResultValue() != null) {
                    LabItemStatistics stats = statisticsComputeService.getStatistics(itemId, "GLOBAL");
                    if (stats != null && stats.getMeanValue() != null && stats.getStdDeviation() != null
                            && stats.getStdDeviation().compareTo(BigDecimal.ZERO) > 0) {
                        vector[i] = r.getResultValue().subtract(stats.getMeanValue())
                                .divide(stats.getStdDeviation(), 4, RoundingMode.HALF_UP)
                                .doubleValue();
                        hasAnyValue = true;
                    } else {
                        vector[i] = r.getResultValue().doubleValue();
                        hasAnyValue = true;
                    }
                } else {
                    vector[i] = 0.0;
                }
            }
            
            if (hasAnyValue) {
                data.add(vector);
            }
        }
        
        return data;
    }

    private IsolationForest getOrBuildModel(IsolationForestRule rule) {
        IsolationForest cached = modelCache.get(rule.getRuleId());
        if (cached != null) {
            return cached;
        }
        
        IsolationForestModel model = isolationForestModelMapper.selectModelByRuleId(rule.getRuleId());
        if (model != null && model.getModelParams() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = objectMapper.readValue(
                        model.getModelParams(), Map.class);
                IsolationForest forest = new IsolationForest();
                forest.loadFromParams(params);
                modelCache.put(rule.getRuleId(), forest);
                return forest;
            } catch (Exception e) {
                log.error("加载孤立森林模型失败: ruleId={}", rule.getRuleId(), e);
            }
        }
        
        buildModel(rule);
        return modelCache.get(rule.getRuleId());
    }

    private void saveModel(IsolationForestRule rule, IsolationForest forest, int sampleCount) {
        try {
            System.out.println("DEBUG saveModel: Starting for ruleId=" + rule.getRuleId());
            Map<String, Object> params = forest.getModelParams();
            String paramsJson = objectMapper.writeValueAsString(params);
            System.out.println("DEBUG saveModel: Params serialized, length=" + paramsJson.length());
            
            List<Integer> itemIdsList = Arrays.stream(rule.getItemIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            double[] scores = forest.predictBatch(collectTrainingData(itemIdsList));
            System.out.println("DEBUG saveModel: Scores calculated, count=" + scores.length);
            
            double meanScore = Arrays.stream(scores).average().orElse(0.0);
            double stdScore = Math.sqrt(Arrays.stream(scores)
                    .map(s -> Math.pow(s - meanScore, 2))
                    .average().orElse(0.0));
            
            IsolationForestModel model = new IsolationForestModel();
            model.setRuleId(rule.getRuleId());
            model.setItemIds(rule.getItemIds());
            model.setModelParams(paramsJson);
            model.setTrainedAt(LocalDateTime.now());
            model.setSampleCount(sampleCount);
            model.setMeanScore(BigDecimal.valueOf(meanScore));
            model.setStdScore(BigDecimal.valueOf(stdScore));
            System.out.println("DEBUG saveModel: Model object created, ruleId=" + model.getRuleId() + ", sampleCount=" + sampleCount);
            
            QueryWrapper<IsolationForestModel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rule_id", rule.getRuleId());
            IsolationForestModel existing = isolationForestModelMapper.selectOne(queryWrapper);
            System.out.println("DEBUG saveModel: Existing model check done, existing=" + (existing != null));
            
            if (existing != null) {
                model.setModelId(existing.getModelId());
                isolationForestModelMapper.updateById(model);
                System.out.println("DEBUG saveModel: Updated existing model, modelId=" + existing.getModelId());
            } else {
                int result = isolationForestModelMapper.insert(model);
                System.out.println("DEBUG saveModel: Insert result=" + result + ", modelId=" + model.getModelId());
            }
            System.out.println("DEBUG saveModel: Complete for ruleId=" + rule.getRuleId());
        } catch (Exception e) {
            System.out.println("DEBUG saveModel ERROR: " + e.getMessage());
            e.printStackTrace();
            log.error("保存孤立森林模型失败: ruleId={}", rule.getRuleId(), e);
        }
    }
}
