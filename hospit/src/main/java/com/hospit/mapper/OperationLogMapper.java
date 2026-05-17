package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 操作日志表（全量操作留痕） Mapper 接口
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
