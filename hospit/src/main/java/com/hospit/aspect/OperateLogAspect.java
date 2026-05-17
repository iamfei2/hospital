package com.hospit.aspect;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospit.annotation.OperateLog;
import com.hospit.entity.OperationLog;
import com.hospit.service.IOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面 - 拦截所有@OperateLog注解方法
 * 自动记录操作人、IP、操作类型、操作表、操作前后的数据快照
 * 异步写入数据库，保证业务接口性能
 */
@Aspect
@Component
public class OperateLogAspect {

    @Autowired
    private IOperationLogService operationLogService;

    @Autowired
    private ApplicationContext applicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("@annotation(com.hospit.annotation.OperateLog)")
    public void operateLogPointcut() {}

    @Around("operateLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OperateLog operateLog = getAnnotation(joinPoint);
        if (operateLog == null) {
            return joinPoint.proceed();
        }

        String operationType = operateLog.operationType();
        String tableName = operateLog.operatedTable();
        String recordId = getRecordId(joinPoint, operationType);
        
        OperationLogContext context = new OperationLogContext();
        context.setOperationType(operationType);
        context.setOperatedTable(tableName);
        context.setRecordId(recordId);
        context.setDescription(operateLog.description());
        context.setUserId(getCurrentUserId());
        context.setIpAddress(getIpAddress());
        context.setUserAgent(getUserAgent());

        String beforeContent = "";
        if (("修改".equals(operationType) || "删除".equals(operationType) || "作废".equals(operationType))
                && recordId != null && !recordId.isEmpty() && EntityTableMapper.isKnownTable(tableName)) {
            beforeContent = fetchOriginalData(tableName, recordId);
            context.setBeforeContent(beforeContent);
        } else {
            context.setBeforeContent("");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            context.setAfterContent("执行失败: " + e.getMessage());
            saveLogAsync(context);
            throw e;
        }

        String afterContent = "";
        if ("新增".equals(operationType) || "修改".equals(operationType)) {
            afterContent = extractEntityFromResult(result, tableName, recordId);
        } else if ("删除".equals(operationType) || "作废".equals(operationType)) {
            afterContent = "{}";
        }
        context.setAfterContent(afterContent);

        saveLogAsync(context);

        return result;
    }

    private String fetchOriginalData(String tableName, String recordId) {
        try {
            Class<?> entityClass = EntityTableMapper.getEntityClass(tableName);
            if (entityClass == null) {
                return "";
            }
            
            String beanName = getServiceBeanName(entityClass);
            Object service = applicationContext.getBean(beanName);
            Method getByIdMethod = service.getClass().getMethod("getById", java.io.Serializable.class);
            Object entity = getByIdMethod.invoke(service, parseRecordId(recordId, entityClass));
            
            if (entity != null) {
                return objectMapper.writeValueAsString(entity);
            }
        } catch (Exception e) {
        }
        return "";
    }

    private String extractEntityFromResult(Object result, String tableName, String recordId) {
        try {
            if (result == null) {
                return "";
            }
            
            if (result instanceof com.hospit.common.Result) {
                Object data = ((com.hospit.common.Result) result).getData();
                if (data != null) {
                    if (data instanceof java.util.Map) {
                        return objectMapper.writeValueAsString(data);
                    }
                    return objectMapper.writeValueAsString(data);
                }
            }
            
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "";
        }
    }

    private String getServiceBeanName(Class<?> entityClass) {
        String entityName = entityClass.getSimpleName();
        String firstChar = entityName.substring(0, 1).toLowerCase();
        return firstChar + entityName.substring(1) + "Service";
    }

    private java.io.Serializable parseRecordId(String recordId, Class<?> entityClass) {
        if (recordId == null || recordId.isEmpty()) {
            return null;
        }
        if (entityClass.getSimpleName().contains("Ct") || 
            entityClass.getSimpleName().contains("Mri") ||
            entityClass.getSimpleName().contains("Pathology") ||
            entityClass.getSimpleName().contains("Enteroscopy")) {
            return Long.parseLong(recordId);
        }
        if (recordId.matches("\\d+")) {
            return Long.parseLong(recordId);
        }
        return recordId;
    }

    private OperateLog getAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            return method.getAnnotation(OperateLog.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Async
    public void saveLogAsync(OperationLogContext context) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(context.getUserId());
            log.setOperationTime(LocalDateTime.now());
            log.setOperationType(context.getOperationType());
            log.setOperatedTable(context.getOperatedTable());
            log.setRelatedRecordId(context.getRecordId());
            log.setBeforeContent(context.getBeforeContent());
            log.setAfterContent(context.getAfterContent());
            log.setRemark(context.getDescription() + " [IP:" + context.getIpAddress() + "]");

            operationLogService.save(log);
        } catch (Exception e) {
        }
    }

    private Integer getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userId = request.getAttribute("userId");
                if (userId != null) {
                    return (Integer) userId;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ua = request.getHeader("User-Agent");
                if (ua != null && ua.length() > 100) {
                    ua = ua.substring(0, 100);
                }
                return ua;
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    private String getRecordId(ProceedingJoinPoint joinPoint, String operationType) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg instanceof Long) {
                        return String.valueOf(arg);
                    } else if (arg instanceof Integer) {
                        return String.valueOf(arg);
                    } else if (arg instanceof String) {
                        String s = (String) arg;
                        if (!s.contains("@") && !s.contains("{") && s.length() < 50) {
                            return s;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }
}
