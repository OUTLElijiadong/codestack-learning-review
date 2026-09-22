package com.wy.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wy.review.entity.Mistake;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 错题 Mapper
 * 归档查询 / 还原 / 彻底删除 需要绕过全局逻辑删除的自动拼接，必须手写 SQL（见 MistakeMapper.xml）
 */
@Mapper
public interface MistakeMapper extends BaseMapper<Mistake> {

    /**
     * 归档（回收站）分页：查 deleted=1 的数据。
     * MyBatis-Plus 自动方法会强制拼接 deleted=0，归档查询只能手写
     */
    IPage<Mistake> selectArchivePage(IPage<Mistake> page, @Param("userId") Long userId, @Param("keyword") String keyword);

    /** 从归档还原：deleted 置回 0（手写 UPDATE 绕过逻辑删除） */
    int restoreById(@Param("id") Long id, @Param("userId") Long userId);

    /** 彻底删除：物理 DELETE，不可恢复 */
    int deleteForever(@Param("id") Long id, @Param("userId") Long userId);
}
