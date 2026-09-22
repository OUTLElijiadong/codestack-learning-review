package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.MistakeTagRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错题-标签关联表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface MistakeTagRelMapper extends BaseMapper<MistakeTagRel> {
}
