<template>
  <div class="notice-page">
    <AppCard title="系统公告">
      <el-skeleton v-if="loading" :rows="4" animated />
      <template v-else>
        <div v-if="list.length === 0">
          <EmptyState description="暂无公告" />
        </div>
        <div v-else class="notice-list">
          <div v-for="n in list" :key="n.id" class="notice-row" @click="openNotice(n)">
            <el-tag :type="typeMeta(n.type).type" effect="light" round>{{ typeMeta(n.type).label }}</el-tag>
            <div class="notice-main">
              <div class="notice-title">{{ n.title }}</div>
              <div class="notice-time">{{ n.publisherName }} 发布于 {{ fmtTime(n.publishTime) }}</div>
            </div>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
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

    <el-dialog v-model="dialog" :title="current?.title" width="560px">
      <div class="notice-content" v-html="contentHtml"></div>
      <template #footer>
        <el-button type="primary" @click="dialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { getNoticePage } from '@/api/notice'
import { NOTICE_TYPES } from '@/constants/dict'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const dialog = ref(false)
const current = ref(null)

const contentHtml = computed(() => (current.value?.content || '').replace(/\n/g, '<br/>'))

function typeMeta(type) {
  return NOTICE_TYPES.find(t => t.value === type) || { label: '通知', type: 'primary' }
}

async function load() {
  loading.value = true
  try {
    const res = await getNoticePage({ pageNum: pageNum.value, pageSize: 10 })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openNotice(n) {
  current.value = n
  dialog.value = true
}

onMounted(load)
</script>

<style scoped>
.notice-page { max-width: 860px; margin: 0 auto; }
.notice-list { display: flex; flex-direction: column; }
.notice-row {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 12px;
  border-bottom: 1px solid var(--gray-100);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--dur-fast);
}
.notice-row:last-child { border-bottom: none; }
.notice-row:hover { background: var(--bg-hover); }
.notice-main { flex: 1; min-width: 0; }
.notice-title { font-size: 15px; font-weight: 600; color: var(--text-title); }
.notice-time { font-size: 12px; color: var(--text-placeholder); margin-top: 4px; }
.arrow { color: var(--text-placeholder); }
.notice-content { font-size: 14px; line-height: 1.9; color: var(--text-main); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
