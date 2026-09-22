<template>
  <div class="login-page">
    <div class="brand-panel">
      <div class="glow glow-a glow-float"></div>
      <div class="glow glow-b glow-float-slow"></div>
      <div class="brand-inner">
        <div class="brand-logo rise-in">
          <div class="brand-logo-icon">&lt;/&gt;</div>
          <span class="brand-logo-name">码栈 CodeStack</span>
        </div>
        <h1 class="brand-title">忘记密码<br/>密保帮你找回来</h1>
        <p class="brand-slogan">回答注册时设置的密保问题，即可重置登录密码</p>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-card rise-in rise-in-1">
        <div class="form-head">
          <h2 class="form-title">找回密码</h2>
          <p class="form-sub">两步完成：验证密保 → 设置新密码</p>
        </div>

        <el-steps :active="step" align-center finish-status="success" class="steps">
          <el-step title="验证密保" />
          <el-step title="设置新密码" />
        </el-steps>

        <!-- 第一步：输用户名，取密保问题并作答 -->
        <el-form v-if="step === 0" ref="step1Ref" :model="form" :rules="step1Rules" size="large" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" @blur="loadQuestion" />
          </el-form-item>
          <template v-if="question">
            <el-form-item label="密保问题">
              <div class="question-box">{{ question }}</div>
            </el-form-item>
            <el-form-item label="密保答案" prop="securityAnswer">
              <el-input v-model="form.securityAnswer" placeholder="请输入密保答案" />
            </el-form-item>
          </template>
          <button type="button" class="login-submit" @click="nextStep">下一步</button>
        </el-form>

        <!-- 第二步：设新密码 -->
        <el-form v-else ref="step2Ref" :model="form" :rules="step2Rules" size="large" label-position="top">
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="6-20 位新密码" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
          </el-form-item>
          <div class="btn-row">
            <el-button @click="step = 0">上一步</el-button>
            <button type="button" class="login-submit btn-flex" :disabled="loading" @click="submit">
              {{ loading ? '提交中…' : '重置密码' }}
            </button>
          </div>
        </el-form>

        <div class="form-foot">
          想起来了？<router-link class="link" to="/login">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSecurityQuestion, resetPassword } from '@/api/auth'
import { rules, confirmPasswordRule } from '@/utils/validate'

const router = useRouter()
const step = ref(0)
const loading = ref(false)
const question = ref('')
const step1Ref = ref(null)
const step2Ref = ref(null)

const form = reactive({
  username: '',
  securityAnswer: '',
  newPassword: '',
  confirmPassword: ''
})

const step1Rules = {
  username: rules.username,
  securityAnswer: [{ required: true, message: '请输入密保答案', trigger: 'blur' }]
}
const step2Rules = {
  newPassword: rules.password,
  confirmPassword: confirmPasswordRule(() => form.newPassword)
}

async function loadQuestion() {
  if (!form.username || form.username.length < 3) return
  try {
    const res = await getSecurityQuestion(form.username)
    question.value = res.data.question
  } catch (e) {
    question.value = ''
  }
}

async function nextStep() {
  try {
    await step1Ref.value.validate()
  } catch (e) {
    return
  }
  if (!question.value) {
    ElMessage.warning('请先输入用户名并加载密保问题')
    return
  }
  step.value = 1
}

async function submit() {
  try {
    await step2Ref.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await resetPassword({
      username: form.username,
      securityAnswer: form.securityAnswer,
      newPassword: form.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch (e) { /* 拦截器已提示（密保答案错误等） */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; min-height: 100vh; }
.brand-panel {
  width: 44%; position: relative; overflow: hidden;
  background:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    var(--brand-gradient-deep);
  background-size: 36px 36px, 36px 36px, 100% 100%;
  display: flex; align-items: center; justify-content: center;
}
.glow { position: absolute; border-radius: 50%; filter: blur(70px); pointer-events: none; }
.glow-a { width: 320px; height: 320px; background: #818CF8; opacity: 0.5; top: -80px; left: -60px; }
.glow-b { width: 280px; height: 280px; background: #C4B5FD; opacity: 0.4; bottom: -60px; right: -40px; }
.brand-inner { position: relative; padding: 48px; max-width: 520px; }
.brand-logo { display: flex; align-items: center; gap: 12px; margin-bottom: 40px; }
.brand-logo-icon {
  width: 40px; height: 40px; border-radius: 12px;
  background: rgba(255, 255, 255, 0.16); border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff; font-family: var(--font-mono); font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.brand-logo-name { color: #fff; font-size: 18px; font-weight: 700; }
.brand-title { color: #fff; font-size: 30px; font-weight: 700; line-height: 1.5; margin: 0 0 12px; }
.brand-slogan { color: rgba(255, 255, 255, 0.75); font-size: 14px; margin: 0; }
.form-panel {
  flex: 1; background: #F8FAFC;
  background-image: radial-gradient(600px at 70% 20%, #EEF2FF, transparent);
  display: flex; align-items: center; justify-content: center; padding: 40px 24px;
}
.form-card {
  width: 440px; background: #fff; border-radius: var(--radius-xl);
  box-shadow: var(--shadow-pop); padding: 40px;
}
.form-head { text-align: center; margin-bottom: 24px; }
.form-title { font-size: 24px; font-weight: 700; color: var(--text-title); margin: 0 0 6px; }
.form-sub { font-size: 13px; color: var(--text-sub); margin: 0; }
.steps { margin-bottom: 28px; }
.question-box {
  width: 100%; background: var(--primary-bg); color: var(--primary-hover);
  border: 1px solid var(--primary-border); border-radius: var(--radius-btn);
  padding: 10px 14px; font-size: 14px; font-weight: 500;
}
.login-submit {
  width: 100%; height: 44px; border: none; border-radius: var(--radius-md);
  background: var(--brand-gradient); color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.login-submit:hover { transform: translateY(-1px); filter: brightness(1.06); }
.login-submit:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }
.btn-row { display: flex; gap: 12px; align-items: center; }
.btn-flex { flex: 1; }
.form-foot { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-sub); }
.link { color: var(--primary); font-size: 13px; text-decoration: none; }
.link:hover { text-decoration: underline; }
@media (max-width: 992px) { .brand-panel { display: none; } }
</style>
