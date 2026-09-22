<template>
  <div class="error-list">
    <!-- 顶部工具条 -->
    <AppCard class="toolbar" pad="16px 20px">
      <div class="toolbar-row">
        <el-input
          v-model="query.keyword"
          class="search-input"
          placeholder="搜索标题 / 报错信息…"
          clearable
          :prefix-icon="Search"
          @keyup.enter="loadList"
          @clear="loadList"
        />
        <el-select v-model="query.techDirection" placeholder="技术方向" clearable style="width: 140px" @change="loadList">
          <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="query.errorType" placeholder="错误类型" clearable style="width: 140px" @change="loadList">
          <el-option v-for="t in ERROR_TYPES" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="query.tagId" placeholder="标签" clearable style="width: 130px" @change="loadList">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-checkbox v-model="onlyFavorite" @change="loadList">只看收藏</el-checkbox>
        <div class="toolbar-right">
          <el-button round @click="tagDialog = true">
            <el-icon><PriceTag /></el-icon>&nbsp;标签管理
          </el-button>
          <el-button round @click="$router.push('/student/error/archive')">
            <el-icon><Box /></el-icon>&nbsp;归档箱
          </el-button>
          <button class="add-btn" @click="$router.push('/student/error/edit')">
            <el-icon><Plus /></el-icon>&nbsp;新增错题
          </button>
        </div>
      </div>
    </AppCard>

    <!-- 错题卡片栅格 -->
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <div v-if="list.length === 0">
        <AppCard>
          <EmptyState description="还没有错题，保持这个好习惯" btn-text="记录第一道错题" @action="$router.push('/student/error/edit')" />
        </AppCard>
      </div>
      <div v-else class="card-grid">
        <div
          v-for="(item, idx) in list"
          :key="item.id"
          class="error-card rise-in"
          :style="{ animationDelay: Math.min(idx * 45, 400) + 'ms' }"
          :class="{ topped: item.isTop === 1 }"
          @click="$router.push(`/student/error/detail/${item.id}`)"
        >
          <div class="card-top-line" :style="{ background: typeMeta(item.errorType).color }"></div>
          <div class="card-head">
            <span class="err-badge" :class="'err-badge--' + item.errorType">{{ typeMeta(item.errorType).label }}</span>
            <div class="card-icons" @click.stop>
              <el-icon
                v-if="item.isTop === 1"
                class="icon-top"
                title="已置顶，点击取消"
                @click="toggleTopOne(item)"
              ><Top /></el-icon>
              <el-icon
                class="icon-fav"
                :class="{ 'star-active': item.isFavorite === 1 }"
                :title="item.isFavorite === 1 ? '取消收藏' : '收藏'"
                @click="toggleFav(item)"
              ><StarFilled v-if="item.isFavorite === 1" /><Star v-else /></el-icon>
            </div>
          </div>
          <h3 class="card-title">
            <el-icon v-if="item.isTop === 1" class="pin"><Top /></el-icon>{{ item.title }}
          </h3>

          <!-- 深色代码预览块（最多 3 行，底部渐隐） -->
          <div v-if="item.errorCode" class="code-preview">
            <pre>{{ item.errorCode }}</pre>
            <div class="code-fade"></div>
          </div>

          <div v-if="item.errorMsg" class="error-msg">{{ item.errorMsg }}</div>

          <div class="card-tags">
            <span
              v-for="tag in item.tags"
              :key="tag.id"
              class="tag-chip"
              :style="tagStyle(tag.id)"
            >#{{ tag.name }}</span>
          </div>

          <div class="card-foot">
            <span class="foot-meta">{{ item.techDirection }} · {{ fmtDate(item.createTime) }}</span>
            <div class="foot-actions" @click.stop>
              <el-icon title="编辑" @click="$router.push(`/student/error/edit/${item.id}`)"><Edit /></el-icon>
              <el-popconfirm title="删除后进入归档箱，可恢复" confirm-button-text="删除" cancel-button-text="取消" @confirm="archiveOne(item)">
                <template #reference>
                  <el-icon title="删除到归档"><Delete /></el-icon>
                </template>
              </el-popconfirm>
            </div>
          </div>

          <div v-if="item.auditStatus === 2" class="audit-mask">已被管理员下架</div>
        </div>
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[9, 18, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </template>

    <!-- 标签管理弹窗 -->
    <el-dialog v-model="tagDialog" title="错题标签管理" width="480px">
      <div class="tag-add-row">
        <el-input v-model="newTagName" placeholder="新标签名，如：空指针" maxlength="16" clearable />
        <el-button type="primary" round @click="addNewTag">新增</el-button>
      </div>
      <el-table :data="tags" size="small" stripe>
        <el-table-column label="标签" min-width="120">
          <template #default="{ row }">
            <span class="tag-chip" :style="tagStyle(row.id)">#{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="错题数" width="80" align="center" />
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="renameOne(row)">改名</el-button>
            <el-popconfirm title="删除标签不会删除错题" @confirm="deleteOne(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Top, Star, StarFilled, Edit, Delete, PriceTag, Box } from '@element-plus/icons-vue'
import { getMistakePage, toggleTop, toggleFavorite, archiveMistake, getTagList, addTag, renameTag, deleteTag } from '@/api/mistake'
import { ERROR_TYPES, TECH_DIRECTIONS, errorTypeMeta, tagColor } from '@/constants/dict'
import { fmtDate } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const tags = ref([])
const onlyFavorite = ref(false)
const tagDialog = ref(false)
const newTagName = ref('')

const query = reactive({
  keyword: '',
  techDirection: '',
  errorType: '',
  tagId: null,
  pageNum: 1,
  pageSize: 9
})

function typeMeta(v) { return errorTypeMeta(v) }
function tagStyle(id) {
  const c = tagColor(id)
  return { color: c.text, background: c.bg }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getMistakePage({
      ...query,
      onlyFavorite: onlyFavorite.value ? 1 : undefined
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  const res = await getTagList()
  tags.value = res.data || []
}

async function toggleTopOne(item) {
  await toggleTop(item.id)
  ElMessage.success(item.isTop === 1 ? '已取消置顶' : '已置顶')
  loadList()
}

async function toggleFav(item) {
  await toggleFavorite(item.id)
  item.isFavorite = item.isFavorite === 1 ? 0 : 1
  ElMessage.success(item.isFavorite === 1 ? '已收藏' : '已取消收藏')
}

async function archiveOne(item) {
  await archiveMistake(item.id)
  ElMessage.success('已移入归档箱，可随时恢复')
  loadList()
}

/* 标签管理 */
async function addNewTag() {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名')
    return
  }
  await addTag({ name: newTagName.value.trim() })
  newTagName.value = ''
  ElMessage.success('标签已创建')
  loadTags()
}

async function renameOne(row) {
  const { value } = await ElMessageBox.prompt('请输入新的标签名', '重命名标签', {
    inputValue: row.name,
    inputPattern: /\S+/,
    inputErrorMessage: '标签名不能为空'
  })
  await renameTag(row.id, { name: value.trim() })
  ElMessage.success('已重命名')
  loadTags()
  loadList()
}

async function deleteOne(row) {
  await deleteTag(row.id)
  ElMessage.success('标签已删除')
  loadTags()
  loadList()
}

onMounted(() => {
  loadList()
  loadTags()
})
</script>

<style scoped>
.error-list { display: flex; flex-direction: column; gap: 20px; }
.toolbar-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.search-input { width: 280px; }
.toolbar-right { margin-left: auto; display: flex; align-items: center; gap: 10px; }
.add-btn {
  display: inline-flex; align-items: center;
  height: 34px; padding: 0 18px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.add-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }

/* ---------- 错题卡片栅格 ---------- */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.error-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 18px 20px 14px;
  cursor: pointer;
  overflow: hidden;
  transition: all var(--dur-base) var(--ease-out);
}
.error-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-card-hover); }
.error-card.topped { border-color: var(--primary-border); }
.card-top-line {
  position: absolute; top: 0; left: 0; right: 0; height: 3px;
}
.error-card.topped .card-top-line { background: var(--brand-gradient) !important; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-icons { display: flex; gap: 10px; }
.icon-top { color: var(--primary); cursor: pointer; }
.icon-fav { color: var(--gray-300); cursor: pointer; font-size: 17px; }
.icon-fav:hover { color: var(--warning); }
.card-title {
  font-size: 15px; font-weight: 600; color: var(--text-title);
  margin: 0 0 10px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.pin { color: var(--primary); margin-right: 4px; transform: rotate(-45deg); }

.code-preview {
  position: relative;
  background: var(--code-bg);
  border-radius: var(--radius-md);
  max-height: 66px;
  overflow: hidden;
  margin-bottom: 10px;
}
.code-preview pre {
  margin: 0; padding: 10px 12px;
  font-family: var(--font-mono); font-size: 12px; line-height: 1.6;
  color: #E2E8F0; white-space: pre-wrap; word-break: break-all;
}
.code-fade {
  position: absolute; bottom: 0; left: 0; right: 0; height: 28px;
  background: linear-gradient(transparent, var(--code-bg));
}
.error-msg {
  background: var(--danger-bg);
  color: #B91C1C;
  border-radius: var(--radius-sm);
  padding: 4px 10px;
  font-size: 12px;
  font-family: var(--font-mono);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 10px;
}
.card-tags { min-height: 22px; margin-bottom: 8px; }
.card-foot {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1px solid var(--gray-100); padding-top: 10px;
}
.foot-meta { font-size: 12px; color: var(--text-placeholder); }
.foot-actions { display: flex; gap: 12px; color: var(--text-placeholder); }
.foot-actions .el-icon:hover { color: var(--primary); }
.audit-mask {
  position: absolute; right: 12px; top: 34px;
  font-size: 11px; color: var(--text-placeholder);
  border: 1px dashed var(--gray-300); border-radius: 999px; padding: 2px 8px;
  transform: rotate(6deg);
}

.pager { display: flex; justify-content: center; }
.tag-add-row { display: flex; gap: 10px; margin-bottom: 14px; }
</style>
