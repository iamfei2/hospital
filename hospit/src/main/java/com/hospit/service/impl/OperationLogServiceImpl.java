package com.hospit.service.impl;

import com.hospit.entity.OperationLog;
import com.hospit.mapper.OperationLogMapper;
import com.hospit.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 操作日志表（全量操作留痕） 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

    // 更新操作日志（不支持）
    @Override
    public boolean updateById(OperationLog entity) {
        throw new UnsupportedOperationException("操作日志不支持修改操作，保障日志记录的不可篡改性");
    }

    // 删除操作日志（不支持）
    @Override
    public boolean deleteById(Serializable id) {
        throw new UnsupportedOperationException("操作日志不支持删除操作，保障日志记录的不可篡改性");
    }

    // 批量删除操作日志（不支持）
    @Override
    public boolean removeByIds(Collection<?> ids) {
        throw new UnsupportedOperationException("操作日志不支持批量删除操作，保障日志记录的不可篡改性");
    }
}
