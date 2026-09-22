package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告表
 */
@Data
@TableName("announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 公告类型：notice编程学习通知 maintenance系统维护通知 */
    private String type;

    /** 发布人ID（管理员） */
    private Long publisherId;

    /** 发布人昵称冗余（改名/删号后公告列表仍可读） */
    private String publisherName;

    /** 状态：0草稿 1已发布 2已下线 */
    private Integer status;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
