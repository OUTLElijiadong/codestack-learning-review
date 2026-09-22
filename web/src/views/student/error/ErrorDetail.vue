<template>
  <div class="error-detail" v-loading="loading">
    <template v-if="detail">
      <div class="back-row">
        <el-button round :icon="Back" text @click="goBack">返回错题本</el-button>
      </div>
      <AppCard>
        <div class="detail-head">
          <div class="head-main">
            <div class="head-badges">
              <span class="err-badge" :class="'err-badge--' + detail.errorType">{{ errorTypeLabel(detail.errorType) }}</span>
              <span class="tech-chip">{{ detail.techDirection }}</span>
              <el-tag v-if="detail.auditStatus === 0" type="warning" size="small" round>待审核</el-tag>
              <el-tag v-if="detail.auditStatus === 2" type="info" size="small" round>已下架</el-tag>
            </div>
            <h2 class="detail-title">{{ detail.title }}</h2>
            <div class="detail-meta">
              收录于 {{ fmtTime(detail.createTime) }}
              <template v-if="detail.reviewCount > 0"> · 已复习 {{ detail.reviewCount }} 次</template>
              <template v-if="detail.lastReviewTime"> · 最近复习 {{ fromNow(detail.lastReviewTime) }}</template>
            </div>
          </div>
          <div class="head-actions">
            <el-button round :icon="Top" :type="detail.isTop === 1 ? 'primary' : 'default'" @click="toggleTopOne">
              {{ detail.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button round :icon="Star" :type="detail.isFavorite === 1 ? 'warning' : 'default'" @click="toggleFav">
              {{ detail.isFavorite === 1 ? '已收藏' : '收藏' }}
            </el-button>
            <el-button round type="primary" :icon="Edit" @click="$router.push(`/student/error/edit/${detail.id}`)">编辑</el-button>
            <el-popconfirm title="删除后进入归档箱，可恢复" @confirm="archiveOne">
              <template #reference>
                <el-button round type="danger" plain :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>

        <div v-if="detail.tags && detail.tags.length" class="detail-tags">
          <span
            v-for="tag in detail.tags"
            :key="tag.id"
            class="tag-chip"
            :style="tagStyle(tag.id)"
          >#{{ tag.name }}</span>
        </div>
      </AppCard>

      <AppCard v-if="detail.errorCode" title="错误代码片段">
        <CodeBlock :code="detail.errorCode" :language="guessLang" />
      </AppCard>

      <AppCard v-if="detail.errorMsg" title="报错信息">
        <div class="error-msg-block">{{ detail.errorMsg }}</div>
      </AppCard>

      <AppCard v-if="detail.solution" title="正确解决方案" class="solution-card">
        <div class="solution-extra">
          <el-button type="success" plain round size="small" @click="askDoubao">
            用 AI（豆包）再看看这道题 →
          </el-button>
        </div>
        <CodeBlock :code="detail.solution" :language="guessLang" />
      </AppCard>

      <AppCard v-if="detail.images && detail.images.length" title="运行 / 报错截图">
        <div class="image-row">
          <el-image
            v-for="(img, i) in detail.images"
            :key="i"
            :src="img"
            :preview-src-list="detail.images"
            :initial-index="i"
            fit="cover"
            class="shot"
            preview-teleported
          />
        </div>
      </AppCard>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Top, Star, Edit, Delete, Back } from '@element-plus/icons-vue'
import { getMistakeDetail, toggleTop, toggleFavorite, archiveMistake } from '@/api/mistake'
import { errorTypeLabel, tagColor } from '@/constants/dict'
import { fmtTime, fromNow } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import CodeBlock from '@/components/CodeBlock.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const detail = ref(null)

// 按技术方向猜代码高亮语言
const guessLang = computed(() => {
  const map = { Vue: 'javascript', SpringBoot: 'java', MySQL: 'sql', 算法: 'java' }
  return map[detail.value?.techDirection] || 'plaintext'
})

function tagStyle(id) {
  const c = tagColor(id)
  return { color: c.text, background: c.bg }
}

async function load() {
  loading.value = true
  try {
    const res = await getMistakeDetail(route.params.id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

async function toggleTopOne() {
  await toggleTop(detail.value.id)
  detail.value.isTop = detail.value.isTop === 1 ? 0 : 1
}

async function toggleFav() {
  await toggleFavorite(detail.value.id)
  detail.value.isFavorite = detail.value.isFavorite === 1 ? 0 : 1
}

async function archiveOne() {
  await archiveMistake(detail.value.id)
  ElMessage.success('已移入归档箱')
  router.push('/student/error')
}

function goBack() {
  router.push('/student/error')
}

/** 把错题（标题+报错+方案）拼成问句，跳转豆包让它给出分析 */
function askDoubao() {
  const prompt = `我的一道编程错题，帮我分析并给出更优解：\n\n【标题】${detail.value.title}\n【方向】${detail.value.techDirection} / ${errorTypeLabel(detail.value.errorType)}\n【报错】${detail.value.errorMsg || ''}\n【我的方案】${detail.value.solution || ''}`
  window.open('https://www.doubao.com/chat/?q=' + encodeURIComponent(prompt), '_blank')
}

onMounted(load)
</script>

<style scoped>
.error-detail { max-width: 960px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.detail-head { display: flex; justify-content: space-between; gap: 20px; flex-wrap: wrap; }
.head-badges { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.tech-chip {
  font-size: 12px; font-weight: 600; color: var(--primary);
  background: var(--primary-bg); border: 1px solid var(--primary-border);
  border-radius: 999px; padding: 2px 10px;
}
.detail-title { font-size: 22px; font-weight: 700; color: var(--text-title); margin: 0 0 8px; }
.detail-meta { font-size: 13px; color: var(--text-sub); }
.head-actions { display: flex; gap: 10px; align-items: flex-start; flex-wrap: wrap; }
.detail-tags { margin-top: 14px; }
.solution-card :deep(.app-card-title)::before { background: linear-gradient(180deg, #10B981, #34D399); }
.back-row { margin-bottom: -4px; }
.solution-extra { margin-bottom: 10px; display: flex; justify-content: flex-end; }
.error-msg-block {
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.8;
  color: #B91C1C;
  white-space: pre-wrap;
  word-break: break-all;
}
.image-row { display: flex; gap: 14px; flex-wrap: wrap; }
.shot {
  width: 220px; height: 140px;
  border-radius: var(--radius-md);
  border: 1px solid var(--gray-200);
  cursor: zoom-in;
}
</style>
