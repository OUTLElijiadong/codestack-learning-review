<template>
  <div class="note-page">
    <div class="note-body">
      <!-- 左侧分类栏 -->
      <AppCard class="category-side" pad="14px 10px">
        <div class="cat-head">
          <span class="cat-title">分类</span>
          <el-icon class="cat-manage" title="管理分类" @click="$router.push('/student/note/category')"><Setting /></el-icon>
        </div>
        <div
          class="cat-item"
          :class="{ active: !query.categoryId }"
          @click="selectCategory(null)"
        >
          <el-icon><Files /></el-icon><span>全部笔记</span>
        </div>
        <div
          v-for="c in categories"
          :key="c.id"
          class="cat-item"
          :class="{ active: query.categoryId === c.id }"
          @click="selectCategory(c.id)"
        >
          <el-icon><Folder /></el-icon>
          <span class="cat-name">{{ c.name }}</span>
          <span class="cat-count">{{ c.count }}</span>
        </div>
      </AppCard>

      <!-- 右侧主区 -->
      <div class="note-main">
        <AppCard pad="16px 20px">
          <div class="note-toolbar">
            <el-tabs v-model="tab" @tab-change="loadList">
              <el-tab-pane label="我的笔记" name="mine" />
              <el-tab-pane label="笔记广场" name="square" />
            </el-tabs>
            <div class="toolbar-right">
              <el-input
                v-model="query.keyword"
                placeholder="搜索笔记标题 / 内容…"
                clearable
                :prefix-icon="Search"
                style="width: 240px"
                @keyup.enter="loadList"
                @clear="loadList"
              />
              <el-select v-if="tab === 'mine'" v-model="query.isPublic" placeholder="公开/私密" clearable style="width: 120px" @change="loadList">
                <el-option label="公开" :value="1" />
                <el-option label="私密" :value="0" />
              </el-select>
              <el-button v-if="tab === 'mine'" round @click="doBackup" :loading="backing">
                <el-icon><Download /></el-icon>&nbsp;备份
              </el-button>
              <button class="add-btn" @click="$router.push('/student/note/edit')">
                <el-icon><Plus /></el-icon>&nbsp;写笔记
              </button>
            </div>
          </div>
        </AppCard>

        <el-skeleton v-if="loading" :rows="5" animated />
        <template v-else>
          <div v-if="list.length === 0">
            <AppCard>
              <EmptyState
                :description="tab === 'mine' ? '笔记还是空的' : '广场还没有公开笔记'"
                :btn-text="tab === 'mine' ? '写下第一篇笔记' : ''"
                @action="$router.push('/student/note/edit')"
              />
            </AppCard>
          </div>
          <div v-else class="note-grid">
            <div
              v-for="note in list"
              :key="note.id"
              class="note-card"
              @click="openNote(note)"
            >
              <div class="note-head">
                <span class="note-cat">{{ categoryName(note.categoryId) }}</span>
                <el-tag
                  v-if="tab === 'mine'"
                  size="small"
                  round
                  :type="note.isPublic === 1 ? 'success' : 'info'"
                  effect="light"
                >{{ note.isPublic === 1 ? '公开' : '私密' }}</el-tag>
              </div>
              <h3 class="note-title">{{ note.title }}</h3>
              <p class="note-summary">{{ note.summary || '（无摘要）' }}</p>
              <div class="note-foot">
                <template v-if="tab === 'square'">
                  <div class="author">
                    <div class="author-avatar">{{ (note.nickname || '同')[0] }}</div>
                    <span>{{ note.nickname }}</span>
                  </div>
                  <span class="note-meta"><el-icon><View /></el-icon> {{ note.viewCount }}</span>
                </template>
                <template v-else>
                  <span class="note-meta">{{ fmtDate(note.createTime) }}</span>
                  <el-tag v-if="note.isPublic === 1 && note.auditStatus === 0" size="small" type="warning" round>审核中</el-tag>
                  <el-tag v-if="note.auditStatus === 2" size="small" type="danger" round>已下架</el-tag>
                </template>
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, Setting, Folder, Files, View, Download } from '@element-plus/icons-vue'
import { getNotePage, getNoteSquare, getCategoryList, backupNotes } from '@/api/note'
import { fmtDate } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const loading = ref(true)
const backing = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const tab = ref('mine')

