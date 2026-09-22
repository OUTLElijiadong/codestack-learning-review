import request from '@/utils/request'

/* 管理端：内容审核（/admin/audit）+ 敏感词库（/admin/sensitive-word） */

// 笔记审核分页
export function getNoteAuditPage(params) {
  return request.get('/admin/audit/note/page', { params })
}

// 审核查看笔记完整内容（管理端）
export function getNoteAuditDetail(id) {
  return request.get(`/admin/audit/note/${id}`)
}

// 审核查看错题完整内容（管理端）
export function getMistakeAuditDetail(id) {
  return request.get(`/admin/audit/mistake/${id}`)
}

// 笔记审核通过
export function passNote(id) {
  return request.put(`/admin/audit/note/${id}/pass`)
}

// 笔记一键下架
export function rejectNote(id, remark) {
  return request.put(`/admin/audit/note/${id}/reject`, { remark })
}

// 错题审核分页
export function getMistakeAuditPage(params) {
  return request.get('/admin/audit/mistake/page', { params })
}

// 错题审核通过
export function passMistake(id) {
  return request.put(`/admin/audit/mistake/${id}/pass`)
}

// 错题一键下架
export function rejectMistake(id, remark) {
  return request.put(`/admin/audit/mistake/${id}/reject`, { remark })
}

/* 敏感词库管理 */
export function getSensitiveWordPage(params) {
  return request.get('/admin/sensitive-word/page', { params })
}

export function addSensitiveWord(word) {
  return request.post('/admin/sensitive-word', { word })
}

export function deleteSensitiveWord(id) {
  return request.delete(`/admin/sensitive-word/${id}`)
}
