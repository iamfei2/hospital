package com.hospit.service;

import com.hospit.common.Result;
import com.hospit.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 患者信息表 服务类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
public interface IPatientService extends IService<Patient> {
    /**
     * 获取患者完整信息（包含基本信息、医嘱、检验结果）
     */
    Result getPatientCompleteInfo(String patientId);

    /**
     * 解密患者敏感字段
     */
    void decryptSensitiveFields(Patient patient);

    /**
     * 保存患者
     */
    Result savePatient(Patient patient);

    /**
     * 更新患者
     */
    Result updatePatient(Patient patient);

    /**
     * 检查患者ID是否存在
     */
    boolean existsByPatientId(String patientId);
}
