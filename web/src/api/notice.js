import request from '@/utils/request'

/* 学生端公告（/student/announcement） */

// 公告列表（仅已发布）
export function getNoticePage(params) {
  return request.get('/student/announcement/page', { params })
}

// 未读公告列表（登录后弹窗推送数据源）
export function getUnreadNotices() {
  return request.get('/student/announcement/unread')
}

// 标记公告已读
export function markNoticeRead(id) {
  return request.post(`/student/announcement/${id}/read`)
}
