package com.wy.review.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录成功返回：JWT + 脱敏用户信息 */
@Data
@AllArgsConstructor
public class LoginVO {

    /** JWT 令牌（前端存 localStorage，请求头 Authorization: Bearer 携带） */
    private String token;

    /** 当前登录人信息（已脱敏） */
    private UserInfoVO userInfo;
}
