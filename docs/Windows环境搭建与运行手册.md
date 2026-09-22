# Windows 环境搭建与运行手册

> 读者：学生本人。按本手册从零操作，即可在 Windows 10/11 + IntelliJ IDEA 上把系统跑起来。
> **只想最快跑起来？先看同目录《快速上手-三步运行.md》**（零命令行版：前端已打包进后端无需 Node.js，数据库启动时自动创建）。本手册是完整版，含环境变量配置与开发模式。
> 前置：拿到完整项目压缩包（含 server、web、sql、docs 四个目录）。

---

## 0. 全局约定

| 约定项 | 取值 | 说明 |
|---|---|---|
| 项目根目录 | `D:\code\code-review-system` | 压缩包解压后**必须把中文目录名改成这个**（中文路径会导致 Maven/npm 各种诡异报错） |
| 后端目录 | `D:\code\code-review-system\server` | IDEA 打开此目录 |
| 前端目录 | `D:\code\code-review-system\web` | IDEA Ultimate 或 VS Code 打开此目录 |
| 数据库 | MySQL 8.0，库名 `review_db` | 执行 `sql\schema.sql` 建库建表 |
| 后端端口 | 8080（接口前缀 /api） | http://localhost:8080/api |
| 前端端口 | 5173 | http://localhost:5173 |
| 内置账号 | admin/admin123（超管）、teacher01/123456（教师）、student01~05/123456（演示学生） | 首次启动自动创建，并播种校规模演示数据（42 名学生、错题/笔记/复盘打卡全套），随真实操作实时变化 |

---

## 1. 软件清单（先装好这 4 个）

| 软件 | 版本 | 下载 | 安装要点 |
|---|---|---|---|
| JDK | 8~25 均可（推荐 8；已实测 8、21、25 全跑通，电脑上已有哪个用哪个） | 腾讯镜像 https://mirrors.cloud.tencent.com/Adoptium/8/jdk/x64/windows/ 选最新 `OpenJDK8U-jdk_x64_windows_hotspot_8uXXX.msi` | 安装后配环境变量（见 1.1） |
| IntelliJ IDEA | Ultimate 2023.3+ | https://www.jetbrains.com/idea/download/ | **必须用 Ultimate**（社区版没有 Spring/JS 支持）；学生用学校邮箱免费申请教育授权：https://www.jetbrains.com/shop/eform/students |
| Node.js | 20 LTS | https://nodejs.org/zh-cn/download（选 Windows Installer .msi） | 一路 Next，自动配好 PATH |
| MySQL | 8.0.x Installer | https://dev.mysql.com/downloads/installer/ | 见第 2 节 |

> 全部装完后逐个验证（Win+R 输入 cmd 打开命令行）：
> `java -version` → 显示 1.8.0_xxx；`node -v` → v20.x.x；`npm -v` → 10.x；`mysql --version` → 8.0.x

### 1.1 JDK 环境变量

1. 右键「此电脑 → 属性 → 高级系统设置 → 环境变量」
2. 新建系统变量：`JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.XXX-hotspot`（按实际安装路径）
3. 编辑系统变量 `Path`，新增一行：`%JAVA_HOME%\bin`，并将其**上移到 Oracle javapath 之上**
4. 新开 cmd 执行 `java -version` 与 `javac -version` 均为 1.8 即成功

---

## 2. MySQL 8 安装与建库

1. 运行 mysql-installer-community，选 **Developer Default** 或至少勾选 MySQL Server
2. 认证方式选 **Use Strong Password Encryption**（默认即可，骨架已适配）
3. 设置 root 密码（例如 `123456`，记住它，后面要填进 application.yml）
4. 服务名保持 `MySQL80`，勾选开机自启
5. 建库（两种方式任选）：
   - **方式 A（推荐）**：打开 IDEA 右侧 Database 工具 → `+` → Data Source → MySQL → 填 root/密码 → Test Connection 变绿 → 新建 Query Console → 打开 `sql\schema.sql` 全选执行
   - **方式 B（命令行）**：cmd 执行
     ```bat
     cd /d D:\code\code-review-system
     mysql -u root -p < sql\schema.sql
     ```
