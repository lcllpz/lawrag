import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 后端统一 Result 包装：{ code, message, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    const { code, message, data } = res || {}
    if (code === 200) return data
    // 401 等场景这里不强制跳转（chat-h5 目前不保证存在登录路由）
    throw new Error(message || '请求失败')
  },
  (error) => {
    const msg =
      error?.response?.data?.message || error?.message || '网络错误'
    // 让调用方决定如何展示 toast/弹窗
    return Promise.reject(new Error(msg))
  }
)

export default request

