package com.hospit.xml.result;

import java.util.ArrayList;
import java.util.List;

public class XmlImportResult {
    
    private int total;
    private int success;
    private int fail;
    private List<String> errors = new ArrayList<>();
    private String importToken;
    private String patientId;
    private Long costTime;
    private String itemSummary;
    private String errorCode;

    public static XmlImportResult success(int total, int success, String itemSummary) {
        XmlImportResult result = new XmlImportResult();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFail(total - success);
        result.setItemSummary(itemSummary);
        return result;
    }

    public static XmlImportResult failure(String errorCode, String error) {
        XmlImportResult result = new XmlImportResult();
        result.setErrorCode(errorCode);
        result.setTotal(0);
        result.setSuccess(0);
        result.setFail(0);
        result.getErrors().add(error);
        return result;
    }

    public static XmlImportResult patientNotFound(String patientId) {
        XmlImportResult result = new XmlImportResult();
        result.setErrorCode("PATIENT_NOT_FOUND");
        result.setPatientId(patientId);
        result.setTotal(0);
        result.setSuccess(0);
        result.setFail(0);
        result.getErrors().add("患者[" + patientId + "]不存在");
        return result;
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getSuccess() { return success; }
    public void setSuccess(int success) { this.success = success; }
    public int getFail() { return fail; }
    public void setFail(int fail) { this.fail = fail; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public String getImportToken() { return importToken; }
    public void setImportToken(String importToken) { this.importToken = importToken; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public Long getCostTime() { return costTime; }
    public void setCostTime(Long costTime) { this.costTime = costTime; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
