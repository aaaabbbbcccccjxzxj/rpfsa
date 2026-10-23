import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
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
      port: 5174,
      // 开发期由 vite 统一转发，解决跨域，后端可不再开放 @CrossOrigin
      proxy: {
        [apiBase]: {
          target,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp('^' + apiBase), '')
        },
        '/img': {
          target,
          changeOrigin: true
        }
      }
    }
  }
})
