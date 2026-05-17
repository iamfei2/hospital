package com.hospit.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 - 标记需要记录操作日志的方法
 * 配合OperateLogAspect使用，自动记录操作留痕
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    
    /** 操作类型：新增/修改/删除/作废 */
    String operationType() default "";
    
    /** 操作的表名，如：patient、lab_result */
    String operatedTable() default "";
    
    /** 操作描述，如：新增患者、修改检验结果 */
    String description() default "";
}
