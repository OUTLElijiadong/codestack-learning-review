import dayjs from 'dayjs'

/** 格式化为 yyyy-MM-dd HH:mm */
export function fmtTime(val) {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD HH:mm')
}

/** 格式化为 yyyy-MM-dd */
export function fmtDate(val) {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD')
}

/** 相对时间：x分钟前 / x小时前 / x天前 / 超过30天显示日期 */
export function fromNow(val) {
  if (!val) return '-'
  const target = dayjs(val)
  const diffMin = dayjs().diff(target, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`
  const diffHour = dayjs().diff(target, 'hour')
  if (diffHour < 24) return `${diffHour} 小时前`
  const diffDay = dayjs().diff(target, 'day')
  if (diffDay < 30) return `${diffDay} 天前`
  return target.format('YYYY-MM-DD')
}

/** 分钟数转"x小时x分" */
export function fmtMinutes(min) {
  if (!min) return '0 分钟'
  const m = Number(min)
  if (m < 60) return `${m} 分钟`
  const h = Math.floor(m / 60)
  const rest = m % 60
  return rest === 0 ? `${h} 小时` : `${h} 小时 ${rest} 分`
}
