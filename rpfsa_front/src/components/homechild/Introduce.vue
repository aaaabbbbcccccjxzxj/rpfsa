<template>
    <div class="home-page">
        <!-- 轮播图 -->
        <div class="section carousel-section">
            <div class="carousel">
                <el-carousel indicator-position="none" height="375px" style="width: 650px;">
                    <el-carousel-item v-for=" item in lbt" :key="item">
                        <img :src="item.url" style="width: 650px; height: auto; cursor: pointer;" @click="godc(item.id)" />
                    </el-carousel-item>
                </el-carousel>
            </div>
        </div>
        <!-- 网站公告 -->
        <div class="section announcement-section">
            <h2>网站公告</h2>
            <ul class="content-list">
                <li v-for="web in webannounce.slice(0, 6)" :key="web.id">
                    <RouterLink :to="`/postdetail/${web.id}`">{{ web.title }}</RouterLink>
                    <p class="timestamp">{{ web.createTime }}</p>
                </li>
            </ul>
            <RouterLink to="/noticelist/0" class="more-link">更多</RouterLink>
        </div>
        <!-- 领养救助 -->
        <div class="section adoption-rescue-section">
            <h2>领养救助</h2>
            <ul class="content-list">
                <li v-for="adoptr in adoptrescue.slice(0, 6)" :key="adoptr.id">
                    <RouterLink :to="`/postdetail/${adoptr.id}`">{{ adoptr.title }}</RouterLink>
                    <p class="timestamp">{{ adoptr.createTime }}</p>
                </li>
            </ul>
            <RouterLink to="/noticelist/1" class="more-link">更多</RouterLink>
        </div>
        <!-- 近期新闻 -->
        <div class="section news-section">
            <h2>近期新闻</h2>
            <ul class="content-list">
                <li v-for="recent in recentnew.slice(0, 6)" :key="recent.id">
                    <RouterLink :to="`/postdetail/${recent.id}`">{{ recent.title }}</RouterLink>
                    <p class="timestamp">{{ recent.createTime }}</p>
                </li>
            </ul>
            <RouterLink to="/noticelist/2" class="more-link">更多</RouterLink>
        </div>
    </div>
</template>
<script setup>
import axios from 'axios';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router'

onMounted(() => {
    getallpost()
})

//#region 轮播图写死
const router = useRouter()
const lbt = ref([
    { id: 1, url: new URL('@/assets/lbt/cat.png', import.meta.url).href },
    { id: 2, url: new URL('@/assets/lbt/dog1.png', import.meta.url).href },
    { id: 3, url: new URL('@/assets/lbt/dog2.png', import.meta.url).href },
])
function godc(id) {
    if (id <= 3) {
        router.push('/dogandcat')
    }
}
//#endregion

//#region 分类
const postsall = ref([])
const webannounce = computed(() => {
    return postsall.value.filter((post) => post.classic == 0)
})
const adoptrescue = computed(() => {
    return postsall.value.filter((post) => post.classic == 1)
})
const recentnew = computed(() => {
    return postsall.value.filter((post) => post.classic == 2)
})
function getallpost() {
    axios.get('/post/alltypeone/-1').then((res) => {
        postsall.value = res.data.data
    })
}
//#endregion

</script>
<style scoped>
.home-page {
    margin-top: 20px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    gap: 20px;
}

.section {
    background-color: #f5f5f5;
    padding: 20px;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.section h2 {
    font-size: 20px;
    margin-bottom: 10px;
}

.carousel-section {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
}

.announcement-section {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
}

.adoption-rescue-section {
    grid-column: 1 / 2;
    grid-row: 2 / 3;
}

.news-section {
    grid-column: 2 / 3;
    grid-row: 2 / 3;
}

.more-link {
    color: #007bff;
    text-decoration: none;
    transition: color 0.3s ease;
}

.more-link:hover {
    color: #0056b3;
}

/* 过渡动画 */
.section {
    transition: box-shadow 0.3s ease;
}

.section:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.content-list {
    list-style-type: none;
    padding: 0;
}

.content-list li {
    background-color: #f5f5f5;
    padding: 10px;
    margin-bottom: 10px;
    border-radius: 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.3s ease;
}

.content-list li:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.content-list li a {
    text-decoration: none;
    color: #333;
}

.content-list li a:hover {
    color: #666;
}

.timestamp {
    float: right;
    color: #999;
    font-size: 12px;
}
</style>
