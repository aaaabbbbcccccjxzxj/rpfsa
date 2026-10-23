<template>
    <div class="page-container fade-up">
        <!-- 页头 -->
        <div class="thank-hero soft-card">
            <h1 class="thank-title">❤️ 贡献与鸣谢</h1>
            <p class="thank-sub">感谢每一位伸出援手的朋友，是你们让流浪的小生命重新有了家</p>
        </div>
        <!-- 榜单四列 -->
        <div class="rank-grid">
            <!-- 领养榜 -->
            <div class="rank-card soft-card">
                <div class="rank-head">
                    <span class="rank-icon">🏠</span>
                    <h3 class="rank-title">领养榜</h3>
                </div>
                <ul class="rank-list">
                    <li v-for="user, index in userlist" :key="user.id" class="rank-item">
                        <span class="rank-no" :class="rankClass(index)">{{ index + 1 }}</span>
                        <div class="rank-body">
                            <div class="rank-name">{{ user.nickname || user.username }}</div>
                            <div class="rank-meta">领养 {{ user.howAdopt }} 只</div>
                        </div>
                    </li>
                    <li v-if="userlist.length === 0" class="empty-state">虚位以待</li>
                </ul>
            </div>
            <!-- 救助榜 -->
            <div class="rank-card soft-card">
                <div class="rank-head">
                    <span class="rank-icon">💝</span>
                    <h3 class="rank-title">救助榜</h3>
                </div>
                <ul class="rank-list">
                    <li v-for="donate, index in donatelist" :key="donate.uid" class="rank-item">
                        <span class="rank-no" :class="rankClass(index)">{{ index + 1 }}</span>
                        <div class="rank-body">
                            <div class="rank-name">{{ donate.user.nickname || donate.user.username }}</div>
                            <div class="rank-meta">捐助 {{ donate.count }} 次 · 共 {{ donate.totalPrice }} 币</div>
                        </div>
                    </li>
                    <li v-if="donatelist.length === 0" class="empty-state">虚位以待</li>
                </ul>
            </div>
            <!-- 热门帖子榜 -->
            <div class="rank-card soft-card">
                <div class="rank-head">
                    <span class="rank-icon">🔥</span>
                    <h3 class="rank-title">热门帖子榜</h3>
                </div>
                <ul class="rank-list">
                    <li v-for="post, index in postlist" :key="post.id" class="rank-item">
                        <span class="rank-no" :class="rankClass(index)">{{ index + 1 }}</span>
                        <div class="rank-body">
                            <RouterLink :to="`/postdetail/${post.id}`" class="rank-link">{{ post.title }}</RouterLink>
                            <div class="rank-meta">评论 {{ post.commentCount }} · {{ post.user.username }}</div>
                        </div>
                    </li>
                    <li v-if="postlist.length === 0" class="empty-state">虚位以待</li>
                </ul>
            </div>
            <!-- 流浪猫狗榜 -->
            <div class="rank-card soft-card">
                <div class="rank-head">
                    <span class="rank-icon">🐾</span>
                    <h3 class="rank-title">流浪猫狗榜</h3>
                </div>
                <ul class="rank-list">
                    <li v-for="pet, index in petlist" :key="pet.id" class="rank-item">
                        <span class="rank-no" :class="rankClass(index)">{{ index + 1 }}</span>
                        <router-link :to="`/petdetail/${pet.id}`" class="rank-pet">
                            <img :src="resolveImg(pet.photo)" alt="Item Image" class="rank-pet-img" />
                        </router-link>
                        <div class="rank-body">
                            <RouterLink :to="`/petdetail/${pet.id}`" class="rank-link">{{ pet.petname }}</RouterLink>
                            <div class="rank-meta">宠宠币 {{ pet.petCoin }}</div>
                        </div>
                    </li>
                    <li v-if="petlist.length === 0" class="empty-state">虚位以待</li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus'
import { resolveImg } from '@/utils/url';

