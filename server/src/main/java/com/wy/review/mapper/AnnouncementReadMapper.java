package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.AnnouncementRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告已读关系表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {
}
