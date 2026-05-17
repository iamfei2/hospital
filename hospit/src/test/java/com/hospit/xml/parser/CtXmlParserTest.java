package com.hospit.xml.parser;

import com.hospit.entity.CtExamination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CtXmlParserTest {

    private CtXmlParser ctXmlParser;

    @BeforeEach
    void setUp() {
        ctXmlParser = new CtXmlParser();
    }

    @Test
    void testParseValidXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ExaminationImport>\n" +
                "  <Type>ct</Type>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID>P001</ID>\n" +
                "        <Name>张三</Name>\n" +
                "      </Patient>\n" +
                "      <ExaminationInfo>\n" +
                "        <ExamNo>CT20260301001</ExamNo>\n" +
                "        <ExamTime>2026-03-01 10:30:00</ExamTime>\n" +
                "        <Part>胸部</Part>\n" +
                "      </ExaminationInfo>\n" +
                "      <Report>\n" +
                "        <Doctor>李医生</Doctor>\n" +
                "        <Dept>放射科</Dept>\n" +
                "        <Conclusion>未见明显异常</Conclusion>\n" +
                "      </Report>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</ExaminationImport>";

        MockMultipartFile file = new MockMultipartFile("file", "ct.xml", "text/xml", xml.getBytes());
        List<CtExamination> results = ctXmlParser.parseToEntities(file);

        assertEquals(1, results.size());
        CtExamination ct = results.get(0);
        assertEquals("P001", ct.getPatientId());
        assertEquals("CT20260301001", ct.getExaminationNo());
        assertEquals("胸部", ct.getExaminationPart());
        assertEquals("李医生", ct.getExamineDoctor());
        assertEquals("放射科", ct.getExamineDept());
        assertEquals("未见明显异常", ct.getReportConclusion());
        assertNotNull(ct.getExaminationTime());
        assertNotNull(ct.getCreateTime());
        assertFalse(ct.getIsInvalid());
    }

    @Test
    void testParseMultipleRecords() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ExaminationImport>\n" +
                "  <Type>ct</Type>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient><ID>P001</ID></Patient>\n" +
                "      <ExaminationInfo>\n" +
                "        <ExamNo>CT001</ExamNo>\n" +
                "        <ExamTime>2026-03-01 10:00:00</ExamTime>\n" +
                "        <Part>胸部</Part>\n" +
                "      </ExaminationInfo>\n" +
                "      <Report><Doctor>医生1</Doctor><Dept>放射科</Dept></Report>\n" +
                "    </Record>\n" +
                "    <Record>\n" +
                "      <Patient><ID>P002</ID></Patient>\n" +
                "      <ExaminationInfo>\n" +
                "        <ExamNo>CT002</ExamNo>\n" +
                "        <ExamTime>2026-03-01 11:00:00</ExamTime>\n" +
                "        <Part>腹部</Part>\n" +
                "      </ExaminationInfo>\n" +
                "      <Report><Doctor>医生2</Doctor><Dept>放射科</Dept></Report>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</ExaminationImport>";

        MockMultipartFile file = new MockMultipartFile("file", "ct.xml", "text/xml", xml.getBytes());
        List<CtExamination> results = ctXmlParser.parseToEntities(file);

        assertEquals(2, results.size());
        assertEquals("P001", results.get(0).getPatientId());
        assertEquals("P002", results.get(1).getPatientId());
    }

    @Test
    void testParseXmlWithMissingPatientId() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ExaminationImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient><ID></ID></Patient>\n" +
                "      <ExaminationInfo>\n" +
                "        <ExamNo>CT001</ExamNo>\n" +
                "        <ExamTime>2026-03-01 10:00:00</ExamTime>\n" +
                "        <Part>胸部</Part>\n" +
                "      </ExaminationInfo>\n" +
                "      <Report><Doctor>医生</Doctor><Dept>放射科</Dept></Report>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</ExaminationImport>";

        MockMultipartFile file = new MockMultipartFile("file", "ct.xml", "text/xml", xml.getBytes());

        assertThrows(AbstractXmlParser.XmlParseException.class, () -> {
            ctXmlParser.parseToEntities(file);
        });
    }

    @Test
    void testParseXmlWithDifferentDateFormats() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ExaminationImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient><ID>P001</ID></Patient>\n" +
                "      <ExaminationInfo>\n" +
                "        <ExamNo>CT001</ExamNo>\n" +
                "        <ExamTime>2026/03/01 10:00:00</ExamTime>\n" +
                "        <Part>胸部</Part>\n" +
                "      </ExaminationInfo>\n" +
                "      <Report><Doctor>医生</Doctor><Dept>放射科</Dept></Report>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</ExaminationImport>";

        MockMultipartFile file = new MockMultipartFile("file", "ct.xml", "text/xml", xml.getBytes());
        List<CtExamination> results = ctXmlParser.parseToEntities(file);

        assertEquals(1, results.size());
        assertNotNull(results.get(0).getExaminationTime());
    }

    @Test
    void testParseXmlWithNoRecords() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ExaminationImport>\n" +
                "  <Records>\n" +
                "  </Records>\n" +
                "</ExaminationImport>";

        MockMultipartFile file = new MockMultipartFile("file", "ct.xml", "text/xml", xml.getBytes());

        assertThrows(AbstractXmlParser.XmlParseException.class, () -> {
            ctXmlParser.parseToEntities(file);
        });
    }
}