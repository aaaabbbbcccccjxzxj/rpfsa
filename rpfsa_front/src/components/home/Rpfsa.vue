<template>
    <!-- 导航栏 -->
    <div class="navbar">
        <div class="logo">
            <RouterLink to="/"><img style="width: 50px" src="logo.png" alt="Logo" /></RouterLink>
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
            <li v-else>
                <el-dropdown>
                    <span>{{ nameonick }}<el-icon><arrow-down /></el-icon>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <RouterLink style="text-decoration: none;" to="/usercenter">个人中心</RouterLink>
                            </el-dropdown-item>
                            <el-dropdown-item @click="exit">退出</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </li>
        </ul>
    </div>
    <!-- 视图内容 -->
    <RouterView></RouterView>
    <!-- 页脚 -->
    <footer class="footer">
        <div class="container">
            <p>© 2023 流浪猫狗之家系统平台. All rights reserved. | 备案号:XXXXXXXXX</p>
        </div>
    </footer>
</template>


<script setup>
import { computed, inject, ref, provide, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

onMounted(() => {
    getlatestmessage()
})

// 设置获取未读消息间隔时长(1小时)
setInterval(getlatestmessage, 1000 * 60 * 60)
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
.footer {
    background-color: #f2f2f2;
    padding: 10px 0;
    text-align: center;
    color: #999;
    font-size: 14px;
}

.container {
    max-width: 960px;
    margin: 0 auto;
}

.navbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo img {
    width: 40px;
    height: 40px;
}

.nav-links {
    list-style: none;
    display: flex;
}

.nav-links li {
    margin-right: 30px;
}

.nav-links a {
    color: #333;
    text-decoration: none;
    font-size: 16px;
    transition: color 0.3s ease;
}

.nav-links a:hover {
    color: #ff5e3a;
}
</style>
