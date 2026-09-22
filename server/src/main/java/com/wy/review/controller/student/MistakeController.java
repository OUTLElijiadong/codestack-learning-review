package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.MistakeDTO;
import com.wy.review.entity.Mistake;
import com.wy.review.service.MistakeService;
import com.wy.review.vo.MistakeVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 错题本控制器（学生端）
 * 覆盖：手动添加、编辑、删除归档、收藏、置顶、分页列表、智能检索、归档回收站
 */
@RestController
@RequestMapping("/student/mistake")
@RequireRole("student")
public class MistakeController {

    private final MistakeService mistakeService;

    public MistakeController(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    /** 手动添加错题 */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody MistakeDTO dto) {
        mistakeService.add(dto);
        return Result.ok();
    }

    /** 一键重新编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody MistakeDTO dto) {
        mistakeService.update(id, dto);
        return Result.ok();
    }

    /** 一键删除归档（逻辑删除，进回收站） */
    @DeleteMapping("/{id}")
    public Result<Void> archive(@PathVariable Long id) {
        mistakeService.archive(id);
        return Result.ok();
    }

    /** 我的错题分页列表 */
    @GetMapping("/page")
    public Result<PageResult<MistakeVO>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String techDirection,
                                              @RequestParam(required = false) String errorType,
                                              @RequestParam(required = false) Long tagId,
                                              @RequestParam(required = false) Integer onlyFavorite,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(mistakeService.search(keyword, techDirection, errorType, tagId,
                null, onlyFavorite, pageNum, pageSize));
    }

    /** 错题详情（含标签与截图数组） */
    @GetMapping("/{id}")
    public Result<MistakeVO> detail(@PathVariable Long id) {
        return Result.ok(mistakeService.detail(id));
    }

    /** 置顶/取消置顶 */
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        mistakeService.toggleTop(id);
        return Result.ok();
    }

    /** 收藏/取消收藏 */
    @PutMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id) {
        mistakeService.toggleFavorite(id);
        return Result.ok();
    }

    /**
     * 错题智能检索：关键词全文搜索(标题+代码+报错) + 技术方向 + 错误类型 +
     * 标签 + 最近N天(最近一周传7)，快速定位自己不会的知识点
     */
    @GetMapping("/search")
    public Result<PageResult<MistakeVO>> search(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String techDirection,
                                                @RequestParam(required = false) String errorType,
                                                @RequestParam(required = false) Long tagId,
                                                @RequestParam(required = false) Integer recentDays,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(mistakeService.search(keyword, techDirection, errorType, tagId,
                recentDays, null, pageNum, pageSize));
    }

    /** 归档（回收站）分页 */
    @GetMapping("/archive")
    public Result<PageResult<Mistake>> archivePage(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(mistakeService.archivePage(keyword, pageNum, pageSize));
    }

    /** 从归档还原 */
    @PutMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        mistakeService.restore(id);
        return Result.ok();
    }

    /** 彻底删除（物理删除，前端需二次确认） */
    @DeleteMapping("/{id}/forever")
    public Result<Void> deleteForever(@PathVariable Long id) {
        mistakeService.deleteForever(id);
        return Result.ok();
    }
}
