package com.hospit.service;

import com.hospit.vo.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface IImportService {

    ImportResultVO importCtExamination(MultipartFile file);

    ImportResultVO importMrtExamination(MultipartFile file);

    ImportResultVO importPathologyExamination(MultipartFile file);

    ImportResultVO importEnteroscopyExamination(MultipartFile file);

    ImportResultVO importWithMode(MultipartFile file, String type, String mode);
}
