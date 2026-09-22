import request from '@/utils/request'

/* 认证与个人中心（/auth 前缀，白名单 + 任意登录角色可用） */

// 获取图形验证码
export function getCaptcha() {
  return request.get('/auth/captcha')
}

// 账号密码 + 图形验证码登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 学生注册（含密保问题/答案）
export function register(data) {
  return request.post('/auth/register', data)
}

// 找回密码第一步：按用户名取密保问题
export function getSecurityQuestion(username) {
  return request.get('/auth/security-question', { params: { username } })
}

// 找回密码第二步：密保答案 + 新密码
export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前登录人信息（刷新页面后恢复用户状态）
export function getProfile() {
  return request.get('/auth/profile')
}

// 修改个人资料（昵称/学习方向/专业班级/简介）
export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

// 修改登录密码
export function changePassword(data) {
  return request.put('/auth/profile/password', data)
}

// 修改头像（先经 /common/upload 传图，再提交返回的 URL）
export function updateAvatar(avatar) {
  return request.put('/auth/profile/avatar', { avatar })
}

// 修改密保问题与答案（需先验登录密码）
export function updateSecurity(data) {
  return request.put('/auth/profile/security', data)
}
