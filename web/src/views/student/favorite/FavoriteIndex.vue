<template>
  <div class="favorite-page" v-loading="loading">
    <AppCard title="收藏总览">
      <template #extra>
        <span class="fav-count">共 {{ total }} 道收藏</span>
      </template>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="还没有收藏任何错题" btn-text="去错题本逛逛" @action="$router.push('/student/error')" />
      </div>
      <div v-else class="fav-grid">
        <div
          v-for="(item, idx) in list"
          :key="item.id"
          class="fav-card rise-in"
          :style="{ '--d': idx * 50 + 'ms' }"
          @click="$router.push(`/student/error/detail/${item.id}`)"
        >
          <div class="fav-top">
            <span class="err-badge" :class="'err-badge--' + item.errorType">{{ errorTypeLabel(item.errorType) }}</span>
            <el-icon class="fav-star" color="#F59E0B"><StarFilled /></el-icon>
          </div>
          <div class="fav-title">{{ item.title }}</div>
          <div class="fav-msg">{{ item.errorMsg }}</div>
          <div class="fav-meta">
            <span class="tech-tag">{{ item.techDirection }}</span>
            <span class="fav-time">{{ fromNow(item.createTime) }}</span>
          </div>
        </div>
      </div>
      <div class="fav-page-bar" v-if="total > pageSize">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="load"
        />
      </div>
    </AppCard>
  </div>
</template>

<script setup>
// 收藏总览：所有 is_favorite=1 的错题的独立聚合页
import { ref, onMounted } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'
import { getMistakePage } from '@/api/mistake'
import { errorTypeLabel } from '@/constants/dict'
import { fromNow } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12

async function load(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const res = await getMistakePage({ pageNum: page, pageSize, onlyFavorite: 1 })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))
</script>

<style scoped>
.favorite-page { max-width: 1080px; margin: 0 auto; }
.fav-count { font-size: 13px; color: var(--text-sub); }
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}
.fav-card {
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  cursor: pointer;
  transition: all var(--dur-fast) var(--ease-out);
  background: var(--bg-card);
}
.fav-card:hover {
  transform: translateY(-3px);
  border-color: var(--primary-border);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.12);
}
.fav-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.fav-title {
  font-size: 14px; font-weight: 600; color: var(--text-title);
  line-height: 1.5; margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.fav-msg {
  font-size: 12px; color: var(--text-sub); line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  margin-bottom: 10px;
}
.fav-meta { display: flex; justify-content: space-between; align-items: center; }
.tech-tag {
  font-size: 11px; color: var(--primary);
  background: var(--primary-bg); border-radius: 4px; padding: 1px 7px;
}
.fav-time { font-size: 11px; color: var(--text-placeholder); }
.fav-page-bar { display: flex; justify-content: center; margin-top: 20px; }
</style>
