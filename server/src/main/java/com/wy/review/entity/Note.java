package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记表（wangEditor 富文本 HTML，代码块 pre>code 保存，前端 highlight.js 渲染）
 */
@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID */
    private Long userId;

    /** 分类ID（→note_category.id） */
    private Long categoryId;

    /** 笔记标题（搜索字段） */
    private String title;

    /** 富文本HTML正文 */
    private String content;

    /** 列表摘要（后端从纯文本截取前120字） */
    private String summary;

    /** 是否公开：0私密 1公开 */
    private Integer isPublic;

    /** 审核状态：0待审核 1正常 2已下架（私密恒为1；切公开置0进入审核） */
    private Integer auditStatus;

    /** 审核备注/下架原因 */
    private String auditRemark;

    /** 浏览数 */
    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
