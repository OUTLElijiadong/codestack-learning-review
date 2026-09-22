package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wy.review.common.BusinessException;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.entity.MistakeTag;
import com.wy.review.entity.MistakeTagRel;
import com.wy.review.mapper.MistakeTagMapper;
import com.wy.review.mapper.MistakeTagRelMapper;
import com.wy.review.service.MistakeTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错题标签服务实现
 */
@Service
public class MistakeTagServiceImpl implements MistakeTagService {

    private final MistakeTagMapper tagMapper;
    private final MistakeTagRelMapper relMapper;

    public MistakeTagServiceImpl(MistakeTagMapper tagMapper, MistakeTagRelMapper relMapper) {
        this.tagMapper = tagMapper;
        this.relMapper = relMapper;
    }

    @Override
    public List<Map<String, Object>> listMyTags() {
        Long userId = UserContext.getUserId();
        List<MistakeTag> tags = tagMapper.selectList(new LambdaQueryWrapper<MistakeTag>()
                .eq(MistakeTag::getUserId, userId)
                .orderByAsc(MistakeTag::getSort)
                .orderByAsc(MistakeTag::getId));
        if (tags.isEmpty()) {
            return tags.stream().map(t -> toMap(t, 0L)).collect(Collectors.toList());
        }
        // 按标签分组统计错题数（点击标签反查错题的计数展示）
        List<Long> tagIds = tags.stream().map(MistakeTag::getId).collect(Collectors.toList());
        List<Map<String, Object>> counts = relMapper.selectMaps(new QueryWrapper<MistakeTagRel>()
                .select("tag_id AS tagId", "COUNT(*) AS cnt")
                .in("tag_id", tagIds)
                .groupBy("tag_id"));
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : counts) {
            countMap.put(Long.valueOf(row.get("tagId").toString()),
                    Long.valueOf(row.get("cnt").toString()));
        }
        return tags.stream()
                .map(t -> toMap(t, countMap.getOrDefault(t.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public MistakeTag add(String name, String color) {
        Long userId = UserContext.getUserId();
        Long dup = tagMapper.selectCount(new LambdaQueryWrapper<MistakeTag>()
                .eq(MistakeTag::getUserId, userId)
                .eq(MistakeTag::getName, name));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "标签已存在");
        }
        MistakeTag tag = new MistakeTag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color == null ? "" : color);
        tag.setSort(0);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void rename(Long id, String name, String color) {
        MistakeTag tag = getOwn(id);
        Long dup = tagMapper.selectCount(new LambdaQueryWrapper<MistakeTag>()
                .eq(MistakeTag::getUserId, UserContext.getUserId())
                .eq(MistakeTag::getName, name)
                .ne(MistakeTag::getId, id));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.REPEAT_SUBMIT, "标签名已被占用");
        }
        MistakeTag update = new MistakeTag();
        update.setId(tag.getId());
        update.setName(name);
        update.setColor(color == null ? tag.getColor() : color);
        tagMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MistakeTag tag = getOwn(id);
        // 逻辑删除前先改名释放 uk_user_name 唯一键，保证同名标签可重建
        MistakeTag rename = new MistakeTag();
        rename.setId(id);
        rename.setName(tag.getName() + "#del" + id);
        tagMapper.updateById(rename);
        tagMapper.deleteById(id); // 逻辑删除
        relMapper.delete(new LambdaQueryWrapper<MistakeTagRel>().eq(MistakeTagRel::getTagId, id));
    }

    private MistakeTag getOwn(Long id) {
        MistakeTag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return tag;
    }

    private Map<String, Object> toMap(MistakeTag tag, Long count) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("name", tag.getName());
        map.put("color", tag.getColor());
        map.put("sort", tag.getSort());
        map.put("count", count);
        return map;
    }
}
