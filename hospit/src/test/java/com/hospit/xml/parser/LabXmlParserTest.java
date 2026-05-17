package com.hospit.xml.parser;

import com.hospit.entity.LabItemDict;
import com.hospit.entity.LabResult;
import com.hospit.mapper.LabItemDictMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LabXmlParserTest {

    @Mock
    private LabItemDictMapper labItemDictMapper;

    private LabXmlParser labXmlParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        labXmlParser = new LabXmlParser(labItemDictMapper);
    }

    @Test
    void testParseValidXml() {
        LabItemDict gammaGt = new LabItemDict();
        gammaGt.setItemId(1);
        gammaGt.setItemCode("GAMMA_GT");
        gammaGt.setItemName("γ-谷氨酰转移酶");
        when(labItemDictMapper.selectByCode("GAMMA_GT")).thenReturn(gammaGt);

        LabItemDict alt = new LabItemDict();
        alt.setItemId(2);
        alt.setItemCode("ALT");
        alt.setItemName("谷丙转氨酶");
        when(labItemDictMapper.selectByCode("ALT")).thenReturn(alt);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LabImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID>P001</ID>\n" +
                "        <Name>张三</Name>\n" +
                "      </Patient>\n" +
                "      <LabReport>\n" +
                "        <ReportTime>2026-03-01 10:00:00</ReportTime>\n" +
                "        <ExecuteDept>检验科</ExecuteDept>\n" +
                "        <ExecuteDoc>王医生</ExecuteDoc>\n" +
                "      </LabReport>\n" +
                "      <LabItems>\n" +
                "        <Item>\n" +
                "          <ItemCode>GAMMA_GT</ItemCode>\n" +
                "          <ItemName>γ-谷氨酰转移酶</ItemName>\n" +
                "          <ResultValue>45.6</ResultValue>\n" +
                "          <ResultUnit>U/L</ResultUnit>\n" +
                "        </Item>\n" +
                "        <Item>\n" +
                "          <ItemCode>ALT</ItemCode>\n" +
                "          <ItemName>谷丙转氨酶</ItemName>\n" +
                "          <ResultValue>32.5</ResultValue>\n" +
                "          <ResultUnit>U/L</ResultUnit>\n" +
                "        </Item>\n" +
                "      </LabItems>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</LabImport>";

        MockMultipartFile file = new MockMultipartFile("file", "lab.xml", "text/xml", xml.getBytes());
        List<LabResult> results = labXmlParser.parseToEntities(file);

        assertEquals(2, results.size());

        LabResult result1 = results.get(0);
        assertEquals("P001", result1.getPatientId());
        assertEquals(1, result1.getItemId());
        assertEquals(new BigDecimal("45.6"), result1.getResultValue());
        assertEquals("U/L", result1.getResultUnit());
        assertNotNull(result1.getReportTime());

        LabResult result2 = results.get(1);
        assertEquals("P001", result2.getPatientId());
        assertEquals(2, result2.getItemId());
        assertEquals(new BigDecimal("32.5"), result2.getResultValue());
    }

    @Test
    void testParseXmlWithEmptyPatientId() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LabImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID></ID>\n" +
                "      </Patient>\n" +
                "      <LabReport>\n" +
                "        <ReportTime>2026-03-01 10:00:00</ReportTime>\n" +
                "        <ExecuteDept>检验科</ExecuteDept>\n" +
                "        <ExecuteDoc>王医生</ExecuteDoc>\n" +
                "      </LabReport>\n" +
                "      <LabItems>\n" +
                "        <Item>\n" +
                "          <ItemCode>ALT</ItemCode>\n" +
                "          <ResultValue>32.5</ResultValue>\n" +
                "        </Item>\n" +
                "      </LabItems>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</LabImport>";

        MockMultipartFile file = new MockMultipartFile("file", "lab.xml", "text/xml", xml.getBytes());

        assertThrows(AbstractXmlParser.XmlParseException.class, () -> {
            labXmlParser.parseToEntities(file);
        });
    }

    @Test
    void testParseXmlWithInvalidItemCode() {
        when(labItemDictMapper.selectByCode(anyString())).thenReturn(null);
        when(labItemDictMapper.selectByName(anyString())).thenReturn(null);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LabImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID>P001</ID>\n" +
                "      </Patient>\n" +
                "      <LabReport>\n" +
                "        <ReportTime>2026-03-01 10:00:00</ReportTime>\n" +
                "        <ExecuteDept>检验科</ExecuteDept>\n" +
                "        <ExecuteDoc>王医生</ExecuteDoc>\n" +
                "      </LabReport>\n" +
                "      <LabItems>\n" +
                "        <Item>\n" +
                "          <ItemCode>INVALID</ItemCode>\n" +
                "          <ResultValue>32.5</ResultValue>\n" +
                "        </Item>\n" +
                "      </LabItems>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</LabImport>";

        MockMultipartFile file = new MockMultipartFile("file", "lab.xml", "text/xml", xml.getBytes());

        assertThrows(AbstractXmlParser.XmlParseException.class, () -> {
            labXmlParser.parseToEntities(file);
        });
    }

    @Test
    void testParseXmlWithMissingResultValue() {
        LabItemDict alt = new LabItemDict();
        alt.setItemId(2);
        alt.setItemCode("ALT");
        when(labItemDictMapper.selectByCode("ALT")).thenReturn(alt);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LabImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID>P001</ID>\n" +
                "      </Patient>\n" +
                "      <LabReport>\n" +
                "        <ReportTime>2026-03-01 10:00:00</ReportTime>\n" +
                "        <ExecuteDept>检验科</ExecuteDept>\n" +
                "        <ExecuteDoc>王医生</ExecuteDoc>\n" +
                "      </LabReport>\n" +
                "      <LabItems>\n" +
                "        <Item>\n" +
                "          <ItemCode>ALT</ItemCode>\n" +
                "          <ResultValue></ResultValue>\n" +
                "        </Item>\n" +
                "      </LabItems>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</LabImport>";

        MockMultipartFile file = new MockMultipartFile("file", "lab.xml", "text/xml", xml.getBytes());

        assertThrows(AbstractXmlParser.XmlParseException.class, () -> {
            labXmlParser.parseToEntities(file);
        });
    }

    @Test
    void testParseXmlWithUnitFallback() {
        LabItemDict alt = new LabItemDict();
        alt.setItemId(2);
        alt.setItemCode("ALT");
        alt.setDefaultUnit("U/L");
        when(labItemDictMapper.selectByCode("ALT")).thenReturn(alt);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<LabImport>\n" +
                "  <Records>\n" +
                "    <Record>\n" +
                "      <Patient>\n" +
                "        <ID>P001</ID>\n" +
                "      </Patient>\n" +
                "      <LabReport>\n" +
                "        <ReportTime>2026-03-01 10:00:00</ReportTime>\n" +
                "        <ExecuteDept>检验科</ExecuteDept>\n" +
                "        <ExecuteDoc>王医生</ExecuteDoc>\n" +
                "      </LabReport>\n" +
                "      <LabItems>\n" +
                "        <Item>\n" +
                "          <ItemCode>ALT</ItemCode>\n" +
                "          <ResultValue>32.5</ResultValue>\n" +
                "          <ResultUnit></ResultUnit>\n" +
                "        </Item>\n" +
                "      </LabItems>\n" +
                "    </Record>\n" +
                "  </Records>\n" +
                "</LabImport>";

        MockMultipartFile file = new MockMultipartFile("file", "lab.xml", "text/xml", xml.getBytes());
        List<LabResult> results = labXmlParser.parseToEntities(file);

        assertEquals(1, results.size());
        assertNull(results.get(0).getResultUnit());
    }
}