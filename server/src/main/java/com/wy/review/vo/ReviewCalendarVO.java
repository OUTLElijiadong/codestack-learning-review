package com.wy.review.vo;

import lombok.Data;

/** 复盘日历单日数据（el-calendar 按日期渲染"学了/没学"） */
@Data
public class ReviewCalendarVO {

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 当日目标题数（无计划为 0） */
    private Integer targetCount;

    /** 当日已完成题数 */
    private Integer completedCount;

    /** 任务状态：0未完成 1已完成（无记录视为 null） */
    private Integer status;

    /** 当日是否打卡 */
    private Boolean checked;
}
