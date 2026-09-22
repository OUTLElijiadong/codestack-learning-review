package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词表（应用层内存加载，发布内容 contains 匹配拦截）
 */
@Data
@TableName("sensitive_word")
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 敏感词内容（唯一） */
    private String word;

    /** 处理级别：1直接拦截 2标记人工复核（预留扩展） */
    private Integer level;

    /** 状态：1启用 0停用 */
    private Integer status;

    private LocalDateTime createTime;
}
