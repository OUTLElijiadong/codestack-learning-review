package com.wy.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统启动类
 * 基于Vue的个人代码学习错题集与编程笔记复盘系统 - 后端
 *
 * @MapperScan     扫描 MyBatis-Plus Mapper 接口
 * @EnableScheduling 开启定时任务（笔记每日凌晨自动备份）
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.wy.review.mapper")
public class ReviewServerApplication {

    public static void main(String[] args) {
        System.out.println("\n============================================================\n  码栈 CodeStack v2.0 答辩整改版（2026-09-22）\n  登录页含身份下拉；错题详情含返回键与豆包AI；含收藏总览；大屏浅色\n============================================================\n");
        SpringApplication.run(ReviewServerApplication.class, args);
    }
}
