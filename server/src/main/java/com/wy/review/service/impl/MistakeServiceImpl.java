package com.wy.review.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.MistakeDTO;
import com.wy.review.entity.Mistake;
import com.wy.review.entity.MistakeTag;
import com.wy.review.entity.MistakeTagRel;
import com.wy.review.mapper.MistakeMapper;
import com.wy.review.mapper.MistakeTagMapper;
import com.wy.review.mapper.MistakeTagRelMapper;
import com.wy.review.service.MistakeService;
import com.wy.review.utils.SensitiveWordUtil;
import com.wy.review.vo.MistakeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题本服务实现
 * 要点：
 *  - 所有查询强制拼接 user_id = 当前登录人，防止横向越权
 *  - 新增/编辑统一在 Service 层过敏感词
 *  - 归档 = 逻辑删除（deleted=1），归档查询手写 SQL 绕过全局逻辑删除
 */
@Service
public class MistakeServiceImpl implements MistakeService {

    private final MistakeMapper mistakeMapper;
    private final MistakeTagMapper tagMapper;
    private final MistakeTagRelMapper relMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    public MistakeServiceImpl(MistakeMapper mistakeMapper, MistakeTagMapper tagMapper,
                              MistakeTagRelMapper relMapper, SensitiveWordUtil sensitiveWordUtil) {
        this.mistakeMapper = mistakeMapper;
        this.tagMapper = tagMapper;
        this.relMapper = relMapper;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MistakeDTO dto) {
        // 发布前敏感词拦截（标题+代码+报错+方案 全部参与校验）
        sensitiveWordUtil.assertClean(joinText(dto));

        Mistake entity = new Mistake();
        BeanUtils.copyProperties(dto, entity, "images", "tagIds");
        entity.setUserId(UserContext.getUserId());
        entity.setImages(dto.getImages() == null ? "[]" : JSONUtil.toJsonStr(dto.getImages()));
        entity.setAuditStatus(1); // 错题默认正常（私有内容，管理员巡查可下架）
        entity.setIsFavorite(0);
        entity.setIsTop(0);
        entity.setReviewCount(0);
        mistakeMapper.insert(entity);
        saveTagRels(entity.getId(), dto.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MistakeDTO dto) {
        Mistake exist = getOwn(id);
        sensitiveWordUtil.assertClean(joinText(dto));

        Mistake entity = new Mistake();
        BeanUtils.copyProperties(dto, entity, "images", "tagIds");
        entity.setId(exist.getId());
        entity.setImages(dto.getImages() == null ? "[]" : JSONUtil.toJsonStr(dto.getImages()));
        // 被下架的错题重新编辑后回到待审核，等待管理员复核
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 2) {
            entity.setAuditStatus(0);
        }
        mistakeMapper.updateById(entity);

        // 标签关联全量重建（先删后插，关联表物理删除即可）
        relMapper.delete(new LambdaQueryWrapper<MistakeTagRel>().eq(MistakeTagRel::getMistakeId, id));
        saveTagRels(id, dto.getTagIds());
    }

    @Override
    public void archive(Long id) {
        getOwn(id);
        mistakeMapper.deleteById(id); // 全局逻辑删除：实际执行 UPDATE deleted=1
    }

    @Override
    public PageResult<MistakeVO> search(String keyword, String techDirection, String errorType,
                                        Long tagId, Integer recentDays, Integer onlyFavorite,
                                        Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId);

        // 关键词全文搜索：标题 + 错误代码 + 报错信息 三字段 LIKE 模糊匹配
        // % 和 _ 是 LIKE 通配符，先转义成字面量，避免搜 "%" 拉出全量
        if (hasText(keyword)) {
            String kw = escapeLike(keyword);
            wrapper.and(w -> w.like(Mistake::getTitle, kw)
                    .or().like(Mistake::getErrorCode, kw)
                    .or().like(Mistake::getErrorMsg, kw));
        }
        if (hasText(techDirection)) {
            wrapper.eq(Mistake::getTechDirection, techDirection);
        }
        if (hasText(errorType)) {
            wrapper.eq(Mistake::getErrorType, errorType);
        }
        // 按标签筛选：先查关联表拿到错题ID集合
        if (tagId != null) {
            List<Long> ids = relMapper.selectList(
                            new LambdaQueryWrapper<MistakeTagRel>().eq(MistakeTagRel::getTagId, tagId))
                    .stream().map(MistakeTagRel::getMistakeId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return emptyPage(pageNum, pageSize);
            }
            wrapper.in(Mistake::getId, ids);
        }
        // 最近 N 天筛选（最近一周传 7）
        if (recentDays != null && recentDays > 0) {
            wrapper.ge(Mistake::getCreateTime, LocalDate.now().minusDays(recentDays).atStartOfDay());
        }
        if (onlyFavorite != null && onlyFavorite == 1) {
            wrapper.eq(Mistake::getIsFavorite, 1);
        }
        wrapper.orderByDesc(Mistake::getIsTop).orderByDesc(Mistake::getCreateTime);

