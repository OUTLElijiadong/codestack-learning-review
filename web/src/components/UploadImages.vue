<template>
  <div class="upload-images">
    <el-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :limit="limit"
      :http-request="customUpload"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :on-exceed="() => ElMessage.warning(`最多上传 ${limit} 张图片`)"
      accept=".jpg,.jpeg,.png,.gif,.webp"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    <div v-if="tip" class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup>
// 多图上传封装：picture-card 列表，自定义 http-request 走 axios（天然带 token）
// v-model 绑定图片 URL 字符串数组
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  limit: { type: Number, default: 3 },
  tip: { type: String, default: '支持 jpg/png/gif/webp，单张不超过 10MB' }
})
const emit = defineEmits(['update:modelValue'])

// el-upload 的 fileList 与父级 URL 数组互转
const fileList = ref(props.modelValue.map((url, i) => ({ name: 'image-' + i, url })))

watch(() => props.modelValue, (val) => {
  const current = fileList.value.map(f => f.url || (f.response && f.response.url))
  if (JSON.stringify(current) !== JSON.stringify(val)) {
    fileList.value = val.map((url, i) => ({ name: 'image-' + i, url }))
  }
})

function syncEmit() {
  const urls = fileList.value
    .filter(f => f.status === 'success' || f.url)
    .map(f => f.url)
  emit('update:modelValue', urls)
}

async function customUpload({ file, onSuccess, onError }) {
  try {
    const res = await uploadImage(file)
    // 把后端返回的 URL 挂到文件对象上
    const target = fileList.value.find(f => f.uid === file.uid)
    if (target) target.url = res.data.url
    onSuccess(res.data)
    syncEmit()
  } catch (e) {
    onError(e)
  }
}

function beforeUpload(file) {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!ok) {
    ElMessage.warning('仅支持 jpg/png/gif/webp 图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }
  return true
}

function handleRemove() {
  syncEmit()
}
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 6px;
}
</style>