6. 验证：执行 `mysql -u root -p` → `USE review_db; SHOW TABLES;` 应列出 user、mistake、note 等 **16 张表**

---

## 3. IDEA 导入并启动后端

1. **打开项目**：IDEA 欢迎页 → Open → 选择 `D:\code\code-review-system\server`（pom.xml 所在目录）→ Trust Project
2. **设置 JDK 8**：`File → Project Structure → Project`：SDK 选 `1.8`（没有就 Add JDK 指到安装目录）；`Modules` 里 Language level 选 `8`
3. **配置阿里云 Maven 镜像**（首次必须，否则下载极慢）：
   - `File → Settings → Build, Execution, Deployment → Build Tools → Maven`
   - `User settings file` 勾选 Override → 点右侧浏览，**直接选择项目包里现成的 `docs\maven-settings.xml`**（阿里云镜像 + 英文仓库路径都配好了，不用手写；也可以自己新建 `D:\dev\settings.xml`，内容如下）：
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
       <localRepository>D:\dev\maven-repository</localRepository>
       <mirrors>
         <mirror>
           <id>aliyun</id>
           <mirrorOf>central</mirrorOf>
           <name>阿里云公共仓库</name>
           <url>https://maven.aliyun.com/repository/public</url>
         </mirror>
       </mirrors>
     </settings>
     ```
   - `Local repository` 应回显 `D:\dev\maven-repository`（固定英文路径，绕开中文用户名问题）
   - 子节点 `Runner → JRE` 选 `1.8`
4. **Reload Maven**：右侧 Maven 面板 → 刷新图标 `Reload All Maven Projects`，首次下载 3~10 分钟
5. **开启 Lombok 注解处理**：`Settings → Plugins` 确认内置 Lombok 已启用；`Settings → Build → Compiler → Annotation Processors` 勾选 **Enable annotation processing**（不做这步会报"找不到符号"）
6. **改数据库账号密码**（全项目唯一需要改的配置）：打开 `server\src\main\resources\application.yml`，把 `spring.datasource` 的 `username` / `password` 改成本机 MySQL 的真实账号密码
7. **启动**：打开 `src\main\java\com\wy\review\ReviewServerApplication.java`，点类名左侧绿色三角 → Run
8. **验证**：控制台出现 `Started ReviewServerApplication in X seconds`，且首次启动日志里能看到 `已创建超级管理员账号：admin / admin123`、`已创建教师(普通管理员)账号：teacher01 / 123456`
   - 浏览器访问 http://localhost:8080/api/auth/captcha ，返回 `{"code":200,"msg":"操作成功","data":{"uuid":"...","img":"data:image/png;base64,..."}}` 即后端全链路通
   - 系统只预置管理端账号与基础配置（内置笔记分类、默认敏感词）；**不预置任何学生账号和业务数据**，学生端数据全部由注册后真实使用产生

---

## 4. 前端启动

1. IDEA Ultimate 中 `File → Open` 打开 `D:\code\code-review-system\web`（或用 VS Code 打开，装 **Vue - Official** 插件）
2. 打开内置终端（IDEA 快捷键 Alt+F12），依次执行：
   ```bat
   npm config set registry https://registry.npmmirror.com
   npm install
   npm run dev
   ```
3. 终端出现 `Local: http://localhost:5173/` 即成功
4. 浏览器访问 http://localhost:5173 → 登录页验证码图片正常显示（验证码本身就是经 Vite 代理从后端 8080 取回来的，显示出来就说明前后端已打通）

---

## 5. 启动顺序与联调验证清单

**每次开机/演示的固定顺序**：

```
① services.msc 确认 MySQL80 正在运行
② IDEA 运行 ReviewServerApplication（等 Started 字样）
③ 浏览器访问 http://localhost:8080/api/auth/captcha 冒烟（返回 code:200）
④ web 目录 npm run dev（等 VITE ready）
⑤ 浏览器访问 http://localhost:5173
```

**端到端联调清单（全勾才算就绪）**：

