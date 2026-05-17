package com.hospit.controller;

import com.hospit.annotation.OperateLog;
import com.hospit.common.Result;
import com.hospit.entity.Patient;
import com.hospit.service.IPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.common.QueryPageParam;

/**
 * <p>
 * 患者信息表 前端控制器
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@RestController
@RequestMapping("/patient")
public class PatientController {
    @Autowired
    private IPatientService patientService;

    /**
     * 根据患者ID获取患者基本信息
     */
    @GetMapping("/detail/{patientId}")
    public Result getPatientDetail(@PathVariable String patientId) {
        try {
            Patient patient = patientService.getById(patientId);
            if (patient != null && !patient.getIsInvalid()) {
                patientService.decryptSensitiveFields(patient);
                return Result.success(patient);
            } else {
                return Result.fail("患者不存在或已作废");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取患者信息失败");
        }
    }

    /**
     * 根据患者姓名获取患者信息列表（支持模糊查询，返回多个结果）
     */
    @GetMapping("/byName")
    public Result getPatientByName(@RequestParam String patientName) {
        try {
            QueryWrapper<Patient> wrapper = new QueryWrapper<>();
            wrapper.like("patient_name", patientName)
                   .eq("is_invalid", false)
                   .orderByDesc("create_time");
            java.util.List<Patient> patients = patientService.list(wrapper);
            if (patients != null && !patients.isEmpty()) {
                return Result.success(patients);
            } else {
                return Result.fail("未找到患者");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取患者信息失败");
        }
    }

    /**
     * 根据患者ID获取完整信息（包含基本信息、医嘱、检验结果）
     */
    @GetMapping("/completeInfo/{patientId}")
    public Result getPatientCompleteInfo(@PathVariable String patientId) {
        try {
            return patientService.getPatientCompleteInfo(patientId);
        } catch (Exception e) {
            return Result.fail("获取患者完整信息失败");
        }
    }

    /**
     * 获取患者列表（用于选择）
     */
@GetMapping("/list")
    public Result getPatientList() {
        try {
            return Result.success(patientService.list());
        } catch (Exception e) {
            return Result.fail("获取患者列表失败");
        }
    }

    /**
     * 分页获取患者列表
     */
    @RequestMapping("/page")
    public Result getPatientPage(@RequestBody QueryPageParam queryPageParam) {
        try {
            Page<Patient> page = new Page<>(queryPageParam.getPageNum(), queryPageParam.getPageSize());
            QueryWrapper<Patient> wrapper = new QueryWrapper<>();
            
            java.util.Map<String, Object> param = queryPageParam.getParam();
            if (param != null) {
                if (param.get("patientId") != null && !param.get("patientId").toString().isEmpty()) {
                    wrapper.eq("patient_id", param.get("patientId").toString());
                }
                if (param.get("patientName") != null && !param.get("patientName").toString().isEmpty()) {
                    wrapper.like("patient_name", param.get("patientName").toString());
                }
            }
            wrapper.orderByDesc("create_time");
            
            Page<Patient> result = patientService.page(page, wrapper);
            result.getRecords().forEach(p -> patientService.decryptSensitiveFields(p));
            return Result.success(result.getRecords(), result.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取患者列表失败");
        }
    }

    /**
     * 新增患者
     */
    @OperateLog(operationType = "新增", operatedTable = "patient", description = "新增患者")
    @RequestMapping("/add")
    public Result addPatient(@RequestBody Patient patient) {
        try {
            return patientService.savePatient(patient);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("新增患者失败");
        }
    }

    /**
     * 更新患者
     */
    @OperateLog(operationType = "修改", operatedTable = "patient", description = "修改患者信息")
    @RequestMapping("/update")
    public Result updatePatient(@RequestBody Patient patient) {
        try {
            return patientService.updatePatient(patient);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("更新患者失败");
        }
    }

    /**
     * 作废患者
     */
    @OperateLog(operationType = "作废", operatedTable = "patient", description = "作废患者")
    @GetMapping("/invalid/{patientId}")
    public Result invalidPatient(@PathVariable String patientId) {
        try {
            Patient patient = patientService.getById(patientId);
            if (patient == null) {
                return Result.fail("患者不存在");
            }
            patient.setIsInvalid(true);
            patientService.updateById(patient);
            return Result.success("作废成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("作废失败");
        }
    }

    /**
     * 恢复患者
     */
    @OperateLog(operationType = "恢复", operatedTable = "patient", description = "恢复患者")
    @GetMapping("/restore/{patientId}")
    public Result restorePatient(@PathVariable String patientId) {
        try {
            Patient patient = patientService.getById(patientId);
            if (patient == null) {
                return Result.fail("患者不存在");
            }
            patient.setIsInvalid(false);
            patientService.updateById(patient);
            return Result.success("恢复成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("恢复失败");
        }
    }

}
