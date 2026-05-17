package com.hospit.service;

import com.hospit.entity.LabItemStatistics;
import com.hospit.entity.LabResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IStatisticsComputeService {

    void computeAndSaveStatistics(Integer itemId, String deptCode);

    void computeAllStatistics();

    BigDecimal calculateZScore(BigDecimal value, BigDecimal mean, BigDecimal stdDeviation);

    boolean isAnomaly(BigDecimal value, BigDecimal mean, BigDecimal stdDeviation, double threshold);

    LabItemStatistics getStatistics(Integer itemId, String deptCode);

    List<LabItemStatistics> getStatisticsByItemId(Integer itemId);

    void updateStatisticsOnNewResult(LabResult result);
}
