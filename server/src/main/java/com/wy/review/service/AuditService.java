package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.entity.Mistake;
import com.wy.review.entity.Note;

/**
 * 内容审核服务（管理端）：违规错题/违规笔记的人工审核与一键下架
 */
public interface AuditService {

    /** 错题审核分页（按审核状态/技术方向/关键词筛选） */
    PageResult<Mistake> mistakePage(Integer auditStatus, String techDirection, String keyword,
                                    Integer pageNum, Integer pageSize);

    /** 错题审核通过 */
    void mistakePass(Long id);

    /** 错题一键下架（可附下架原因） */
    void mistakeReject(Long id, String remark);

    /** 笔记审核分页 */
    PageResult<Note> notePage(Integer auditStatus, String keyword, Integer pageNum, Integer pageSize);

    /** 笔记审核通过（公开笔记进入广场可见） */
    void notePass(Long id);

    /** 笔记一键下架 */
    void noteReject(Long id, String remark);

    /** 审核查看笔记完整内容（管理端，不受公开/审核状态限制） */
    Note noteDetail(Long id);

    /** 审核查看错题完整内容（管理端） */
    Mistake mistakeDetail(Long id);
}
