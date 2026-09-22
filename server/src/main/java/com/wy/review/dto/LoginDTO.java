package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 登录入参：账号密码 + 图形验证码 */
@Data
public class LoginDTO {

    @NotBlank(message = "请输入用户名或手机号")
    private String username;

    /** 所选身份：student 学生 / teacher 教师 / admin 超级管理员；为空不校验 */
    private String role;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请输入图形验证码")
    @Size(min = 4, max = 4, message = "验证码为 4 位")
    private String captchaCode;

    @NotBlank(message = "验证码标识缺失，请刷新验证码")
    private String captchaUuid;
}
