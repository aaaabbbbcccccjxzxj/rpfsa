import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 全局设计系统（tokens + Element Plus 主题接管），必须晚于 element 样式引入才能覆盖
import './styles/global.css'
import router from './router'
// 全局 axios 配置（baseURL / token / 401 处理），必须早于任何组件发起请求
import './utils/request'

createApp(App).use(router).use(ElementPlus).mount('#app')
