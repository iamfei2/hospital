package com.hospit.service.impl;

import com.hospit.entity.Attachment;
import com.hospit.mapper.AttachmentMapper;
import com.hospit.service.IAttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 附件存储表（CT影像/病历原图等） 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

}
