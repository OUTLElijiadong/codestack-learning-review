package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 新增管理员（教师）账号入参，仅超管可操作 */
@Data
public class AdminCreateDTO {

    @NotBlank(message = "请输入用户名")
    @Size(min = 3, max = 20, message = "用户名长度为 3-20 个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 20, message = "密码长度为 6-20 个字符")
    private String password;

    @NotBlank(message = "请输入昵称")
    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;
}
