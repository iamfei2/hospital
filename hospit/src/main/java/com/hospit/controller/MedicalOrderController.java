package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;

import com.hospit.common.Result;
import com.hospit.entity.MedicalOrder;
import com.hospit.entity.User;
import com.hospit.service.IMedicalOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 医嘱信息表 前端控制器
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@RestController
@RequestMapping("/medicalOrder")
public class MedicalOrderController {

    @Autowired
    private IMedicalOrderService medicalOrderService;

    /**
     * 分页查询医嘱信息
     */
    // 分页查询医嘱信息
    @PostMapping("/page")
    public Result page(@RequestBody QueryPageParam queryPageParam) {
        try {
            HashMap param = queryPageParam.getParam();
            // 这里可以添加查询条件处理
            return medicalOrderService.pageCustom(queryPageParam);
        } catch (Exception e) {
            return Result.fail();
        }
    }

    @OperateLog(operationType = "新增", operatedTable = "medical_order", description = "新增医嘱")
    @PostMapping("/add")
    public Result addMedicalOrder(@RequestBody MedicalOrder medicalOrder, HttpServletRequest request) {
        try {
            // 如果执行医生为空，默认使用当前用户
            if (medicalOrder.getExecuteDoc() == null || medicalOrder.getExecuteDoc().trim().isEmpty()) {
                String loginAccount = (String) request.getAttribute("loginAccount");
                if (loginAccount != null) {
                    medicalOrder.setExecuteDoc(loginAccount);
                }
            }
            return medicalOrderService.addMedicalOrder(medicalOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("新增医嘱失败: " + e.getMessage());
        }
    }


    /**
     * 新增：停止医嘱接口
     */
    @OperateLog(operationType = "停止", operatedTable = "medical_order", description = "停止医嘱")
    @PutMapping("/stop")
    public Result stopMedicalOrder(@RequestBody HashMap<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String endTimeStr = params.get("endTime").toString();

            // 解析结束时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            return medicalOrderService.stopMedicalOrder(orderId, endTime);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("停止医嘱失败: " + e.getMessage());
        }
    }

    /**
     * 根据患者ID获取医嘱信息
     */
    // 根据患者ID获取医嘱信息
    @GetMapping("/byPatient/{patientId}")
    public Result getMedicalOrdersByPatient(@PathVariable String patientId) {
        try {
            QueryWrapper<MedicalOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("patient_id", patientId)
                    .eq("is_invalid", 0)
                    .orderByDesc("start_time");
            List<MedicalOrder> medicalOrders = medicalOrderService.list(queryWrapper);
            return Result.success(medicalOrders);
        } catch (Exception e) {
            return Result.fail("获取医嘱信息失败");
        }
    }
}