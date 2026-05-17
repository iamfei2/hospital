package com.hospit.xml.parser;

import com.hospit.xml.result.XmlImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface XmlParser<T> {
    
    XmlImportResult parse(MultipartFile file);
    
    List<T> parseToEntities(MultipartFile file);
    
    Class<T> getEntityClass();
}
