package com.hospit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.common.Result;
import com.hospit.service.IOperationLogService;
import com.hospit.entity.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {

    @Autowired
    private IOperationLogService operationLogService;

    // 获取操作日志列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(operationLogService.list());
    }

    // 分页查询操作日志
    @PostMapping("/page")
    public Result page(@RequestBody Map<String, Object> params) {
        try {
            Integer pageSize = (Integer) params.get("pageSize");
            Integer pageNum = (Integer) params.get("pageNum");
            Map<String, Object> param = (Map<String, Object>) params.get("param");

            Page<OperationLog> page = new Page<>(pageNum, pageSize);
            
            // 按操作时间降序排序
            page = operationLogService.lambdaQuery()
                    .orderByDesc(OperationLog::getOperationTime)
                    .page(page);
            
            return Result.success(page.getRecords(), page.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询失败");
        }
    }
}
