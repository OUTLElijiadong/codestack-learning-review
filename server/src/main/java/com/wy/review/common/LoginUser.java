package com.wy.review.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户载体：JWT 解析出的当前登录人信息，
 * 由 LoginInterceptor 写入 UserContext，Service 层取用
 */
@Data
@AllArgsConstructor
public class LoginUser implements Serializable {

    /** 用户ID */
    private Long id;
    /** 登录用户名 */
    private String username;
    /** 角色：student学生 teacher普通管理员 admin超级管理员 */
    private String role;
}
