<template>
  <div class="screen-page" :class="{ 'is-fullscreen': isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="screen-clock">{{ clock }}</span>
        <span class="screen-week">{{ weekText }}</span>
      </div>
      <div class="screen-title-wrap">
        <h1 class="screen-title-text">全局学习数据总后台</h1>
        <div class="screen-title-line"></div>
      </div>
      <div class="header-right">
        <span class="screen-updated">数据更新于 {{ lastUpdated }}</span>
        <button class="screen-ctrl" :class="{ on: autoRefresh }" :title="autoRefresh ? '自动刷新已开启（30 秒）' : '开启 30 秒自动刷新'" @click="autoRefresh = !autoRefresh">
          <el-icon><Refresh /></el-icon>
        </button>
        <button class="screen-ctrl" :title="isFullscreen ? '退出全屏' : '进入全屏'" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
        </button>
        <button class="screen-btn" @click="$router.push('/admin/user')">退出大屏</button>
      </div>
    </div>

    <!-- 总量统计卡 -->
    <div class="screen-stats">
      <ScreenPanel v-for="(card, idx) in statCards" :key="card.label" class="rise-in" :style="{ '--d': idx * 60 + 'ms' }">
        <div class="screen-stat-card" :class="{ clickable: !!card.to }" @click="card.to && $router.push(card.to)">
          <div class="screen-stat-icon" :style="{ color: card.color, background: card.color + '1f', boxShadow: '0 0 14px ' + card.color + '33' }">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="screen-stat-value screen-num"><CountUp :end="card.value" /></div>
          <div class="screen-stat-label">{{ card.label }}</div>
        </div>
      </ScreenPanel>
    </div>

    <!-- 三列图表 -->
    <div class="screen-grid">
      <ScreenPanel title="各专业学习活跃度（近7天登录）" class="rise-in" :style="{ '--d': 420 + 'ms' }">
        <Charts :option="majorOption" theme="learn" height="300px" @chart-click="onMajorClick" />
        <div class="panel-hint">点击柱体查看专业学生明细</div>
      </ScreenPanel>
      <ScreenPanel class="rise-in" :style="{ '--d': 480 + 'ms' }">
        <div class="trend-head">
          <div class="screen-panel-title-inline">系统使用趋势</div>
          <div class="range-chips">
            <span v-for="r in [7, 14, 30]" :key="r" :class="{ active: trendRange === r }" @click="trendRange = r">近{{ r }}天</span>
          </div>
        </div>
        <Charts :option="trendOption" theme="learn" height="292px" />
      </ScreenPanel>
      <ScreenPanel title="全校错题技术方向分布" class="rise-in" :style="{ '--d': 540 + 'ms' }">
        <Charts :option="techOption" theme="learn" height="300px" @chart-click="onTechClick" />
        <div class="panel-hint">点击扇区查看该方向错题</div>
      </ScreenPanel>
    </div>

    <!-- 右侧下钻明细面板 -->
    <transition name="slide-right">
      <div v-if="detail" class="screen-detail">
        <div class="screen-detail-head">
          <b>{{ detail.title }}</b>
          <button class="screen-ctrl" @click="detail = null"><el-icon><Close /></el-icon></button>
        </div>
        <div class="screen-detail-sub">{{ detail.sub }}</div>

        <template v-if="detail.type === 'major'">
          <div v-if="detail.loading" class="detail-loading">加载中…</div>
          <div v-else-if="!detail.rows.length" class="detail-loading">该专业暂无学生</div>
          <div v-else class="detail-table detail-table-roster">
            <div class="dt-row dt-head"><span>学生</span><span>班级</span><span>错题</span><span>近7天打卡</span></div>
            <div v-for="(row, i) in detail.rows" :key="i" class="dt-row">
              <span>{{ row.nickname }}<i v-if="row.active" class="active-dot" title="近7天有登录"></i></span>
              <span class="dim">{{ row.className }}</span>
              <span>{{ row.mistakes }}</span>
              <span :class="row.checkinDays7 > 0 ? 'ok' : 'dim'">{{ row.checkinDays7 > 0 ? row.checkinDays7 + ' 天' : '未打卡' }}</span>
            </div>
          </div>
        </template>
        <template v-else>
          <div v-if="detail.loading" class="detail-loading">加载中…</div>
          <div v-else-if="!detail.rows.length" class="detail-loading">该方向暂无错题记录</div>
          <div v-else class="detail-list">
            <div v-for="(row, i) in detail.rows" :key="i" class="detail-item">
              <div class="detail-item-title">{{ row.title }}</div>
              <div class="detail-item-meta">
                <span>{{ row.author }}</span><span>{{ row.time }}</span>
                <span v-if="row.reviewed != null">已复习 {{ row.reviewed }} 次</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup>
