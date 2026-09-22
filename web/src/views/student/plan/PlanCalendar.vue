<template>
  <div class="calendar-page">
    <!-- 顶部统计条 -->
    <div class="stat-row">
      <div class="mini-stat">
        <span class="mini-value" style="color: var(--primary)">{{ monthChecked }}</span>
        <span class="mini-label">本月打卡天数</span>
      </div>
      <div class="mini-stat">
        <span class="mini-value" style="color: var(--warning)">{{ monthDone }}</span>
        <span class="mini-label">本月完成任务天数</span>
      </div>
      <div class="mini-stat">
        <span class="mini-value" style="color: var(--success)">{{ monthRate }}%</span>
        <span class="mini-label">本月复盘完成率</span>
      </div>
    </div>

    <AppCard>
      <el-calendar v-model="currentDate">
        <template #header="{ date }">
          <div class="cal-header">
            <span class="cal-title">{{ dayjs(currentDate).format('YYYY 年 M 月') }} 复盘日历</span>
            <div class="cal-actions">
              <el-button round size="small" @click="goMonth(-1)">上一月</el-button>
              <el-button round size="small" type="primary" plain @click="goToday">今天</el-button>
              <el-button round size="small" @click="goMonth(1)">下一月</el-button>
            </div>
          </div>
        </template>
        <template #date-cell="{ data }">
          <div class="cal-cell" :class="cellClass(data)" @click="openDay(data)">
            <span class="cal-day">{{ dayjs(data.date).date() }}</span>
            <template v-if="dayInfo(data)">
              <span v-if="dayInfo(data).checked" class="cal-check">✓</span>
              <span v-if="dayInfo(data).targetCount" class="cal-progress">
                {{ dayInfo(data).completedCount }}/{{ dayInfo(data).targetCount }}
              </span>
            </template>
          </div>
        </template>
      </el-calendar>

      <!-- 图例：一眼读懂每种颜色 -->
      <div class="legend">
        <span class="legend-item"><i class="lg lg-done"></i>全部完成</span>
        <span class="legend-item"><i class="lg lg-part"></i>部分完成</span>
        <span class="legend-item"><i class="lg lg-miss"></i>未完成</span>
        <span class="legend-item"><i class="lg lg-future"></i>未来计划</span>
        <span class="legend-item"><i class="lg lg-check"></i>已打卡 ✓</span>
      </div>
    </AppCard>

    <!-- 当日详情弹窗 -->
    <el-dialog v-model="dayDialog" :title="selectedDay + ' 复盘详情'" width="420px">
      <template v-if="selectedInfo">
        <p class="day-line">目标复习：{{ selectedInfo.targetCount }} 道</p>
        <p class="day-line">实际完成：{{ selectedInfo.completedCount }} 道</p>
        <p class="day-line">
          状态：
          <el-tag :type="selectedInfo.status === 1 ? 'success' : 'warning'" size="small" round>
            {{ selectedInfo.status === 1 ? '已完成' : '未完成' }}
          </el-tag>
          <el-tag v-if="selectedInfo.checked" type="primary" size="small" round effect="plain" class="day-tag">已打卡</el-tag>
        </p>
      </template>
      <p v-else class="day-line">这一天没有复盘记录</p>
      <template #footer>
        <el-button type="primary" round @click="$router.push('/student/plan')">去复盘</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 复盘日历：el-calendar 自定义格子渲染"哪天学了哪天没学"
import { ref, computed, watch, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getCalendar } from '@/api/plan'
import AppCard from '@/components/AppCard.vue'

const currentDate = ref(new Date())
const records = ref([])
const dayDialog = ref(false)
const selectedDay = ref('')
const selectedInfo = ref(null)

const recordMap = computed(() => {
  const map = {}
  records.value.forEach(r => { map[r.date] = r })
  return map
})

const monthChecked = computed(() => records.value.filter(r => r.checked).length)
const monthDone = computed(() => records.value.filter(r => r.status === 1).length)
const monthRate = computed(() => {
  const withTask = records.value.filter(r => r.targetCount > 0).length
  return withTask === 0 ? 0 : Math.round(monthDone.value / withTask * 100)
})

function dayInfo(data) {
  return recordMap.value[dayjs(data.date).format('YYYY-MM-DD')] || null
}

