<template>
  <div class="dashboard">
    <!-- 首屏骨架：接口等待不许白屏或干转圈 -->
    <div v-if="loading" class="dash-skeleton">
      <el-skeleton animated class="skel-block">
        <template #template><el-skeleton-item variant="rect" style="height: 132px; border-radius: 18px" /></template>
      </el-skeleton>
      <div class="skel-grid">
        <el-skeleton v-for="i in 4" :key="i" animated>
          <template #template><el-skeleton-item variant="rect" style="height: 96px; border-radius: 14px" /></template>
        </el-skeleton>
      </div>
      <el-skeleton animated>
        <template #template><el-skeleton-item variant="rect" style="height: 320px; border-radius: 14px" /></template>
      </el-skeleton>
    </div>

    <template v-else>
    <!-- 带 1 · 欢迎横幅（渐变深底大卡 + 今日进度 + 快捷操作） -->
    <div class="banner rise-in">
      <div class="banner-deco deco-a"></div>
      <div class="banner-deco deco-b"></div>
      <div class="banner-left">
        <h2 class="banner-title">{{ greeting }}，{{ userStore.userInfo?.nickname || '同学' }}</h2>
        <p class="banner-sub">
          今天已复盘 <b>{{ overview.todayFinished || 0 }}</b> /
          <el-tooltip
            content="当日目标取自当天首次进入系统时的复盘计划快照；当天修改计划，自次日起按新目标执行"
            placement="top"
          ><b class="target-hit">{{ overview.todayTarget || 0 }}</b></el-tooltip> 道错题，
          {{ overview.todayDone ? '今日目标已完成，太棒了！' : '继续保持，完成今日目标' }}
        </p>
        <div class="banner-progress">
          <div class="banner-progress-inner" :style="{ width: todayPercent + '%' }"></div>
        </div>
      </div>
      <div class="banner-actions">
        <button class="glass-btn" @click="$router.push('/student/plan')">立即复盘</button>
        <button class="glass-btn" @click="$router.push('/student/error/edit')">录错题</button>
        <button class="glass-btn" @click="$router.push('/student/note/edit')">写笔记</button>
      </div>
    </div>

    <!-- 带 2 · 四张统计卡（CountUp 数字滚动 + 渐变图标块 + 交错入场） -->
    <div class="stat-grid">
      <div v-for="(card, i) in statCards" :key="card.label" class="stat-card rise-in stat-card-link" :class="'rise-in-' + (i + 1)" @click="card.to && $router.push(card.to)">
        <div class="stat-text">
          <span class="stat-label">{{ card.label }}</span>
          <span class="stat-value"><CountUp :end="card.value" /></span>
          <span class="stat-chip" :style="{ color: card.chipColor, background: card.chipBg }">{{ card.chip }}</span>
        </div>
        <div class="stat-icon" :style="{ background: card.gradient }">
          <el-icon :size="24" color="#fff"><component :is="card.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 带 3 · 图表双卡 -->
    <div class="chart-row rise-in rise-in-3">
      <AppCard class="chart-main" title="每周错题增长">
        <template #extra>
          <router-link class="more-link" to="/student/stats">查看全部统计 &gt;</router-link>
        </template>
        <Charts :option="weeklyOption" height="clamp(280px, 32vh, 340px)" />
      </AppCard>
      <AppCard class="chart-side" title="技术方向占比">
        <Charts :option="pieOption" height="clamp(280px, 32vh, 340px)" />
      </AppCard>
    </div>

    <!-- 带 4 · 今日复盘任务 + 最近错题 -->
    <div class="bottom-row rise-in rise-in-4">
      <AppCard class="bottom-task" title="今日复盘任务">
        <template #extra>
          <span class="task-progress-text">今日完成 {{ today.completedCount || 0 }}/{{ today.targetCount || 0 }}</span>
        </template>
        <div v-if="!today.enabled" class="task-tip">
          <EmptyState description="今天还没有复盘计划" btn-text="制定今日计划" @action="$router.push('/student/plan')" />
        </div>
        <template v-else>
          <div v-if="recommends.length === 0" class="task-done">
            <el-icon :size="40" color="var(--success)"><CircleCheckFilled /></el-icon>
            <p>今日任务已完成，明天继续加油！</p>
          </div>
          <div v-else class="task-list">
            <div
              v-for="item in recommends"
              :key="item.id"
              class="task-item"
            >
              <span class="err-badge" :class="'err-badge--' + item.errorType">{{ errorTypeLabel(item.errorType) }}</span>
              <span class="task-title" @click="$router.push(`/student/error/detail/${item.id}`)">{{ item.title }}</span>
              <el-button size="small" type="primary" round plain @click="finishOne(item.id)">完成复习</el-button>
            </div>
          </div>
        </template>
      </AppCard>

      <AppCard class="bottom-recent" title="最近错题">
        <template #extra>
          <router-link class="more-link" to="/student/error">进入错题本 &gt;</router-link>
        </template>
        <div v-if="recentList.length === 0">
          <EmptyState description="还没有错题，保持这个好习惯" btn-text="记录第一道错题" @action="$router.push('/student/error/edit')" />
        </div>
        <div v-else class="recent-list">
          <div
            v-for="item in recentList"
            :key="item.id"
            class="recent-item"
            @click="$router.push(`/student/error/detail/${item.id}`)"
          >
            <span class="err-badge" :class="'err-badge--' + item.errorType">{{ errorTypeLabel(item.errorType) }}</span>
            <div class="recent-main">
              <div class="recent-title">{{ item.title }}</div>
              <div class="recent-meta">
                <span class="tech-tag">{{ item.techDirection }}</span>
                <span class="recent-time">{{ fromNow(item.createTime) }}</span>
              </div>
            </div>
            <el-icon v-if="item.isTop === 1" class="top-icon" title="已置顶"><Top /></el-icon>
            <el-icon v-if="item.isFavorite === 1" class="fav-icon" title="已收藏"><StarFilled /></el-icon>
          </div>
        </div>
      </AppCard>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getOverview, getWeeklyMistake, getTechPie } from '@/api/stats'
