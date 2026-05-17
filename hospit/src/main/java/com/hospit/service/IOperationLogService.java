package com.hospit.service;

import com.hospit.entity.OperationLog;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 操作日志表（全量操作留痕） 服务类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
public interface IOperationLogService extends IService<OperationLog> {

    boolean updateById(OperationLog entity);

    boolean deleteById(Serializable id);

    boolean removeByIds(Collection<?> ids);
}
