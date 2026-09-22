package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡签到表（流水表，只增不改；uk_user_date 保证一天只能打一次）
 */
@Data
@TableName("check_in")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long userId;

    /** 打卡日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkDate;

    /** 实际打卡时刻 */
    private LocalDateTime checkTime;

    private LocalDateTime createTime;
}