onMounted(() => {
    postget()
    petget()
    userget()
    donateget()
})

// 前三名奖牌色
function rankClass(index) {
    if (index === 0) return 'no-gold'
    if (index === 1) return 'no-silver'
    if (index === 2) return 'no-bronze'
    return ''
}

//#region 帖子榜
const postlist = ref([])
function postget() {
    axios.get('/post').then((res) => {
        postlist.value = res.data.data
    })
}
//#endregion

//#region 宠物榜
const petlist = ref([])
function petget() {
    axios.get('/pet').then((res) => {
        petlist.value = res.data.data
    })
}
//#endregion

//#region 用户领养榜
const userlist = ref([])
function userget() {
    axios.get('/user').then((res) => {
        if (res.data.code == 200) {
            userlist.value = res.data.data
        } else ElMessage.warning(res.data.msg)
    })
}
//#endregion

//#region 用户捐献榜
const donatelist = ref([])
function donateget() {
    axios.get('/donate').then((res) => {
        donatelist.value = res.data.data
    })
}
//#endregion

</script>
<style scoped>
/* ===== 页头横幅 ===== */
.thank-hero {
    text-align: center;
    padding: 36px 24px;
    margin-bottom: 24px;
    background:
        radial-gradient(420px 200px at 50% 0%, rgba(255, 138, 80, .14) 0%, transparent 65%),
        #fff;
}

.thank-title {
    margin: 0 0 10px;
    font-size: 28px;
    color: var(--ink-1);
    letter-spacing: 1px;
}

.thank-sub {
    margin: 0;
    color: var(--ink-2);
    font-size: 14px;
}

/* ===== 榜单栅格：四列均分（minmax(0,1fr) 防止长标题 nowrap 撑爆列宽） ===== */
.rank-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.rank-card {
    padding: 20px 18px;
    transition: box-shadow .25s ease, transform .25s ease;
}

.rank-card:hover {
    box-shadow: var(--shadow);
    transform: translateY(-3px);
}

.rank-head {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 12px;
    margin-bottom: 12px;
    border-bottom: 1px solid var(--line);
}

.rank-icon {
    font-size: 20px;
}

.rank-title {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: var(--ink-1);
}

.rank-list {
    list-style: none;
    margin: 0;
    padding: 0;
}

.rank-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 9px 6px;
    border-radius: var(--radius-sm);
    transition: background-color .2s ease;
}

.rank-item:hover {
    background: var(--brand-100);
}

/* 序号圆牌 */
.rank-no {
    flex-shrink: 0;
    width: 26px;
    height: 26px;
    line-height: 26px;
    text-align: center;
    font-size: 13px;
    font-weight: 700;
    border-radius: 50%;
    background: #eef0f6;
    color: var(--ink-2);
}

.rank-no.no-gold {
    background: linear-gradient(135deg, #ffd666, var(--gold));
    color: #7a5200;
    box-shadow: 0 3px 8px rgba(247, 181, 0, .4);
}

.rank-no.no-silver {
    background: linear-gradient(135deg, #d7dde6, var(--silver));
    color: #fff;
    box-shadow: 0 3px 8px rgba(154, 165, 177, .4);
}

.rank-no.no-bronze {
    background: linear-gradient(135deg, #e8b18a, var(--bronze));
    color: #fff;
    box-shadow: 0 3px 8px rgba(208, 138, 78, .4);
}

.rank-pet {
    flex-shrink: 0;
    display: block;
}

.rank-pet-img {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid var(--brand-100);
    display: block;
}

.rank-body {
    flex: 1;
    min-width: 0;
}

.rank-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--ink-1);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.rank-link {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: var(--ink-1);
    text-decoration: none;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.rank-link:hover {
    color: var(--brand);
}

.rank-meta {
    font-size: 12px;
    color: var(--ink-3);
    margin-top: 2px;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
    .rank-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 576px) {
    .rank-grid {
        grid-template-columns: minmax(0, 1fr);
    }
}
</style>
