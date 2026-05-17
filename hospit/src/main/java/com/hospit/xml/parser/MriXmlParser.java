package com.hospit.xml.parser;

import com.hospit.entity.MriExamination;
import com.hospit.xml.ExaminationPathMapping;
import com.hospit.xml.XmlPathMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MriXmlParser extends AbstractXmlParser<MriExamination> {

    private final ExaminationPathMapping pathMapping = new ExaminationPathMapping();

    @Override
    protected XmlPathMapping getPathMapping() {
        return pathMapping;
    }

    @Override
    protected List<MriExamination> parseRecords(Document doc) throws Exception {
        List<MriExamination> results = new ArrayList<>();

        NodeList recordNodes = doc.getElementsByTagName("Record");
        if (recordNodes.getLength() == 0) {
            throw new XmlParseException("未找到Record节点，请检查XML格式");
        }

        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            MriExamination mri = parseRecord(record, i);
            results.add(mri);
        }

        return results;
    }

    private MriExamination parseRecord(Element record, int index) {
        MriExamination mri = new MriExamination();

        String patientId = getElementText(record, ExaminationPathMapping.PATIENT_ID);
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new XmlParseException("第" + (index + 1) + "条记录的Patient/ID不能为空");
        }
        mri.setPatientId(patientId.trim());

        mri.setExaminationNo(getElementText(record, ExaminationPathMapping.EXAMINATION_NO));
        mri.setExaminationTime(parseDateTime(getElementText(record, ExaminationPathMapping.EXAMINATION_TIME)));
        mri.setExaminationPart(getElementText(record, ExaminationPathMapping.EXAMINATION_PART));
        mri.setExamineDoctor(getElementText(record, ExaminationPathMapping.EXAMINE_DOCTOR));
        mri.setExamineDept(getElementText(record, ExaminationPathMapping.EXAMINE_DEPT));
        mri.setReportConclusion(getElementText(record, ExaminationPathMapping.REPORT_CONCLUSION));

        mri.setUserId(1);
        mri.setUploadTime(LocalDateTime.now());
        mri.setIsInvalid(false);
        mri.setCreateTime(LocalDateTime.now());

        return mri;
    }

    @Override
    protected int getTotalCount(Document doc) {
        return doc.getElementsByTagName("Record").getLength();
    }

    @Override
    public Class<MriExamination> getEntityClass() {
        return MriExamination.class;
    }
}
