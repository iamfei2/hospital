package com.hospit.xml.parser;

import com.hospit.entity.CtExamination;
import com.hospit.xml.ExaminationPathMapping;
import com.hospit.xml.XmlPathMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CtXmlParser extends AbstractXmlParser<CtExamination> {

    private final ExaminationPathMapping pathMapping = new ExaminationPathMapping();

    @Override
    protected XmlPathMapping getPathMapping() {
        return pathMapping;
    }

    @Override
    protected List<CtExamination> parseRecords(Document doc) throws Exception {
        List<CtExamination> results = new ArrayList<>();

        NodeList recordNodes = doc.getElementsByTagName("Record");
        if (recordNodes.getLength() == 0) {
            throw new XmlParseException("未找到Record节点，请检查XML格式");
        }

        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            CtExamination ct = parseRecord(record, i);
            results.add(ct);
        }

        return results;
    }

    private CtExamination parseRecord(Element record, int index) {
        CtExamination ct = new CtExamination();

        String patientId = getElementText(record, ExaminationPathMapping.PATIENT_ID);
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new XmlParseException("第" + (index + 1) + "条记录的Patient/ID不能为空");
        }
        ct.setPatientId(patientId.trim());

        ct.setExaminationNo(getElementText(record, ExaminationPathMapping.EXAMINATION_NO));
        ct.setExaminationTime(parseDateTime(getElementText(record, ExaminationPathMapping.EXAMINATION_TIME)));
        ct.setExaminationPart(getElementText(record, ExaminationPathMapping.EXAMINATION_PART));
        ct.setExamineDoctor(getElementText(record, ExaminationPathMapping.EXAMINE_DOCTOR));
        ct.setExamineDept(getElementText(record, ExaminationPathMapping.EXAMINE_DEPT));
        ct.setReportConclusion(getElementText(record, ExaminationPathMapping.REPORT_CONCLUSION));

        ct.setUserId(1);
        ct.setUploadTime(LocalDateTime.now());
        ct.setIsInvalid(false);
        ct.setCreateTime(LocalDateTime.now());

        return ct;
    }

    @Override
    protected int getTotalCount(Document doc) {
        return doc.getElementsByTagName("Record").getLength();
    }

    @Override
    public Class<CtExamination> getEntityClass() {
        return CtExamination.class;
    }
}
