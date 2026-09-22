package com.wy.review.vo;

import com.wy.review.entity.MistakeTag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题详情/列表 VO：
 * images 与 tags 已解析为数组（数据库中 images 是 JSON 字符串，标签存关联表）
 */
@Data
public class MistakeVO {

    private Long id;
    private String title;
    private String techDirection;

    /** 错误类型：grammar语法错误 logic逻辑错误 api接口报错 env环境问题 */
    private String errorType;

    private String errorCode;
    private String errorMsg;
    private String solution;

    /** 截图URL数组（JSON 已解析） */
    private List<String> images;

    /** 标签对象数组（含名称与颜色） */
    private List<MistakeTag> tags;

    private Integer isFavorite;
    private Integer isTop;
    private Integer auditStatus;
    private String auditRemark;
    private Integer reviewCount;
    private LocalDateTime lastReviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
