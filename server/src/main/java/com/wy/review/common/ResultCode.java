package com.wy.review.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举
 * 200 成功 / 4xx 客户端问题 / 5xx 服务端问题 / 1xxx 具体业务异常
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    FAIL(500, "系统异常，请稍后重试"),

    CAPTCHA_ERROR(1001, "验证码错误或已过期"),
    PASSWORD_ERROR(1002, "用户名或密码错误"),
    ACCOUNT_FROZEN(1003, "账号已被冻结，请联系管理员"),
    USERNAME_EXISTS(1004, "用户名已被注册"),
    SENSITIVE_HIT(1005, "内容包含不良关键词，请修改后发布"),
    SECURITY_ANSWER_ERROR(1006, "密保答案错误"),
    DATA_NOT_FOUND(1007, "数据不存在或已被删除"),
    REPEAT_SUBMIT(1008, "请勿重复操作");

    private final Integer code;
    private final String msg;
}
