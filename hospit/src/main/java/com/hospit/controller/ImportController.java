package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.Result;
import com.hospit.service.IImportService;
import com.hospit.vo.ImportResultVO;
import com.hospit.xml.handler.XmlImportHandler;
import com.hospit.xml.result.XmlImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "批量导入")
@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired
    private IImportService importService;

    @Autowired
    private XmlImportHandler xmlImportHandler;

    @Operation(summary = "导入CT检查数据")
    @OperateLog(operationType = "导入", operatedTable = "ct_examination", description = "批量导入CT检查数据")
    @PostMapping("/ctExamination")
    public Result importCtExamination(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "strict") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            return Result.fail("只支持xls或xlsx格式的Excel文件");
        }
        ImportResultVO result = importService.importWithMode(file, "ct", mode);
        return Result.success(result);
    }

    @Operation(summary = "导入MRI检查数据")
    @OperateLog(operationType = "导入", operatedTable = "mri_examination", description = "批量导入MRI检查数据")
    @PostMapping("/mriExamination")
    public Result importMriExamination(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "strict") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            return Result.fail("只支持xls或xlsx格式的Excel文件");
        }
        ImportResultVO result = importService.importWithMode(file, "mri", mode);
        return Result.success(result);
    }

    @Operation(summary = "导入病理检查数据")
    @OperateLog(operationType = "导入", operatedTable = "pathology_examination", description = "批量导入病理检查数据")
    @PostMapping("/pathologyExamination")
    public Result importPathologyExamination(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "strict") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            return Result.fail("只支持xls或xlsx格式的Excel文件");
        }
        ImportResultVO result = importService.importWithMode(file, "pathology", mode);
        return Result.success(result);
    }

    @Operation(summary = "导入肠镜检查数据")
    @OperateLog(operationType = "导入", operatedTable = "enteroscopy_examination", description = "批量导入肠镜检查数据")
    @PostMapping("/enteroscopyExamination")
    public Result importEnteroscopyExamination(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "strict") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            return Result.fail("只支持xls或xlsx格式的Excel文件");
        }
        ImportResultVO result = importService.importWithMode(file, "enteroscopy", mode);
        return Result.success(result);
    }

    @Operation(summary = "导入检验结果数据(XML)")
    @OperateLog(operationType = "导入", operatedTable = "lab_result", description = "批量导入检验结果数据(XML)")
    @PostMapping("/lab/xml")
    public Result importLabXml(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的XML文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xml")) {
            return Result.fail("只支持xml格式的文件");
        }
        XmlImportResult result = xmlImportHandler.importXml(file, "lab");
        if ("PATIENT_NOT_FOUND".equals(result.getErrorCode())) {
            return Result.result(417, "患者不存在", 0L, result);
        }
        if (result.getErrors().isEmpty()) {
            return Result.success(result);
        } else {
            return Result.result(400, result.getErrors().get(0), 0L, result);
        }
    }

    @Operation(summary = "导入CT检查数据(XML)")
    @PostMapping("/ct/xml")
    public Result importCtXml(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的XML文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xml")) {
            return Result.fail("只支持xml格式的文件");
        }
        XmlImportResult result = xmlImportHandler.importXml(file, "ct");
        if ("PATIENT_NOT_FOUND".equals(result.getErrorCode())) {
            return Result.result(417, "患者不存在", 0L, result);
        }
        if (result.getErrors().isEmpty()) {
            return Result.success(result);
        } else {
            return Result.result(400, result.getErrors().get(0), 0L, result);
        }
    }

    @Operation(summary = "导入MRI检查数据(XML)")
    @PostMapping("/mri/xml")
    public Result importMriXml(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的XML文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xml")) {
            return Result.fail("只支持xml格式的文件");
        }
        XmlImportResult result = xmlImportHandler.importXml(file, "mri");
        if ("PATIENT_NOT_FOUND".equals(result.getErrorCode())) {
            return Result.result(417, "患者不存在", 0L, result);
        }
        if (result.getErrors().isEmpty()) {
            return Result.success(result);
        } else {
            return Result.result(400, result.getErrors().get(0), 0L, result);
        }
    }

    @Operation(summary = "导入病理检查数据(XML)")
    @PostMapping("/pathology/xml")
    public Result importPathologyXml(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的XML文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xml")) {
            return Result.fail("只支持xml格式的文件");
        }
        XmlImportResult result = xmlImportHandler.importXml(file, "pathology");
        if ("PATIENT_NOT_FOUND".equals(result.getErrorCode())) {
            return Result.result(417, "患者不存在", 0L, result);
        }
        if (result.getErrors().isEmpty()) {
            return Result.success(result);
        } else {
            return Result.result(400, result.getErrors().get(0), 0L, result);
        }
    }

    @Operation(summary = "导入肠镜检查数据(XML)")
    @PostMapping("/enteroscopy/xml")
    public Result importEnteroscopyXml(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的XML文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xml")) {
            return Result.fail("只支持xml格式的文件");
        }
        XmlImportResult result = xmlImportHandler.importXml(file, "enteroscopy");
        if ("PATIENT_NOT_FOUND".equals(result.getErrorCode())) {
            return Result.result(417, "患者不存在", 0L, result);
        }
        if (result.getErrors().isEmpty()) {
            return Result.success(result);
        } else {
            return Result.result(400, result.getErrors().get(0), 0L, result);
        }
    }
}
