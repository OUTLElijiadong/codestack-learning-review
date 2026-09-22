# 码栈 CodeStack —— 基于Vue的个人代码学习错题集与编程笔记复盘系统

> 某高校 软件工程专业 毕业设计（同学）
> 前后端分离：Vue 3 + Vite + Element Plus + ECharts ｜ Spring Boot 2.7 + MyBatis-Plus + MySQL 8 + JWT

---

## 项目结构

```
.
├── README.md                     ← 本文件
├── docs/
│   ├── 项目开发计划.md             ← 完整开发计划（选型/结构/数据库/进度/测试）
│   ├── Windows环境搭建与运行手册.md ← 同学 Windows + IDEA 逐步操作手册（先读这个）
│   └── 接口清单.md                 ← 全部 REST API 速查表
├── sql/
│   └── schema.sql                  ← 建库 + 16 张表（含全部列注释与索引）
├── server/                         ← Spring Boot 后端（IDEA 打开此目录）
│   ├── pom.xml
│   └── src/main/java/com/wy/review/...
└── web/                            ← Vue 3 前端（IDEA Ultimate 或 VS Code 打开此目录）
    ├── package.json  vite.config.js  index.html
    └── src/...
```

## 30 秒速览

| 项 | 值 |
|---|---|
| 后端 | `server/`，端口 8080，接口前缀 `/api`，启动类 `ReviewServerApplication` |
| 前端页面 | **已打包进后端**（`server/src/main/resources/static/`），后端启动后直接访问 `http://localhost:8080/api`，无需 Node.js |
| 前端源码 | `web/`（开发模式用：`npm run dev` 起在 5173，改完 `npm run build` 后把 `dist/` 内容拷回 static） |
| 数据库 | MySQL 8，库名 `review_db`，**后端首次启动自动建库建表**（无需手工执行 SQL） |
| 演示数据 | **首次启动自动播种校规模仿真数据**（42 名学生 / 380+ 错题 / 160+ 笔记 / 问答 / 30 天复盘打卡与学习时长），全部真实入库、随操作实时变化 |

## 内置账号（首次启动自动创建）

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `admin123` | 超级管理员 | 系统预置，不可删除 |
| `teacher01` | `123456` | 教师（普通管理员） | 权限分级对照账号 |

**演示学生账号**：`student01` ~ `student05`（密码统一 `123456`），另有 37 名仿真同学。首次启动且库中无学生时由 `DemoDataInitializer` 自动播种整套演示数据（固定随机种子，可复现）；已有学生的库绝不触碰。之后页面上每个数字、每条记录都对应数据库真实行——复盘、打卡、审核、冻结等任何操作都会实时反映到统计与大屏（数据闭环）。

## Windows 上跑起来（零命令行，详见 docs/快速上手-三步运行.md）

```bat
REM 1. 解压到 D:\code\ ，IDEA 打开 D:\code\code-review-system\server，等依赖下载完

REM 2. 把 src\main\resources\application.yml 里的 password 改成自己的 MySQL root 密码，
REM    右键运行 ReviewServerApplication —— 首次启动自动建库、建 16 张表、创建内置账号

REM 3. 浏览器打开 http://localhost:8080/api
```

> 只需安装 JDK、MySQL 8、IDEA 三个软件——**JDK 8/11/17/21/25 均可**（已实测 8、21、25 编译运行全通过），已有哪个用哪个；Node.js 仅在需要改前端源码时才要装（见《Windows环境搭建与运行手册》开发模式章节）。
> **不需要执行任何命令行**：数据库为空时由 `SchemaInitializer` + `schema-init.sql` 自动初始化（幂等，已有数据永不动）；`sql/schema.sql` 保留给习惯手工建库的情况，两种方式等效。

## 注意事项

- **唯一需要改的配置**：`server/src/main/resources/application.yml` 里的数据库账号密码。
- **项目不要放在含中文的路径下**（Windows 上把本目录改名为 `code-review-system`，放到 `D:\code\` 下）。
- 本压缩包已剔除 `web/node_modules`、`web/dist`、`server/target` 等平台相关产物；若手动拷贝工程文件夹到 Windows，请先删除这三项再拷。
- 学习方向/错误类型等字典取值前后端约定见 `web/src/constants/dict.js`，改动需前后端同步。

## 更新记录

- **2026-09-10**：全面 QA 巡查后修复 3 个问题——①删除自己的提问报 500（评论连带清理的 SQL 生成错误）；②接口异常不再向浏览器回传 SQL/堆栈细节，参数类型错误改回 400；③智能检索的关键词不再把 `%`、`_` 当通配符。交互优化：笔记/问答详情的代码块新增一键复制；仪表盘「今日目标」数字悬停可见快照规则说明。
- **2026-09-10（二）**：部署方式简化——前端构建产物打进后端 `server/src/main/resources/static/`，路由改 hash 模式，**后端一个进程即可跑完整系统**（访问 `http://localhost:8080/api`），Windows 端不再需要 Node.js 与前端终端；新增《快速上手-三步运行.md》。
- **2026-09-10（三）**：数据库初始化自动化——JDBC 参数 `createDatabaseIfNotExist` + 新增 `SchemaInitializer`（空库时执行幂等的 `schema-init.sql`），**Windows 端零命令行**：不再需要 `mysql < schema.sql`，装好 MySQL、填对密码、启动即自动建库建表播种；二次启动自动跳过、绝不动已有数据。
- **2026-09-10（四）**：Lombok 升级 1.18.30 → 1.18.48——修复在过新 JDK（22+）下编译报 `ExceptionInInitializerError / TypeTag :: UNKNOWN` 的问题；正式运行仍按手册统一使用 JDK 8。
- **2026-09-10（五）**：JDK 兼容性实测放宽——Docker 内以 JDK 21 与 JDK 25 完整验证编译、启动、自动建库、接口与页面全通过；**装了哪个 JDK 用哪个（8/11/17/21/25 均可）**，不再强制 JDK 8。首次启动时敏感词库的一条过时提示文案同步修正。
- **2026-09-10（六）**：按需求改为**数据库内种子演示数据**——移除前端演示模式，新增 `DemoDataInitializer`（空库时播种 42 名学生/384 道错题/164 篇笔记/15 条问答/497 天次复盘任务/196 人次打卡/近 14 天学习时长，交叉自洽且随操作实时变化）；大屏专业柱下钻改为真实接口 `/admin/screen/major-students`。修复初始化器排序（ApplicationRunner 整组先于 CommandLineRunner，三类必须同为 CommandLineRunner 才能按 @Order 排序）。
