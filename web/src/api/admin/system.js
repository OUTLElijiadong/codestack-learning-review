import request from '@/utils/request'

/* 管理端：权限分级管理（/admin/manage，仅超级管理员 admin） */

// 管理员账号分页
export function getAdminPage(params) {
  return request.get('/admin/manage/admin/page', { params })
}

// 新增普通管理员（教师）账号
export function addAdmin(data) {
  return request.post('/admin/manage/admin', data)
}

// 重置管理员密码
export function resetAdminPassword(id, newPassword) {
  return request.put(`/admin/manage/admin/${id}/password`, null, { params: { newPassword } })
}

// 删除管理员
export function removeAdmin(id) {
  return request.delete(`/admin/manage/admin/${id}`)
}

// 操作日志分页查询
export function getLogPage(params) {
  return request.get('/admin/manage/log/page', { params })
}
