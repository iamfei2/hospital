package com.hospit.xml;

public class EnteroscopyPathMapping extends XmlPathMapping {

    public static final String PATIENT_ID = "Patient/ID";
    public static final String PATIENT_NAME = "Patient/Name";
    public static final String EXAMINATION_NO = "ExaminationInfo/ExamNo";
    public static final String EXAMINATION_TIME = "ExaminationInfo/ExamTime";
    public static final String ENTEROSCOPY_TYPE = "ExaminationInfo/EnteroscopyType";
    public static final String EXAMINE_DOCTOR = "Report/Doctor";
    public static final String EXAMINE_DEPT = "Report/Dept";
    public static final String REPORT_CONCLUSION = "Report/Conclusion";

    public EnteroscopyPathMapping() {
        initialize();
    }

    @Override
    public void initialize() {
        addMapping(PATIENT_ID, "patientId");
        addMapping(PATIENT_NAME, "patientName");
        addMapping(EXAMINATION_NO, "examinationNo");
        addMapping(EXAMINATION_TIME, "examinationTime");
        addMapping(ENTEROSCOPY_TYPE, "enteroscopyType");
        addMapping(EXAMINE_DOCTOR, "examineDoctor");
        addMapping(EXAMINE_DEPT, "examineDept");
        addMapping(REPORT_CONCLUSION, "reportConclusion");
    }
}
