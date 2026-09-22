package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记 Mapper：单表 CRUD 用 BaseMapper；
 * 审核/广场等列表查询在 Service 层用 QueryWrapper 完成
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
