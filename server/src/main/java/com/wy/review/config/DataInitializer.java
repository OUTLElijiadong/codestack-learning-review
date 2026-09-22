package com.wy.review.config;

import com.wy.review.entity.NoteCategory;
import com.wy.review.entity.SensitiveWord;
import com.wy.review.entity.User;
import com.wy.review.mapper.NoteCategoryMapper;
import com.wy.review.mapper.SensitiveWordMapper;
import com.wy.review.mapper.UserMapper;
import com.wy.review.utils.PasswordUtil;
import com.wy.review.utils.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化器：应用启动时播种系统运行所必需的基础数据
 *
 * 只播种三类"系统配置级"数据，绝不播种任何演示/示例业务数据：
 *  1. 账号：超管 admin（没有它无法进入管理端）、教师 teacher01（权限分级对照）；
 *     学生账号一律由注册产生，不预置。
 *  2. 系统内置笔记分类（user_id=0，全员共用的基础分类）。
 *  3. 默认敏感词库（内容审核的初始拦截词表）。
 *
 * 业务数据（错题/笔记/复盘/打卡/问答/公告）全部由真实用户操作产生，
 * 页面上出现的每一行数据都对应数据库里的真实记录。
 *
 * 前置条件：已执行 sql/schema.sql 建表；未建表时打印醒目提示并跳过。
 */
@Slf4j
@Order(2)
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final NoteCategoryMapper categoryMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    public DataInitializer(UserMapper userMapper, NoteCategoryMapper categoryMapper,
                           SensitiveWordMapper sensitiveWordMapper, SensitiveWordUtil sensitiveWordUtil) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.sensitiveWordMapper = sensitiveWordMapper;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    @Override
    public void run(String... args) {
        try {
            userMapper.selectCount(null);
        } catch (Exception e) {
            log.warn("============================================================");
            log.warn("  数据表不存在，已跳过基础数据播种！");
            log.warn("  请先在 MySQL 中执行 sql/schema.sql 建库建表，然后重启后端。");
            log.warn("============================================================");
            return;
        }
        seedAccounts();
        seedCategories();
        seedSensitiveWords();
        // 敏感词在播种后才入库，刷新内存词库保证首次启动即生效
        sensitiveWordUtil.refresh();
    }

    /** 系统必需账号：超管 + 一个教师账号（权限分级对照），密码 BCrypt 现场生成 */
    private void seedAccounts() {
        if (countByUsername("admin") == 0) {
            User admin = baseUser("admin", PasswordUtil.encode("admin123"), "超级管理员", "admin");
            userMapper.insert(admin);
            log.info("已创建超级管理员账号：admin / admin123");
        }
        if (countByUsername("teacher01") == 0) {
            User teacher = baseUser("teacher01", PasswordUtil.encode("123456"), "赵老师", "teacher");
            userMapper.insert(teacher);
            log.info("已创建教师(普通管理员)账号：teacher01 / 123456");
        }
    }

    /** 系统内置笔记分类（user_id=0，全员可见共用） */
    private void seedCategories() {
        List<String> defaults = Arrays.asList(
                "Vue学习笔记", "数据库笔记", "框架笔记", "Java基础", "算法笔记", "项目实战", "随笔心得");
        int sort = 1;
        for (String name : defaults) {
            Long count = categoryMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoteCategory>()
                    .eq(NoteCategory::getUserId, 0L).eq(NoteCategory::getName, name));
            if (count == null || count == 0) {
                NoteCategory cat = new NoteCategory();
                cat.setUserId(0L);
                cat.setName(name);
                cat.setSort(sort);
                cat.setIsSystem(1);
                categoryMapper.insert(cat);
            }
            sort++;
        }
    }

    /** 默认敏感词（贴合校园学习场景，管理端可增删） */
    private void seedSensitiveWords() {
        List<String> words = Arrays.asList("代考", "代写", "代做", "答案售卖", "考试作弊", "外挂", "刷单", "包过");
        for (String word : words) {
            Long count = sensitiveWordMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SensitiveWord>()
                    .eq(SensitiveWord::getWord, word));
            if (count == null || count == 0) {
                SensitiveWord sw = new SensitiveWord();
                sw.setWord(word);
                sw.setLevel(1);
                sw.setStatus(1);
                sensitiveWordMapper.insert(sw);
            }
        }
    }

    private User baseUser(String username, String encodedPwd, String nickname, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(encodedPwd);
        u.setNickname(nickname);
        u.setRole(role);
        u.setStatus(1);
        u.setGender(0);
        return u;
    }

    private long countByUsername(String username) {
        Long count = userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        return count == null ? 0 : count;
    }
}
