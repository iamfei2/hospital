package com.hospit.xml;

import java.util.HashMap;
import java.util.Map;

public abstract class XmlPathMapping {
    
    protected final Map<String, String> pathToFieldMap = new HashMap<>();
    protected final Map<String, String> fieldToPathMap = new HashMap<>();

    protected void addMapping(String xmlPath, String fieldName) {
        pathToFieldMap.put(xmlPath, fieldName);
        fieldToPathMap.put(fieldName, xmlPath);
    }

    public String getFieldName(String xmlPath) {
        return pathToFieldMap.get(xmlPath);
    }

    public String getXmlPath(String fieldName) {
        return fieldToPathMap.get(fieldName);
    }

    public Map<String, String> getAllMappings() {
        return new HashMap<>(pathToFieldMap);
    }

    public abstract void initialize();
}
