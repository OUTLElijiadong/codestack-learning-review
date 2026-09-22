import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// Vite 配置：插件、@ 别名、开发服务器与 /api 代理
// base 说明：生产构建打进 Spring Boot 的 static 目录随 /api 上下文一起对外，
// 因此产物资源前缀必须是 /api/；本地 dev 仍在根路径，避免与 /api 代理冲突
export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/api/' : '/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}))
