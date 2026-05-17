package com.hospit.vo;

import java.util.HashMap;
import java.util.Map;

public class HeaderMapping {

    private final Map<Integer, String> columnToField = new HashMap<>();
    private final Map<String, Integer> fieldToColumn = new HashMap<>();

    public void put(int columnIndex, String fieldName) {
        columnToField.put(columnIndex, fieldName);
        fieldToColumn.put(fieldName, columnIndex);
    }

    public String getField(int columnIndex) {
        return columnToField.get(columnIndex);
    }

    public Integer getColumn(String fieldName) {
        return fieldToColumn.get(fieldName);
    }

    public boolean containsField(String fieldName) {
        return fieldToColumn.containsKey(fieldName);
    }

    public boolean containsColumn(int columnIndex) {
        return columnToField.containsKey(columnIndex);
    }

    public Map<Integer, String> getColumnToField() {
        return columnToField;
    }

    public int size() {
        return columnToField.size();
    }
}