package com.hospit.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospit.entity.*;
import com.hospit.service.*;
import com.hospit.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警扫描定时任务
 * 扫描数据缺失预警（非计划性）和操作异常预警（批量删除、非工作时间操作）
 */
@Component
public class WarningScannerTask {

    private static final Logger log = LoggerFactory.getLogger(WarningScannerTask.class);

    @Autowired
    private IWarningRuleService warningRuleService;

    @Autowired
    private IWarningRecordService warningRecordService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private ILabResultService labResultService;

    @Autowired
    private ILabItemDictService labItemDictService;

    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    // 每30分钟扫描数据缺失
    @Scheduled(cron = "0 */30 * * * ?")
    public void scanMissingData() {
        log.info("开始执行数据缺失预警扫描...");
        try {
            List<WarningRule> missingRules = warningRuleService.lambdaQuery()
                    .eq(WarningRule::getRuleType, "MISSING")
                    .eq(WarningRule::getEnabled, true)
                    .list();

            for (WarningRule rule : missingRules) {
                int missingDays = rule.getMissingDays() != null ? rule.getMissingDays() : 7;
                LocalDateTime threshold = LocalDateTime.now().minusDays(missingDays);

                QueryWrapper<Patient> patientWrapper = new QueryWrapper<>();
                patientWrapper.eq("is_invalid", false);
                List<Patient> patients = patientService.list(patientWrapper);

                for (Patient patient : patients) {
                    QueryWrapper<LabResult> resultWrapper = new QueryWrapper<>();
                    resultWrapper.eq("patient_id", patient.getPatientId())
                                 .ge("report_time", threshold)
                                 .eq("is_invalid", false);
                    long count = labResultService.count(resultWrapper);

                    if (count == 0) {
                        WarningRecord record = new WarningRecord();
                        record.setPatientId(patient.getPatientId());
                        record.setPatientName(patient.getPatientName());
                        record.setRuleId(rule.getRuleId());
                        record.setRuleType("MISSING");
                        record.setSeverity(rule.getSeverity());
                        record.setMessage(String.format("[数据缺失] 患者%s已超过%d天无检验记录", patient.getPatientName(), missingDays));
                        record.setIsRead(false);
                        record.setCreateTime(LocalDateTime.now());
                        warningRecordService.save(record);
                        webSocketSessionManager.broadcast(buildMissingMessage(record));
                    }
                }
            }
            log.info("数据缺失预警扫描完成");
        } catch (Exception e) {
            log.error("数据缺失预警扫描异常", e);
        }
    }

    private java.util.Map<String, Object> buildMissingMessage(WarningRecord record) {
        java.util.Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", "WARNING");
        msg.put("severity", record.getSeverity());
        msg.put("patientName", record.getPatientName());
        msg.put("message", record.getMessage());
        msg.put("createTime", record.getCreateTime().toString());
        return msg;
    }

    @Autowired
    private IOperationLogService operationLogService;

    @Scheduled(cron = "0 */15 * * * ?")
    public void scanOperationAnomaly() {
        log.info("开始执行操作异常预警扫描...");
        try {
            List<WarningRule> operationRules = warningRuleService.lambdaQuery()
                    .eq(WarningRule::getRuleType, "OPERATION")
                    .eq(WarningRule::getEnabled, true)
                    .list();

            if (operationRules.isEmpty()) {
                log.info("无启用的操作异常预警规则，跳过扫描");
                return;
            }

            LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);

            QueryWrapper<OperationLog> recentWrapper = new QueryWrapper<>();
            recentWrapper.ge("operation_time", fiveMinAgo);
            List<OperationLog> recentLogs = operationLogService.list(recentWrapper);

            for (WarningRule rule : operationRules) {
                switch (rule.getConditionType()) {
                    case "ABOVE":
                        scanMassDelete(recentLogs, rule);
                        break;
                    case "BELOW":
                        scanOffHourOperation(recentLogs, rule);
                        break;
                    default:
                        break;
                }
            }
            log.info("操作异常预警扫描完成");
        } catch (Exception e) {
            log.error("操作异常预警扫描异常", e);
        }
    }

    private void scanMassDelete(List<OperationLog> recentLogs, WarningRule rule) {
        long deleteCount = recentLogs.stream()
                .filter(log -> "删除".equals(log.getOperationType()) || "作废".equals(log.getOperationType()))
                .count();

        long threshold = rule.getThresholdHigh() != null ? rule.getThresholdHigh().longValue() : 10;

        if (deleteCount >= threshold) {
            WarningRecord record = new WarningRecord();
            record.setRuleId(rule.getRuleId());
            record.setRuleType("OPERATION");
            record.setSeverity(rule.getSeverity());
            record.setMessage(String.format("[操作异常] 近5分钟内有%d条删除/作废操作，超过阈值%d", deleteCount, threshold));
            record.setIsRead(false);
            record.setCreateTime(LocalDateTime.now());
            warningRecordService.save(record);

            java.util.Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("type", "WARNING");
            msg.put("severity", record.getSeverity());
            msg.put("message", record.getMessage());
            msg.put("createTime", record.getCreateTime().toString());
            webSocketSessionManager.broadcast(msg);
        }
    }

    private void scanOffHourOperation(List<OperationLog> recentLogs, WarningRule rule) {
        int currentHour = java.time.LocalTime.now().getHour();
        int currentDay = java.time.LocalDate.now().getDayOfWeek().getValue() % 7;

        int offHourStart = rule.getOffHourStart() != null ? rule.getOffHourStart() : 22;
        int offHourEnd = rule.getOffHourEnd() != null ? rule.getOffHourEnd() : 6;
        String offDays = rule.getOffDays() != null ? rule.getOffDays() : "0,6";

        boolean isOffHour = (currentHour >= offHourStart || currentHour < offHourEnd)
                          || offDays.contains(String.valueOf(currentDay));

        if (isOffHour && !recentLogs.isEmpty()) {
            WarningRecord record = new WarningRecord();
            record.setRuleId(rule.getRuleId());
            record.setRuleType("OPERATION");
            record.setSeverity(rule.getSeverity());
            record.setMessage(String.format("[操作异常] 非工作时间(%d:00)检测到%d条操作记录", currentHour, recentLogs.size()));
            record.setIsRead(false);
            record.setCreateTime(LocalDateTime.now());
            warningRecordService.save(record);

            java.util.Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("type", "WARNING");
            msg.put("severity", record.getSeverity());
            msg.put("message", record.getMessage());
            msg.put("createTime", record.getCreateTime().toString());
            webSocketSessionManager.broadcast(msg);
        }
    }
}
