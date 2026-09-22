package com.wy.review.service;

import com.wy.review.dto.*;
import com.wy.review.vo.CaptchaVO;
import com.wy.review.vo.LoginVO;
import com.wy.review.vo.UserInfoVO;

/**
 * 认证与个人中心服务
 */
public interface AuthService {

    /** 生成图形验证码（Hutool LineCaptcha，答案存服务端内存 5 分钟） */
    CaptchaVO captcha();

    /** 学生注册（用户名唯一、两次密码一致、强制密保） */
    void register(RegisterDTO dto);

    /** 密码登录：验证码校验 → BCrypt 比对 → 冻结检查 → 签发 JWT */
    LoginVO login(LoginDTO dto);

    /** 找回密码第一步：按用户名取密保问题（不返回答案） */
    String getSecurityQuestion(String username);

    /** 找回密码第二步：密保答案比对通过后重置密码 */
    void resetPassword(ResetPasswordDTO dto);

    /** 查询当前登录人信息（任意角色） */
    UserInfoVO profile();

    /** 完善个人学习档案（任意角色） */
    void updateProfile(ProfileUpdateDTO dto);

    /** 修改登录密码（先验旧密码） */
    void updatePassword(PasswordChangeDTO dto);

    /** 修改头像（上传后提交 URL） */
    void updateAvatar(String avatarUrl);

    /** 修改密保问题与答案（先验登录密码） */
    void updateSecurity(SecurityUpdateDTO dto);
}
