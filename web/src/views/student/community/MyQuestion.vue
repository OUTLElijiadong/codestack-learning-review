<template>
  <div class="my-question">
    <AppCard pad="16px 20px">
      <el-tabs v-model="tab" @tab-change="loadAll">
        <el-tab-pane label="我的提问" name="questions" />
        <el-tab-pane label="我的回复" name="replies" />
      </el-tabs>

      <!-- 我的提问 -->
      <template v-if="tab === 'questions'">
        <el-table :data="questions" stripe v-loading="loadingQ" @row-click="(row) => $router.push(`/student/community/detail/${row.id}`)" class="clickable-table">
          <el-table-column label="问题标题" min-width="240">
            <template #default="{ row }">
              <span class="row-title">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="techDirection" label="方向" width="110" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" round>
                {{ row.status === 1 ? '已解决' : '待解答' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="answerCount" label="回复" width="70" align="center" />
          <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
          <el-table-column label="发布时间" width="150" align="center">
            <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <EmptyState v-if="!loadingQ && questions.length === 0" description="你还没有提过问题" btn-text="去提问" @action="$router.push('/student/community')" />
        <div class="pager">
          <el-pagination v-model:current-page="qPage" :total="qTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="loadQuestions" />
        </div>
      </template>

      <!-- 我的回复 -->
      <template v-else>
        <el-table :data="replies" stripe v-loading="loadingR">
          <el-table-column label="回复内容" min-width="260">
            <template #default="{ row }">
              <span class="reply-text">{{ row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column label="所属问题" min-width="220">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/student/community/detail/${row.questionId}`)">
                {{ row.questionTitle }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="回复时间" width="150" align="center">
            <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <EmptyState v-if="!loadingR && replies.length === 0" description="你还没有回复过别人的问题" btn-text="去逛逛问答区" @action="$router.push('/student/community')" />
        <div class="pager">
          <el-pagination v-model:current-page="rPage" :total="rTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="loadReplies" />
        </div>
      </template>
    </AppCard>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getQuestionPage, getMyReplies } from '@/api/community'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const tab = ref('questions')
const questions = ref([])
const replies = ref([])
const qTotal = ref(0)
const rTotal = ref(0)
const qPage = ref(1)
const rPage = ref(1)
const loadingQ = ref(false)
const loadingR = ref(false)

async function loadQuestions() {
  loadingQ.value = true
  try {
    const res = await getQuestionPage({ my: 1, pageNum: qPage.value, pageSize: 10 })
    questions.value = res.data.list || []
    qTotal.value = res.data.total || 0
  } finally {
    loadingQ.value = false
  }
}

async function loadReplies() {
  loadingR.value = true
  try {
    const res = await getMyReplies({ pageNum: rPage.value, pageSize: 10 })
    replies.value = res.data.list || []
    rTotal.value = res.data.total || 0
  } finally {
    loadingR.value = false
  }
}

function loadAll() {
  if (tab.value === 'questions') loadQuestions()
  else loadReplies()
}

onMounted(loadAll)
</script>

<style scoped>
.my-question { max-width: 1080px; margin: 0 auto; }
.row-title { font-weight: 500; color: var(--text-title); }
.clickable-table :deep(tbody tr) { cursor: pointer; }
.reply-text { color: var(--text-main); }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
