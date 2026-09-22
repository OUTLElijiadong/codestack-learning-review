import request from '@/utils/request'

/* 管理端：系统公告推送管理（/admin/announcement） */

// 公告分页（含草稿/已发布/已下线）
export function getAdminNoticePage(params) {
  return request.get('/admin/announcement/page', { params })
}

// 发布公告（status=1 直接发布 / 0 存草稿）
export function publishNotice(data) {
  return request.post('/admin/announcement', data)
}

// 编辑公告
export function updateNotice(id, data) {
  return request.put(`/admin/announcement/${id}`, data)
}

// 下线公告
export function offlineNotice(id) {
  return request.put(`/admin/announcement/${id}/offline`)
}

// 删除公告
export function removeNotice(id) {
  return request.delete(`/admin/announcement/${id}`)
}

// 公告已读/未读人数统计
export function getNoticeReadStats(id) {
  return request.get(`/admin/announcement/${id}/read-stats`)
}
