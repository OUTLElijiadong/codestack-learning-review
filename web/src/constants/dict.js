/**
 * 全站字典常量：与后端取值字典一一对应，改动必须前后端同步
 * （后端实体注释、数据库列注释与本文件是唯一权威口径）
 */

/* 错误类型：错题核心语义，四处复用（列表徽标/筛选/详情/图表） */
export const ERROR_TYPES = [
  { value: 'grammar', label: '语法错误', color: '#F97316', textColor: '#C2410C', bg: '#FFF7ED', border: '#FFEDD5' },
  { value: 'logic', label: '逻辑错误', color: '#8B5CF6', textColor: '#6D28D9', bg: '#F5F3FF', border: '#EDE9FE' },
  { value: 'api', label: '接口报错', color: '#0EA5E9', textColor: '#0369A1', bg: '#F0F9FF', border: '#E0F2FE' },
  { value: 'env', label: '环境问题', color: '#14B8A6', textColor: '#0F766E', bg: '#F0FDFA', border: '#CCFBF1' }
]

export function errorTypeLabel(value) {
  const hit = ERROR_TYPES.find(t => t.value === value)
  return hit ? hit.label : value || '未分类'
}

export function errorTypeMeta(value) {
  return ERROR_TYPES.find(t => t.value === value) || { value, label: value || '未分类', color: '#64748B', textColor: '#475569', bg: '#F1F5F9', border: '#E2E8F0' }
}

/* 技术方向：错题/问答共用筛选 */
export const TECH_DIRECTIONS = ['Vue', 'SpringBoot', 'MySQL', '算法', '其他']

/* 学习方向：个人档案设置 */
export const LEARN_DIRECTIONS = [
  { value: 'frontend', label: '前端', color: '#0EA5E9' },
  { value: 'backend', label: '后端', color: '#10B981' },
  { value: 'test', label: '测试', color: '#F59E0B' }
]

export function directionLabel(value) {
  const hit = LEARN_DIRECTIONS.find(d => d.value === value)
  return hit ? hit.label : '未设置'
}

export function directionColor(value) {
  const hit = LEARN_DIRECTIONS.find(d => d.value === value)
  return hit ? hit.color : '#94A3B8'
}

/* 审核状态：错题/笔记共用 */
export const AUDIT_STATUS = [
  { value: 0, label: '待审核', type: 'warning' },
  { value: 1, label: '正常', type: 'success' },
  { value: 2, label: '已下架', type: 'info' }
]

export function auditStatusLabel(value) {
  const hit = AUDIT_STATUS.find(s => s.value === value)
  return hit ? hit.label : '正常'
}

/* 公告类型 */
export const NOTICE_TYPES = [
  { value: 'notice', label: '学习通知', type: 'primary' },
  { value: 'maintenance', label: '维护通知', type: 'warning' }
]

export function noticeTypeLabel(value) {
  const hit = NOTICE_TYPES.find(t => t.value === value)
  return hit ? hit.label : value
}

/* 角色 */
export const ROLES = [
  { value: 'student', label: '学生', type: 'primary' },
  { value: 'teacher', label: '教师', type: 'success' },
  { value: 'admin', label: '超级管理员', type: 'danger' }
]

export function roleLabel(value) {
  const hit = ROLES.find(r => r.value === value)
  return hit ? hit.label : value
}

/* 标签 chips 八色循环：用户自建标签按 id % 8 取色，保证任何标签都有好看配色 */
export const TAG_COLORS = [
  { text: '#6366F1', bg: '#EEF2FF' },
  { text: '#0EA5E9', bg: '#F0F9FF' },
  { text: '#10B981', bg: '#ECFDF5' },
  { text: '#F59E0B', bg: '#FFFBEB' },
  { text: '#EF4444', bg: '#FEF2F2' },
  { text: '#8B5CF6', bg: '#F5F3FF' },
  { text: '#EC4899', bg: '#FDF2F8' },
  { text: '#14B8A6', bg: '#F0FDFA' }
]

export function tagColor(id) {
  const idx = Math.abs(Number(id) || 0) % TAG_COLORS.length
  return TAG_COLORS[idx]
}
