package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.entity.LabResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 患者检验结果表 Mapper 接口
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Mapper
public interface LabResultMapper extends BaseMapper<LabResult> {

    // 分页查询分组后的检验结果
    IPage<Map<String, Object>> selectGroupedPage(Page<?> page,
                                                  @Param("patientId") String patientId,
                                                  @Param("patientName") String patientName,
                                                  @Param("testName") String testName,
                                                  @Param("resultValue") String resultValue,
                                                  @Param("executeDoc") String executeDoc,
                                                  @Param("executeDept") String executeDept,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    // 查询分组后的项目ID列表
    List<Map<String, Object>> selectGroupItemIds(@Param("patientId") String patientId,
                                                  @Param("patientName") String patientName,
                                                  @Param("testName") String testName,
                                                  @Param("resultValue") String resultValue,
                                                  @Param("executeDoc") String executeDoc,
                                                  @Param("executeDept") String executeDept,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    // 游标分页查询分组检验结果
    List<Map<String, Object>> selectGroupedPageWithCursor(@Param("patientId") String patientId,
                                                           @Param("patientName") String patientName,
                                                           @Param("testName") String testName,
                                                           @Param("resultValue") String resultValue,
                                                           @Param("executeDoc") String executeDoc,
                                                           @Param("executeDept") String executeDept,
                                                           @Param("startTime") String startTime,
                                                           @Param("endTime") String endTime,
                                                           @Param("cursor") String cursor,
                                                           @Param("pageSize") int pageSize);

    // 统计游标分页剩余数量
    Long countGroupedPageWithCursor(@Param("patientId") String patientId,
                                    @Param("patientName") String patientName,
                                    @Param("testName") String testName,
                                    @Param("resultValue") String resultValue,
                                    @Param("executeDoc") String executeDoc,
                                    @Param("executeDept") String executeDept,
                                    @Param("startTime") String startTime,
                                    @Param("endTime") String endTime,
                                    @Param("cursor") String cursor);
}
