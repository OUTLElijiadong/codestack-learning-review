/**
 * 搜索关键词高亮指令：v-highlight="keyword"
 * 把文本中出现的关键词包成 <mark class="hl">（黄色荧光笔效果，样式在 global.css）
 */
function applyHighlight(el, keyword) {
  const raw = el.dataset.raw ?? el.textContent
  el.dataset.raw = raw
  if (!keyword) {
    el.textContent = raw
    return
  }
  const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  el.innerHTML = raw.replace(
    new RegExp(escaped, 'gi'),
    (m) => `<mark class="hl">${m}</mark>`
  )
}

export default {
  mounted(el, binding) { applyHighlight(el, binding.value) },
  updated(el, binding) { applyHighlight(el, binding.value) }
}
