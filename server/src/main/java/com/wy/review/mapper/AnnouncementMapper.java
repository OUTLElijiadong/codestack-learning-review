package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
