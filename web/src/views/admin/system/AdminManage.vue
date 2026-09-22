<template>
  <div class="admin-manage">
    <AppCard title="管理员账号管理（仅超级管理员）">
      <template #extra>
        <el-button type="primary" round :icon="Plus" @click="openAdd">新增教师管理员</el-button>
      </template>

      <el-alert type="info" :closable="false" class="tip">
        普通管理员（教师）可进行用户管理、内容审核、大屏查看与公告管理；仅超级管理员可管理管理员账号与查看操作日志。
      </el-alert>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column label="管理员" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="u-avatar">{{ (row.nickname || row.username)[0] }}</div>
              <div>
                <div class="u-name">{{ row.nickname }}</div>
                <div class="u-sub">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'success'" size="small" round>
              {{ row.role === 'admin' ? '超级管理员' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" round>
              {{ row.status === 1 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150" align="center">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <template v-if="row.username !== 'admin'">
              <el-popconfirm title="将该管理员密码重置为 123456？" @confirm="resetPwd(row)">
                <template #reference><el-button link type="warning" size="small">重置密码</el-button></template>
              </el-popconfirm>
              <el-popconfirm title="确定删除该管理员账号？" @confirm="removeOne(row)">
                <template #reference><el-button link type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
            <span v-else class="muted">内置账号</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="pageNum" :total="total" :page-size="10" layout="total, prev, pager, next" background @current-change="load" />
      </div>
    </AppCard>

    <el-dialog v-model="dialog" title="新增教师管理员" width="440px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="3-20 位字母/数字/下划线" maxlength="20" />
        </el-form-item>
        <el-form-item label="昵称（如：张老师）" required>
          <el-input v-model="form.nickname" maxlength="20" />
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input v-model="form.password" type="password" show-password placeholder="6-20 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminPage, addAdmin, resetAdminPassword, removeAdmin } from '@/api/admin/system'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const dialog = ref(false)
const saving = ref(false)
const form = reactive({ username: '', nickname: '', password: '' })

async function load() {
  loading.value = true
  try {
    const res = await getAdminPage({ pageNum: pageNum.value, pageSize: 10 })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  form.username = ''
  form.nickname = ''
  form.password = ''
  dialog.value = true
}

async function save() {
  if (!form.username || !form.nickname || !form.password) {
    ElMessage.warning('请完整填写用户名、昵称与初始密码')
    return
  }
  saving.value = true
  try {
    await addAdmin(form)
    ElMessage.success('教师管理员账号已创建')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function resetPwd(row) {
  await resetAdminPassword(row.id, '123456')
  ElMessage.success(`已将 ${row.nickname} 的密码重置为 123456`)
}

async function removeOne(row) {
  await removeAdmin(row.id)
  ElMessage.success('管理员账号已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.tip { margin-bottom: 16px; border-radius: var(--radius-md); }
.user-cell { display: flex; align-items: center; gap: 10px; }
.u-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.u-name { font-size: 14px; font-weight: 600; color: var(--text-title); }
.u-sub { font-size: 12px; color: var(--text-placeholder); }
.muted { font-size: 12px; color: var(--text-placeholder); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
