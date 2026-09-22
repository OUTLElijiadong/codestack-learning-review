<template>
  <div class="note-edit">
    <AppCard :title="isEdit ? '编辑笔记' : '写笔记'">
      <template #extra>
        <div class="head-actions">
          <el-button round @click="$router.back()">返回</el-button>
          <button class="save-btn" :disabled="saving" @click="submit">
            {{ saving ? '保存中…' : '保存笔记' }}
          </button>
        </div>
      </template>

      <!-- 无边框大标题 -->
      <input v-model="form.title" class="title-input" placeholder="请输入笔记标题" maxlength="128" />

      <!-- meta 行：分类 + 公开开关 -->
      <div class="meta-row">
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 180px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <div class="public-switch">
          <el-switch v-model="isPublic" active-text="公开" inactive-text="私密" inline-prompt />
          <span class="switch-tip">
            {{ isPublic ? '公开后进入审核，通过后全站可见' : '仅自己可见' }}
          </span>
        </div>
      </div>

      <!-- 富文本编辑器（支持代码块高亮） -->
      <Editor v-model="form.content" height="52vh" placeholder="记录知识点、项目经验、技术难点…支持插入代码块" />

      <el-alert v-if="isPublic" type="warning" :closable="false" class="audit-tip">
        公开笔记需管理员审核通过后才会出现在笔记广场；含不良关键词的内容会被自动拦截。
      </el-alert>
    </AppCard>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addNote, updateNote, getNoteDetail, getCategoryList } from '@/api/note'
import AppCard from '@/components/AppCard.vue'
import Editor from '@/components/Editor.vue'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const categories = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  content: '',
  categoryId: null,
  isPublic: 0
})

// isPublic 在表单里用布尔开关交互，提交时转 0/1
const isPublic = computed({
  get: () => form.isPublic === 1,
  set: (v) => { form.isPublic = v ? 1 : 0 }
})

async function submit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入笔记标题')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择笔记分类')
    return
  }
  if (!form.content || form.content === '<p><br></p>') {
    ElMessage.warning('笔记内容不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateNote(route.params.id, form)
      ElMessage.success('笔记已保存')
    } else {
      await addNote(form)
      ElMessage.success(form.isPublic === 1 ? '已提交，公开笔记审核通过后全站可见' : '笔记已保存')
    }
    router.push('/student/note')
  } catch (e) { /* 拦截器已提示（敏感词拦截会在这里提示） */ } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const res = await getCategoryList()
  categories.value = res.data || []
  if (isEdit.value) {
    const detail = await getNoteDetail(route.params.id)
    form.title = detail.data.title
    form.content = detail.data.content || ''
    form.categoryId = detail.data.categoryId
    form.isPublic = detail.data.isPublic
  }
})
</script>

<style scoped>
.note-edit { max-width: 1080px; margin: 0 auto; }
.head-actions { display: flex; gap: 12px; align-items: center; }
.save-btn {
  height: 36px; padding: 0 24px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.save-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }
.save-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.title-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-title);
  padding: 6px 0 14px;
  border-bottom: 1px solid var(--gray-100);
  background: transparent;
}
.title-input::placeholder { color: var(--text-disabled); }

.meta-row {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 14px 0;
}
.public-switch { display: flex; align-items: center; gap: 10px; }
.switch-tip { font-size: 12px; color: var(--text-placeholder); }
.audit-tip { margin-top: 16px; border-radius: var(--radius-md); }
</style>
