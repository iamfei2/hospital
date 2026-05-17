package com.hospit.vo;

import java.util.List;
import java.util.Map;

public class ImportResultVO {

    private Integer total;
    private Integer success;
    private Integer fail;
    private List<String> errors;
    private Long costTime;
    private List<FailedRowDetail> failedRows;
    private Map<String, Integer> errorSummary;
    private String importMode;
    private String importToken;

    public ImportResultVO() {}

    public ImportResultVO(Integer total, Integer success, Integer fail, List<String> errors, Long costTime,
                          List<FailedRowDetail> failedRows, Map<String, Integer> errorSummary, String importMode, String importToken) {
        this.total = total;
        this.success = success;
        this.fail = fail;
        this.errors = errors;
        this.costTime = costTime;
        this.failedRows = failedRows;
        this.errorSummary = errorSummary;
        this.importMode = importMode;
        this.importToken = importToken;
    }

    public static ImportResultVOBuilder builder() { return new ImportResultVOBuilder(); }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getSuccess() { return success; }
    public void setSuccess(Integer success) { this.success = success; }
    public Integer getFail() { return fail; }
    public void setFail(Integer fail) { this.fail = fail; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public Long getCostTime() { return costTime; }
    public void setCostTime(Long costTime) { this.costTime = costTime; }
    public List<FailedRowDetail> getFailedRows() { return failedRows; }
    public void setFailedRows(List<FailedRowDetail> failedRows) { this.failedRows = failedRows; }
    public Map<String, Integer> getErrorSummary() { return errorSummary; }
    public void setErrorSummary(Map<String, Integer> errorSummary) { this.errorSummary = errorSummary; }
    public String getImportMode() { return importMode; }
    public void setImportMode(String importMode) { this.importMode = importMode; }
    public String getImportToken() { return importToken; }
    public void setImportToken(String importToken) { this.importToken = importToken; }

    public static class ImportResultVOBuilder {
        private Integer total;
        private Integer success;
        private Integer fail;
        private List<String> errors;
        private Long costTime;
        private List<FailedRowDetail> failedRows;
        private Map<String, Integer> errorSummary;
        private String importMode;
        private String importToken;

        public ImportResultVOBuilder total(Integer total) { this.total = total; return this; }
        public ImportResultVOBuilder success(Integer success) { this.success = success; return this; }
        public ImportResultVOBuilder fail(Integer fail) { this.fail = fail; return this; }
        public ImportResultVOBuilder errors(List<String> errors) { this.errors = errors; return this; }
        public ImportResultVOBuilder costTime(Long costTime) { this.costTime = costTime; return this; }
        public ImportResultVOBuilder failedRows(List<FailedRowDetail> failedRows) { this.failedRows = failedRows; return this; }
        public ImportResultVOBuilder errorSummary(Map<String, Integer> errorSummary) { this.errorSummary = errorSummary; return this; }
        public ImportResultVOBuilder importMode(String importMode) { this.importMode = importMode; return this; }
        public ImportResultVOBuilder importToken(String importToken) { this.importToken = importToken; return this; }
        public ImportResultVO build() { return new ImportResultVO(total, success, fail, errors, costTime, failedRows, errorSummary, importMode, importToken); }
    }
}
