package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记分类表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {
}
