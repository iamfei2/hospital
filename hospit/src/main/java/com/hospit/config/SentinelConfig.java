package com.hospit.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelConfig.class);

    private static final String OPEN_API_RESOURCE = "open-api";
    private static final String BACKUP_RESOURCE = "backup";
    private static final String STATISTICS_RESOURCE = "statistics";

    // 初始化Sentinel规则
    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("Sentinel 熔断限流规则初始化完成");
    }

    // 初始化流控规则
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule openApiRule = new FlowRule();
        openApiRule.setResource(OPEN_API_RESOURCE);
        openApiRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        openApiRule.setCount(100);
        openApiRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        openApiRule.setMaxQueueingTimeMs(500);
        rules.add(openApiRule);

        FlowRule backupRule = new FlowRule();
        backupRule.setResource(BACKUP_RESOURCE);
        backupRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        backupRule.setCount(10);
        backupRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(backupRule);

        FlowRule statisticsRule = new FlowRule();
        statisticsRule.setResource(STATISTICS_RESOURCE);
        statisticsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        statisticsRule.setCount(50);
        statisticsRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(statisticsRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 流控规则已加载: {} 条", rules.size());
    }

    // 初始化熔断规则
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule openApiDegrade = new DegradeRule();
        openApiDegrade.setResource(OPEN_API_RESOURCE);
        openApiDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        openApiDegrade.setCount(0.3);
        openApiDegrade.setMinRequestAmount(5);
        openApiDegrade.setStatIntervalMs(60000);
        openApiDegrade.setTimeWindow(30);
        rules.add(openApiDegrade);

        DegradeRule backupDegrade = new DegradeRule();
        backupDegrade.setResource(BACKUP_RESOURCE);
        backupDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        backupDegrade.setCount(3);
        backupDegrade.setMinRequestAmount(3);
        backupDegrade.setTimeWindow(60);
        rules.add(backupDegrade);

        DegradeRule statisticsDegrade = new DegradeRule();
        statisticsDegrade.setResource(STATISTICS_RESOURCE);
        statisticsDegrade.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        statisticsDegrade.setCount(1000);
        statisticsDegrade.setMinRequestAmount(10);
        statisticsDegrade.setTimeWindow(30);
        rules.add(statisticsDegrade);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel 熔断规则已加载: {} 条", rules.size());
    }
}
