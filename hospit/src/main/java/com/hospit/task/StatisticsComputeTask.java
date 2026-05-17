package com.hospit.task;

import com.hospit.service.IStatisticsComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计基准计算定时任务
 * 每天凌晨2点重新计算所有检验项目的均值、标准差等统计基准
 */
@Component
public class StatisticsComputeTask {

    private static final Logger log = LoggerFactory.getLogger(StatisticsComputeTask.class);

    @Autowired
    private IStatisticsComputeService statisticsComputeService;

    // 每天凌晨2点执行
    @Scheduled(cron = "0 0 2 * * ?")
    public void recomputeAllStatistics() {
        log.info("开始执行检验项目统计全量重算...");
        long startTime = System.currentTimeMillis();
        try {
            statisticsComputeService.computeAllStatistics();
            long duration = System.currentTimeMillis() - startTime;
            log.info("检验项目统计全量重算完成，耗时: {} ms", duration);
        } catch (Exception e) {
            log.error("检验项目统计全量重算失败", e);
        }
    }
}
