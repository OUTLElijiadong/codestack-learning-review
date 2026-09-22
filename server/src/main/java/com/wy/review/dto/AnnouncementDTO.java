package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 公告发布/编辑入参 */
@Data
public class AnnouncementDTO {

    @NotBlank(message = "请输入公告标题")
    @Size(max = 128, message = "标题最长 128 个字符")
    private String title;

    @NotBlank(message = "请输入公告内容")
    private String content;

    /** 公告类型：notice编程学习通知 maintenance系统维护通知 */
    @NotBlank(message = "请选择公告类型")
    private String type;

    /** 状态：0存草稿 1直接发布 */
    @NotNull(message = "请选择发布状态")
    private Integer status;
}
