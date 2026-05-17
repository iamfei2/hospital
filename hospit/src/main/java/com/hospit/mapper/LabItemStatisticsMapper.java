package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.LabItemStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LabItemStatisticsMapper extends BaseMapper<LabItemStatistics> {

    // 根据项目ID和科室查询统计
    LabItemStatistics selectByItemIdAndDept(@Param("itemId") Integer itemId, @Param("deptCode") String deptCode);

    // 根据项目ID查询统计列表
    List<LabItemStatistics> selectByItemId(@Param("itemId") Integer itemId);

    // 插入或更新统计
    int upsert(LabItemStatistics statistics);
}
