package com.wy.review.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.NoteDTO;
import com.wy.review.entity.Note;
import com.wy.review.entity.NoteCategory;
import com.wy.review.entity.User;
import com.wy.review.mapper.NoteCategoryMapper;
import com.wy.review.mapper.NoteMapper;
import com.wy.review.mapper.UserMapper;
import com.wy.review.service.NoteService;
import com.wy.review.utils.SensitiveWordUtil;
import com.wy.review.vo.NoteSquareVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线代码笔记服务实现
 * 审核状态机：私密(恒为正常) ⇄ 公开(待审核 → 管理员通过 → 广场可见；下架 → 仅本人可见标记)
 */
@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteCategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    @Value("${review.backup.path}")
    private String backupPath;

    public NoteServiceImpl(NoteMapper noteMapper, NoteCategoryMapper categoryMapper,
                           UserMapper userMapper, SensitiveWordUtil sensitiveWordUtil) {
        this.noteMapper = noteMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    @Override
    public void add(NoteDTO dto) {
        sensitiveWordUtil.assertClean(dto.getTitle() + " " + stripHtml(dto.getContent()));
        Note note = new Note();
        note.setUserId(UserContext.getUserId());
        note.setCategoryId(dto.getCategoryId());
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setSummary(makeSummary(dto.getContent()));
        note.setIsPublic(dto.getIsPublic());
        // 公开笔记进入审核流；私密笔记恒为正常
        note.setAuditStatus(dto.getIsPublic() != null && dto.getIsPublic() == 1 ? 0 : 1);
        note.setViewCount(0);
        noteMapper.insert(note);
    }

    @Override
    public void update(Long id, NoteDTO dto) {
        Note exist = getOwn(id);
        sensitiveWordUtil.assertClean(dto.getTitle() + " " + stripHtml(dto.getContent()));
        Note update = new Note();
        update.setId(exist.getId());
        update.setTitle(dto.getTitle());
        update.setContent(dto.getContent());
        update.setSummary(makeSummary(dto.getContent()));
        update.setCategoryId(dto.getCategoryId());
        update.setIsPublic(dto.getIsPublic());
        // 公开笔记内容变更后重新进入审核
        update.setAuditStatus(dto.getIsPublic() != null && dto.getIsPublic() == 1 ? 0 : 1);
        noteMapper.updateById(update);
    }

    @Override
    public void remove(Long id) {
        getOwn(id);
        noteMapper.deleteById(id); // 逻辑删除
    }

    @Override
    public PageResult<Note> page(String keyword, Long categoryId, Integer isPublic,
                                 Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, UserContext.getUserId())
                // 列表页不查 content 大字段，提升分页性能
                .select(Note.class, field -> !"content".equals(field.getColumn()));
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getSummary, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        if (isPublic != null) {
            wrapper.eq(Note::getIsPublic, isPublic);
        }
        wrapper.orderByDesc(Note::getCreateTime);
        return PageResult.of(noteMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public Note detail(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        boolean mine = note.getUserId().equals(UserContext.getUserId());
        if (!mine) {
            // 非本人仅可查看公开且审核通过的笔记；不暴露存在性
            if (note.getIsPublic() == null || note.getIsPublic() != 1
                    || note.getAuditStatus() == null || note.getAuditStatus() != 1) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND);
            }
            Note inc = new Note();
            inc.setId(note.getId());
            inc.setViewCount(note.getViewCount() + 1);
            noteMapper.updateById(inc);
            // 返回对象同步最新浏览数，保证接口出参就是数据库当前值
            note.setViewCount(note.getViewCount() + 1);
        }
        return note;
    }

    @Override
    public void toggleVisibility(Long id, Integer isPublic) {
        Note exist = getOwn(id);
        Note update = new Note();
        update.setId(exist.getId());
        update.setIsPublic(isPublic);
        // 私密转公开 → 进入审核流；公开转私密 → 立即生效且脱离公域
        update.setAuditStatus(isPublic != null && isPublic == 1 ? 0 : 1);
        noteMapper.updateById(update);
    }

    @Override
    public PageResult<NoteSquareVO> square(String keyword, Long categoryId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getIsPublic, 1)
                .eq(Note::getAuditStatus, 1)
                .select(Note.class, field -> !"content".equals(field.getColumn()));
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getSummary, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Note::getCreateTime);
        Page<Note> page = noteMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量取作者与分类信息，避免循环查库
        List<Long> userIds = page.getRecords().stream().map(Note::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, String> catMap = categoryNameMap();

        return PageResult.of(page, note -> {
            NoteSquareVO vo = new NoteSquareVO();
            vo.setId(note.getId());
            vo.setTitle(note.getTitle());
            vo.setSummary(note.getSummary());
            vo.setCategoryId(note.getCategoryId());
            vo.setCategoryName(catMap.getOrDefault(note.getCategoryId(), "未分类"));
            vo.setViewCount(note.getViewCount());
            vo.setCreateTime(note.getCreateTime());
            vo.setUserId(note.getUserId());
            User author = userMap.get(note.getUserId());
            vo.setNickname(author == null ? "匿名同学" : author.getNickname());
            vo.setAvatar(author == null ? "" : author.getAvatar());
            return vo;
        });
    }

    // ------------------------------------------------------------------
    // 分类管理
    // ------------------------------------------------------------------

    @Override
    public List<Map<String, Object>> categoryList() {
        Long userId = UserContext.getUserId();
        List<NoteCategory> cats = categoryMapper.selectList(new LambdaQueryWrapper<NoteCategory>()
                .and(w -> w.eq(NoteCategory::getUserId, 0).or().eq(NoteCategory::getUserId, userId))
                .orderByAsc(NoteCategory::getSort)
                .orderByAsc(NoteCategory::getId));
        // 我的笔记按分类计数
        List<Map<String, Object>> counts = noteMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Note>()
                        .select("category_id AS categoryId", "COUNT(*) AS cnt")
                        .eq("user_id", userId)
                        .groupBy("category_id"));
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : counts) {
            countMap.put(Long.valueOf(row.get("categoryId").toString()), Long.valueOf(row.get("cnt").toString()));
        }
        return cats.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("sort", c.getSort());
            map.put("isSystem", c.getIsSystem());
            map.put("count", countMap.getOrDefault(c.getId(), 0L));
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public NoteCategory addCategory(String name) {
        Long userId = UserContext.getUserId();
        Long dup = categoryMapper.selectCount(new LambdaQueryWrapper<NoteCategory>()
                .in(NoteCategory::getUserId, Arrays.asList(0L, userId))
                .eq(NoteCategory::getName, name));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "分类已存在");
        }
        NoteCategory cat = new NoteCategory();
        cat.setUserId(userId);
        cat.setName(name);
        cat.setSort(99);
        cat.setIsSystem(0);
        categoryMapper.insert(cat);
        return cat;
    }

    @Override
    public void renameCategory(Long id, String name) {
        NoteCategory cat = categoryMapper.selectById(id);
        if (cat == null || cat.getIsSystem() == 1
                || !cat.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "分类不存在或为系统内置分类");
        }
        NoteCategory update = new NoteCategory();
        update.setId(id);
        update.setName(name);
        categoryMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        NoteCategory cat = categoryMapper.selectById(id);
        if (cat == null || cat.getIsSystem() == 1
                || !cat.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "分类不存在或为系统内置分类");
        }
        // 分类下我的笔记移入"未分类"（categoryId=0），保证笔记不丢
        Note move = new Note();
        move.setCategoryId(0L);
        noteMapper.update(move, new LambdaQueryWrapper<Note>()
                .eq(Note::getCategoryId, id)
                .eq(Note::getUserId, UserContext.getUserId()));
        // 逻辑删除前先改名释放 uk_user_name 唯一键，保证同名分类可重建
        NoteCategory rename = new NoteCategory();
        rename.setId(id);
        rename.setName(cat.getName() + "#del" + id);
        categoryMapper.updateById(rename);
        categoryMapper.deleteById(id);
    }

    // ------------------------------------------------------------------
    // 备份
    // ------------------------------------------------------------------

    @Override
    public String backupMyNotes() {
        Long userId = UserContext.getUserId();
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .orderByAsc(Note::getId));
        String fileName = "notes-u" + userId + "-"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".json";
        File dir = new File(backupPath).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileUtil.writeString(JSONUtil.toJsonPrettyStr(notes), new File(dir, fileName), StandardCharsets.UTF_8);
        return fileName;
    }

    @Override
    public List<Map<String, Object>> backupList() {
        String prefix = "notes-u" + UserContext.getUserId() + "-";
        File dir = new File(backupPath).getAbsoluteFile();
        if (!dir.exists()) {
            return Collections.emptyList();
        }
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix) && name.endsWith(".json"));
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .sorted((a, b) -> b.getName().compareTo(a.getName()))
                .map(f -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileName", f.getName());
                    map.put("size", f.length());
                    map.put("time", new Date(f.lastModified()));
                    return map;
                }).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    private Note getOwn(Long id) {
        Note exist = noteMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return exist;
    }

    /** 富文本转纯文本摘要（去 HTML 标签，截取前 120 字） */
    private String makeSummary(String html) {
        String text = stripHtml(html);
        return text.length() > 120 ? text.substring(0, 120) : text;
    }

    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private Map<Long, String> categoryNameMap() {
        Long userId = UserContext.getUserId();
        List<NoteCategory> cats = categoryMapper.selectList(new LambdaQueryWrapper<NoteCategory>()
                .and(w -> w.eq(NoteCategory::getUserId, 0).or().eq(NoteCategory::getUserId, userId)));
        Map<Long, String> map = new HashMap<>();
        map.put(0L, "未分类");
        for (NoteCategory c : cats) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