| # | 操作 | 预期 | 失败排查 |
|---|---|---|---|
| 0 | `java: java.lang.ExceptionInInitializerError com.sun.tools.javac.code.TypeTag :: UNKNOWN` | 编译 JDK 太新（非 8）与 Lombok 不兼容。Project Structure → SDK 选 1.8；Maven → Runner → JRE 选 1.8；新版包已升 Lombok 1.18.48 兜底 |
| 1 | services.msc 看 MySQL80 | 正在运行 | 右键启动；失败看第 6 节 #3 |
| 2 | `SHOW TABLES;`(review_db) | 16 张表 | 重新执行 schema.sql |
| 3 | IDEA 启动后端 | Started，无红色堆栈 | 看第 6 节按关键字对照 |
| 4 | 访问 /api/auth/captcha | code:200 JSON | 8080 占用/数据库连接失败 |
| 5 | npm run dev | Local: 5173 | 删 node_modules 重装 |
| 6 | 登录页验证码显示 | 点击可刷新 | 后端没起/代理被改 |
| 7 | 登录页点「立即注册」注册学生账号并登录 | 进入学生端首页（初始数据为 0 是正常的） | F12 看 Network 红色接口 |
| 8 | 真实录入 2~3 条错题、写 1 篇笔记、完成打卡 | 首页图表出现真实数据 | 图表空白看 IDEA 控制台 SQL |
| 9 | 新增错题时上传一张截图 | 列表出现，图可点开大图 | server\uploads 目录是否生成 |
| 10 | admin/admin123 登录进管理端 | 大屏有数据、菜单含管理员管理 | 角色是否 admin |

---

## 6. 常见坑与解决表

| # | 现象/报错原文 | 解决 |
|---|---|---|
| 1 | `Web server failed to start. Port 8080 was already in use` | cmd 执行 `netstat -ano \| findstr :8080` 找 PID → `taskkill /PID 该PID /F` |
| 2 | 5173 被占用自动跳到 5174 | `netstat -ano \| findstr :5173` 杀掉旧进程；演示前统一用 5173 |
| 3 | MySQL80 启动失败/第二遍装连不上 | 3306 被旧 MySQL/MariaDB 占用：services.msc 停掉旧服务 |
| 4 | `The server time zone value 'ÖÐ¹ú±ê×¼Ê±¼ä'...`（中文 Windows 必现） | 骨架 url 已带 `serverTimezone=Asia/Shanghai`，被改没就加回；根治：`C:\ProgramData\MySQL\MySQL Server 8.0\my.ini` 的 `[mysqld]` 下加 `default-time-zone='+08:00'` 重启 MySQL80 |
| 5 | `Public Key Retrieval is not allowed` | url 末尾补 `&allowPublicKeyRetrieval=true`（骨架已带） |
| 6 | 项目/Maven 仓库/npm 缓存路径含中文导致诡异报错 | 项目放 `D:\code\code-review-system`；settings.xml 的 localRepository 固定 `D:\dev\maven-repository`；`npm config set cache "D:\dev\npm-cache"` |
| 7 | F12 报 CORS 跨域错误 | 检查请求是否走了 /api 前缀；vite.config.js 代理配置是否被改（改了必须重启 npm run dev） |
| 8 | npm install 超慢/ETIMEDOUT | `npm config set registry https://registry.npmmirror.com` 后删 node_modules 重装 |
| 9 | npm install 报 ERESOLVE | `npm install --legacy-peer-deps` |
| 10 | PowerShell 禁止运行 npm.ps1 | 用 cmd；或管理员 PowerShell 执行 `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` |
| 11 | 实体类 @Data 灰显、getter/setter 报红 | 开启 Annotation Processors + 确认 Lombok 插件启用 + Maven Reload |
| 12 | `Unsupported class file major version 65` 等 | 用了高版本 JDK：Project Structure 三处（Project SDK / Modules / Maven Runner JRE）全部锁回 1.8 |
| 13 | 登录成功但马上跳回登录页 | F12 → Application → Local Storage 清空后重登（旧 token 与新密钥不匹配） |
| 14 | emoji/生僻字入库报 `Incorrect string value` | 库表须 utf8mb4：`ALTER DATABASE review_db CHARACTER SET utf8mb4;` 并对该表 `ALTER TABLE 表名 CONVERT TO CHARACTER SET utf8mb4;` |
| 15 | 启动日志提示"数据表不存在，已跳过基础数据播种" | 先执行 schema.sql 再重启后端；重启后自动补种内置账号/分类/敏感词 |

