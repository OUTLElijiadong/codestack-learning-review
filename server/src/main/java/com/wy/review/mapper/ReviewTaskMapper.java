package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.ReviewTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 复盘任务完成记录表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface ReviewTaskMapper extends BaseMapper<ReviewTask> {
}
