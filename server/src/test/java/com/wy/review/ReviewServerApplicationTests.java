package com.wy.review;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 上下文加载冒烟测试
 * 注意：运行测试需要本机 MySQL 已建 review_db 库（执行过 sql/schema.sql）
 */
@SpringBootTest
class ReviewServerApplicationTests {

    @Test
    void contextLoads() {
        // Spring 上下文能完整装配即通过
    }
}
