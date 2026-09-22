<template>
  <div class="search-page">
    <!-- 筛选栏（sticky 顶部） -->
    <AppCard class="filter-bar" pad="16px 20px">
      <div class="filter-row">
        <el-input
          v-model="query.keyword"
          placeholder="关键词全文搜索：标题 / 错误代码 / 报错信息…"
          clearable
          :prefix-icon="Search"
          class="kw-input"
          @keyup.enter="doSearch"
          @clear="doSearch"
        />
        <el-select v-model="query.techDirection" placeholder="技术方向" clearable style="width: 130px" @change="doSearch">
          <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="query.tagId" placeholder="标签" clearable style="width: 120px" @change="doSearch">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <div class="date-chips">
          <span
            v-for="c in dateChips"
            :key="c.label"
            class="date-chip"
            :class="{ active: query.recentDays === c.value }"
            @click="pickDate(c.value)"
          >{{ c.label }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">错误类型：</span>
        <span
          class="type-chip"
          :class="{ active: !query.errorType }"
          @click="pickType('')"
        >全部</span>
        <span
          v-for="t in ERROR_TYPES"
          :key="t.value"
          class="type-chip"
          :class="{ active: query.errorType === t.value }"
          :style="query.errorType === t.value ? { background: t.bg, color: t.textColor, borderColor: t.border } : {}"
          @click="pickType(t.value)"
        >
          <i class="type-dot" :style="{ background: t.color }"></i>{{ t.label }}
        </span>
        <span class="result-count">共找到 <b>{{ total }}</b> 条结果</span>
      </div>
    </AppCard>

    <!-- 结果列表 -->
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <div v-if="list.length === 0">
        <AppCard>
          <EmptyState description="没有找到相关内容，换个关键词或减少筛选条件试试" btn-text="清空筛选" @action="resetAll" />
        </AppCard>
      </div>
      <AppCard v-else pad="8px 20px">
        <div
          v-for="item in list"
          :key="item.id"
          class="result-item"
          @click="$router.push(`/student/error/detail/${item.id}`)"
        >
          <div class="result-head">
            <span class="err-badge" :class="'err-badge--' + item.errorType">{{ errorTypeLabel(item.errorType) }}</span>
            <span class="result-title" v-highlight="query.keyword">{{ item.title }}</span>
            <span class="result-tech">{{ item.techDirection }}</span>
          </div>
          <p v-if="item.errorMsg" class="result-msg" v-highlight="query.keyword">{{ item.errorMsg }}</p>
          <div class="result-foot">
            <span v-for="tag in item.tags" :key="tag.id" class="tag-chip" :style="tagStyle(tag.id)">#{{ tag.name }}</span>
            <span class="result-time">{{ fmtDate(item.createTime) }}</span>
          </div>
        </div>
        <div class="pager">
          <el-pagination
            v-model:current-page="query.pageNum"
            :total="total"
            :page-size="query.pageSize"
            layout="total, prev, pager, next"
            background
            @current-change="doSearch"
          />
        </div>
      </AppCard>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchMistake, getTagList } from '@/api/mistake'
import { ERROR_TYPES, TECH_DIRECTIONS, errorTypeLabel, tagColor } from '@/constants/dict'
import { fmtDate } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import vHighlight from '@/directives/highlight'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const tags = ref([])

const dateChips = [
  { label: '全部时间', value: null },
  { label: '今天', value: 1 },
  { label: '最近一周', value: 7 },
  { label: '最近一月', value: 30 }
]

const query = reactive({
  keyword: '',
  techDirection: '',
  errorType: '',
  tagId: null,
  recentDays: null,
  pageNum: 1,
  pageSize: 10
})

function tagStyle(id) {
  const c = tagColor(id)
  return { color: c.text, background: c.bg }
}

function pickDate(v) {
  query.recentDays = v
  query.pageNum = 1
  doSearch()
}

function pickType(v) {
  query.errorType = v
  query.pageNum = 1
  doSearch()
}

function resetAll() {
  query.keyword = ''
  query.techDirection = ''
  query.errorType = ''
  query.tagId = null
  query.recentDays = null
  query.pageNum = 1
  doSearch()
}

async function doSearch() {
  loading.value = true
  try {
    const res = await searchMistake({
      keyword: query.keyword || undefined,
      techDirection: query.techDirection || undefined,
      errorType: query.errorType || undefined,
      tagId: query.tagId || undefined,
      recentDays: query.recentDays || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const route = useRoute()

onMounted(async () => {
  // 顶栏全局快搜跳转进来时带上关键词
  if (route.query.keyword) {
    query.keyword = String(route.query.keyword)
  }
  const res = await getTagList()
  tags.value = res.data || []
  doSearch()
})

// 已在检索页时顶栏再次搜索：query 变化直接触发
watch(() => route.query.keyword, (kw) => {
  if (kw !== undefined) {
    query.keyword = String(kw)
    query.pageNum = 1
    doSearch()
  }
})
</script>

<style scoped>
.search-page { display: flex; flex-direction: column; gap: 20px; }
.filter-bar { position: sticky; top: 84px; z-index: 10; }
.filter-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.filter-row + .filter-row { margin-top: 12px; }
.kw-input { width: 360px; }
.filter-label { font-size: 13px; color: var(--text-sub); }

.date-chips { display: flex; gap: 8px; }
.date-chip {
  font-size: 12px; padding: 4px 12px; border-radius: 999px;
  background: var(--gray-100); color: var(--text-sub); cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--dur-fast);
}
.date-chip:hover { color: var(--primary); }
.date-chip.active { background: var(--primary-bg); color: var(--primary); border-color: var(--primary-border); font-weight: 600; }

.type-chip {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px; padding: 4px 12px; border-radius: 999px;
  background: var(--gray-100); color: var(--text-sub); cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--dur-fast);
}
.type-dot { width: 6px; height: 6px; border-radius: 50%; }
.result-count { margin-left: auto; font-size: 13px; color: var(--text-sub); }
.result-count b { color: var(--primary); }

.result-item {
  padding: 14px 10px;
  border-bottom: 1px solid var(--gray-100);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--dur-fast);
}
.result-item:last-child { border-bottom: none; }
.result-item:hover { background: var(--bg-hover); }
.result-head { display: flex; align-items: center; gap: 10px; }
.result-title { font-size: 15px; font-weight: 600; color: var(--text-title); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.result-tech { font-size: 12px; color: var(--primary); background: var(--primary-bg); padding: 1px 8px; border-radius: 999px; }
.result-msg {
  font-size: 12px; color: var(--text-sub); font-family: var(--font-mono);
  margin: 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.result-foot { display: flex; align-items: center; gap: 4px; }
.result-time { margin-left: auto; font-size: 12px; color: var(--text-placeholder); }
.pager { display: flex; justify-content: center; padding: 14px 0 8px; }
</style>
