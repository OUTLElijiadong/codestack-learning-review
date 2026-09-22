package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习时长记录表（流水表；折线图按日 SUM(duration) 聚合）
 */
@Data
@TableName("study_time_log")
public class StudyTimeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long userId;

    /** 学习日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate studyDate;

    /** 本次学习时长（分钟） */
    private Integer duration;

    /** 时长来源：review复盘 note写笔记 practice刷题 manual手动补录 */
    private String source;

    private LocalDateTime createTime;
}
