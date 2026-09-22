package com.wy.review.controller.admin;

import com.wy.review.annotation.OperationLog;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.entity.SensitiveWord;
import com.wy.review.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 敏感词库管理控制器（不良关键词自动拦截的词表维护）
 */
@RestController
@RequestMapping("/admin/sensitive-word")
@RequireRole({"teacher", "admin"})
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /** 敏感词分页列表 */
    @GetMapping("/page")
    public Result<PageResult<SensitiveWord>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(sensitiveWordService.page(keyword, pageNum, pageSize));
    }

    /** 新增敏感词（成功后内存词库热刷新，立即生效） */
    @PostMapping
    @OperationLog(module = "敏感词管理", operation = "新增敏感词")
    public Result<Void> add(@RequestBody Map<String, String> body) {
        sensitiveWordService.add(body.get("word"));
        return Result.ok();
    }

    /** 删除敏感词 */
    @DeleteMapping("/{id}")
    @OperationLog(module = "敏感词管理", operation = "删除敏感词")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.ok();
    }
}