const query = reactive({
  keyword: '',
  categoryId: null,
  isPublic: null,
  pageNum: 1,
  pageSize: 12
})

function categoryName(id) {
  const hit = categories.value.find(c => c.id === id)
  return hit ? hit.name : '未分类'
}

function selectCategory(id) {
  query.categoryId = id
  query.pageNum = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const params = { keyword: query.keyword || undefined, categoryId: query.categoryId || undefined, pageNum: query.pageNum, pageSize: query.pageSize }
    const res = tab.value === 'mine'
      ? await getNotePage({ ...params, isPublic: query.isPublic ?? undefined })
      : await getNoteSquare(params)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  const res = await getCategoryList()
  categories.value = res.data || []
}

function openNote(note) {
  // 我的笔记 → 编辑页；广场笔记 → 只读详情页
  if (tab.value === 'mine') {
    router.push(`/student/note/edit/${note.id}`)
  } else {
    router.push(`/student/note/detail/${note.id}`)
  }
}

async function doBackup() {
  backing.value = true
  try {
    const res = await backupNotes()
    ElMessage.success('备份完成：' + res.data.fileName + '（每天凌晨 2 点系统还会自动备份）')
  } finally {
    backing.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.note-body { display: flex; gap: 20px; align-items: flex-start; }

/* 左侧分类栏 */
.category-side { width: 220px; flex-shrink: 0; position: sticky; top: 88px; }
.cat-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 10px 10px;
  border-bottom: 1px solid var(--gray-100);
  margin-bottom: 8px;
}
.cat-title { font-size: 14px; font-weight: 700; color: var(--text-title); }
.cat-manage { cursor: pointer; color: var(--text-sub); }
.cat-manage:hover { color: var(--primary); }
.cat-item {
  display: flex; align-items: center; gap: 8px;
  padding: 9px 10px;
  border-radius: var(--radius-btn);
  font-size: 13px;
  color: var(--text-main);
  cursor: pointer;
  transition: all var(--dur-fast);
}
.cat-item:hover { background: var(--gray-50); }
.cat-item.active { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.cat-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cat-count { font-size: 11px; color: var(--text-placeholder); background: var(--gray-100); border-radius: 999px; padding: 1px 7px; }
.cat-item.active .cat-count { background: #fff; color: var(--primary); }

/* 右侧主区 */
.note-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 20px; }
.note-toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.note-toolbar :deep(.el-tabs__header) { margin-bottom: 0; }
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.add-btn {
  display: inline-flex; align-items: center;
  height: 34px; padding: 0 18px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.add-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }

/* 笔记卡片 */
.note-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.note-card {
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 18px 20px;
  cursor: pointer;
  display: flex; flex-direction: column;
  transition: all var(--dur-base) var(--ease-out);
}
.note-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-card-hover); }
.note-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.note-cat {
  font-size: 12px; font-weight: 600; color: var(--primary);
  background: var(--primary-bg); border-radius: 999px; padding: 2px 10px;
}
.note-title {
  font-size: 15px; font-weight: 600; color: var(--text-title); margin: 0 0 8px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.note-summary {
  font-size: 13px; color: var(--text-sub); line-height: 1.7; margin: 0 0 14px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  flex: 1;
}
.note-foot {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1px solid var(--gray-100); padding-top: 10px;
}
.author { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-sub); }
.author-avatar {
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--brand-gradient); color: #fff;
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.note-meta { font-size: 12px; color: var(--text-placeholder); display: inline-flex; align-items: center; gap: 4px; }
.pager { display: flex; justify-content: center; }
</style>
