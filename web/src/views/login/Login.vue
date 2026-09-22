<template>
  <div class="login-page">
    <!-- 左侧品牌区：渐变深底 + 网格底纹 + 光斑 + 仿真代码窗 -->
    <div class="brand-panel">
      <div class="glow glow-a glow-float"></div>
      <div class="glow glow-b glow-float-slow"></div>
      <div class="brand-inner">
        <div class="brand-logo rise-in">
          <div class="brand-logo-icon">&lt;/&gt;</div>
          <span class="brand-logo-name">码栈 CodeStack</span>
        </div>
        <h1 class="brand-title rise-in rise-in-1">基于 Vue 的<br/>个人代码学习错题集<br/>与编程笔记复盘系统</h1>
        <p class="brand-slogan rise-in rise-in-2">记录每一道错题，见证每一步成长</p>

        <!-- 仿真代码编辑器装饰窗（本页点睛之笔，纯 HTML/CSS 手工上色） -->
        <div class="brand-window rise-in rise-in-3">
          <div class="brand-window-bar">
            <i class="dot dot-r"></i><i class="dot dot-y"></i><i class="dot dot-g"></i>
            <span class="brand-window-title">today-review.vue</span>
          </div>
          <pre class="brand-code"><span class="tk-c">// 今日复盘 · Vue 响应式</span>
<span class="tk-k">const</span> mistakes = <span class="tk-f">ref</span>(<span class="tk-n">0</span>)
<span class="tk-k">const</span> progress = <span class="tk-f">computed</span>(() => mistakes.value * <span class="tk-n">2</span>)
<span class="tk-f">watch</span>(mistakes, (val) => {
  console.<span class="tk-f">log</span>(<span class="tk-s">'今日已复盘'</span>, val)
})</pre>
        </div>

        <div class="brand-tags rise-in rise-in-4">
          <span class="brand-tag">错题归档</span>
          <span class="brand-tag">代码高亮</span>
          <span class="brand-tag">复盘日历</span>
          <span class="brand-tag">数据统计</span>
        </div>
        <div class="brand-version">v2.0 · 答辩整改版（2026-09-22）· 含身份下拉登录 / 豆包AI / 收藏总览 / 浅色大屏</div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-card rise-in rise-in-1">
        <div class="form-head">
          <div class="form-logo-icon">&lt;/&gt;</div>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-sub">登录码栈，继续今天的编程复盘</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="formRules" size="large" @keyup.enter="submit">
          <el-form-item prop="role">
            <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
              <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value">
                <span style="float: left">{{ r.label }}</span>
                <span style="float: right; color: var(--text-placeholder); font-size: 12px">{{ r.hint }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名 / 手机号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input v-model="form.captchaCode" placeholder="图形验证码" :prefix-icon="Key" maxlength="4" />
              <img
                v-if="captchaImg"
                class="captcha-img"
                :src="captchaImg"
                alt="验证码"
                title="点击刷新验证码"
                @click="loadCaptcha"
              />
              <div v-else class="captcha-img captcha-loading" @click="loadCaptcha">点击加载</div>
            </div>
          </el-form-item>

          <div class="form-links">
            <el-checkbox v-model="remember">记住我</el-checkbox>
            <router-link class="link" to="/forgot-password">忘记密码？</router-link>
          </div>

          <button type="button" class="login-submit" :disabled="loading" @click="submit">
            {{ loading ? '登录中…' : '登 录' }}
          </button>

          <div class="form-foot">
            还没有账号？<router-link class="link" to="/register">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { rules } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const remember = ref(true)
const captchaImg = ref('')

const roles = [
  { value: 'student', label: '学生', hint: '登录个人错题集' },
  { value: 'teacher', label: '教师', hint: '登录管理后台' },
  { value: 'admin', label: '超级管理员', hint: '登录管理后台' }
]

const form = reactive({
  role: 'student',
  username: '',
  password: '',
  captchaCode: '',
  captchaUuid: ''
})

const formRules = {
  role: [{ required: true, message: '请选择登录身份', trigger: 'change' }],
  username: rules.username,
  password: rules.password,
  captchaCode: rules.captcha
}

async function loadCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImg.value = res.data.img
    form.captchaUuid = res.data.uuid
    form.captchaCode = ''
  } catch (e) { /* 拦截器已提示 */ }
}

