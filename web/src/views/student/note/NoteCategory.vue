<template>
  <div class="category-page">
    <AppCard title="笔记分类管理">
      <template #extra>
        <el-button type="primary" round :icon="Plus" @click="openAdd">新增分类</el-button>
      </template>

      <el-alert type="info" :closable="false" class="tip">
        系统内置分类不可修改；删除自建分类时，分类下的笔记会自动移入「未分类」，不会丢失。
      </el-alert>

      <el-table :data="categories" stripe v-loading="loading">
        <el-table-column label="分类名" min-width="180">
          <template #default="{ row }">
            <el-icon class="row-icon"><Folder /></el-icon>
            {{ row.name }}
            <el-tag v-if="row.isSystem === 1" size="small" type="info" round class="sys-tag">内置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="我的笔记数" width="120" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <template v-if="row.isSystem === 0">
              <el-button link type="primary" size="small" @click="openRename(row)">改名</el-button>
              <el-popconfirm title="删除后笔记移入未分类，确定删除？" @confirm="removeOne(row)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
            <span v-else class="muted">不可修改</span>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="dialog" :title="editRow ? '重命名分类' : '新增分类'" width="420px">
      <el-input v-model="catName" placeholder="分类名，如：React 学习笔记" maxlength="16" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Folder } from '@element-plus/icons-vue'
import { getCategoryList, addCategory, renameCategory, deleteCategory } from '@/api/note'
import AppCard from '@/components/AppCard.vue'

const loading = ref(true)
const categories = ref([])
const dialog = ref(false)
const catName = ref('')
const editRow = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editRow.value = null
  catName.value = ''
  dialog.value = true
}

function openRename(row) {
  editRow.value = row
  catName.value = row.name
  dialog.value = true
}

async function save() {
  if (!catName.value.trim()) {
    ElMessage.warning('请输入分类名')
    return
  }
  if (editRow.value) {
    await renameCategory(editRow.value.id, { name: catName.value.trim() })
    ElMessage.success('已重命名')
  } else {
    await addCategory({ name: catName.value.trim() })
    ElMessage.success('分类已创建')
  }
  dialog.value = false
  load()
}

async function removeOne(row) {
  await deleteCategory(row.id)
  ElMessage.success('分类已删除，笔记已移入未分类')
  load()
}

onMounted(load)
</script>

<style scoped>
.category-page { max-width: 860px; margin: 0 auto; }
.tip { margin-bottom: 16px; border-radius: var(--radius-md); }
.row-icon { vertical-align: -2px; margin-right: 6px; color: var(--primary); }
.sys-tag { margin-left: 8px; }
.muted { font-size: 12px; color: var(--text-placeholder); }
</style>
