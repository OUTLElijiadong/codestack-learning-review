package com.wy.review.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 评论树节点（二级楼中楼：一级评论带 children 子回复列表） */
@Data
public class CommentVO {

    private Long id;
    private Long questionId;
    private Long parentId;
    private Long rootId;

    private String content;

    /** 评论人信息 */
    private Long userId;
    private String nickname;
    private String avatar;

    /** 被回复人昵称（"回复 @昵称"） */
    private String replyNickname;

    /** 被回复人角色（标识"教师"回复） */
    private String role;

    /** 所属问题标题（"只看自己回复"列表填充） */
    private String questionTitle;

    private LocalDateTime createTime;

    /** 子回复（仅一级评论挂载） */
    private List<CommentVO> children = new ArrayList<>();
}
