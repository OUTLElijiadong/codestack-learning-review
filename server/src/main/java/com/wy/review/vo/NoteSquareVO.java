package com.wy.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 公开笔记广场 VO（列表页不返回 content 大字段，带作者信息） */
@Data
public class NoteSquareVO {

    private Long id;
    private String title;
    private String summary;
    private Long categoryId;
    private String categoryName;
    private Integer viewCount;
    private LocalDateTime createTime;

    /** 作者信息 */
    private Long userId;
    private String nickname;
    private String avatar;
}
