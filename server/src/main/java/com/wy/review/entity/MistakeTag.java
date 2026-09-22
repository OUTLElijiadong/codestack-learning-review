package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题标签表（学生个人私有标签，同一用户下不重名）
 */
@Data
@TableName("mistake_tag")
public class MistakeTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生ID */
    private Long userId;

    /** 标签名 */
    private String name;

    /** 标签颜色（留空则前端按 id 循环取色） */
    private String color;

    /** 排序号 */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
