package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wy.review.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper：单表 CRUD 用 BaseMapper；
 * 各专业活跃度等统计查询在 Service 层用 QueryWrapper 分组聚合完成
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
