<template>
  <div class="note-detail" v-loading="loading">
    <AppCard v-if="note">
      <div class="head">
        <div>
          <span class="note-cat">{{ categoryName }}</span>
          <h2 class="title">{{ note.title }}</h2>
          <div class="meta">
            {{ fmtTime(note.createTime) }} · 浏览 {{ note.viewCount }}
          </div>
        </div>
        <el-button round @click="$router.back()">返回</el-button>
      </div>
      <!-- 富文本渲染区：代码块由全局 editor-content 样式 + highlight.js 着色 -->
      <div class="editor-content note-content" v-html="renderedContent"></div>
    </AppCard>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNoteDetail, getCategoryList } from '@/api/note'
import { fmtTime } from '@/utils/format'
import { injectCopyButtons } from '@/utils/richCode'
import AppCard from '@/components/AppCard.vue'
import hljs from 'highlight.js/lib/common'

const route = useRoute()
const loading = ref(true)
const note = ref(null)
const categories = ref([])
const categoryName = ref('未分类')
const renderedContent = ref('')

onMounted(async () => {
  try {
    const [detail, cats] = await Promise.all([getNoteDetail(route.params.id), getCategoryList()])
    note.value = detail.data
    categories.value = cats.data || []
    const hit = categories.value.find(c => c.id === note.value.categoryId)
    categoryName.value = hit ? hit.name : '未分类'
    renderedContent.value = injectCopyButtons(highlightCodeBlocks(note.value.content || ''))
  } finally {
    loading.value = false
  }
})

/** 把 wangEditor 保存的 <pre><code> 代码块用 highlight.js 着色 */
function highlightCodeBlocks(html) {
  const div = document.createElement('div')
  div.innerHTML = html
  div.querySelectorAll('pre code').forEach((block) => {
    const langClass = Array.from(block.classList).find(c => c.startsWith('language-'))
    const lang = langClass ? langClass.replace('language-', '') : ''
    try {
      if (lang && hljs.getLanguage(lang)) {
        block.innerHTML = hljs.highlight(block.textContent, { language: lang }).value
      } else {
        block.innerHTML = hljs.highlightAuto(block.textContent).value
      }
    } catch (e) { /* 保持原文 */ }
  })
  return div.innerHTML
}
</script>

<style scoped>
.note-detail { max-width: 960px; margin: 0 auto; }
.head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.note-cat {
  font-size: 12px; font-weight: 600; color: var(--primary);
  background: var(--primary-bg); border-radius: 999px; padding: 2px 10px;
}
.title { font-size: 24px; font-weight: 700; color: var(--text-title); margin: 10px 0 8px; }
.meta { font-size: 13px; color: var(--text-sub); }
.note-content {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--gray-100);
  font-size: 14px;
  line-height: 1.9;
  color: var(--text-main);
}
.note-content :deep(img) { max-width: 100%; border-radius: var(--radius-md); }
.note-content :deep(a) { color: var(--primary); }
</style>
