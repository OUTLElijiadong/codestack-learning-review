package com.wy.review.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 问答问题 VO（列表/详情共用；详情时带 content 与 comments 评论树） */
@Data
public class QuestionVO {

    private Long id;
    private String title;
    private String content;
    private String techDirection;

    /** 解决状态：0未解决 1已解决 */
    private Integer status;

    private Integer viewCount;
    private Integer answerCount;
    private LocalDateTime createTime;

    /** 提问人信息 */
    private Long userId;
    private String nickname;
    private String avatar;

    /** 是否本人发布（前端控制"标记已解决/删除"按钮显隐） */
    private Boolean mine;

    /** 评论树（仅详情接口填充） */
    private List<CommentVO> comments;
}
