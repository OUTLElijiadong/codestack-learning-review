import { createRouter, createWebHashHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

// 全部页面路由懒加载；meta.title 页签标题，meta.icon 侧边菜单图标，
// meta.roles 角色鉴权，meta.public 免登录，meta.hidden 不进侧边菜单
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/ForgotPassword.vue'),
    meta: { title: '找回密码', public: true }
  },
  {
    path: '/student',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/student/dashboard',
    meta: { roles: ['student'] },
    children: [
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('@/views/student/dashboard/index.vue'), meta: { title: '学习总览', icon: 'Odometer', roles: ['student'] } },
      { path: 'error', name: 'ErrorList', component: () => import('@/views/student/error/ErrorList.vue'), meta: { title: '错题本', icon: 'Notebook', roles: ['student'] } },
      { path: 'error/edit/:id?', name: 'ErrorEdit', component: () => import('@/views/student/error/ErrorEdit.vue'), meta: { title: '编辑错题', roles: ['student'], hidden: true } },
      { path: 'error/detail/:id', name: 'ErrorDetail', component: () => import('@/views/student/error/ErrorDetail.vue'), meta: { title: '错题详情', roles: ['student'], hidden: true } },
      { path: 'error/archive', name: 'ErrorArchive', component: () => import('@/views/student/error/ErrorArchive.vue'), meta: { title: '错题归档', icon: 'Box', roles: ['student'] } },
      { path: 'note', name: 'NoteList', component: () => import('@/views/student/note/NoteList.vue'), meta: { title: '编程笔记', icon: 'EditPen', roles: ['student'] } },
      { path: 'note/edit/:id?', name: 'NoteEdit', component: () => import('@/views/student/note/NoteEdit.vue'), meta: { title: '写笔记', roles: ['student'], hidden: true } },
      { path: 'note/category', name: 'NoteCategory', component: () => import('@/views/student/note/NoteCategory.vue'), meta: { title: '笔记分类', roles: ['student'], hidden: true } },
      { path: 'note/detail/:id', name: 'NoteDetail', component: () => import('@/views/student/note/NoteDetail.vue'), meta: { title: '笔记详情', roles: ['student'], hidden: true } },
      { path: 'plan', name: 'PlanIndex', component: () => import('@/views/student/plan/PlanIndex.vue'), meta: { title: '复盘计划', icon: 'AlarmClock', roles: ['student'] } },
      { path: 'plan/calendar', name: 'PlanCalendar', component: () => import('@/views/student/plan/PlanCalendar.vue'), meta: { title: '复盘日历', icon: 'Calendar', roles: ['student'] } },
      { path: 'favorite', name: 'FavoriteIndex', component: () => import('@/views/student/favorite/FavoriteIndex.vue'), meta: { title: '收藏总览', icon: 'Star', roles: ['student'] } },
      { path: 'search', name: 'SearchIndex', component: () => import('@/views/student/search/SearchIndex.vue'), meta: { title: '智能检索', icon: 'Search', roles: ['student'] } },
      { path: 'stats', name: 'StatsIndex', component: () => import('@/views/student/stats/StatsIndex.vue'), meta: { title: '学习统计', icon: 'DataAnalysis', roles: ['student'] } },
      { path: 'community', name: 'QuestionList', component: () => import('@/views/student/community/QuestionList.vue'), meta: { title: '互助问答', icon: 'ChatDotRound', roles: ['student'] } },
      { path: 'community/detail/:id', name: 'QuestionDetail', component: () => import('@/views/student/community/QuestionDetail.vue'), meta: { title: '问题详情', roles: ['student'], hidden: true } },
      { path: 'community/mine', name: 'MyQuestion', component: () => import('@/views/student/community/MyQuestion.vue'), meta: { title: '我的问答', roles: ['student'], hidden: true } },
      { path: 'notice', name: 'StudentNotice', component: () => import('@/views/student/notice/NoticeList.vue'), meta: { title: '系统公告', icon: 'Bell', roles: ['student'] } },
      { path: 'profile', name: 'ProfileInfo', component: () => import('@/views/student/profile/ProfileInfo.vue'), meta: { title: '个人资料', roles: ['student'], hidden: true } },
      { path: 'profile/security', name: 'ProfileSecurity', component: () => import('@/views/student/profile/ProfileSecurity.vue'), meta: { title: '安全设置', roles: ['student'], hidden: true } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/screen',
    meta: { roles: ['teacher', 'admin'] },
    children: [
      { path: 'screen', name: 'DataScreen', component: () => import('@/views/admin/screen/DataScreen.vue'), meta: { title: '数据大屏', icon: 'Monitor', roles: ['teacher', 'admin'] } },
      { path: 'user', name: 'UserManage', component: () => import('@/views/admin/user/UserManage.vue'), meta: { title: '用户管理', icon: 'User', roles: ['teacher', 'admin'] } },
      { path: 'audit', name: 'AuditManage', component: () => import('@/views/admin/audit/AuditManage.vue'), meta: { title: '内容审核', icon: 'View', roles: ['teacher', 'admin'] } },
      { path: 'notice', name: 'NoticeManage', component: () => import('@/views/admin/notice/NoticeManage.vue'), meta: { title: '公告管理', icon: 'Bell', roles: ['teacher', 'admin'] } },
      { path: 'system/admin', name: 'AdminManage', component: () => import('@/views/admin/system/AdminManage.vue'), meta: { title: '管理员管理', icon: 'Avatar', roles: ['admin'] } },
      { path: 'system/log', name: 'OperationLog', component: () => import('@/views/admin/system/OperationLog.vue'), meta: { title: '操作日志', icon: 'Document', roles: ['admin'] } }
    ]
  },
  { path: '/', redirect: '/login' },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局前置守卫：进度条 → 标题 → 免登录白名单 → token 校验 → 拉取用户信息 → 角色鉴权
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title
    ? `${to.meta.title} - 码栈 CodeStack`
    : '码栈 CodeStack · 编程学习错题集与笔记复盘系统'
  const userStore = useUserStore()

  // 1. 免登录页面：已登录用户再访问登录页时，回自己端首页
  if (to.meta.public) {
    if (userStore.token && to.path === '/login') {
      if (!userStore.userInfo) {
        try {
          await userStore.fetchProfile()
        } catch (e) {
          await userStore.logout()
          return next()
        }
      }
      return next(userStore.homePath)
    }
    return next()
  }

  // 2. 未登录：跳登录页并记录来源地址，登录后可跳回
  if (!userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 3. 有 token 无用户信息（浏览器刷新场景）：先拉取个人信息
  if (!userStore.userInfo) {
    try {
      await userStore.fetchProfile()
    } catch (e) {
      await userStore.logout()
      return next('/login')
    }
  }

  // 4. 角色鉴权：目标路由声明了 roles 且当前角色不在其中 => 拒绝
  if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo.role)) {
    ElMessage.warning('没有权限访问该页面')
    return next(userStore.homePath)
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
