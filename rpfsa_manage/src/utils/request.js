import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 全局 axios 配置（管理端）。
 * <p>
 * 与用户端保持同一套约定：直接配置 axios 实例本身，
 * 各组件原有的 {@code axios.get/post} 调用无需改写即可获得
 * 统一 baseURL、管理员 token、401 跳登录。
 */
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || '/'
axios.defaults.timeout = 15000

axios.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('adminToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response && error.response.status
    const msg = error.response && error.response.data && error.response.data.msg
    if (status === 401) {
      sessionStorage.clear()
      ElMessage.error(msg || '登录已过期，请重新登录')
      if (window.location.hash !== '#/') {
        window.location.hash = '#/'
      }
    } else if (status === 403) {
      ElMessage.error(msg || '需要管理员权限')
    } else if (!error.response) {
      ElMessage.error('网络异常，请确认后端服务已启动')
    }
    return Promise.reject(error)
  }
)

export default axios
