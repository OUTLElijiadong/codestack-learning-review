package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 评论/回复入参 */
@Data
public class CommentDTO {

    @NotNull(message = "缺少问题ID")
    private Long questionId;

    @NotBlank(message = "请输入回复内容")
    @Size(max = 1000, message = "回复最长 1000 个字符")
    private String content;

    /** 父评论ID：0 或 null 表示一级评论（直接回答） */
    private Long parentId;
}
