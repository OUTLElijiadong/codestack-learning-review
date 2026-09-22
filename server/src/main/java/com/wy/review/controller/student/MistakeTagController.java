package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.Result;
import com.wy.review.entity.MistakeTag;
import com.wy.review.service.MistakeTagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 错题标签控制器（学生端）：标签分类管理
 */
@RestController
@RequestMapping("/student/mistake/tag")
@RequireRole("student")
public class MistakeTagController {

    private final MistakeTagService tagService;

    public MistakeTagController(MistakeTagService tagService) {
        this.tagService = tagService;
    }

    /** 我的标签列表（含每个标签下错题数） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(tagService.listMyTags());
    }

    /** 新增标签 */
    @PostMapping
    public Result<MistakeTag> add(@RequestBody Map<String, String> body) {
        return Result.ok(tagService.add(body.get("name"), body.get("color")));
    }

    /** 重命名标签 */
    @PutMapping("/{id}")
    public Result<Void> rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
        tagService.rename(id, body.get("name"), body.get("color"));
        return Result.ok();
    }

    /** 删除标签 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
