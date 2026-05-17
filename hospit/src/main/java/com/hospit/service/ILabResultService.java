package com.hospit.service;

import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.LabResult;
import com.hospit.vo.CursorPageResult;
import com.hospit.vo.IsolationForestResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 患者检验结果表 服务类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
public interface ILabResultService extends IService<LabResult> {
    Result getLabResultPage(QueryPageParam queryPageParam);
    Result getLabResultDetail(Long resultId);
    Result getLabResultsByTime(QueryPageParam queryPageParam);

    /**
     * 评估检验结果并触发预警（用于单条录入）
     */
    void evaluateAndSaveWarnings(LabResult result);

    /**
     * 批量评估检验结果并触发预警（用于批量导入）
     */
    void evaluateAndSaveWarningsBatch(List<LabResult> results);
    /**
     * 根据患者ID获取检验结果
     */
    Result getLabResultsByPatient(String patientId);

    /**
     * 获取患者检验项目历史趋势
     */
    Result getTrend(String patientId, Integer itemId);

    /**
     * 游标分页查询检验结果（按时间分组）
     * @param queryPageParam 查询参数
     * @param cursor 游标（上一页最后一条的reportTime），null表示第一页
     * @return 游标分页结果
     */
    Result getLabResultPageWithCursor(QueryPageParam queryPageParam, String cursor);

    /**
     * 孤立森林联合检测（Z-Score + 孤立森林）
     * @param patientId 患者ID
     * @param itemIds 要检测的指标ID列表
     * @return 联合检测结果
     */
    IsolationForestResultVO jointIsolationForestDetect(String patientId, List<Integer> itemIds);
}
