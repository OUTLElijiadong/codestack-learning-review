package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答评论表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
