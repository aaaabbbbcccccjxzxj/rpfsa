import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
// 全局设计系统（与用户端同一套品牌色板），晚于 element 样式引入以便覆盖
import './styles/global.css'
import router from './router'
// 全局 axios 配置（baseURL / 管理员 token / 401 处理）
import './utils/request'

createApp(App).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
