import { defineStore } from 'pinia'
import { login, getProfile, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),   // 刷新页面后从 localStorage 恢复
    userInfo: null       // { id, username, nickname, avatar, role, learnDirection, major, ... }
  }),
  getters: {
    // 按角色决定登录后落地页：学生进 /student，教师与超管进 /admin
    homePath: (state) =>
      state.userInfo && state.userInfo.role === 'student' ? '/student/dashboard' : '/admin/screen',
    isAdmin: (state) => state.userInfo && state.userInfo.role === 'admin',
    isManager: (state) =>
      state.userInfo && ['teacher', 'admin'].includes(state.userInfo.role)
  },
  actions: {
    // 登录：保存 token 与用户信息
    async login(loginForm) {
      const res = await login(loginForm)
      this.token = res.data.token
      this.userInfo = res.data.userInfo
      setToken(this.token)
    },
    // 拉取个人信息（刷新页面后路由守卫中调用）
    async fetchProfile() {
      const res = await getProfile()
      this.userInfo = res.data
    },
    // 登出：清状态 + 清 token
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 登出失败不阻塞本地清理
      }
      this.token = null
      this.userInfo = null
      removeToken()
    }
  }
})
