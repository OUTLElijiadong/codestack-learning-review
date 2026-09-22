package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.dto.CommentDTO;
import com.wy.review.dto.QuestionDTO;
import com.wy.review.vo.CommentVO;
import com.wy.review.vo.QuestionVO;

/**
 * 社区互助问答服务
 */
public interface QuestionService {

    /** 发布问题（敏感词过滤） */
    void publish(QuestionDTO dto);

    /** 问题分页：关键词/技术方向/是否解决筛选；my=1 只看自己提问 */
    PageResult<QuestionVO> page(String keyword, String techDirection, Integer status,
                                Integer my, Integer pageNum, Integer pageSize);

    /** 问题详情：浏览数+1，返回问题 + 二级评论树 */
    QuestionVO detail(Long id);

    /** 删除自己的提问（连带评论逻辑删除） */
    void remove(Long id);

    /** 提问人标记已解决 */
    void markSolved(Long id);

    /** 评论回复解答（parentId 二级楼中楼；敏感词过滤；回复数+1） */
    void comment(CommentDTO dto);

    /** 删除自己的评论（回复数-1） */
    void removeComment(Long id);

    /** 只看自己回复：我的评论分页（含所属问题标题） */
    PageResult<CommentVO> myComments(Integer pageNum, Integer pageSize);
}
