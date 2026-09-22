package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题-标签关联表（多对多）
 * 关联记录无业务字段、跟随主数据清理，物理删除即可，故无 deleted
 */
@Data
@TableName("mistake_tag_rel")
public class MistakeTagRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 错题ID */
    private Long mistakeId;

    /** 标签ID */
    private Long tagId;

    private LocalDateTime createTime;
}
