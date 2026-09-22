package com.wy.review.controller.admin;

import com.wy.review.annotation.OperationLog;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.dto.AdminCreateDTO;
import com.wy.review.service.UserService;
import com.wy.review.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员账号管理控制器（仅超级管理员）
 * 体现权限分级：teacher 角色访问本控制器会被权限拦截器 403 拒绝
 */
@RestController
@RequestMapping("/admin/manage/admin")
@RequireRole("admin")
public class AdminManageController {

    private final UserService userService;

    public AdminManageController(UserService userService) {
        this.userService = userService;
    }

    /** 管理员账号列表（teacher + admin） */
    @GetMapping("/page")
    public Result<PageResult<UserInfoVO>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(userService.adminPage(keyword, pageNum, pageSize));
    }

    /** 新增普通管理员（教师）账号 */
    @PostMapping
    @OperationLog(module = "权限管理", operation = "新增管理员")
    public Result<Void> create(@Validated @RequestBody AdminCreateDTO dto) {
        userService.createAdmin(dto);
        return Result.ok();
    }

    /** 重置管理员密码 */
    @PutMapping("/{id}/password")
    @OperationLog(module = "权限管理", operation = "重置管理员密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetAdminPassword(id, newPassword);
        return Result.ok();
    }

    /** 删除管理员（内置 admin 与当前登录人不可删） */
    @DeleteMapping("/{id}")
    @OperationLog(module = "权限管理", operation = "删除管理员")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteAdmin(id);
        return Result.ok();
    }
}
