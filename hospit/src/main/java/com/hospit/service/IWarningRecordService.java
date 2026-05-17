package com.hospit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospit.entity.WarningRecord;

import java.util.List;
import java.util.Map;

public interface IWarningRecordService extends IService<WarningRecord> {
    List<WarningRecord> getUnreadCount();
    long countUnread();
    List<Map<String, Object>> getStatsBySeverity();
}
