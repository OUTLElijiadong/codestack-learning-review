package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.dto.NoteDTO;
import com.wy.review.entity.Note;
import com.wy.review.entity.NoteCategory;
import com.wy.review.vo.NoteSquareVO;

import java.util.List;
import java.util.Map;

/**
 * 在线代码笔记服务（含分类管理与备份）
 */
public interface NoteService {

    /** 新增笔记（敏感词过滤；公开则进入待审核） */
    void add(NoteDTO dto);

    /** 编辑笔记（内容变更后重新进入审核流） */
    void update(Long id, NoteDTO dto);

    /** 删除笔记（逻辑删除） */
    void remove(Long id);

    /** 我的笔记分页（分类/关键词/公开性筛选；列表不查 content 大字段） */
    PageResult<Note> page(String keyword, Long categoryId, Integer isPublic, Integer pageNum, Integer pageSize);

    /** 笔记详情：本人任意状态；非本人仅公开且审核通过，浏览数+1 */
    Note detail(Long id);

    /** 公开/私密切换：私密→公开 置待审核；公开→私密 立即生效 */
    void toggleVisibility(Long id, Integer isPublic);

    /** 公开笔记广场分页（全站公开且审核通过，带作者信息） */
    PageResult<NoteSquareVO> square(String keyword, Long categoryId, Integer pageNum, Integer pageSize);

    // ---------------- 分类管理 ----------------

    /** 分类列表：系统内置(user_id=0) + 我的自建，含各类笔记数 */
    List<Map<String, Object>> categoryList();

    NoteCategory addCategory(String name);

    void renameCategory(Long id, String name);

    /** 删除分类：系统内置不可删；分类下我的笔记移入"未分类"(categoryId=0) */
    void deleteCategory(Long id);

    // ---------------- 备份 ----------------

    /** 手动备份我的全部笔记为 JSON 文件，返回备份文件名 */
    String backupMyNotes();

    /** 我的备份文件列表（扫描 backups 目录） */
    List<Map<String, Object>> backupList();
}
