<template>
  <div class="empty-state">
    <el-empty :image-size="140" :description="description">
      <!-- 默认插槽：外部可完全自定义；不传则显示引导按钮 -->
      <slot>
        <el-button v-if="btnText" type="primary" round @click="$emit('action')">{{ btnText }}</el-button>
      </slot>
    </el-empty>
  </div>
</template>

<script setup>
// 统一空状态：文案有温度 + 引导按钮
defineProps({
  description: { type: String, default: '暂无数据' },
  btnText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  padding: var(--space-10) 0;
  display: flex;
  justify-content: center;
}
</style>
