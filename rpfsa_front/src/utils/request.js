import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 全局 axios 配置。
 * <p>
 * 改造前：{@code main.js} 里硬编码 {@code axios.defaults.baseURL = 'http://localhost'}，
 * 换个部署地址就得全项目搜索替换；且每个请求各写各的 catch。
 * <p>
 * 设计取舍：这里直接配置 axios 实例本身（而不是 new 一个新实例），
 * 这样各组件既有的 {@code import axios from 'axios'} 无需改动即可获得
 * 统一 baseURL / token / 401 处理，改动面最小。
 * 响应拦截器不解包 data，保持组件里 {@code res.data.code} 的既有写法不变。
 */
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || '/'
axios.defaults.timeout = 15000

/** 携带登录令牌 */
axios.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** 处理 HTTP 层异常：401 清理登录态并跳转，其余给出可读提示 */
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response && error.response.status
    const msg = error.response && error.response.data && error.response.data.msg
    if (status === 401) {
      sessionStorage.clear()
      ElMessage.error(msg || '登录已过期，请重新登录')
      if (window.location.hash !== '#/login') {
        window.location.hash = '#/login'
      }
    } else if (status === 403) {
      ElMessage.error(msg || '没有操作权限')
    } else if (!error.response) {
      ElMessage.error('网络异常，请确认后端服务已启动')
    }
    return Promise.reject(error)
  }
)

export default axios
