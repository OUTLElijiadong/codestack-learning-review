package com.wy.review.controller.admin;

import com.wy.review.annotation.OperationLog;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.entity.Mistake;
import com.wy.review.entity.Note;
import com.wy.review.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内容审核控制器：违规错题/违规笔记的人工审核与一键下架
 * （不良关键词自动拦截在发布链路的 Service 层，这里是人工复核通道）
 */
@RestController
@RequestMapping("/admin/audit")
@RequireRole({"teacher", "admin"})
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /** 笔记审核列表 */
    @GetMapping("/note/page")
    public Result<PageResult<Note>> notePage(@RequestParam(required = false) Integer auditStatus,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(auditService.notePage(auditStatus, keyword, pageNum, pageSize));
    }

    /** 笔记审核通过 */
    @PutMapping("/note/{id}/pass")
    @OperationLog(module = "内容审核", operation = "笔记审核通过")
    public Result<Void> notePass(@PathVariable Long id) {
        auditService.notePass(id);
        return Result.ok();
    }

    /** 笔记违规一键下架 */
    @PutMapping("/note/{id}/reject")
    @OperationLog(module = "内容审核", operation = "下架违规笔记")
    public Result<Void> noteReject(@PathVariable Long id,
                                   @RequestBody(required = false) Map<String, String> body) {
        auditService.noteReject(id, body == null ? null : body.get("remark"));
        return Result.ok();
    }

    /** 审核查看笔记完整内容（管理端不受状态限制） */
    @GetMapping("/note/{id}")
    public Result<Note> noteDetail(@PathVariable Long id) {
        return Result.ok(auditService.noteDetail(id));
    }

    /** 审核查看错题完整内容（管理端不受状态限制） */
    @GetMapping("/mistake/{id}")
    public Result<Mistake> mistakeDetail(@PathVariable Long id) {
        return Result.ok(auditService.mistakeDetail(id));
    }

    /** 错题审核列表（管理员巡查） */
    @GetMapping("/mistake/page")
    public Result<PageResult<Mistake>> mistakePage(@RequestParam(required = false) Integer auditStatus,
                                                   @RequestParam(required = false) String techDirection,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(auditService.mistakePage(auditStatus, techDirection, keyword, pageNum, pageSize));
    }

    /** 错题审核通过 */
    @PutMapping("/mistake/{id}/pass")
    @OperationLog(module = "内容审核", operation = "错题审核通过")
    public Result<Void> mistakePass(@PathVariable Long id) {
        auditService.mistakePass(id);
        return Result.ok();
    }

    /** 错题违规一键下架 */
    @PutMapping("/mistake/{id}/reject")
    @OperationLog(module = "内容审核", operation = "下架违规错题")
    public Result<Void> mistakeReject(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body) {
        auditService.mistakeReject(id, body == null ? null : body.get("remark"));
        return Result.ok();
    }
}
