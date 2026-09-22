<template>
  <div class="log-page">
    <AppCard title="操作日志（仅超级管理员可见）">
      <div class="filter-row">
        <el-input v-model="query.username" placeholder="操作人" clearable style="width: 160px" @keyup.enter="load" @clear="load" />
        <el-select v-model="query.module" placeholder="模块" clearable style="width: 140px" @change="load">
          <el-option v-for="m in MODULES" :key="m" :label="m" :value="m" />
        </el-select>
        <el-select v-model="query.result" placeholder="结果" clearable style="width: 110px" @change="load">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="load"
        />
        <el-button type="primary" round :icon="Search" @click="load">查询</el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="username" label="操作人" width="110" align="center" />
        <el-table-column prop="module" label="模块" width="110" align="center" />
        <el-table-column prop="operation" label="操作" min-width="140" />
        <el-table-column label="请求" min-width="220">
          <template #default="{ row }">
            <span class="req-method" :class="'m-' + row.requestMethod.toLowerCase()">{{ row.requestMethod }}</span>
            <span class="req-url">{{ row.requestUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small" round>
              {{ row.result === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="120" align="center" />
        <el-table-column label="耗时" width="90" align="center">
          <template #default="{ row }">{{ row.costTime }} ms</template>
        </el-table-column>
        <el-table-column label="时间" width="160" align="center">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="详情" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" :total="total" :page-size="query.pageSize" layout="total, prev, pager, next" background @current-change="load" />
      </div>
    </AppCard>

    <el-dialog v-model="detailDialog" title="操作详情" width="560px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="操作人">{{ current.username }}（{{ current.role }}）</el-descriptions-item>
        <el-descriptions-item label="模块 / 操作">{{ current.module }} / {{ current.operation }}</el-descriptions-item>
        <el-descriptions-item label="方法签名">{{ current.method }}</el-descriptions-item>
        <el-descriptions-item label="请求">{{ current.requestMethod }} {{ current.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="参数">{{ current.params || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="current.result === 1 ? 'success' : 'danger'" size="small">{{ current.result === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="current.errorMsg" label="失败原因">{{ current.errorMsg }}</el-descriptions-item>
        <el-descriptions-item label="IP / 耗时">{{ current.ip }} / {{ current.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="时间">{{ fmtTime(current.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getLogPage } from '@/api/admin/system'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'

const MODULES = ['用户管理', '内容审核', '公告管理', '敏感词管理', '权限管理']

const loading = ref(true)
const list = ref([])
const total = ref(0)
const dateRange = ref(null)
const detailDialog = ref(false)
const current = ref(null)

const query = reactive({ username: '', module: '', result: null, pageNum: 1, pageSize: 10 })

async function load() {
  loading.value = true
  try {
    const res = await getLogPage({
      username: query.username || undefined,
      module: query.module || undefined,
      result: query.result ?? undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function showDetail(row) {
  current.value = row
  detailDialog.value = true
}

onMounted(load)
</script>

<style scoped>
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.req-method {
  font-family: var(--font-mono);
  font-size: 11px; font-weight: 700;
  padding: 2px 8px; border-radius: 6px; margin-right: 8px;
}
.m-get { background: var(--success-bg); color: var(--success); }
.m-post { background: var(--primary-bg); color: var(--primary); }
.m-put { background: var(--warning-bg); color: var(--warning); }
.m-delete { background: var(--danger-bg); color: var(--danger); }
.req-url { font-family: var(--font-mono); font-size: 12px; color: var(--text-sub); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
