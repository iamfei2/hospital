package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.ExaminationContext;
import com.hospit.entity.PathologyExamination;
import com.hospit.service.IPathologyExaminationService;
import com.hospit.service.IWarningEngineService;
import com.hospit.mapper.PathologyExaminationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.hospit.vo.PathologyExaminationExportVO;
import com.alibaba.excel.EasyExcel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pathologyExamination")
public class PathologyExaminationController {

    @Autowired
    private IPathologyExaminationService pathologyExaminationService;

    @Autowired
    private PathologyExaminationMapper pathologyExaminationMapper;

    @Autowired
    private IWarningEngineService warningEngineService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    // 获取病理检查列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(pathologyExaminationService.list());
    }

    // 根据ID获取病理检查详情
    @GetMapping("/{pathologyId}")
    public Result getById(@PathVariable Long pathologyId) {
        return Result.success(pathologyExaminationService.getById(pathologyId));
    }

    // 分页查询病理检查
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            QueryWrapper<PathologyExamination> wrapper = new QueryWrapper<>();
            
            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = pathology_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("pathologyNo") != null && !param.get("pathologyNo").toString().isEmpty()) {
                    wrapper.like("pathology_no", param.get("pathologyNo"));
                }
                if (param.get("specimenType") != null && !param.get("specimenType").toString().isEmpty()) {
                    wrapper.like("specimen_type", param.get("specimenType"));
                }
                if (param.get("pathologyDoctor") != null && !param.get("pathologyDoctor").toString().isEmpty()) {
                    wrapper.like("pathology_doctor", param.get("pathologyDoctor"));
                }
                if (param.get("pathologyDept") != null && !param.get("pathologyDept").toString().isEmpty()) {
                    wrapper.eq("pathology_dept", param.get("pathologyDept"));
                }
                if (param.get("pathologyDiagnosis") != null && !param.get("pathologyDiagnosis").toString().isEmpty()) {
                    wrapper.like("pathology_diagnosis", param.get("pathologyDiagnosis"));
                }
                if (param.get("startTime") != null && !param.get("startTime").toString().isEmpty()
                        && param.get("endTime") != null && !param.get("endTime").toString().isEmpty()) {
                    wrapper.between("sampling_time", param.get("startTime"), param.get("endTime"));
                }
            }
            
            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("sampling_time");

            Page<PathologyExamination> page = new Page<>(pageNum, pageSize);
            Page<PathologyExamination> resultPage = pathologyExaminationMapper.selectPage(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 根据患者ID获取病理检查列表
    @GetMapping("/byPatient/{patientId}")
    public Result getByPatient(@PathVariable String patientId) {
        return Result.success(pathologyExaminationService.lambdaQuery()
                .eq(com.hospit.entity.PathologyExamination::getPatientId, patientId)
                .orderByDesc(com.hospit.entity.PathologyExamination::getSamplingTime)
                .list());
    }

    @OperateLog(operationType = "新增", operatedTable = "pathology_examination", description = "新增病理检查")
    @PostMapping("/add")
    public Result add(@RequestParam String patientId,
                      @RequestParam(required = false) String pathologyNo,
                      @RequestParam(required = false) String specimenType,
                      @RequestParam(required = false) String samplingTime,
                      @RequestParam(required = false) String reportTime,
                      @RequestParam(required = false) String pathologyDoctor,
                      @RequestParam(required = false) String pathologyDept,
                      @RequestParam(required = false) String pathologyDiagnosis,
                      @RequestParam(required = false) MultipartFile pdfFile,
                      HttpServletRequest request) {
        try {
            if (patientId == null || patientId.trim().isEmpty()) {
                return Result.fail("请输入患者ID");
            }

            // 如果诊断医生为空，默认使用当前用户
            if (pathologyDoctor == null || pathologyDoctor.trim().isEmpty()) {
                String loginAccount = (String) request.getAttribute("loginAccount");
                if (loginAccount != null) {
                    pathologyDoctor = loginAccount;
                }
            }

            // 如果病理号为空，自动生成唯一编号
            if (pathologyNo == null || pathologyNo.trim().isEmpty()) {
                pathologyNo = "PATH" + System.currentTimeMillis();
            }

            PathologyExamination pathology = new PathologyExamination();
            pathology.setPatientId(patientId.trim());
            pathology.setPathologyNo(pathologyNo);
            pathology.setSpecimenType(specimenType);
            if (samplingTime != null && !samplingTime.isEmpty()) {
                pathology.setSamplingTime(LocalDateTime.parse(samplingTime.replace(" ", "T")));
            }
            if (reportTime != null && !reportTime.isEmpty()) {
                pathology.setReportTime(LocalDateTime.parse(reportTime.replace(" ", "T")));
            }
            pathology.setPathologyDoctor(pathologyDoctor);
            pathology.setPathologyDept(pathologyDept);
            pathology.setPathologyDiagnosis(pathologyDiagnosis);
            pathology.setUserId(1);
            pathology.setUploadTime(LocalDateTime.now());
            pathology.setIsInvalid(false);
            pathology.setCreateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String pathologyDir = uploadPath + File.separator + "pathology";
                File dir = new File(pathologyDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = pathologyDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                pathology.setReportUrl("/pathologyExamination/download/" + fileName);
            }

            pathologyExaminationService.save(pathology);

            ExaminationContext context = new ExaminationContext();
            context.setPatientId(pathology.getPatientId());
            context.setExaminationType("PATHOLOGY");
            context.setExaminationId(pathology.getPathologyId());
            context.setReportConclusion(pathology.getPathologyDiagnosis());
            context.setReportTime(pathology.getReportTime());
            context.setReportUrl(pathology.getReportUrl());
            warningEngineService.evaluateExamination(context);

            return Result.success(pathology, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "pathology_examination", description = "修改病理检查")
    @PostMapping("/update")
    public Result update(@RequestParam Long pathologyId,
                         @RequestParam(required = false) String patientId,
                         @RequestParam(required = false) String pathologyNo,
                         @RequestParam(required = false) String specimenType,
                         @RequestParam(required = false) String samplingTime,
                         @RequestParam(required = false) String reportTime,
                         @RequestParam(required = false) String pathologyDoctor,
                         @RequestParam(required = false) String pathologyDept,
                         @RequestParam(required = false) String pathologyDiagnosis,
                         @RequestParam(required = false) MultipartFile pdfFile) {
        try {
            PathologyExamination pathology = pathologyExaminationService.getById(pathologyId);
            if (pathology == null) {
                return Result.fail("记录不存在");
            }

            if (patientId != null) pathology.setPatientId(patientId);
            if (pathologyNo != null) pathology.setPathologyNo(pathologyNo);
            if (specimenType != null) pathology.setSpecimenType(specimenType);
            if (samplingTime != null && !samplingTime.isEmpty()) {
                pathology.setSamplingTime(LocalDateTime.parse(samplingTime.replace(" ", "T")));
            }
            if (reportTime != null && !reportTime.isEmpty()) {
                pathology.setReportTime(LocalDateTime.parse(reportTime.replace(" ", "T")));
            }
            if (pathologyDoctor != null) pathology.setPathologyDoctor(pathologyDoctor);
            if (pathologyDept != null) pathology.setPathologyDept(pathologyDept);
            if (pathologyDiagnosis != null) pathology.setPathologyDiagnosis(pathologyDiagnosis);
            pathology.setUpdateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String pathologyDir = uploadPath + File.separator + "pathology";
                File dir = new File(pathologyDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = pathologyDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                pathology.setReportUrl("/pathologyExamination/download/" + fileName);
            }

            boolean success = pathologyExaminationService.updateById(pathology);

            if (success) {
                ExaminationContext context = new ExaminationContext();
                context.setPatientId(pathology.getPatientId());
                context.setExaminationType("PATHOLOGY");
                context.setExaminationId(pathology.getPathologyId());
                context.setReportConclusion(pathology.getPathologyDiagnosis());
                context.setReportTime(pathology.getReportTime());
                context.setReportUrl(pathology.getReportUrl());
                warningEngineService.evaluateExamination(context);
            }

            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "pathology_examination", description = "删除病理检查")
    @DeleteMapping("/{pathologyId}")
    public Result delete(@PathVariable Long pathologyId) {
        try {
            boolean success = pathologyExaminationService.removeById(pathologyId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    // 下载病理检查PDF
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = uploadPath + File.separator + "pathology" + File.separator + fileName;
            File file = new File(filePath);

            if (!file.exists()) {
                response.setStatus(404);
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" + 
                URLEncoder.encode(fileName.substring(fileName.indexOf("_") + 1), "UTF-8"));

            try (InputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 导出病理检查数据
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam queryPageParam, HttpServletResponse response) {
        try {
            HashMap param = queryPageParam.getParam();
            QueryWrapper<PathologyExamination> wrapper = new QueryWrapper<>();

            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = pathology_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("pathologyNo") != null && !param.get("pathologyNo").toString().isEmpty()) {
                    wrapper.like("pathology_no", param.get("pathologyNo"));
                }
                if (param.get("specimenType") != null && !param.get("specimenType").toString().isEmpty()) {
                    wrapper.like("specimen_type", param.get("specimenType"));
                }
                if (param.get("pathologyDoctor") != null && !param.get("pathologyDoctor").toString().isEmpty()) {
                    wrapper.like("pathology_doctor", param.get("pathologyDoctor"));
                }
                if (param.get("pathologyDept") != null && !param.get("pathologyDept").toString().isEmpty()) {
                    wrapper.eq("pathology_dept", param.get("pathologyDept"));
                }
                if (param.get("pathologyDiagnosis") != null && !param.get("pathologyDiagnosis").toString().isEmpty()) {
                    wrapper.like("pathology_diagnosis", param.get("pathologyDiagnosis"));
                }
                if (param.get("startTime") != null && !param.get("startTime").toString().isEmpty()
                        && param.get("endTime") != null && !param.get("endTime").toString().isEmpty()) {
                    wrapper.between("sampling_time", param.get("startTime"), param.get("endTime"));
                }
            }

            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("sampling_time");

            List<PathologyExamination> list = pathologyExaminationMapper.selectList(wrapper);
            List<PathologyExaminationExportVO> exportList = list.stream().map(path -> {
                PathologyExaminationExportVO vo = new PathologyExaminationExportVO();
                vo.setPatientId(path.getPatientId());
                vo.setPathologyNo(path.getPathologyNo());
                vo.setSpecimenType(path.getSpecimenType());
                vo.setSamplingTime(path.getSamplingTime());
                vo.setReportTime(path.getReportTime());
                vo.setPathologyDoctor(path.getPathologyDoctor());
                vo.setPathologyDept(path.getPathologyDept());
                vo.setPathologyDiagnosis(path.getPathologyDiagnosis());
                vo.setUploadTime(path.getUploadTime());
                return vo;
            }).collect(Collectors.toList());

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("病理检查数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), PathologyExaminationExportVO.class).sheet("病理检查数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
