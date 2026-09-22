package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答评论表（二级楼中楼：root_id + parent_id，两次查询无递归）
 * root_id=0 表示一级评论；子回复 root_id 统一指向一级评论，parent_id 指向被回复的评论
 */
@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属问题ID */
    private Long questionId;

    /** 评论人ID */
    private Long userId;

    /** 父评论ID：0=一级评论 */
    private Long parentId;

    /** 根评论ID：0=自身即一级评论 */
    private Long rootId;

    /** 被回复人ID（前端显示"回复 @昵称"） */
    private Long replyUserId;

    private String content;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
