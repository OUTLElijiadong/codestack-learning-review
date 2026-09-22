package com.wy.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wy.review.common.BusinessException;
import com.wy.review.common.PageResult;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.dto.AnnouncementDTO;
import com.wy.review.entity.Announcement;
import com.wy.review.entity.AnnouncementRead;
import com.wy.review.entity.User;
import com.wy.review.mapper.AnnouncementMapper;
import com.wy.review.mapper.AnnouncementReadMapper;
import com.wy.review.mapper.UserMapper;
import com.wy.review.service.AnnouncementService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统公告服务实现
 * 未读弹窗口径：已发布(status=1)公告 LEFT JOIN 我的已读记录，已读为空的即未读；
 * 公告量小，采用"两次查询 + 内存差集"，无需 JOIN SQL
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadMapper readMapper;
    private final UserMapper userMapper;

    public AnnouncementServiceImpl(AnnouncementMapper announcementMapper,
                                   AnnouncementReadMapper readMapper, UserMapper userMapper) {
        this.announcementMapper = announcementMapper;
        this.readMapper = readMapper;
        this.userMapper = userMapper;
    }

    // ------------------------------------------------------------------
    // 管理端
    // ------------------------------------------------------------------

    @Override
    public PageResult<Announcement> adminPage(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Announcement::getTitle, keyword.trim());
        }
        wrapper.orderByDesc(Announcement::getCreateTime);
        return PageResult.of(announcementMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Override
    public void add(AnnouncementDTO dto) {
        Announcement a = new Announcement();
        a.setTitle(dto.getTitle());
        a.setContent(dto.getContent());
        a.setType(dto.getType());
        a.setPublisherId(UserContext.getUserId());
        User publisher = userMapper.selectById(UserContext.getUserId());
        a.setPublisherName(publisher == null ? "" : publisher.getNickname());
        a.setStatus(dto.getStatus());
        if (dto.getStatus() != null && dto.getStatus() == 1) {
            a.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.insert(a);
    }

    @Override
    public void update(Long id, AnnouncementDTO dto) {
        Announcement exist = getExist(id);
        Announcement update = new Announcement();
        update.setId(exist.getId());
        update.setTitle(dto.getTitle());
        update.setContent(dto.getContent());
        update.setType(dto.getType());
        update.setStatus(dto.getStatus());
        // 草稿首次变为已发布时写发布时间；已发布公告修改后已读记录保留
        if (dto.getStatus() != null && dto.getStatus() == 1 && exist.getPublishTime() == null) {
            update.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.updateById(update);
    }

    @Override
    public void offline(Long id) {
        getExist(id);
        Announcement update = new Announcement();
        update.setId(id);
        update.setStatus(2);
        announcementMapper.updateById(update);
    }

    @Override
    public void remove(Long id) {
        getExist(id);
        announcementMapper.deleteById(id); // 逻辑删除
    }

    @Override
    public Map<String, Object> readStats(Long id) {
        getExist(id);
        Long totalStudents = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "student"));
        Long readCount = readMapper.selectCount(new LambdaQueryWrapper<AnnouncementRead>()
                .eq(AnnouncementRead::getAnnouncementId, id));
        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("readCount", readCount);
        result.put("unreadCount", Math.max(0, totalStudents - readCount));
        return result;
    }

    // ------------------------------------------------------------------
    // 学生端
    // ------------------------------------------------------------------

    @Override
    public PageResult<Announcement> studentPage(Integer pageNum, Integer pageSize) {
        Page<Announcement> page = announcementMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getPublishTime));
        return PageResult.of(page);
    }

    @Override
    public List<Announcement> unread() {
        Long userId = UserContext.getUserId();
        List<Announcement> published = announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishTime));
        if (published.isEmpty()) {
            return published;
        }
        Set<Long> readIds = readMapper.selectList(new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getUserId, userId))
                .stream().map(AnnouncementRead::getAnnouncementId).collect(Collectors.toSet());
        return published.stream().filter(a -> !readIds.contains(a.getId())).collect(Collectors.toList());
    }

    @Override
    public void markRead(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        AnnouncementRead read = new AnnouncementRead();
        read.setAnnouncementId(id);
        read.setUserId(UserContext.getUserId());
        read.setReadTime(LocalDateTime.now());
        try {
            readMapper.insert(read);
        } catch (DuplicateKeyException ignored) {
            // 联合唯一键防重复已读，重复标记静默成功
        }
    }

    private Announcement getExist(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return a;
    }
}
