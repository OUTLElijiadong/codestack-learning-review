package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
