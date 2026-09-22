package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.entity.SensitiveWord;

/**
 * 敏感词库管理服务（增/删/分页；操作后热刷新内存词库）
 */
public interface SensitiveWordService {

    PageResult<SensitiveWord> page(String keyword, Integer pageNum, Integer pageSize);

    /** 新增敏感词（去重），成功后刷新内存词库 */
    void add(String word);

    /** 删除敏感词，成功后刷新内存词库 */
    void delete(Long id);
}
