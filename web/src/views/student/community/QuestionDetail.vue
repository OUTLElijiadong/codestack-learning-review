<template>
  <div class="question-detail" v-loading="loading">
    <template v-if="detail">
      <!-- 问题主体 -->
      <AppCard>
        <div class="q-head">
          <div class="q-head-main">
            <div class="q-badges">
              <span class="tech-chip">{{ detail.techDirection }}</span>
              <el-tag v-if="detail.status === 1" type="success" size="small" round>已解决</el-tag>
              <el-tag v-else size="small" type="info" round effect="plain">待解答</el-tag>
            </div>
            <h2 class="q-title">{{ detail.title }}</h2>
            <div class="q-meta">
              <div class="q-avatar">{{ (detail.nickname || '同')[0] }}</div>
              <span>{{ detail.nickname }}</span>
              <span class="muted">发布于 {{ fmtTime(detail.createTime) }} · {{ detail.viewCount }} 次浏览</span>
            </div>
          </div>
          <div class="q-actions">
            <el-button v-if="detail.mine && detail.status === 0" type="success" round plain @click="solveIt">标记已解决</el-button>
            <el-popconfirm v-if="detail.mine" title="确定删除该提问？" @confirm="removeIt">
              <template #reference>
                <el-button type="danger" round plain>删除</el-button>
              </template>
            </el-popconfirm>
            <el-button round @click="$router.back()">返回</el-button>
          </div>
        </div>
        <div class="q-content" v-html="contentHtml"></div>
      </AppCard>

      <!-- 评论区 -->
      <AppCard :title="`全部回复（${detail.answerCount}）`">
        <div v-if="!detail.comments || detail.comments.length === 0">
          <EmptyState description="还没有回复，来抢沙发" />
        </div>
        <div v-else class="comment-list">
          <div v-for="c in detail.comments" :key="c.id" class="comment-item">
            <div class="c-avatar">{{ (c.nickname || '同')[0] }}</div>
            <div class="c-main">
              <div class="c-head">
                <span class="c-name">{{ c.nickname }}</span>
                <el-tag v-if="c.role === 'teacher'" size="small" type="success" round effect="dark">教师</el-tag>
                <el-tag v-if="c.role === 'admin'" size="small" type="danger" round effect="dark">管理员</el-tag>
                <span class="c-time">{{ fromNow(c.createTime) }}</span>
                <span class="c-ops">
                  <el-button link type="primary" size="small" @click="openReply(c)">回复</el-button>
                  <el-popconfirm v-if="c.userId === userStore.userInfo?.id" title="删除这条回复？" @confirm="removeCommentOne(c)">
                    <template #reference><el-button link type="danger" size="small">删除</el-button></template>
                  </el-popconfirm>
                </span>
              </div>
              <p class="c-content">{{ c.content }}</p>
              <!-- 二级回复 -->
              <div v-if="c.children && c.children.length" class="children">
                <div v-for="child in c.children" :key="child.id" class="child-item">
                  <span class="c-name">{{ child.nickname }}</span>
                  <el-tag v-if="child.role === 'teacher'" size="small" type="success" round effect="dark">教师</el-tag>
                  <span v-if="child.replyNickname" class="reply-to">回复 <b>@{{ child.replyNickname }}</b></span>
                  <span class="c-content-inline">{{ child.content }}</span>
                  <span class="c-time">{{ fromNow(child.createTime) }}</span>
                  <el-button link type="primary" size="small" @click="openReply(child)">回复</el-button>
                  <el-popconfirm v-if="child.userId === userStore.userInfo?.id" title="删除这条回复？" @confirm="removeCommentOne(child)">
                    <template #reference><el-button link type="danger" size="small">删除</el-button></template>
                  </el-popconfirm>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部回复框 -->
        <div class="reply-box">
          <div v-if="replyTarget" class="reply-target">
            回复 <b>@{{ replyTarget.nickname }}</b>
            <el-icon class="reply-cancel" @click="replyTarget = null"><Close /></el-icon>
          </div>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="3"
            :placeholder="replyTarget ? `回复 @${replyTarget.nickname}…` : '写下你的解答，帮助同学…'"
            maxlength="1000"
            show-word-limit
          />
          <div class="reply-actions">
            <el-button type="primary" round :loading="replying" @click="submitReply">发表回复</el-button>
          </div>
        </div>
      </AppCard>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { getQuestionDetail, replyQuestion, markSolved, removeQuestion, removeComment } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { fmtTime, fromNow } from '@/utils/format'
