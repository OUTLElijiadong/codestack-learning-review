package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 学习时长上报入参（学习时长折线图数据写入入口） */
@Data
public class StudyTimeDTO {

    /** 本次学习时长（分钟） */
    @NotNull(message = "请填写学习时长")
    @Min(value = 1, message = "时长至少 1 分钟")
    @Max(value = 1440, message = "单次时长不能超过 24 小时")
    private Integer duration;

    /** 时长来源：review复盘 note写笔记 practice刷题 manual手动补录 */
    private String source;
}
