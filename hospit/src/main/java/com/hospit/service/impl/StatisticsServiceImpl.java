package com.hospit.service.impl;

import com.hospit.mapper.*;
import com.hospit.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private CtExaminationMapper ctExaminationMapper;

    @Autowired
    private MriExaminationMapper mriExaminationMapper;

    @Autowired
    private PathologyExaminationMapper pathologyExaminationMapper;

    @Autowired
    private EnteroscopyExaminationMapper enteroscopyExaminationMapper;

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter FULL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 按类型统计数量
    @Override
    public Map<String, Object> getCountByTypes(List<String> types, String startTime, String endTime, String periodType) {
        Map<String, Object> result = new HashMap<>();
        
        List<String> periods = generatePeriods(startTime, endTime, periodType);
        result.put("periods", periods);
        result.put("periodType", periodType);

        List<Map<String, Object>> series = new ArrayList<>();
        Map<String, Long> typeTotals = new LinkedHashMap<>();
        List<String> typeLabels = new ArrayList<>();

        for (String type : types) {
            String label = getTypeLabel(type);
            typeLabels.add(label);
            typeTotals.put(label, 0L);

            Map<String, Long> periodCounts = new LinkedHashMap<>();
            for (String p : periods) {
                periodCounts.put(p, 0L);
            }

            List<Map<String, Object>> groupedData = getGroupedData(type, startTime, endTime, periodType);

            for (Map<String, Object> row : groupedData) {
                String period = formatPeriod(row, periodType);
                Object countObj = row.get("count");
                long count = countObj != null ? ((Number) countObj).longValue() : 0L;
                periodCounts.put(period, count);
                typeTotals.merge(label, count, Long::sum);
            }

            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", label);
            seriesItem.put("data", new ArrayList<>(periodCounts.values()));
            series.add(seriesItem);
        }

        result.put("series", series);
        result.put("typeLabels", typeLabels);
        result.put("typeTotals", new ArrayList<>(typeTotals.values()));

        return result;
    }

    // 统计检查类型占比
    @Override
    public Map<String, Object> getTypeRatio(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();

        long ctCount = countByType("ct", startTime, endTime);
        long mriCount = countByType("mri", startTime, endTime);
        long entCount = countByType("enteroscopy", startTime, endTime);
        long pathCount = countByType("pathology", startTime, endTime);

        result.put("labels", Arrays.asList("CT检查", "核磁检查", "肠镜检查", "病理检查"));
        result.put("values", Arrays.asList(ctCount, mriCount, entCount, pathCount));
        result.put("total", ctCount + mriCount + entCount + pathCount);

        return result;
    }

    // 统计医生工作量
    @Override
    public Map<String, Object> getDoctorWorkload(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Long> doctorCounts = new LinkedHashMap<>();

        mergeDoctorCounts(doctorCounts, ctExaminationMapper.countByDoctorGrouped(startTime, endTime));
        mergeDoctorCounts(doctorCounts, mriExaminationMapper.countByDoctorGrouped(startTime, endTime));
        mergeDoctorCounts(doctorCounts, enteroscopyExaminationMapper.countByDoctorGrouped(startTime, endTime));
        mergeDoctorCounts(doctorCounts, pathologyExaminationMapper.countByDoctorGrouped(startTime, endTime));

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        doctorCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> {
                    labels.add(e.getKey());
                    values.add(e.getValue());
                });

        result.put("labels", labels);
        result.put("values", values);

        return result;
    }

    // 按科室统计月度数据
    @Override
    public Map<String, Object> getMonthlyByDept(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();

        List<String> months = generateMonthPeriods(startTime, endTime);
        result.put("months", months);

        Map<String, Map<String, Long>> deptData = new LinkedHashMap<>();

        for (String month : months) {
            String monthStart = month + "-01 00:00:00";
            String monthEnd = month + "-31 23:59:59";

            mergeDeptCounts(deptData, month, ctExaminationMapper.countByDeptGrouped(monthStart, monthEnd));
            mergeDeptCounts(deptData, month, mriExaminationMapper.countByDeptGrouped(monthStart, monthEnd));
            mergeDeptCounts(deptData, month, enteroscopyExaminationMapper.countByDeptGrouped(monthStart, monthEnd));
            mergeDeptCounts(deptData, month, pathologyExaminationMapper.countByDeptGrouped(monthStart, monthEnd));
        }

        result.put("deptData", deptData);

        return result;
    }

    // 按类型统计数量
    private long countByType(String type, String startTime, String endTime) {
        switch (type) {
            case "ct":
                Long ct = ctExaminationMapper.countByDateRange(startTime, endTime);
                return ct != null ? ct : 0L;
            case "mri":
                Long mri = mriExaminationMapper.countByDateRange(startTime, endTime);
                return mri != null ? mri : 0L;
            case "enteroscopy":
                Long ent = enteroscopyExaminationMapper.countByDateRange(startTime, endTime);
                return ent != null ? ent : 0L;
            case "pathology":
                Long path = pathologyExaminationMapper.countByDateRange(startTime, endTime);
                return path != null ? path : 0L;
            default:
                return 0L;
        }
    }

    // 获取分组数据
    private List<Map<String, Object>> getGroupedData(String type, String startTime, String endTime, String periodType) {
        switch (type) {
            case "ct":
                if ("day".equals(periodType)) return ctExaminationMapper.countDailyGrouped(startTime, endTime);
                if ("week".equals(periodType)) return ctExaminationMapper.countWeeklyGrouped(startTime, endTime);
                if ("month".equals(periodType)) return ctExaminationMapper.countMonthlyGrouped(startTime, endTime);
                return new ArrayList<>();
            case "mri":
                if ("day".equals(periodType)) return mriExaminationMapper.countDailyGrouped(startTime, endTime);
                if ("week".equals(periodType)) return mriExaminationMapper.countWeeklyGrouped(startTime, endTime);
                if ("month".equals(periodType)) return mriExaminationMapper.countMonthlyGrouped(startTime, endTime);
                return new ArrayList<>();
            case "enteroscopy":
                if ("day".equals(periodType)) return enteroscopyExaminationMapper.countDailyGrouped(startTime, endTime);
                if ("week".equals(periodType)) return enteroscopyExaminationMapper.countWeeklyGrouped(startTime, endTime);
                if ("month".equals(periodType)) return enteroscopyExaminationMapper.countMonthlyGrouped(startTime, endTime);
                return new ArrayList<>();
            case "pathology":
                if ("day".equals(periodType)) return pathologyExaminationMapper.countDailyGrouped(startTime, endTime);
                if ("week".equals(periodType)) return pathologyExaminationMapper.countWeeklyGrouped(startTime, endTime);
                if ("month".equals(periodType)) return pathologyExaminationMapper.countMonthlyGrouped(startTime, endTime);
                return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    // 格式化周期标签
    private String formatPeriod(Map<String, Object> row, String periodType) {
        if ("week".equals(periodType)) {
            Object yearWeek = row.get("yearWeek");
            if (yearWeek != null) {
                String yw = yearWeek.toString();
                if (yw.length() == 6) {
                    return yw.substring(0, 4) + "-W" + yw.substring(4);
                }
            }
            return yearWeek != null ? yearWeek.toString() : "";
        } else {
            Object period = row.get("period");
            return period != null ? period.toString() : "";
        }
    }

    // 生成周期列表
    private List<String> generatePeriods(String startTime, String endTime, String periodType) {
        List<String> periods = new ArrayList<>();
        LocalDate start = LocalDate.parse(startTime.substring(0, 10));
        LocalDate end = LocalDate.parse(endTime.substring(0, 10));

        if ("day".equals(periodType)) {
            LocalDate current = start;
            while (!current.isAfter(end)) {
                periods.add(current.format(DAY_FORMAT));
                current = current.plusDays(1);
            }
        } else if ("week".equals(periodType)) {
            LocalDate current = start;
            while (!current.isAfter(end)) {
                int year = current.getYear();
                int week = current.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                periods.add(String.format("%d-W%02d", year, week));
                current = current.plusWeeks(1);
            }
        } else if ("month".equals(periodType)) {
            LocalDate current = start.withDayOfMonth(1);
            LocalDate endMonth = end.withDayOfMonth(1);
            while (!current.isAfter(endMonth)) {
                periods.add(current.format(MONTH_FORMAT));
                current = current.plusMonths(1);
            }
        }

        return periods;
    }

    // 生成月度周期列表
    private List<String> generateMonthPeriods(String startTime, String endTime) {
        List<String> months = new ArrayList<>();
        LocalDate start = LocalDate.parse(startTime.substring(0, 10));
        LocalDate end = LocalDate.parse(endTime.substring(0, 10));
        LocalDate current = start.withDayOfMonth(1);
        LocalDate endMonth = end.withDayOfMonth(1);

        while (!current.isAfter(endMonth)) {
            months.add(current.format(MONTH_FORMAT));
            current = current.plusMonths(1);
        }
        return months;
    }

    // 合并医生统计
    private void mergeDoctorCounts(Map<String, Long> doctorCounts, List<Map<String, Object>> data) {
        if (data == null) return;
        for (Map<String, Object> row : data) {
            String doctor = row.get("doctor") != null ? row.get("doctor").toString() : "未知";
            long count = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;
            doctorCounts.merge(doctor, count, Long::sum);
        }
    }

    // 合并科室统计
    private void mergeDeptCounts(Map<String, Map<String, Long>> deptData, String month, List<Map<String, Object>> data) {
        if (data == null) return;
        for (Map<String, Object> row : data) {
            String dept = row.get("dept") != null ? row.get("dept").toString() : "未知";
            long count = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;
            deptData.computeIfAbsent(dept, k -> new HashMap<>()).merge(month, count, Long::sum);
        }
    }

    // 获取类型标签
    private String getTypeLabel(String type) {
        switch (type) {
            case "ct": return "CT检查";
            case "mri": return "核磁检查";
            case "enteroscopy": return "肠镜检查";
            case "pathology": return "病理检查";
            default: return type;
        }
    }
}