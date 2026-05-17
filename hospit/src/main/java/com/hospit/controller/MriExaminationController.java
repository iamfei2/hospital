package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.ExaminationContext;
import com.hospit.entity.MriExamination;
import com.hospit.service.IMriExaminationService;
import com.hospit.service.IWarningEngineService;
import com.hospit.mapper.MriExaminationMapper;
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

import com.hospit.vo.MriExaminationExportVO;
import com.alibaba.excel.EasyExcel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mriExamination")
public class MriExaminationController {

    @Autowired
    private IMriExaminationService mriExaminationService;

    @Autowired
    private MriExaminationMapper mriExaminationMapper;

    @Autowired
    private IWarningEngineService warningEngineService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    // 获取核磁检查列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(mriExaminationService.list());
    }

    // 根据ID获取核磁检查详情
    @GetMapping("/{mriId}")
    public Result getById(@PathVariable Long mriId) {
        return Result.success(mriExaminationService.getById(mriId));
    }

    // 分页查询核磁检查
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            QueryWrapper<MriExamination> wrapper = new QueryWrapper<>();
            
            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = mri_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
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

            Page<MriExamination> page = new Page<>(pageNum, pageSize);
            Page<MriExamination> resultPage = mriExaminationMapper.selectPage(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 根据患者ID获取核磁检查列表
    @GetMapping("/byPatient/{patientId}")
    public Result getByPatient(@PathVariable String patientId) {
        return Result.success(mriExaminationService.lambdaQuery()
                .eq(com.hospit.entity.MriExamination::getPatientId, patientId)
                .orderByDesc(com.hospit.entity.MriExamination::getExaminationTime)
                .list());
    }

    @OperateLog(operationType = "新增", operatedTable = "mri_examination", description = "新增核磁检查")
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
                examinationNo = "MRI" + System.currentTimeMillis();
            }

            MriExamination mri = new MriExamination();
            mri.setPatientId(patientId.trim());
            mri.setExaminationNo(examinationNo);
            mri.setExaminationPart(examinationPart);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                mri.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            mri.setExamineDoctor(examineDoctor);
            mri.setExamineDept(examineDept);
            mri.setReportConclusion(reportConclusion);
            mri.setUserId(1);
            mri.setUploadTime(LocalDateTime.now());
            mri.setIsInvalid(false);
            mri.setCreateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String mriDir = uploadPath + File.separator + "mri";
                File dir = new File(mriDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = mriDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                mri.setReportUrl("/mriExamination/download/" + fileName);
            }

            mriExaminationService.save(mri);

            ExaminationContext context = new ExaminationContext();
            context.setPatientId(mri.getPatientId());
            context.setExaminationType("MRI");
            context.setExaminationId(mri.getMriId());
            context.setReportConclusion(mri.getReportConclusion());
            context.setReportTime(mri.getExaminationTime());
            context.setReportUrl(mri.getReportUrl());
            warningEngineService.evaluateExamination(context);

            return Result.success(mri, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "mri_examination", description = "修改核磁检查")
    @PostMapping("/update")
    public Result update(@RequestParam Long mriId,
                         @RequestParam(required = false) String patientId,
                         @RequestParam(required = false) String examinationNo,
                         @RequestParam(required = false) String examinationPart,
                         @RequestParam(required = false) String examinationTime,
                         @RequestParam(required = false) String examineDoctor,
                         @RequestParam(required = false) String examineDept,
                         @RequestParam(required = false) String reportConclusion,
                         @RequestParam(required = false) MultipartFile pdfFile) {
        try {
            MriExamination mri = mriExaminationService.getById(mriId);
            if (mri == null) {
                return Result.fail("记录不存在");
            }

            if (patientId != null) mri.setPatientId(patientId);
            if (examinationNo != null) mri.setExaminationNo(examinationNo);
            if (examinationPart != null) mri.setExaminationPart(examinationPart);
            if (examinationTime != null && !examinationTime.isEmpty()) {
                mri.setExaminationTime(LocalDateTime.parse(examinationTime.replace(" ", "T")));
            }
            if (examineDoctor != null) mri.setExamineDoctor(examineDoctor);
            if (examineDept != null) mri.setExamineDept(examineDept);
            if (reportConclusion != null) mri.setReportConclusion(reportConclusion);
            mri.setUpdateTime(LocalDateTime.now());

            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String mriDir = uploadPath + File.separator + "mri";
                File dir = new File(mriDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = mriDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                mri.setReportUrl("/mriExamination/download/" + fileName);
            }

            boolean success = mriExaminationService.updateById(mri);

            if (success) {
                ExaminationContext context = new ExaminationContext();
                context.setPatientId(mri.getPatientId());
                context.setExaminationType("MRI");
                context.setExaminationId(mri.getMriId());
                context.setReportConclusion(mri.getReportConclusion());
                context.setReportTime(mri.getExaminationTime());
                context.setReportUrl(mri.getReportUrl());
                warningEngineService.evaluateExamination(context);
            }

            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "mri_examination", description = "删除核磁检查")
    @DeleteMapping("/{mriId}")
    public Result delete(@PathVariable Long mriId) {
        try {
            boolean success = mriExaminationService.removeById(mriId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    // 下载核磁检查PDF
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = uploadPath + File.separator + "mri" + File.separator + fileName;
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

    // 导出核磁检查数据
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam queryPageParam, HttpServletResponse response) {
        try {
            HashMap param = queryPageParam.getParam();
            QueryWrapper<MriExamination> wrapper = new QueryWrapper<>();

            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM patient p WHERE p.patient_id = mri_examination.patient_id AND p.patient_name LIKE '%" + param.get("patientName") + "%')");
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

            List<MriExamination> list = mriExaminationMapper.selectList(wrapper);
            List<MriExaminationExportVO> exportList = list.stream().map(mri -> {
                MriExaminationExportVO vo = new MriExaminationExportVO();
                vo.setPatientId(mri.getPatientId());
                vo.setExaminationNo(mri.getExaminationNo());
                vo.setExaminationTime(mri.getExaminationTime());
                vo.setExaminationPart(mri.getExaminationPart());
                vo.setExamineDoctor(mri.getExamineDoctor());
                vo.setExamineDept(mri.getExamineDept());
                vo.setReportConclusion(mri.getReportConclusion());
                vo.setUploadTime(mri.getUploadTime());
                return vo;
            }).collect(Collectors.toList());

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("核磁检查数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), MriExaminationExportVO.class).sheet("核磁检查数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
