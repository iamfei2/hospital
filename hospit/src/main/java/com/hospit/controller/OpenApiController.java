package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.common.Result;
import com.hospit.entity.*;
import com.hospit.service.*;
import com.hospit.util.DesensitizationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/open/api/v1")
@Tag(name = "对外数据服务", description = "医院管理系统对外数据服务API")
public class OpenApiController {

    @Autowired
    private IPatientService patientService;

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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "获取患者检查数据", description = "根据患者ID获取该患者的所有检查数据（CT、MRI、肠镜、病理、检验等）")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "患者不存在")
    })
    @GetMapping("/patient/{patientId}/examinations")
    public Result getPatientExaminations(
            @Parameter(description = "患者ID", example = "4451355")
            @PathVariable String patientId,
            @Parameter(description = "检查类型筛选", example = "ct", schema = @Schema(allowableValues = {"ct", "mri", "pathology", "enteroscopy", "lab"}))
            @RequestParam(required = false) String type,
            @Parameter(description = "开始时间", example = "2026-01-01")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2026-12-31")
            @RequestParam(required = false) String endTime) {
        Map<String, Object> result = new HashMap<>();

        Patient patient = patientService.getById(patientId);
        if (patient == null) {
            return Result.fail("患者不存在");
        }
        result.put("patient", desensitizePatient(patient));

        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime + " 00:00:00", FORMATTER) : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime + " 23:59:59", FORMATTER) : LocalDateTime.of(2099, 12, 31, 23, 59);

        if (type == null || "ct".equals(type)) {
            List<CtExamination> ctList = ctExaminationService.lambdaQuery()
                    .eq(CtExamination::getPatientId, patientId)
                    .ge(CtExamination::getExaminationTime, start)
                    .le(CtExamination::getExaminationTime, end)
                    .eq(CtExamination::getIsInvalid, false)
                    .orderByDesc(CtExamination::getExaminationTime)
                    .list();
            result.put("ctExaminations", ctList);
        }

        if (type == null || "mri".equals(type)) {
            List<MriExamination> mriList = mriExaminationService.lambdaQuery()
                    .eq(MriExamination::getPatientId, patientId)
                    .ge(MriExamination::getExaminationTime, start)
                    .le(MriExamination::getExaminationTime, end)
                    .eq(MriExamination::getIsInvalid, false)
                    .orderByDesc(MriExamination::getExaminationTime)
                    .list();
            result.put("mriExaminations", mriList);
        }

        if (type == null || "enteroscopy".equals(type)) {
            List<EnteroscopyExamination> entList = enteroscopyExaminationService.lambdaQuery()
                    .eq(EnteroscopyExamination::getPatientId, patientId)
                    .ge(EnteroscopyExamination::getExaminationTime, start)
                    .le(EnteroscopyExamination::getExaminationTime, end)
                    .eq(EnteroscopyExamination::getIsInvalid, false)
                    .orderByDesc(EnteroscopyExamination::getExaminationTime)
                    .list();
            result.put("enteroscopyExaminations", entList);
        }

        if (type == null || "pathology".equals(type)) {
            List<PathologyExamination> pathList = pathologyExaminationService.lambdaQuery()
                    .eq(PathologyExamination::getPatientId, patientId)
                    .ge(PathologyExamination::getSamplingTime, start)
                    .le(PathologyExamination::getSamplingTime, end)
                    .eq(PathologyExamination::getIsInvalid, false)
                    .orderByDesc(PathologyExamination::getSamplingTime)
                    .list();
            result.put("pathologyExaminations", pathList);
        }

        if (type == null || "lab".equals(type)) {
            List<LabResult> labList = labResultService.lambdaQuery()
                    .eq(LabResult::getPatientId, patientId)
                    .ge(LabResult::getReportTime, start)
                    .le(LabResult::getReportTime, end)
                    .eq(LabResult::getIsInvalid, false)
                    .orderByDesc(LabResult::getReportTime)
                    .list();
            result.put("labResults", labList);
        }

        return Result.success(result);
    }

    @Operation(summary = "查询患者列表", description = "分页查询患者列表，数据已脱敏处理")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/patients")
    public Result getPatients(
            @Parameter(description = "患者姓名（模糊查询）", example = "张")
            @RequestParam(required = false) String patientName,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "50")
            @RequestParam(defaultValue = "50") Integer size) {
        try {
            QueryWrapper<Patient> wrapper = new QueryWrapper<>();
            if (patientName != null && !patientName.isEmpty()) {
                wrapper.like("patient_name", patientName);
            }
            wrapper.eq("is_invalid", false).orderByDesc("create_time");

            Page<Patient> pageObj = new Page<>(page, size);
            Page<Patient> resultPage = patientService.page(pageObj, wrapper);

            List<Map<String, Object>> desensitizedRecords = resultPage.getRecords().stream()
                    .map(this::desensitizePatient)
                    .collect(Collectors.toList());

            return Result.success(desensitizedRecords, resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 患者数据脱敏处理
    private Map<String, Object> desensitizePatient(Patient p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("patientId", DesensitizationUtil.maskPatientId(p.getPatientId()));
        map.put("patientName", DesensitizationUtil.maskName(p.getPatientName()));
        map.put("gender", p.getGender());
        map.put("age", p.getAge());
        map.put("phone", DesensitizationUtil.maskPhone(p.getPhone()));
        map.put("idCard", DesensitizationUtil.maskIdCard(p.getIdCard()));
        map.put("createTime", p.getCreateTime());
        return map;
    }
}