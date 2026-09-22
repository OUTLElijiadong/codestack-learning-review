import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from '@/utils/auth'

// 统一 Axios 实例；baseURL 为 /api，开发环境由 vite proxy 转发到后端 8080
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 JWT
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一拆解 Result{code, msg, data}
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res // 业务页拿到的就是 { code, msg, data }
    }
    if (res.code === 401) {
      // token 缺失/失效：清 token、跳登录
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(res.msg || 'Unauthorized'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  (error) => {
    // HTTP 层兜底（后端拦截器直接返回的状态码）
    if (error.response && error.response.status === 401) {
      removeToken()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
