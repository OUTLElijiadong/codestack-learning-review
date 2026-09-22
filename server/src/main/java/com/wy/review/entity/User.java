package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表：学生 / 教师(普通管理员) / 超级管理员 三类角色合一，role 区分
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名（唯一） */
    private String username;

    /** 登录密码（BCrypt密文），永不出网（VO 脱敏） */
    private String password;

    private String nickname;

    /** 头像访问URL，空则前端显示昵称首字 */
    private String avatar;

    private String email;

    private String phone;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 角色：student学生 teacher普通管理员(教师) admin超级管理员 */
    private String role;

    /** 专业（大屏按专业统计活跃度） */
    private String major;

    private String className;

    /** 学习方向：frontend前端 backend后端 test测试 */
    private String learnDirection;

    /** 个人简介/学习宣言 */
    private String bio;

    /** 密保问题（找回密码用） */
    private String securityQuestion;

    /** 密保答案（BCrypt密文），永不出网 */
    private String securityAnswer;

    /** 账号状态：1正常 0冻结 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
