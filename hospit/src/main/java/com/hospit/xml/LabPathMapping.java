package com.hospit.xml;

public class LabPathMapping extends XmlPathMapping {

    public static final String PATIENT_ID = "Patient/ID";
    public static final String PATIENT_NAME = "Patient/Name";
    public static final String REPORT_TIME = "LabReport/ReportTime";
    public static final String EXECUTE_DEPT = "LabReport/ExecuteDept";
    public static final String EXECUTE_DOC = "LabReport/ExecuteDoc";
    public static final String ITEM_CODE = "ItemCode";
    public static final String ITEM_NAME = "ItemName";
    public static final String RESULT_VALUE = "ResultValue";
    public static final String RESULT_UNIT = "ResultUnit";

    public LabPathMapping() {
        initialize();
    }

    @Override
    public void initialize() {
        addMapping(PATIENT_ID, "patientId");
        addMapping(PATIENT_NAME, "patientName");
        addMapping(REPORT_TIME, "reportTime");
        addMapping(EXECUTE_DEPT, "executeDept");
        addMapping(EXECUTE_DOC, "executeDoc");
        addMapping(ITEM_CODE, "itemCode");
        addMapping(ITEM_NAME, "itemName");
        addMapping(RESULT_VALUE, "resultValue");
        addMapping(RESULT_UNIT, "resultUnit");
    }
}
