package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 笔记新增/编辑入参 */
@Data
public class NoteDTO {

    @NotBlank(message = "请输入笔记标题")
    @Size(max = 128, message = "标题最长 128 个字符")
    private String title;

    /** 富文本 HTML 内容（wangEditor 输出） */
    private String content;

    @NotNull(message = "请选择笔记分类")
    private Long categoryId;

    /** 是否公开：0私密 1公开（公开将进入审核流） */
    @NotNull(message = "请选择公开/私密")
    private Integer isPublic;
}
