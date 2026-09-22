package com.wy.review.controller.admin;

import com.wy.review.annotation.RequireRole;
import com.wy.review.common.PageResult;
import com.wy.review.common.Result;
import com.wy.review.entity.OperationLogEntity;
import com.wy.review.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志查询控制器（仅超级管理员）
 * 全部管理员操作由 AOP 自动落库，这里提供多条件分页查询
 */
@RestController
@RequestMapping("/admin/manage/log")
@RequireRole("admin")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /** 操作日志分页查询 */
    @GetMapping("/page")
    public Result<PageResult<OperationLogEntity>> page(@RequestParam(required = false) String username,
                                                       @RequestParam(required = false) String module,
                                                       @RequestParam(required = false) Integer result,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(operationLogService.page(username, module, result, startDate, endDate, pageNum, pageSize));
    }
}
