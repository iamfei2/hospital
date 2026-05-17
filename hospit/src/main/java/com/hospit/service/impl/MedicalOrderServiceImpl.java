package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.MedicalOrder;
import com.hospit.mapper.MedicalOrderMapper;
import com.hospit.service.IMedicalOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * <p>
 * 医嘱信息表 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class MedicalOrderServiceImpl extends ServiceImpl<MedicalOrderMapper, MedicalOrder> implements IMedicalOrderService {
    // 分页查询医嘱
    @Override
    public Result pageCustom(QueryPageParam queryPageParam) {
        Page<MedicalOrder> page = new Page<>();
        page.setCurrent(queryPageParam.getPageNum());
        page.setSize(queryPageParam.getPageSize());

        QueryWrapper<MedicalOrder> wrapper = new QueryWrapper<>();
        HashMap param = queryPageParam.getParam();

        // 添加查询条件
        if (param != null) {
            // 患者ID查询条件
            if (param.get("patientId") != null && !param.get("patientId").toString().trim().isEmpty()) {
                wrapper.like("patient_id", param.get("patientId").toString().trim());
            }

            // 开始时间查询条件
            if (param.get("startTime") != null && !param.get("startTime").toString().trim().isEmpty()) {
                String startTimeStr = param.get("startTime").toString().trim();
                LocalDateTime startTime = parseDateTime(startTimeStr);
                if (startTime != null) {
                    wrapper.ge("start_time", startTime);
                }
            }

            // 结束时间查询条件
            if (param.get("endTime") != null && !param.get("endTime").toString().trim().isEmpty()) {
                String endTimeStr = param.get("endTime").toString().trim();
                LocalDateTime endTime = parseDateTime(endTimeStr);
                if (endTime != null) {
                    wrapper.le("start_time", endTime);
                }
            }
        }

        // 添加逻辑删除过滤，只查询有效数据
        wrapper.eq("is_invalid", 0);

        // 按开始时间倒序排列
        wrapper.orderByDesc("start_time");

        Page<MedicalOrder> result = this.page(page, wrapper);

        return Result.success(result.getRecords(), result.getTotal());
    }

    // 停止医嘱
    @Override
    public Result stopMedicalOrder(Long orderId, LocalDateTime endTime) {
        try {
            // 查询医嘱是否存在且状态为"执行"
            MedicalOrder medicalOrder = this.getById(orderId);
            if (medicalOrder == null) {
                return Result.fail("医嘱不存在");
            }

            if (!"执行".equals(medicalOrder.getOrderStatus())) {
                return Result.fail("只能停止状态为'执行'的医嘱");
            }

            // 更新医嘱状态和结束时间
            UpdateWrapper<MedicalOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("order_id", orderId)
                    .set("order_status", "终止")
                    .set("end_time", endTime)
                    .set("update_time", LocalDateTime.now());

            boolean success = this.update(updateWrapper);

            if (success) {
                return Result.success("医嘱已成功停止");
            } else {
                return Result.fail("停止医嘱失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("停止医嘱时发生错误: " + e.getMessage());
        }
    }

    // 解析日期时间字符串为LocalDateTime
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            System.err.println("日期时间解析失败: " + dateTimeStr + ", 错误: " + e.getMessage());
            return null;
        }
    }

    // 新增医嘱
    @Override
    public Result addMedicalOrder(MedicalOrder medicalOrder) {
        try {
            // 验证必填字段
            if (medicalOrder.getPatientId() == null || medicalOrder.getPatientId().trim().isEmpty()) {
                return Result.fail("患者ID不能为空");
            }
            if (medicalOrder.getOrderName() == null || medicalOrder.getOrderName().trim().isEmpty()) {
                return Result.fail("医嘱名称不能为空");
            }
            if (medicalOrder.getStartTime() == null) {
                return Result.fail("开始时间不能为空");
            }


            // 设置默认值
            medicalOrder.setIsInvalid(0); // 逻辑删除标识设为有效
            medicalOrder.setCreateTime(LocalDateTime.now());
            medicalOrder.setUpdateTime(LocalDateTime.now());
            System.out.println(123);
            // 保存到数据库
            boolean success = this.save(medicalOrder);
            System.out.println(medicalOrder);
            if (success) {
                System.out.println(1);
                return Result.success("医嘱新增成功");
            } else {
                return Result.fail("医嘱新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("新增医嘱时发生错误: " + e.getMessage());
        }
    }
}