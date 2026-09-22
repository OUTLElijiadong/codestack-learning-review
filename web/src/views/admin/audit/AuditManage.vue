<template>
  <div class="audit-page">
    <AppCard pad="16px 20px">
      <el-tabs v-model="tab" @tab-change="loadAll">
        <!-- 笔记审核 -->
        <el-tab-pane label="笔记审核" name="note">
          <div class="filter-row">
            <el-radio-group v-model="noteStatus" @change="loadNotes">
              <el-radio-button :value="0">待审核</el-radio-button>
              <el-radio-button :value="1">已通过</el-radio-button>
              <el-radio-button :value="2">已下架</el-radio-button>
              <el-radio-button :value="null">全部公开</el-radio-button>
            </el-radio-group>
            <el-input v-model="noteKeyword" placeholder="标题/摘要关键词" clearable style="width: 220px" @keyup.enter="loadNotes" @clear="loadNotes" />
          </div>
          <el-table :data="notes" stripe v-loading="loadingNote">
            <el-table-column label="标题" min-width="200">
              <template #default="{ row }"><span class="row-title">{{ row.title }}</span></template>
            </el-table-column>
            <el-table-column label="摘要" min-width="220">
              <template #default="{ row }"><span class="row-summary">{{ row.summary }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="auditMeta(row.auditStatus).type" size="small" round>{{ auditMeta(row.auditStatus).label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="150" align="center">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="previewNote(row)">查看内容</el-button>
                <el-button v-if="row.auditStatus !== 1" link type="success" size="small" @click="pass('note', row)">通过</el-button>
                <el-button v-if="row.auditStatus !== 2" link type="danger" size="small" @click="reject('note', row)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <EmptyState v-if="!loadingNote && notes.length === 0" description="当前没有需要审核的笔记" />
          <div class="pager">
            <el-pagination v-model:current-page="notePage" :total="noteTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="loadNotes" />
          </div>
        </el-tab-pane>

        <!-- 错题审核 -->
        <el-tab-pane label="错题审核" name="mistake">
          <div class="filter-row">
            <el-radio-group v-model="mistakeStatus" @change="loadMistakes">
              <el-radio-button :value="0">待审核</el-radio-button>
              <el-radio-button :value="1">正常</el-radio-button>
              <el-radio-button :value="2">已下架</el-radio-button>
              <el-radio-button :value="null">全部</el-radio-button>
            </el-radio-group>
            <el-select v-model="mistakeDirection" placeholder="技术方向" clearable style="width: 130px" @change="loadMistakes">
              <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
            </el-select>
            <el-input v-model="mistakeKeyword" placeholder="标题关键词" clearable style="width: 200px" @keyup.enter="loadMistakes" @clear="loadMistakes" />
          </div>
          <el-table :data="mistakes" stripe v-loading="loadingMistake">
            <el-table-column label="错题标题" min-width="220">
              <template #default="{ row }"><span class="row-title">{{ row.title }}</span></template>
            </el-table-column>
            <el-table-column prop="techDirection" label="方向" width="100" align="center" />
            <el-table-column label="错误类型" width="110" align="center">
              <template #default="{ row }">
                <span class="err-badge" :class="'err-badge--' + row.errorType">{{ errorTypeLabel(row.errorType) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="auditMeta(row.auditStatus).type" size="small" round>{{ auditMeta(row.auditStatus).label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="150" align="center">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template #default="{ row }">
                <el-button v-if="row.auditStatus !== 1" link type="success" size="small" @click="pass('mistake', row)">通过</el-button>
                <el-button v-if="row.auditStatus !== 2" link type="danger" size="small" @click="reject('mistake', row)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <EmptyState v-if="!loadingMistake && mistakes.length === 0" description="当前没有需要审核的错题" />
          <div class="pager">
            <el-pagination v-model:current-page="mistakePage" :total="mistakeTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="loadMistakes" />
          </div>
        </el-tab-pane>

        <!-- 敏感词库 -->
        <el-tab-pane label="敏感词库" name="word">
          <div class="filter-row">
            <el-alert type="warning" :closable="false" class="word-tip">
              发布错题 / 笔记 / 问答 / 评论时会自动匹配敏感词并拦截。新增或删除后即时生效。
            </el-alert>
            <div class="word-add">
              <el-input v-model="newWord" placeholder="新增敏感词，如：代考" maxlength="30" style="width: 240px" />
              <el-button type="primary" round @click="addWord">添加</el-button>
            </div>
          </div>
          <el-table :data="words" stripe v-loading="loadingWord">
            <el-table-column prop="word" label="敏感词" min-width="160" />
            <el-table-column label="处理级别" width="130" align="center">
              <template #default="{ row }">
                <el-tag :type="row.level === 1 ? 'danger' : 'warning'" size="small" round>
                  {{ row.level === 1 ? '直接拦截' : '人工复核' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="添加时间" width="160" align="center">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-popconfirm title="删除后该词不再拦截，确定？" @confirm="removeWord(row)">
                  <template #reference><el-button link type="danger" size="small">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="wordPage" :total="wordTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="loadWords" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </AppCard>

    <!-- 笔记内容预览 -->
    <el-dialog v-model="previewDialog" :title="previewData?.title" width="720px">
      <div class="editor-content preview-body" v-html="previewData?.content"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteAuditPage, passNote, rejectNote, getMistakeAuditPage, passMistake, rejectMistake, getSensitiveWordPage, addSensitiveWord, deleteSensitiveWord, getNoteAuditDetail } from '@/api/admin/audit'
import { AUDIT_STATUS, ERROR_TYPES, TECH_DIRECTIONS, errorTypeLabel } from '@/constants/dict'
import { fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const tab = ref('note')
const notes = ref([])
const mistakes = ref([])
const words = ref([])
const noteStatus = ref(0)
const mistakeStatus = ref(0)
const noteKeyword = ref('')
const mistakeKeyword = ref('')
const mistakeDirection = ref('')
const newWord = ref('')
const notePage = ref(1)
const mistakePage = ref(1)
const wordPage = ref(1)
const noteTotal = ref(0)
const mistakeTotal = ref(0)
const wordTotal = ref(0)
const loadingNote = ref(false)
const loadingMistake = ref(false)
const loadingWord = ref(false)
const previewDialog = ref(false)
const previewData = ref(null)

function auditMeta(v) {
  return AUDIT_STATUS.find(s => s.value === v) || { label: '未知', type: 'info' }
}

async function loadNotes() {
  loadingNote.value = true
  try {
    const res = await getNoteAuditPage({ auditStatus: noteStatus.value ?? undefined, keyword: noteKeyword.value || undefined, pageNum: notePage.value, pageSize: 10 })
    notes.value = res.data.list || []
    noteTotal.value = res.data.total || 0
  } finally {
    loadingNote.value = false
  }
}

async function loadMistakes() {
  loadingMistake.value = true
  try {
    const res = await getMistakeAuditPage({ auditStatus: mistakeStatus.value ?? undefined, techDirection: mistakeDirection.value || undefined, keyword: mistakeKeyword.value || undefined, pageNum: mistakePage.value, pageSize: 10 })
    mistakes.value = res.data.list || []
    mistakeTotal.value = res.data.total || 0
  } finally {
    loadingMistake.value = false
  }
}

async function loadWords() {
  loadingWord.value = true
  try {
    const res = await getSensitiveWordPage({ pageNum: wordPage.value, pageSize: 10 })
    words.value = res.data.list || []
    wordTotal.value = res.data.total || 0
  } finally {
    loadingWord.value = false
  }
}

async function previewNote(row) {
  const res = await getNoteAuditDetail(row.id)
  previewData.value = res.data
  previewDialog.value = true
}

async function pass(type, row) {
  if (type === 'note') await passNote(row.id)
  else await passMistake(row.id)
  ElMessage.success('已通过审核')
  loadAll()
}

async function reject(type, row) {
  const { value } = await ElMessageBox.prompt('请输入下架原因（学生可见）', '违规下架', {
    inputValue: '内容违规，已被管理员下架',
    confirmButtonText: '确认下架',
    cancelButtonText: '取消'
  })
  if (type === 'note') await rejectNote(row.id, value)
  else await rejectMistake(row.id, value)
  ElMessage.success('已下架')
  loadAll()
}

async function addWord() {
  if (!newWord.value.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  await addSensitiveWord(newWord.value.trim())
  ElMessage.success('敏感词已添加并即时生效')
  newWord.value = ''
  loadWords()
}

async function removeWord(row) {
  await deleteSensitiveWord(row.id)
  ElMessage.success('已删除')
  loadWords()
}

function loadAll() {
  if (tab.value === 'note') loadNotes()
  else if (tab.value === 'mistake') loadMistakes()
  else loadWords()
}

onMounted(loadAll)
</script>

<style scoped>
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; margin-bottom: 16px; }
.row-title { font-weight: 600; color: var(--text-title); }
.row-summary {
  font-size: 12px; color: var(--text-sub);
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.word-tip { flex: 1; border-radius: var(--radius-md); }
.word-add { display: flex; gap: 10px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
.preview-body { max-height: 60vh; overflow-y: auto; font-size: 14px; line-height: 1.9; }
</style>
