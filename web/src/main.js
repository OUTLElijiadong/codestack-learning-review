import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

/* 样式引入顺序（顺序就是生命：覆盖必须在被覆盖之后）：
   Element 基础样式 → 设计令牌 → Element 主题覆盖 → 代码高亮 → 全局基座 */
import 'element-plus/dist/index.css'
import '@/assets/styles/variables.css'
import '@/assets/styles/element-overrides.css'
import '@wangeditor/editor/dist/css/style.css'
import 'highlight.js/styles/atom-one-dark.css'
import '@/assets/styles/global.css'

/* 代码等宽字体本地打包（答辩现场断网也能用），不走 Google Fonts CDN */
import '@fontsource/jetbrains-mono/400.css'
import '@fontsource/jetbrains-mono/500.css'
import '@fontsource/jetbrains-mono/700.css'

const app = createApp(App)

// 1. 先装 Pinia：路由守卫里会调用 useUserStore()，必须先于路由就绪
app.use(createPinia())

// 2. 再装路由：install 时会触发首次导航，此时 Pinia 已可用
app.use(router)

// 3. Element Plus 全量引入 + 中文语言包（分页/日期/弹窗文案全中文）
app.use(ElementPlus, { locale: zhCn })

// 4. 全量全局注册 Element 图标，模板与菜单直接用组件名
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.mount('#app')
