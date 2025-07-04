<template>
    <div class="section announcement-section">
        <h2>{{ classic == 0 ? '网站公告' : classic == 1 ? '领养救助指南' : '近期新闻' }}</h2>
        <ul class="content-list">
            <li v-for="web in webannounce" :key="web.id">
                <RouterLink :to="`/postdetail/${web.id}`">{{ web.title }}</RouterLink>
                <p class="timestamp">{{ web.createTime }}</p>
            </li>
        </ul>
    </div>
</template>

<script setup>
import axios from 'axios';
import { defineProps, onMounted, ref } from 'vue';

onMounted(() => {
    getclassicpost()
})

//#region 全部查了,不分页,要不就搞个无限滚动
const { classic } = defineProps(['classic'])
const webannounce = ref([])
function getclassicpost() {
    axios.get(`/post/alltypeone/${classic}`).then((res) => {
        console.log(res.data.data);
        webannounce.value = res.data.data
    }).catch((err) => {
        console.log(err);
    })
}
//#endregion
</script>

<style scoped>
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