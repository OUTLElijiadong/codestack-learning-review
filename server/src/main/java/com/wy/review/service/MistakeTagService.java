package com.wy.review.service;

import com.wy.review.entity.MistakeTag;

import java.util.List;
import java.util.Map;

/**
 * 错题标签服务（学生个人标签的增删改查，含每个标签下的错题数）
 */
public interface MistakeTagService {

    /** 我的标签列表（含每个标签关联的错题数） */
    List<Map<String, Object>> listMyTags();

    /** 新增标签（同名去重） */
    MistakeTag add(String name, String color);

    /** 重命名标签 */
    void rename(Long id, String name, String color);

    /** 删除标签（连带清理关联记录） */
    void delete(Long id);
}
