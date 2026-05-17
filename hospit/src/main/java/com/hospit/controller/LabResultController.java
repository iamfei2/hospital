package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.LabResult;
import com.hospit.service.ILabResultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hospit.vo.LabResultExportVO;
import com.hospit.vo.IsolationForestResultVO;
import com.hospit.entity.LabItemDict;
import com.hospit.service.ILabItemDictService;
import com.hospit.service.IStatisticsComputeService;
import com.hospit.entity.Patient;
import com.hospit.service.IPatientService;
import com.hospit.service.IWarningEngineService;
import com.alibaba.excel.EasyExcel;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/labResult")
public class LabResultController {
    @Autowired
    private ILabResultService labResultService;

    @Autowired
    private ILabItemDictService labItemDictService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private IWarningEngineService warningEngineService;

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    // 分页查询检验结果
    @PostMapping("/page")
    public Result getLabResultPage(@RequestBody QueryPageParam queryPageParam) {
        System.out.println(queryPageParam);
        try {
            return labResultService.getLabResultPage(queryPageParam);
        } catch (Exception e) {
            return Result.fail("查询检验结果失败");
        }
    }

    // 分页查询检验结果（游标分页）
    @PostMapping("/page/cursor")
    public Result getLabResultPageWithCursor(@RequestBody QueryPageParam queryPageParam,
                                             @RequestParam(required = false) String cursor) {
        try {
            return labResultService.getLabResultPageWithCursor(queryPageParam, cursor);
        } catch (Exception e) {
            return Result.fail("查询检验结果失败");
        }
    }

    // 获取检验结果详情
    @GetMapping("/detail/{resultId}")
    public Result getLabResultDetail(@PathVariable Long resultId) {
        try {
            return labResultService.getLabResultDetail(resultId);
        } catch (Exception e) {
            return Result.fail("获取检验结果详情失败");
        }
    }

    // 根据时间查询检验结果
    @PostMapping("/byTime")
    public Result getLabResultsByTime(@RequestBody QueryPageParam queryPageParam) {
        try {
            return labResultService.getLabResultsByTime(queryPageParam);
        } catch (Exception e) {
            return Result.fail("查询检验结果失败");
        }
    }

    // 根据患者ID获取检验结果列表
    @GetMapping("/byPatient/{patientId}")
    public Result getLabResultsByPatient(@PathVariable String patientId) {
        try {
            return labResultService.getLabResultsByPatient(patientId);
        } catch (Exception e) {
            return Result.fail("获取检验结果失败");
        }
    }

    /**
     * 批量新增检验结果（支持PDF上传）
     */
    @OperateLog(operationType = "新增", operatedTable = "lab_result", description = "新增检验结果")
    @PostMapping("/add")
    public Result add(@RequestParam String patientId,
                      @RequestParam String reportTime,
                      @RequestParam(required = false) String executeDept,
                      @RequestParam(required = false) String executeDoc,
                      @RequestParam String items,
                      @RequestParam(required = false) MultipartFile pdfFile,
                      HttpServletRequest request) {
        try {
            // 如果执行医生为空，默认使用当前用户
            if (executeDoc == null || executeDoc.trim().isEmpty()) {
                String loginAccount = (String) request.getAttribute("loginAccount");
                if (loginAccount != null) {
                    executeDoc = loginAccount;
                }
            }

            if (patientId == null || patientId.trim().isEmpty()) {
                return Result.fail("请输入患者ID");
            }
            if (reportTime == null || reportTime.trim().isEmpty()) {
                return Result.fail("请选择检验时间");
            }

            // 解析检验时间
            LocalDateTime reportTimeValue = LocalDateTime.parse(reportTime.replace(" ", "T"));

            // 处理PDF上传
            String reportUrl = null;
            System.out.println("PDF文件: " + (pdfFile != null ? pdfFile.getOriginalFilename() : "null"));
            System.out.println("PDF文件是否为空: " + (pdfFile != null ? pdfFile.isEmpty() : "null"));
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String originalFilename = pdfFile.getOriginalFilename();
                if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                    return Result.fail("只支持PDF格式文件");
                }

                String labDir = uploadPath + File.separator + "lab";
                File dir = new File(labDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String filePath = labDir + File.separator + fileName;
                pdfFile.transferTo(new File(filePath));
                reportUrl = "/labResult/download/" + fileName;
            }

            // 解析items JSON字符串
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> itemList = null;
            
            // 检查items是否为空
            if (items != null && !items.trim().isEmpty() && !"[]".equals(items.trim())) {
                itemList = mapper.readValue(items, List.class);
            }

            // 如果items为空，只上传PDF，不保存检验结果
            if (itemList == null || itemList.isEmpty()) {
                if (reportUrl == null) {
                    return Result.fail("请至少添加一个检验项目或上传PDF文件");
                }
                // 只上传PDF，保存一条记录
                LabResult labResult = new LabResult();
                labResult.setPatientId(patientId);
                labResult.setReportTime(reportTimeValue);
                labResult.setExecuteDept(executeDept);
                labResult.setExecuteDoc(executeDoc);
                labResult.setReportUrl(reportUrl);
                labResult.setIsInvalid(false);
                labResult.setCreateTime(LocalDateTime.now());
                labResultService.save(labResult);
                statisticsComputeService.updateStatisticsOnNewResult(labResult);
                return Result.success(null, "PDF上传成功");
            }

            // 批量保存检验结果
            for (Map<String, Object> item : itemList) {
                LabResult labResult = new LabResult();
                labResult.setPatientId(patientId);
                labResult.setReportTime(reportTimeValue);
                labResult.setExecuteDept(executeDept);
                labResult.setExecuteDoc(executeDoc);
                labResult.setItemId((Integer) item.get("itemId"));
                
                Object resultValue = item.get("resultValue");
                if (resultValue != null && !resultValue.toString().isEmpty()) {
                    labResult.setResultValue(new java.math.BigDecimal(resultValue.toString()));
                }
                
                labResult.setResultUnit((String) item.get("resultUnit"));
                labResult.setReportUrl(reportUrl);
                labResult.setIsInvalid(false);
                labResult.setCreateTime(LocalDateTime.now());
                
                labResultService.save(labResult);
                warningEngineService.evaluate(labResult);
                statisticsComputeService.updateStatisticsOnNewResult(labResult);
            }

            return Result.success(null, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败");
        }
    }

