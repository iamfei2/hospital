package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 附件存储表（CT影像/病历原图等） Mapper 接口
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

}
