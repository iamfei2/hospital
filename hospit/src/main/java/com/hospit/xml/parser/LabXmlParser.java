package com.hospit.xml.parser;

import com.hospit.entity.LabItemDict;
import com.hospit.entity.LabResult;
import com.hospit.mapper.LabItemDictMapper;
import com.hospit.xml.LabPathMapping;
import com.hospit.xml.XmlPathMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LabXmlParser extends AbstractXmlParser<LabResult> {

    private final LabPathMapping pathMapping = new LabPathMapping();

    @Autowired
    private LabItemDictMapper labItemDictMapper;

    public LabXmlParser() {
    }

    public LabXmlParser(LabItemDictMapper labItemDictMapper) {
        this.labItemDictMapper = labItemDictMapper;
    }

    @Override
    protected XmlPathMapping getPathMapping() {
        return pathMapping;
    }

    @Override
    protected List<LabResult> parseRecords(Document doc) throws Exception {
        List<LabResult> allResults = new ArrayList<>();

        NodeList recordNodes = doc.getElementsByTagName("Record");
        if (recordNodes.getLength() == 0) {
            throw new XmlParseException("未找到Record节点，请检查XML格式");
        }

        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            LabResultParseContext ctx = parseRecordContext(record);
            
            if (ctx.patientId == null || ctx.patientId.trim().isEmpty()) {
                throw new XmlParseException("第" + (i + 1) + "条记录的Patient/ID不能为空");
            }
            if (ctx.reportTime == null) {
                throw new XmlParseException("第" + (i + 1) + "条记录的LabReport/ReportTime不能为空");
            }
            if (ctx.executeDept == null || ctx.executeDept.trim().isEmpty()) {
                throw new XmlParseException("第" + (i + 1) + "条记录的LabReport/ExecuteDept不能为空");
            }
            if (ctx.executeDoc == null || ctx.executeDoc.trim().isEmpty()) {
                throw new XmlParseException("第" + (i + 1) + "条记录的LabReport/ExecuteDoc不能为空");
            }

            List<LabResult> results = parseLabItems(record, ctx);
            allResults.addAll(results);
        }

        return allResults;
    }

    private LabResultParseContext parseRecordContext(Element record) {
        LabResultParseContext ctx = new LabResultParseContext();
        ctx.patientId = getElementText(record, LabPathMapping.PATIENT_ID);
        ctx.reportTime = parseDateTime(getElementText(record, LabPathMapping.REPORT_TIME));
        ctx.executeDept = getElementText(record, LabPathMapping.EXECUTE_DEPT);
        ctx.executeDoc = getElementText(record, LabPathMapping.EXECUTE_DOC);
        return ctx;
    }

    private List<LabResult> parseLabItems(Element record, LabResultParseContext ctx) {
        List<LabResult> results = new ArrayList<>();

        NodeList itemNodes = record.getElementsByTagName("Item");
        if (itemNodes.getLength() == 0) {
            throw new XmlParseException("记录中未找到Item节点，请检查XML格式");
        }

        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element item = (Element) itemNodes.item(i);
            LabResult lr = new LabResult();
            lr.setPatientId(ctx.patientId);
            lr.setReportTime(ctx.reportTime);
            lr.setExecuteDept(ctx.executeDept);
            lr.setExecuteDoc(ctx.executeDoc);

            String itemCode = getElementText(item, LabPathMapping.ITEM_CODE);
            String itemName = getElementText(item, LabPathMapping.ITEM_NAME);

            Integer itemId = resolveItemId(itemCode, itemName);
            if (itemId == null) {
                String foundBy = itemCode != null ? "代码[" + itemCode + "]" : "名称[" + itemName + "]";
                throw new XmlParseException("无效的检验项目" + foundBy + "，请检查项目代码或名称");
            }
            lr.setItemId(itemId);

            String resultValueStr = getElementText(item, LabPathMapping.RESULT_VALUE);
            if (resultValueStr == null || resultValueStr.trim().isEmpty()) {
                throw new XmlParseException("Item中ResultValue不能为空");
            }
            try {
                lr.setResultValue(new BigDecimal(resultValueStr.trim()));
            } catch (NumberFormatException e) {
                throw new XmlParseException("无效的数值格式: " + resultValueStr);
            }

            String unit = getElementText(item, LabPathMapping.RESULT_UNIT);
            lr.setResultUnit(unit != null && !unit.trim().isEmpty() ? unit.trim() : null);

            lr.setCreateTime(java.time.LocalDateTime.now());
            lr.setIsInvalid(false);

            results.add(lr);
        }

        return results;
    }

    private Integer resolveItemId(String itemCode, String itemName) {
        if (labItemDictMapper == null) {
            return null;
        }
        
        if (itemCode != null && !itemCode.trim().isEmpty()) {
            LabItemDict dict = labItemDictMapper.selectByCode(itemCode.trim());
            if (dict != null) {
                return dict.getItemId();
            }
        }
        
        if (itemName != null && !itemName.trim().isEmpty()) {
            LabItemDict dict = labItemDictMapper.selectByName(itemName.trim());
            if (dict != null) {
                return dict.getItemId();
            }
        }
        
        return null;
    }

    @Override
    protected int getTotalCount(Document doc) {
        int count = 0;
        NodeList recordNodes = doc.getElementsByTagName("Record");
        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            NodeList itemNodes = record.getElementsByTagName("Item");
            count += itemNodes.getLength();
        }
        return count;
    }

    @Override
    public Class<LabResult> getEntityClass() {
        return LabResult.class;
    }

    private static class LabResultParseContext {
        String patientId;
        java.time.LocalDateTime reportTime;
        String executeDept;
        String executeDoc;
    }
}
