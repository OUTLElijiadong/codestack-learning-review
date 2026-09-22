<template>
  <div class="user-manage">
    <!-- 活跃用户统计卡 -->
    <div class="stat-row" v-if="active">
      <div class="mini-stat"><span class="mini-value">{{ active.studentTotal }}</span><span class="mini-label">注册学生总数</span></div>
      <div class="mini-stat"><span class="mini-value" style="color: var(--success)">{{ active.activeWeek }}</span><span class="mini-label">近7天活跃学习用户</span></div>
      <div class="mini-stat"><span class="mini-value" style="color: var(--primary)">{{ active.activeToday }}</span><span class="mini-label">今日活跃</span></div>
      <div class="mini-stat"><span class="mini-value" style="color: var(--danger)">{{ active.frozenTotal }}</span><span class="mini-label">冻结账号</span></div>
    </div>

    <AppCard>
      <!-- 筛选条 -->
      <div class="filter-row">
        <el-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable :prefix-icon="Search" style="width: 220px" @keyup.enter="loadList" @clear="loadList" />
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 130px" @change="loadList">
          <el-option label="学生" value="student" />
          <el-option label="教师" value="teacher" />
          <el-option label="超级管理员" value="admin" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadList">
          <el-option label="正常" :value="1" />
          <el-option label="已冻结" :value="0" />
        </el-select>
        <el-button type="primary" round :icon="Search" @click="loadList">查询</el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="u-avatar">{{ (row.nickname || row.username)[0] }}</div>
              <div>
                <div class="u-name">{{ row.nickname || row.username }}</div>
                <div class="u-sub">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="roleMeta(row.role).type" size="small" round effect="light">{{ roleMeta(row.role).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" width="130" align="center">
          <template #default="{ row }">{{ row.major || '-' }}</template>
        </el-table-column>
        <el-table-column label="学习方向" width="100" align="center">
          <template #default="{ row }">{{ directionLabel(row.learnDirection) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" round>
              {{ row.status === 1 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近登录" width="150" align="center">
          <template #default="{ row }">{{ fmtTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" width="120" align="center">
          <template #default="{ row }">{{ fmtDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role === 'student'">
              <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
              <el-popconfirm v-if="row.status === 1" title="冻结后该账号将无法登录，确定？" @confirm="freeze(row)">
                <template #reference><el-button link type="danger" size="small">冻结</el-button></template>
              </el-popconfirm>
              <el-popconfirm v-else title="确定解封该账号？" @confirm="unfreeze(row)">
                <template #reference><el-button link type="success" size="small">解封</el-button></template>
              </el-popconfirm>
              <el-popconfirm title="将该学生密码重置为 123456？" @confirm="resetPwd(row)">
                <template #reference><el-button link type="warning" size="small">重置密码</el-button></template>
              </el-popconfirm>
            </template>
            <span v-else class="muted">管理员账号</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          :total="total"
          :page-size="query.pageSize"
          layout="total, prev, pager, next"
          background
          @current-change="loadList"
        />
      </div>
    </AppCard>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="drawer" title="用户详情" size="420px">
      <template v-if="detail">
        <div class="d-head">
          <div class="u-avatar big">{{ (detail.info.nickname || detail.info.username)[0] }}</div>
          <div class="d-name">{{ detail.info.nickname }}</div>
          <div class="u-sub">@{{ detail.info.username }}</div>
        </div>
        <div class="d-stats">
          <div class="d-stat"><span class="d-num">{{ detail.mistakeTotal }}</span><span>错题</span></div>
          <div class="d-stat"><span class="d-num">{{ detail.noteTotal }}</span><span>笔记</span></div>
          <div class="d-stat"><span class="d-num">{{ detail.checkInTotal }}</span><span>打卡天数</span></div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="专业">{{ detail.info.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.info.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学习方向">{{ directionLabel(detail.info.learnDirection) }}</el-descriptions-item>
          <el-descriptions-item label="个人简介">{{ detail.info.bio || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ fmtTime(detail.info.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="最近登录">{{ fmtTime(detail.info.lastLoginTime) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserPage, getUserDetail, freezeUser, unfreezeUser, resetUserPassword, getActiveStats } from '@/api/admin/user'
import { ROLES, directionLabel } from '@/constants/dict'
import { fmtDate, fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const active = ref(null)
const drawer = ref(false)
const detail = ref(null)

const query = reactive({ keyword: '', role: '', status: null, pageNum: 1, pageSize: 10 })

function roleMeta(role) {
  return ROLES.find(r => r.value === role) || { label: role, type: 'info' }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getUserPage({
      keyword: query.keyword || undefined,
      role: query.role || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadActive() {
  const res = await getActiveStats()
  active.value = res.data
}

async function openDetail(row) {
  const res = await getUserDetail(row.id)
  detail.value = res.data
  drawer.value = true
}

async function freeze(row) {
  await freezeUser(row.id)
  ElMessage.success(`已冻结 ${row.nickname || row.username}`)
  loadList()
  loadActive()
}

async function unfreeze(row) {
  await unfreezeUser(row.id)
  ElMessage.success(`已解封 ${row.nickname || row.username}`)
  loadList()
  loadActive()
}

async function resetPwd(row) {
  await resetUserPassword(row.id)
  ElMessage.success(`已将 ${row.nickname || row.username} 的密码重置为 123456`)
}

onMounted(() => {
  loadList()
  loadActive()
})
</script>

<style scoped>
.user-manage { display: flex; flex-direction: column; gap: 20px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.mini-stat {
  background: var(--bg-card); border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg); box-shadow: var(--shadow-card);
  padding: 18px 22px; display: flex; flex-direction: column; gap: 4px;
}
.mini-value { font-size: 26px; font-weight: 700; color: var(--text-title); font-variant-numeric: tabular-nums; }
.mini-label { font-size: 12px; color: var(--text-sub); }
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.u-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.u-avatar.big { width: 64px; height: 64px; font-size: 26px; margin: 0 auto; }
.u-name { font-size: 14px; font-weight: 600; color: var(--text-title); }
.u-sub { font-size: 12px; color: var(--text-placeholder); }
.muted { font-size: 12px; color: var(--text-placeholder); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
.d-head { text-align: center; margin-bottom: 18px; }
.d-name { font-size: 18px; font-weight: 700; color: var(--text-title); margin-top: 10px; }
.d-stats { display: flex; justify-content: space-around; margin-bottom: 18px; }
.d-stat { display: flex; flex-direction: column; align-items: center; gap: 2px; font-size: 12px; color: var(--text-sub); }
.d-num { font-size: 22px; font-weight: 700; color: var(--primary); font-variant-numeric: tabular-nums; }
@media (max-width: 1200px) { .stat-row { grid-template-columns: repeat(2, 1fr); } }
</style>
