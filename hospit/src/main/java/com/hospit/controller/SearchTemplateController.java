package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospit.common.Result;
import com.hospit.entity.SearchTemplate;
import com.hospit.service.ISearchTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "查询模板")
@RestController
@RequestMapping("/searchTemplate")
public class SearchTemplateController {

    @Autowired
    private ISearchTemplateService searchTemplateService;

    @Operation(summary = "保存查询模板")
    @PostMapping("/save")
    public Result save(@RequestBody SearchTemplate template, HttpServletRequest request) {
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return Result.fail("请输入模板名称");
        }
        if (template.getTemplateType() == null || template.getTemplateType().trim().isEmpty()) {
            return Result.fail("请选择模板类型");
        }

        Integer userId = getCurrentUserId(request);
        template.setUserId(userId);
        template.setIsShared(template.getIsShared() != null ? template.getIsShared() : false);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());

        boolean success = searchTemplateService.save(template);
        return success ? Result.success(template) : Result.fail("保存失败");
    }

    @Operation(summary = "更新查询模板")
    @PutMapping("/update")
    public Result update(@RequestBody SearchTemplate template) {
        if (template.getTemplateId() == null) {
            return Result.fail("模板ID不能为空");
        }
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return Result.fail("请输入模板名称");
        }

        template.setUpdateTime(LocalDateTime.now());
        boolean success = searchTemplateService.updateById(template);
        return success ? Result.success(template) : Result.fail("更新失败");
    }

    @Operation(summary = "获取查询模板列表")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String templateType, HttpServletRequest request) {
        QueryWrapper<SearchTemplate> wrapper = new QueryWrapper<>();
        if (templateType != null && !templateType.trim().isEmpty()) {
            wrapper.eq("template_type", templateType);
        }
        wrapper.and(w -> w.eq("user_id", getCurrentUserId(request)).or().eq("is_shared", true));
        wrapper.orderByDesc("update_time");
        List<SearchTemplate> list = searchTemplateService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "获取所有模板（管理员）")
    @GetMapping("/all")
    public Result all(@RequestParam(required = false) String templateType) {
        QueryWrapper<SearchTemplate> wrapper = new QueryWrapper<>();
        if (templateType != null && !templateType.trim().isEmpty()) {
            wrapper.eq("template_type", templateType);
        }
        wrapper.orderByDesc("update_time");
        List<SearchTemplate> list = searchTemplateService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "切换模板共享状态")
    @PutMapping("/share/{templateId}")
    public Result toggleShare(@PathVariable Long templateId) {
        SearchTemplate template = searchTemplateService.getById(templateId);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        template.setIsShared(!template.getIsShared());
        template.setUpdateTime(LocalDateTime.now());
        boolean success = searchTemplateService.updateById(template);
        return success ? Result.success(template) : Result.fail("操作失败");
    }

    @Operation(summary = "删除查询模板")
    @DeleteMapping("/{templateId}")
    public Result delete(@PathVariable Long templateId) {
        boolean success = searchTemplateService.removeById(templateId);
        return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : 1;
    }
}
