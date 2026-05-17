package com.hospit.util;

import org.springframework.stereotype.Component;

@Component
public class DesensitizationUtil {

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        return name.substring(0, 1) + "*";
    }

    public static String maskPatientId(String patientId) {
        if (patientId == null || patientId.length() < 4) {
            return patientId;
        }
        return "***" + patientId.substring(patientId.length() - 3);
    }
}
