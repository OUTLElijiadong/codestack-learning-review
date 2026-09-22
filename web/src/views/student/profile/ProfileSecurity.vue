<template>
  <div class="security-page">
    <div class="security-grid">
      <!-- 修改登录密码 -->
      <AppCard title="修改登录密码">
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top" size="large">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <button type="button" class="save-btn" @click="changePwd">确认修改（改完需重新登录）</button>
        </el-form>
      </AppCard>

      <!-- 修改密保 -->
      <AppCard title="密保设置（找回密码的唯一凭证）">
        <el-form ref="secRef" :model="secForm" :rules="secRules" label-position="top" size="large">
          <el-form-item label="登录密码（验证身份）" prop="password">
            <el-input v-model="secForm.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="密保问题" prop="securityQuestion">
            <el-input v-model="secForm.securityQuestion" placeholder="如：我的班主任姓什么？" maxlength="64" />
          </el-form-item>
          <el-form-item label="密保答案" prop="securityAnswer">
            <el-input v-model="secForm.securityAnswer" maxlength="32" />
          </el-form-item>
          <button type="button" class="save-btn" @click="changeSecurity">保存密保</button>
        </el-form>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, updateSecurity, getProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { rules, confirmPasswordRule } from '@/utils/validate'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()
const pwdRef = ref(null)
const secRef = ref(null)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const secForm = reactive({ password: '', securityQuestion: '', securityAnswer: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: rules.password,
  confirmPassword: confirmPasswordRule(() => pwdForm.newPassword)
}
const secRules = {
  password: [{ required: true, message: '请输入登录密码验证身份', trigger: 'blur' }],
  securityQuestion: rules.securityQuestion,
  securityAnswer: rules.securityAnswer
}

async function changePwd() {
  try {
    await pwdRef.value.validate()
  } catch (e) { return }
  await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
  ElMessage.success('密码修改成功，请重新登录')
  await userStore.logout()
  router.push('/login')
}

async function changeSecurity() {
  try {
    await secRef.value.validate()
  } catch (e) { return }
  await updateSecurity(secForm)
  ElMessage.success('密保已更新')
  secForm.password = ''
}

onMounted(async () => {
  const res = await getProfile()
  secForm.securityQuestion = res.data.securityQuestion || ''
})
</script>

<style scoped>
.security-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  max-width: 980px;
  margin: 0 auto;
}
.save-btn {
  width: 100%; height: 42px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
  margin-top: 6px;
}
.save-btn:hover { filter: brightness(1.06); }
@media (max-width: 900px) { .security-grid { grid-template-columns: 1fr; } }
</style>
