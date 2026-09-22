package com.wy.review.controller.admin;

import com.wy.review.annotation.OperationLog;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.AnnouncementDTO;
import com.wy.review.entity.Announcement;
import com.wy.review.service.AnnouncementService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统公告推送管理控制器
 * 发布编程学习通知/系统维护通知，发布后弹窗推送所有用户（登录未读弹窗）
 */
@RestController
@RequestMapping("/admin/announcement")
@RequireRole({"teacher", "admin"})
public class AnnouncementManageController {

    private final AnnouncementService announcementService;

    public AnnouncementManageController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** 公告分页列表（含草稿/已发布/已下线） */
    @GetMapping("/page")
    public Result<PageResult<Announcement>> page(@RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(announcementService.adminPage(status, keyword, pageNum, pageSize));
    }

    /** 发布公告（可直接发布或存草稿） */
    @PostMapping
    @OperationLog(module = "公告管理", operation = "发布公告")
    public Result<Void> add(@Validated @RequestBody AnnouncementDTO dto) {
        announcementService.add(dto);
        return Result.ok();
    }

    /** 编辑公告 */
    @PutMapping("/{id}")
    @OperationLog(module = "公告管理", operation = "编辑公告")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody AnnouncementDTO dto) {
        announcementService.update(id, dto);
        return Result.ok();
    }

    /** 下线公告 */
    @PutMapping("/{id}/offline")
    @OperationLog(module = "公告管理", operation = "下线公告")
    public Result<Void> offline(@PathVariable Long id) {
        announcementService.offline(id);
        return Result.ok();
    }

    /** 删除公告（逻辑删除） */
    @DeleteMapping("/{id}")
    @OperationLog(module = "公告管理", operation = "删除公告")
    public Result<Void> remove(@PathVariable Long id) {
        announcementService.remove(id);
        return Result.ok();
    }

    /** 公告已读/未读人数统计 */
    @GetMapping("/{id}/read-stats")
    public Result<Map<String, Object>> readStats(@PathVariable Long id) {
        return Result.ok(announcementService.readStats(id));
    }
}
