package com.wy.review.service;

import com.wy.review.vo.OverviewVO;

import java.util.List;
import java.util.Map;

/**
 * 学习数据统计服务（学生端 ECharts 四图 + 管理端数据大屏）
 */
public interface StatsService {

    // ---------------- 学生端 ----------------

    /** 个人学习数据总览卡片（错题/笔记/收藏/今日进度/连续打卡/累计时长） */
    OverviewVO overview();

    /** 每周错题增长柱状图：近 8 周 [{week, count}] */
    List<Map<String, Object>> weeklyMistakeGrowth();

    /** 各技术方向错题占比饼图：[{name, value}] 与 ECharts pie 格式一致 */
    List<Map<String, Object>> techDirectionPie();

    /** 每日学习时长折线图：近 14 天 [{date, minutes}] */
    List<Map<String, Object>> dailyDuration();

    /** 学习完成率统计卡片：近 30 天完成率/连续打卡/累计复习数 */
    Map<String, Object> completion();

    // ---------------- 管理端数据大屏 ----------------

    /** 大屏顶部总量卡片 */
    Map<String, Object> screenOverview();

    /** 各专业学习活跃度（按专业分组：总人数/近7天活跃数） */
    List<Map<String, Object>> screenMajorActive();

    /** 系统使用趋势：近 30 天 新增错题/新增笔记/打卡人次 三条按日序列 */
    Map<String, List<Map<String, Object>>> screenTrend();

    /** 全校错题技术方向分布饼图 */
    List<Map<String, Object>> screenTechDist();

    /** 大屏专业柱下钻：某专业学生名册（错题数 / 近7天打卡天数 / 登录是否活跃） */
    List<Map<String, Object>> screenMajorStudents(String major);
}
