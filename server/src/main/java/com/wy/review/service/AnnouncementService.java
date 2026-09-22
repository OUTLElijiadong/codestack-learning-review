package com.wy.review.service;

import com.wy.review.common.PageResult;
import com.wy.review.dto.AnnouncementDTO;
import com.wy.review.entity.Announcement;

import java.util.List;
import java.util.Map;

/**
 * 系统公告服务（管理端发布管理 + 学生端未读弹窗）
 */
public interface AnnouncementService {

    // ---------------- 管理端 ----------------

    PageResult<Announcement> adminPage(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /** 发布/存草稿公告 */
    void add(AnnouncementDTO dto);

    void update(Long id, AnnouncementDTO dto);

    /** 下线公告（用户端不再展示与弹出） */
    void offline(Long id);

    void remove(Long id);

    /** 公告已读/未读人数统计 */
    Map<String, Object> readStats(Long id);

    // ---------------- 学生端 ----------------

    PageResult<Announcement> studentPage(Integer pageNum, Integer pageSize);

    /** 未读公告列表（登录弹窗数据源）：已发布且我不在已读表中的公告 */
    List<Announcement> unread();

    /** 标记已读（联合唯一键防重复） */
    void markRead(Long id);
}
