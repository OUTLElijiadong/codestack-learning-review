<template>
  <div class="layout">
    <!-- 深色侧边栏（藏青 #0F172A 压场，"深-浅-白"三层纵深） -->
    <aside class="sidebar" :class="{ folded }">
      <div class="logo-area" @click="$router.push('/student/dashboard')">
        <div class="logo-icon">&lt;/&gt;</div>
        <transition name="fade-slide">
          <div v-if="!folded" class="logo-text">
            <span class="logo-name">码栈 CodeStack</span>
            <span class="logo-sub">错题集与笔记复盘系统</span>
          </div>
        </transition>
      </div>
      <el-menu
        class="side-menu"
        :default-active="$route.path"
        :collapse="folded"
        router
        background-color="transparent"
        text-color="#94A3B8"
        active-text-color="#FFFFFF"
      >
        <template v-for="item in menus" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>
              <span>{{ item.title }}</span>
              <el-badge
                v-if="item.path === '/student/plan' && appStore.planRemind"
                is-dot
                class="menu-badge"
              />
            </template>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <div class="main-wrap">
      <!-- 顶栏：面包屑 + 搜索 + 打卡 + 公告 + 头像 -->
      <header class="topbar">
        <div class="topbar-left">
          <el-icon class="fold-btn" @click="folded = !folded">
            <Fold v-if="!folded" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>码栈 CodeStack</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 中部全局快搜：回车直达智能检索页 -->
        <div class="topbar-search">
          <el-input
            v-model="quickKeyword"
            placeholder="搜索错题 / 报错关键词，回车直达检索"
            :prefix-icon="Search"
            clearable
            @keyup.enter="goSearch"
          />
        </div>

        <div class="topbar-right">
          <!-- 打卡按钮：渐变胶囊；未完成复盘任务时挂红点，未打卡挂呼吸光圈 -->
          <el-badge :is-dot="appStore.planRemind" class="checkin-badge">
            <button
              class="checkin-btn"
              :class="{ checked: todayChecked, 'pulse-ring': !todayChecked }"
              @click="handleCheckIn"
            >
              <el-icon><Sunny /></el-icon>
              {{ todayChecked ? '今日已打卡' : '打卡 · 已连续 ' + checkInDays + ' 天' }}
            </button>
          </el-badge>

          <!-- 公告铃铛 -->
          <el-badge :value="appStore.unreadNotices.length" :hidden="appStore.unreadNotices.length === 0" :max="9">
            <el-icon class="bell" @click="noticeDrawer = true"><Bell /></el-icon>
          </el-badge>

          <!-- 头像下拉 -->
          <el-dropdown @command="handleCommand">
            <div class="avatar-wrap">
              <div class="avatar" :style="avatarStyle">
                {{ avatarText }}
              </div>
              <span class="nickname">{{ userStore.userInfo?.nickname || '同学' }}</span>
              <span
                v-if="userStore.userInfo?.learnDirection"
                class="direction-chip"
                :style="{ color: directionColor(userStore.userInfo.learnDirection) }"
              >{{ directionLabel(userStore.userInfo.learnDirection) }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="security">
                  <el-icon><Lock /></el-icon>账号安全
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon><span class="logout-text">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 公告抽屉 -->
    <el-drawer v-model="noticeDrawer" title="系统公告" size="380px">
      <div v-if="noticeList.length === 0" class="notice-empty">暂无公告</div>
      <div v-for="n in noticeList" :key="n.id" class="notice-item" @click="openNotice(n)">
        <div class="notice-item-head">
          <el-tag size="small" :type="noticeTypeMeta(n.type).type" effect="light">{{ noticeTypeMeta(n.type).label }}</el-tag>
          <span class="notice-time">{{ fmtDate(n.publishTime || n.createTime) }}</span>
        </div>
        <div class="notice-title">{{ n.title }}</div>
      </div>
    </el-drawer>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="noticeDialog" :title="currentNotice?.title" width="560px">
      <div class="notice-content" v-html="noticeHtml"></div>
      <template #footer>
        <el-button type="primary" @click="noticeDialog = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 登录后未读公告自动弹窗 -->
    <el-dialog v-model="unreadDialog" title="📢 新公告" width="560px" :close-on-click-modal="false">
      <div v-for="n in appStore.unreadNotices" :key="n.id" class="unread-item">
        <div class="unread-title">
          <el-tag size="small" :type="noticeTypeMeta(n.type).type" effect="light">{{ noticeTypeMeta(n.type).label }}</el-tag>
          {{ n.title }}
        </div>
        <div class="unread-content">{{ n.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="readAllNotices">全部已读</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getNoticePage } from '@/api/notice'
import { checkIn } from '@/api/plan'
import { getOverview } from '@/api/stats'
import { directionLabel, directionColor, NOTICE_TYPES } from '@/constants/dict'
import { fmtDate } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const folded = ref(false)
const noticeDrawer = ref(false)
const noticeDialog = ref(false)
const unreadDialog = ref(false)
const noticeList = ref([])
const currentNotice = ref(null)
const todayChecked = ref(false)
const checkInDays = ref(0)
const quickKeyword = ref('')

// 全局快搜：回车直达智能检索页并带上关键词
function goSearch() {
  const kw = quickKeyword.value.trim()
  router.push({ path: '/student/search', query: kw ? { keyword: kw } : {} })
}

// 侧边菜单（复盘计划项在未完成任务时显示红点角标）
const menus = [
  { path: '/student/dashboard', title: '学习总览', icon: 'Odometer' },
  { path: '/student/error', title: '错题本', icon: 'Notebook' },
  { path: '/student/favorite', title: '收藏总览', icon: 'StarFilled' },
  { path: '/student/note', title: '编程笔记', icon: 'EditPen' },
  { path: '/student/plan', title: '复盘计划', icon: 'AlarmClock' },
  { path: '/student/plan/calendar', title: '复盘日历', icon: 'Calendar' },
  { path: '/student/search', title: '智能检索', icon: 'Search' },
  { path: '/student/stats', title: '学习统计', icon: 'DataAnalysis' },
  { path: '/student/community', title: '互助问答', icon: 'ChatDotRound' },
  { path: '/student/notice', title: '系统公告', icon: 'Bell' }
]

const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '同'
  return name.charAt(0).toUpperCase()
})

