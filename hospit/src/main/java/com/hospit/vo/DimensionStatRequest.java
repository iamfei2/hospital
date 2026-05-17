package com.hospit.vo;

import java.util.List;
import java.util.Map;

public class DimensionStatRequest {
    private List<String> dimensions;
    private List<String> tables;
    private String startTime;
    private String endTime;
    private Map<String, String> filters;

    public List<String> getDimensions() { return dimensions; }
    public void setDimensions(List<String> dimensions) { this.dimensions = dimensions; }
    public List<String> getTables() { return tables; }
    public void setTables(List<String> tables) { this.tables = tables; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Map<String, String> getFilters() { return filters; }
    public void setFilters(Map<String, String> filters) { this.filters = filters; }
}