    /**
     * PDF预览/下载
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String filePath = uploadPath + File.separator + "lab" + File.separator + fileName;
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

    /**
     * 获取患者检验项目历史趋势
     */
    // 获取患者检验项目历史趋势
    @GetMapping("/trend")
    public Result getTrend(@RequestParam String patientId, @RequestParam Integer itemId) {
        try {
            return labResultService.getTrend(patientId, itemId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取趋势数据失败");
        }
    }

    // 导出检验结果数据
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam queryPageParam, HttpServletResponse response) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            HashMap param = queryPageParam.getParam();

            QueryWrapper<LabResult> wrapper = new QueryWrapper<>();

            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.like("patient_id", param.get("patientId"));
                }
                if (param.get("testName") != null && !param.get("testName").toString().isEmpty()) {
                    wrapper.apply(" EXISTS (SELECT 1 FROM lab_item_dict d WHERE d.item_id = lab_result.item_id AND d.item_name LIKE '%" + param.get("testName") + "%')");
                }
            }

            wrapper.eq("is_invalid", false);
            wrapper.orderByDesc("report_time");

            Page<LabResult> page = new Page<>(1, 10000);
            Page<LabResult> resultPage = labResultService.page(page, wrapper);
            List<LabResult> list = resultPage.getRecords();

            List<LabResultExportVO> exportList = list.stream().map(lr -> {
                LabResultExportVO vo = new LabResultExportVO();
                vo.setPatientId(lr.getPatientId());
                Patient patient = patientService.getById(lr.getPatientId());
                vo.setPatientName(patient != null ? patient.getPatientName() : "");
                if (lr.getItemId() != null) {
                    LabItemDict item = labItemDictService.getById(lr.getItemId());
                    vo.setItemName(item != null ? item.getItemName() : "");
                }
                vo.setResultValue(lr.getResultValue());
                vo.setResultUnit(lr.getResultUnit());
                vo.setReportTime(lr.getReportTime());
                vo.setExecuteDept(lr.getExecuteDept());
                vo.setExecuteDoc(lr.getExecuteDoc());
                return vo;
            }).collect(Collectors.toList());

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("检验结果数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), LabResultExportVO.class).sheet("检验结果数据").doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 联合孤立森林算法检测异常
    @PostMapping("/jointDetect")
    public Result jointIsolationForestDetect(@RequestBody JointDetectRequest request) {
        try {
            IsolationForestResultVO result = labResultService.jointIsolationForestDetect(
                    request.getPatientId(), request.getItemIds());
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("联合检测失败: " + e.getMessage());
        }
    }

    public static class JointDetectRequest {
        private String patientId;
        private java.util.List<Integer> itemIds;

        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public java.util.List<Integer> getItemIds() { return itemIds; }
        public void setItemIds(java.util.List<Integer> itemIds) { this.itemIds = itemIds; }
    }
}
