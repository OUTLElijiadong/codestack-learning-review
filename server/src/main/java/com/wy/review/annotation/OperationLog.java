package com.wy.review.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注在后台管理 Controller 方法上，
 * OperationLogAspect 环绕通知自动把操作写入 operation_log 表（仅超管可查）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 所属模块，如：用户管理、内容审核、公告管理 */
    String module() default "";

    /** 操作描述，如：冻结用户、下架笔记 */
    String operation() default "";
}
