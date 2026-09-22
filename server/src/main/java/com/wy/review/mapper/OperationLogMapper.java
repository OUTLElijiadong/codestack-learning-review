package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 * 注意：实体类是 OperationLogEntity（避开与注解 @OperationLog 同名），
 * 但 Mapper 命名为 OperationLogMapper 更符合业务语义
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
