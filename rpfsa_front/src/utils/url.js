/**
 * 静态资源与上传地址的唯一出口。
 * <p>
 * 改造前这些地址散落在 6 个组件里，且有两种写法
 * （{@code new URL('img/' + url, 'http://localhost/')} 与字符串拼接），
 * 换环境必然漏改。
 */

import { ElMessage } from 'element-plus'

const STATIC_BASE = (import.meta.env.VITE_STATIC_BASE_URL || '/').replace(/\/$/, '')
const API_BASE = (import.meta.env.VITE_API_BASE_URL || '/').replace(/\/$/, '')

/**
 * 把后端返回的图片文件名拼成可直接访问的地址。
 *
 * @param {string} name 文件名，如 {@code 1684806569115.jpg}
 * @returns {string} 完整地址，如 {@code /img/1684806569115.jpg}
 */
export function resolveImg(name) {
  if (!name) {
    return ''
  }
  // 已经是绝对地址或 base64 的原样返回
  if (/^(https?:)?\/\//.test(name) || name.startsWith('data:')) {
    return name
  }
  return `${STATIC_BASE}/img/${name}`
}

/** el-upload 的提交地址 */
export function resolveUploadUrl() {
  return `${API_BASE}/upload`
}

/**
 * el-upload 的请求头。el-upload 用原生表单提交，不走 axios 拦截器，
 * 而 /upload 接口要求登录（@RequireLogin），必须显式携带 token，
 * 否则上传会 401（表现为"头像无法修改"）。
 */
export function uploadHeaders() {
  const token = sessionStorage.getItem('token') || sessionStorage.getItem('adminToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

/** el-upload 上传失败的统一提示（如 token 失效导致的 401） */
export function uploadError() {
  ElMessage.error('图片上传失败，请重新登录后再试')
}
