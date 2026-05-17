package com.hospit.config;

import com.hospit.entity.Patient;
import com.hospit.mapper.PatientMapper;
import com.hospit.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataEncryptionInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataEncryptionInitializer.class);

    private final PatientMapper patientMapper;

    public DataEncryptionInitializer(PatientMapper patientMapper) {
        this.patientMapper = patientMapper;
    }

    // 启动时自动加密未加密的患者数据
    @Override
    public void run(String... args) {
        List<Patient> patients = patientMapper.selectList(null);
        int updated = 0;
        for (Patient patient : patients) {
            boolean needUpdate = false;
            String phone = patient.getPhone();
            String idCard = patient.getIdCard();

            if (phone != null && !phone.isEmpty() && !isEncrypted(phone)) {
                patient.setPhone(CryptoUtil.encrypt(phone));
                needUpdate = true;
            }
            if (idCard != null && !idCard.isEmpty() && !isEncrypted(idCard)) {
                patient.setIdCard(CryptoUtil.encrypt(idCard));
                needUpdate = true;
            }
            if (needUpdate) {
                patientMapper.updateById(patient);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("自动加密修复: 共更新 {} 条患者数据", updated);
        }
    }

    // 检查文本是否已加密
    private boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(text);
            return decoded.length % 16 == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
