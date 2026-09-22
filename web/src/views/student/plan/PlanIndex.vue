<template>
  <div class="plan-page" v-loading="loading">
    <div class="plan-grid">
      <!-- 左列：计划设置 + 打卡 -->
      <div class="plan-left">
        <AppCard title="我的复盘计划">
          <el-form label-position="top">
            <el-form-item label="计划名称">
              <el-input v-model="planForm.planName" maxlength="30" />
            </el-form-item>
            <el-form-item label="每天复习几道错题">
              <el-input-number v-model="planForm.dailyCount" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
            <el-form-item label="未完成时提醒我（登录弹窗 + 菜单角标）">
              <el-switch v-model="planForm.remindEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="启用计划">
              <el-switch v-model="planForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <button type="button" class="save-btn" :disabled="saving" @click="savePlanNow">
              {{ saving ? '保存中…' : '保存计划' }}
            </button>
          </el-form>
        </AppCard>

        <AppCard title="今日打卡" class="checkin-card">
          <div class="checkin-box">
            <div class="checkin-flame">
              <el-icon :size="44" :color="today.checked ? '#F59E0B' : '#CBD5E1'"><Sunny /></el-icon>
            </div>
            <div class="checkin-text">
              <div class="checkin-title">{{ today.checked ? '今日已打卡' : '今天还没打卡' }}</div>
              <div class="checkin-sub">完成每日目标会自动打卡，也可以手动补卡</div>
            </div>
            <button
              class="checkin-btn"
              :class="{ done: today.checked }"
              :disabled="today.checked"
              @click="doCheckIn"
            >{{ today.checked ? '已完成 ✓' : '立即打卡' }}</button>
          </div>
        </AppCard>

        <AppCard title="记录学习时长">
          <div class="time-row">
            <el-input-number v-model="minutes" :min="1" :max="1440" placeholder="分钟" style="flex:1" />
            <el-select v-model="timeSource" style="width: 130px">
              <el-option label="复盘错题" value="review" />
              <el-option label="写笔记" value="note" />
              <el-option label="刷题" value="practice" />
              <el-option label="手动补录" value="manual" />
            </el-select>
            <el-button type="primary" round @click="reportTime">上报</el-button>
          </div>
          <p class="time-tip">学习时长会进入「学习统计」的每日时长折线图</p>
        </AppCard>
      </div>

      <!-- 右列：今日复盘任务 -->
      <AppCard class="plan-right" title="今日复盘任务">
        <template #extra>
          <div class="task-head-right">
            <el-progress
              type="circle"
              :percentage="taskPercent"
              :width="46"
              :stroke-width="5"
              :color="taskPercent >= 100 ? '#10B981' : '#6366F1'"
            />
            <router-link class="more-link" to="/student/plan/calendar">复盘日历 &gt;</router-link>
          </div>
        </template>

        <el-alert
          v-if="!today.enabled"
          type="info"
          :closable="false"
          title="复盘计划未启用，先在左侧开启计划并保存"
          class="task-alert"
        />

        <template v-else>
          <div v-if="today.finished" class="all-done">
            <el-icon :size="52" color="var(--success)"><CircleCheckFilled /></el-icon>
            <p class="all-done-text">今日 {{ today.targetCount }} 道错题全部复盘完成，已自动打卡！</p>
            <el-button round @click="$router.push('/student/plan/calendar')">去日历看看</el-button>
          </div>
          <template v-else>
            <p class="task-desc">
              今日目标复习 <b>{{ today.targetCount }}</b> 道，已完成 <b class="done-num">{{ today.completedCount }}</b> 道。
              点击「完成复习」即打勾；全部完成会自动打卡。
            </p>
            <div v-if="recommends.length === 0" class="no-reco">
              <EmptyState description="没有可推荐的错题了，去错题本收录几道吧" btn-text="去录错题" @action="$router.push('/student/error/edit')" />
            </div>
            <div v-else class="reco-list">
              <div v-for="item in recommends" :key="item.id" class="reco-item">
                <span class="err-badge" :class="'err-badge--' + item.errorType">{{ errorTypeLabel(item.errorType) }}</span>
                <div class="reco-main" @click="$router.push(`/student/error/detail/${item.id}`)">
                  <div class="reco-title">{{ item.title }}</div>
                  <div class="reco-meta">{{ item.techDirection }} · 已复习 {{ item.reviewCount }} 次</div>
                </div>
                <el-button type="primary" round plain size="small" @click="finishOne(item.id)">完成复习</el-button>
              </div>
            </div>
          </template>
        </template>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunny, CircleCheckFilled } from '@element-plus/icons-vue'
