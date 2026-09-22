import request from '@/utils/request'

/* 公共文件上传：返回可访问 URL（带 /api 前缀，直接可作 img src） */
export function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/common/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
