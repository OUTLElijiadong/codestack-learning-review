package com.wy.review.controller;

import com.wy.review.common.Result;
import com.wy.review.common.UserContext;
import com.wy.review.dto.*;
import com.wy.review.service.AuthService;
import com.wy.review.vo.CaptchaVO;
import com.wy.review.vo.LoginVO;
import com.wy.review.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证与个人中心控制器（公共）
 * 白名单接口：captcha / register / login / security-question / reset-password
 * 其余接口任意登录角色可用（学生与管理员都有个人资料与安全设置）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 获取图形验证码（公开） */
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.ok(authService.captcha());
    }

    /** 学生注册（公开） */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    /** 密码登录（公开）：验证码 → 密码 → 冻结检查 → JWT */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /** 找回密码第一步：按用户名取密保问题（公开） */
    @GetMapping("/security-question")
    public Result<Map<String, String>> securityQuestion(@RequestParam String username) {
        Map<String, String> data = new HashMap<>();
        data.put("question", authService.getSecurityQuestion(username));
        return Result.ok(data);
    }

    /** 找回密码第二步：密保答案比对通过后重置密码（公开） */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.ok();
    }

    /** 退出登录：JWT 无状态，后端无需清理，仅留痕返回成功 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    /** 查询当前登录人信息（任意角色；前端刷新页面后恢复用户状态用） */
    @GetMapping("/profile")
    public Result<UserInfoVO> profile() {
        return Result.ok(authService.profile());
    }

    /** 完善个人学习档案：昵称/学习方向/专业班级/简介 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Validated @RequestBody ProfileUpdateDTO dto) {
        authService.updateProfile(dto);
        return Result.ok();
    }

    /** 修改登录密码 */
    @PutMapping("/profile/password")
    public Result<Void> updatePassword(@Validated @RequestBody PasswordChangeDTO dto) {
        authService.updatePassword(dto);
        return Result.ok();
    }

    /** 修改头像：先经 /common/upload 传图，再把返回 URL 提交保存 */
    @PutMapping("/profile/avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> body) {
        authService.updateAvatar(body.get("avatar"));
        return Result.ok();
    }

    /** 设置/修改密保问题与答案（需先验登录密码） */
    @PutMapping("/profile/security")
    public Result<Void> updateSecurity(@Validated @RequestBody SecurityUpdateDTO dto) {
        authService.updateSecurity(dto);
        return Result.ok();
    }
}
