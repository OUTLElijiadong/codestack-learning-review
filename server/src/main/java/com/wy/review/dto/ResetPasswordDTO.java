package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 找回密码入参：校验密保答案通过后重置为新密码 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密保答案")
    private String securityAnswer;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 20, message = "新密码长度为 6-20 个字符")
    private String newPassword;
}
