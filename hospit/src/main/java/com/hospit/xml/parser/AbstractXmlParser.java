package com.hospit.xml.parser;

import com.hospit.xml.XmlPathMapping;
import com.hospit.xml.result.XmlImportResult;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * XML解析器基类 - 使用DOM方式解析XML
 * 支持多日期格式自适应、任意深度路径查询
 */
public abstract class AbstractXmlParser<T> implements XmlParser<T> {

    // 支持的日期格式：2024-01-01 10:00:00 / 2024/01/01 10:00:00 / 2024-01-01 / 2024/01/01
    protected static final Pattern DATE_PATTERN_1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s*\\d{2}:\\d{2}:\\d{2}");
    protected static final Pattern DATE_PATTERN_2 = Pattern.compile("\\d{4}/\\d{2}/\\d{2}\\s*\\d{2}:\\d{2}:\\d{2}");
    protected static final Pattern DATE_PATTERN_3 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    protected static final Pattern DATE_PATTERN_4 = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");

    /** 获取XML路径映射配置 */
    protected abstract XmlPathMapping getPathMapping();
    
    /** 解析XML记录列表 */
    protected abstract List<T> parseRecords(Document doc) throws Exception;
    
    /** 获取总记录数 */
    protected abstract int getTotalCount(Document doc);

    @Override
    public XmlImportResult parse(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            String xmlContent = new String(file.getBytes(), "UTF-8");
            Document doc = parseXml(xmlContent);
            
            List<T> entities = parseRecords(doc);
            int total = getTotalCount(doc);
            
            XmlImportResult result = XmlImportResult.success(total, entities.size(), buildItemSummary(entities));
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (XmlParseException e) {
            XmlImportResult result = XmlImportResult.failure("PARSE_ERROR", e.getMessage());
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            XmlImportResult result = XmlImportResult.failure("SYSTEM_ERROR", "XML解析失败: " + e.getMessage());
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    @Override
    public List<T> parseToEntities(MultipartFile file) {
        try {
            String xmlContent = new String(file.getBytes(), "UTF-8");
            Document doc = parseXml(xmlContent);
            return parseRecords(doc);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("XML解析失败: " + e.getMessage(), e);
        }
    }

    protected Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlContent)));
    }

    protected String getElementText(Element parent, String childPath) {
        String[] parts = childPath.split("/");
        Element current = parent;
        
        for (int i = 0; i < parts.length - 1; i++) {
            NodeList children = current.getElementsByTagName(parts[i]);
            if (children.getLength() == 0) {
                return null;
            }
            current = (Element) children.item(0);
        }
        
        String tagName = parts[parts.length - 1];
        NodeList nodes = current.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    protected LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception ignored) {
            }
        }
        
        if (DATE_PATTERN_1.matcher(dateStr).matches()) {
            return LocalDateTime.parse(dateStr.replace("/", "-"));
        }
        if (DATE_PATTERN_2.matcher(dateStr).matches()) {
            return LocalDateTime.parse(dateStr.replace("/", "-"));
        }
        if (DATE_PATTERN_3.matcher(dateStr).matches()) {
            return LocalDateTime.parse(dateStr);
        }
        if (DATE_PATTERN_4.matcher(dateStr).matches()) {
            return LocalDateTime.parse(dateStr.replace("/", "-"));
        }
        
        return null;
    }

    protected List<Element> getChildElements(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getElementsByTagName(tagName);
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                if (child.getParentNode().equals(parent)) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    public String buildItemSummary(List<T> entities) {
        return "共 " + entities.size() + " 条记录";
    }

    public static class XmlParseException extends RuntimeException {
        public XmlParseException(String message) {
            super(message);
        }
        
        public XmlParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
