package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospit.entity.WarningRecord;
import com.hospit.mapper.WarningRecordMapper;
import com.hospit.service.IWarningRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WarningRecordServiceImpl extends ServiceImpl<WarningRecordMapper, WarningRecord> implements IWarningRecordService {

    // 获取未读预警列表
    @Override
    public List<WarningRecord> getUnreadCount() {
        QueryWrapper<WarningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("is_read", false)
               .orderByDesc("create_time")
               .last("LIMIT 50");
        return list(wrapper);
    }

    // 统计未读预警数量
    @Override
    public long countUnread() {
        QueryWrapper<WarningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("is_read", false);
        return count(wrapper);
    }

    // 按严重程度统计预警
    @Override
    public List<Map<String, Object>> getStatsBySeverity() {
        return null;
    }
}
