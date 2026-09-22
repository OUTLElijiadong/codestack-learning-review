package com.wy.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.PageResult;
import com.wy.review.dto.MistakeDTO;
import com.wy.review.entity.Mistake;
import com.wy.review.vo.MistakeVO;

/**
 * 错题本服务（核心模块）
 */
public interface MistakeService {

    /** 新增错题（敏感词过滤 + 标签关联） */
    void add(MistakeDTO dto);

    /** 一键重新编辑（全量更新；曾被下架的错题编辑后重新进入待审核） */
    void update(Long id, MistakeDTO dto);

    /** 一键删除归档：逻辑删除 deleted=1，进回收站，可恢复 */
    void archive(Long id);

    /** 错题分页列表/智能检索（同一实现：关键词/技术方向/错误类型/标签/最近N天/只看收藏） */
    PageResult<MistakeVO> search(String keyword, String techDirection, String errorType,
                                 Long tagId, Integer recentDays, Integer onlyFavorite,
                                 Integer pageNum, Integer pageSize);

    /** 错题详情（仅本人；含标签与截图数组解析） */
    MistakeVO detail(Long id);

    /** 置顶/取消置顶（取反） */
    void toggleTop(Long id);

    /** 收藏/取消收藏（取反） */
    void toggleFavorite(Long id);

    /** 归档（回收站）分页：查 deleted=1 */
    PageResult<Mistake> archivePage(String keyword, Integer pageNum, Integer pageSize);

    /** 从归档还原 */
    void restore(Long id);

    /** 彻底删除（物理删除，连带清理标签关联） */
    void deleteForever(Long id);
}