import { getTodayPlan, finishReview } from '@/api/plan'
import { getMistakePage } from '@/api/mistake'
import { errorTypeLabel } from '@/constants/dict'
import { fromNow } from '@/utils/format'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/AppCard.vue'
import CountUp from '@/components/CountUp.vue'
import Charts from '@/components/Charts.vue'
import EmptyState from '@/components/EmptyState.vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const appStore = useAppStore()

const loading = ref(true)
const overview = ref({})
const weeklyData = ref([])
const pieData = ref([])
const today = ref({ enabled: true })
const recommends = ref([])
const recentList = ref([])

const greeting = computed(() => {
  const h = dayjs().hour()
  if (h < 6) return '夜深了'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayPercent = computed(() => {
  const target = overview.value.todayTarget || 0
  if (target === 0) return 0
  return Math.min(100, Math.round(((overview.value.todayFinished || 0) / target) * 100))
})

const statCards = computed(() => [
  { label: '错题总数', to: '/student/error', value: overview.value.mistakeTotal || 0, chip: `本周 +${overview.value.mistakeWeekNew || 0}`, chipColor: '#0D9467', chipBg: '#ECFDF5', icon: 'Document', gradient: 'linear-gradient(135deg,#6366F1,#818CF8)' },
  { label: '笔记总数', to: '/student/note', value: overview.value.noteTotal || 0, chip: `本周 +${overview.value.noteWeekNew || 0}`, chipColor: '#6D28D9', chipBg: '#F5F3FF', icon: 'Notebook', gradient: 'linear-gradient(135deg,#8B5CF6,#C4B5FD)' },
  { label: '连续打卡', to: '/student/plan/calendar', value: overview.value.checkInDays || 0, chip: (overview.value.checkInDays || 0) > 0 ? '坚持中' : '今天开始', chipColor: '#C47E09', chipBg: '#FFFBEB', icon: 'Sunny', gradient: 'linear-gradient(135deg,#F59E0B,#FBBF24)' },
  { label: '今日复盘进度', to: '/student/plan', value: todayPercent.value, chip: todayPercent.value >= 100 ? '已完成' : '进行中', chipColor: todayPercent.value >= 100 ? '#0D9467' : '#4F46E5', chipBg: todayPercent.value >= 100 ? '#ECFDF5' : '#EEF2FF', icon: 'CircleCheck', gradient: 'linear-gradient(135deg,#10B981,#34D399)' }
])

/* 每周错题增长柱状图（靛蓝渐变柱 + 圆角柱头） */
const weeklyOption = computed(() => {
  // 后端返回 ISO 年-周（如 2026-37），取"-"后的周数展示
  const weeks = weeklyData.value.map(i => '第' + String(i.week).split('-')[1] + '周')
  const counts = weeklyData.value.map(i => i.count)
  return {
    grid: { left: 16, right: 16, top: 40, bottom: 8, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: weeks },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: '#818CF8' }, { offset: 1, color: '#6366F1' }]
        }
      },
      barMaxWidth: 28
    }]
  }
})

/* 技术方向占比环形饼图（中心显示总数） */
const pieOption = computed(() => {
  const total = pieData.value.reduce((s, i) => s + Number(i.value), 0)
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['52%', '72%'],
      center: ['50%', '44%'],
      data: pieData.value,
      label: { show: false },
      emphasis: { scaleSize: 6 }
    }],
    graphic: [{
      type: 'text', left: 'center', top: '40%',
      style: { text: String(total), fontSize: 26, fontWeight: 700, fill: '#0F172A', textAlign: 'center' }
    }, {
      type: 'text', left: 'center', top: '52%',
      style: { text: '错题总数', fontSize: 12, fill: '#94A3B8', textAlign: 'center' }
    }]
  }
})