async function submit() {
  // 校验失败直接返回，不产生未处理的 Promise 拒绝
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    // 记住我：记住用户名，下次打开自动回填
    if (remember.value) {
      localStorage.setItem('review_remember_name', form.username)
      localStorage.setItem('review_remember_role', form.role)
    } else {
      localStorage.removeItem('review_remember_name')
    }
    ElMessage.success('登录成功，欢迎回来！')
    // 优先回跳登录前想去的页面，否则按角色进各自端首页
    const redirect = route.query.redirect
    router.push(redirect || userStore.homePath)
  } catch (e) {
    loadCaptcha() // 登录失败刷新验证码（一次性防重放）
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 回填上次记住的用户名
  const remembered = localStorage.getItem('review_remember_name')
  if (remembered) {
    form.username = remembered
    remember.value = true
  }
  const rememberedRole = localStorage.getItem('review_remember_role')
  if (rememberedRole) {
    form.role = rememberedRole
  }
  loadCaptcha()
})
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}

/* ---------- 左侧品牌区 ---------- */
.brand-panel {
  width: 44%;
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    var(--brand-gradient-deep);
  background-size: 36px 36px, 36px 36px, 100% 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  pointer-events: none;
}
.glow-a { width: 320px; height: 320px; background: #818CF8; opacity: 0.5; top: -80px; left: -60px; }
.glow-b { width: 280px; height: 280px; background: #C4B5FD; opacity: 0.4; bottom: -60px; right: -40px; }

.brand-inner { position: relative; padding: 48px; max-width: 520px; }
.brand-logo { display: flex; align-items: center; gap: 12px; margin-bottom: 40px; }
.brand-logo-icon {
  width: 40px; height: 40px; border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff; font-family: var(--font-mono); font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.brand-logo-name { color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
.brand-title { color: #fff; font-size: 30px; font-weight: 700; line-height: 1.5; margin: 0 0 12px; }
.brand-slogan { color: rgba(255, 255, 255, 0.75); font-size: 14px; margin: 0 0 32px; }

.brand-window {
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 28px;
}
.brand-window-bar {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.dot { width: 10px; height: 10px; border-radius: 50%; }
.dot-r { background: #EF4444; }
.dot-y { background: #F59E0B; }
.dot-g { background: #10B981; }
.brand-window-title { margin-left: 8px; font-family: var(--font-mono); font-size: 11px; color: rgba(255,255,255,0.5); }
.brand-code {
  margin: 0;
  padding: 16px 18px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.9;
  color: #E2E8F0;
}
.tk-c { color: #7C8DB0; }
.tk-k { color: #C4B5FD; }
.tk-f { color: #7DD3FC; }
.tk-s { color: #86EFAC; }
.tk-n { color: #FCA5A5; }

.brand-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.brand-version { margin-top: 26px; font-size: 11px; color: rgba(255, 255, 255, 0.45); font-family: var(--font-mono); letter-spacing: 0.5px; }
.brand-tag {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.28);
  color: #fff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
}

/* ---------- 右侧表单区 ---------- */
.form-panel {
  flex: 1;
  background: #F8FAFC;
  background-image: radial-gradient(600px at 70% 20%, #EEF2FF, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.form-card {
  width: 420px;
  background: #fff;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-pop);
  padding: 40px;
}
.form-head { text-align: center; margin-bottom: 28px; }
.form-logo-icon {
  width: 44px; height: 44px; border-radius: 12px; margin: 0 auto 14px;
  background: var(--brand-gradient); color: #fff;
  font-family: var(--font-mono); font-weight: 700; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
}
.form-title { font-size: 24px; font-weight: 700; color: var(--text-title); margin: 0 0 6px; }
.form-sub { font-size: 13px; color: var(--text-sub); margin: 0; }

.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-row .el-input { flex: 1; }
.captcha-img {
  width: 112px;
  height: 40px;
  border-radius: var(--radius-btn);
  border: 1px solid var(--gray-200);
  cursor: pointer;
  flex-shrink: 0;
  object-fit: cover;
}
.captcha-loading {
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: var(--text-placeholder);
}

.form-links {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.link { color: var(--primary); font-size: 13px; text-decoration: none; }
.link:hover { color: var(--primary-hover); text-decoration: underline; }

.login-submit {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--brand-gradient);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.login-submit:hover { transform: translateY(-1px); filter: brightness(1.06); }
.login-submit:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }

.form-foot { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-sub); }

/* 小屏隐藏左侧品牌区 */
@media (max-width: 992px) {
  .brand-panel { display: none; }
}
</style>
