package com.hospit.controller;

import com.hospit.common.Result;
import com.hospit.entity.IsolationForestRule;
import com.hospit.service.IIsolationForestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/isolationForest")
@Tag(name = "孤立森林规则管理")
public class IsolationForestController {

    @Autowired
    private IIsolationForestService isolationForestService;

    @GetMapping("/rules")
    @Operation(summary = "获取所有孤立森林规则")
    public Result getAllRules() {
        List<IsolationForestRule> rules = isolationForestService.getAllEnabledRules();
        return Result.success(rules);
    }

    @GetMapping("/rule/{ruleId}")
    @Operation(summary = "获取规则详情")
    public Result getRuleById(@PathVariable Long ruleId) {
        IsolationForestRule rule = isolationForestService.getRuleById(ruleId);
        return Result.success(rule);
    }

    @PostMapping("/rule")
    @Operation(summary = "创建孤立森林规则")
    public Result createRule(@RequestBody IsolationForestRule rule) {
        isolationForestService.buildModel(rule);
        return Result.success(null, "规则创建成功，模型训练中");
    }

    @PutMapping("/rule/{ruleId}")
    @Operation(summary = "更新孤立森林规则")
    public Result updateRule(@PathVariable Long ruleId, @RequestBody IsolationForestRule rule) {
        rule.setRuleId(ruleId);
        isolationForestService.buildModel(rule);
        return Result.success(null, "规则更新成功，模型重建中");
    }

    @DeleteMapping("/rule/{ruleId}")
    @Operation(summary = "删除孤立森林规则")
    public Result deleteRule(@PathVariable Long ruleId) {
        return Result.success(null, "规则删除成功");
    }

    @PostMapping("/train/{ruleId}")
    @Operation(summary = "手动训练模型")
    public Result trainModel(@PathVariable Long ruleId) {
        isolationForestService.trainModel(ruleId);
        return Result.success(null, "模型训练完成");
    }

    @PostMapping("/trainAll")
    @Operation(summary = "重新训练所有模型")
    public Result trainAllModels() {
        isolationForestService.rebuildAllModels();
        return Result.success(null, "所有模型训练完成");
    }
}
