package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.Result;
import com.hospit.service.BackupService;
import com.hospit.service.RestoreConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @Autowired
    private RestoreConfirmationService confirmationService;

    @OperateLog(operationType = "备份", operatedTable = "database", description = "手动备份数据库")
    @PostMapping("/manual")
    public Result manualBackup(@RequestParam(defaultValue = "true") boolean encrypted,
                               @RequestParam(defaultValue = "true") boolean compressed) {
        try {
            BackupService.BackupResult result = backupService.backup(encrypted, compressed);
            return Result.success(Map.of(
                    "fileName", result.fileName(),
                    "filePath", result.filePath(),
                    "fileSize", result.fileSize(),
                    "checksum", result.checksum(),
                    "backupTime", result.backupTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ), "备份成功");
        } catch (Exception e) {
            return Result.fail("备份失败: " + e.getMessage());
        }
    }

    // 获取备份文件列表
    @GetMapping("/list")
    public Result listBackups() {
        try {
            File backupDir = new File(backupService.getBackupDir());
            if (!backupDir.exists()) {
                return Result.success(Collections.emptyList());
            }

            File[] files = backupDir.listFiles((d, name) -> 
                name.endsWith(".bak.gz") || name.endsWith(".sql.gz") || name.endsWith(".bak"));
            if (files == null) {
                return Result.success(Collections.emptyList());
            }

            List<Map<String, Object>> backupList = Arrays.stream(files)
                    .sorted(Comparator.comparingLong(File::lastModified).reversed())
                    .map(f -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("fileName", f.getName());
                        map.put("fileSize", f.length());
                        map.put("backupTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                .format(java.time.LocalDateTime.ofInstant(
                                        java.time.Instant.ofEpochMilli(f.lastModified()),
                                        java.time.ZoneId.systemDefault())));
                        map.put("filePath", f.getAbsolutePath());
                        map.put("encrypted", f.getName().startsWith("enc_"));
                        map.put("compressed", f.getName().endsWith(".gz"));
                        return map;
                    })
                    .collect(Collectors.toList());

            return Result.success(backupList);
        } catch (Exception e) {
            return Result.fail("查询备份列表失败: " + e.getMessage());
        }
    }

    // 校验备份文件完整性
    @GetMapping("/verify/{fileName}")
    public Result verifyBackup(@PathVariable String fileName) {
        try {
            File backupDir = new File(backupService.getBackupDir());
            File file = new File(backupDir, fileName);
            if (!file.exists()) {
                return Result.fail("备份文件不存在");
            }

            byte[] content = Files.readAllBytes(file.toPath());
            String checksum = backupService.calculateChecksum(content);
            boolean isEncrypted = fileName.startsWith("enc_");
            boolean isCompressed = fileName.endsWith(".gz");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileName", fileName);
            result.put("fileSize", file.length());
            result.put("checksum", checksum);
            result.put("encrypted", isEncrypted);
            result.put("compressed", isCompressed);
            result.put("valid", true);

            if (isCompressed && !isEncrypted) {
                try {
                    byte[] decompressed = decompress(content);
                    result.put("decompressedSize", decompressed.length);
                    result.put("sqlContentPreview", new String(decompressed, java.nio.charset.StandardCharsets.UTF_8).substring(0, Math.min(500, decompressed.length)));
                } catch (Exception e) {
                    result.put("decompressionNote", "无法解压预览，可能是其他压缩格式");
                }
            } else if (!isEncrypted) {
                String sqlPreview = new String(content, java.nio.charset.StandardCharsets.UTF_8).substring(0, Math.min(500, content.length));
                result.put("sqlContentPreview", sqlPreview);
            }

            return Result.success(result, "备份文件完整");
        } catch (Exception e) {
            return Result.fail("校验失败: " + e.getMessage());
        }
    }

    // 解压GZIP数据
    private byte[] decompress(byte[] data) throws Exception {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(bais)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    @OperateLog(operationType = "删除", operatedTable = "database", description = "删除备份文件")
    @DeleteMapping("/{fileName}")
    public Result deleteBackup(@PathVariable String fileName) {
        try {
            File backupDir = new File(backupService.getBackupDir());
            File file = new File(backupDir, fileName);
            if (!file.exists()) {
                return Result.fail("备份文件不存在");
            }
            if (!file.delete()) {
                return Result.fail("删除失败");
            }
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    // 初始化数据库恢复（已废弃）
    @PostMapping("/restore/init")
    public Result restoreInit(@RequestParam String fileName,
                              @RequestParam(defaultValue = "true") boolean encrypted,
                              @RequestParam(defaultValue = "true") boolean compressed,
                              HttpServletRequest request) {
        return Result.fail("此接口已废弃");
    }

    // 确认恢复操作（已废弃）
    @PostMapping("/restore/confirm")
    public Result restoreConfirm(@RequestParam String token,
                                 @RequestParam int confirmCode,
                                 HttpServletRequest request) {
        return Result.fail("此接口已废弃");
    }

    @OperateLog(operationType = "恢复", operatedTable = "database", description = "从备份恢复数据库")
    @PostMapping("/restore")
    public Result restore(@RequestParam String fileName,
                         @RequestParam(defaultValue = "true") boolean encrypted,
                         @RequestParam(defaultValue = "true") boolean compressed,
                         HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            backupService.restore(fileName, encrypted, compressed);
            return Result.success(null, "数据库恢复成功");
        } catch (Exception e) {
            return Result.fail("恢复执行失败: " + e.getMessage());
        }
    }

    // 获取客户端IP地址
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
