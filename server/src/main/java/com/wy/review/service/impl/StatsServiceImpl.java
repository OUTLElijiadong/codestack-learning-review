package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wy.review.common.UserContext;
import com.wy.review.entity.*;
import com.wy.review.mapper.*;
import com.wy.review.service.StatsService;
import com.wy.review.vo.OverviewVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习数据统计服务实现
 * 所有聚合查询用 QueryWrapper.select(原生SQL片段) + selectMaps 完成，
 * 单表 GROUP BY 各自走索引，互不 JOIN，SQL 简洁可查
 */
@Service
public class StatsServiceImpl implements StatsService {

    private final MistakeMapper mistakeMapper;
    private final NoteMapper noteMapper;
    private final ReviewTaskMapper taskMapper;
    private final CheckInMapper checkInMapper;
    private final StudyTimeLogMapper studyTimeMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;

    public StatsServiceImpl(MistakeMapper mistakeMapper, NoteMapper noteMapper,
                            ReviewTaskMapper taskMapper, CheckInMapper checkInMapper,
                            StudyTimeLogMapper studyTimeMapper, UserMapper userMapper,
                            QuestionMapper questionMapper) {
        this.mistakeMapper = mistakeMapper;
        this.noteMapper = noteMapper;
        this.taskMapper = taskMapper;
        this.checkInMapper = checkInMapper;
        this.studyTimeMapper = studyTimeMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
    }

    // ------------------------------------------------------------------
    // 学生端
    // ------------------------------------------------------------------