// 有头像图则用图，否则昵称首字 + 品牌渐变底
const avatarStyle = computed(() => {
  const url = userStore.userInfo?.avatar
  if (url) {
    return { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return { background: 'var(--brand-gradient)', color: '#fff' }
})

const noticeHtml = computed(() => (currentNotice.value?.content || '').replace(/\n/g, '<br/>'))

function noticeTypeMeta(type) {
  return NOTICE_TYPES.find(t => t.value === type) || { label: '通知', type: 'primary' }
}

onMounted(async () => {
  // 登录后依次拉取：未读公告（有则自动弹窗）+ 今日复盘任务（未完成提醒）+ 总览（打卡状态）
  await Promise.all([appStore.fetchUnreadNotices(), appStore.fetchTodayPlan(), loadOverview()])
  if (appStore.unreadNotices.length > 0) {
    unreadDialog.value = true
  }
  loadNotices()
})

async function loadOverview() {
  try {
    const res = await getOverview()
    todayChecked.value = !!res.data.todayChecked
    checkInDays.value = res.data.checkInDays || 0
  } catch (e) { /* 静默 */ }
}

async function loadNotices() {
  try {
    const res = await getNoticePage({ pageNum: 1, pageSize: 20 })
    noticeList.value = res.data.list || []
  } catch (e) { /* 静默 */ }
}

function openNotice(n) {
  currentNotice.value = n
  noticeDialog.value = true
  appStore.readNotice(n.id)
}

function readAllNotices() {
  appStore.unreadNotices.forEach(n => appStore.readNotice(n.id))
  unreadDialog.value = false
}

// 顶栏打卡按钮
async function handleCheckIn() {
  if (todayChecked.value) {
    ElMessage.success('今天已经打过卡啦，继续保持！')
    return
  }
  try {
    await checkIn()
    todayChecked.value = true
    checkInDays.value += 1
    ElMessage.success('打卡成功！已连续学习 ' + checkInDays.value + ' 天')
    appStore.fetchTodayPlan()
  } catch (e) { /* 拦截器已提示 */ }
}

async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/student/profile')
  } else if (cmd === 'security') {
    router.push('/student/profile/security')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', { type: 'warning' })
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

/* ---------- 深色侧边栏 ---------- */
.sidebar {
  width: var(--sidebar-w);
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width var(--dur-base) var(--ease-out);
  flex-shrink: 0;
}
.sidebar.folded { width: var(--sidebar-w-fold); }

.logo-area {
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
}
.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--brand-gradient);
  color: #fff;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logo-text { display: flex; flex-direction: column; white-space: nowrap; }
.logo-name { color: #fff; font-size: 15px; font-weight: 700; }
.logo-sub { color: var(--sidebar-text); font-size: 11px; margin-top: 1px; }

.side-menu {
  border-right: none;
  padding: 12px 10px;
}
.side-menu :deep(.el-menu-item) {
  height: 44px;
  margin: 4px 0;
  border-radius: var(--radius-md);
  color: var(--sidebar-text);
}
.side-menu :deep(.el-menu-item:hover) {
  background: var(--sidebar-hover-bg);
}
.side-menu :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active-bg);
  color: var(--sidebar-text-active);
  box-shadow: inset 3px 0 0 var(--primary);
}
.menu-badge { margin-left: 6px; vertical-align: super; }

