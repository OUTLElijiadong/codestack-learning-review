package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.dto.ReviewPlanDTO;
import com.wy.review.dto.StudyTimeDTO;
import com.wy.review.entity.ReviewPlan;
import com.wy.review.entity.ReviewTask;
import com.wy.review.vo.ReviewCalendarVO;

import java.util.List;
import java.util.Map;

/**
 * 每日复盘计划服务（计划设置 / 今日任务 / 完成打勾 / 打卡 / 日历 / 学习时长上报）
 */
public interface ReviewService {

    /** 查看我的复盘计划（无记录时返回默认计划，不落库） */
    ReviewPlan getPlan();

    /** 保存复盘计划（按 userId upsert） */
    void savePlan(ReviewPlanDTO dto);

    /**
     * 今日复盘任务（同时是"未完成自动提醒"的数据源）：
     * 返回计划状态、今日目标/已完成、是否打卡、推荐复习错题列表
     */
    Map<String, Object> today();

    /** 完成一道错题复习：计数+1，达标自动打勾完成并联动打卡 */
    Map<String, Object> finish(Long mistakeId);

    /** 每日打卡签到（uk_user_date 保证一天一次） */
    void sign();

    /** 复盘日历：按月返回每日任务与打卡状态 */
    List<ReviewCalendarVO> calendar(String month);

    /** 复盘历史记录分页 */
    PageResult<ReviewTask> records(Integer pageNum, Integer pageSize);

    /** 上报学习时长（学习时长折线图的唯一写入入口） */
    void reportStudyTime(StudyTimeDTO dto);
}
