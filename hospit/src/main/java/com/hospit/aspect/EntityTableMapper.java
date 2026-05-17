package com.hospit.aspect;

import java.util.HashMap;
import java.util.Map;

public class EntityTableMapper {
    
    private static final Map<String, Class<?>> TABLE_ENTITY_MAP = new HashMap<>();
    private static final Map<String, String> TABLE_ID_FIELD_MAP = new HashMap<>();
    
    static {
        TABLE_ENTITY_MAP.put("ct_examination", com.hospit.entity.CtExamination.class);
        TABLE_ID_FIELD_MAP.put("ct_examination", "ctId");
        
        TABLE_ENTITY_MAP.put("mri_examination", com.hospit.entity.MriExamination.class);
        TABLE_ID_FIELD_MAP.put("mri_examination", "mriId");
        
        TABLE_ENTITY_MAP.put("pathology_examination", com.hospit.entity.PathologyExamination.class);
        TABLE_ID_FIELD_MAP.put("pathology_examination", "pathologyId");
        
        TABLE_ENTITY_MAP.put("enteroscopy_examination", com.hospit.entity.EnteroscopyExamination.class);
        TABLE_ID_FIELD_MAP.put("enteroscopy_examination", "enteroscopyId");
        
        TABLE_ENTITY_MAP.put("lab_result", com.hospit.entity.LabResult.class);
        TABLE_ID_FIELD_MAP.put("lab_result", "resultId");
        
        TABLE_ENTITY_MAP.put("patient", com.hospit.entity.Patient.class);
        TABLE_ID_FIELD_MAP.put("patient", "patientId");
        
        TABLE_ENTITY_MAP.put("user", com.hospit.entity.User.class);
        TABLE_ID_FIELD_MAP.put("user", "userId");
        
        TABLE_ENTITY_MAP.put("lab_item_dict", com.hospit.entity.LabItemDict.class);
        TABLE_ID_FIELD_MAP.put("lab_item_dict", "itemId");
        
        TABLE_ENTITY_MAP.put("warning_rule", com.hospit.entity.WarningRule.class);
        TABLE_ID_FIELD_MAP.put("warning_rule", "ruleId");
        
        TABLE_ENTITY_MAP.put("warning_record", com.hospit.entity.WarningRecord.class);
        TABLE_ID_FIELD_MAP.put("warning_record", "recordId");
        
        TABLE_ENTITY_MAP.put("medical_order", com.hospit.entity.MedicalOrder.class);
        TABLE_ID_FIELD_MAP.put("medical_order", "orderId");
        
        TABLE_ENTITY_MAP.put("attachment", com.hospit.entity.Attachment.class);
        TABLE_ID_FIELD_MAP.put("attachment", "attachmentId");
    }
    
    public static Class<?> getEntityClass(String tableName) {
        return TABLE_ENTITY_MAP.get(tableName.toLowerCase());
    }
    
    public static String getIdField(String tableName) {
        return TABLE_ID_FIELD_MAP.get(tableName.toLowerCase());
    }
    
    public static boolean isKnownTable(String tableName) {
        return TABLE_ENTITY_MAP.containsKey(tableName.toLowerCase());
    }
}
