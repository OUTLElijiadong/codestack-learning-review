<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
// ECharts 统一封装：业务页只传 option，颜色/tooltip/坐标轴/圆角柱头继承注册主题
// theme: learn(浅色，默认) / learn-dark(数据大屏)
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import echarts from '@/utils/echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' },
  theme: { type: String, default: 'learn' }
})

const chartRef = ref(null)
let chart = null
let observer = null

// 透出 ECharts 原生 click（params 含 seriesName/name/value 等），供大屏做点击下钻
const emit = defineEmits(['chart-click'])

onMounted(() => {
  initChart()
  observer = new ResizeObserver(() => chart && chart.resize())
  observer.observe(chartRef.value)
})

function initChart() {
  chart = echarts.init(chartRef.value, props.theme)
  chart.setOption(props.option)
  // 透出 ECharts 原生 click（params 含 seriesName/name/value 等），供大屏做点击下钻
  chart.on('click', (params) => emit('chart-click', params))
}

watch(
  () => props.option,
  (val) => { if (chart) chart.setOption(val, { notMerge: true }) },
  { deep: true }
)

watch(
  () => props.theme,
  (val) => {
    if (chart) {
      chart.dispose()
      initChart()
    }
  }
)

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
  if (chart) chart.dispose()
})
</script>
