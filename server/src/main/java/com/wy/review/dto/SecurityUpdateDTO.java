package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 密保修改入参：修改密保前需先验证登录密码 */
@Data
public class SecurityUpdateDTO {

    @NotBlank(message = "请先输入登录密码确认身份")
    private String password;

    @NotBlank(message = "请设置密保问题")
    @Size(max = 64, message = "密保问题最长 64 个字符")
    private String securityQuestion;

    @NotBlank(message = "请设置密保答案")
    @Size(max = 32, message = "密保答案最长 32 个字符")
    private String securityAnswer;
}