async function loadAll() {
  loading.value = true
  try {
    const [ov, weekly, pie, todayRes, recent] = await Promise.all([
      getOverview(),
      getWeeklyMistake(),
      getTechPie(),
      getTodayPlan(),
      getMistakePage({ pageNum: 1, pageSize: 5 })
    ])
    overview.value = ov.data
    weeklyData.value = weekly.data || []
    pieData.value = pie.data || []
    today.value = todayRes.data || { enabled: false }
    recommends.value = (todayRes.data && todayRes.data.recommends) || []
    recentList.value = recent.data.list || []
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

async function finishOne(id) {
  try {
    const res = await finishReview(id)
    ElMessage.success(res.data.finished ? '今日复盘任务全部完成，已自动打卡！' : '完成一道，继续加油！')
    loadAll()
    appStore.fetchTodayPlan()
  } catch (e) { /* 拦截器已提示（如重复复习） */ }
}

onMounted(loadAll)
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 20px; }

/* 首屏骨架 */
.dash-skeleton { display: flex; flex-direction: column; gap: 20px; }
.skel-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 20px; }

/* ---------- 欢迎横幅 ---------- */
.banner {
  position: relative;
  overflow: hidden;
  background: var(--brand-gradient-deep);
  border-radius: var(--radius-xl);
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
}
.banner-deco {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  pointer-events: none;
}
.deco-a { width: 180px; height: 180px; right: 220px; top: -70px; }
.deco-b { width: 120px; height: 120px; right: 160px; bottom: -50px; }
.banner-title { font-size: 24px; font-weight: 700; margin: 0 0 8px; }
.banner-sub { font-size: 14px; color: rgba(255, 255, 255, 0.85); margin: 0 0 14px; }
.banner-sub b { font-size: 18px; }
.banner-sub .target-hit { cursor: help; border-bottom: 1px dashed rgba(255, 255, 255, 0.5); }
.banner-progress {
  width: 320px; height: 6px; border-radius: 999px;
  background: rgba(255, 255, 255, 0.25); overflow: hidden;
}
.banner-progress-inner {
  height: 100%; background: #fff; border-radius: 999px;
  transition: width 0.6s var(--ease-out);
}
.banner-actions { display: flex; gap: 10px; position: relative; }
.glass-btn {
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: #fff; border-radius: 999px; padding: 8px 18px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all var(--dur-base) var(--ease-out);
}
.glass-btn:hover { background: rgba(255, 255, 255, 0.28); }

/* ---------- 统计卡 ---------- */
.stat-card-link { cursor: pointer; transition: transform var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out); }
.stat-card-link:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(99, 102, 241, 0.18); }
.stat-card-link:active { transform: translateY(-1px); }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 20px;
}
.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all var(--dur-base) var(--ease-out);
}
.stat-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-card-hover); }
.stat-text { display: flex; flex-direction: column; gap: 6px; }
.stat-label { font-size: 13px; color: var(--text-sub); }
.stat-value { font-size: 28px; font-weight: 700; color: var(--text-title); line-height: 1; }
.stat-chip { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 999px; width: fit-content; }
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

/* ---------- 图表双卡 ---------- */
.chart-row { display: flex; gap: 20px; }
.chart-main { flex: 2; min-width: 0; }
.chart-side { flex: 1; min-width: 0; }
.more-link { font-size: 13px; color: var(--primary); text-decoration: none; }
.more-link:hover { text-decoration: underline; }

/* ---------- 底部双卡 ---------- */
.bottom-row { display: flex; gap: 20px; }
.bottom-task { flex: 1; min-width: 0; }
.bottom-recent { flex: 2; min-width: 0; }
.task-progress-text { font-size: 13px; color: var(--text-sub); }
.task-done { text-align: center; padding: 28px 0; color: var(--text-sub); }
.task-done p { margin: 10px 0 0; font-size: 13px; }
.task-list { display: flex; flex-direction: column; gap: 10px; }
.task-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-md);
  transition: all var(--dur-base) var(--ease-out);
}
.task-item:hover { border-color: var(--primary-border); background: var(--primary-bg); }
.task-title {
  flex: 1; font-size: 13px; color: var(--text-title); font-weight: 500;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer;
}
.task-title:hover { color: var(--primary); }

.recent-list { display: flex; flex-direction: column; }
.recent-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--dur-fast);
  border-bottom: 1px solid var(--gray-100);
}
.recent-item:last-child { border-bottom: none; }
.recent-item:hover { background: var(--bg-hover); }
.recent-main { flex: 1; min-width: 0; }
.recent-title {
  font-size: 14px; font-weight: 500; color: var(--text-title);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.recent-meta { display: flex; align-items: center; gap: 10px; margin-top: 3px; }
.tech-tag { font-size: 12px; color: var(--primary); background: var(--primary-bg); padding: 1px 8px; border-radius: 999px; }
.recent-time { font-size: 12px; color: var(--text-placeholder); }
.top-icon { color: var(--primary); }
.fav-icon { color: var(--warning); }

@media (max-width: 1280px) {
  .chart-row, .bottom-row { flex-direction: column; }
}
</style>
