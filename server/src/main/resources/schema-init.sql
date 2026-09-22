-- ============================================================================
-- 首次启动自动初始化脚本（无需手动执行！）
-- 由 SchemaInitializer 在检测到 user 表不存在时自动执行一次：
--   · 库不存在 → JDBC 参数 createDatabaseIfNotExist 自动创建
--   · 表不存在 → 执行本脚本建 16 张表（CREATE IF NOT EXISTS，幂等）
--   · 已有表   → 完全跳过，绝不影响已有数据
-- 与 sql/schema.sql 的表结构完全一致；手工建库仍可用那份脚本。
-- ============================================================================

-- ============================================================================
-- 基于Vue的个人代码学习错题集与编程笔记复盘系统 —— 建库建表脚本
-- 数据库：MySQL 8.0    库名：review_db    字符集：utf8mb4
-- 用法：在 MySQL 中直接执行本文件（IDEA Database 工具 / Navicat / 命令行均可）
-- 说明：本脚本只负责建库建表；演示账号与演示业务数据由后端启动时自动播种
--      （com.wy.review.config.DataInitializer），无需手工 INSERT
-- ============================================================================




-- ---------------------------------------------------------------------------
-- 1. 用户表（学生 / 教师=普通管理员 / 超级管理员 三类角色合一张表，role 区分）
--    合并理由：三类角色字段重合度超过 80%，登录鉴权逻辑完全一致，
--    分表只会带来三套登录代码与跨表统计 JOIN
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username          VARCHAR(32)  NOT NULL COMMENT '登录用户名',
  password          VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt密文，60位，预留到100）',
  nickname          VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '昵称',
  avatar            VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像访问URL，空则前端显示昵称首字',
  email             VARCHAR(64)           DEFAULT NULL COMMENT '邮箱（可选）',
  phone             VARCHAR(20)           DEFAULT NULL COMMENT '手机号（可选）',
  gender            TINYINT      NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  role              VARCHAR(16)  NOT NULL DEFAULT 'student' COMMENT '角色：student学生 teacher普通管理员(教师) admin超级管理员',
  major             VARCHAR(64)           DEFAULT NULL COMMENT '专业（学生填写，大屏按专业统计活跃度）',
  class_name        VARCHAR(64)           DEFAULT NULL COMMENT '班级',
  learn_direction   VARCHAR(16)           DEFAULT NULL COMMENT '学习方向：frontend前端 backend后端 test测试',
  bio               VARCHAR(255)          DEFAULT NULL COMMENT '个人简介/学习宣言',
  security_question VARCHAR(128)          DEFAULT NULL COMMENT '密保问题（找回密码用）',
  security_answer   VARCHAR(100)          DEFAULT NULL COMMENT '密保答案（BCrypt密文，答案不明文存储）',
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1正常 0冻结（冻结后禁止登录）',
  last_login_time   DATETIME              DEFAULT NULL COMMENT '最近登录时间（活跃用户统计口径）',
  create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_role (role),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ---------------------------------------------------------------------------
