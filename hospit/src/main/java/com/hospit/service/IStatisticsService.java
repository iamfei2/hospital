package com.hospit.service;

import java.util.List;
import java.util.Map;

public interface IStatisticsService {

    Map<String, Object> getCountByTypes(List<String> types, String startTime, String endTime, String periodType);

    Map<String, Object> getTypeRatio(String startTime, String endTime);

    Map<String, Object> getDoctorWorkload(String startTime, String endTime);

    Map<String, Object> getMonthlyByDept(String startTime, String endTime);
}