package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospit.entity.WarningRule;
import com.hospit.mapper.WarningRuleMapper;
import com.hospit.service.IWarningRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarningRuleServiceImpl extends ServiceImpl<WarningRuleMapper, WarningRule> implements IWarningRuleService {

    // 获取已启用的预警规则
    @Override
    public List<WarningRule> getEnabledRules() {
        QueryWrapper<WarningRule> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", true);
        return list(wrapper);
    }
}
