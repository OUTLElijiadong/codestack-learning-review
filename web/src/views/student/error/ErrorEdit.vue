<template>
  <div class="error-edit">
    <AppCard :title="isEdit ? '编辑错题' : '新增错题'">
      <template #extra>
        <el-button round @click="$router.back()">返回</el-button>
      </template>

      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" size="large">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="错题标题" prop="title">
              <el-input v-model="form.title" placeholder="一句话描述这个错误，如：v-for 与 v-if 同用导致渲染异常" maxlength="128" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="技术方向" prop="techDirection">
              <el-select v-model="form.techDirection" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in TECH_DIRECTIONS" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="错误原因分类" prop="errorType">
              <el-select v-model="form.errorType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in ERROR_TYPES" :key="t.value" :label="t.label" :value="t.value">
                  <span class="err-badge" :class="'err-badge--' + t.value">{{ t.label }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="错误代码片段" prop="errorCode">
          <el-input
            v-model="form.errorCode"
            type="textarea"
            :rows="6"
            placeholder="粘贴出错的代码片段…"
            class="code-input"
          />
        </el-form-item>

        <el-form-item label="报错信息 / 异常堆栈" prop="errorMsg">
          <el-input
            v-model="form.errorMsg"
            type="textarea"
            :rows="4"
            placeholder="粘贴控制台/终端的报错信息…"
            class="code-input"
          />
        </el-form-item>

        <el-form-item label="正确解决方案（代码 + 思路）" prop="solution">
          <el-input
            v-model="form.solution"
            type="textarea"
            :rows="6"
            placeholder="写下正确的代码与解决思路，复盘时这就是标准答案…"
            class="code-input"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="运行截图 / 报错截图">
              <UploadImages v-model="form.images" :limit="6" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="错题标签">
              <el-select v-model="form.tagIds" multiple collapse-tags collapse-tags-tooltip placeholder="选择标签（可在错题本页管理标签）" style="width: 100%">
                <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="submit-row">
          <el-button round size="large" @click="$router.back()">取消</el-button>
          <button type="button" class="save-btn" :disabled="saving" @click="submit">
            {{ saving ? '保存中…' : (isEdit ? '保存修改' : '保存错题') }}
          </button>
        </div>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addMistake, updateMistake, getMistakeDetail, getTagList } from '@/api/mistake'
import { ERROR_TYPES, TECH_DIRECTIONS } from '@/constants/dict'
import AppCard from '@/components/AppCard.vue'
import UploadImages from '@/components/UploadImages.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const tags = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  techDirection: '',
  errorType: '',
  errorCode: '',
  errorMsg: '',
  solution: '',
  images: [],
  tagIds: []
})

const formRules = {
  title: [{ required: true, message: '请输入错题标题', trigger: 'blur' }],
  techDirection: [{ required: true, message: '请选择技术方向', trigger: 'change' }],
  errorType: [{ required: true, message: '请选择错误类型', trigger: 'change' }]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateMistake(route.params.id, form)
      ElMessage.success('修改已保存')
    } else {
      await addMistake(form)
      ElMessage.success('错题已收录，记得按时复盘！')
    }
    router.push('/student/error')
  } catch (e) {
    /* 拦截器已提示（敏感词拦截会在这里提示） */
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const res = await getTagList()
  tags.value = res.data || []
  if (isEdit.value) {
    const detail = await getMistakeDetail(route.params.id)
    const d = detail.data
    form.title = d.title
    form.techDirection = d.techDirection
    form.errorType = d.errorType
    form.errorCode = d.errorCode || ''
    form.errorMsg = d.errorMsg || ''
    form.solution = d.solution || ''
    form.images = d.images || []
    form.tagIds = (d.tags || []).map(t => t.id)
  }
})
</script>

<style scoped>
.error-edit { max-width: 1080px; margin: 0 auto; }
.code-input :deep(textarea) {
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.7;
  background: var(--gray-50);
}
.submit-row {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  border-top: 1px solid var(--gray-100);
  padding-top: 20px;
  margin-top: 8px;
}
.save-btn {
  height: 42px; padding: 0 32px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.save-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }
.save-btn:disabled { opacity: 0.7; cursor: not-allowed; }
</style>
