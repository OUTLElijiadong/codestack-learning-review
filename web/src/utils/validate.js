// 通用表单校验规则，登录/注册/找回密码/安全设置共用
export const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度为 3-20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字、下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度为 6-20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称最长 20 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题最长 100 个字符', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请设置密保问题（找回密码用）', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请设置密保答案', trigger: 'blur' }
  ]
}

// 确认密码校验：getPassword 为函数，返回表单中 password 的当前值
export function confirmPasswordRule(getPassword) {
  return [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== getPassword()) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
