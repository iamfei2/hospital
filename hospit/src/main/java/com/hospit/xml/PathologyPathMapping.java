package com.hospit.xml;

public class PathologyPathMapping extends XmlPathMapping {

    public static final String PATIENT_ID = "Patient/ID";
    public static final String PATIENT_NAME = "Patient/Name";
    public static final String PATHOLOGY_NO = "Specimen/PathologyNo";
    public static final String SPECIMEN_TYPE = "Specimen/Type";
    public static final String SAMPLING_TIME = "Specimen/SamplingTime";
    public static final String PATHOLOGY_DOCTOR = "Diagnosis/Doctor";
    public static final String PATHOLOGY_DEPT = "Diagnosis/Dept";
    public static final String PATHOLOGY_DIAGNOSIS = "Diagnosis/Conclusion";

    public PathologyPathMapping() {
        initialize();
    }

    @Override
    public void initialize() {
        addMapping(PATIENT_ID, "patientId");
        addMapping(PATIENT_NAME, "patientName");
        addMapping(PATHOLOGY_NO, "pathologyNo");
        addMapping(SPECIMEN_TYPE, "specimenType");
        addMapping(SAMPLING_TIME, "samplingTime");
        addMapping(PATHOLOGY_DOCTOR, "pathologyDoctor");
        addMapping(PATHOLOGY_DEPT, "pathologyDept");
        addMapping(PATHOLOGY_DIAGNOSIS, "pathologyDiagnosis");
    }
}
