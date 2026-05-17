package com.hospit.xml;

public enum XmlTypeEnum {
    CT("ct", "CT检查"),
    MRI("mri", "MRI检查"),
    PATHOLOGY("pathology", "病理检查"),
    ENTEROSCOPY("enteroscopy", "肠镜检查"),
    LAB("lab", "检验结果");

    private final String code;
    private final String description;

    XmlTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static XmlTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (XmlTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}