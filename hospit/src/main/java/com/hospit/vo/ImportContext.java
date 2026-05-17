package com.hospit.vo;

import com.hospit.listener.CtExcelImportListener;
import com.hospit.listener.EnteroscopyExcelImportListener;
import com.hospit.listener.MriExcelImportListener;
import com.hospit.listener.PathologyExcelImportListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImportContext {
    private String importToken;
    private String importMode;
    private String importType;
    private int totalRows;
    private int successCount;
    private int failCount;
    private long startTime;
    private List<FailedRowDetail> failedRows = new ArrayList<>();
    private Map<String, Integer> errorSummary = new HashMap<>();
    private List<Object> dataList = new ArrayList<>();
    private transient CtExcelImportListener ctListener;
    private transient MriExcelImportListener mriListener;
    private transient PathologyExcelImportListener pathologyListener;
    private transient EnteroscopyExcelImportListener enteroscopyListener;

    public String getImportToken() { return importToken; }
    public void setImportToken(String importToken) { this.importToken = importToken; }
    public String getImportMode() { return importMode; }
    public void setImportMode(String importMode) { this.importMode = importMode; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public List<FailedRowDetail> getFailedRows() { return failedRows; }
    public void setFailedRows(List<FailedRowDetail> failedRows) { this.failedRows = failedRows; }
    public Map<String, Integer> getErrorSummary() { return errorSummary; }
    public void setErrorSummary(Map<String, Integer> errorSummary) { this.errorSummary = errorSummary; }
    public List<Object> getDataList() { return dataList; }
    public void setDataList(List<Object> dataList) { this.dataList = dataList; }
    public CtExcelImportListener getCtListener() { return ctListener; }
    public void setCtListener(CtExcelImportListener ctListener) { this.ctListener = ctListener; }
    public MriExcelImportListener getMriListener() { return mriListener; }
    public void setMriListener(MriExcelImportListener mriListener) { this.mriListener = mriListener; }
    public PathologyExcelImportListener getPathologyListener() { return pathologyListener; }
    public void setPathologyListener(PathologyExcelImportListener pathologyListener) { this.pathologyListener = pathologyListener; }
    public EnteroscopyExcelImportListener getEnteroscopyListener() { return enteroscopyListener; }
    public void setEnteroscopyListener(EnteroscopyExcelImportListener enteroscopyListener) { this.enteroscopyListener = enteroscopyListener; }

    public static ImportContext create(String mode, String type) {
        ImportContext ctx = new ImportContext();
        ctx.setImportToken(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        ctx.setImportMode(mode);
        ctx.setImportType(type);
        ctx.setStartTime(System.currentTimeMillis());
        return ctx;
    }

    public void addFailedRow(int rowIndex, String rawData, List<String> errors) {
        FailedRowDetail detail = new FailedRowDetail();
        detail.setRowIndex(rowIndex);
        detail.setRawData(rawData != null && rawData.length() > 2000 ? rawData.substring(0, 2000) : rawData);
        detail.setErrors(errors);
        failedRows.add(detail);
        failCount++;
        for (String err : errors) {
            errorSummary.merge(err, 1, Integer::sum);
        }
    }

    public void incrementSuccess() { successCount++; }
}
