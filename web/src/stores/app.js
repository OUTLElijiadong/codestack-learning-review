import { defineStore } from 'pinia'
import { getUnreadNotices, markNoticeRead } from '@/api/notice'
import { getTodayPlan } from '@/api/plan'

// 应用级状态：未读公告（登录后弹窗）+ 今日复盘未完成提醒（菜单/顶栏角标）
export const useAppStore = defineStore('app', {
  state: () => ({
    unreadNotices: [],   // 未读公告数组 [{ id, title, content, type, publishTime }]
    todayPlan: null,     // 今日复盘任务 { targetCount, completedCount, finished, checked, ... }
    planRemind: false    // 今日复盘未完成 => 显示提醒
  }),
  getters: {
    // 顶栏角标总数 = 未读公告数 + 复盘提醒
    badgeCount: (state) => state.unreadNotices.length + (state.planRemind ? 1 : 0)
  },
  actions: {
    async fetchUnreadNotices() {
      try {
        const res = await getUnreadNotices()
        this.unreadNotices = res.data || []
      } catch (e) {
        // 忽略：公告拉取失败不影响主流程
      }
    },
    // 看完公告后标记已读并移出列表
    async readNotice(id) {
      try {
        await markNoticeRead(id)
      } catch (e) {
        // 已读失败静默
      }
      this.unreadNotices = this.unreadNotices.filter((n) => n.id !== id)
    },
    async fetchTodayPlan() {
      try {
        const res = await getTodayPlan()
        this.todayPlan = res.data
        // 计划启用 + 开启提醒 + 今日未完成 → 提醒
        this.planRemind = !!(res.data && res.data.enabled && res.data.remind && !res.data.finished)
      } catch (e) {
        this.planRemind = false
      }
    }
  }
})
