import request from '@/utils/request'

/* 管理端：全部用户统一管理（/admin/user，teacher + admin） */

// 用户分页（角色/状态/关键词筛选）
export function getUserPage(params) {
  return request.get('/admin/user/page', { params })
}

// 用户详情（含学习数据摘要）
export function getUserDetail(id) {
  return request.get(`/admin/user/${id}`)
}

// 冻结异常账号
export function freezeUser(id) {
  return request.put(`/admin/user/${id}/freeze`)
}

// 解封账号
export function unfreezeUser(id) {
  return request.put(`/admin/user/${id}/unfreeze`)
}

// 重置学生密码为初始值 123456
export function resetUserPassword(id) {
  return request.put(`/admin/user/${id}/reset-password`)
}

// 活跃学习用户统计
export function getActiveStats() {
  return request.get('/admin/user/active-count')
}
