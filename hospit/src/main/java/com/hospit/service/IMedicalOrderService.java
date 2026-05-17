package com.hospit.service;

import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.MedicalOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 医嘱信息表 服务类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
public interface IMedicalOrderService extends IService<MedicalOrder> {
    Result pageCustom(QueryPageParam queryPageParam);

    Result stopMedicalOrder(Long orderId, LocalDateTime endTime);
    Result addMedicalOrder(MedicalOrder medicalOrder);
}
