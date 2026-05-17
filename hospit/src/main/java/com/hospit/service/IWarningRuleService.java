package com.hospit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospit.entity.WarningRule;

import java.util.List;

public interface IWarningRuleService extends IService<WarningRule> {
    List<WarningRule> getEnabledRules();
}
