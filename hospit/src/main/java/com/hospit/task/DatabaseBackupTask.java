package com.hospit.task;

import com.hospit.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据库自动备份定时任务
 * 每天凌晨2点执行全量备份，中午12点执行增量备份
 * 保留最近30份全量备份和7份增量备份
 */
@Component
public class DatabaseBackupTask {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupTask.class);

    @Autowired
    private BackupService backupService;

    // 每天凌晨2点全量备份
    @Scheduled(cron = "0 0 2 * * ?")
    public void fullBackup() {
        log.info("开始执行每日全量备份（加密+压缩）...");
        try {
            BackupService.BackupResult result = backupService.backup(true, true);
            log.info("全量备份成功: {}, 大小: {} bytes", result.fileName(), result.fileSize());
            backupService.cleanOldBackups(30);
        } catch (Exception e) {
            log.error("全量备份异常", e);
        }
    }

    // 每天中午12点增量备份
    @Scheduled(cron = "0 0 12 * * ?")
    public void incrementalBackup() {
        log.info("开始执行每日增量备份...");
        try {
            BackupService.BackupResult result = backupService.incrementalBackup(true, true);
            log.info("增量备份成功: {}, 大小: {} bytes", result.fileName(), result.fileSize());
        } catch (Exception e) {
            log.error("增量备份异常", e);
        }
    }
}
