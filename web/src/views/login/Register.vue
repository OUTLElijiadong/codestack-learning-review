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
        <h1 class="brand-title">加入码栈<br/>开始你的编程复盘之旅</h1>
        <p class="brand-slogan">记录每一道错题，见证每一步成长</p>
        <div class="brand-tags">
          <span class="brand-tag">错题归档</span>
          <span class="brand-tag">代码高亮</span>
          <span class="brand-tag">复盘日历</span>
          <span class="brand-tag">数据统计</span>
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-card rise-in rise-in-1">
        <div class="form-head">
          <h2 class="form-title">注册账号</h2>
          <p class="form-sub">注册即设置密保问题，忘记密码时可通过密保找回</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="formRules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（3-20 位字母/数字/下划线）" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（6-20 位）" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="securityQuestion">
            <el-input v-model="form.securityQuestion" placeholder="密保问题，如：我的班主任姓什么？" :prefix-icon="QuestionFilled" />
          </el-form-item>
          <el-form-item prop="securityAnswer">
            <el-input v-model="form.securityAnswer" placeholder="密保答案（找回密码的唯一凭证）" :prefix-icon="Key" />
          </el-form-item>

          <button type="button" class="login-submit" :disabled="loading" @click="submit">
            {{ loading ? '注册中…' : '注 册' }}
          </button>

          <div class="form-foot">
            已有账号？<router-link class="link" to="/login">返回登录</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Key, Avatar, QuestionFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { rules, confirmPasswordRule } from '@/utils/validate'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  securityQuestion: '',
  securityAnswer: ''
})

const formRules = {
  username: rules.username,
  nickname: rules.nickname,
  password: rules.password,
  confirmPassword: confirmPasswordRule(() => form.password),
  securityQuestion: rules.securityQuestion,
  securityAnswer: rules.securityAnswer
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录！')
    router.push('/login')
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 复用登录页视觉体系 */
.login-page { display: flex; min-height: 100vh; }
.brand-panel {
  width: 44%;
  position: relative;
  overflow: hidden;
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
.brand-slogan { color: rgba(255, 255, 255, 0.75); font-size: 14px; margin: 0 0 32px; }
.brand-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.brand-tag {
  background: rgba(255, 255, 255, 0.14); border: 1px solid rgba(255, 255, 255, 0.28);
  color: #fff; border-radius: 999px; padding: 6px 14px; font-size: 12px;
}
.form-panel {
  flex: 1; background: #F8FAFC;
  background-image: radial-gradient(600px at 70% 20%, #EEF2FF, transparent);
  display: flex; align-items: center; justify-content: center; padding: 40px 24px;
}
.form-card {
  width: 420px; background: #fff; border-radius: var(--radius-xl);
  box-shadow: var(--shadow-pop); padding: 40px;
}
.form-head { text-align: center; margin-bottom: 24px; }
.form-title { font-size: 24px; font-weight: 700; color: var(--text-title); margin: 0 0 6px; }
.form-sub { font-size: 13px; color: var(--text-sub); margin: 0; }
.login-submit {
  width: 100%; height: 44px; border: none; border-radius: var(--radius-md);
  background: var(--brand-gradient); color: #fff; font-size: 15px; font-weight: 600;
  letter-spacing: 4px; cursor: pointer; box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.login-submit:hover { transform: translateY(-1px); filter: brightness(1.06); }
.login-submit:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }
.form-foot { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-sub); }
.link { color: var(--primary); font-size: 13px; text-decoration: none; }
.link:hover { text-decoration: underline; }
@media (max-width: 992px) { .brand-panel { display: none; } }
</style>
