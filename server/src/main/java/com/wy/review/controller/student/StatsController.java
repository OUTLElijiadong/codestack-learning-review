package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.Result;
import com.wy.review.service.StatsService;
import com.wy.review.vo.OverviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 学习数据可视化统计控制器（学生端）
 * 全部为 ECharts 图表与个人总览提供数据
 */
@RestController
@RequestMapping("/student")
@RequireRole("student")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 个人学习数据总览（仪表盘四张统计卡） */
    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.ok(statsService.overview());
    }

    /** 每周错题增长柱状图：近 8 周 */
    @GetMapping("/stats/weekly")
    public Result<List<Map<String, Object>>> weekly() {
        return Result.ok(statsService.weeklyMistakeGrowth());
    }

    /** 各技术方向错题占比饼图 */
    @GetMapping("/stats/tech-pie")
    public Result<List<Map<String, Object>>> techPie() {
        return Result.ok(statsService.techDirectionPie());
    }

    /** 每日学习时长折线图：近 14 天 */
    @GetMapping("/stats/daily-duration")
    public Result<List<Map<String, Object>>> dailyDuration() {
        return Result.ok(statsService.dailyDuration());
    }

    /** 学习完成率统计卡片：近 30 天 */
    @GetMapping("/stats/completion")
    public Result<Map<String, Object>> completion() {
        return Result.ok(statsService.completion());
    }
}
