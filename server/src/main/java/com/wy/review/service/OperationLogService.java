package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.entity.OperationLogEntity;

/**
 * 操作日志查询服务（仅超管）
 */
public interface OperationLogService {

    /** 日志分页：按操作人/模块/结果/时间范围筛选，按时间倒序 */
    PageResult<OperationLogEntity> page(String username, String module, Integer result,
                                        String startDate, String endDate,
                                        Integer pageNum, Integer pageSize);
}