        Page<Mistake> page = mistakeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, this::toVO);
    }

    @Override
    public MistakeVO detail(Long id) {
        return toVO(getOwn(id));
    }

    @Override
    public void toggleTop(Long id) {
        Mistake exist = getOwn(id);
        Mistake update = new Mistake();
        update.setId(id);
        update.setIsTop(exist.getIsTop() != null && exist.getIsTop() == 1 ? 0 : 1);
        mistakeMapper.updateById(update);
    }

    @Override
    public void toggleFavorite(Long id) {
        Mistake exist = getOwn(id);
        Mistake update = new Mistake();
        update.setId(id);
        update.setIsFavorite(exist.getIsFavorite() != null && exist.getIsFavorite() == 1 ? 0 : 1);
        mistakeMapper.updateById(update);
    }

    @Override
    public PageResult<Mistake> archivePage(String keyword, Integer pageNum, Integer pageSize) {
        IPage<Mistake> page = mistakeMapper.selectArchivePage(
                new Page<>(pageNum, pageSize), UserContext.getUserId(), keyword);
        return PageResult.of(page);
    }

    @Override
    public void restore(Long id) {
        int rows = mistakeMapper.restoreById(id, UserContext.getUserId());
        if (rows == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForever(Long id) {
        int rows = mistakeMapper.deleteForever(id, UserContext.getUserId());
        if (rows == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "仅归档中的错题可以彻底删除");
        }
        relMapper.delete(new LambdaQueryWrapper<MistakeTagRel>().eq(MistakeTagRel::getMistakeId, id));
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 查询并校验归属：只能操作自己的错题（Service 层数据归属校验，防横向越权） */
    private Mistake getOwn(Long id) {
        Mistake exist = mistakeMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return exist;
    }

    private void saveTagRels(Long mistakeId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Long userId = UserContext.getUserId();
        for (Long tagId : tagIds) {
            // 只允许打自己的标签
            MistakeTag tag = tagMapper.selectById(tagId);
            if (tag == null || !tag.getUserId().equals(userId)) {
                continue;
            }
            MistakeTagRel rel = new MistakeTagRel();
            rel.setMistakeId(mistakeId);
            rel.setTagId(tagId);
            relMapper.insert(rel);
        }
    }

    /** 实体 → VO：解析 images JSON 数组、查询标签对象列表 */
    private MistakeVO toVO(Mistake entity) {
        MistakeVO vo = new MistakeVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setImages(JSONUtil.isTypeJSON(entity.getImages())
                ? JSONUtil.toList(entity.getImages(), String.class)
                : Collections.emptyList());
        List<Long> tagIds = relMapper.selectList(new LambdaQueryWrapper<MistakeTagRel>()
                        .eq(MistakeTagRel::getMistakeId, entity.getId()))
                .stream().map(MistakeTagRel::getTagId).collect(Collectors.toList());
        vo.setTags(tagIds.isEmpty() ? Collections.emptyList() : tagMapper.selectBatchIds(tagIds));
        return vo;
    }

    private String joinText(MistakeDTO dto) {
        return String.join(" ",
                dto.getTitle() == null ? "" : dto.getTitle(),
                dto.getErrorCode() == null ? "" : dto.getErrorCode(),
                dto.getErrorMsg() == null ? "" : dto.getErrorMsg(),
                dto.getSolution() == null ? "" : dto.getSolution());
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** LIKE 关键词转义：\ % _ 转为字面量（MySQL LIKE 默认转义符为 \） */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private PageResult<MistakeVO> emptyPage(Integer pageNum, Integer pageSize) {
        PageResult<MistakeVO> r = new PageResult<>();
        r.setTotal(0L);
        r.setPages(0L);
        r.setCurrent(Long.valueOf(pageNum));
        r.setSize(Long.valueOf(pageSize));
        r.setList(Collections.emptyList());
        return r;
    }
}
