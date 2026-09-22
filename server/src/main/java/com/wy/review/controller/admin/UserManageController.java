package com.wy.review.controller.admin;

import com.wy.review.annotation.OperationLog;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.service.UserService;
import com.wy.review.vo.UserInfoVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 全部用户统一管理控制器（教师=普通管理员 与 超管 均可）
 * 覆盖：查看所有注册学生、冻结/解封异常账号、活跃学习用户统计
 */
@RestController
@RequestMapping("/admin/user")
@RequireRole({"teacher", "admin"})
public class UserManageController {

    private final UserService userService;

    public UserManageController(UserService userService) {
        this.userService = userService;
    }

    /** 用户分页列表（角色/状态/关键词筛选，密码密保不出参） */
    @GetMapping("/page")
    public Result<PageResult<UserInfoVO>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String role,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(userService.page(keyword, role, status, pageNum, pageSize));
    }

    /** 用户详情（基础信息 + 学习数据摘要） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(userService.detail(id));
    }

    /** 冻结异常账号 */
    @PutMapping("/{id}/freeze")
    @OperationLog(module = "用户管理", operation = "冻结账号")
    public Result<Void> freeze(@PathVariable Long id) {
        userService.freeze(id);
        return Result.ok();
    }

    /** 解封账号 */
    @PutMapping("/{id}/unfreeze")
    @OperationLog(module = "用户管理", operation = "解封账号")
    public Result<Void> unfreeze(@PathVariable Long id) {
        userService.unfreeze(id);
        return Result.ok();
    }

    /** 重置学生密码为初始值 123456 */
    @PutMapping("/{id}/reset-password")
    @OperationLog(module = "用户管理", operation = "重置学生密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok();
    }

    /** 活跃学习用户统计 */
    @GetMapping("/active-count")
    public Result<Map<String, Object>> activeStats() {
        return Result.ok(userService.activeStats());
    }
}
