package com.hospit.vo;

import java.util.List;

public class FailedRowDetail {
    private int rowIndex;
    private String rawData;
    private List<String> errors;

    public FailedRowDetail() {}
    public FailedRowDetail(int rowIndex, String rawData, List<String> errors) {
        this.rowIndex = rowIndex;
        this.rawData = rawData;
        this.errors = errors;
    }

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