// 全局学习数据大屏：全部数据来自数据库实时统计（首次启动由 DemoDataInitializer 播种校规模演示数据，
// 之后随真实操作持续变化）；图表点击下钻为真实接口查询。
import '@/assets/styles/screen.css'
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { getScreenOverview, getMajorActive, getUsageTrend, getTechDist, getMajorStudents } from '@/api/admin/screen'
import { getMistakeAuditPage } from '@/api/admin/audit'
import ScreenPanel from '@/components/ScreenPanel.vue'
import Charts from '@/components/Charts.vue'
import CountUp from '@/components/CountUp.vue'
import { Refresh, FullScreen, Close, Files, Plus, User, Notebook, ChatDotRound, AlarmClock, Lock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

const overview = ref({})
const majorActive = ref([])
const trend = ref({ mistake: [], note: [], checkin: [] })
const techDist = ref([])

const trendRange = ref(30)
const autoRefresh = ref(true)
const isFullscreen = ref(false)
const lastUpdated = ref('')
const detail = ref(null)

const clock = ref('')
let timer = null
let refreshTimer = null

const weekText = computed(() => dayjs().format('dddd'))

const statCards = computed(() => [
  { label: '全校错题总量', value: overview.value.mistakeTotal || 0, icon: 'Files', color: '#818CF8', to: '/admin/audit' },
  { label: '今日新增错题', value: overview.value.mistakeToday || 0, icon: 'Plus', color: '#F472B6' },
  { label: '注册学生数', value: overview.value.studentTotal || 0, icon: 'User', color: '#22D3EE', to: '/admin/user' },
  { label: '笔记总量', value: overview.value.noteTotal || 0, icon: 'Notebook', color: '#34D399', to: '/admin/audit' },
  { label: '问答总数', value: overview.value.questionTotal || 0, icon: 'ChatDotRound', color: '#FBBF24' },
  { label: '今日打卡人数', value: overview.value.checkinToday || 0, icon: 'AlarmClock', color: '#A78BFA' },
  { label: '冻结账号数', value: overview.value.frozenTotal || 0, icon: 'Lock', color: '#F87171' }
])

/* 各专业活跃度：横向条形图（总人数 vs 近7天登录人数） */
const majorOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0 },
  grid: { left: 8, right: 30, top: 20, bottom: 30, containLabel: true },
  xAxis: { type: 'value', minInterval: 1 },
  yAxis: { type: 'category', data: majorActive.value.map(i => i.major) },
  series: [
    { name: '总人数', type: 'bar', data: majorActive.value.map(i => i.totalUsers), barMaxWidth: 14, cursor: 'pointer' },
    { name: '近7天活跃', type: 'bar', data: majorActive.value.map(i => i.activeUsers), barMaxWidth: 14, cursor: 'pointer' }
  ]
}))

/* 系统使用趋势：按所选天数切片 */
const trendOption = computed(() => {
  const days = []
  const mMap = {}, nMap = {}, cMap = {}
  ;(trend.value.mistake || []).forEach(i => { mMap[String(i.dt).slice(0, 10)] = Number(i.cnt) })
  ;(trend.value.note || []).forEach(i => { nMap[String(i.dt).slice(0, 10)] = Number(i.cnt) })
  ;(trend.value.checkin || []).forEach(i => { cMap[String(i.dt).slice(0, 10)] = Number(i.cnt) })
  for (let i = trendRange.value - 1; i >= 0; i--) {
    const d = dayjs().subtract(i, 'day')
    days.push({ label: d.format('MM-DD'), key: d.format('YYYY-MM-DD') })
  }
  const seriesOf = (name, map) => ({
    name, type: 'line', smooth: true, symbol: 'circle', symbolSize: 4,
    data: days.map(d => map[d.key] || 0),
    areaStyle: { opacity: 0.12 }
  })
  return {
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 8, right: 16, top: 20, bottom: 30, containLabel: true },
    xAxis: { type: 'category', data: days.map(d => d.label) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [seriesOf('新增错题', mMap), seriesOf('新增笔记', nMap), seriesOf('打卡人次', cMap)]
  }
})

const techOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 道（{d}%）' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['42%', '66%'],
    center: ['50%', '46%'],
    data: techDist.value,
    cursor: 'pointer',
    label: { show: true, formatter: '{b}', color: '#94A3B8', fontSize: 12 },
    emphasis: { scaleSize: 6 }
  }]
}))

async function loadAll() {
  try {
    const [ov, ma, tr, td] = await Promise.all([getScreenOverview(), getMajorActive(), getUsageTrend(), getTechDist()])
    overview.value = ov.data || {}
    majorActive.value = ma.data || []
    trend.value = tr.data || { mistake: [], note: [], checkin: [] }
    techDist.value = td.data || []
    lastUpdated.value = dayjs().format('HH:mm:ss')
  } catch (e) { /* 拦截器已提示 */ }
}

