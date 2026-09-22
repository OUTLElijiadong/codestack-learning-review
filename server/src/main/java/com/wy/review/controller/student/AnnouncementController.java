package com.wy.review.controller.student;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.entity.Announcement;
import com.wy.review.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端公告控制器：公告列表 + 未读公告弹窗 + 标记已读
 */
@RestController
@RequestMapping("/student/announcement")
@RequireRole("student")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** 公告列表（仅已发布，按发布时间倒序） */
    @GetMapping("/page")
    public Result<PageResult<Announcement>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(announcementService.studentPage(pageNum, pageSize));
    }

    /** 未读公告列表（登录后弹窗推送数据源） */
    @GetMapping("/unread")
    public Result<List<Announcement>> unread() {
        return Result.ok(announcementService.unread());
    }

    /** 标记已读（弹窗关闭时逐条调用） */
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        announcementService.markRead(id);
        return Result.ok();
    }
}
