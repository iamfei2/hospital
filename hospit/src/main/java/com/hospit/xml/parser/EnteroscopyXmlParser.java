package com.hospit.xml.parser;

import com.hospit.entity.EnteroscopyExamination;
import com.hospit.xml.EnteroscopyPathMapping;
import com.hospit.xml.XmlPathMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnteroscopyXmlParser extends AbstractXmlParser<EnteroscopyExamination> {

    private final EnteroscopyPathMapping pathMapping = new EnteroscopyPathMapping();

    @Override
    protected XmlPathMapping getPathMapping() {
        return pathMapping;
    }

    @Override
    protected List<EnteroscopyExamination> parseRecords(Document doc) throws Exception {
        List<EnteroscopyExamination> results = new ArrayList<>();

        NodeList recordNodes = doc.getElementsByTagName("Record");
        if (recordNodes.getLength() == 0) {
            throw new XmlParseException("未找到Record节点，请检查XML格式");
        }

        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            EnteroscopyExamination enteroscopy = parseRecord(record, i);
            results.add(enteroscopy);
        }

        return results;
    }

    private EnteroscopyExamination parseRecord(Element record, int index) {
        EnteroscopyExamination enteroscopy = new EnteroscopyExamination();

        String patientId = getElementText(record, EnteroscopyPathMapping.PATIENT_ID);
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new XmlParseException("第" + (index + 1) + "条记录的Patient/ID不能为空");
        }
        enteroscopy.setPatientId(patientId.trim());

        enteroscopy.setExaminationNo(getElementText(record, EnteroscopyPathMapping.EXAMINATION_NO));
        enteroscopy.setExaminationTime(parseDateTime(getElementText(record, EnteroscopyPathMapping.EXAMINATION_TIME)));
        enteroscopy.setEnteroscopyType(getElementText(record, EnteroscopyPathMapping.ENTEROSCOPY_TYPE));
        enteroscopy.setExamineDoctor(getElementText(record, EnteroscopyPathMapping.EXAMINE_DOCTOR));
        enteroscopy.setExamineDept(getElementText(record, EnteroscopyPathMapping.EXAMINE_DEPT));
        enteroscopy.setReportConclusion(getElementText(record, EnteroscopyPathMapping.REPORT_CONCLUSION));

        enteroscopy.setUserId(1);
        enteroscopy.setUploadTime(LocalDateTime.now());
        enteroscopy.setIsInvalid(false);
        enteroscopy.setCreateTime(LocalDateTime.now());

        return enteroscopy;
    }

    @Override
    protected int getTotalCount(Document doc) {
        return doc.getElementsByTagName("Record").getLength();
    }

    @Override
    public Class<EnteroscopyExamination> getEntityClass() {
        return EnteroscopyExamination.class;
    }
}
