package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分类表
 * user_id=0 为系统内置分类（全员可见共用），免去给每个新用户复制分类的麻烦
 */
@Data
@TableName("note_category")
public class NoteCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID：0=系统默认分类 */
    private Long userId;

    /** 分类名 */
    private String name;

    /** 排序号（越小越靠前） */
    private Integer sort;

    /** 是否系统内置：1内置（不可删改） 0用户自建 */
    private Integer isSystem;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