import { getPlan, savePlan, getTodayPlan, finishReview, checkIn, reportStudyTime } from '@/api/plan'
import { errorTypeLabel } from '@/constants/dict'
import { useAppStore } from '@/stores/app'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const appStore = useAppStore()
const loading = ref(true)
const saving = ref(false)
const minutes = ref(30)
const timeSource = ref('review')

const planForm = reactive({
  planName: '每日复盘计划',
  dailyCount: 5,
  remindEnabled: 1,
  status: 1
})

const today = ref({ enabled: true })
const recommends = ref([])

const taskPercent = computed(() => {
  const target = today.value.targetCount || 0
  if (!target) return 0
  return Math.min(100, Math.round(((today.value.completedCount || 0) / target) * 100))
})

async function loadAll() {
  loading.value = true
  try {
    const [planRes, todayRes] = await Promise.all([getPlan(), getTodayPlan()])
    const p = planRes.data
    planForm.planName = p.planName || '每日复盘计划'
    planForm.dailyCount = p.dailyCount || 5
    planForm.remindEnabled = p.remindEnabled ?? 1
    planForm.status = p.status ?? 1
    today.value = todayRes.data || { enabled: false }
    recommends.value = (todayRes.data && todayRes.data.recommends) || []
  } finally {
    loading.value = false
  }
}

async function savePlanNow() {
  saving.value = true
  try {
    await savePlan(planForm)
    ElMessage.success('复盘计划已保存')
    loadAll()
  } finally {
    saving.value = false
  }
}

async function doCheckIn() {
  await checkIn()
  ElMessage.success('打卡成功！继续保持')
  loadAll()
}

async function finishOne(id) {
  try {
    const res = await finishReview(id)
    if (res.data.finished) {
      ElMessage.success('今日复盘任务全部完成，已自动打卡！')
    } else {
      ElMessage.success(`已完成 ${res.data.completedCount}/${res.data.targetCount} 道`)
    }
    loadAll()
    appStore.fetchTodayPlan()
  } catch (e) { /* 拦截器已提示（如该题今日已复习） */ }
}

async function reportTime() {
  if (!minutes.value) {
    ElMessage.warning('请填写学习时长')
    return
  }
  await reportStudyTime({ duration: minutes.value, source: timeSource.value })
  ElMessage.success(`已记录 ${minutes.value} 分钟学习时长`)
}

onMounted(loadAll)
</script>

<style scoped>
.plan-grid { display: flex; gap: 20px; align-items: flex-start; }
.plan-left { width: 340px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; }
.plan-right { flex: 1; min-width: 0; }

.save-btn {
  width: 100%; height: 40px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.save-btn:hover { filter: brightness(1.06); }
.save-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.checkin-box { display: flex; align-items: center; gap: 14px; }
.checkin-flame {
  width: 68px; height: 68px; border-radius: 50%;
  background: var(--warning-bg);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.checkin-text { flex: 1; }
.checkin-title { font-size: 16px; font-weight: 700; color: var(--text-title); }
.checkin-sub { font-size: 12px; color: var(--text-sub); margin-top: 4px; }
.checkin-btn {
  height: 38px; padding: 0 20px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
}
.checkin-btn.done {
  background: var(--success-bg); color: var(--success);
  border: 1px solid var(--success-border); box-shadow: none; cursor: default;
}

.time-row { display: flex; gap: 10px; align-items: center; }
.time-tip { font-size: 12px; color: var(--text-placeholder); margin: 10px 0 0; }

.task-head-right { display: flex; align-items: center; gap: 14px; }
.more-link { font-size: 13px; color: var(--primary); text-decoration: none; }
.more-link:hover { text-decoration: underline; }
.task-alert { border-radius: var(--radius-md); }
.task-desc { font-size: 13px; color: var(--text-sub); margin: 0 0 16px; }
.task-desc b { color: var(--text-title); }
.done-num { color: var(--primary) !important; }
.all-done { text-align: center; padding: 40px 0; }
.all-done-text { font-size: 14px; color: var(--text-sub); margin: 14px 0 18px; }

.reco-list { display: flex; flex-direction: column; gap: 12px; }
.reco-item {
  display: flex; align-items: center; gap: 12px;
  border: 1px solid var(--gray-200); border-radius: var(--radius-md);
  padding: 12px 14px;
  transition: all var(--dur-base) var(--ease-out);
}
.reco-item:hover { border-color: var(--primary-border); background: var(--primary-bg); }
.reco-main { flex: 1; min-width: 0; cursor: pointer; }
.reco-title {
  font-size: 14px; font-weight: 600; color: var(--text-title);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.reco-meta { font-size: 12px; color: var(--text-placeholder); margin-top: 3px; }

@media (max-width: 1100px) {
  .plan-grid { flex-direction: column; }
  .plan-left { width: 100%; }
}
</style>
