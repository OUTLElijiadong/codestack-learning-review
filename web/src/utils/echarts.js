import * as echarts from 'echarts'

/* 全局图表色板：与品牌色同族，图表、chips、图例统一取色 */
export const CHART_COLORS = [
  '#6366F1', '#8B5CF6', '#0EA5E9', '#10B981',
  '#F59E0B', '#EC4899', '#14B8A6', '#F97316'
]
export const CHART_COLORS_DARK = [
  '#818CF8', '#A78BFA', '#38BDF8', '#34D399',
  '#FBBF24', '#F472B6', '#2DD4BF', '#FB923C'
]

const FONT = '"PingFang SC", "Microsoft YaHei", system-ui, sans-serif'

/* 浅色主题：学生端全部图表 */
echarts.registerTheme('learn', {
  color: CHART_COLORS,
  textStyle: { fontFamily: FONT },
  legend: {
    textStyle: { color: '#475569', fontSize: 12 },
    itemWidth: 12,
    itemHeight: 12,
    icon: 'roundRect'
  },
  tooltip: {
    backgroundColor: '#FFFFFF',
    borderColor: '#E2E8F0',
    borderWidth: 1,
    padding: [10, 14],
    textStyle: { color: '#334155', fontSize: 12 },
    extraCssText: 'box-shadow: 0 8px 24px rgba(15,23,42,0.10); border-radius: 10px;'
  },
  categoryAxis: {
    axisLine: { lineStyle: { color: '#CBD5E1' } },
    axisTick: { show: false },
    axisLabel: { color: '#64748B', fontSize: 12 },
    splitLine: { show: false }
  },
  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#94A3B8', fontSize: 12 },
    splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
  },
  logAxis: {
    axisLabel: { color: '#94A3B8', fontSize: 12 },
    splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
  },
  timeAxis: {
    axisLine: { lineStyle: { color: '#CBD5E1' } },
    axisLabel: { color: '#64748B', fontSize: 12 },
    splitLine: { show: false }
  },
  bar: { itemStyle: { borderRadius: [6, 6, 0, 0] }, barMaxWidth: 28 },
  line: { smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 3 } },
  pie: {
    itemStyle: { borderColor: '#FFFFFF', borderWidth: 2, borderRadius: 6 },
    label: { color: '#475569', fontSize: 12 }
  }
})

/* 深色主题：仅数据大屏使用 */
echarts.registerTheme('learn-dark', {
  color: CHART_COLORS_DARK,
  textStyle: { fontFamily: FONT },
  legend: {
    textStyle: { color: '#94A3B8', fontSize: 12 },
    itemWidth: 12,
    itemHeight: 12,
    icon: 'roundRect'
  },
  tooltip: {
    backgroundColor: '#111C3B',
    borderColor: 'rgba(129, 140, 248, 0.4)',
    borderWidth: 1,
    padding: [10, 14],
    textStyle: { color: '#E2E8F0', fontSize: 12 },
    extraCssText: 'box-shadow: 0 0 20px rgba(99,102,241,0.35); border-radius: 10px;'
  },
  categoryAxis: {
    axisLine: { lineStyle: { color: 'rgba(148,163,184,0.35)' } },
    axisTick: { show: false },
    axisLabel: { color: '#8CA0C2', fontSize: 12 },
    splitLine: { show: false }
  },
  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#8CA0C2', fontSize: 12 },
    splitLine: { lineStyle: { color: 'rgba(148,163,184,0.14)', type: 'dashed' } }
  },
  logAxis: {
    axisLabel: { color: '#8CA0C2', fontSize: 12 },
    splitLine: { lineStyle: { color: 'rgba(148,163,184,0.14)', type: 'dashed' } }
  },
  timeAxis: {
    axisLine: { lineStyle: { color: 'rgba(148,163,184,0.35)' } },
    axisLabel: { color: '#8CA0C2', fontSize: 12 },
    splitLine: { show: false }
  },
  bar: { itemStyle: { borderRadius: [6, 6, 0, 0] }, barMaxWidth: 28 },
  line: { smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 3 } },
  pie: {
    itemStyle: { borderColor: 'rgba(10,15,31,0.9)', borderWidth: 2, borderRadius: 6 },
    label: { color: '#94A3B8', fontSize: 12 }
  }
})

export default echarts
