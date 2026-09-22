package com.wy.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题表（系统最核心表）
 * deleted=1 即"删除进归档"（回收站语义，可恢复）
 */
@Data
@TableName("mistake")
public class Mistake {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生ID */
    private Long userId;

    /** 错题标题（关键词检索字段） */
    private String title;

    /** 技术方向：Vue/SpringBoot/MySQL/算法/其他 */
    private String techDirection;

    /** 错误类型：grammar语法错误 logic逻辑错误 api接口报错 env环境问题 */
    private String errorType;

    /** 错误代码片段 */
    private String errorCode;

    /** 报错信息/异常堆栈（关键词检索字段） */
    private String errorMsg;

    /** 正确解决方案（代码+思路） */
    private String solution;

    /** 截图URL的JSON数组串，如 ["/api/uploads/2026/09/a.png"] */
    private String images;

    /** 是否收藏：0否 1是 */
    private Integer isFavorite;

    /** 是否置顶：0否 1是 */
    private Integer isTop;

    /** 审核状态：0待审核 1正常 2已下架 */
    private Integer auditStatus;

    /** 审核备注/下架原因 */
    private String auditRemark;

    /** 累计被复习次数 */
    private Integer reviewCount;

    /** 最近复习时间（复盘推荐排序用） */
    private LocalDateTime lastReviewTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除：0正常 1已删除（归档回收站） */
    @TableLogic
    private Integer deleted;
}
