package com.hospit.service;

import com.hospit.entity.ExaminationContext;
import com.hospit.entity.LabResult;

import java.util.List;

public interface IWarningEngineService {
    void evaluate(LabResult result);
    void evaluateBatch(List<LabResult> results);
    void evaluateExamination(ExaminationContext context);
    int rescanAllLabResults();
    int rescanAllExaminations();
}
