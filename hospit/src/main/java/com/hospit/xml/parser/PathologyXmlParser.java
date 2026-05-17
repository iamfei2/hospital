package com.hospit.xml.parser;

import com.hospit.entity.PathologyExamination;
import com.hospit.xml.PathologyPathMapping;
import com.hospit.xml.XmlPathMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PathologyXmlParser extends AbstractXmlParser<PathologyExamination> {

    private final PathologyPathMapping pathMapping = new PathologyPathMapping();

    @Override
    protected XmlPathMapping getPathMapping() {
        return pathMapping;
    }

    @Override
    protected List<PathologyExamination> parseRecords(Document doc) throws Exception {
        List<PathologyExamination> results = new ArrayList<>();

        NodeList recordNodes = doc.getElementsByTagName("Record");
        if (recordNodes.getLength() == 0) {
            throw new XmlParseException("未找到Record节点，请检查XML格式");
        }

        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element record = (Element) recordNodes.item(i);
            PathologyExamination pathology = parseRecord(record, i);
            results.add(pathology);
        }

        return results;
    }

    private PathologyExamination parseRecord(Element record, int index) {
        PathologyExamination pathology = new PathologyExamination();

        String patientId = getElementText(record, PathologyPathMapping.PATIENT_ID);
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new XmlParseException("第" + (index + 1) + "条记录的Patient/ID不能为空");
        }
        pathology.setPatientId(patientId.trim());

        String pathologyNo = getElementText(record, PathologyPathMapping.PATHOLOGY_NO);
        if (pathologyNo == null || pathologyNo.trim().isEmpty()) {
            throw new XmlParseException("第" + (index + 1) + "条记录的Specimen/PathologyNo不能为空");
        }
        pathology.setPathologyNo(pathologyNo.trim());

        pathology.setSpecimenType(getElementText(record, PathologyPathMapping.SPECIMEN_TYPE));
        pathology.setSamplingTime(parseDateTime(getElementText(record, PathologyPathMapping.SAMPLING_TIME)));
        pathology.setPathologyDoctor(getElementText(record, PathologyPathMapping.PATHOLOGY_DOCTOR));
        pathology.setPathologyDept(getElementText(record, PathologyPathMapping.PATHOLOGY_DEPT));
        pathology.setPathologyDiagnosis(getElementText(record, PathologyPathMapping.PATHOLOGY_DIAGNOSIS));

        pathology.setUserId(1);
        pathology.setUploadTime(LocalDateTime.now());
        pathology.setIsInvalid(false);
        pathology.setCreateTime(LocalDateTime.now());

        return pathology;
    }

    @Override
    protected int getTotalCount(Document doc) {
        return doc.getElementsByTagName("Record").getLength();
    }

    @Override
    public Class<PathologyExamination> getEntityClass() {
        return PathologyExamination.class;
    }
}
