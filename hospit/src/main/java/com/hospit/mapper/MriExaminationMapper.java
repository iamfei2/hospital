package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.MriExamination;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface MriExaminationMapper extends BaseMapper<MriExamination> {

    // 查询已存在的检查编号
    @Select("<script>" +
            "SELECT examination_no FROM mri_examination WHERE is_invalid = 0 AND examination_no IN " +
            "<foreach collection='examinationNos' item='no' open='(' separator=',' close=')'>" +
            "#{no}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistExaminationNos(@Param("examinationNos") List<String> examinationNos);

    // 按日期范围统计数量
    Long countByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按日分组统计
    List<Map<String, Object>> countDailyGrouped(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按周分组统计
    List<Map<String, Object>> countWeeklyGrouped(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按月分组统计
    List<Map<String, Object>> countMonthlyGrouped(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按科室分组统计
    List<Map<String, Object>> countByDeptGrouped(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按医生分组统计
    List<Map<String, Object>> countByDoctorGrouped(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按维度和时间分组统计
    List<Map<String, Object>> countByDimensionAndTime(@Param("dimensionField") String dimensionField,
                                                        @Param("timeField") String timeField,
                                                        @Param("timeFormat") String timeFormat,
                                                        @Param("startTime") String startTime,
                                                        @Param("endTime") String endTime);
}