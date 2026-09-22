package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答问题表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
