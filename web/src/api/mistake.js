import request from '@/utils/request'

/* 错题本（学生端 /student/mistake） */

// 错题分页列表
export function getMistakePage(params) {
  return request.get('/student/mistake/page', { params })
}

// 错题智能检索（关键词全文 + 方向 + 类型 + 标签 + 最近N天）
export function searchMistake(params) {
  return request.get('/student/mistake/search', { params })
}

// 错题详情
export function getMistakeDetail(id) {
  return request.get(`/student/mistake/${id}`)
}

// 新增错题
export function addMistake(data) {
  return request.post('/student/mistake', data)
}

// 一键重新编辑
export function updateMistake(id, data) {
  return request.put(`/student/mistake/${id}`, data)
}

// 一键删除归档（逻辑删除进回收站）
export function archiveMistake(id) {
  return request.delete(`/student/mistake/${id}`)
}

// 置顶/取消置顶
export function toggleTop(id) {
  return request.put(`/student/mistake/${id}/top`)
}

// 收藏/取消收藏
export function toggleFavorite(id) {
  return request.put(`/student/mistake/${id}/favorite`)
}

// 归档（回收站）分页
export function getArchivePage(params) {
  return request.get('/student/mistake/archive', { params })
}

// 从归档还原
export function restoreMistake(id) {
  return request.put(`/student/mistake/${id}/restore`)
}

// 彻底删除（物理删除）
export function deleteMistakeForever(id) {
  return request.delete(`/student/mistake/${id}/forever`)
}

/* 错题标签管理 */
export function getTagList() {
  return request.get('/student/mistake/tag/list')
}

export function addTag(data) {
  return request.post('/student/mistake/tag', data)
}

export function renameTag(id, data) {
  return request.put(`/student/mistake/tag/${id}`, data)
}

export function deleteTag(id) {
  return request.delete(`/student/mistake/tag/${id}`)
}
