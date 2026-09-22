package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.StudyTimeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习时长记录表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface StudyTimeLogMapper extends BaseMapper<StudyTimeLog> {
}
