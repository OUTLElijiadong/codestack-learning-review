package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复盘任务完成记录表（每人每天一行）
 * completed_count >= target_count 时 status 自动置 1 —— 即"学习完成自动打勾"
 */
@Data
@TableName("review_task")
public class ReviewTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生ID */
    private Long userId;

    /** 任务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskDate;

    /** 当日目标复习题数（生成时取计划快照，改计划不影响历史） */
    private Integer targetCount;

    /** 当日已完成复习题数 */
    private Integer completedCount;

    /** 当日已复习错题ID的JSON数组，如 [12,35,41]（防重复计数） */
    private String reviewedIds;

    /** 完成状态：0未完成 1已完成（达标自动打勾） */
    private Integer status;

    /** 完成时间（自动打勾时刻） */
    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