-- 2. 错题表（本系统最核心表）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mistake (
  id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '错题ID',
  user_id          BIGINT        NOT NULL COMMENT '所属学生ID（逻辑外键→user.id）',
  title            VARCHAR(128)  NOT NULL COMMENT '错题标题（关键词检索字段）',
  tech_direction   VARCHAR(32)   NOT NULL COMMENT '技术方向：Vue/SpringBoot/MySQL/算法/其他',
  error_type       VARCHAR(16)   NOT NULL COMMENT '错误类型：grammar语法错误 logic逻辑错误 api接口报错 env环境问题',
  error_code       TEXT                   DEFAULT NULL COMMENT '错误代码片段',
  error_msg        TEXT                   DEFAULT NULL COMMENT '报错信息/异常堆栈（关键词检索字段）',
  solution         TEXT                   DEFAULT NULL COMMENT '正确解决方案（代码+思路）',
  images           VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '运行/报错截图URL的JSON数组（多图，≤9张）',
  is_favorite      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否收藏：0否 1是',
  is_top           TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是（列表置顶排前）',
  audit_status     TINYINT       NOT NULL DEFAULT 1 COMMENT '审核状态：0待审核 1正常 2已下架',
  audit_remark     VARCHAR(255)           DEFAULT NULL COMMENT '审核备注/下架原因',
  review_count     INT           NOT NULL DEFAULT 0 COMMENT '累计被复习次数（复盘打勾时+1）',
  last_review_time DATETIME               DEFAULT NULL COMMENT '最近复习时间（复盘推荐排序用）',
  create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（日期筛选/周增长统计用）',
  update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted          TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除（即归档回收站，可恢复）',
  PRIMARY KEY (id),
  KEY idx_user_create (user_id, create_time),
  KEY idx_tech_direction (tech_direction),
  KEY idx_error_type (error_type),
  KEY idx_create_time (create_time),
  KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题表';

-- ---------------------------------------------------------------------------
-- 3. 错题标签表（学生个人私有标签）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mistake_tag (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  user_id     BIGINT      NOT NULL COMMENT '所属学生ID（逻辑外键→user.id）',
  name        VARCHAR(32) NOT NULL COMMENT '标签名（同一用户下不重名），如：空指针、数组越界',
  color       VARCHAR(16) NOT NULL DEFAULT '' COMMENT '标签颜色（留空则前端按 id 循环取色）',
  sort        INT         NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题标签表';

-- ---------------------------------------------------------------------------
-- 4. 错题-标签关联表（多对多；关联记录无业务字段，跟随主数据物理清理）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mistake_tag_rel (
  id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  mistake_id  BIGINT   NOT NULL COMMENT '错题ID（逻辑外键→mistake.id）',
  tag_id      BIGINT   NOT NULL COMMENT '标签ID（逻辑外键→mistake_tag.id）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打标时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mistake_tag (mistake_id, tag_id),
  KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题-标签关联表';

-- ---------------------------------------------------------------------------
-- 5. 笔记表
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS note (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  user_id      BIGINT       NOT NULL COMMENT '作者ID（逻辑外键→user.id）',
  category_id  BIGINT       NOT NULL DEFAULT 0 COMMENT '分类ID（逻辑外键→note_category.id）',
  title        VARCHAR(128) NOT NULL COMMENT '笔记标题（搜索字段）',
  content      MEDIUMTEXT             DEFAULT NULL COMMENT '富文本HTML正文（wangEditor输出，代码块 pre>code 保存）',
  summary      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '列表摘要（后端从纯文本截取，列表页不查content大字段）',
  is_public    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否公开：0私密 1公开',
  audit_status TINYINT      NOT NULL DEFAULT 1 COMMENT '审核状态：0待审核 1正常 2已下架（私密恒为1；切公开时置0进入审核）',
  audit_remark VARCHAR(255)           DEFAULT NULL COMMENT '审核备注/下架原因',
  view_count   INT          NOT NULL DEFAULT 0 COMMENT '浏览数（公开笔记被查看时+1）',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  KEY idx_user_create (user_id, create_time),
  KEY idx_category_id (category_id),
  KEY idx_public_audit (is_public, audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记表';

-- ---------------------------------------------------------------------------
-- 6. 笔记分类表（user_id=0 为系统内置分类，全员可见；用户可自建分类）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS note_category (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  user_id     BIGINT      NOT NULL DEFAULT 0 COMMENT '所属用户ID：0=系统默认分类（全员共用）',
  name        VARCHAR(32) NOT NULL COMMENT '分类名，如：Vue学习笔记、数据库笔记',
  sort        INT         NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  is_system   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否系统内置：1内置（不可删改） 0用户自建',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记分类表';

-- ---------------------------------------------------------------------------
-- 7. 复盘计划表（一人一计划；故意不设 deleted：uk_user_id 与逻辑删除冲突，
--    逻辑删的旧行仍占唯一键会导致重建计划失败，停用用 status=0 表达）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS review_plan (
  id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  user_id        BIGINT      NOT NULL COMMENT '所属学生ID（一人一套计划，逻辑外键→user.id）',
  plan_name      VARCHAR(64) NOT NULL DEFAULT '每日复盘计划' COMMENT '计划名称',
  daily_count    INT         NOT NULL DEFAULT 5 COMMENT '每天复习错题数（自定义复盘任务核心设置）',
  remind_enabled TINYINT     NOT NULL DEFAULT 1 COMMENT '未完成提醒：1开启（登录弹窗/菜单角标） 0关闭',
  status         TINYINT     NOT NULL DEFAULT 1 COMMENT '计划状态：1启用 0停用',
  create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='复盘计划表';

-- ---------------------------------------------------------------------------
-- 8. 复盘任务完成记录表（每人每天一行，驱动自动打勾/复盘日历/完成率）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS review_task (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  user_id        BIGINT        NOT NULL COMMENT '所属学生ID（逻辑外键→user.id）',
  task_date      DATE          NOT NULL COMMENT '任务日期（每人每天至多一条）',
  target_count   INT           NOT NULL DEFAULT 0 COMMENT '当日目标复习题数（生成时取计划快照，改计划不影响历史）',
  completed_count INT          NOT NULL DEFAULT 0 COMMENT '当日已完成复习题数',
  reviewed_ids   VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '当日已复习错题ID的JSON数组（防重复计数+复习明细）',
  status         TINYINT       NOT NULL DEFAULT 0 COMMENT '完成状态：0未完成 1已完成（达标自动打勾置1）',
  finish_time    DATETIME                DEFAULT NULL COMMENT '完成时间（自动打勾时刻）',
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_date (user_id, task_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='复盘任务完成记录表';

-- ---------------------------------------------------------------------------
-- 9. 打卡签到表（流水表，只增不改；一天只能打一次）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS check_in (
  id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
  user_id     BIGINT   NOT NULL COMMENT '学生ID（逻辑外键→user.id）',
  check_date  DATE     NOT NULL COMMENT '打卡日期',
  check_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '实际打卡时刻',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_date (user_id, check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='打卡签到表';

-- ---------------------------------------------------------------------------
-- 10. 学习时长记录表（流水表，驱动每日学习时长折线图）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS study_time_log (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  user_id     BIGINT      NOT NULL COMMENT '学生ID（逻辑外键→user.id）',
  study_date  DATE        NOT NULL COMMENT '学习日期',
  duration    INT         NOT NULL DEFAULT 0 COMMENT '本次学习时长（分钟）',
  source      VARCHAR(16) NOT NULL DEFAULT 'manual' COMMENT '时长来源：review复盘 note写笔记 practice刷题 manual手动补录',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  PRIMARY KEY (id),
  KEY idx_user_date (user_id, study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学习时长记录表';

-- ---------------------------------------------------------------------------
-- 11. 问答问题表（社区互助问答）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS question (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  user_id        BIGINT       NOT NULL COMMENT '提问人ID（逻辑外键→user.id，"只看自己提问"用）',
  title          VARCHAR(128) NOT NULL COMMENT '问题标题',
  content        TEXT                   DEFAULT NULL COMMENT '问题详情（支持贴代码）',
  tech_direction VARCHAR(32)  NOT NULL DEFAULT '其他' COMMENT '技术方向（字典同错题表）',
  status         TINYINT      NOT NULL DEFAULT 0 COMMENT '解决状态：0未解决 1已解决（提问人标记）',
  view_count     INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  answer_count   INT          NOT NULL DEFAULT 0 COMMENT '回复数冗余（评论增删时维护，列表免COUNT）',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除（违规问答下架）',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_tech_direction (tech_direction),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='问答问题表';

-- ---------------------------------------------------------------------------
-- 12. 问答评论表（二级楼中楼：root_id + parent_id，无递归查询）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  question_id   BIGINT        NOT NULL COMMENT '所属问题ID（逻辑外键→question.id）',
  user_id       BIGINT        NOT NULL COMMENT '评论人ID（逻辑外键→user.id）',
  parent_id     BIGINT        NOT NULL DEFAULT 0 COMMENT '父评论ID：0=一级评论（直接回答），否则=被回复的评论ID',
  root_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '根评论ID：0=自身是一级评论；子回复统一挂到一级评论下（保证只有两级）',
  reply_user_id BIGINT                   DEFAULT NULL COMMENT '被回复人ID（前端显示"回复 @昵称"）',
  content       VARCHAR(1000) NOT NULL COMMENT '评论内容',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  KEY idx_q_root (question_id, root_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='问答评论表';

-- ---------------------------------------------------------------------------
-- 13. 公告表
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS announcement (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title          VARCHAR(128) NOT NULL COMMENT '公告标题',
  content        TEXT                   DEFAULT NULL COMMENT '公告正文',
  type           VARCHAR(16)  NOT NULL DEFAULT 'notice' COMMENT '公告类型：notice编程学习通知 maintenance系统维护通知',
  publisher_id   BIGINT       NOT NULL COMMENT '发布人ID（管理员，逻辑外键→user.id）',
  publisher_name VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '发布人昵称冗余（改名/删号后公告仍可读）',
  status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0草稿 1已发布 2已下线',
  publish_time   DATETIME               DEFAULT NULL COMMENT '发布时间（列表按此倒序）',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  KEY idx_status_publish (status, publish_time),
  KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- ---------------------------------------------------------------------------
-- 14. 公告已读关系表（公告-用户 多对多；登录后未读公告弹窗取数）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS announcement_read (
  id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  announcement_id BIGINT   NOT NULL COMMENT '公告ID（逻辑外键→announcement.id）',
  user_id         BIGINT   NOT NULL COMMENT '用户ID（逻辑外键→user.id）',
  read_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ann_user (announcement_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告已读关系表';

-- ---------------------------------------------------------------------------
-- 15. 敏感词表（发布错题/笔记/问答/评论时内存匹配自动拦截）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sensitive_word (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  word        VARCHAR(64) NOT NULL COMMENT '敏感词内容',
  level       TINYINT     NOT NULL DEFAULT 1 COMMENT '处理级别：1直接拦截 2标记人工复核（预留扩展）',
  status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词表';

-- ---------------------------------------------------------------------------
-- 16. 操作日志表（AOP 自动记录管理员操作，仅超管可查；日志不删，无 deleted）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS operation_log (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  user_id        BIGINT                  DEFAULT NULL COMMENT '操作人ID（逻辑外键→user.id）',
  username       VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '操作人用户名冗余（改名/删号后日志仍可读）',
  role           VARCHAR(16)   NOT NULL DEFAULT '' COMMENT '操作人角色',
  module         VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '所属模块：用户管理/内容审核/公告管理/敏感词管理/权限管理等',
  operation      VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '操作描述，如：冻结用户、下架笔记',
  request_method VARCHAR(8)    NOT NULL DEFAULT '' COMMENT 'HTTP方法：GET/POST/PUT/DELETE',
  request_url    VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '请求路径',
  method         VARCHAR(200)  NOT NULL DEFAULT '' COMMENT 'Java方法签名（类名.方法名）',
  params         TEXT                    DEFAULT NULL COMMENT '请求参数JSON（密码等敏感字段已脱敏）',
  ip             VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '操作人IP',
  result         TINYINT       NOT NULL DEFAULT 1 COMMENT '执行结果：1成功 0失败',
  error_msg      VARCHAR(2000)           DEFAULT NULL COMMENT '失败原因/异常摘要',
  cost_time      BIGINT        NOT NULL DEFAULT 0 COMMENT '接口耗时（毫秒）',
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（日志按时间倒序分页）',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ============================================================================
-- 建表完成。共 16 张表。
-- 下一步：启动后端（ReviewServerApplication），DataInitializer 会自动播种：
--   4 个演示账号：admin/admin123(超管) teacher01/123456(教师)
--                 student01/123456 student02/123456(学生)
--   7 个系统笔记分类、8 个默认敏感词、2 条示例公告
--   student01 的演示业务数据（错题/笔记/打卡/学习时长，日期相对当天生成，
--   保证"近7天/近30天"统计图表在任意演示日都有数据）
-- ============================================================================
