package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 注册入参：注册即强制设置密保问题/答案（找回密码的唯一凭证） */
@Data
public class RegisterDTO {

    @NotBlank(message = "请输入用户名")
    @Size(min = 3, max = 20, message = "用户名长度为 3-20 个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度为 6-20 个字符")
    private String password;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    @NotBlank(message = "请输入昵称")
    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    @NotBlank(message = "请设置密保问题（找回密码用）")
    @Size(max = 64, message = "密保问题最长 64 个字符")
    private String securityQuestion;

    @NotBlank(message = "请设置密保答案")
    @Size(max = 32, message = "密保答案最长 32 个字符")
    private String securityAnswer;
}
