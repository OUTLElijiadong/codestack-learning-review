<template>
  <div class="profile-page" v-loading="loading">
    <div class="profile-grid">
      <!-- 左：头像卡片 -->
      <AppCard class="avatar-card" title="我的头像">
        <div class="avatar-zone">
          <div class="big-avatar" :style="avatarStyle">{{ avatarText }}</div>
          <el-upload
            :show-file-list="false"
            :http-request="doUploadAvatar"
            :before-upload="beforeUpload"
            accept=".jpg,.jpeg,.png,.webp"
          >
            <el-button round>更换头像</el-button>
          </el-upload>
          <p class="avatar-tip">支持 jpg/png/webp，不超过 10MB</p>
        </div>
        <div class="account-brief">
          <p><span class="brief-label">用户名</span>{{ profile.username }}</p>
          <p><span class="brief-label">角色</span>学生</p>
          <p><span class="brief-label">注册时间</span>{{ fmtDate(profile.createTime) }}</p>
          <p><span class="brief-label">最近登录</span>{{ fmtTime(profile.lastLoginTime) }}</p>
        </div>
      </AppCard>

      <!-- 右：学习档案表单 -->
      <AppCard title="编程学习档案" class="profile-form-card">
        <el-form :model="form" label-position="top" size="large">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学习方向">
                <el-radio-group v-model="form.learnDirection">
                  <el-radio-button v-for="d in LEARN_DIRECTIONS" :key="d.value" :value="d.value">
                    {{ d.label }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="专业">
                <el-input v-model="form.major" placeholder="如：软件工程" maxlength="64" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班级">
                <el-input v-model="form.className" placeholder="如：软件23H03" maxlength="64" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱（选填）">
                <el-input v-model="form.email" maxlength="64" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号（选填）">
                <el-input v-model="form.phone" maxlength="20" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="个人简介 / 学习宣言">
            <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="写一句激励自己的话…" />
          </el-form-item>
          <div class="submit-row">
            <button type="button" class="save-btn" :disabled="saving" @click="save">
              {{ saving ? '保存中…' : '保存档案' }}
            </button>
          </div>
        </el-form>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updateAvatar } from '@/api/auth'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'
import { LEARN_DIRECTIONS } from '@/constants/dict'
import { fmtDate, fmtTime } from '@/utils/format'
import AppCard from '@/components/AppCard.vue'

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const profile = ref({})

const form = reactive({
  nickname: '',
  learnDirection: '',
  major: '',
  className: '',
  bio: '',
  email: '',
  phone: ''
})

const avatarText = computed(() => (profile.value.nickname || profile.value.username || '同')[0] || '同')
const avatarStyle = computed(() => {
  if (profile.value.avatar) {
    return { backgroundImage: `url(${profile.value.avatar})`, backgroundSize: 'cover', backgroundPosition: 'center', color: 'transparent' }
  }
  return { background: 'var(--brand-gradient)', color: '#fff' }
})

async function load() {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
    Object.assign(form, {
      nickname: res.data.nickname || '',
      learnDirection: res.data.learnDirection || '',
      major: res.data.major || '',
      className: res.data.className || '',
      bio: res.data.bio || '',
      email: res.data.email || '',
      phone: res.data.phone || ''
    })
  } finally {
    loading.value = false
  }
}

function beforeUpload(file) {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function doUploadAvatar({ file }) {
  const res = await uploadImage(file)
  await updateAvatar(res.data.url)
  profile.value.avatar = res.data.url
  await userStore.fetchProfile()
  ElMessage.success('头像已更新')
}

async function save() {
  saving.value = true
  try {
    await updateProfile(form)
    await userStore.fetchProfile()
    ElMessage.success('学习档案已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile-grid { display: flex; gap: 20px; align-items: flex-start; max-width: 1080px; margin: 0 auto; }
.avatar-card { width: 300px; flex-shrink: 0; }
.avatar-zone { display: flex; flex-direction: column; align-items: center; gap: 14px; padding: 10px 0 18px; }
.big-avatar {
  width: 96px; height: 96px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 36px; font-weight: 700;
  border: 3px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
}
.avatar-tip { font-size: 12px; color: var(--text-placeholder); margin: 0; }
.account-brief { border-top: 1px solid var(--gray-100); padding-top: 14px; }
.account-brief p { font-size: 13px; color: var(--text-main); margin: 8px 0; }
.brief-label {
  display: inline-block; width: 70px;
  color: var(--text-placeholder); font-size: 12px;
}
.profile-form-card { flex: 1; min-width: 0; }
.submit-row { display: flex; justify-content: flex-end; border-top: 1px solid var(--gray-100); padding-top: 18px; }
.save-btn {
  height: 40px; padding: 0 30px; border: none; border-radius: 999px;
  background: var(--brand-gradient); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: var(--shadow-btn);
  transition: all var(--dur-base) var(--ease-out);
}
.save-btn:hover { transform: translateY(-1px); filter: brightness(1.06); }
.save-btn:disabled { opacity: 0.7; cursor: not-allowed; }
@media (max-width: 992px) { .profile-grid { flex-direction: column; } .avatar-card { width: 100%; } }
</style>
