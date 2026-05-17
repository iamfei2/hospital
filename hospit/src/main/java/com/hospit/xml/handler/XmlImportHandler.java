package com.hospit.xml.handler;

import com.hospit.entity.*;
import com.hospit.mapper.*;
import com.hospit.service.*;
import com.hospit.xml.*;
import com.hospit.xml.parser.*;
import com.hospit.xml.result.XmlImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class XmlImportHandler {

    @Autowired
    private LabItemDictMapper labItemDictMapper;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private ICtExaminationService ctExaminationService;

    @Autowired
    private IMriExaminationService mriExaminationService;

    @Autowired
    private IPathologyExaminationService pathologyExaminationService;

    @Autowired
    private IEnteroscopyExaminationService enteroscopyExaminationService;

    @Autowired
    private ILabResultService labResultService;

    public XmlImportResult importXml(MultipartFile file, String type) {
        XmlTypeEnum xmlType = XmlTypeEnum.fromCode(type);
        if (xmlType == null) {
            return XmlImportResult.failure("INVALID_TYPE", "不支持的类型: " + type);
        }

        long startTime = System.currentTimeMillis();
        
        try {
            switch (xmlType) {
                case CT:
                    return importCt(file, startTime);
                case MRI:
                    return importMri(file, startTime);
                case PATHOLOGY:
                    return importPathology(file, startTime);
                case ENTEROSCOPY:
                    return importEnteroscopy(file, startTime);
                case LAB:
                    return importLab(file, startTime);
                default:
                    return XmlImportResult.failure("INVALID_TYPE", "不支持的类型: " + type);
            }
        } catch (AbstractXmlParser.XmlParseException e) {
            XmlImportResult result = XmlImportResult.failure("PARSE_ERROR", e.getMessage());
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            XmlImportResult result = XmlImportResult.failure("SYSTEM_ERROR", "导入失败: " + e.getMessage());
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    private XmlImportResult importCt(MultipartFile file, long startTime) {
        CtXmlParser parser = new CtXmlParser();
        List<CtExamination> entities = parser.parseToEntities(file);
        
        String patientId = entities.isEmpty() ? null : entities.get(0).getPatientId();
        if (!validatePatient(patientId, startTime)) {
            return XmlImportResult.patientNotFound(patientId);
        }
        
        int success = saveCtEntities(entities);
        XmlImportResult result = XmlImportResult.success(entities.size(), success, parser.buildItemSummary(entities));
        result.setCostTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private XmlImportResult importMri(MultipartFile file, long startTime) {
        MriXmlParser parser = new MriXmlParser();
        List<MriExamination> entities = parser.parseToEntities(file);
        
        String patientId = entities.isEmpty() ? null : entities.get(0).getPatientId();
        if (!validatePatient(patientId, startTime)) {
            return XmlImportResult.patientNotFound(patientId);
        }
        
        int success = saveMriEntities(entities);
        XmlImportResult result = XmlImportResult.success(entities.size(), success, parser.buildItemSummary(entities));
        result.setCostTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private XmlImportResult importPathology(MultipartFile file, long startTime) {
        PathologyXmlParser parser = new PathologyXmlParser();
        List<PathologyExamination> entities = parser.parseToEntities(file);
        
        String patientId = entities.isEmpty() ? null : entities.get(0).getPatientId();
        if (!validatePatient(patientId, startTime)) {
            return XmlImportResult.patientNotFound(patientId);
        }
        
        int success = savePathologyEntities(entities);
        XmlImportResult result = XmlImportResult.success(entities.size(), success, parser.buildItemSummary(entities));
        result.setCostTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private XmlImportResult importEnteroscopy(MultipartFile file, long startTime) {
        EnteroscopyXmlParser parser = new EnteroscopyXmlParser();
        List<EnteroscopyExamination> entities = parser.parseToEntities(file);
        
        String patientId = entities.isEmpty() ? null : entities.get(0).getPatientId();
        if (!validatePatient(patientId, startTime)) {
            return XmlImportResult.patientNotFound(patientId);
        }
        
        int success = saveEnteroscopyEntities(entities);
        XmlImportResult result = XmlImportResult.success(entities.size(), success, parser.buildItemSummary(entities));
        result.setCostTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private XmlImportResult importLab(MultipartFile file, long startTime) {
        LabXmlParser parser = new LabXmlParser(labItemDictMapper);
        List<LabResult> entities = parser.parseToEntities(file);
        
        if (entities.isEmpty()) {
            XmlImportResult result = XmlImportResult.failure("NO_DATA", "未解析到任何检验记录");
            result.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        }
        
        String patientId = entities.get(0).getPatientId();
        if (!validatePatient(patientId, startTime)) {
            return XmlImportResult.patientNotFound(patientId);
        }
        
        int success = saveLabEntities(entities);
        XmlImportResult result = XmlImportResult.success(entities.size(), success, parser.buildItemSummary(entities));
        result.setCostTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private boolean validatePatient(String patientId, long startTime) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return false;
        }
        return patientService.existsByPatientId(patientId.trim());
    }

    private int saveCtEntities(List<CtExamination> entities) {
        ctExaminationService.saveBatch(entities, 500);
        return entities.size();
    }

    private int saveMriEntities(List<MriExamination> entities) {
        mriExaminationService.saveBatch(entities, 500);
        return entities.size();
    }

    private int savePathologyEntities(List<PathologyExamination> entities) {
        pathologyExaminationService.saveBatch(entities, 500);
        return entities.size();
    }

    private int saveEnteroscopyEntities(List<EnteroscopyExamination> entities) {
        enteroscopyExaminationService.saveBatch(entities, 500);
        return entities.size();
    }

    private int saveLabEntities(List<LabResult> entities) {
        labResultService.saveBatch(entities, 500);
        labResultService.evaluateAndSaveWarningsBatch(entities);
        return entities.size();
    }
}
