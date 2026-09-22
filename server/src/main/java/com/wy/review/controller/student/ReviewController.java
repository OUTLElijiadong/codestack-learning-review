package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.ReviewPlanDTO;
import com.wy.review.dto.StudyTimeDTO;
import com.wy.review.entity.ReviewPlan;
import com.wy.review.entity.ReviewTask;
import com.wy.review.service.ReviewService;
import com.wy.review.vo.ReviewCalendarVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 每日编程刷题复盘计划控制器（学生端）
 * 覆盖：自定义复盘任务、每日打卡签到、完成自动打勾、未完成提醒数据、复盘日历
 */
@RestController
@RequestMapping("/student/review")
@RequireRole("student")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 查看我的复盘计划 */
    @GetMapping("/plan")
    public Result<ReviewPlan> getPlan() {
        return Result.ok(reviewService.getPlan());
    }

    /** 自定义每日复盘任务：设置每天复习几道错题 */
    @PutMapping("/plan")
    public Result<Void> savePlan(@Validated @RequestBody ReviewPlanDTO dto) {
        reviewService.savePlan(dto);
        return Result.ok();
    }

    /** 今日复盘任务（含推荐复习错题；同时是未完成自动提醒的数据源） */
    @GetMapping("/today")
    public Result<Map<String, Object>> today() {
        return Result.ok(reviewService.today());
    }

    /** 完成一道错题复习（达标自动打勾并联动打卡） */
    @PostMapping("/finish/{mistakeId}")
    public Result<Map<String, Object>> finish(@PathVariable Long mistakeId) {
        return Result.ok(reviewService.finish(mistakeId));
    }

    /** 每日打卡签到 */
    @PostMapping("/sign")
    public Result<Void> sign() {
        reviewService.sign();
        return Result.ok();
    }

    /** 复盘日历：按月查询（month=yyyy-MM），展示哪天学了哪天没学 */
    @GetMapping("/calendar")
    public Result<List<ReviewCalendarVO>> calendar(@RequestParam String month) {
        return Result.ok(reviewService.calendar(month));
    }

    /** 复盘历史记录分页 */
    @GetMapping("/records")
    public Result<PageResult<ReviewTask>> records(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(reviewService.records(pageNum, pageSize));
    }

    /** 上报学习时长（每日学习时长折线图的写入入口） */
    @PostMapping("/study-time")
    public Result<Void> reportStudyTime(@Validated @RequestBody StudyTimeDTO dto) {
        reviewService.reportStudyTime(dto);
        return Result.ok();
    }
}
