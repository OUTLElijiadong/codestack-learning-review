import request from '@/utils/request'

/* 在线代码笔记（学生端 /student/note） */

// 我的笔记分页
export function getNotePage(params) {
  return request.get('/student/note/page', { params })
}

// 笔记详情
export function getNoteDetail(id) {
  return request.get(`/student/note/${id}`)
}

// 新增笔记
export function addNote(data) {
  return request.post('/student/note', data)
}

// 编辑笔记
export function updateNote(id, data) {
  return request.put(`/student/note/${id}`, data)
}

// 删除笔记
export function removeNote(id) {
  return request.delete(`/student/note/${id}`)
}

// 公开/私密切换
export function toggleNoteVisibility(id, isPublic) {
  return request.put(`/student/note/${id}/visibility`, null, { params: { isPublic } })
}

// 公开笔记广场
export function getNoteSquare(params) {
  return request.get('/student/note/square', { params })
}

/* 笔记分类管理 */
export function getCategoryList() {
  return request.get('/student/note/category/list')
}

export function addCategory(data) {
  return request.post('/student/note/category', data)
}

export function renameCategory(id, data) {
  return request.put(`/student/note/category/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/student/note/category/${id}`)
}

/* 笔记备份 */
export function backupNotes() {
  return request.post('/student/note/backup')
}

export function getBackupList() {
  return request.get('/student/note/backup/list')
}
