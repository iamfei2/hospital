package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.EnteroscopyExamination;
import com.hospit.entity.ExaminationContext;
import com.hospit.service.IEnteroscopyExaminationService;
import com.hospit.service.IWarningEngineService;
import com.hospit.mapper.EnteroscopyExaminationMapper;
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

import com.hospit.vo.EnteroscopyExaminationExportVO;
import com.alibaba.excel.EasyExcel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enteroscopyExamination")
public class EnteroscopyExaminationController {

    @Autowired
    private IEnteroscopyExaminationService enteroscopyExaminationService;

    @Autowired
    private EnteroscopyExaminationMapper enteroscopyExaminationMapper;

    @Autowired
    private IWarningEngineService warningEngineService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    // 获取肠镜检查列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(enteroscopyExaminationService.list());
    }

    // 根据ID获取肠镜检查详情
    @GetMapping("/{enteroscopyId}")
    public Result getById(@PathVariable Long enteroscopyId) {
        return Result.success(enteroscopyExaminationService.getById(enteroscopyId));
    }

    // 分页查询肠镜检查
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            QueryWrapper<EnteroscopyExamination> wrapper = new QueryWrapper<>();
            
            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = enteroscopy_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("examinationNo") != null && !param.get("examinationNo").toString().isEmpty()) {
                    wrapper.like("examination_no", param.get("examinationNo"));
                }
                if (param.get("enteroscopyType") != null && !param.get("enteroscopyType").toString().isEmpty()) {
                    wrapper.like("enteroscopy_type", param.get("enteroscopyType"));
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

            Page<EnteroscopyExamination> page = new Page<>(pageNum, pageSize);
            Page<EnteroscopyExamination> resultPage = enteroscopyExaminationMapper.selectPage(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 根据患者ID获取肠镜检查列表
    @GetMapping("/byPatient/{patientId}")
    public Result getByPatient(@PathVariable String patientId) {
        return Result.success(enteroscopyExaminationService.lambdaQuery()
                .eq(com.hospit.entity.EnteroscopyExamination::getPatientId, patientId)
                .orderByDesc(com.hospit.entity.EnteroscopyExamination::getExaminationTime)
                .list());
    }

    @OperateLog(operationType = "新增", operatedTable = "enteroscopy_examination", description = "新增肠镜检查")
    @PostMapping("/add")
    public Result add(@RequestParam String patientId,
                      @RequestParam(required = false) String examinationNo,
                      @RequestParam(required = false) String enteroscopyType,
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
                examinationNo = "ENT" + System.currentTimeMillis();
            }

            EnteroscopyExamination enteroscopy = new EnteroscopyExamination();
            enteroscopy.setPatientId(patientId.trim());
            enteroscopy.setExaminationNo(examinationNo);
            enteroscopy.setEnteroscopyType(enteroscopyType);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                enteroscopy.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            enteroscopy.setExamineDoctor(examineDoctor);
            enteroscopy.setExamineDept(examineDept);
            enteroscopy.setReportConclusion(reportConclusion);
            enteroscopy.setUserId(1);
            enteroscopy.setUploadTime(LocalDateTime.now());
            enteroscopy.setIsInvalid(false);
            enteroscopy.setCreateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String enteroscopyDir = uploadPath + File.separator + "enteroscopy";
                File dir = new File(enteroscopyDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = enteroscopyDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                enteroscopy.setReportUrl("/enteroscopyExamination/download/" + fileName);
            }

            enteroscopyExaminationService.save(enteroscopy);

            ExaminationContext context = new ExaminationContext();
            context.setPatientId(enteroscopy.getPatientId());
            context.setExaminationType("ENTEROSCOPY");
            context.setExaminationId(enteroscopy.getEnteroscopyId());
            context.setReportConclusion(enteroscopy.getReportConclusion());
            context.setReportTime(enteroscopy.getExaminationTime());
            context.setReportUrl(enteroscopy.getReportUrl());
            warningEngineService.evaluateExamination(context);

            return Result.success(enteroscopy, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "enteroscopy_examination", description = "修改肠镜检查")
    @PostMapping("/update")
    public Result update(@RequestParam Long enteroscopyId,
                         @RequestParam(required = false) String patientId,
                         @RequestParam(required = false) String examinationNo,
                         @RequestParam(required = false) String enteroscopyType,
                         @RequestParam(required = false) String examinationTime,
                         @RequestParam(required = false) String examineDoctor,
                         @RequestParam(required = false) String examineDept,
                         @RequestParam(required = false) String reportConclusion,
                         @RequestParam(required = false) MultipartFile pdfFile) {
        try {
            EnteroscopyExamination enteroscopy = enteroscopyExaminationService.getById(enteroscopyId);
            if (enteroscopy == null) {
                return Result.fail("记录不存在");
            }

            if (patientId != null) enteroscopy.setPatientId(patientId);
            if (examinationNo != null) enteroscopy.setExaminationNo(examinationNo);
            if (enteroscopyType != null) enteroscopy.setEnteroscopyType(enteroscopyType);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                enteroscopy.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            if (examineDoctor != null) enteroscopy.setExamineDoctor(examineDoctor);
            if (examineDept != null) enteroscopy.setExamineDept(examineDept);
            if (reportConclusion != null) enteroscopy.setReportConclusion(reportConclusion);
            enteroscopy.setUpdateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String enteroscopyDir = uploadPath + File.separator + "enteroscopy";
                File dir = new File(enteroscopyDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = enteroscopyDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                enteroscopy.setReportUrl("/enteroscopyExamination/download/" + fileName);
            }

            boolean success = enteroscopyExaminationService.updateById(enteroscopy);

            if (success) {
                ExaminationContext context = new ExaminationContext();
                context.setPatientId(enteroscopy.getPatientId());
                context.setExaminationType("ENTEROSCOPY");
                context.setExaminationId(enteroscopy.getEnteroscopyId());
                context.setReportConclusion(enteroscopy.getReportConclusion());
                context.setReportTime(enteroscopy.getExaminationTime());
                context.setReportUrl(enteroscopy.getReportUrl());
                warningEngineService.evaluateExamination(context);
            }

            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "enteroscopy_examination", description = "删除肠镜检查")
    @DeleteMapping("/{enteroscopyId}")
    public Result delete(@PathVariable Long enteroscopyId) {
        try {
            boolean success = enteroscopyExaminationService.removeById(enteroscopyId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    // 下载肠镜检查PDF
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = uploadPath + File.separator + "enteroscopy" + File.separator + fileName;
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

    // 导出肠镜检查数据
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam queryPageParam, HttpServletResponse response) {
        try {
            HashMap param = queryPageParam.getParam();
            QueryWrapper<EnteroscopyExamination> wrapper = new QueryWrapper<>();

            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = enteroscopy_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
                }
                if (param.get("examinationNo") != null && !param.get("examinationNo").toString().isEmpty()) {
                    wrapper.like("examination_no", param.get("examinationNo"));
                }
                if (param.get("enteroscopyType") != null && !param.get("enteroscopyType").toString().isEmpty()) {
                    wrapper.like("enteroscopy_type", param.get("enteroscopyType"));
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

            List<EnteroscopyExamination> list = enteroscopyExaminationMapper.selectList(wrapper);
            List<EnteroscopyExaminationExportVO> exportList = list.stream().map(ent -> {
                EnteroscopyExaminationExportVO vo = new EnteroscopyExaminationExportVO();
                vo.setPatientId(ent.getPatientId());
                vo.setExaminationNo(ent.getExaminationNo());
                vo.setExaminationTime(ent.getExaminationTime());
                vo.setEnteroscopyType(ent.getEnteroscopyType());
                vo.setExamineDoctor(ent.getExamineDoctor());
                vo.setExamineDept(ent.getExamineDept());
                vo.setReportConclusion(ent.getReportConclusion());
                vo.setUploadTime(ent.getUploadTime());
                return vo;
            }).collect(Collectors.toList());

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("肠镜检查数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), EnteroscopyExaminationExportVO.class).sheet("肠镜检查数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
