<template>
  <div class="layout">
    <!-- 管理端深色侧边栏 -->
    <aside class="sidebar" :class="{ folded }">
      <div class="logo-area" @click="$router.push('/admin/screen')">
        <div class="logo-icon">&lt;/&gt;</div>
        <div v-if="!folded" class="logo-text">
          <span class="logo-name">码栈 CodeStack</span>
          <span class="logo-sub">管理后台</span>
        </div>
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
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>
            <span>{{ item.title }}</span>
            <el-icon v-if="item.adminOnly" class="lock-icon" title="仅超级管理员"><Lock /></el-icon>
          </template>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-wrap">
      <header class="topbar">
        <div class="topbar-left">
          <el-icon class="fold-btn" @click="folded = !folded">
            <Fold v-if="!folded" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" effect="light" round>
            {{ userStore.isAdmin ? '超级管理员' : '教师' }}
          </el-tag>
          <span class="admin-name">{{ userStore.userInfo?.nickname }}</span>
          <el-dropdown @command="handleCommand">
            <div class="avatar" :style="avatarStyle">{{ avatarText }}</div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon><span class="logout-text">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
// 管理端布局：菜单按角色过滤，超管独有菜单项带小锁图标
// （前端隐藏 + 路由守卫 + 后端 @RequireRole 接口拦截，三层防护）
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const folded = ref(false)

// 菜单：adminOnly 项仅超管可见（teacher 登录时直接不渲染）
const menus = computed(() => {
  const base = [
    { path: '/admin/screen', title: '数据大屏', icon: 'Monitor' },
    { path: '/admin/user', title: '用户管理', icon: 'User' },
    { path: '/admin/audit', title: '内容审核', icon: 'View' },
    { path: '/admin/notice', title: '公告管理', icon: 'Bell' }
  ]
  if (userStore.isAdmin) {
    base.push(
      { path: '/admin/system/admin', title: '管理员管理', icon: 'Avatar', adminOnly: true },
      { path: '/admin/system/log', title: '操作日志', icon: 'Document', adminOnly: true }
    )
  }
  return base
})

const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '管'
  return name.charAt(0).toUpperCase()
})

const avatarStyle = computed(() => {
  const url = userStore.userInfo?.avatar
  if (url) {
    return { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return { background: 'var(--brand-gradient)', color: '#fff' }
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', { type: 'warning' })
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { display: flex; min-height: 100vh; background: var(--bg-page); }

.sidebar {
  width: var(--sidebar-w);
  background: var(--sidebar-bg);
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
  width: 36px; height: 36px; border-radius: 10px;
  background: var(--brand-gradient); color: #fff;
  font-family: var(--font-mono); font-weight: 700; font-size: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.logo-text { display: flex; flex-direction: column; white-space: nowrap; }
.logo-name { color: #fff; font-size: 15px; font-weight: 700; }
.logo-sub { color: var(--sidebar-text); font-size: 11px; margin-top: 1px; }

.side-menu { border-right: none; padding: 12px 10px; }
.side-menu :deep(.el-menu-item) {
  height: 44px; margin: 4px 0; border-radius: var(--radius-md); color: var(--sidebar-text);
}
.side-menu :deep(.el-menu-item:hover) { background: var(--sidebar-hover-bg); }
.side-menu :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active-bg);
  color: var(--sidebar-text-active);
  box-shadow: inset 3px 0 0 var(--primary);
}
.lock-icon { margin-left: 6px; color: var(--warning); font-size: 13px; }

.main-wrap { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: var(--header-h);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid var(--gray-200);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 var(--content-pad);
  position: sticky; top: 0; z-index: 20;
}
.topbar-left { display: flex; align-items: center; gap: 14px; }
.fold-btn { font-size: 18px; cursor: pointer; color: var(--text-sub); }
.fold-btn:hover { color: var(--primary); }
.topbar-right { display: flex; align-items: center; gap: 14px; }
.admin-name { font-size: 14px; color: var(--text-title); font-weight: 600; }
.avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700; cursor: pointer;
  border: 2px solid var(--gray-200);
}
.logout-text { color: var(--danger); }

.content { flex: 1; padding: var(--content-pad); min-width: 0; }
</style>
