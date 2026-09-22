package com.wy.review.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.ReviewPlanDTO;
import com.wy.review.dto.StudyTimeDTO;
import com.wy.review.entity.*;
import com.wy.review.mapper.*;
import com.wy.review.service.ReviewService;
import com.wy.review.vo.ReviewCalendarVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日复盘计划服务实现
 * 核心链路：计划(review_plan 一人一套) → 每日任务(review_task 一人一天一行)
 *          → 完成复习自动打勾 → 打卡(check_in) → 日历/提醒/完成率统计
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewPlanMapper planMapper;
    private final ReviewTaskMapper taskMapper;
    private final CheckInMapper checkInMapper;
    private final StudyTimeLogMapper studyTimeMapper;
    private final MistakeMapper mistakeMapper;

    public ReviewServiceImpl(ReviewPlanMapper planMapper, ReviewTaskMapper taskMapper,
                             CheckInMapper checkInMapper, StudyTimeLogMapper studyTimeMapper,
                             MistakeMapper mistakeMapper) {
        this.planMapper = planMapper;
        this.taskMapper = taskMapper;
        this.checkInMapper = checkInMapper;
        this.studyTimeMapper = studyTimeMapper;
        this.mistakeMapper = mistakeMapper;
    }

    @Override
    public ReviewPlan getPlan() {
        ReviewPlan plan = planMapper.selectOne(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, UserContext.getUserId()));
        if (plan == null) {
            // 未设置过计划时返回默认值（不落库，保存时才插入）
            plan = new ReviewPlan();
            plan.setPlanName("每日复盘计划");
            plan.setDailyCount(5);
            plan.setRemindEnabled(1);
            plan.setStatus(1);
        }
        return plan;
    }

    @Override
    public void savePlan(ReviewPlanDTO dto) {
        Long userId = UserContext.getUserId();
        ReviewPlan exist = planMapper.selectOne(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId));
        if (exist == null) {
            ReviewPlan plan = new ReviewPlan();
            plan.setUserId(userId);
            plan.setPlanName(dto.getPlanName() == null || dto.getPlanName().isEmpty()
                    ? "每日复盘计划" : dto.getPlanName());
            plan.setDailyCount(dto.getDailyCount());
            plan.setRemindEnabled(dto.getRemindEnabled() == null ? 1 : dto.getRemindEnabled());
            plan.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            planMapper.insert(plan);
        } else {
            ReviewPlan update = new ReviewPlan();
            update.setId(exist.getId());
            if (dto.getPlanName() != null && !dto.getPlanName().isEmpty()) {
                update.setPlanName(dto.getPlanName());
            }
            update.setDailyCount(dto.getDailyCount());
            if (dto.getRemindEnabled() != null) update.setRemindEnabled(dto.getRemindEnabled());
            if (dto.getStatus() != null) update.setStatus(dto.getStatus());
            planMapper.updateById(update);
        }
    }

    @Override
    public Map<String, Object> today() {
        Long userId = UserContext.getUserId();
        Map<String, Object> result = new HashMap<>();
        ReviewPlan plan = getPlan();
        boolean enabled = plan.getStatus() != null && plan.getStatus() == 1;
        result.put("enabled", enabled);
        result.put("plan", plan);

        if (!enabled) {
            result.put("recommends", Collections.emptyList());
            return result;
        }

        // 今日任务不存在则按计划快照生成（target_count 快照，改计划不影响当天）
        ReviewTask task = getOrCreateTodayTask(userId, plan.getDailyCount());
        result.put("targetCount", task.getTargetCount());
        result.put("completedCount", task.getCompletedCount());
        result.put("status", task.getStatus());
        result.put("finished", task.getStatus() != null && task.getStatus() == 1);
        result.put("remind", plan.getRemindEnabled() != null && plan.getRemindEnabled() == 1
                && task.getStatus() != null && task.getStatus() == 0);
        result.put("checked", isCheckedToday(userId));

        // 推荐复习列表：排除今日已复习，收藏优先 + 最久未复习优先，取 dailyCount 条
        List<Long> reviewed = parseIds(task.getReviewedIds());
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .eq(Mistake::getAuditStatus, 1)
                .orderByDesc(Mistake::getIsFavorite)
                .orderByAsc(Mistake::getLastReviewTime)
                .orderByDesc(Mistake::getCreateTime)
                .last("LIMIT " + Math.max(plan.getDailyCount(), 5));
        if (!reviewed.isEmpty()) {
            wrapper.notIn(Mistake::getId, reviewed);
        }
        List<Mistake> recommends = mistakeMapper.selectList(wrapper);
        result.put("recommends", recommends);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> finish(Long mistakeId) {
        Long userId = UserContext.getUserId();
        Mistake mistake = mistakeMapper.selectById(mistakeId);
        if (mistake == null || !mistake.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        ReviewPlan plan = getPlan();
        ReviewTask task = getOrCreateTodayTask(userId, plan.getDailyCount());

        List<Long> reviewed = parseIds(task.getReviewedIds());
        if (reviewed.contains(mistakeId)) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "该错题今日已完成复习");
        }
        reviewed.add(mistakeId);

        ReviewTask update = new ReviewTask();
        update.setId(task.getId());
        update.setReviewedIds(JSONUtil.toJsonStr(reviewed));
        int completed = (task.getCompletedCount() == null ? 0 : task.getCompletedCount()) + 1;
        update.setCompletedCount(completed);
        // 达到当日目标 → 自动打勾完成（学习完成自动打勾），并联动打卡
        if (completed >= task.getTargetCount() && (task.getStatus() == null || task.getStatus() == 0)) {
            update.setStatus(1);
            update.setFinishTime(LocalDateTime.now());
        }
        taskMapper.updateById(update);

        // 错题侧：复习次数 +1、刷新最近复习时间
        Mistake m = new Mistake();
        m.setId(mistakeId);
        m.setReviewCount((mistake.getReviewCount() == null ? 0 : mistake.getReviewCount()) + 1);
        m.setLastReviewTime(LocalDateTime.now());
        mistakeMapper.updateById(m);

        if (update.getStatus() != null && update.getStatus() == 1) {
            signQuietly(userId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("completedCount", completed);
        result.put("targetCount", task.getTargetCount());
        result.put("finished", completed >= task.getTargetCount());
        return result;
    }

    @Override
    public void sign() {
        Long userId = UserContext.getUserId();
        if (isCheckedToday(userId)) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "今日已打卡");
        }
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(LocalDate.now());
        checkIn.setCheckTime(LocalDateTime.now());
        try {
            checkInMapper.insert(checkIn);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "今日已打卡");
        }
    }

    @Override
    public List<ReviewCalendarVO> calendar(String month) {
        Long userId = UserContext.getUserId();
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<ReviewTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getUserId, userId)
                .between(ReviewTask::getTaskDate, start, end));
        List<CheckIn> checks = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .between(CheckIn::getCheckDate, start, end));

        Map<LocalDate, ReviewTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(ReviewTask::getTaskDate, t -> t));
        Set<LocalDate> checkSet = checks.stream().map(CheckIn::getCheckDate).collect(Collectors.toSet());

        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(taskMap.keySet());
        dates.addAll(checkSet);

        List<ReviewCalendarVO> list = new ArrayList<>();
        for (LocalDate date : dates) {
            ReviewCalendarVO vo = new ReviewCalendarVO();
            vo.setDate(date.toString());
            ReviewTask t = taskMap.get(date);
            vo.setTargetCount(t == null ? 0 : t.getTargetCount());
            vo.setCompletedCount(t == null ? 0 : t.getCompletedCount());
            vo.setStatus(t == null ? null : t.getStatus());
            vo.setChecked(checkSet.contains(date));
            list.add(vo);
        }
        return list;
    }

    @Override
    public PageResult<ReviewTask> records(Integer pageNum, Integer pageSize) {
        Page<ReviewTask> page = taskMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ReviewTask>()
                        .eq(ReviewTask::getUserId, UserContext.getUserId())
                        .orderByDesc(ReviewTask::getTaskDate));
        return PageResult.of(page);
    }

    @Override
    public void reportStudyTime(StudyTimeDTO dto) {
        StudyTimeLog log = new StudyTimeLog();
        log.setUserId(UserContext.getUserId());
        log.setStudyDate(LocalDate.now());
        log.setDuration(dto.getDuration());
        log.setSource(dto.getSource() == null || dto.getSource().isEmpty() ? "manual" : dto.getSource());
        studyTimeMapper.insert(log);
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 获取（不存在则创建）今日复盘任务行 */
    private ReviewTask getOrCreateTodayTask(Long userId, Integer dailyCount) {
        LocalDate today = LocalDate.now();
        ReviewTask task = taskMapper.selectOne(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getUserId, userId)
                .eq(ReviewTask::getTaskDate, today));
        if (task == null) {
            task = new ReviewTask();
            task.setUserId(userId);
            task.setTaskDate(today);
            task.setTargetCount(dailyCount == null ? 5 : dailyCount);
            task.setCompletedCount(0);
            task.setReviewedIds("[]");
            task.setStatus(0);
            try {
                taskMapper.insert(task);
            } catch (DuplicateKeyException e) {
                // 并发下已被插入，重新查询即可
                task = taskMapper.selectOne(new LambdaQueryWrapper<ReviewTask>()
                        .eq(ReviewTask::getUserId, userId)
                        .eq(ReviewTask::getTaskDate, today));
            }
        }
        return task;
    }

    private boolean isCheckedToday(Long userId) {
        Long count = checkInMapper.selectCount(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckDate, LocalDate.now()));
        return count != null && count > 0;
    }

    /** 达标自动打卡：已打过则静默跳过 */
    private void signQuietly(Long userId) {
        if (isCheckedToday(userId)) {
            return;
        }
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(LocalDate.now());
        checkIn.setCheckTime(LocalDateTime.now());
        try {
            checkInMapper.insert(checkIn);
        } catch (DuplicateKeyException ignored) {
            // 唯一键兜底，重复打卡静默忽略
        }
    }

    private List<Long> parseIds(String json) {
        if (json == null || !JSONUtil.isTypeJSON(json)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(JSONUtil.toList(json, Long.class));
    }
}
