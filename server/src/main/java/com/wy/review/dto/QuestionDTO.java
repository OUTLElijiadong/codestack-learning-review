package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 发布问题入参 */
@Data
public class QuestionDTO {

    @NotBlank(message = "请输入问题标题")
    @Size(max = 128, message = "标题最长 128 个字符")
    private String title;

    /** 问题详情（支持贴代码） */
    private String content;

    /** 技术方向 */
    private String techDirection;
}