/** 单元格五态着色：全部完成绿 / 部分完成黄 / 未完成红 / 未来计划虚线蓝 / 今天描边 */
function cellClass(data) {
  const dateStr = dayjs(data.date).format('YYYY-MM-DD')
  const info = recordMap.value[dateStr]
  const isToday = dateStr === dayjs().format('YYYY-MM-DD')
  const isFuture = dayjs(data.date).isAfter(dayjs(), 'day')
  const cls = []
  if (isToday) cls.push('is-today')
  if (info && info.targetCount > 0) {
    if (isFuture) {
      cls.push('is-future')
    } else if (info.status === 1) {
      cls.push('is-done')
    } else if (info.completedCount > 0) {
      cls.push('is-part')
    } else if (!isToday) {
      cls.push('is-miss')
    }
  } else if (info && info.checked) {
    cls.push('is-checked')
  }
  return cls.join(' ')
}

function openDay(data) {
  selectedDay.value = dayjs(data.date).format('YYYY-MM-DD')
  selectedInfo.value = recordMap.value[selectedDay.value]
  dayDialog.value = true
}

function goMonth(offset) {
  currentDate.value = dayjs(currentDate.value).add(offset, 'month').toDate()
}

function goToday() {
  currentDate.value = new Date()
}

async function load() {
  const month = dayjs(currentDate.value).format('YYYY-MM')
  const res = await getCalendar(month)
  records.value = res.data || []
}

watch(currentDate, load)
onMounted(load)
</script>

<style scoped>
.calendar-page { max-width: 1080px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.mini-stat {
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 18px 22px;
  display: flex; flex-direction: column; gap: 4px;
}
.mini-value { font-size: 26px; font-weight: 700; font-variant-numeric: tabular-nums; }
.mini-label { font-size: 12px; color: var(--text-sub); }

.cal-header { display: flex; justify-content: space-between; align-items: center; width: 100%; padding: 4px 8px 14px; }
.cal-title { font-size: 17px; font-weight: 700; color: var(--text-title); }

/* 日历格子定制 */
:deep(.el-calendar__body) { padding: 0; }
:deep(.el-calendar-table .el-calendar-day) {
  height: 88px;
  padding: 6px;
}
.cal-cell {
  height: 100%;
  border-radius: var(--radius-btn);
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  transition: all var(--dur-fast);
  border: 1px solid transparent;
}
.cal-cell:hover { border-color: var(--primary-border); }
.cal-day { font-size: 14px; font-weight: 600; color: var(--text-main); }
.cal-check { color: var(--success); font-weight: 700; font-size: 13px; }
.cal-progress { font-size: 11px; color: var(--text-sub); font-variant-numeric: tabular-nums; }

.cal-cell.is-done { background: #D1FAE5; }
.cal-cell.is-done .cal-day, .cal-cell.is-done .cal-progress { color: #065F46; }
.cal-cell.is-part { background: #FEF3C7; }
.cal-cell.is-part .cal-day, .cal-cell.is-part .cal-progress { color: #92400E; }
.cal-cell.is-miss { background: #FEF2F2; }
.cal-cell.is-miss .cal-day, .cal-cell.is-miss .cal-progress { color: #B91C1C; }
.cal-cell.is-future { background: #EEF2FF; border: 1px dashed #C7D2FE; }
.cal-cell.is-future .cal-day, .cal-cell.is-future .cal-progress { color: #4338CA; }
.cal-cell.is-checked { background: var(--gray-50); }
.cal-cell.is-today { border: 2px solid var(--primary); }
.cal-cell.is-today .cal-day { color: var(--primary); }

.legend {
  display: flex; gap: 18px; flex-wrap: wrap;
  margin-top: 16px; padding-top: 14px;
  border-top: 1px solid var(--gray-100);
  font-size: 12px; color: var(--text-sub);
}
.legend-item { display: inline-flex; align-items: center; gap: 6px; }
.lg { width: 12px; height: 12px; border-radius: 4px; display: inline-block; }
.lg-done { background: #D1FAE5; }
.lg-part { background: #FEF3C7; }
.lg-miss { background: #FEF2F2; }
.lg-future { background: #EEF2FF; border: 1px dashed #C7D2FE; }
.lg-check { background: var(--gray-100); }

.day-line { font-size: 14px; color: var(--text-main); margin: 6px 0; }
.day-tag { margin-left: 8px; }
</style>
