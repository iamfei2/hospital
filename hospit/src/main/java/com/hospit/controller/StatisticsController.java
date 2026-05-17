package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospit.common.Result;
import com.hospit.entity.*;
import com.hospit.service.*;
import com.hospit.statistics.AggregationEngine;
import com.hospit.vo.DimensionStatRequest;
import com.hospit.vo.DimensionStatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.hospit.vo.StatisticsCountExportVO;
import com.alibaba.excel.EasyExcel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Calendar;
import java.util.stream.Collectors;
import java.util.Comparator;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private ICtExaminationService ctExaminationService;

    @Autowired
    private IMriExaminationService mriExaminationService;

    @Autowired
    private IEnteroscopyExaminationService enteroscopyExaminationService;

    @Autowired
    private IPathologyExaminationService pathologyExaminationService;

    @Autowired
    private ILabResultService labResultService;

    @Autowired
    private ILabItemDictService labItemDictService;

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private AggregationEngine aggregationEngine;

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    // 统计检查数量
    @PostMapping("/count")
    public Result count(@RequestBody Map<String, Object> params) {
        try {
            String startTimeStr = (String) params.get("startTime");
            String endTimeStr = (String) params.get("endTime");
            List<String> types = (List<String>) params.get("types");
            String periodType = params.get("periodType") != null ? (String) params.get("periodType") : "day";

            if (startTimeStr == null || endTimeStr == null) {
                return Result.fail("请选择时间范围");
            }

            String startTime = startTimeStr + " 00:00:00";
            String endTime = endTimeStr + " 23:59:59";

            if (types == null || types.isEmpty()) {
                types = Arrays.asList("ct", "mri", "enteroscopy", "pathology");
            }

            if ("day".equals(periodType) || "week".equals(periodType) || "month".equals(periodType)) {
                Map<String, Object> result = statisticsService.getCountByTypes(types, startTime, endTime, periodType);
                return Result.success(result);
            } else {
                long total = 0;
                List<String> labels = new ArrayList<>();
                List<Long> values = new ArrayList<>();

                for (String type : types) {
                    long count = 0;
                    String label = getTypeLabel(type);

                    switch (type) {
                        case "ct":
                            count = ctExaminationService.lambdaQuery()
                                    .ge(CtExamination::getUploadTime, LocalDateTime.parse(startTime))
                                    .le(CtExamination::getUploadTime, LocalDateTime.parse(endTime))
                                    .count();
                            break;
                        case "mri":
                            count = mriExaminationService.lambdaQuery()
                                    .ge(MriExamination::getUploadTime, LocalDateTime.parse(startTime))
                                    .le(MriExamination::getUploadTime, LocalDateTime.parse(endTime))
                                    .count();
                            break;
                        case "enteroscopy":
                            count = enteroscopyExaminationService.lambdaQuery()
                                    .ge(EnteroscopyExamination::getUploadTime, LocalDateTime.parse(startTime))
                                    .le(EnteroscopyExamination::getUploadTime, LocalDateTime.parse(endTime))
                                    .count();
                            break;
                        case "pathology":
                            count = pathologyExaminationService.lambdaQuery()
                                    .ge(PathologyExamination::getUploadTime, LocalDateTime.parse(startTime))
                                    .le(PathologyExamination::getUploadTime, LocalDateTime.parse(endTime))
                                    .count();
                            break;
                    }

                    labels.add(label);
                    values.add(count);
                    total += count;
                }

                Map<String, Object> result = new HashMap<>();
                result.put("labels", labels);
                result.put("values", values);
                result.put("total", total);
                return Result.success(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计失败");
        }
    }

    // 获取检查类型标签
    private String getTypeLabel(String type) {
        switch (type) {
            case "ct": return "CT检查";
            case "mri": return "核磁检查";
            case "enteroscopy": return "肠镜检查";
            case "pathology": return "病理检查";
            default: return type;
        }
    }

    // 获取患者趋势数据
    @GetMapping("/patientTrend")
    public Result patientTrend(@RequestParam String patientId, @RequestParam Integer itemId) {
        try {
            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId)
                    .eq("item_id", itemId)
                    .eq("is_invalid", false)
                    .orderByAsc("report_time");
            
            List<LabResult> results = labResultService.list(wrapper);
            
            List<String> dates = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            
            LabItemDict item = labItemDictService.getById(itemId);
            String normalRange = item != null ? item.getNormalRange() : "";
            
            for (LabResult r : results) {
                dates.add(r.getReportTime().format(MONTH_FORMATTER));
                values.add(r.getResultValue());
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("dates", dates);
            data.put("values", values);
            data.put("itemName", item != null ? item.getItemName() : "");
            data.put("normalRange", normalRange);
            
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取趋势数据失败");
        }
    }

    // 按科室统计月度数据
    @GetMapping("/monthlyByDept")
    public Result monthlyByDept(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            String startTime = startDate + "-01 00:00:00";
            String endTime = endDate + "-31 23:59:59";
            Map<String, Object> result = statisticsService.getMonthlyByDept(startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计失败");
        }
    }

    // 统计医生工作量
    @GetMapping("/doctorWorkload")
    public Result doctorWorkload(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            String startTime = startDate + "-01 00:00:00";
            String endTime = endDate + "-31 23:59:59";
            Map<String, Object> result = statisticsService.getDoctorWorkload(startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计失败");
        }
    }

    // 统计检查类型占比
    @GetMapping("/typeRatio")
    public Result typeRatio(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            String startTime = startDate + "-01 00:00:00";
            String endTime = endDate + "-31 23:59:59";
            Map<String, Object> result = statisticsService.getTypeRatio(startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计失败");
        }
    }

    // 动态统计
    @PostMapping("/dynamic")
    public Result dynamicStat(@RequestBody DimensionStatRequest request) {
        try {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.fail("请选择时间范围");
            }
            String startTime = request.getStartTime();
            String endTime = request.getEndTime();
            
            if (!startTime.contains(" ")) {
                startTime = startTime + " 00:00:00";
            }
            if (!endTime.contains(" ")) {
                endTime = endTime + " 23:59:59";
            }
            
            request.setStartTime(startTime);
            request.setEndTime(endTime);

            DimensionStatResponse result = aggregationEngine.aggregate(request);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("动态统计失败: " + e.getMessage());
        }
    }

    // 计算统计数据
    @PostMapping("/compute")
    public Result computeStatistics() {
        try {
            statisticsComputeService.computeAllStatistics();
            return Result.success(null, "统计计算完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计计算失败: " + e.getMessage());
        }
    }

    // 导出统计数据
    @PostMapping("/exportCount")
    public void exportCount(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        try {
            String startTimeStr = (String) params.get("startTime");
            String endTimeStr = (String) params.get("endTime");
            List<String> types = (List<String>) params.get("types");

            if (startTimeStr == null || endTimeStr == null) {
                return;
            }

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr + " 00:00:00", FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr + " 23:59:59", FORMATTER);

            if (types == null || types.isEmpty()) {
                types = Arrays.asList("ct", "mri", "enteroscopy", "pathology");
            }

            List<StatisticsCountExportVO> exportList = new java.util.ArrayList<>();
            for (String type : types) {
                long count = 0;
                String label = "";
                switch (type) {
                    case "ct":
                        count = ctExaminationService.lambdaQuery()
                                .ge(CtExamination::getUploadTime, startTime)
                                .le(CtExamination::getUploadTime, endTime)
                                .count();
                        label = "CT检查";
                        break;
                    case "mri":
                        count = mriExaminationService.lambdaQuery()
                                .ge(MriExamination::getUploadTime, startTime)
                                .le(MriExamination::getUploadTime, endTime)
                                .count();
                        label = "核磁检查";
                        break;
                    case "enteroscopy":
                        count = enteroscopyExaminationService.lambdaQuery()
                                .ge(EnteroscopyExamination::getUploadTime, startTime)
                                .le(EnteroscopyExamination::getUploadTime, endTime)
                                .count();
                        label = "肠镜检查";
                        break;
                    case "pathology":
                        count = pathologyExaminationService.lambdaQuery()
                                .ge(PathologyExamination::getUploadTime, startTime)
                                .le(PathologyExamination::getUploadTime, endTime)
                                .count();
                        label = "病理检查";
                        break;
                }
                StatisticsCountExportVO vo = new StatisticsCountExportVO();
                vo.setTypeName(label);
                vo.setCount(count);
                exportList.add(vo);
            }

            StatisticsCountExportVO totalVO = new StatisticsCountExportVO();
            totalVO.setTypeName("合计");
            totalVO.setCount(exportList.stream().mapToLong(StatisticsCountExportVO::getCount).sum());
            exportList.add(totalVO);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("检查统计数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), StatisticsCountExportVO.class).sheet("检查统计").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 导出患者趋势数据
    @GetMapping("/exportPatientTrend")
    public void exportPatientTrend(@RequestParam String patientId, @RequestParam Integer itemId, HttpServletResponse response) {
        try {
            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();
            wrapper.eq("patient_id", patientId)
                    .eq("item_id", itemId)
                    .eq("is_invalid", false)
                    .orderByAsc("report_time");

            List<LabResult> results = labResultService.list(wrapper);
            LabItemDict item = labItemDictService.getById(itemId);
            String itemName = item != null ? item.getItemName() : "";

            List<Map<String, Object>> exportList = new java.util.ArrayList<>();
            for (LabResult r : results) {
                Map<String, Object> row = new java.util.HashMap<>();
                row.put("reportTime", r.getReportTime());
                row.put("resultValue", r.getResultValue());
                row.put("unit", r.getResultUnit());
                exportList.add(row);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(itemName + "_患者趋势数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream()).head(buildPatientTrendHead()).sheet("患者趋势数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 构建患者趋势表头
    private List<List<String>> buildPatientTrendHead() {
        List<List<String>> head = new java.util.ArrayList<>();
        head.add(java.util.Collections.singletonList("报告时间"));
        head.add(java.util.Collections.singletonList("检验值"));
        head.add(java.util.Collections.singletonList("单位"));
        return head;
    }
}