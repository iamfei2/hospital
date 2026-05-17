package com.hospit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospit.common.Result;
import com.hospit.entity.MedicalOrder;
import com.hospit.entity.Patient;
import com.hospit.mapper.PatientMapper;
import com.hospit.service.ILabResultService;
import com.hospit.service.IMedicalOrderService;
import com.hospit.service.IPatientService;
import com.hospit.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 患者信息表 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {
    @Autowired
    private IMedicalOrderService medicalOrderService;

    @Autowired
    @Lazy
    private ILabResultService labResultService;

    // 保存患者信息（加密敏感字段）
    @Override
    public boolean save(Patient entity) {
        encryptSensitiveFields(entity);
        return super.save(entity);
    }

    // 更新患者信息
    @Override
    public boolean updateById(Patient entity) {
        return super.updateById(entity);
    }

    // 获取患者信息（解密敏感字段）
    @Override
    public Patient getById(java.io.Serializable id) {
        Patient patient = super.getById(id);
        if (patient != null) {
            decryptSensitiveFields(patient);
        }
        return patient;
    }

    // 获取患者列表（解密敏感字段）
    @Override
    public List<Patient> list() {
        List<Patient> patients = super.list();
        patients.forEach(this::decryptSensitiveFields);
        return patients;
    }

    // 根据ID列表获取患者（解密敏感字段）
    @Override
    public List<Patient> listByIds(java.util.Collection<? extends java.io.Serializable> ids) {
        List<Patient> patients = super.listByIds(ids);
        patients.forEach(this::decryptSensitiveFields);
        return patients;
    }

    // 查询患者列表（解密敏感字段）
    public List<Patient> list(QueryWrapper<Patient> wrapper) {
        List<Patient> patients = super.list(wrapper);
        patients.forEach(this::decryptSensitiveFields);
        return patients;
    }

    // 加密敏感字段
    private void encryptSensitiveFields(Patient patient) {
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            patient.setPhone(CryptoUtil.encrypt(patient.getPhone()));
        }
        if (patient.getIdCard() != null && !patient.getIdCard().isEmpty()) {
            patient.setIdCard(CryptoUtil.encrypt(patient.getIdCard()));
        }
    }

    // 解密敏感字段
    public void decryptSensitiveFields(Patient patient) {
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            String decrypted = CryptoUtil.decrypt(patient.getPhone());
            if (decrypted != null && !decrypted.equals(patient.getPhone())) {
                patient.setPhone(decrypted);
                String decrypted2 = CryptoUtil.decrypt(patient.getPhone());
                if (decrypted2 != null && !decrypted2.equals(patient.getPhone())) {
                    patient.setPhone(decrypted2);
                }
            }
        }
        if (patient.getIdCard() != null && !patient.getIdCard().isEmpty()) {
            String decrypted = CryptoUtil.decrypt(patient.getIdCard());
            if (decrypted != null && !decrypted.equals(patient.getIdCard())) {
                patient.setIdCard(decrypted);
                String decrypted2 = CryptoUtil.decrypt(patient.getIdCard());
                if (decrypted2 != null && !decrypted2.equals(patient.getIdCard())) {
                    patient.setIdCard(decrypted2);
                }
            }
        }
    }

    // 获取患者完整信息
    @Override
    public Result getPatientCompleteInfo(String patientId) {
        try {
            Map<String, Object> result = new HashMap<>();

            Patient patient = this.getById(patientId);
            if (patient == null || patient.getIsInvalid()) {
                return Result.fail("患者不存在或已作废");
            }
            decryptSensitiveFields(patient);
            result.put("patientInfo", patient);

            QueryWrapper<MedicalOrder> orderQuery = new QueryWrapper<>();
            orderQuery.eq("patient_id", patientId)
                    .eq("is_invalid", 0)
                    .orderByDesc("start_time");
            List<MedicalOrder> medicalOrders = medicalOrderService.list(orderQuery);
            result.put("medicalOrders", medicalOrders);

            Result labResult = labResultService.getLabResultsByPatient(patientId);
            if (labResult.getCode() == 200) {
                result.put("labResults", labResult.getData());
            } else {
                result.put("labResults", null);
            }

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取患者完整信息失败");
        }
    }

    // 保存患者
    @Override
    public Result savePatient(Patient patient) {
        try {
            if (patient.getPatientId() == null || patient.getPatientId().trim().isEmpty()) {
                return Result.fail("患者ID不能为空");
            }
            Patient existing = super.getById(patient.getPatientId());
            if (existing != null) {
                return Result.fail("患者ID已存在");
            }
            patient.setCreateTime(java.time.LocalDateTime.now());
            patient.setIsInvalid(false);
            boolean success = super.save(patient);
            if (success) {
                return Result.success("新增成功");
            } else {
                return Result.fail("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("新增患者失败");
        }
    }

    // 更新患者
    @Override
    public Result updatePatient(Patient patient) {
        try {
            if (patient.getPatientId() == null || patient.getPatientId().trim().isEmpty()) {
                return Result.fail("患者ID不能为空");
            }
            Patient existing = super.getById(patient.getPatientId());
            if (existing == null) {
                return Result.fail("患者不存在");
            }
            patient.setCreateTime(existing.getCreateTime());
            patient.setIsInvalid(existing.getIsInvalid());
            patient.setUpdateTime(java.time.LocalDateTime.now());
            encryptSensitiveFields(patient);
            baseMapper.updateById(patient);
            return Result.success("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改患者失败");
        }
    }

    // 检查患者ID是否存在
    @Override
    public boolean existsByPatientId(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return false;
        }
        Patient patient = super.getById(patientId.trim());
        return patient != null && !patient.getIsInvalid();
    }
}
