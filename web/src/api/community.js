import request from '@/utils/request'

/* 社区互助问答（学生端 /student/question） */

// 问题分页（my=1 只看自己提问）
export function getQuestionPage(params) {
  return request.get('/student/question/page', { params })
}

// 问题详情（含二级评论树）
export function getQuestionDetail(id) {
  return request.get(`/student/question/${id}`)
}

// 发布问题
export function publishQuestion(data) {
  return request.post('/student/question', data)
}

// 删除自己的提问
export function removeQuestion(id) {
  return request.delete(`/student/question/${id}`)
}

// 标记已解决
export function markSolved(id) {
  return request.put(`/student/question/${id}/solved`)
}

// 评论回复解答（parentId 支持二级回复，0/不传为一级评论）
export function replyQuestion(data) {
  return request.post('/student/question/comment', data)
}

// 删除自己的评论
export function removeComment(id) {
  return request.delete(`/student/question/comment/${id}`)
}

// 只看自己回复（含所属问题标题）
export function getMyReplies(params) {
  return request.get('/student/question/comment/my', { params })
}
