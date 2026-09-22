package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.PageResult;
import com.wy.review.entity.OperationLogEntity;
import com.wy.review.mapper.OperationLogMapper;
import com.wy.review.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志查询服务实现（仅超管可查，体现权限分级）
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageResult<OperationLogEntity> page(String username, String module, Integer result,
                                               String startDate, String endDate,
                                               Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(OperationLogEntity::getUsername, username.trim());
        }
        if (module != null && !module.trim().isEmpty()) {
            wrapper.eq(OperationLogEntity::getModule, module.trim());
        }
        if (result != null) {
            wrapper.eq(OperationLogEntity::getResult, result);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLogEntity::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLogEntity::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(OperationLogEntity::getId);
        return PageResult.of(operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }
}
