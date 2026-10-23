import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  // 与 .env 中的 VITE_API_BASE_URL 保持一致；后端地址可用 VITE_PROXY_TARGET 覆盖
  const apiBase = env.VITE_API_BASE_URL || '/api'
  const target = env.VITE_PROXY_TARGET || 'http://localhost:80'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      // 开发期由 vite 统一转发，解决跨域，后端也就不必再开 @CrossOrigin
      proxy: {
        [apiBase]: {
          target,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp('^' + apiBase), '')
        },
        // 上传后回显的图片
        '/img': {
          target,
          changeOrigin: true
        }
      }
    }
  }
})
