<template>
  <div class="stats-page" v-loading="loading">
    <!-- 完成率统计卡片组 -->
    <div class="rate-row">
      <AppCard class="rate-card" pad="20px">
        <div class="rate-main">
          <el-progress
            type="dashboard"
            :percentage="completion.rate || 0"
            :stroke-width="10"
            :color="rateColor"
          >
            <template #default>
              <div class="rate-center">
                <span class="rate-num">{{ completion.rate || 0 }}%</span>
                <span class="rate-text">近30天完成率</span>
              </div>
            </template>
          </el-progress>
        </div>
        <div class="rate-side">
          <div class="rate-item"><span class="rate-item-num">{{ completion.doneDays || 0 }}</span><span class="rate-item-label">完成天数</span></div>
          <div class="rate-item"><span class="rate-item-num">{{ completion.totalDays || 0 }}</span><span class="rate-item-label">有任务天数</span></div>
          <div class="rate-item"><span class="rate-item-num">{{ completion.checkInDays || 0 }}</span><span class="rate-item-label">连续打卡</span></div>
          <div class="rate-item"><span class="rate-item-num">{{ completion.totalReviewed || 0 }}</span><span class="rate-item-label">累计复习题数</span></div>
        </div>
      </AppCard>
    </div>

    <!-- 三图 -->
    <AppCard title="每周错题增长（近 8 周）">
      <Charts :option="weeklyOption" height="clamp(280px, 32vh, 340px)" />
    </AppCard>

    <div class="chart-two">
      <AppCard title="各技术方向错题占比">
        <Charts :option="pieOption" height="300px" />
      </AppCard>
      <AppCard title="每日学习时长（近 14 天）">
        <Charts :option="durationOption" height="300px" />
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWeeklyMistake, getTechPie, getDailyDuration, getCompletion } from '@/api/stats'
import AppCard from '@/components/AppCard.vue'
import Charts from '@/components/Charts.vue'
import dayjs from 'dayjs'

const loading = ref(true)
const weekly = ref([])
const pie = ref([])
const duration = ref([])
const completion = ref({})

const rateColor = computed(() => {
  const r = completion.value.rate || 0
  if (r >= 80) return '#10B981'
  if (r >= 50) return '#6366F1'
  return '#F59E0B'
})

const weeklyOption = computed(() => ({
  grid: { left: 16, right: 16, top: 40, bottom: 8, containLabel: true },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: weekly.value.map(i => '第' + String(i.week).split('-')[1] + '周') },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '新增错题',
    type: 'bar',
    data: weekly.value.map(i => i.count),
    itemStyle: {
      borderRadius: [6, 6, 0, 0],
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: '#818CF8' }, { offset: 1, color: '#6366F1' }]
      }
    },
    barMaxWidth: 32
  }]
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 道（{d}%）' },
  legend: { bottom: 0 },
  series: [{
    name: '技术方向',
    type: 'pie',
    radius: ['45%', '68%'],
    center: ['50%', '46%'],
    data: pie.value,
    label: { show: true, formatter: '{b} {c}道' }
  }]
}))

/* 近 14 天连续日期轴，无记录的日子补 0 */
const durationOption = computed(() => {
  const days = []
  const map = {}
  duration.value.forEach(i => { map[String(i.date).slice(0, 10)] = Number(i.minutes) })
  for (let i = 13; i >= 0; i--) {
    const d = dayjs().subtract(i, 'day')
    days.push({ label: d.format('MM-DD'), minutes: map[d.format('YYYY-MM-DD')] || 0 })
  }
  return {
    grid: { left: 16, right: 16, top: 40, bottom: 8, containLabel: true },
    tooltip: { trigger: 'axis', formatter: '{b}<br/>学习时长：{c} 分钟' },
    xAxis: { type: 'category', data: days.map(d => d.label) },
    yAxis: { type: 'value', name: '分钟' },
    series: [{
      name: '学习时长',
      type: 'line',
      smooth: true,
      data: days.map(d => d.minutes),
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(99,102,241,0.28)' }, { offset: 1, color: 'rgba(99,102,241,0.02)' }]
        }
      },
      lineStyle: { width: 3 },
      symbol: 'circle',
      symbolSize: 6
    }]
  }
})

onMounted(async () => {
  loading.value = true
  try {
    const [w, p, d, c] = await Promise.all([getWeeklyMistake(), getTechPie(), getDailyDuration(), getCompletion()])
    weekly.value = w.data || []
    pie.value = p.data || []
    duration.value = d.data || []
    completion.value = c.data || {}
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stats-page { display: flex; flex-direction: column; gap: 20px; }
.rate-card :deep(.app-card) { display: block; }
.rate-main { display: flex; justify-content: center; }
.rate-row :deep(.app-card) { display: flex; align-items: center; gap: 40px; justify-content: center; }
.rate-center { display: flex; flex-direction: column; align-items: center; }
.rate-num { font-size: 26px; font-weight: 700; color: var(--text-title); font-variant-numeric: tabular-nums; }
.rate-text { font-size: 12px; color: var(--text-sub); margin-top: 4px; }
.rate-side { display: grid; grid-template-columns: repeat(2, 140px); gap: 18px 32px; }
.rate-item { display: flex; flex-direction: column; align-items: center; gap: 4px; background: var(--gray-50); border-radius: var(--radius-md); padding: 14px 0; }
.rate-item-num { font-size: 22px; font-weight: 700; color: var(--text-title); font-variant-numeric: tabular-nums; }
.rate-item-label { font-size: 12px; color: var(--text-sub); }
.chart-two { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 1200px) { .chart-two { grid-template-columns: 1fr; } }
</style>
