package com.wy.review.service.impl;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wy.review.common.BusinessException;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.*;
import com.wy.review.entity.User;
import com.wy.review.mapper.UserMapper;
import com.wy.review.service.AuthService;
import com.wy.review.utils.JwtUtil;
import com.wy.review.utils.PasswordUtil;
import com.wy.review.vo.CaptchaVO;
import com.wy.review.vo.LoginVO;
import com.wy.review.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证与个人中心服务实现
 *
 * 验证码方案：Hutool LineCaptcha 生成图形，答案存服务端 ConcurrentHashMap
 * （5 分钟过期、一次性使用），毕设单机环境足够；分布式场景应换 Redis。
 * 找回密码方案：密保问题（注册时强制设置，答案 BCrypt 加密存储）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 验证码内存缓存：uuid → (答案, 过期时间戳) */
    private static final Map<String, CacheEntry> CAPTCHA_CACHE = new ConcurrentHashMap<>();
    private static final long CAPTCHA_TTL = 5 * 60 * 1000L;

    private static class CacheEntry {
        final String code;
        final long expireAt;
        CacheEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    @Override
    public CaptchaVO captcha() {
        // 顺带清理过期验证码，避免内存膨胀
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, CacheEntry>> it = CAPTCHA_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().expireAt < now) {
                it.remove();
            }
        }
        LineCaptcha captcha = new LineCaptcha(160, 60, 4, 30);
        String uuid = UUID.fastUUID().toString(true);
        CAPTCHA_CACHE.put(uuid, new CacheEntry(captcha.getCode(), now + CAPTCHA_TTL));
        return new CaptchaVO(uuid, "data:image/png;base64," + captcha.getImageBase64());
    }

    /** 校验验证码：一次性，无论对错立即删除（防重放） */
    private void verifyCaptcha(String uuid, String code) {
        CacheEntry entry = uuid == null ? null : CAPTCHA_CACHE.remove(uuid);
        if (entry == null || entry.expireAt < System.currentTimeMillis()
                || !entry.code.equalsIgnoreCase(code)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        // 密码与密保答案都 BCrypt 加密入库，绝不存明文
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setSecurityAnswer(PasswordUtil.encode(dto.getSecurityAnswer().trim().toLowerCase()));
        user.setRole("student");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
        // 登录账号支持用户名或手机号（手机号仅学生端常用，管理端仍用账号）
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", dto.getUsername()).or().eq("phone", dto.getUsername()).last("LIMIT 1"));
        if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        // 所选身份与实际角色不符时明确拒绝（防止学生误用管理入口登录）
        if (dto.getRole() != null && !dto.getRole().isEmpty() && !dto.getRole().equals(user.getRole())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "账号或密码与所选身份不符");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_FROZEN);
        }
        // 刷新最近登录时间（活跃用户统计口径）
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, UserInfoVO.of(user));
    }

    @Override
    public String getSecurityQuestion(String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户名不存在");
        }
        if (user.getSecurityQuestion() == null || user.getSecurityQuestion().isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该账号未设置密保，请联系管理员重置密码");
        }
        return user.getSecurityQuestion();
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户名不存在");
        }
        if (user.getSecurityAnswer() == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该账号未设置密保，请联系管理员重置密码");
        }
        boolean ok = PasswordUtil.matches(dto.getSecurityAnswer().trim().toLowerCase(), user.getSecurityAnswer());
        if (!ok) {
            throw new BusinessException(ResultCode.SECURITY_ANSWER_ERROR);
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public UserInfoVO profile() {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return UserInfoVO.of(user);
    }

    @Override
    public void updateProfile(ProfileUpdateDTO dto) {
        User update = new User();
        update.setId(UserContext.getUserId());
        if (dto.getNickname() != null) update.setNickname(dto.getNickname());
        if (dto.getLearnDirection() != null) update.setLearnDirection(dto.getLearnDirection());
        if (dto.getMajor() != null) update.setMajor(dto.getMajor());
        if (dto.getClassName() != null) update.setClassName(dto.getClassName());
        if (dto.getBio() != null) update.setBio(dto.getBio());
        if (dto.getEmail() != null) update.setEmail(dto.getEmail());
        if (dto.getPhone() != null) update.setPhone(dto.getPhone());
        if (dto.getGender() != null) update.setGender(dto.getGender());
        userMapper.updateById(update);
    }

    @Override
    public void updatePassword(PasswordChangeDTO dto) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null || !PasswordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "原密码错误");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        User update = new User();
        update.setId(UserContext.getUserId());
        update.setAvatar(avatarUrl);
        userMapper.updateById(update);
    }

    @Override
    public void updateSecurity(SecurityUpdateDTO dto) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "登录密码错误，无法修改密保");
        }
        User update = new User();
        update.setId(user.getId());
        update.setSecurityQuestion(dto.getSecurityQuestion());
        update.setSecurityAnswer(PasswordUtil.encode(dto.getSecurityAnswer().trim().toLowerCase()));
        userMapper.updateById(update);
    }
}
