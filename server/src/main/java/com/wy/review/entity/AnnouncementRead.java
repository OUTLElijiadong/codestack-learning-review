package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告已读关系表（公告-用户 多对多）
 * uk_ann_user 联合唯一键防重复已读
 */
@Data
@TableName("announcement_read")
public class AnnouncementRead {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long announcementId;

    private Long userId;

    private LocalDateTime readTime;

    private LocalDateTime createTime;
}
