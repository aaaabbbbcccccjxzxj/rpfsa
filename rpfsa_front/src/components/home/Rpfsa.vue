<template>
    <!-- 导航栏 -->
    <div class="navbar">
        <div class="nav-inner">
            <div class="logo">
                <RouterLink to="/introduce" class="brand">
                    <img src="/logo.png" alt="Logo" />
                    <span class="brand-name">流浪猫狗之家</span>
                </RouterLink>
            </div>
            <ul class="nav-links">
                <li>
                    <RouterLink to="/introduce">首页</RouterLink>
                </li>
                <li>
                    <RouterLink to="/dogandcat">流浪之家</RouterLink>
                </li>
                <li>
                    <RouterLink to="/community">社区讨论</RouterLink>
                </li>
                <li>
                    <RouterLink to="/thank">贡献与鸣谢</RouterLink>
                </li>
                <li>
                    <RouterLink to="/about">关于我们</RouterLink>
                </li>
                <li v-if="!usersession">
                    <RouterLink to="/login">登录</RouterLink>
                </li>
                <template v-else>
                    <!-- 消息铃铛：聚合三类未读 -->
                    <li>
                        <RouterLink to="/messages" class="bell-link" title="消息中心">
                            <el-badge :value="totalUnread || undefined" :max="99" class="bell-badge">
                                <span class="bell-icon">🔔</span>
                            </el-badge>
                        </RouterLink>
                    </li>
                    <li>
                        <el-dropdown>
                            <span class="user-chip">{{ nameonick }}<el-icon><arrow-down /></el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item>
                                        <RouterLink style="text-decoration: none;" to="/usercenter">个人中心</RouterLink>
                                    </el-dropdown-item>
                                    <el-dropdown-item>
                                        <RouterLink style="text-decoration: none;" to="/messages">消息中心</RouterLink>
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="exit">退出</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </li>
                </template>
            </ul>
        </div>
    </div>
    <!-- 视图内容 -->
    <RouterView></RouterView>
    <!-- 页脚 -->
    <footer class="footer">
        <div class="footer-inner">
            <p>© 2023 流浪猫狗之家系统平台. All rights reserved. | 备案号:XXXXXXXXX</p>
        </div>
    </footer>
</template>


<script setup>
import { computed, inject, ref, provide, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

// 定时器句柄。改造前 setInterval 直接写在 setup 顶层且从不清理，
// 组件每次重建都会多留一个定时器（配合 App.vue 的"快速刷新"会持续累积）。
let messageTimer = null

onMounted(() => {
    getlatestmessage()
    // 未读消息轮询间隔：1 小时
    messageTimer = setInterval(getlatestmessage, 1000 * 60 * 60)
})

onUnmounted(() => {
    if (messageTimer) {
        clearInterval(messageTimer)
        messageTimer = null
    }
})
// 传递'个人中心'的数据
const adoptnews = ref()
provide('adoptnews', adoptnews)
const donatenews = ref()
provide('donatenews', donatenews)
const messagenews = ref()
provide('messagenews', messagenews)
// 用户session
const usersession = JSON.parse(sessionStorage.getItem("user"))
function getlatestmessage() {
    if (usersession != null) {
        //发请求
        axios.get(`/adopt/noread/${usersession.id}`).then((res) => {
            if (res.data.data) adoptnews.value = res.data.data
        })
        axios.get(`/donate/noread/${usersession.id}`).then((res) => {
            if (res.data.data) donatenews.value = res.data.data
        })
        axios.get(`/message/noread/${usersession.id}/user`).then((res) => {
            if (res.data.data) messagenews.value = res.data.data
        })
    }
}

//计算属性，显示昵称还是用户名
const nameonick = computed(() => {
    return usersession.nickname == null ? usersession.username : usersession.nickname
})

// 导航铃铛的聚合未读数
const totalUnread = computed(() => {
    return (adoptnews.value || 0) + (donatenews.value || 0) + (messagenews.value || 0)
})

//#region 退出功能
const reload = inject('reload')
const router = useRouter()
function exit() {
    sessionStorage.clear()
    router.replace('/')
    reload()
}
//#endregion

</script>


<style scoped>
/* ===== 吸顶导航：毛玻璃 + 细分隔线，弱化"框"的存在感 ===== */
.navbar {
    position: sticky;
    top: 0;
    z-index: 100;
    background: rgba(255, 255, 255, .82);
    backdrop-filter: saturate(1.6) blur(12px);
    -webkit-backdrop-filter: saturate(1.6) blur(12px);
    border-bottom: 1px solid var(--line);
}

.nav-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
}

.logo img {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: block;
}

.brand {
    display: flex;
    align-items: center;
    gap: 10px;
    text-decoration: none;
}

.brand-name {
    font-size: 18px;
    font-weight: 700;
    color: var(--ink-1);
    letter-spacing: .5px;
    white-space: nowrap;
}

.nav-links {
    list-style: none;
    display: flex;
    align-items: center;
    gap: 4px;
    margin: 0;
    padding: 0;
}

.nav-links li {
    margin: 0;
}

.nav-links a {
    display: block;
    color: var(--ink-2);
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    padding: 8px 14px;
    border-radius: var(--radius-sm);
    transition: color .2s ease, background-color .2s ease;
    white-space: nowrap;
}

.nav-links a:hover {
    color: var(--brand);
    background: var(--brand-100);
}

/* 当前页高亮：Vue Router 原生提供的 exact-active 类，纯 CSS 实现 */
.nav-links a.router-link-exact-active {
    color: var(--brand);
    background: var(--brand-100);
}

/* 登录用户的小胶囊 */
.user-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    color: var(--ink-1);
    font-size: 15px;
    font-weight: 500;
    padding: 8px 14px;
    border-radius: var(--radius-sm);
    background: var(--brand-100);
    outline: none;
}

/* 消息铃铛 */
.bell-link {
    display: flex;
    align-items: center;
}

.bell-icon {
    font-size: 17px;
    line-height: 1;
}

.bell-badge :deep(.el-badge__content) {
    border: none;
}

/* ===== 页脚 ===== */
.footer {
    background: #ffffff;
    border-top: 1px solid var(--line);
    padding: 20px 0;
    text-align: center;
    color: var(--ink-3);
    font-size: 13px;
}

.footer-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.footer p {
    margin: 0;
}

/* ===== 移动端：品牌行独立、导航横向滑动，避免挤压换行 ===== */
@media (max-width: 768px) {
    .nav-inner {
        height: auto;
        flex-direction: column;
        align-items: stretch;
        padding: 10px 12px 0;
        gap: 0;
    }

    .brand-name {
        font-size: 16px;
    }

    .nav-links {
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
        scrollbar-width: none;
        margin-top: 6px;
        padding-bottom: 6px;
    }

    .nav-links::-webkit-scrollbar {
        display: none;
    }

    .nav-links a,
    .user-chip {
        padding: 6px 10px;
        font-size: 14px;
    }
}
</style>
