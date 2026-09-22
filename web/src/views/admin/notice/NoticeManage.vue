<template>
  <div class="notice-manage">
    <AppCard title="公告管理">
      <template #extra>
        <el-button type="primary" round :icon="Plus" @click="openEdit(null)">发布公告</el-button>
      </template>

      <div class="filter-row">
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="load">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下线" :value="2" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="公告标题关键词" clearable style="width: 220px" @keyup.enter="load" @clear="load" />
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column label="公告标题" min-width="200">
          <template #default="{ row }"><span class="row-title">{{ row.title }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="typeMeta(row.type).type" size="small" round effect="light">{{ typeMeta(row.type).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" size="small" round>{{ statusMeta(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="110" align="center" />
        <el-table-column label="发布时间" width="150" align="center">
          <template #default="{ row }">{{ row.publishTime ? fmtTime(row.publishTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="已读情况" width="110" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showReadStats(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="offline(row)">下线</el-button>
            <el-popconfirm title="确定删除该公告？" @confirm="removeOne(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" :total="total" :page-size="10" layout="total, prev, pager, next" background @current-change="load" />
      </div>
    </AppCard>

    <!-- 发布/编辑公告弹窗 -->
    <el-dialog v-model="dialog" :title="editId ? '编辑公告' : '发布公告'" width="680px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" maxlength="128" show-word-limit placeholder="如：第 12 周上机实训安排" />
        </el-form-item>
        <el-form-item label="公告类型" required>
          <el-radio-group v-model="form.type">
            <el-radio-button value="notice">编程学习通知</el-radio-button>
            <el-radio-button value="maintenance">系统维护通知</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="公告正文…发布后，所有学生登录时会收到弹窗推送" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button @click="save(0)">存为草稿</el-button>
        <el-button type="primary" :loading="saving" @click="save(1)">立即发布并推送</el-button>
      </template>
    </el-dialog>

    <!-- 已读统计 -->
    <el-dialog v-model="statsDialog" title="公告阅读情况" width="420px">
      <div v-if="readStats" class="stats-body">
        <div class="stats-item"><span class="stats-num">{{ readStats.readCount }}</span><span>已读人数</span></div>
        <div class="stats-item"><span class="stats-num warn">{{ readStats.unreadCount }}</span><span>未读人数</span></div>
        <div class="stats-item"><span class="stats-num">{{ readStats.totalStudents }}</span><span>学生总数</span></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminNoticePage, publishNotice, updateNotice, offlineNotice, removeNotice, getNoticeReadStats } from '@/api/admin/notice'
import { NOTICE_TYPES } from '@/constants/dict'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const dialog = ref(false)
const saving = ref(false)
const editId = ref(null)
const statsDialog = ref(false)
const readStats = ref(null)

const query = reactive({ status: null, keyword: '', pageNum: 1, pageSize: 10 })
const form = reactive({ title: '', content: '', type: 'notice' })

function typeMeta(type) {
  return NOTICE_TYPES.find(t => t.value === type) || { label: '通知', type: 'primary' }
}

function statusMeta(status) {
  return { 0: { label: '草稿', type: 'info' }, 1: { label: '已发布', type: 'success' }, 2: { label: '已下线', type: 'warning' } }[status] || { label: '未知', type: 'info' }
}

async function load() {
  loading.value = true
  try {
    const res = await getAdminNoticePage({ status: query.status ?? undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editId.value = row ? row.id : null
  form.title = row ? row.title : ''
  form.content = row ? row.content : ''
  form.type = row ? row.type : 'notice'
  dialog.value = true
}

async function save(status) {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写公告标题与内容')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, status }
    if (editId.value) {
      await updateNotice(editId.value, payload)
      ElMessage.success('公告已更新')
    } else {
      await publishNotice(payload)
      ElMessage.success(status === 1 ? '公告已发布，学生登录时将收到弹窗推送' : '草稿已保存')
    }
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function offline(row) {
  await offlineNotice(row.id)
  ElMessage.success('公告已下线，学生端不再展示')
  load()
}

async function removeOne(row) {
  await removeNotice(row.id)
  ElMessage.success('公告已删除')
  load()
}

async function showReadStats(row) {
  const res = await getNoticeReadStats(row.id)
  readStats.value = res.data
  statsDialog.value = true
}

onMounted(load)
</script>

<style scoped>
.filter-row { display: flex; gap: 12px; margin-bottom: 16px; }
.row-title { font-weight: 600; color: var(--text-title); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
.stats-body { display: flex; justify-content: space-around; padding: 10px 0 20px; }
.stats-item { display: flex; flex-direction: column; align-items: center; gap: 6px; font-size: 13px; color: var(--text-sub); }
.stats-num { font-size: 28px; font-weight: 700; color: var(--success); font-variant-numeric: tabular-nums; }
.stats-num.warn { color: var(--warning); }
</style>
