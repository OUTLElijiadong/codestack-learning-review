package com.wy.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/** 错题新增/编辑入参 */
@Data
public class MistakeDTO {

    @NotBlank(message = "请输入错题标题")
    @Size(max = 128, message = "标题最长 128 个字符")
    private String title;

    @NotBlank(message = "请选择技术方向")
    private String techDirection;

    /** 错误类型：grammar语法错误 logic逻辑错误 api接口报错 env环境问题 */
    @NotBlank(message = "请选择错误类型")
    private String errorType;

    /** 错误代码片段 */
    private String errorCode;

    /** 报错信息/异常堆栈 */
    private String errorMsg;

    /** 正确解决方案 */
    private String solution;

    /** 截图URL数组（先经 /common/upload 上传） */
    private List<String> images;

    /** 标签ID数组（→mistake_tag_rel 关联） */
    private List<Long> tagIds;
}
