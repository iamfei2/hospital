package com.hospit.statistics;

import com.hospit.mapper.*;
import com.hospit.vo.DimensionStatRequest;
import com.hospit.vo.DimensionStatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AggregationEngine {

    @Autowired
    private CtExaminationMapper ctExaminationMapper;

    @Autowired
    private MriExaminationMapper mriExaminationMapper;

    @Autowired
    private PathologyExaminationMapper pathologyExaminationMapper;

    @Autowired
    private EnteroscopyExaminationMapper enteroscopyExaminationMapper;

    private static final Map<String, String> TABLE_TIME_FIELD_MAP = new HashMap<>();
    private static final Map<String, String> DEPT_FIELD_MAP = new HashMap<>();
    private static final Map<String, String> DOCTOR_FIELD_MAP = new HashMap<>();

    static {
        TABLE_TIME_FIELD_MAP.put("ct", "upload_time");
        TABLE_TIME_FIELD_MAP.put("mri", "upload_time");
        TABLE_TIME_FIELD_MAP.put("pathology", "sampling_time");
        TABLE_TIME_FIELD_MAP.put("enteroscopy", "upload_time");

        DEPT_FIELD_MAP.put("ct", "examine_dept");
        DEPT_FIELD_MAP.put("mri", "examine_dept");
        DEPT_FIELD_MAP.put("pathology", "pathology_dept");
        DEPT_FIELD_MAP.put("enteroscopy", "examine_dept");

        DOCTOR_FIELD_MAP.put("ct", "examine_doctor");
        DOCTOR_FIELD_MAP.put("mri", "examine_doctor");
        DOCTOR_FIELD_MAP.put("pathology", "pathology_doctor");
        DOCTOR_FIELD_MAP.put("enteroscopy", "examine_doctor");
    }

    // 执行维度聚合统计
    public DimensionStatResponse aggregate(DimensionStatRequest request) {
        List<String> dimensions = request.getDimensions();
        List<String> tables = request.getTables();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();

        if (dimensions == null || dimensions.isEmpty()) {
            return new DimensionStatResponse(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        if (tables == null || tables.isEmpty()) {
            tables = Arrays.asList("ct", "mri", "pathology", "enteroscopy");
        }

        String primaryDim = dimensions.get(0);
        String secondaryDim = dimensions.size() > 1 ? dimensions.get(1) : null;

        Map<String, Map<String, Long>> mergedData = new LinkedHashMap<>();

        for (String table : tables) {
            String timeField = TABLE_TIME_FIELD_MAP.get(table);
            if (timeField == null) continue;

            List<Map<String, Object>> rawData = queryDimensionData(table, primaryDim, secondaryDim, timeField, startTime, endTime);
            mergeData(mergedData, rawData, secondaryDim != null);
        }

        return buildResponse(dimensions, mergedData, secondaryDim != null);
    }

    // 查询维度数据
    private List<Map<String, Object>> queryDimensionData(String table, String primaryDim, String secondaryDim, String timeField, String startTime, String endTime) {
        String dimensionField;
        String timeFormat;

        if ("byDept".equals(primaryDim)) {
            dimensionField = DEPT_FIELD_MAP.get(table);
            timeFormat = secondaryDim != null ? getTimeFormat(secondaryDim) : "%Y-%m";
        } else if ("byDoctor".equals(primaryDim)) {
            dimensionField = DOCTOR_FIELD_MAP.get(table);
            timeFormat = secondaryDim != null ? getTimeFormat(secondaryDim) : "%Y-%m";
        } else if ("byMonth".equals(primaryDim) || "byWeek".equals(primaryDim) || "byDay".equals(primaryDim)) {
            dimensionField = secondaryDim != null ? getDimensionField(secondaryDim, table) : getDimensionField(primaryDim, table);
            timeFormat = getTimeFormat(secondaryDim != null ? secondaryDim : primaryDim);
        } else {
            dimensionField = getDimensionField(primaryDim, table);
            timeFormat = getTimeFormat(primaryDim);
        }

        if (dimensionField == null || timeFormat == null) {
            return new ArrayList<>();
        }

        return queryByDimensionAndTime(table, dimensionField, timeField, timeFormat, startTime, endTime);
    }

    // 按维度和时间查询
    private List<Map<String, Object>> queryByDimensionAndTime(String table, String dimensionField, String timeField, String timeFormat, String startTime, String endTime) {
        switch (table) {
            case "ct":
                return ctExaminationMapper.countByDimensionAndTime(dimensionField, timeField, timeFormat, startTime, endTime);
            case "mri":
                return mriExaminationMapper.countByDimensionAndTime(dimensionField, timeField, timeFormat, startTime, endTime);
            case "pathology":
                return pathologyExaminationMapper.countByDimensionAndTime(dimensionField, timeField, timeFormat, startTime, endTime);
            case "enteroscopy":
                return enteroscopyExaminationMapper.countByDimensionAndTime(dimensionField, timeField, timeFormat, startTime, endTime);
            default:
                return new ArrayList<>();
        }
    }

    // 获取维度字段
    private String getDimensionField(String dimension, String table) {
        switch (dimension) {
            case "byDept":
                return DEPT_FIELD_MAP.get(table);
            case "byDoctor":
                return DOCTOR_FIELD_MAP.get(table);
            case "byExaminationPart":
                return "examination_part";
            case "bySpecimenType":
                return "specimen_type";
            case "byEnteroscopyType":
                return "enteroscopy_type";
            default:
                return null;
        }
    }

    // 获取时间格式
    private String getTimeFormat(String dimension) {
        switch (dimension) {
            case "byMonth":
                return "%Y-%m";
            case "byWeek":
                return "%Y-%u";
            case "byDay":
                return "%Y-%m-%d";
            default:
                return "%Y-%m";
        }
    }

    // 合并数据
    private void mergeData(Map<String, Map<String, Long>> mergedData, List<Map<String, Object>> rawData, boolean hasSecondary) {
        for (Map<String, Object> row : rawData) {
            String dimension = row.get("dimension") != null ? row.get("dimension").toString() : "未知";
            if (dimension.isEmpty()) dimension = "未知";
            
            String period = hasSecondary && row.get("period") != null ? row.get("period").toString() : "";
            long count = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;

            String key = hasSecondary ? dimension + "|" + period : dimension;
            if (key.isEmpty()) key = "未知";

            mergedData.computeIfAbsent(key, k -> new HashMap<>())
                    .merge(period.isEmpty() ? "_total" : period, count, Long::sum);
        }
    }

    private DimensionStatResponse buildResponse(List<String> dimensions, Map<String, Map<String, Long>> mergedData, boolean hasSecondary) {
        List<String> xAxis = new ArrayList<>();
        List<DimensionStatResponse.SeriesData> seriesList = new ArrayList<>();

        if (mergedData.isEmpty()) {
            return new DimensionStatResponse(dimensions, xAxis, seriesList);
        }

        if (hasSecondary) {
            Set<String> periodSet = new LinkedHashSet<>();
            for (Map<String, Long> innerMap : mergedData.values()) {
                periodSet.addAll(innerMap.keySet().stream().filter(p -> !"_total".equals(p)).collect(Collectors.toSet()));
            }
            xAxis.addAll(periodSet);

            for (Map.Entry<String, Map<String, Long>> entry : mergedData.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.isEmpty()) continue;
                
                String[] parts = key.split("\\|");
                if (parts.length == 0) continue;
                
                String dimValue = parts[0];
                if (dimValue == null || dimValue.isEmpty()) dimValue = "未知";

                List<Long> data = new ArrayList<>();
                for (String period : xAxis) {
                    data.add(entry.getValue().getOrDefault(period, 0L));
                }

                seriesList.add(new DimensionStatResponse.SeriesData(dimValue, data));
            }
        } else {
            xAxis.add("总计");
            for (Map.Entry<String, Map<String, Long>> entry : mergedData.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.isEmpty()) continue;
                
                long total = entry.getValue().getOrDefault("_total", 0L);
                seriesList.add(new DimensionStatResponse.SeriesData(key, Arrays.asList(total)));
            }
        }

        return new DimensionStatResponse(dimensions, xAxis, seriesList);
    }
}
