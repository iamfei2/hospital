package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.WarningRecord;
import com.hospit.service.IWarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warningRecord")
public class WarningRecordController {

    @Autowired
    private IWarningRecordService warningRecordService;

    // 分页查询预警记录
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            QueryWrapper<WarningRecord> wrapper = new QueryWrapper<>();

            if (queryPageParam.getParam() != null) {
                Object patientId = queryPageParam.getParam().get("patientId");
                if (patientId != null && !patientId.toString().isEmpty()) {
                    wrapper.like("patient_id", patientId);
                }
                Object severity = queryPageParam.getParam().get("severity");
                if (severity != null && !severity.toString().isEmpty()) {
                    wrapper.eq("severity", severity);
                }
                Object ruleType = queryPageParam.getParam().get("ruleType");
                if (ruleType != null && !ruleType.toString().isEmpty()) {
                    wrapper.eq("rule_type", ruleType);
                }
                Object isRead = queryPageParam.getParam().get("isRead");
                if (isRead != null) {
                    wrapper.eq("is_read", Boolean.parseBoolean(isRead.toString()));
                }
                Object startTime = queryPageParam.getParam().get("startTime");
                Object endTime = queryPageParam.getParam().get("endTime");
                if (startTime != null && endTime != null) {
                    wrapper.between("create_time", startTime, endTime);
                }
            }

            wrapper.orderByDesc("create_time");
            Page<WarningRecord> page = new Page<>(pageNum, pageSize);
            Page<WarningRecord> resultPage = warningRecordService.page(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 获取未读预警数量
    @GetMapping("/unreadCount")
    public Result unreadCount() {
        try {
            long count = warningRecordService.countUnread();
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 获取未读预警列表
    @GetMapping("/unreadList")
    public Result unreadList() {
        try {
            List<WarningRecord> list = warningRecordService.getUnreadCount();
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "warning_record", description = "标记预警记录为已读")
    @PostMapping("/markRead/{warningId}")
    public Result markRead(@PathVariable Long warningId) {
        try {
            WarningRecord record = warningRecordService.getById(warningId);
            if (record == null) {
                return Result.fail("记录不存在");
            }
            record.setIsRead(true);
            boolean success = warningRecordService.updateById(record);
            return success ? Result.success(null, "操作成功") : Result.fail("操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "warning_record", description = "批量标记已读")
    @PostMapping("/markAllRead")
    public Result markAllRead() {
        try {
            QueryWrapper<WarningRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("is_read", false);
            WarningRecord update = new WarningRecord();
            update.setIsRead(true);
            boolean success = warningRecordService.update(update, wrapper);
            return success ? Result.success(null, "操作成功") : Result.fail("操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    // 获取预警统计信息
    @GetMapping("/stats")
    public Result stats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", warningRecordService.count());

            QueryWrapper<WarningRecord> unreadWrapper = new QueryWrapper<>();
            unreadWrapper.eq("is_read", false);
            stats.put("unread", warningRecordService.count(unreadWrapper));

            QueryWrapper<WarningRecord> criticalWrapper = new QueryWrapper<>();
            criticalWrapper.in("severity", "CRITICAL", "EMERGENCY").eq("is_read", false);
            stats.put("critical", warningRecordService.count(criticalWrapper));

            QueryWrapper<WarningRecord> todayWrapper = new QueryWrapper<>();
            todayWrapper.ge("create_time", java.time.LocalDate.now().atStartOfDay());
            stats.put("today", warningRecordService.count(todayWrapper));

            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("统计失败");
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "warning_record", description = "删除预警记录")
    @DeleteMapping("/{warningId}")
    public Result delete(@PathVariable Long warningId) {
        try {
            boolean success = warningRecordService.removeById(warningId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
}
