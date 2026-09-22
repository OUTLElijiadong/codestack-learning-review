package com.wy.review.vo;

import lombok.Data;

/** 个人学习数据总览（学生端仪表盘四张统计卡 + 今日任务进度） */
@Data
public class OverviewVO {

    /** 错题总数 */
    private Long mistakeTotal;

    /** 笔记总数 */
    private Long noteTotal;

    /** 收藏错题数 */
    private Long favoriteTotal;

    /** 本周新增错题数（统计卡趋势 chip） */
    private Long mistakeWeekNew;

    /** 本周新增笔记数 */
    private Long noteWeekNew;

    /** 今日目标复习题数 */
    private Integer todayTarget;

    /** 今日已复习题数 */
    private Integer todayFinished;

    /** 今日任务是否已完成（自动打勾） */
    private Boolean todayDone;

    /** 今日是否已打卡 */
    private Boolean todayChecked;

    /** 连续打卡天数 */
    private Integer checkInDays;

    /** 累计学习时长（分钟） */
    private Long totalMinutes;
}
