package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答问题表（社区互助问答）
 * 问答不进内容审核流；发布时过敏感词拦截，违规问答由管理员逻辑删除下架
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提问人ID */
    private Long userId;

    private String title;

    /** 问题详情（支持贴代码） */
    private String content;

    /** 技术方向（字典同错题表） */
    private String techDirection;

    /** 解决状态：0未解决 1已解决（提问人标记） */
    private Integer status;

    private Integer viewCount;

    /** 回复数冗余（评论增删时维护，列表免COUNT子查询） */
    private Integer answerCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
