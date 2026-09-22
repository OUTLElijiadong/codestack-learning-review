<template>
  <div class="screen-panel">
    <i class="corner corner-tl"></i>
    <i class="corner corner-tr"></i>
    <i class="corner corner-bl"></i>
    <i class="corner corner-br"></i>
    <div v-if="title" class="screen-panel-title">{{ title }}</div>
    <slot></slot>
  </div>
</template>

<script setup>
// 数据大屏发光边框面板：四角青色光角 + 内发光，纯 CSS 实现
defineProps({ title: { type: String, default: '' } })
</script>

<style scoped>
.screen-panel {
  position: relative;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(148, 163, 184, 0.12);
  padding: 16px 18px;
}
.corner {
  position: absolute;
  width: 12px;
  height: 12px;
  border: 0 solid #6366F1;
}
.corner-tl { top: -1px; left: -1px; border-top-width: 2px; border-left-width: 2px; border-top-left-radius: 12px; }
.corner-tr { top: -1px; right: -1px; border-top-width: 2px; border-right-width: 2px; border-top-right-radius: 12px; }
.corner-bl { bottom: -1px; left: -1px; border-bottom-width: 2px; border-left-width: 2px; border-bottom-left-radius: 12px; }
.corner-br { bottom: -1px; right: -1px; border-bottom-width: 2px; border-right-width: 2px; border-bottom-right-radius: 12px; }
.screen-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  letter-spacing: 1px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.screen-panel-title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: linear-gradient(180deg, #6366F1, #818CF8);
  box-shadow: 0 0 6px rgba(99, 102, 241, 0.5);
}
</style>