---

## 7. 从 macOS 拷贝到 Windows 的注意事项

1. 拷贝前删除 `web\node_modules`、`web\dist`、`server\target`（平台二进制不通用且极慢），到 Windows 重新 `npm install` / Maven Reload
2. 中文父目录改名为 `code-review-system`，放到 `D:\code\` 下
3. 建议压缩成 zip 再拷（单个大文件比几万小文件快得多）
4. 数据库不随文件走：到 Windows 后必须重新执行 `sql\schema.sql`
5. 行尾符无需处理：Java/Vue 构建工具对 LF/CRLF 都兼容

---

## 8. 答辩演示 Checklist

### 演示前一天

- [ ] 按第 5 节 10 项清单完整跑通一遍；用真实账号录入一批真实错题/笔记（演示时图表才丰满）
- [ ] 如需重置数据：删库重新执行 schema.sql 再重启后端即可回到纯净状态
- [ ] 暂停 Windows 更新；退出杀毒/网盘托盘；电源设为"从不睡眠"
- [ ] 录一遍 8 分钟完整演示视频放桌面（现场崩了直接放视频）
- [ ] U 盘备份：源码 zip + schema.sql + 四个离线安装包
- [ ] 浏览器缩放 100%、隐藏收藏夹栏、提前登录一次

### 演示当天（提前 30 分钟）

1. [ ] 插电源 → services.msc 确认 MySQL80 运行
2. [ ] IDEA 启动后端 → 访问 /api/auth/captcha 冒烟（顺便预热 JVM）
3. [ ] web 目录 npm run dev
4. [ ] 浏览器开两个标签：A 登自己注册的学生账号（学生端），B 登 admin（管理端），演示只切标签
5. [ ] F11 全屏

### 演示路线（8~10 分钟）

**开场 30 秒**：三层架构——Vue3 前端 5173、Spring Boot 后端 8080 统一 /api 前缀、MySQL 8；JWT + 拦截器接口级权限；密码 BCrypt。

**学生端（约 5 分钟）**：登录页图形验证码（可现场注册一个账号演示注册流程）→ 学习总览（四卡+三图）→ 错题本现场新增一条（代码块/截图/标签）→ 智能检索（方向+类型+最近一周+关键词高亮）→ 收藏/置顶/删除进归档再恢复 → 笔记（富文本代码块 + 公开切换 + 发一条含"代考"的内容演示敏感词拦截）→ 复盘计划（打卡 + 完成复习自动打勾）→ 复盘日历（哪天学了哪天没学）→ 互助问答（提问 + 另一账号/teacher01 回复，展示二级楼中楼）

**管理端（约 3 分钟）**：数据大屏（全校总量/专业活跃/趋势）→ 用户管理冻结一个学生账号 → 切学生标签页重新登录显示"账号已被冻结" → 解封 → 内容审核下架一条笔记 → 公告管理发布公告 → 学生端重新登录收到弹窗 → 用 teacher01 登录演示权限分级（看不到管理员管理/操作日志菜单）→ admin 打开操作日志，刚才的冻结/下架/发公告全部在案

**收尾 30 秒**：亮点四条——JWT 无状态鉴权与三层权限防护、逻辑删除归档回收站、AOP 操作日志 + 敏感词内存拦截、ECharts 个人端 + 管理端双层可视化。

### 论文截图清单（对应"系统实现"章节插图）

登录页、注册页、学习总览、错题列表（卡片式）、错题新增编辑、错题归档箱、笔记编辑器、复盘日历、智能检索（关键词高亮）、社区问答详情、用户管理、内容审核、数据大屏、公告管理+学生端弹窗、操作日志。用 `Win + Shift + S` 框选保存。
