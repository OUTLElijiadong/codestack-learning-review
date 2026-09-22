<template>
  <div class="archive-page">
    <AppCard title="错题归档箱">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索归档的错题…"
          clearable
          :prefix-icon="Search"
          style="width: 240px"
          @keyup.enter="load"
          @clear="load"
        />
      </template>

      <el-alert type="info" :closable="false" class="tip">
        删除的错题会进入归档箱，可以随时恢复；彻底删除后不可找回，请谨慎操作。
      </el-alert>

      <el-skeleton v-if="loading" :rows="5" animated />
      <template v-else>
        <el-table v-if="list.length" :data="list" stripe>
          <el-table-column label="错题标题" min-width="220">
            <template #default="{ row }">
              <span class="row-title">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="techDirection" label="技术方向" width="110" align="center" />
          <el-table-column label="错误类型" width="110" align="center">
            <template #default="{ row }">
              <span class="err-badge" :class="'err-badge--' + row.errorType">{{ errorTypeLabel(row.errorType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="归档时间" width="160" align="center">
            <template #default="{ row }">{{ fmtTime(row.updateTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="restoreOne(row)">恢复</el-button>
              <el-popconfirm title="彻底删除后不可找回，确定吗？" confirm-button-type="danger" @confirm="foreverOne(row)">
                <template #reference>
                  <el-button link type="danger">彻底删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <EmptyState v-else description="归档箱是空的，没有被删除的错题" />
        <div class="pager">
          <el-pagination
            v-model:current-page="pageNum"
            :total="total"
            :page-size="10"
            layout="total, prev, pager, next"
            background
            @current-change="load"
          />
        </div>
      </template>
    </AppCard>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getArchivePage, restoreMistake, deleteMistakeForever } from '@/api/mistake'
import { errorTypeLabel } from '@/constants/dict'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const keyword = ref('')
const pageNum = ref(1)

async function load() {
  loading.value = true
  try {
    const res = await getArchivePage({ keyword: keyword.value || undefined, pageNum: pageNum.value, pageSize: 10 })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function restoreOne(row) {
  await restoreMistake(row.id)
  ElMessage.success('已恢复到错题本')
  load()
}

async function foreverOne(row) {
  await deleteMistakeForever(row.id)
  ElMessage.success('已彻底删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.archive-page { max-width: 1080px; margin: 0 auto; }
.tip { margin-bottom: 16px; border-radius: var(--radius-md); }
.row-title { font-weight: 500; color: var(--text-title); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
