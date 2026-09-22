<template>
  <div class="community-page">
    <AppCard pad="16px 20px">
      <div class="toolbar">
        <el-tabs v-model="scope" @tab-change="loadList">
          <el-tab-pane label="全部问题" name="all" />
          <el-tab-pane label="待解答" name="unsolved" />
          <el-tab-pane label="我的提问" name="mine" />
        </el-tabs>
        <div class="toolbar-right">
          <el-input
            v-model="query.keyword"
            placeholder="搜索问题…"
            clearable
            :prefix-icon="Search"
            style="width: 220px"
            @keyup.enter="loadList"
            @clear="loadList"
          />
          <el-select v-model="query.techDirection" placeholder="技术方向" clearable style="width: 130px" @change="loadList">
            <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
          </el-select>
          <button class="ask-btn" @click="askDialog = true">
            <el-icon><EditPen /></el-icon>&nbsp;我要提问
          </button>
          <el-button round @click="$router.push('/student/community/mine')">我的问答</el-button>
        </div>
      </div>
    </AppCard>

    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <div v-if="list.length === 0">
        <AppCard>
          <EmptyState description="这里静悄悄的，提出你的第一个编程问题吧" btn-text="我要提问" @action="askDialog = true" />
        </AppCard>
      </div>
      <div v-else class="q-list">
        <div
          v-for="q in list"
          :key="q.id"
          class="q-card"
          @click="$router.push(`/student/community/detail/${q.id}`)"
        >
          <!-- 左侧统计竖列 -->
          <div class="q-stats">
            <div class="q-stat">
              <span class="q-stat-num" :class="{ solved: q.status === 1 }">{{ q.answerCount }}</span>
              <span class="q-stat-label">回复</span>
            </div>
            <div class="q-stat">
              <span class="q-stat-num muted">{{ q.viewCount }}</span>
              <span class="q-stat-label">浏览</span>
            </div>
          </div>
          <!-- 右侧主体 -->
          <div class="q-main">
            <div class="q-title-row">
              <span class="q-title">{{ q.title }}</span>
              <el-tag v-if="q.status === 1" type="success" size="small" round>已解决</el-tag>
              <el-tag v-else size="small" type="info" round effect="plain">待解答</el-tag>
            </div>
            <div class="q-foot">
              <span class="tech-chip">{{ q.techDirection }}</span>
              <div class="q-author">
                <div class="q-avatar">{{ (q.nickname || '同')[0] }}</div>
                <span>{{ q.nickname }}</span>
                <span class="q-time">· {{ fromNow(q.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

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
    </template>

    <!-- 发布问题弹窗 -->
    <el-dialog v-model="askDialog" title="发布编程问题" width="680px" destroy-on-close>
      <el-form :model="askForm" label-position="top">
        <el-form-item label="问题标题" required>
          <el-input v-model="askForm.title" placeholder="一句话描述你的问题，如：Vue3 watch 监听对象不触发" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="技术方向" required>
          <el-select v-model="askForm.techDirection" style="width: 200px">
            <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题详情（支持贴代码，用 ``` 包裹）">
          <el-input v-model="askForm.content" type="textarea" :rows="6" placeholder="描述你做了什么、期望什么、实际报什么错…" class="code-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="askDialog = false">取消</el-button>
        <el-button type="primary" :loading="asking" @click="submitQuestion">发布问题</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, EditPen } from '@element-plus/icons-vue'
import { getQuestionPage, publishQuestion } from '@/api/community'
import { TECH_DIRECTIONS } from '@/constants/dict'
import { fromNow } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const scope = ref('all')
const askDialog = ref(false)
const asking = ref(false)

const query = reactive({ keyword: '', techDirection: '', pageNum: 1, pageSize: 10 })
const askForm = reactive({ title: '', content: '', techDirection: 'Vue' })

async function loadList() {
  loading.value = true
  try {
    const res = await getQuestionPage({
      keyword: query.keyword || undefined,
      techDirection: query.techDirection || undefined,
      status: scope.value === 'unsolved' ? 0 : undefined,
      my: scope.value === 'mine' ? 1 : undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function submitQuestion() {
  if (!askForm.title.trim()) {
    ElMessage.warning('请输入问题标题')
    return
  }
  asking.value = true
  try {
    await publishQuestion(askForm)
    ElMessage.success('问题发布成功，等待同学和老师来解答')
    askDialog.value = false
    askForm.title = ''
    askForm.content = ''
    loadList()
  } catch (e) { /* 拦截器已提示（敏感词拦截会在这里提示） */ } finally {
    asking.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.community-page { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; }
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.ask-btn {
  display: inline-flex; align-items: center;
  height: 34px; padding: 0 18px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.ask-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }

.q-list { display: flex; flex-direction: column; gap: 14px; }
.q-card {
  display: flex; gap: 18px;
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 16px 20px;
  cursor: pointer;
  transition: all var(--dur-base) var(--ease-out);
}
.q-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-card-hover); }
.q-stats { display: flex; flex-direction: column; gap: 8px; width: 56px; flex-shrink: 0; }
.q-stat {
  display: flex; flex-direction: column; align-items: center;
  background: var(--gray-50); border-radius: var(--radius-btn);
  padding: 8px 0;
}
.q-stat-num { font-size: 16px; font-weight: 700; color: var(--text-title); font-variant-numeric: tabular-nums; }
.q-stat-num.solved { color: var(--success); }
.q-stat-num.muted { color: var(--text-placeholder); }
.q-stat-label { font-size: 11px; color: var(--text-sub); margin-top: 2px; }
.q-main { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between; }
.q-title-row { display: flex; align-items: center; gap: 10px; }
.q-title {
  font-size: 16px; font-weight: 600; color: var(--text-title);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.q-card:hover .q-title { color: var(--primary); }
.q-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.tech-chip { font-size: 12px; font-weight: 600; color: var(--primary); background: var(--primary-bg); border-radius: 999px; padding: 2px 10px; }
.q-author { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-sub); }
.q-avatar {
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.q-time { color: var(--text-placeholder); }
.pager { display: flex; justify-content: center; }
.code-input :deep(textarea) { font-family: var(--font-mono); font-size: 13px; line-height: 1.7; }
</style>
