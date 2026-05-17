package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.WarningRule;
import com.hospit.service.IWarningEngineService;
import com.hospit.service.IWarningRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/warningRule")
public class WarningRuleController {

    @Autowired
    private IWarningRuleService warningRuleService;

    // 获取预警规则列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(warningRuleService.list());
    }

    // 分页查询预警规则
    @PostMapping("/page")
    public Result getPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            int pageNum = queryPageParam.getPageNum();
            int pageSize = queryPageParam.getPageSize();
            QueryWrapper<WarningRule> wrapper = new QueryWrapper<>();

            if (queryPageParam.getParam() != null) {
                Object ruleType = queryPageParam.getParam().get("ruleType");
                if (ruleType != null && !ruleType.toString().isEmpty()) {
                    wrapper.eq("rule_type", ruleType);
                }
                Object severity = queryPageParam.getParam().get("severity");
                if (severity != null && !severity.toString().isEmpty()) {
                    wrapper.eq("severity", severity);
                }
                Object enabled = queryPageParam.getParam().get("enabled");
                if (enabled != null && !enabled.toString().isEmpty()) {
                    wrapper.eq("enabled", Boolean.parseBoolean(enabled.toString()));
                }
            }

            wrapper.orderByDesc("create_time");
            Page<WarningRule> page = new Page<>(pageNum, pageSize);
            Page<WarningRule> resultPage = warningRuleService.page(page, wrapper);

            return Result.success(resultPage.getRecords(), resultPage.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }

    // 根据ID获取预警规则详情
    @GetMapping("/{ruleId}")
    public Result getById(@PathVariable Long ruleId) {
        return Result.success(warningRuleService.getById(ruleId));
    }

    @OperateLog(operationType = "新增", operatedTable = "warning_rule", description = "新增预警规则")
    @PostMapping("/add")
    public Result add(@RequestBody WarningRule warningRule) {
        try {
            warningRule.setCreateTime(LocalDateTime.now());
            warningRule.setUpdateTime(LocalDateTime.now());
            boolean success = warningRuleService.save(warningRule);
            return success ? Result.success(null, "添加成功") : Result.fail("添加失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "warning_rule", description = "修改预警规则")
    @PostMapping("/update")
    public Result update(@RequestBody WarningRule warningRule) {
        try {
            warningRule.setUpdateTime(LocalDateTime.now());
            boolean success = warningRuleService.updateById(warningRule);
            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "warning_rule", description = "删除预警规则")
    @DeleteMapping("/{ruleId}")
    public Result delete(@PathVariable Long ruleId) {
        try {
            boolean success = warningRuleService.removeById(ruleId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "warning_rule", description = "切换预警规则状态")
    @PostMapping("/toggle/{ruleId}")
    public Result toggle(@PathVariable Long ruleId) {
        try {
            WarningRule rule = warningRuleService.getById(ruleId);
            if (rule == null) {
                return Result.fail("规则不存在");
            }
            rule.setEnabled(!rule.getEnabled());
            rule.setUpdateTime(LocalDateTime.now());
            boolean success = warningRuleService.updateById(rule);
            return success ? Result.success(null, "操作成功") : Result.fail("操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @Autowired
    private IWarningEngineService warningEngineService;

    @OperateLog(operationType = "扫描", operatedTable = "warning_rule", description = "扫描历史检验结果")
    @PostMapping("/rescanLab")
    public Result rescanLab() {
        try {
            int count = warningEngineService.rescanAllLabResults();
            return Result.success(null, "扫描完成，共触发 " + count + " 条预警");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("扫描失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "扫描", operatedTable = "warning_rule", description = "扫描历史检查结果")
    @PostMapping("/rescanExam")
    public Result rescanExam() {
        try {
            int count = warningEngineService.rescanAllExaminations();
            return Result.success(null, "扫描完成，共触发 " + count + " 条预警");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("扫描失败：" + e.getMessage());
        }
    }
}
