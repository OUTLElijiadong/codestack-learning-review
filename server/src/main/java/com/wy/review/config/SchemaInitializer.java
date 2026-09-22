package com.wy.review.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * 首次启动自动建表：数据库为空（user 表不存在）时执行 classpath:schema-init.sql。
 *
 * 三重保护，永不动已有数据：
 *   1. 只在 user 表不存在时执行（老库直接跳过）；
 *   2. 脚本内全部是 CREATE TABLE IF NOT EXISTS（幂等）；
 *   3. 库本身由 JDBC 参数 createDatabaseIfNotExist=true 自动创建。
 *
 * 因此 Windows 端无需再用命令行导入 sql/schema.sql —— 装好 MySQL、
 * 在 application.yml 填对密码、启动后端即完成建库建表。
 * 运行顺序在 DataInitializer（@Order 默认最低）之前，保证播种时表已存在。
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class SchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name = 'user'",
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        log.info("首次启动：检测到数据库为空，自动建表中（16 张表，幂等脚本）...");
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            ScriptUtils.executeSqlScript(conn,
                    new EncodedResource(new ClassPathResource("schema-init.sql"), "UTF-8"));
        }
        log.info("自动建表完成，内置账号将由 DataInitializer 播种。");
    }
}
