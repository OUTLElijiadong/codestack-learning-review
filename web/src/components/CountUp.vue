<template>
  <span class="num">{{ display }}</span>
</template>

<script setup>
// 统计数字滚动动画（不引第三方库，requestAnimationFrame 实现）
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  end: { type: Number, default: 0 },
  duration: { type: Number, default: 1200 },
  decimals: { type: Number, default: 0 }
})

const display = ref((0).toFixed(props.decimals))
let raf = null

function animate(from, to) {
  const start = performance.now()
  const tick = (now) => {
    const t = Math.min((now - start) / props.duration, 1)
    const eased = 1 - Math.pow(1 - t, 3)
    display.value = (from + (to - from) * eased).toFixed(props.decimals)
    if (t < 1) raf = requestAnimationFrame(tick)
  }
  cancelAnimationFrame(raf)
  raf = requestAnimationFrame(tick)
}

watch(() => props.end, (n, o) => animate(o || 0, n))
onMounted(() => animate(0, props.end))
onBeforeUnmount(() => cancelAnimationFrame(raf))
</script>
