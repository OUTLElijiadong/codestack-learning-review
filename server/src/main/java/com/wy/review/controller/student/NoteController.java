package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.NoteDTO;
import com.wy.review.entity.Note;
import com.wy.review.entity.NoteCategory;
import com.wy.review.service.NoteService;
import com.wy.review.vo.NoteSquareVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线代码笔记控制器（学生端）
 * 覆盖：富文本笔记 CRUD、分类归档、公开/私密切换、公开笔记广场、搜索、备份
 */
@RestController
@RequestMapping("/student/note")
@RequireRole("student")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /** 新增笔记 */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody NoteDTO dto) {
        noteService.add(dto);
        return Result.ok();
    }

    /** 编辑笔记 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody NoteDTO dto) {
        noteService.update(id, dto);
        return Result.ok();
    }

    /** 删除笔记（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        noteService.remove(id);
        return Result.ok();
    }

    /** 我的笔记分页：分类/关键词快速查找/公开性筛选 */
    @GetMapping("/page")
    public Result<PageResult<Note>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) Integer isPublic,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(noteService.page(keyword, categoryId, isPublic, pageNum, pageSize));
    }

    /** 笔记详情 */
    @GetMapping("/{id}")
    public Result<Note> detail(@PathVariable Long id) {
        return Result.ok(noteService.detail(id));
    }

    /** 公开/私密切换 */
    @PutMapping("/{id}/visibility")
    public Result<Void> visibility(@PathVariable Long id, @RequestParam Integer isPublic) {
        noteService.toggleVisibility(id, isPublic);
        return Result.ok();
    }

    /** 公开笔记广场：浏览全站公开且审核通过的笔记 */
    @GetMapping("/square")
    public Result<PageResult<NoteSquareVO>> square(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long categoryId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(noteService.square(keyword, categoryId, pageNum, pageSize));
    }

    // ---------------- 分类管理 ----------------

    /** 分类列表（系统内置 + 我的自建，含各类笔记数） */
    @GetMapping("/category/list")
    public Result<List<Map<String, Object>>> categoryList() {
        return Result.ok(noteService.categoryList());
    }

    /** 新增分类 */
    @PostMapping("/category")
    public Result<NoteCategory> addCategory(@RequestBody Map<String, String> body) {
        return Result.ok(noteService.addCategory(body.get("name")));
    }

    /** 分类改名（系统内置不可改） */
    @PutMapping("/category/{id}")
    public Result<Void> renameCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        noteService.renameCategory(id, body.get("name"));
        return Result.ok();
    }

    /** 删除分类（系统内置不可删；分类下笔记移入未分类） */
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        noteService.deleteCategory(id);
        return Result.ok();
    }

    // ---------------- 备份 ----------------

    /** 手动备份我的全部笔记（定时自动备份由 NoteBackupTask 每天凌晨执行） */
    @PostMapping("/backup")
    public Result<Map<String, String>> backup() {
        Map<String, String> data = new HashMap<>();
        data.put("fileName", noteService.backupMyNotes());
        return Result.ok(data);
    }

    /** 我的备份文件列表 */
    @GetMapping("/backup/list")
    public Result<List<Map<String, Object>>> backupList() {
        return Result.ok(noteService.backupList());
    }
}
