package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打卡签到表 Mapper：单表 CRUD 直接继承 BaseMapper，无需 XML
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
