<template>
    <div class="page-container home fade-up">
        <!-- ===== Hero：左侧标语 + 右侧轮播大卡 ===== -->
        <section class="hero soft-card">
            <div class="hero-copy">
                <div class="hero-badge">
                    <span class="paw">🐾</span> 流浪猫狗之家 · 公益救助平台
                </div>
                <h1 class="hero-title">
                    给每一只流浪的小生命<br />
                    <span class="accent">一个温暖的家</span>
                </h1>
                <p class="hero-sub">
                    在这里，你可以领养待回家的猫狗、捐助救助物资、分享救助故事。
                    每一次点击，都是它们回家的希望。
                </p>
                <div class="hero-actions">
                    <RouterLink to="/dogandcat" class="btn-primary">去领养 / 救助</RouterLink>
                    <RouterLink to="/community" class="btn-ghost">逛逛社区</RouterLink>
                </div>
            </div>
            <div class="hero-carousel">
                <el-carousel indicator-position="none" height="360px" class="hero-el">
                    <el-carousel-item v-for="item in lbt" :key="item">
                        <img :src="item.url" class="hero-img" @click="godc(item.id)" />
                    </el-carousel-item>
                </el-carousel>
            </div>
        </section>

        <!-- ===== 三栏信息流：公告 / 领养救助 / 新闻 ===== -->
        <section class="feed-grid">
            <div class="feed-card soft-card">
                <div class="section-head">
                    <h3 class="section-title">网站公告</h3>
                    <RouterLink to="/noticelist/0" class="section-more">更多 →</RouterLink>
                </div>
                <ul class="feed-list">
                    <li v-for="web in webannounce.slice(0, 6)" :key="web.id">
                        <RouterLink :to="`/postdetail/${web.id}`" class="feed-link">{{ web.title }}</RouterLink>
                        <span class="feed-time">{{ web.createTime }}</span>
                    </li>
                    <li v-if="webannounce.length === 0" class="empty-state">暂无公告</li>
                </ul>
            </div>
            <div class="feed-card soft-card">
                <div class="section-head">
                    <h3 class="section-title">领养救助</h3>
                    <RouterLink to="/noticelist/1" class="section-more">更多 →</RouterLink>
                </div>
                <ul class="feed-list">
                    <li v-for="adoptr in adoptrescue.slice(0, 6)" :key="adoptr.id">
                        <RouterLink :to="`/postdetail/${adoptr.id}`" class="feed-link">{{ adoptr.title }}</RouterLink>
                        <span class="feed-time">{{ adoptr.createTime }}</span>
                    </li>
                    <li v-if="adoptrescue.length === 0" class="empty-state">暂无内容</li>
                </ul>
            </div>
            <div class="feed-card soft-card">
                <div class="section-head">
                    <h3 class="section-title">近期新闻</h3>
                    <RouterLink to="/noticelist/2" class="section-more">更多 →</RouterLink>
                </div>
                <ul class="feed-list">
                    <li v-for="recent in recentnew.slice(0, 6)" :key="recent.id">
                        <RouterLink :to="`/postdetail/${recent.id}`" class="feed-link">{{ recent.title }}</RouterLink>
                        <span class="feed-time">{{ recent.createTime }}</span>
                    </li>
                    <li v-if="recentnew.length === 0" class="empty-state">暂无新闻</li>
                </ul>
            </div>
        </section>
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
/* ===== Hero：奶油渐变底 + 左文右图 ===== */
.hero {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1.05fr);
    gap: 32px;
    align-items: center;
    padding: 40px 44px;
    margin-bottom: 28px;
    background:
        radial-gradient(420px 240px at 8% 0%, rgba(255, 138, 80, .12) 0%, transparent 60%),
        linear-gradient(120deg, #fffaf5 0%, #ffffff 55%);
    overflow: hidden;
}

.hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: var(--brand-600);
    background: var(--brand-100);
    border: 1px solid var(--brand-200);
    padding: 6px 14px;
    border-radius: 999px;
    margin-bottom: 20px;
}

.hero-title {
    font-size: 38px;
    line-height: 1.3;
    margin: 0 0 16px;
    color: var(--ink-1);
    letter-spacing: 1px;
}

.hero-title .accent {
    background: var(--brand-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
}

.hero-sub {
    font-size: 15px;
    line-height: 1.8;
    color: var(--ink-2);
    margin: 0 0 28px;
    max-width: 460px;
}

.hero-actions {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.btn-primary {
    display: inline-block;
    padding: 12px 30px;
    border-radius: 999px;
    background: var(--brand-gradient);
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    text-decoration: none;
    box-shadow: var(--shadow-brand);
    transition: transform .2s ease, box-shadow .2s ease;
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(255, 107, 53, .4);
}

.btn-ghost {
    display: inline-block;
    padding: 12px 30px;
    border-radius: 999px;
    background: #fff;
    color: var(--brand-600);
    font-size: 15px;
    font-weight: 600;
    text-decoration: none;
    border: 1px solid var(--brand-200);
    transition: background-color .2s ease, transform .2s ease;
}

.btn-ghost:hover {
    background: var(--brand-100);
    transform: translateY(-2px);
}

.hero-carousel {
    border-radius: var(--radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow);
}

.hero-el {
    border-radius: var(--radius-lg);
}

.hero-img {
    width: 100%;
    height: 360px;
    object-fit: cover;
    display: block;
    cursor: pointer;
}

/* ===== 三栏信息流卡片 =====
 * 注意必须 minmax(0, 1fr)：grid 的 1fr 实为 minmax(auto, 1fr)，
 * 长标题 nowrap 会把列撑爆导致整行溢出容器（页面看起来"整体偏右"） */
.feed-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 20px;
}

.feed-card {
    padding: 22px 22px 14px;
}

.feed-card:hover {
    box-shadow: var(--shadow);
    transform: translateY(-3px);
}

.feed-list {
    list-style: none;
    margin: 0;
    padding: 0;
}

.feed-list li {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 8px;
    border-radius: var(--radius-sm);
    transition: background-color .2s ease;
}

.feed-list li:hover {
    background: var(--brand-100);
}

.feed-link {
    flex: 1;
    min-width: 0;
    text-decoration: none;
    color: var(--ink-1);
    font-size: 14px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.feed-link:hover {
    color: var(--brand);
}

.feed-time {
    flex-shrink: 0;
    font-size: 12px;
    color: var(--ink-3);
}

/* ===== 响应式 ===== */
@media (max-width: 960px) {
    .hero {
        grid-template-columns: 1fr;
        padding: 30px 24px;
    }

    .hero-title {
        font-size: 28px;
    }

    .feed-grid {
        grid-template-columns: 1fr;
    }
}
</style>
