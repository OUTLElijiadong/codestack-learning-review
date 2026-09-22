<template>
  <div class="app-card" :style="{ padding: pad }">
    <div v-if="title" class="app-card-header">
      <span class="app-card-title">{{ title }}</span>
      <slot name="extra"></slot>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
// 全站统一白色内容卡片容器（标题左侧带品牌渐变指示条）
defineProps({
  title: { type: String, default: '' },
  pad: { type: String, default: '20px 24px' }
})
</script>

<style scoped>
.app-card {
  background: var(--bg-card);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: box-shadow var(--dur-base) var(--ease-out),
              transform var(--dur-base) var(--ease-out);
}
.app-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}
.app-card-title {
  font-size: var(--fs-md);
  font-weight: 600;
  color: var(--text-title);
  display: flex;
  align-items: center;
  gap: 8px;
}
.app-card-title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand-gradient);
}
</style>
