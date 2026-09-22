package com.wy.review.config;

import com.wy.review.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 生产演示数据初始化器：首次启动（库中还没有任何学生）时，向数据库写入一套
 * 校规模的仿真业务数据，模拟"系统已正常运行两个月"的状态。
 *
 * 与前端假数据不同，这里全部是真实入库的行——大屏每个数字、学生端每条记录
 * 都能落到具体行上，任何页面操作（复盘/打卡/发笔记/审核）都会实时改变统计。
 *
 * 触发与安全：
 *   · 只在 role=student 的用户数为 0 时执行一次（已有学生=已在用，绝不打扰）；
 *   · 固定随机种子(42)，重复初始化结果可复现；
 *   · 演示学生统一密码 123456（BCrypt 单哈希复用），已知账号 student01~student05。
 *
 * 运行顺序：SchemaInitializer(ApplicationRunner@1) 建表 → DataInitializer(@2) 账号/分类/敏感词 → 本类(@3)。
 * 注意：三类必须同为 CommandLineRunner 才能靠 @Order 排先后（Spring 先跑完所有 ApplicationRunner 再跑 CommandLineRunner）。
 */
@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;
    private final Random random = new Random(42L);

    /* ---------------- 数据池 ---------------- */

    private static final String[] MAJORS = {"软件工程", "大数据管理与应用", "计算机应用技术", "网络工程", "人工智能", "信息安全"};
    private static final int[] MAJOR_COUNTS = {18, 8, 7, 4, 3, 2};
    private static final String[][] CLASS_NAMES = {
            {"软件2301班", "软件2302班"}, {"大数据2301班"}, {"计科2301班"}, {"网工2301班"}, {"人工智能2301班"}, {"信安2301班"}
    };

    private static final String[] SURNAMES = {"王", "李", "张", "刘", "陈", "杨", "赵", "周", "吴", "徐", "孙", "马", "林", "何", "高"};
    private static final String[] GIVENS = {"一诺", "沐宸", "浩然", "若彤", "子墨", "欣怡", "俊杰", "雨桐", "思远", "乐言",
            "佳琪", "晨曦", "宇航", "书瑶", "明轩", "可欣", "文博", "雅静", "睿哲", "晓彤"};
    private static final String[] DIRECTIONS = {"frontend", "backend", "test"};

    private static final String[] TECHS = {"Vue", "SpringBoot", "MySQL", "算法", "其他"};
    private static final double[] TECH_WEIGHT = {0.26, 0.24, 0.19, 0.17, 0.14};
    private static final String[][] ERROR_TYPES = {{"grammar", "logic", "api"}, {"logic", "api", "env"}, {"api", "env"}, {"logic"}, {"env", "api"}};

    private static final String[][] MISTAKE_TITLES = {
            {"v-for 未绑定 key 导致列表渲染错乱", "computed 里发请求引发死循环", "watch 深度监听新旧值同引用", "ref 在模板自动展开但 JS 里要 .value",
                    "父子组件 v-model 双向绑定失效", "keep-alive 缓存后生命周期不再触发", "provide/inject 响应性丢失"},
            {"@Value 注入静态字段为 null", "事务注解同类自调用失效", "跨域预检 OPTIONS 被拦截器拦下", "jar 包内读取 resources 文件失败",
                    "全局异常处理吞掉参数校验信息", "定时任务线程池打满日志延迟", "RestTemplate 超时未配置拖垮服务"},
            {"utf8mb4 才能存 emoji", "GROUP BY 遇到 only_full_group_by 报错", "联合索引不满足最左前缀失效", "隐式类型转换导致索引失效",
                    "大事务删除千万级数据锁表", "慢查询未走覆盖索引导出全表扫描", "连接池耗尽等待超时"},
            {"二分查找边界死循环", "递归缺终止条件栈溢出", "快排最坏复杂度退化未随机化", "动态规划状态转移遗漏边界",
                    "链表反转丢失后续节点", "字符串回文中心扩展越界", "堆的下沉操作写错边界"},
            {"Nginx 反向代理 WebSocket 握手 400", "Git rebase 冲突解决后提交丢失", "Docker 端口映射后宿主机访问不通",
                    "Redis 缓存击穿打挂数据库", "Linux crontab 环境变量缺失", "正则贪婪匹配吃掉整行"}
    };
    private static final String[] TAG_POOL = {"面试高频", "语法陷阱", "项目实战", "环境配置", "期末复习", "易错边界"};

    private static final String[] NOTE_TITLES = {
            "Vue3 组合式 API 使用心得", "SpringBoot 事务失效八大场景", "MySQL 索引失效场景清单", "算法刷题周记", "环境配置踩坑备忘",
            "JWT 鉴权流程图解", "MyBatis-Plus 逻辑删除实践", "前后端联调排错手册", "Redis 缓存三兄弟详解", "毕业设计进度记录",
            "ECharts 图表定制笔记", "RESTful 接口设计规范", "单元测试入门总结", "Linux 常用命令整理", "正则表达式速查"
    };
    private static final String[] CATEGORY_POOL = {"前端进阶", "后端笔记", "面试整理", "课程作业", "读源码"};

    private static final String[] QUESTION_TITLES = {
            "Vue3 里 ref 和 reactive 到底该怎么选？", "SpringBoot @Transactional 什么情况下会失效？", "MySQL 联合索引遇到范围查询后面的列还能用上吗？",
            "二分查找总是写错边界，有什么口诀吗？", "JWT 无状态登录怎么实现踢人下线？", "前端打包后接口跨域怎么配 Nginx？",
            "MyBatis-Plus 逻辑删除和唯一索引冲突怎么解决？", "Redis 分布式锁有什么坑？", "docker compose 里 MySQL 时区不对怎么改？",
            "实习面试被问索引底层 B+树，怎么答？", "vue-router hash 和 history 模式怎么选？", "BCrypt 加盐后密码怎么校验？",
            "ECharts 大屏自适应窗口怎么处理？", "接口幂等性怎么设计？", "文件上传大文件断点续传思路？"
    };

    @Override
    public void run(String... args) {
        Long studentCount;
        try {
            studentCount = jdbc.queryForObject("SELECT COUNT(*) FROM user WHERE role='student'", Long.class);
        } catch (Exception e) {
            log.warn("演示数据初始化：数据表不存在，跳过（SchemaInitializer 会先建表）");
            return;
        }
        if (studentCount != null && studentCount > 0) {
            log.info("演示数据初始化：已有 {} 名学生，跳过（不触碰在用数据）", studentCount);
            return;
        }
        log.info("首次启动：开始写入生产演示数据（约 42 名学生 / 360+ 错题 / 150+ 笔记 / 55+ 问答 / 30 天复盘打卡历史）...");
        long t0 = System.currentTimeMillis();

        try {
            List<StudentSeed> students = seedUsers();
            List<long[]> mistakeIds = seedMistakes(students);
            seedTagsAndRels(students, mistakeIds);
            seedNotes(students);
            seedQuestions(students);
            seedReviewPlansTasksCheckins(students, mistakeIds);
            seedStudyTime(students);
            seedAnnouncementsAndReads(students);
            seedOperationLogs();
            log.info("生产演示数据写入完成，耗时 {}ms。演示学生账号：student01~student05，密码统一 123456",
                    System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("演示数据写入失败：{}", e.getMessage(), e);
        }
    }

    /* ---------------- 学生 ---------------- */

    private List<StudentSeed> seedUsers() {
        String pwd = PasswordUtil.encode("123456");
        String ansHash = PasswordUtil.encode("vue");
        List<StudentSeed> list = new ArrayList<>();
        List<Object[]> rows = new ArrayList<>();
        int seq = 0;
        for (int m = 0; m < MAJORS.length; m++) {
            String[] classes = CLASS_NAMES[m];
            for (int i = 0; i < MAJOR_COUNTS[m]; i++) {
                seq++;
                StudentSeed s = new StudentSeed();
                s.seq = seq;
                s.major = MAJORS[m];
                s.className = classes[i % classes.length];
                s.username = seq <= 5 ? String.format("student%02d", seq)
                        : SURNAMES[seq % SURNAMES.length].toLowerCase() + GIVENS[(seq * 3) % GIVENS.length] + seq;
                s.username = s.username.length() > 20 ? s.username.substring(0, 20) : s.username;
                s.nickname = SURNAMES[seq % SURNAMES.length] + GIVENS[(seq * 7 + 3) % GIVENS.length];
                s.direction = DIRECTIONS[seq % DIRECTIONS.length];
                s.active7d = random.nextDouble() < 0.72;
                // 近7天活跃的登录时间 0~6 天前；不活跃的 8~40 天前
                int backDays = s.active7d ? random.nextInt(7) : 8 + random.nextInt(32);
                s.lastLogin = LocalDateTime.now().minusDays(backDays).minusHours(random.nextInt(20));
                s.frozen = (m == MAJORS.length - 1 && i == 0); // 信息安全第一名学生冻结，演示冻结账号数
                rows.add(new Object[]{s.username, pwd, s.nickname, seq % 3, s.direction, s.major, s.className,
                        s.lastLogin, s.frozen ? 0 : 1, ansHash,
                        Timestamp.valueOf(s.lastLogin.minusDays(50))}); // 注册时间早于最近登录
                list.add(s);
            }
        }
        jdbc.batchUpdate("INSERT INTO user(username,password,nickname,gender,learn_direction,major,class_name,last_login_time,status,security_question,security_answer,create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,'我最喜欢的编程语言',?,?)", rows);
        List<Map<String, Object>> ids = jdbc.queryForList("SELECT id, username FROM user WHERE role='student'");
        Map<String, Long> byName = new HashMap<>();
        for (Map<String, Object> r : ids) {
            byName.put(String.valueOf(r.get("username")), ((Number) r.get("id")).longValue());
        }
        for (StudentSeed s : list) {
            s.id = byName.get(s.username);
        }
        return list;
    }

    /* ---------------- 错题 ---------------- */

    private List<long[]> seedMistakes(List<StudentSeed> students) {
        List<Object[]> rows = new ArrayList<>();
        long total = 0;
        Set<Integer> backDaysToday = new HashSet<>();
        for (StudentSeed s : students) {
            if (s.seq > 5 && s.active7d && random.nextInt(100) < 22) {
                backDaysToday.add(s.seq);
            }
        }
        for (StudentSeed s : students) {
            int count = 4 + random.nextInt(11); // 4~14 道
            if (s.seq <= 5) {
                count = 12 + random.nextInt(4); // 演示账号更丰富
            }
            for (int i = 0; i < count; i++) {
                int tech = pickTech();
                String[] titles = MISTAKE_TITLES[tech];
                String title = titles[random.nextInt(titles.length)];
                // 日期：60 天内，近期权重更高；student01 保证今天有 2 道
                int backDays;
                if (s.seq == 1 && i < 2) {
                    backDays = 0;
                } else if (backDaysToday.contains(s.seq) && i == 0) {
                    backDays = 0; // 少量学生今天各录入 1 道，控制"今日新增"在合理区间
                } else {
                    double r = random.nextDouble();
                    backDays = r < 0.20 ? 1 + random.nextInt(3) : r < 0.48 ? 3 + random.nextInt(6) : random.nextInt(60);
                }
                LocalDateTime time = LocalDateTime.now().minusDays(backDays)
                        .withHour(9 + random.nextInt(12)).withMinute(random.nextInt(60)).withSecond(0);
                String errorType = ERROR_TYPES[tech][random.nextInt(ERROR_TYPES[tech].length)];
                rows.add(new Object[]{s.id, title, TECHS[tech], errorType,
                        "错误代码片段见截图与笔记", "控制台报错：" + title, "定位到根因后按正确方案修复，并记录到笔记",
                        "[]", random.nextInt(100) < 15 ? 1 : 0, 0, time, time});
                total++;
                if (rows.size() >= 500) {
                    flushMistakes(rows);
                }
            }
        }
        flushMistakes(rows);
        // 回读错题 id（按 user 分组，按时间排序，供复盘任务引用）
        List<long[]> mistakeIds = new ArrayList<>(); // [id, userId, epochDay]
        List<Map<String, Object>> all = jdbc.queryForList(
                "SELECT id, user_id, TO_DAYS(create_time) - 719528 AS d FROM mistake ORDER BY user_id, create_time");
        for (Map<String, Object> r : all) {
            mistakeIds.add(new long[]{((Number) r.get("id")).longValue(), ((Number) r.get("user_id")).longValue(),
                    ((Number) r.get("d")).longValue()});
        }
        log.info("演示错题 {} 道", total);
        return mistakeIds;
    }

    private void flushMistakes(List<Object[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        jdbc.batchUpdate("INSERT INTO mistake(user_id,title,tech_direction,error_type,error_code,error_msg,solution,images,is_favorite,is_top,create_time,update_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", new ArrayList<>(rows));
        rows.clear();
    }

    private int pickTech() {
        double r = random.nextDouble();
        double acc = 0;
        for (int i = 0; i < TECH_WEIGHT.length; i++) {
            acc += TECH_WEIGHT[i];
            if (r < acc) {
                return i;
            }
        }
        return TECHS.length - 1;
    }

    /* ---------------- 标签与关联 ---------------- */

    private void seedTagsAndRels(List<StudentSeed> students, List<long[]> mistakeIds) {
        List<Object[]> tagRows = new ArrayList<>();
        Map<Long, List<Long>> userTags = new HashMap<>();
        for (StudentSeed s : students) {
            int n = random.nextInt(4); // 0~3 个标签
            Set<String> used = new HashSet<>();
            List<Long> tagIds = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String name = TAG_POOL[random.nextInt(TAG_POOL.length)];
                if (!used.add(name)) {
                    continue;
                }
                tagRows.add(new Object[]{s.id, name, s.seq + ""});
                tagIds.add(-1L); // 占位，插完后回填
            }
            userTags.put(s.id, tagIds);
        }
        jdbc.batchUpdate("INSERT INTO mistake_tag(user_id,name,color) VALUES (?,?,?)", tagRows);
        List<Map<String, Object>> tid = jdbc.queryForList("SELECT id, user_id FROM mistake_tag");
        Map<Long, List<Long>> byUser = new HashMap<>();
        for (Map<String, Object> r : tid) {
            long uid = ((Number) r.get("user_id")).longValue();
            List<Long> l = byUser.get(uid);
            if (l == null) {
                l = new ArrayList<>();
                byUser.put(uid, l);
            }
            l.add(((Number) r.get("id")).longValue());
        }
        List<Object[]> rels = new ArrayList<>();
        for (long[] m : mistakeIds) {
            List<Long> tags = byUser.get(m[1]);
            if (tags != null && !tags.isEmpty() && random.nextInt(100) < 60) {
                rels.add(new Object[]{m[0], tags.get(random.nextInt(tags.size()))});
            }
        }
        jdbc.batchUpdate("INSERT INTO mistake_tag_rel(mistake_id,tag_id) VALUES (?,?)", rels);
        log.info("演示标签 {} 个，关联 {} 条", tagRows.size(), rels.size());
    }

    /* ---------------- 笔记与自建分类 ---------------- */

    private void seedNotes(List<StudentSeed> students) {
        // 自建分类
        List<Object[]> catRows = new ArrayList<>();
        Map<Long, List<Long>> userCats = new HashMap<>();
        for (StudentSeed s : students) {
            if (random.nextInt(100) < 55) {
                String name = CATEGORY_POOL[random.nextInt(CATEGORY_POOL.length)];
                catRows.add(new Object[]{s.id, name});
                userCats.put(s.id, new ArrayList<Long>());
            }
        }
        jdbc.batchUpdate("INSERT INTO note_category(user_id,name) VALUES (?,?)", catRows);
        List<Map<String, Object>> cids = jdbc.queryForList("SELECT id, user_id FROM note_category WHERE user_id > 0");
        for (Map<String, Object> r : cids) {
            long uid = ((Number) r.get("user_id")).longValue();
            userCats.get(uid).add(((Number) r.get("id")).longValue());
        }
        // 笔记
        List<Object[]> rows = new ArrayList<>();
        int pending = 0;
        int rejected = 0;
        for (StudentSeed s : students) {
            int count = 1 + random.nextInt(6);
            if (s.seq <= 5) {
                count = 5 + random.nextInt(3);
            }
            for (int i = 0; i < count; i++) {
                String title = NOTE_TITLES[random.nextInt(NOTE_TITLES.length)];
                boolean isPublic = random.nextInt(100) < 65;
                int audit = 1;
                String remark = null;
                if (isPublic) {
                    int dice = random.nextInt(100);
                    if (dice < 10 && pending < 8) {
                        audit = 0;
                        pending++;
                    } else if (dice < 15 && rejected < 4) {
                        audit = 2;
                        remark = "内容不完整，请补充示例后重新提交";
                        rejected++;
                    }
                }
                List<Long> cats = userCats.get(s.id);
                Long catId = (cats != null && !cats.isEmpty() && random.nextInt(100) < 50)
                        ? cats.get(random.nextInt(cats.size())) : (long) (1 + random.nextInt(7));
                int backDays = random.nextInt(45);
                LocalDateTime time = LocalDateTime.now().minusDays(backDays)
                        .withHour(10 + random.nextInt(10)).withMinute(random.nextInt(60));
                String content = "<p>" + title + "，记录本周学习与实践要点。</p>"
                        + "<pre><code>const key = 'demo'\nconsole.log(key)</code></pre>"
                        + "<p>持续更新中。</p>";
                rows.add(new Object[]{s.id, catId, title, content, "学习要点与实践记录",
                        isPublic ? 1 : 0, audit, remark, isPublic ? random.nextInt(80) : 0, time});
            }
        }
        jdbc.batchUpdate("INSERT INTO note(user_id,category_id,title,content,summary,is_public,audit_status,audit_remark,view_count,create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)", rows);
        log.info("演示笔记 {} 篇（待审核 {}，已下架 {}）", rows.size(), pending, rejected);
    }

    /* ---------------- 问答与评论 ---------------- */

    private void seedQuestions(List<StudentSeed> students) {
        List<StudentSeed> pool = new ArrayList<>(students);
        List<Object[]> qRows = new ArrayList<>();
        for (int i = 0; i < QUESTION_TITLES.length; i++) {
            StudentSeed s = pool.get(random.nextInt(pool.size()));
            String title = QUESTION_TITLES[i];
            String content = title + "\n```java\n// 相关代码\n```\n求大佬指点思路";
            LocalDateTime time = LocalDateTime.now().minusDays(random.nextInt(30)).withHour(10 + random.nextInt(10));
            qRows.add(new Object[]{s.id, title, content, TECHS[pickTech()], random.nextInt(100) < 25 ? 1 : 0,
                    5 + random.nextInt(60), time});
        }
        jdbc.batchUpdate("INSERT INTO question(user_id,title,content,tech_direction,status,view_count,create_time) "
                + "VALUES (?,?,?,?,?,?,?)", qRows);
        List<Map<String, Object>> qids = jdbc.queryForList("SELECT id, user_id FROM question ORDER BY id");

        // 先生成评论计划：每个问题 0~3 条一级，之后 40% 概率跟 1 条二级
        Map<Long, Long> qOwner = new HashMap<>();
        for (Map<String, Object> q : qids) {
            qOwner.put(((Number) q.get("id")).longValue(), ((Number) q.get("user_id")).longValue());
        }
        List<Object[]> level1 = new ArrayList<>();
        Map<Long, Integer> qL1Count = new HashMap<>(); // 问题 → 一级评论条数
        for (Long qid : qOwner.keySet()) {
            int n = random.nextInt(100) < 55 ? 1 + random.nextInt(3) : 0;
            for (int i = 0; i < n; i++) {
                StudentSeed replier = pool.get(random.nextInt(pool.size()));
                for (int t = 0; t < 6 && replier.id == qOwner.get(qid); t++) {
                    replier = pool.get(random.nextInt(pool.size()));
                }
                if (replier.id == qOwner.get(qid)) {
                    continue;
                }
                Timestamp time = Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(28)).withHour(9 + random.nextInt(12)));
                level1.add(new Object[]{qid, replier.id, "思路是对的，注意边界情况的处理，可以再看看官方文档第 " + (i + 1) + " 节。", time});
                qL1Count.merge(qid, 1, Integer::sum);
            }
        }
        if (!level1.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO comment(question_id,user_id,parent_id,root_id,reply_user_id,content,create_time) "
                    + "VALUES (?,?,0,0,NULL,?,?)", level1);
        }
        // 回读一级评论 id（按插入顺序），按问题分桶后挂二级回复
        List<Map<String, Object>> c1 = jdbc.queryForList("SELECT id, question_id, user_id FROM comment WHERE parent_id=0 ORDER BY id");
        Map<Long, List<long[]>> byQ = new LinkedHashMap<>(); // qid → [commentId, authorId]
        for (Map<String, Object> c : c1) {
            long qid = ((Number) c.get("question_id")).longValue();
            List<long[]> l = byQ.get(qid);
            if (l == null) {
                l = new ArrayList<>();
                byQ.put(qid, l);
            }
            l.add(new long[]{((Number) c.get("id")).longValue(), ((Number) c.get("user_id")).longValue()});
        }
        List<Object[]> level2 = new ArrayList<>();
        for (Map.Entry<Long, List<long[]>> e : byQ.entrySet()) {
            if (random.nextInt(100) >= 40 || e.getValue().isEmpty()) {
                continue;
            }
            long[] root = e.getValue().get(0);
            StudentSeed replier = pool.get(random.nextInt(pool.size()));
            if (replier.id == root[1]) {
                continue;
            }
            Timestamp time = Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(25)).withHour(9 + random.nextInt(12)));
            level2.add(new Object[]{e.getKey(), replier.id, root[0], root[0], root[1],
                    "回复 @" + nicknameOf(students, root[1]) + "：补充一点，官方文档里也有说明～", time});
        }
        if (!level2.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO comment(question_id,user_id,parent_id,root_id,reply_user_id,content,create_time) "
                    + "VALUES (?,?,?,?,?,?,?)", level2);
        }
        // answer_count = 该问题全部评论数
        jdbc.update("UPDATE question q SET answer_count = (SELECT COUNT(*) FROM comment c WHERE c.question_id = q.id AND c.deleted = 0)");
        log.info("演示问答 {} 条，评论 {}+{} 条", qRows.size(), level1.size(), level2.size());
    }

    private String nicknameOf(List<StudentSeed> students, long userId) {
        for (StudentSeed s : students) {
            if (s.id == userId) {
                return s.nickname;
            }
        }
        return "同学";
    }

    /* ---------------- 复盘计划 / 任务 / 打卡 ---------------- */

    private void seedReviewPlansTasksCheckins(List<StudentSeed> students, List<long[]> mistakeIds) {
        // 按学生归组错题
        Map<Long, List<long[]>> byUser = new HashMap<>();
        for (long[] m : mistakeIds) {
            List<long[]> l = byUser.get(m[1]);
            if (l == null) {
                l = new ArrayList<>();
                byUser.put(m[1], l);
            }
            l.add(m);
        }
        List<Object[]> plans = new ArrayList<>();
        List<Object[]> tasks = new ArrayList<>();
        List<Object[]> checkins = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int todayChecked = 0;
        for (StudentSeed s : students) {
            List<long[]> mine = byUser.get(s.id);
            if (mine == null || mine.isEmpty()) {
                continue;
            }
            int daily = random.nextInt(100) < 50 ? 3 : 5;
            if (s.seq <= 5) {
                daily = 3;
            }
            if (s.active7d || s.seq <= 5) {
                plans.add(new Object[]{s.id, "每日错题复盘", daily, 1, Timestamp.valueOf(LocalDateTime.now().minusDays(40))});
            }
            for (int back = 21; back >= 0; back--) {
                LocalDate date = today.minusDays(back);
                boolean isToday = back == 0;
                // 活跃学生大部分天有任务；不活跃的只有早期零星几天
                double taskProb = s.active7d || s.seq <= 5 ? 0.72 : 0.15;
                // 演示账号（student01~05）今天必安排任务；student01 差一道达标——登录后点一次"完成复习"即触发自动打卡，正好构成闭环演示
                if ((isToday && (s.seq <= 5 || (s.active7d && random.nextInt(100) < 70))) || (!isToday && random.nextDouble() < taskProb)) {
                    int target = daily;
                    int completed;
                    boolean done;
                    if (isToday) {
                        // 今天：部分已完成，少数已达标（达标的自动打卡）
                        done = random.nextInt(100) < 45;
                        completed = done ? target : random.nextInt(Math.max(1, target));
                        if (s.seq == 1) {
                            done = false;
                            completed = target - 1;
                        }
                    } else {
                        done = random.nextInt(100) < 70;
                        completed = done ? target : random.nextInt(Math.max(1, target));
                    }
                    // reviewed_ids：取该生任务日期当天及之前创建的错题（避免"复习了未来才创建的题"）
                    long taskDay = java.sql.Date.valueOf(date).toLocalDate().toEpochDay();
                    List<String> reviewed = new ArrayList<>();
                    int picked = 0;
                    for (long[] m : mine) {
                        if (picked >= completed) {
                            break;
                        }
                        if (m[2] <= taskDay) {
                            reviewed.add(String.valueOf(m[0]));
                            picked++;
                        }
                    }
                    completed = reviewed.size();
                    done = completed >= target;
                    String reviewedJson = reviewed.isEmpty() ? "[]" : toJsonArray(reviewed);
                    Timestamp finish = done ? Timestamp.valueOf(date.atTime(20 + random.nextInt(3), random.nextInt(60))) : null;
                    tasks.add(new Object[]{s.id, java.sql.Date.valueOf(date), target, completed, reviewedJson,
                            done ? 1 : 0, finish, Timestamp.valueOf(date.atTime(8, 0))});
                    if (done && random.nextInt(100) < 92) {
                        checkins.add(new Object[]{s.id, java.sql.Date.valueOf(date),
                                Timestamp.valueOf(date.atTime(21, 15 + random.nextInt(40)))});
                        if (isToday) {
                            todayChecked++;
                        }
                    }
                }
            }
        }
        jdbc.batchUpdate("INSERT INTO review_plan(user_id,plan_name,daily_count,remind_enabled,status,create_time) VALUES (?,?,?,?,1,?)", plans);
        // 回写被复习错题的累计次数与最近复习时间（与任务 reviewed_ids 对齐）
        for (Object[] t : tasks) {
            String json = String.valueOf(t[4]);
            if (json.length() <= 2) {
                continue;
            }
            Timestamp finish = (Timestamp) t[6];
            for (String one : json.replace("[", "").replace("]", "").replace("\"", "").split(",")) {
                if (one.isEmpty()) {
                    continue;
                }
                jdbc.update("UPDATE mistake SET review_count = review_count + 1, last_review_time = COALESCE(last_review_time, ?) WHERE id = ?",
                        finish != null ? finish : Timestamp.valueOf(LocalDateTime.now()), Long.valueOf(one.trim()));
            }
        }
        jdbc.batchUpdate("INSERT INTO review_task(user_id,task_date,target_count,completed_count,reviewed_ids,status,finish_time,create_time) VALUES (?,?,?,?,?,?,?,?)", tasks);
        jdbc.batchUpdate("INSERT INTO check_in(user_id,check_date,check_time) VALUES (?,?,?)", checkins);
        log.info("演示复盘计划 {} 份，任务 {} 天次，打卡 {} 人次（今日 {}）", plans.size(), tasks.size(), checkins.size(), todayChecked);
    }

    /* ---------------- 学习时长 ---------------- */

    private void seedStudyTime(List<StudentSeed> students) {
        List<Object[]> rows = new ArrayList<>();
        for (StudentSeed s : students) {
            if (!s.active7d && s.seq > 5) {
                continue;
            }
            for (int back = 13; back >= 0; back--) {
                if (random.nextDouble() < 0.62) {
                    int minutes = 20 + random.nextInt(110);
                    String source = new String[]{"review", "practice", "note"}[random.nextInt(3)];
                    rows.add(new Object[]{s.id, java.sql.Date.valueOf(LocalDate.now().minusDays(back)), minutes, source,
                            Timestamp.valueOf(LocalDate.now().minusDays(back).atTime(21, 30))});
                }
            }
        }
        jdbc.batchUpdate("INSERT INTO study_time_log(user_id,study_date,duration,source,create_time) VALUES (?,?,?,?,?)", rows);
        log.info("演示学习时长 {} 条（近 14 天）", rows.size());
    }

    /* ---------------- 公告与已读 ---------------- */

    private void seedAnnouncementsAndReads(List<StudentSeed> students) {
        LocalDateTime t1 = LocalDateTime.now().minusDays(9).withHour(9).withMinute(30);
        LocalDateTime t2 = LocalDateTime.now().minusDays(2).withHour(18).withMinute(0);
        jdbc.update("INSERT INTO announcement(title,content,type,publisher_id,publisher_name,status,publish_time,create_time) VALUES (?,?,?,?,?,1,?,?)",
                "「码栈」错题复盘系统正式上线", "各位同学：系统已正式上线！把每天的编程错题录入错题本，配合每日复盘计划巩固记忆，坚持复盘，稳步提升。", "notice", 1L, "admin", t1, t1);
        jdbc.update("INSERT INTO announcement(title,content,type,publisher_id,publisher_name,status,publish_time,create_time) VALUES (?,?,?,?,?,1,?,?)",
                "期中复习季：复盘计划打卡活动", "即日起坚持每日复盘打卡满 14 天的同学，将出现在大屏活跃榜上。学习数据实时统计，一起冲榜！", "notice", 2L, "teacher01", t2, t2);
        jdbc.update("INSERT INTO announcement(title,content,type,publisher_id,publisher_name,status,create_time) VALUES (?,?,?,?,?,0,?)",
                "五一假期系统维护通知（草稿）", "草稿：假期安排维护窗口，确认后发布。", "maintenance", 1L, "admin", LocalDateTime.now().minusDays(1));
        List<Map<String, Object>> anns = jdbc.queryForList("SELECT id FROM announcement ORDER BY id");
        Long oldAnn = ((Number) anns.get(0).get("id")).longValue();
        Long newAnn = ((Number) anns.get(1).get("id")).longValue();
        List<Object[]> reads = new ArrayList<>();
        for (StudentSeed s : students) {
            // 老公告大部分人已读；新公告少部分已读（student01 保持未读 → 登录可见弹窗）
            if (s.seq != 1 && random.nextInt(100) < 75) {
                reads.add(new Object[]{s.id, oldAnn, Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(8)))});
            }
            if (s.seq != 1 && random.nextInt(100) < 20) {
                reads.add(new Object[]{s.id, newAnn, Timestamp.valueOf(LocalDateTime.now().minusHours(random.nextInt(40)))});
            }
        }
        jdbc.batchUpdate("INSERT INTO announcement_read(user_id,announcement_id,read_time) VALUES (?,?,?)", reads);
        log.info("演示公告 3 条（2 发布 1 草稿），已读 {} 条", reads.size());
    }

    /* ---------------- 操作日志 ---------------- */

    private void seedOperationLogs() {
        List<Object[]> rows = new ArrayList<>();
        String[][] entries = {
                {"admin", "admin", "内容审核", "笔记审核通过", "PUT", "/api/admin/audit/note/1/pass", "AuditController.passNote"},
                {"teacher01", "teacher", "内容审核", "笔记审核通过", "PUT", "/api/admin/audit/note/3/pass", "AuditController.passNote"},
                {"teacher01", "teacher", "内容审核", "笔记下架", "PUT", "/api/admin/audit/note/5/reject", "AuditController.rejectNote"},
                {"admin", "admin", "用户管理", "冻结违规账号", "PUT", "/api/admin/user/40/freeze", "UserManageController.freeze"},
                {"admin", "admin", "用户管理", "重置学生密码", "PUT", "/api/admin/user/12/reset-password", "UserManageController.resetPassword"},
                {"admin", "admin", "公告管理", "发布公告", "POST", "/api/admin/announcement", "AnnouncementManageController.create"},
                {"teacher01", "teacher", "公告管理", "发布公告", "POST", "/api/admin/announcement", "AnnouncementManageController.create"},
                {"admin", "admin", "权限分级", "新增教师管理员", "POST", "/api/admin/manage/admin", "AdminManageController.create"},
                {"admin", "admin", "敏感词", "新增敏感词", "POST", "/api/admin/sensitive-word", "SensitiveWordController.create"},
                {"teacher01", "teacher", "内容审核", "错题巡查通过", "PUT", "/api/admin/audit/mistake/9/pass", "AuditController.passMistake"},
                {"admin", "admin", "用户管理", "解封申诉账号", "PUT", "/api/admin/user/38/unfreeze", "UserManageController.unfreeze"},
                {"admin", "admin", "权限分级", "重置管理员密码", "PUT", "/api/admin/manage/admin/2/password", "AdminManageController.resetPwd"},
        };
        for (int i = 0; i < entries.length; i++) {
            String[] e = entries[i];
            rows.add(new Object[]{e[0].equals("admin") ? 1L : 2L, e[0], e[1], e[2], e[3], e[4], e[5], e[6],
                    "192.168.1." + (20 + i), 20 + random.nextInt(180),
                    Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(25)).withHour(10 + random.nextInt(9)))});
        }
        jdbc.batchUpdate("INSERT INTO operation_log(user_id,username,role,module,operation,request_method,request_url,method,ip,result,cost_time,create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,1,?,?)", rows);
        log.info("演示操作日志 {} 条", rows.size());
    }

    /* ---------------- 工具 ---------------- */

    private String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(list.get(i)).append("\"");
        }
        return sb.append("]").toString();
    }

    /** 学生种子载体 */
    private static class StudentSeed {
        long id;
        int seq;
        String username;
        String nickname;
        String major;
        String className;
        String direction;
        boolean active7d;
        boolean frozen;
        LocalDateTime lastLogin;
    }
}
