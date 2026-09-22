<template>
  <div class="code-block">
    <div class="code-block-bar">
      <span class="code-lang">{{ language }}</span>
      <span class="code-copy" @click="copyCode">
        <el-icon><CopyDocument /></el-icon> 复制
      </span>
    </div>
    <pre class="code-pre"><code v-html="highlighted"></code></pre>
  </div>
</template>

<script setup>
// highlight.js 代码高亮只读展示 + 一键复制
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import hljs from 'highlight.js/lib/common'

const props = defineProps({
  code: { type: String, default: '' },
  language: { type: String, default: 'plaintext' }
})

const highlighted = computed(() => {
  if (!props.code) return ''
  try {
    if (hljs.getLanguage(props.language)) {
      return hljs.highlight(props.code, { language: props.language }).value
    }
    return hljs.highlightAuto(props.code).value
  } catch (e) {
    return props.code
  }
})

async function copyCode() {
  // Clipboard API 被拒（嵌入面板/非安全上下文）时退回 execCommand
  let ok = false
  try {
    await navigator.clipboard.writeText(props.code)
    ok = true
  } catch (e) {
    const ta = document.createElement('textarea')
    ta.value = props.code
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    try { ok = document.execCommand('copy') } catch (e2) { ok = false }
    document.body.removeChild(ta)
  }
  if (ok) ElMessage.success('代码已复制')
  else ElMessage.warning('复制失败，请手动选择复制')
}
</script>

<style scoped>
.code-block {
  background: var(--code-bg);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06);
}
.code-block-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.code-lang {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #94A3B8;
}
.code-copy {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94A3B8;
  cursor: pointer;
  transition: color var(--dur-fast);
}
.code-copy:hover { color: #818CF8; }
.code-pre {
  margin: 0;
  padding: 14px 16px;
  overflow-x: auto;
}
.code-pre code {
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.7;
  background: transparent;
  color: #E2E8F0;
}
</style>
