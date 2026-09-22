import { ElMessage } from 'element-plus'

// 富文本（v-html）代码块的一键复制：v-html 内容拿不到 scoped 样式与组件状态，
// 因此用「字符串注入按钮 + document 事件委托」的轻量方案，供 NoteDetail / QuestionDetail 共用

let listenerBound = false

/** 旧版兜底：部分环境（嵌入面板/非安全上下文）拒绝 Clipboard API，退回 execCommand */
function legacyCopy(text) {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  let ok = false
  try { ok = document.execCommand('copy') } catch (e) { ok = false }
  document.body.removeChild(ta)
  return ok
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch (e) {
    return legacyCopy(text)
  }
}

function ensureClickListener() {
  if (listenerBound) return
  listenerBound = true
  document.addEventListener('click', (e) => {
    const btn = e.target.closest && e.target.closest('.rc-copy')
    if (!btn) return
    const wrap = btn.closest('.rich-code')
    const pre = wrap && wrap.querySelector('pre')
    if (!pre) return
    copyText(pre.innerText)
      .then((ok) => {
        if (!ok) {
          ElMessage.warning('复制失败，请手动选择复制')
          return
        }
        btn.classList.add('copied')
        btn.textContent = '已复制'
        ElMessage.success('代码已复制')
        setTimeout(() => {
          btn.classList.remove('copied')
          btn.textContent = '复制'
        }, 1600)
      })
  })
}

/**
 * 给富文本 HTML 里的每个 <pre> 包一层带「复制」按钮的工具条。
 * 纯字符串变换，放在 v-html 赋值前调用。
 */
export function injectCopyButtons(html) {
  if (!html) return html
  ensureClickListener()
  return html
    .replace(/<pre([^>]*)>/g,
      '<div class="rich-code"><div class="rich-code-bar"><button type="button" class="rc-copy">复制</button></div><pre$1>')
    .replace(/<\/pre>/g, '</pre></div>')
}
