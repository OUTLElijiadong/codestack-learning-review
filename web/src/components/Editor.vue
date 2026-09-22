<template>
  <div class="editor-shell">
    <Toolbar class="editor-toolbar" :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" />
    <Editor
      class="editor-body"
      :style="{ minHeight: height }"
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      mode="default"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>

<script setup>
// wangEditor 二次封装：工具栏保留标题/粗体/列表/代码块/图片，图片上传走 axios（带 token）
import '@wangeditor/editor/dist/css/style.css'
import { ref, shallowRef, watch, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容，支持插入代码块…' },
  height: { type: String, default: '360px' }
})
const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef()
const valueHtml = ref(props.modelValue)

// 父级外部改值时回显（编辑场景）
watch(() => props.modelValue, (val) => {
  if (val !== valueHtml.value) valueHtml.value = val
})

const toolbarConfig = {
  toolbarKeys: [
    'headerSelect', 'bold', 'italic', 'underline', 'through',
    'color', 'bgColor', '|',
    'bulletedList', 'numberedList', 'todo', '|',
    'blockquote', 'codeBlock', 'insertLink', 'uploadImage', 'insertTable', '|',
    'undo', 'redo', 'clearStyle', 'fullScreen'
  ]
}

const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      // 自定义上传：走 axios 实例，天然携带 token 与统一报错
      async customUpload(file, insertFn) {
        try {
          const res = await uploadImage(file)
          insertFn(res.data.url, file.name, res.data.url)
        } catch (e) {
          ElMessage.error('图片上传失败')
        }
      }
    },
    codeBlock: {
      // 代码块语言列表（展示高亮由 highlight.js 完成）
      codeSelectLangs: ['javascript', 'java', 'sql', 'html', 'css', 'python', 'bash', 'json', 'plaintext']
    }
  }
}

function handleCreated(editor) {
  editorRef.value = editor
}

function handleChange(editor) {
  emit('update:modelValue', editor.getHtml())
}

onBeforeUnmount(() => {
  // 必须销毁编辑器，防止内存泄漏
  const editor = editorRef.value
  if (editor) editor.destroy()
})
</script>

<style scoped>
.editor-shell {
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-md);
  overflow: hidden;
}
.editor-body {
  overflow-y: hidden !important;
}
</style>
