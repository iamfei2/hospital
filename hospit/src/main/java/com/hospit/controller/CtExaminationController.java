package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.CtExamination;
import com.hospit.entity.ExaminationContext;
import com.hospit.service.ICtExaminationService;
import com.hospit.service.IWarningEngineService;
import com.hospit.mapper.CtExaminationMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.hospit.vo.CtExaminationExportVO;
import com.alibaba.excel.EasyExcel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ctExamination")
public class CtExaminationController {

    @Autowired
    private ICtExaminationService ctExaminationService;

    @Autowired
    private CtExaminationMapper ctExaminationMapper;

    @Autowired
    private IWarningEngineService warningEngineService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    // 获取CT检查列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(ctExaminationService.list());
    }

    // 根据ID获取CT检查详情
    @GetMapping("/{ctId}")
    public Result getById(@PathVariable Long ctId) {
        return Result.success(ctExaminationService.getById(ctId));
    }

    // 分页查询CT检查
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            QueryWrapper<CtExamination> wrapper = new QueryWrapper<>();
            
            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = ct_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("examinationNo") != null && !param.get("examinationNo").toString().isEmpty()) {
                    wrapper.like("examination_no", param.get("examinationNo"));
                }
                if (param.get("examinationPart") != null && !param.get("examinationPart").toString().isEmpty()) {
                    wrapper.like("examination_part", param.get("examinationPart"));
                }
                if (param.get("examineDoctor") != null && !param.get("examineDoctor").toString().isEmpty()) {
                    wrapper.like("examine_doctor", param.get("examineDoctor"));
                }
                if (param.get("examineDept") != null && !param.get("examineDept").toString().isEmpty()) {
                    wrapper.eq("examine_dept", param.get("examineDept"));
                }
                if (param.get("reportConclusion") != null && !param.get("reportConclusion").toString().isEmpty()) {
                    wrapper.like("report_conclusion", param.get("reportConclusion"));
                }
                if (param.get("startTime") != null && !param.get("startTime").toString().isEmpty()
                        && param.get("endTime") != null && !param.get("endTime").toString().isEmpty()) {
                    wrapper.between("examination_time", param.get("startTime"), param.get("endTime"));
                }
            }
            
            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("examination_time");

            Page<CtExamination> page = new Page<>(pageNum, pageSize);
            Page<CtExamination> resultPage = ctExaminationMapper.selectPage(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 根据患者ID获取CT检查列表
    @GetMapping("/byPatient/{patientId}")
    public Result getByPatient(@PathVariable String patientId) {
        return Result.success(ctExaminationService.lambdaQuery()
                .eq(com.hospit.entity.CtExamination::getPatientId, patientId)
                .orderByDesc(com.hospit.entity.CtExamination::getExaminationTime)
                .list());
    }

    /**
     * 新增CT检查（支持所有字段+PDF上传）
     */
    @OperateLog(operationType = "新增", operatedTable = "ct_examination", description = "新增CT检查")
    @PostMapping("/add")
    public Result add(@RequestParam String patientId,
                      @RequestParam(required = false) String examinationNo,
                      @RequestParam(required = false) String examinationPart,
                      @RequestParam(required = false) String examinationTime,
                      @RequestParam(required = false) String examineDoctor,
                      @RequestParam(required = false) String examineDept,
                      @RequestParam(required = false) String reportConclusion,
                      @RequestParam(required = false) MultipartFile pdfFile,
                      HttpServletRequest request) {
        try {
            if (patientId == null || patientId.trim().isEmpty()) {
                return Result.fail("请输入患者ID");
            }

            // 如果执行医生为空，默认使用当前用户
            if (examineDoctor == null || examineDoctor.trim().isEmpty()) {
                String loginAccount = (String) request.getAttribute("loginAccount");
                if (loginAccount != null) {
                    examineDoctor = loginAccount;
                }
            }

            // 如果检查编号为空，自动生成唯一编号
            if (examinationNo == null || examinationNo.trim().isEmpty()) {
                examinationNo = "CT" + System.currentTimeMillis();
            }

            CtExamination ct = new CtExamination();
            ct.setPatientId(patientId.trim());
            ct.setExaminationNo(examinationNo);
            ct.setExaminationPart(examinationPart);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                ct.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            ct.setExamineDoctor(examineDoctor);
            ct.setExamineDept(examineDept);
            ct.setReportConclusion(reportConclusion);
            ct.setUserId(1);
            ct.setUploadTime(LocalDateTime.now());
            ct.setIsInvalid(false);
            ct.setCreateTime(LocalDateTime.now());

            // 处理PDF上传
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String ctDir = uploadPath + File.separator + "ct";
                File dir = new File(ctDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = ctDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                ct.setReportUrl("/ctExamination/download/" + fileName);
            }

            ctExaminationService.save(ct);

            ExaminationContext context = new ExaminationContext();
            context.setPatientId(ct.getPatientId());
            context.setExaminationType("CT");
            context.setExaminationId(ct.getCtId());
            context.setReportConclusion(ct.getReportConclusion());
            context.setReportTime(ct.getExaminationTime());
            context.setReportUrl(ct.getReportUrl());
            warningEngineService.evaluateExamination(context);

            return Result.success(ct, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    /**
     * 修改CT检查（支持PDF重新上传）
     */
    @OperateLog(operationType = "修改", operatedTable = "ct_examination", description = "修改CT检查")
    @PostMapping("/update")
    public Result update(@RequestParam Long ctId,
                         @RequestParam(required = false) String patientId,
                         @RequestParam(required = false) String examinationNo,
                         @RequestParam(required = false) String examinationPart,
                         @RequestParam(required = false) String examinationTime,
                         @RequestParam(required = false) String examineDoctor,
                         @RequestParam(required = false) String examineDept,
                         @RequestParam(required = false) String reportConclusion,
                         @RequestParam(required = false) MultipartFile pdfFile) {
        try {
            CtExamination ct = ctExaminationService.getById(ctId);
            if (ct == null) {
                return Result.fail("记录不存在");
            }

            if (patientId != null) ct.setPatientId(patientId);
            if (examinationNo != null) ct.setExaminationNo(examinationNo);
            if (examinationPart != null) ct.setExaminationPart(examinationPart);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                ct.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            if (examineDoctor != null) ct.setExamineDoctor(examineDoctor);
            if (examineDept != null) ct.setExamineDept(examineDept);
            if (reportConclusion != null) ct.setReportConclusion(reportConclusion);
            ct.setUpdateTime(LocalDateTime.now());

            // 处理PDF重新上传
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String ctDir = uploadPath + File.separator + "ct";
                File dir = new File(ctDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = ctDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                ct.setReportUrl("/ctExamination/download/" + fileName);
            }

            boolean success = ctExaminationService.updateById(ct);

            if (success) {
                ExaminationContext context = new ExaminationContext();
                context.setPatientId(ct.getPatientId());
                context.setExaminationType("CT");
                context.setExaminationId(ct.getCtId());
                context.setReportConclusion(ct.getReportConclusion());
                context.setReportTime(ct.getExaminationTime());
                context.setReportUrl(ct.getReportUrl());
                warningEngineService.evaluateExamination(context);
            }

            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除CT检查
     */
    @OperateLog(operationType = "删除", operatedTable = "ct_examination", description = "删除CT检查")
    @DeleteMapping("/{ctId}")
    public Result delete(@PathVariable Long ctId) {
        try {
            boolean success = ctExaminationService.removeById(ctId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * PDF预览/下载
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = uploadPath + File.separator + "ct" + File.separator + fileName;
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

    // 导出CT检查数据
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam queryPageParam, HttpServletResponse response) {
        try {
            HashMap param = queryPageParam.getParam();
            QueryWrapper<CtExamination> wrapper = new QueryWrapper<>();

            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = ct_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("examinationNo") != null && !param.get("examinationNo").toString().isEmpty()) {
                    wrapper.like("examination_no", param.get("examinationNo"));
                }
                if (param.get("examinationPart") != null && !param.get("examinationPart").toString().isEmpty()) {
                    wrapper.like("examination_part", param.get("examinationPart"));
                }
                if (param.get("examineDoctor") != null && !param.get("examineDoctor").toString().isEmpty()) {
                    wrapper.like("examine_doctor", param.get("examineDoctor"));
                }
                if (param.get("examineDept") != null && !param.get("examineDept").toString().isEmpty()) {
                    wrapper.eq("examine_dept", param.get("examineDept"));
                }
                if (param.get("reportConclusion") != null && !param.get("reportConclusion").toString().isEmpty()) {
                    wrapper.like("report_conclusion", param.get("reportConclusion"));
                }
                if (param.get("startTime") != null && !param.get("startTime").toString().isEmpty()
                        && param.get("endTime") != null && !param.get("endTime").toString().isEmpty()) {
                    wrapper.between("examination_time", param.get("startTime"), param.get("endTime"));
                }
            }

            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("examination_time");

            List<CtExamination> list = ctExaminationMapper.selectList(wrapper);
            List<CtExaminationExportVO> exportList = list.stream().map(ct -> {
                CtExaminationExportVO vo = new CtExaminationExportVO();
                vo.setPatientId(ct.getPatientId());
                vo.setExaminationNo(ct.getExaminationNo());
                vo.setExaminationTime(ct.getExaminationTime());
                vo.setExaminationPart(ct.getExaminationPart());
                vo.setExamineDoctor(ct.getExamineDoctor());
                vo.setExamineDept(ct.getExamineDept());
                vo.setReportConclusion(ct.getReportConclusion());
                vo.setUploadTime(ct.getUploadTime());
                return vo;
            }).collect(Collectors.toList());

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("CT检查数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), CtExaminationExportVO.class).sheet("CT检查数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
