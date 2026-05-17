package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 患者信息表 Mapper 接口
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

}
