package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 复盘计划表（一人一计划，uk_user_id 唯一）
 * 故意不设 deleted：唯一索引与逻辑删除冲突，停用用 status=0 表达
 */
@Data
@TableName("review_plan")
public class ReviewPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生ID（一人一套计划） */
    private Long userId;

    private String planName;

    /** 每天复习错题数（自定义每日复盘任务的核心设置） */
    private Integer dailyCount;

    /** 未完成提醒：1开启（登录弹窗/菜单角标） 0关闭 */
    private Integer remindEnabled;

    /** 计划状态：1启用 0停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
