package com.hospit.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SentinelAspect {

    private static final Logger log = LoggerFactory.getLogger(SentinelAspect.class);

    @Pointcut("execution(* com.hospit.controller.OpenApiController.*(..))")
    public void openApiPointcut() {}

    @Pointcut("execution(* com.hospit.controller.BackupController.*(..))")
    public void backupPointcut() {}

    @Pointcut("execution(* com.hospit.controller.StatisticsController.*(..))")
    public void statisticsPointcut() {}

    @Around("openApiPointcut()")
    public Object aroundOpenApi(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithSentinel(joinPoint, "open-api", EntryType.IN);
    }

    @Around("backupPointcut()")
    public Object aroundBackup(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithSentinel(joinPoint, "backup", EntryType.IN);
    }

    @Around("statisticsPointcut()")
    public Object aroundStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithSentinel(joinPoint, "statistics", EntryType.IN);
    }

    private Object executeWithSentinel(ProceedingJoinPoint joinPoint, String resourceName, EntryType entryType) throws Throwable {
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, entryType);
            return joinPoint.proceed();
        } catch (BlockException e) {
            log.warn("Sentinel 拦截: resource={}, method={}", resourceName, joinPoint.getSignature().getName());
            return com.hospit.common.Result.result(429, "请求过于频繁，请稍后重试", 0L, null);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
