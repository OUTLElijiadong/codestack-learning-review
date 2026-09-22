package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表（AOP 自动记录管理员操作，仅超管可查）
 * 类名加 Entity 后缀，避免与注解 com.wy.review.annotation.OperationLog 同名冲突
 */
@Data
@TableName("operation_log")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名冗余（改名/删号后日志仍可读） */
    private String username;

    /** 操作人角色 */
    private String role;

    /** 所属模块：用户管理/内容审核/公告管理/敏感词管理/权限管理等 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** HTTP方法 */
    private String requestMethod;

    /** 请求路径 */
    private String requestUrl;

    /** Java方法签名 */
    private String method;

    /** 请求参数JSON（密码等敏感字段已脱敏） */
    private String params;

    /** 操作人IP */
    private String ip;

    /** 执行结果：1成功 0失败 */
    private Integer result;

    /** 失败原因/异常摘要 */
    private String errorMsg;

    /** 接口耗时（毫秒） */
    private Long costTime;

    private LocalDateTime createTime;
}
