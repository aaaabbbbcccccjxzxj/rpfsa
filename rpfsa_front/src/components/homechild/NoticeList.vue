<template>
    <div class="page-container fade-up">
        <div class="notice-card soft-card">
            <div class="section-head">
                <h3 class="section-title">{{ classic == 0 ? '网站公告' : classic == 1 ? '领养救助指南' : '近期新闻' }}</h3>
            </div>
            <ul class="notice-list">
                <li v-for="web in webannounce" :key="web.id" class="notice-item">
                    <RouterLink :to="`/postdetail/${web.id}`" class="notice-link">
                        <span class="notice-dot"></span>
                        <span class="notice-title">{{ web.title }}</span>
                        <span class="notice-time">{{ web.createTime }}</span>
                    </RouterLink>
                </li>
                <li v-if="webannounce.length === 0" class="empty-state">暂无内容</li>
            </ul>
        </div>
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
        webannounce.value = res.data.data
    }).catch((err) => {
    })
}
//#endregion
</script>

<style scoped>
.notice-card {
    max-width: 860px;
    margin: 0 auto;
    padding: 26px 30px;
}

.notice-list {
    list-style: none;
    margin: 0;
    padding: 0;
}

.notice-item {
    border-bottom: 1px dashed var(--line);
}

.notice-item:last-child {
    border-bottom: none;
}

.notice-link {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 8px;
    text-decoration: none;
    border-radius: var(--radius-sm);
    transition: background-color .2s ease;
}

.notice-link:hover {
    background: var(--brand-100);
}

.notice-dot {
    flex-shrink: 0;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--brand);
}

.notice-title {
    flex: 1;
    min-width: 0;
    color: var(--ink-1);
    font-size: 15px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.notice-link:hover .notice-title {
    color: var(--brand);
}

.notice-time {
    flex-shrink: 0;
    font-size: 12px;
    color: var(--ink-3);
}
</style>
