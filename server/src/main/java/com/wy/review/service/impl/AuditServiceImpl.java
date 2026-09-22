package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.entity.Mistake;
import com.wy.review.entity.Note;
import com.wy.review.mapper.MistakeMapper;
import com.wy.review.mapper.NoteMapper;
import com.wy.review.service.AuditService;
import org.springframework.stereotype.Service;

/**
 * 内容审核服务实现：审核状态机 0待审核 → 1正常 / 2已下架
 */
@Service
public class AuditServiceImpl implements AuditService {

    private final MistakeMapper mistakeMapper;
    private final NoteMapper noteMapper;

    public AuditServiceImpl(MistakeMapper mistakeMapper, NoteMapper noteMapper) {
        this.mistakeMapper = mistakeMapper;
        this.noteMapper = noteMapper;
    }

    @Override
    public PageResult<Mistake> mistakePage(Integer auditStatus, String techDirection, String keyword,
                                           Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>()
                // 审核列表不查代码/方案大字段
                .select(Mistake.class, field -> !"error_code".equals(field.getColumn())
                        && !"solution".equals(field.getColumn()));
        if (auditStatus != null) {
            wrapper.eq(Mistake::getAuditStatus, auditStatus);
        }
        if (techDirection != null && !techDirection.isEmpty()) {
            wrapper.eq(Mistake::getTechDirection, techDirection);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Mistake::getTitle, keyword.trim());
        }
        // 待审核优先，其余按创建时间倒序
        wrapper.orderByAsc(Mistake::getAuditStatus).orderByDesc(Mistake::getCreateTime);
        return PageResult.of(mistakeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public void mistakePass(Long id) {
        Mistake exist = mistakeMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Mistake update = new Mistake();
        update.setId(id);
        update.setAuditStatus(1);
        update.setAuditRemark("");
        mistakeMapper.updateById(update);
    }

    @Override
    public void mistakeReject(Long id, String remark) {
        Mistake exist = mistakeMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Mistake update = new Mistake();
        update.setId(id);
        update.setAuditStatus(2); // 已下架：学生本人列表打标记，巡查列表不再按待审核展示
        update.setAuditRemark(remark == null ? "内容违规，已被管理员下架" : remark);
        mistakeMapper.updateById(update);
    }

    @Override
    public PageResult<Note> notePage(Integer auditStatus, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .select(Note.class, field -> !"content".equals(field.getColumn()));
        if (auditStatus != null) {
            wrapper.eq(Note::getAuditStatus, auditStatus);
        } else {
            // 默认只看公开内容（私密笔记是学生隐私，不进审核流）
            wrapper.eq(Note::getIsPublic, 1);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Note::getTitle, keyword.trim())
                    .or().like(Note::getSummary, keyword.trim()));
        }
        wrapper.orderByAsc(Note::getAuditStatus).orderByDesc(Note::getCreateTime);
        return PageResult.of(noteMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public void notePass(Long id) {
        Note exist = noteMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Note update = new Note();
        update.setId(id);
        update.setAuditStatus(1);
        update.setAuditRemark("");
        noteMapper.updateById(update);
    }

    @Override
    public void noteReject(Long id, String remark) {
        Note exist = noteMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        Note update = new Note();
        update.setId(id);
        update.setAuditStatus(2);
        update.setAuditRemark(remark == null ? "内容违规，已被管理员下架" : remark);
        noteMapper.updateById(update);
    }

    @Override
    public Note noteDetail(Long id) {
        Note exist = noteMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return exist;
    }

    @Override
    public Mistake mistakeDetail(Long id) {
        Mistake exist = mistakeMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return exist;
    }
}
