package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.Result;
import com.hospit.entity.LabItemDict;
import com.hospit.service.ILabItemDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/labItemDict")
public class LabItemDictController {

    @Autowired
    private ILabItemDictService labItemDictService;

    // 获取检验项目列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(labItemDictService.list());
    }

    // 根据ID获取检验项目详情
    @GetMapping("/{itemId}")
    public Result getById(@PathVariable Integer itemId) {
        return Result.success(labItemDictService.getById(itemId));
    }

    @OperateLog(operationType = "新增", operatedTable = "lab_item_dict", description = "新增检验项目")
    @PostMapping("/add")
    public Result add(@RequestBody LabItemDict labItemDict) {
        try {
            labItemDict.setCreateTime(LocalDateTime.now());
            boolean success = labItemDictService.save(labItemDict);
            return success ? Result.success(null, "添加成功") : Result.fail("添加失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "修改", operatedTable = "lab_item_dict", description = "修改检验项目")
    @PostMapping("/update")
    public Result update(@RequestBody LabItemDict labItemDict) {
        try {
            labItemDict.setUpdateTime(LocalDateTime.now());
            boolean success = labItemDictService.updateById(labItemDict);
            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "lab_item_dict", description = "删除检验项目")
    @DeleteMapping("/{itemId}")
    public Result delete(@PathVariable Integer itemId) {
        try {
            boolean success = labItemDictService.removeById(itemId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
}
