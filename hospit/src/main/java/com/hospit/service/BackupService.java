package com.hospit.service;

import com.hospit.entity.BackupRecord;
import com.hospit.mapper.BackupRecordMapper;
import com.hospit.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库备份恢复服务
 * 支持mysqldump备份、AES/GCM加密、GZIP压缩、SHA-256校验
 * 自动清理旧备份，保留最新N份
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/hospital}")
    private String dbUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUsername;

    @Value("${spring.datasource.password:123456}")
    private String dbPassword;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Autowired
    private BackupRecordMapper backupRecordMapper;

    private static final Map<String, String> BACKUP_CHECKSUMS = new ConcurrentHashMap<>();

    public String getBackupDir() {
        return uploadPath + File.separator + "backups";
    }

    private String getDbName() {
        log.info("原始dbUrl: " + dbUrl);
        Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");
        Matcher matcher = pattern.matcher(dbUrl);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            log.info("提取的数据库名: " + dbName);
            return dbName;
        }
        return "hospital";
    }

    private String getMysqldumpPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String mysqldump = findMysqldumpOnWindows();
            if (!mysqldump.equals("mysqldump")) {
                return mysqldump;
            }
        }
        return "mysqldump";
    }

    private String getMysqlPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String mysql = findMysqlOnWindows();
            if (!mysql.equals("mysql")) {
                return mysql;
            }
        }
        return "mysql";
    }

    private String findMysqldumpOnWindows() {
        String[] possiblePaths = {
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
            "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
            "C:\\mysql\\bin\\mysqldump.exe"
        };
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                log.info("找到mysqldump: " + path);
                return path;
            }
        }
        log.warn("未找到mysqldump，使用默认命令");
        return "mysqldump";
    }

    private String findMysqlOnWindows() {
        String[] possiblePaths = {
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
            "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
            "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
            "C:\\mysql\\bin\\mysql.exe"
        };
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                log.info("找到mysql: " + path);
                return path;
            }
        }
        log.warn("未找到mysql，使用默认命令");
        return "mysql";
    }

    public record BackupResult(String fileName, String filePath, long fileSize, String checksum, LocalDateTime backupTime) {}

    public BackupResult backup(boolean encrypted, boolean compressed) throws IOException, InterruptedException {
        String backupDir = getBackupDir();
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String dbName = getDbName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String prefix = encrypted ? "enc_" : "";
        String suffix = compressed ? ".gz" : "";
        String ext = encrypted ? ".bak" : ".sql" + suffix;
        String fileName = prefix + "backup_" + timestamp + ext;
        String filePath = backupDir + File.separator + fileName;

        String mysqldump = getMysqldumpPath();
        log.info("使用mysqldump路径: " + mysqldump);
        
        String passwordArg = "";
        if (dbPassword != null && !dbPassword.isEmpty()) {
            passwordArg = "-p" + dbPassword;
        }
        
        String dumpCmd;
        if (passwordArg.isEmpty()) {
            dumpCmd = String.format("\"%s\" -u %s --single-transaction --routines --triggers --hex-blob %s",
                    mysqldump, dbUsername, dbName);
        } else {
            dumpCmd = String.format("\"%s\" -u %s %s --single-transaction --routines --triggers --hex-blob %s",
                    mysqldump, dbUsername, passwordArg, dbName);
        }
        
        log.info("执行命令: " + dumpCmd);
        
        ProcessBuilder dumpPb = new ProcessBuilder(
                "cmd", "/c", dumpCmd
        );
        dumpPb.redirectErrorStream(true);
        Process dumpProcess = dumpPb.start();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dumpProcess.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                baos.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
        }

        int dumpExitCode = dumpProcess.waitFor();
        if (dumpExitCode != 0) {
            String output = baos.toString();
            log.error("mysqldump错误输出: " + output);
            throw new IOException("mysqldump failed with exit code: " + dumpExitCode + ", output: " + output);
        }

        byte[] sqlBytes = baos.toByteArray();
        byte[] processedBytes;

        if (compressed) {
            processedBytes = compress(sqlBytes);
        } else {
            processedBytes = sqlBytes;
        }

        if (encrypted) {
            String encryptedBase64 = CryptoUtil.encrypt(new String(processedBytes, StandardCharsets.UTF_8));
            processedBytes = encryptedBase64.getBytes(StandardCharsets.UTF_8);
        }

        String checksum = calculateChecksum(processedBytes);
        BACKUP_CHECKSUMS.put(fileName, checksum);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(processedBytes);
        }

        File backupFile = new File(filePath);
        log.info("备份成功: {}, 大小: {} bytes, SHA256: {}...", fileName, backupFile.length(), checksum.substring(0, 16));

        return new BackupResult(fileName, filePath, backupFile.length(), checksum, LocalDateTime.now());
    }

    // 获取当前 MySQL binlog 位点
    public BackupPosition getCurrentBinlogPosition() {
        // 降级方案：用时间戳作为增量起点
        if (backupRecordMapper == null) {
            return new BackupPosition(null, System.currentTimeMillis(), LocalDateTime.now());
        }
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW MASTER STATUS")) {
            if (rs.next()) {
                String file = rs.getString("File");
                long position = rs.getLong("Position");
                return new BackupPosition(file, position, LocalDateTime.now());
            }
        } catch (Exception e) {
            log.warn("无法获取binlog位点，将使用时间戳作为增量标记: {}", e.getMessage());
        }
        return new BackupPosition(null, System.currentTimeMillis(), LocalDateTime.now());
    }

    // 增量备份 - 基于上次备份后的数据变更
    public BackupResult incrementalBackup(boolean encrypted, boolean compressed) throws IOException, InterruptedException {
        BackupRecord lastBackup = backupRecordMapper != null ? backupRecordMapper.selectLatestSuccessful() : null;
        LocalDateTime lastBackupTime = lastBackup != null ? lastBackup.getEndTime() : LocalDateTime.now().minusDays(1);

        String backupDir = getBackupDir();
        File dir = new File(backupDir);
        if (!dir.exists()) dir.mkdirs();

        String dbName = getDbName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String prefix = encrypted ? "enc_" : "";
        String suffix = compressed ? ".gz" : "";
        String ext = encrypted ? ".bak_incr" : ".sql_incr" + suffix;
        String fileName = prefix + "backup_incr_" + timestamp + ext;
        String filePath = backupDir + File.separator + fileName;

        String mysqldump = getMysqldumpPath();
        String passwordArg = dbPassword != null && !dbPassword.isEmpty() ? "-p" + dbPassword : "";

        // 使用 --where 过滤增量的表
        String tablesWithTime = "lab_result ct_examination mri_examination enteroscopy_examination pathology_examination";
        String[] tables = tablesWithTime.split(" ");

        ByteArrayOutputStream totalOutput = new ByteArrayOutputStream();
        LocalDateTime backupStart = LocalDateTime.now();

        for (String table : tables) {
            String dumpCmd;
            String whereClause = String.format(" --where=\"update_time > '%s' OR create_time > '%s'\"",
                    lastBackupTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    lastBackupTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (passwordArg.isEmpty()) {
                dumpCmd = String.format("\"%s\" -u %s --single-transaction --no-create-info %s %s %s",
                        mysqldump, dbUsername, whereClause, dbName, table);
            } else {
                dumpCmd = String.format("\"%s\" -u %s %s --single-transaction --no-create-info %s %s %s",
                        mysqldump, dbUsername, passwordArg, whereClause, dbName, table);
            }

            ProcessBuilder dumpPb = new ProcessBuilder("cmd", "/c", dumpCmd);
            dumpPb.redirectErrorStream(true);
            Process dumpProcess = dumpPb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dumpProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    totalOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }

            int exitCode = dumpProcess.waitFor();
            if (exitCode != 0) {
                log.warn("table {} incremental dump failed with exit {}", table, exitCode);
            }
        }

        byte[] sqlBytes = totalOutput.toByteArray();
        byte[] processedBytes;
        if (compressed) processedBytes = compress(sqlBytes);
        else processedBytes = sqlBytes;

        if (encrypted) {
            String encryptedBase64 = CryptoUtil.encrypt(new String(processedBytes, StandardCharsets.UTF_8));
            processedBytes = encryptedBase64.getBytes(StandardCharsets.UTF_8);
        }

        String checksum = calculateChecksum(processedBytes);
        BACKUP_CHECKSUMS.put(fileName, checksum);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(processedBytes);
        }

        File backupFile = new File(filePath);
        LocalDateTime backupEnd = LocalDateTime.now();

        // 记录备份元数据
        if (backupRecordMapper != null) {
            BackupRecord record = new BackupRecord();
            record.setBackupType("INCREMENTAL");
            record.setFileName(fileName);
            record.setStartTime(backupStart);
            record.setEndTime(backupEnd);
            record.setFileSize(backupFile.length());
            record.setSuccess(true);
            record.setCreateTime(LocalDateTime.now());
            backupRecordMapper.insert(record);
        }

        log.info("增量备份成功: {}, 大小: {} bytes", fileName, backupFile.length());
        return new BackupResult(fileName, filePath, backupFile.length(), checksum, backupEnd);
    }

    public record BackupPosition(String binlogFile, long binlogPosition, LocalDateTime recordTime) {}

    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(baos)) {
            gzos.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(bais)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    public String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("计算校验和失败", e);
            return "";
        }
    }

    public boolean verifyChecksum(String fileName, byte[] data) {
        String storedChecksum = BACKUP_CHECKSUMS.get(fileName);
        if (storedChecksum == null) {
            storedChecksum = calculateChecksum(data);
        }
        String currentChecksum = calculateChecksum(data);
        return storedChecksum.equals(currentChecksum);
    }

    public void restore(String fileName, boolean expectEncrypted, boolean expectCompressed) throws IOException, InterruptedException {
        String backupDir = getBackupDir();
        File backupFile = new File(backupDir + File.separator + fileName);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("备份文件不存在: " + fileName);
        }

        byte[] fileContent = Files.readAllBytes(backupFile.toPath());

        byte[] sqlBytes;
        if (expectEncrypted) {
            String decrypted = CryptoUtil.decrypt(new String(fileContent, StandardCharsets.UTF_8));
            sqlBytes = decrypted.getBytes(StandardCharsets.UTF_8);
        } else {
            sqlBytes = fileContent;
        }

        if (expectCompressed) {
            sqlBytes = decompress(sqlBytes);
        }

        String dbName = getDbName();
        String mysql = getMysqlPath();
        log.info("使用mysql路径: " + mysql);
        
        String passwordArg = "";
        if (dbPassword != null && !dbPassword.isEmpty()) {
            passwordArg = "-p" + dbPassword;
        }
        
        String restoreCmd;
        if (passwordArg.isEmpty()) {
            restoreCmd = String.format("\"%s\" -u %s --default-character-set=utf8mb4 %s",
                    mysql, dbUsername, dbName);
        } else {
            restoreCmd = String.format("\"%s\" -u %s %s --default-character-set=utf8mb4 %s",
                    mysql, dbUsername, passwordArg, dbName);
        }
        
        log.info("执行恢复命令: " + restoreCmd);
        
        ProcessBuilder restorePb = new ProcessBuilder(
                "cmd", "/c", restoreCmd
        );
        restorePb.redirectErrorStream(true);
        Process restoreProcess = restorePb.start();

        try (BufferedOutputStream bos = new BufferedOutputStream(restoreProcess.getOutputStream())) {
            bos.write(sqlBytes);
            bos.flush();
        }

        int exitCode = restoreProcess.waitFor();
        if (exitCode != 0) {
            throw new IOException("mysql restore failed with exit code: " + exitCode);
        }

        log.info("数据库恢复成功: {}", fileName);
    }

    public void cleanOldBackups(int keepCount) {
        try {
            File dir = new File(getBackupDir());
            File[] files = dir.listFiles((d, name) -> name.endsWith(".bak.gz") || name.endsWith(".sql.gz") || name.endsWith(".sql"));
            if (files == null || files.length <= keepCount) {
                return;
            }

            java.util.Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            int toDelete = files.length - keepCount;
            for (int i = 0; i < toDelete; i++) {
                BACKUP_CHECKSUMS.remove(files[i].getName());
                if (files[i].delete()) {
                    log.info("清理旧备份: {}", files[i].getName());
                }
            }
        } catch (Exception e) {
            log.error("清理旧备份异常", e);
        }
    }
}
