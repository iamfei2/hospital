package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.PathologyExamination;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PathologyExaminationMapper extends BaseMapper<PathologyExamination> {

    // 查询已存在的病理编号
    @Select("<script>" +
            "SELECT pathology_no FROM pathology_examination WHERE is_invalid = 0 AND pathology_no IN " +
            "<foreach collection='pathologyNos' item='no' open='(' separator=',' close=')'>" +
            "#{no}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistPathologyNos(@Param("pathologyNos") List<String> pathologyNos);

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