/* 图表下钻（均为真实接口查询） */
async function onMajorClick(params) {
  const major = params.name
  const hit = majorActive.value.find(i => i.major === major)
  if (!hit) return
  detail.value = { type: 'major', title: major + ' · 学生明细', sub: `总人数 ${hit.totalUsers}，近 7 天活跃 ${hit.activeUsers}`, rows: [], loading: true }
  try {
    const res = await getMajorStudents(major)
    detail.value.rows = res.data || []
    detail.value.loading = false
  } catch (e) {
    detail.value.loading = false
  }
}

async function onTechClick(params) {
  const tech = params.name
  detail.value = { type: 'tech', title: tech + ' · 错题明细', sub: `共 ${params.value} 道（最新 20 条）`, rows: [], loading: true }
  try {
    const res = await getMistakeAuditPage({ pageNum: 1, pageSize: 20, techDirection: tech })
    const list = (res.data && res.data.list) || []
    detail.value.rows = list.map(m => ({
      title: m.title, author: m.username || '—', time: String(m.createTime || '').slice(0, 10), reviewed: m.reviewCount
    }))
    detail.value.loading = false
  } catch (e) {
    detail.value.loading = false
  }
}

/* 全屏 */
function toggleFullscreen() {
  if (document.fullscreenElement) document.exitFullscreen()
  else document.documentElement.requestFullscreen()
}
function onFsChange() { isFullscreen.value = !!document.fullscreenElement }

watch(autoRefresh, (on) => {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (on) refreshTimer = setInterval(loadAll, 30000)
})

onMounted(() => {
  loadAll()
  timer = setInterval(() => { clock.value = dayjs().format('YYYY-MM-DD HH:mm:ss') }, 1000)
  clock.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  if (autoRefresh.value) refreshTimer = setInterval(loadAll, 30000)
  document.addEventListener('fullscreenchange', onFsChange)
})
onBeforeUnmount(() => {
  clearInterval(timer)
  if (refreshTimer) clearInterval(refreshTimer)
  document.removeEventListener('fullscreenchange', onFsChange)
})
</script>

<style scoped>
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px 6px;
  gap: 12px;
}
.header-left { min-width: 240px; display: flex; align-items: baseline; gap: 10px; }
.screen-clock {
  font-family: var(--font-mono);
  font-size: 14px;
  color: #0369A1;
}
.screen-week { font-size: 13px; color: #94A3B8; }
.header-right { display: flex; align-items: center; gap: 10px; min-width: 240px; justify-content: flex-end; }
.screen-updated { font-size: 12px; color: #94A3B8; font-family: var(--font-mono); }
.screen-title-wrap { text-align: center; }
.screen-title-wrap h1 {
  font-size: 26px; font-weight: 700; margin: 0; letter-spacing: 2px;
  background: linear-gradient(92deg, #0369A1 0%, #4F46E5 45%, #7C3AED 100%);
  -webkit-background-clip: text; background-clip: text; color: transparent;
  text-shadow: none;
}

.screen-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 16px;
  padding: 16px 24px 0;
}
.screen-stat-card { display: flex; flex-direction: column; gap: 4px; }
.screen-stat-card.clickable { cursor: pointer; }
.screen-stat-icon {
  width: 34px; height: 34px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; margin-bottom: 4px;
}
.screen-grid {
  display: grid;
  grid-template-columns: 1fr 1.4fr 1fr;
  gap: 16px;
  padding: 16px 24px 24px;
}
@media (max-width: 1280px) {
  .screen-grid { grid-template-columns: 1fr; }
}

/* 趋势面板头（标题 + 范围切换） */
.trend-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.screen-panel-title-inline {
  font-size: 15px; font-weight: 600; color: #334155; letter-spacing: 1px;
  display: flex; align-items: center; gap: 8px;
}
.screen-panel-title-inline::before {
  content: ''; width: 4px; height: 14px; border-radius: 2px;
  background: linear-gradient(180deg, #22D3EE, #6366F1);
  box-shadow: 0 0 8px rgba(34, 211, 238, 0.8);
}
.range-chips { display: flex; gap: 6px; }
.range-chips span {
  font-size: 12px; color: #64748B; padding: 3px 10px; border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.25); cursor: pointer; user-select: none;
  transition: all 0.15s;
}
.range-chips span:hover { color: #C7D2FE; border-color: rgba(129, 140, 248, 0.5); }
.range-chips span.active {
  color: #FFFFFF; background: linear-gradient(92deg, #6366F1, #818CF8); border-color: transparent;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.35);
}
.panel-hint {
  margin-top: 6px; font-size: 12px; color: #94A3B8; text-align: center; letter-spacing: 1px;
}
.active-dot {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%;
  background: #10B981; margin-left: 6px; vertical-align: middle;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
}
</style>
