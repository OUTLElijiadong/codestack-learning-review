package com.wy.review.vo;

import com.wy.review.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 脱敏用户信息 VO：password、securityAnswer 永不包含在内
 */
@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;

    /** 角色：student学生 teacher普通管理员 admin超级管理员 */
    private String role;

    private String major;
    private String className;

    /** 学习方向：frontend前端 backend后端 test测试 */
    private String learnDirection;

    private String bio;

    /** 密保问题（答案不返回） */
    private String securityQuestion;

    /** 账号状态：1正常 0冻结 */
    private Integer status;

    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;

    /** 实体 → VO（BeanUtils 拷贝同名属性，天然剔除密码字段） */
    public static UserInfoVO of(User user) {
        if (user == null) {
            return null;
        }
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
