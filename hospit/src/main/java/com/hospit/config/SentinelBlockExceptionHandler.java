package com.hospit.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hospit.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SentinelBlockExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SentinelBlockExceptionHandler.class);

    // 处理Sentinel限流/熔断异常
    @ExceptionHandler(BlockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result handleBlockException(BlockException e) {
        String resourceName = e.getRuleLimitApp();
        log.warn("Sentinel 限流/熔断触发: resource={}, blockType={}", resourceName, e.getClass().getSimpleName());
        
        if (e.getRule() != null) {
            log.warn("触发规则: {}", e.getRule());
        }
        
        return Result.result(429, "服务暂时不可用，请稍后重试", 0L, null);
    }
}
