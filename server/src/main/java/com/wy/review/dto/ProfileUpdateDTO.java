package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/** 个人学习档案入参（昵称/学习方向/专业班级/简介等） */
@Data
public class ProfileUpdateDTO {

    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    /** 学习方向：frontend前端 backend后端 test测试 */
    private String learnDirection;

    @Size(max = 64, message = "专业最长 64 个字符")
    private String major;

    @Size(max = 64, message = "班级最长 64 个字符")
    private String className;

    @Size(max = 255, message = "个人简介最长 255 个字符")
    private String bio;

    private String email;

    private String phone;

    /** 性别：0未知 1男 2女 */
    private Integer gender;
}