    @Override
    public OverviewVO overview() {
        Long userId = UserContext.getUserId();
        OverviewVO vo = new OverviewVO();

        vo.setMistakeTotal(mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)));
        vo.setFavoriteTotal(mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId).eq(Mistake::getIsFavorite, 1)));
        vo.setNoteTotal(noteMapper.selectCount(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)));

        // 本周新增（周一 00:00 起算），统计卡趋势 chip 用真实数据
        LocalDateTime weekStart = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        vo.setMistakeWeekNew(mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId).ge(Mistake::getCreateTime, weekStart)));
        vo.setNoteWeekNew(noteMapper.selectCount(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId).ge(Note::getCreateTime, weekStart)));

        // 今日任务进度
        ReviewTask today = taskMapper.selectOne(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getUserId, userId)
                .eq(ReviewTask::getTaskDate, LocalDate.now()));
        vo.setTodayTarget(today == null ? 0 : today.getTargetCount());
        vo.setTodayFinished(today == null ? 0 : today.getCompletedCount());
        vo.setTodayDone(today != null && today.getStatus() != null && today.getStatus() == 1);

        // 今日是否打卡
        Long checked = checkInMapper.selectCount(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckDate, LocalDate.now()));
        vo.setTodayChecked(checked != null && checked > 0);

        vo.setCheckInDays(continuousCheckInDays(userId));

        // 累计学习时长
        List<Map<String, Object>> sums = studyTimeMapper.selectMaps(new QueryWrapper<StudyTimeLog>()
                .select("IFNULL(SUM(duration),0) AS total")
                .eq("user_id", userId));
        vo.setTotalMinutes(sums.isEmpty() ? 0L : Long.valueOf(sums.get(0).get("total").toString()));
        return vo;
    }

    @Override
    public List<Map<String, Object>> weeklyMistakeGrowth() {
        // 按 ISO 年-周分组（%x-%v 跨年不出错），近 8 周
        return mistakeMapper.selectMaps(new QueryWrapper<Mistake>()
                .select("DATE_FORMAT(create_time, '%x-%v') AS week", "COUNT(*) AS count")
                .eq("user_id", UserContext.getUserId())
                .ge("create_time", LocalDate.now().minusWeeks(8).atStartOfDay())
                .groupBy("DATE_FORMAT(create_time, '%x-%v')")
                .orderByAsc("week"));
    }

    @Override
    public List<Map<String, Object>> techDirectionPie() {
        return mistakeMapper.selectMaps(new QueryWrapper<Mistake>()
                .select("tech_direction AS name", "COUNT(*) AS value")
                .eq("user_id", UserContext.getUserId())
                .groupBy("tech_direction"));
    }

    @Override
    public List<Map<String, Object>> dailyDuration() {
        return studyTimeMapper.selectMaps(new QueryWrapper<StudyTimeLog>()
                .select("study_date AS date", "SUM(duration) AS minutes")
                .eq("user_id", UserContext.getUserId())
                .ge("study_date", LocalDate.now().minusDays(14))
                .groupBy("study_date")
                .orderByAsc("study_date"));
    }

    @Override
    public Map<String, Object> completion() {
        Long userId = UserContext.getUserId();
        LocalDate since = LocalDate.now().minusDays(30);
        List<ReviewTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getUserId, userId)
                .ge(ReviewTask::getTaskDate, since));
        int totalDays = tasks.size();
        int doneDays = (int) tasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
        int totalReviewed = tasks.stream()
                .mapToInt(t -> t.getCompletedCount() == null ? 0 : t.getCompletedCount()).sum();
        double rate = totalDays == 0 ? 0 : Math.round(doneDays * 1000.0 / totalDays) / 10.0;
        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", totalDays);
        result.put("doneDays", doneDays);
        result.put("rate", rate);
        result.put("checkInDays", continuousCheckInDays(userId));
        result.put("totalReviewed", totalReviewed);
        return result;
    }

    // ------------------------------------------------------------------
    // 管理端数据大屏
    // ------------------------------------------------------------------

    @Override
    public Map<String, Object> screenOverview() {
        Long mistakeTotal = mistakeMapper.selectCount(null);
        Long mistakeToday = mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .ge(Mistake::getCreateTime, LocalDate.now().atStartOfDay()));
        Long studentTotal = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student"));
        Long noteTotal = noteMapper.selectCount(null);
        Long questionTotal = questionMapper.selectCount(null);
        Long checkinToday = checkInMapper.selectCount(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getCheckDate, LocalDate.now()));
        Long frozenTotal = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 0));
        Map<String, Object> result = new HashMap<>();
        result.put("mistakeTotal", mistakeTotal);
        result.put("mistakeToday", mistakeToday);
        result.put("studentTotal", studentTotal);
        result.put("noteTotal", noteTotal);
        result.put("questionTotal", questionTotal);
        result.put("checkinToday", checkinToday);
        result.put("frozenTotal", frozenTotal);
        return result;
    }

    @Override
    public List<Map<String, Object>> screenMajorActive() {
        // 按专业分组：总人数 + 近 7 天有登录行为的活跃人数
        return userMapper.selectMaps(new QueryWrapper<User>()
                .select("major",
                        "COUNT(*) AS totalUsers",
                        "SUM(CASE WHEN last_login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS activeUsers")
                .eq("role", "student")
                .isNotNull("major")
                .ne("major", "")
                .groupBy("major")
                .orderByDesc("activeUsers"));
    }

    @Override
    public Map<String, List<Map<String, Object>>> screenTrend() {
        LocalDate since = LocalDate.now().minusDays(30);
        List<Map<String, Object>> mistakeDaily = mistakeMapper.selectMaps(new QueryWrapper<Mistake>()
                .select("DATE(create_time) AS dt", "COUNT(*) AS cnt")
                .ge("create_time", since.atStartOfDay())
                .groupBy("DATE(create_time)"));
        List<Map<String, Object>> noteDaily = noteMapper.selectMaps(new QueryWrapper<Note>()
                .select("DATE(create_time) AS dt", "COUNT(*) AS cnt")
                .ge("create_time", since.atStartOfDay())
                .groupBy("DATE(create_time)"));
        List<Map<String, Object>> checkinDaily = checkInMapper.selectMaps(new QueryWrapper<CheckIn>()
                .select("check_date AS dt", "COUNT(*) AS cnt")
                .ge("check_date", since)
                .groupBy("check_date"));
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("mistake", mistakeDaily);
        result.put("note", noteDaily);
        result.put("checkin", checkinDaily);
        return result;
    }

    @Override
    public List<Map<String, Object>> screenTechDist() {
        return mistakeMapper.selectMaps(new QueryWrapper<Mistake>()
                .select("tech_direction AS name", "COUNT(*) AS value")
                .groupBy("tech_direction"));
    }

    @Override
    public List<Map<String, Object>> screenMajorStudents(String major) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student")
                .eq(User::getMajor, major)
                .orderByDesc(User::getLastLoginTime));
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        for (User u : users) {
            ids.add(u.getId());
        }
        Map<Long, Long> mistakeCnt = new HashMap<>();
        for (Map<String, Object> m : mistakeMapper.selectMaps(new QueryWrapper<Mistake>()
                .select("user_id", "COUNT(*) AS cnt").in("user_id", ids).groupBy("user_id"))) {
            mistakeCnt.put(((Number) m.get("user_id")).longValue(), ((Number) m.get("cnt")).longValue());
        }
        Map<Long, Long> checkin7 = new HashMap<>();
        for (Map<String, Object> m : checkInMapper.selectMaps(new QueryWrapper<CheckIn>()
                .select("user_id", "COUNT(*) AS cnt")
                .ge("check_date", LocalDate.now().minusDays(6))
                .in("user_id", ids).groupBy("user_id"))) {
            checkin7.put(((Number) m.get("user_id")).longValue(), ((Number) m.get("cnt")).longValue());
        }
        LocalDateTime activeSince = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("nickname", u.getNickname());
            row.put("className", u.getClassName());
            row.put("mistakes", mistakeCnt.getOrDefault(u.getId(), 0L));
            row.put("checkinDays7", checkin7.getOrDefault(u.getId(), 0L));
            row.put("active", u.getLastLoginTime() != null && u.getLastLoginTime().isAfter(activeSince));
            result.add(row);
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 连续打卡天数：从今天（或昨天）向前逐日连续计数 */
    private Integer continuousCheckInDays(Long userId) {
        List<CheckIn> checks = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .orderByDesc(CheckIn::getCheckDate)
                .last("LIMIT 90"));
        if (checks.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        // 今天还没打卡就从昨天开始算连续段
        if (!checks.get(0).getCheckDate().equals(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int days = 0;
        for (CheckIn c : checks) {
            if (c.getCheckDate().equals(cursor)) {
                days++;
                cursor = cursor.minusDays(1);
            } else if (c.getCheckDate().isBefore(cursor)) {
                break;
            }
        }
        return days;
    }
}
