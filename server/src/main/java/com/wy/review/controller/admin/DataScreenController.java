package com.wy.review.controller.admin;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.Result;
import com.wy.review.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 全局学习数据总后台大屏控制器
 * 覆盖：全校错题总量统计、各专业学习活跃度、系统使用趋势图、全校方向分布
 */
@RestController
@RequestMapping("/admin/screen")
@RequireRole({"teacher", "admin"})
public class DataScreenController {

    private final StatsService statsService;

    public DataScreenController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 大屏专业柱点击下钻：该专业学生名册 */
    @GetMapping("/major-students")
    public Result<List<Map<String, Object>>> majorStudents(@RequestParam String major) {
        return Result.ok(statsService.screenMajorStudents(major));
    }

    /** 大屏顶部总量卡片 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(statsService.screenOverview());
    }

    /** 各专业学习活跃度图表 */
    @GetMapping("/major-active")
    public Result<List<Map<String, Object>>> majorActive() {
        return Result.ok(statsService.screenMajorActive());
    }

    /** 系统使用趋势图（近 30 天：新增错题/新增笔记/打卡人次） */
    @GetMapping("/trend")
    public Result<Map<String, List<Map<String, Object>>>> trend() {
        return Result.ok(statsService.screenTrend());
    }

    /** 全校错题技术方向分布饼图 */
    @GetMapping("/tech-dist")
    public Result<List<Map<String, Object>>> techDist() {
        return Result.ok(statsService.screenTechDist());
    }
}
