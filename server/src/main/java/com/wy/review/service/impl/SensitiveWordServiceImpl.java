package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.entity.SensitiveWord;
import com.wy.review.mapper.SensitiveWordMapper;
import com.wy.review.service.SensitiveWordService;
import com.wy.review.utils.SensitiveWordUtil;
import org.springframework.stereotype.Service;

/**
 * 敏感词库管理服务实现：增删后同步刷新内存词库
 */
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    public SensitiveWordServiceImpl(SensitiveWordMapper sensitiveWordMapper,
                                    SensitiveWordUtil sensitiveWordUtil) {
        this.sensitiveWordMapper = sensitiveWordMapper;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    @Override
    public PageResult<SensitiveWord> page(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SensitiveWord::getWord, keyword.trim());
        }
        wrapper.orderByDesc(SensitiveWord::getId);
        return PageResult.of(sensitiveWordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public void add(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "敏感词不能为空");
        }
        Long dup = sensitiveWordMapper.selectCount(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, word.trim()));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "该敏感词已存在");
        }
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(word.trim());
        entity.setLevel(1);
        entity.setStatus(1);
        sensitiveWordMapper.insert(entity);
        sensitiveWordUtil.refresh(); // 热刷新内存词库，立即生效
    }

    @Override
    public void delete(Long id) {
        sensitiveWordMapper.deleteById(id); // 词表无 deleted 字段，物理删除
        sensitiveWordUtil.refresh();
    }
}