import { injectCopyButtons } from '@/utils/richCode'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import hljs from 'highlight.js/lib/common'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const detail = ref(null)
const replyContent = ref('')
const replyTarget = ref(null)
const replying = ref(false)

// 问题内容：把 ``` 代码段渲染成高亮代码块，其余按纯文本换行展示
const contentHtml = computed(() => {
  const raw = detail.value?.content || ''
  return renderContent(raw)
})

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function renderContent(raw) {
  // 支持 ```lang ... ``` 代码段；其余转义后按换行转 <br>
  const parts = raw.split(/```(\w*)\n?([\s\S]*?)```/g)
  let html = ''
  for (let i = 0; i < parts.length; i++) {
    if (i % 3 === 0) {
      html += `<p>${escapeHtml(parts[i]).replace(/\n/g, '<br/>')}</p>`
    } else if (i % 3 === 2) {
      const lang = parts[i - 1] || 'plaintext'
      const code = parts[i]
      let highlighted
      try {
        highlighted = hljs.getLanguage(lang)
          ? hljs.highlight(code, { language: lang }).value
          : hljs.highlightAuto(code).value
      } catch (e) {
        highlighted = escapeHtml(code)
      }
      html += `<pre class="code-block-dark"><code>${highlighted}</code></pre>`
    }
  }
  return injectCopyButtons(html)
}

async function load() {
  loading.value = true
  try {
    const res = await getQuestionDetail(route.params.id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

function openReply(comment) {
  replyTarget.value = comment
}

async function submitReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    await replyQuestion({
      questionId: detail.value.id,
      content: replyContent.value.trim(),
      parentId: replyTarget.value ? replyTarget.value.id : 0
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyTarget.value = null
    load()
  } catch (e) { /* 拦截器已提示（敏感词拦截会在这里提示） */ } finally {
    replying.value = false
  }
}

async function solveIt() {
  await markSolved(detail.value.id)
  ElMessage.success('已标记为已解决')
  load()
}

async function removeIt() {
  await removeQuestion(detail.value.id)
  ElMessage.success('提问已删除')
  router.push('/student/community')
}

async function removeCommentOne(c) {
  await removeComment(c.id)
  ElMessage.success('回复已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.question-detail { max-width: 960px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.q-head { display: flex; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.q-head-main { flex: 1; min-width: 0; }
.q-badges { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.tech-chip { font-size: 12px; font-weight: 600; color: var(--primary); background: var(--primary-bg); border-radius: 999px; padding: 2px 10px; }
.q-title { font-size: 22px; font-weight: 700; color: var(--text-title); margin: 0 0 10px; }
.q-meta { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--text-sub); }
.q-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.muted { color: var(--text-placeholder); }
.q-actions { display: flex; gap: 10px; align-items: flex-start; }
.q-content {
  margin-top: 16px; padding-top: 16px;
  border-top: 1px solid var(--gray-100);
  font-size: 14px; line-height: 1.9; color: var(--text-main);
}
.q-content :deep(pre) { margin: 12px 0; }

.comment-list { display: flex; flex-direction: column; gap: 18px; }
.comment-item { display: flex; gap: 12px; }
.c-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 14px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.c-main { flex: 1; min-width: 0; }
.c-head { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.c-name { font-size: 14px; font-weight: 600; color: var(--text-title); }
.c-time { font-size: 12px; color: var(--text-placeholder); }
.c-ops { margin-left: auto; }
.c-content { font-size: 14px; color: var(--text-main); line-height: 1.8; margin: 6px 0 0; white-space: pre-wrap; }
.children {
  margin-top: 10px;
  background: var(--gray-50);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  display: flex; flex-direction: column; gap: 8px;
}
.child-item { font-size: 13px; color: var(--text-main); line-height: 1.7; }
.reply-to { color: var(--text-sub); }
.reply-to b { color: var(--primary); }
.c-content-inline { margin: 0 6px; }

.reply-box { margin-top: 20px; border-top: 1px solid var(--gray-100); padding-top: 16px; }
.reply-target {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px; color: var(--text-sub);
  background: var(--primary-bg); border-radius: 999px; padding: 3px 12px;
  margin-bottom: 10px;
}
.reply-target b { color: var(--primary); }
.reply-cancel { cursor: pointer; }
.reply-cancel:hover { color: var(--danger); }
.reply-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
</style>
