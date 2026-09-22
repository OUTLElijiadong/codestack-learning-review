import request from '@/utils/request'

/* 每日复盘计划（学生端 /student/review） */

// 我的复盘计划设置
export function getPlan() {
  return request.get('/student/review/plan')
}

// 保存/修改计划（每天复习几道错题）
export function savePlan(data) {
  return request.put('/student/review/plan', data)
}

// 今日任务（含推荐复习错题；也是未完成提醒的数据源）
export function getTodayPlan() {
  return request.get('/student/review/today')
}

// 完成一道错题复习（达标自动打勾并联动打卡）
export function finishReview(mistakeId) {
  return request.post(`/student/review/finish/${mistakeId}`)
}

// 每日打卡签到
export function checkIn() {
  return request.post('/student/review/sign')
}

// 复盘日历（按月查询，month 格式 yyyy-MM）
export function getCalendar(month) {
  return request.get('/student/review/calendar', { params: { month } })
}

// 复盘历史记录分页
export function getReviewRecords(params) {
  return request.get('/student/review/records', { params })
}

// 上报学习时长（每日时长折线图写入入口）
export function reportStudyTime(data) {
  return request.post('/student/review/study-time', data)
}
