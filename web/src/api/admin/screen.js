import request from '@/utils/request'

/* 管理端：全局学习数据大屏（/admin/screen） */

// 大屏顶部总量卡片
export function getScreenOverview() {
  return request.get('/admin/screen/overview')
}

// 各专业学习活跃度
export function getMajorActive() {
  return request.get('/admin/screen/major-active')
}

// 系统使用趋势（近 30 天：新增错题/新增笔记/打卡人次）
export function getUsageTrend() {
  return request.get('/admin/screen/trend')
}

// 全校错题技术方向分布饼图
export function getTechDist() {
  return request.get('/admin/screen/tech-dist')
}

/** 大屏专业柱下钻：该专业学生名册（错题数/近7天打卡/活跃标记） */
export function getMajorStudents(major) {
  return request.get('/admin/screen/major-students', { params: { major } })
}
