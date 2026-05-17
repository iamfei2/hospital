package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospit.entity.LabItemStatistics;
import com.hospit.entity.LabItemDict;
import com.hospit.entity.LabResult;
import com.hospit.mapper.LabItemStatisticsMapper;
import com.hospit.mapper.LabResultMapper;
import com.hospit.mapper.LabItemDictMapper;
import com.hospit.service.IStatisticsComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计计算服务 - 计算检验项目的历史基准（均值、标准差）
 * 用于Z-Score异常检测的动态基准计算
 */
@Service
public class StatisticsComputeServiceImpl implements IStatisticsComputeService {

    private static final String GLOBAL_DEPT = "GLOBAL";
    private static final int MIN_SAMPLE_COUNT = 10;

    @Autowired
    private LabItemStatisticsMapper statisticsMapper;

    @Autowired
    private LabResultMapper labResultMapper;

    @Autowired
    private LabItemDictMapper labItemDictMapper;

    // 计算并保存单个检验项目的统计基准
    @Override
    public void computeAndSaveStatistics(Integer itemId, String deptCode) {
        QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
        wrapper.eq("item_id", itemId);
        wrapper.eq("is_invalid", false);
        
        if (!GLOBAL_DEPT.equals(deptCode)) {
            wrapper.eq("execute_dept", deptCode);
        }
        
        List<LabResult> results = labResultMapper.selectList(wrapper);
        
        if (results == null || results.size() < MIN_SAMPLE_COUNT) {
            return;
        }

        List<BigDecimal> values = results.stream()
                .map(LabResult::getResultValue)
                .filter(v -> v != null)
                .toList();

        if (values.isEmpty()) {
            return;
        }

        int count = values.size();
        BigDecimal mean = calculateMean(values);
        BigDecimal stdDev = calculateStdDev(values, mean);
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(null);

        LabItemStatistics stats = new LabItemStatistics();
        stats.setItemId(itemId);
        stats.setDeptCode(deptCode);
        stats.setSampleCount(count);
        stats.setMeanValue(mean);
        stats.setStdDeviation(stdDev);
        stats.setMinValue(min);
        stats.setMaxValue(max);
        stats.setUpdateTime(LocalDateTime.now());

        statisticsMapper.upsert(stats);
    }

    // 重新计算所有检验项目的统计基准
    @Override
    public void computeAllStatistics() {
        QueryWrapper<LabItemDict> dictWrapper = new QueryWrapper<>();
        List<LabItemDict> items = labItemDictMapper.selectList(dictWrapper);

        for (LabItemDict item : items) {
            computeAndSaveStatistics(item.getItemId(), GLOBAL_DEPT);
        }
    }

    // 计算Z-Score：(当前值-均值)/标准差
    @Override
    public BigDecimal calculateZScore(BigDecimal value, BigDecimal mean, BigDecimal stdDeviation) {
        if (value == null || mean == null || stdDeviation == null) {
            return BigDecimal.ZERO;
        }
        if (stdDeviation.compareTo(new BigDecimal("0.0001")) < 0) {
            return BigDecimal.ZERO;
        }
        return value.subtract(mean).divide(stdDeviation, 4, RoundingMode.HALF_UP);
    }

    // 判断是否异常：|Z-Score| > 阈值
    @Override
    public boolean isAnomaly(BigDecimal value, BigDecimal mean, BigDecimal stdDeviation, double threshold) {
        if (value == null || mean == null || stdDeviation == null) {
            return false;
        }
        BigDecimal zScore = calculateZScore(value, mean, stdDeviation);
        return zScore.abs().compareTo(new BigDecimal(String.valueOf(threshold))) > 0;
    }

    // 获取指定项目和科室的统计基准
    @Override
    public LabItemStatistics getStatistics(Integer itemId, String deptCode) {
        return statisticsMapper.selectByItemIdAndDept(itemId, deptCode);
    }

    // 获取指定项目的所有统计基准（包括全院和科室级）
    @Override
    public List<LabItemStatistics> getStatisticsByItemId(Integer itemId) {
        return statisticsMapper.selectByItemId(itemId);
    }

    // 新检验结果录入时增量更新统计基准
    @Override
    public void updateStatisticsOnNewResult(LabResult result) {
        if (result == null || result.getResultValue() == null) {
            return;
        }
        String deptCode = result.getExecuteDept() != null ? result.getExecuteDept() : GLOBAL_DEPT;
        computeAndSaveStatistics(result.getItemId(), deptCode);
        computeAndSaveStatistics(result.getItemId(), GLOBAL_DEPT);
    }

    // 计算均值
    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(values.size()), 4, RoundingMode.HALF_UP);
    }

    // 计算标准差（样本标准差）
    private BigDecimal calculateStdDev(List<BigDecimal> values, BigDecimal mean) {
        if (values == null || values.size() < 2) {
            return BigDecimal.ONE;
        }
        BigDecimal sumSquaredDiff = values.stream()
                .map(v -> v.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double variance = sumSquaredDiff.doubleValue() / (values.size() - 1);
        double stdDev = Math.sqrt(variance);
        if (stdDev < 0.0001) {
            stdDev = 1.0;
        }
        return new BigDecimal(stdDev).setScale(4, RoundingMode.HALF_UP);
    }
}
