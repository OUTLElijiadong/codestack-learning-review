package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 复盘计划入参：自定义每日复盘任务 */
@Data
public class ReviewPlanDTO {

    private String planName;

    /** 每天复习错题数 */
    @NotNull(message = "请设置每天复习的错题数")
    @Min(value = 1, message = "每天至少复习 1 道")
    @Max(value = 50, message = "每天最多复习 50 道")
    private Integer dailyCount;

    /** 未完成提醒：1开启 0关闭 */
    private Integer remindEnabled;

    /** 计划状态：1启用 0停用 */
    private Integer status;
}
