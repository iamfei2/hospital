package com.hospit.task;

import com.hospit.service.IIsolationForestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 孤立森林模型自动训练任务
 * 每天凌晨3点重建所有模型，适配最新数据分布
 * 模型版本自动管理，保留最新2个版本用于回溯对比
 */
@Component
public class IsolationForestTrainTask {

    private static final Logger log = LoggerFactory.getLogger(IsolationForestTrainTask.class);

    @Autowired
    private IIsolationForestService isolationForestService;

    // 每天凌晨3点重建所有模型
    @Scheduled(cron = "0 0 3 * * ?")
    public void trainModels() {
        log.info("开始自动训练孤立森林模型...");
        long startTime = System.currentTimeMillis();
        try {
            isolationForestService.rebuildAllModels();
            long duration = System.currentTimeMillis() - startTime;
            log.info("孤立森林模型训练完成，耗时: {} ms", duration);
        } catch (Exception e) {
            log.error("孤立森林模型训练失败", e);
        }
    }
}
