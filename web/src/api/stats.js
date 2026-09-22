import request from '@/utils/request'

/* 学习数据可视化统计（学生端 /student/stats + /student/overview） */

// 个人学习数据总览卡片
export function getOverview() {
  return request.get('/student/overview')
}

// 每周错题增长柱状图（近 8 周）
export function getWeeklyMistake() {
  return request.get('/student/stats/weekly')
}

// 各技术方向错题占比饼图
export function getTechPie() {
  return request.get('/student/stats/tech-pie')
}

// 每日学习时长折线图（近 14 天）
export function getDailyDuration() {
  return request.get('/student/stats/daily-duration')
}

// 学习完成率统计卡片（近 30 天）
export function getCompletion() {
  return request.get('/student/stats/completion')
}