/* ---------- 顶栏 ---------- */
.main-wrap { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: var(--header-h);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid var(--gray-200);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--content-pad);
  position: sticky;
  top: 0;
  z-index: 20;
}
.topbar-left { display: flex; align-items: center; gap: 14px; }
.fold-btn { font-size: 18px; cursor: pointer; color: var(--text-sub); }
.fold-btn:hover { color: var(--primary); }

/* 中部全局快搜：胶囊圆角，聚焦变白底 + 靛蓝描边 */
.topbar-search { flex: 1; display: flex; justify-content: center; padding: 0 24px; }
.topbar-search :deep(.el-input) { max-width: 380px; }
.topbar-search :deep(.el-input__wrapper) {
  border-radius: 999px;
  background: var(--gray-100);
  box-shadow: none;
  transition: all var(--dur-base) var(--ease-out);
}
.topbar-search :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1.5px var(--primary) inset, 0 4px 14px rgba(99, 102, 241, 0.12);
}
.topbar-right { display: flex; align-items: center; gap: 20px; }

/* 打卡按钮：品牌渐变胶囊 */
.checkin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: var(--brand-gradient);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.checkin-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }
.checkin-btn.checked {
  background: var(--success-bg);
  color: var(--success);
  border: 1px solid var(--success-border);
  box-shadow: none;
}

.bell { font-size: 19px; color: var(--text-sub); cursor: pointer; }
.bell:hover { color: var(--primary); }

.avatar-wrap { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  border: 2px solid var(--gray-200);
}
.nickname { font-size: 14px; color: var(--text-title); font-weight: 600; }
.direction-chip {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--gray-100);
  font-weight: 600;
}
.logout-text { color: var(--danger); }

/* ---------- 主内容区 ---------- */
.content {
  flex: 1;
  padding: var(--content-pad);
  min-width: 0;
}

/* ---------- 公告 ---------- */
.notice-empty { text-align: center; color: var(--text-placeholder); padding: 40px 0; }
.notice-item {
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--gray-200);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-out);
}
.notice-item:hover { border-color: var(--primary-border); background: var(--primary-bg); }
.notice-item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.notice-time { font-size: 12px; color: var(--text-placeholder); }
.notice-title { font-size: 14px; font-weight: 600; color: var(--text-title); }
.notice-content { font-size: 14px; line-height: 1.9; color: var(--text-main); white-space: normal; }
.unread-item { margin-bottom: 16px; }
.unread-title { font-size: 15px; font-weight: 600; color: var(--text-title); margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
.unread-content { font-size: 13px; color: var(--text-sub); line-height: 1.8; background: var(--gray-50); padding: 10px 14px; border-radius: var(--radius-md); }
</style>
