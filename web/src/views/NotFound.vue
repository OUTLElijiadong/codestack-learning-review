<template>
  <div class="not-found">
    <div class="nf-code">404</div>
    <p class="nf-text">页面不存在或已被移除</p>
    <el-button type="primary" round @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function goHome() {
  router.push(userStore.userInfo ? userStore.homePath : '/login')
}
</script>

<style scoped>
.not-found {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}
.nf-code {
  font-size: 96px;
  font-weight: 800;
  font-family: var(--font-mono);
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  line-height: 1;
}
.nf-text { color: var(--text-sub); margin: 0 0 10px; }
</style>
