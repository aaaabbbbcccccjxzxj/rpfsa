<template>
    <div class="parent">
        <!-- 领养榜 -->
        <div class="div1">
            <div class="ranking">
                <h3 class="ranking-title">领养榜</h3>
                <ul class="ranking-list">
                    <li v-for="user, index in userlist" :key="user.id" style="height: 80px;">
                        <div class="ranking-info">
                            <span class="ranking-number">{{ index + 1 }}</span>
                            <div class="ranking-details">
                                <h4 class="ranking-name">{{ user.nickname }}</h4>
                                <p class="ranking-score">领养个数: {{ user.howAdopt }}只</p>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <!-- 救助榜 -->
        <div class="div2">
            <div class="ranking">
                <h3 class="ranking-title">救助榜</h3>
                <ul class="ranking-list">
                    <li v-for="donate, index in donatelist" :key="donate.uid" style="height: 80px;">
                        <div class="ranking-info">
                            <span class="ranking-number">{{ index + 1 }}</span>
                            <div class="ranking-details">
                                <h4 class="ranking-name">{{ donate.user.nickname }}</h4>
                                <p class="ranking-score">捐助次数: {{ donate.count }} <br>
                                    总贡献:{{ donate.totalPrice }}宠宠币
                                </p>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <!-- 热门帖子榜 -->
        <div class="div3">
            <div class="ranking">
                <h3 class="ranking-title">热门帖子榜</h3>
                <ul class="ranking-list">
                    <li v-for="post, index in postlist" :key="post.id" style="height: 80px;">
                        <div class="ranking-info">
                            <span class="ranking-number">{{ index + 1 }}</span>
                            <div class="ranking-details">
                                <h4 class="ranking-name">
                                    <RouterLink :to="`/postdetail/${post.id}`">
                                        <el-text class="mx-1" truncated style="font-size:16px;color: black;width: 180px;">
                                            {{ post.title }}
                                        </el-text>
                                    </RouterLink>
                                </h4>
                                <p class="ranking-score">评论数: {{ post.commentCount }} <br>
                                    作者: {{ post.user.username }}
                                </p>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <!-- 流浪猫狗榜 -->
        <div class="div4">
            <div class="ranking">
                <h3 class="ranking-title">流浪猫狗榜</h3>
                <ul class="ranking-list">
                    <li v-for="pet, index in petlist" :key="pet.id" style="height: 80px;">
                        <span class="ranking-number">{{ index + 1 }}</span>
                        <div class="ranking-info">
                            <router-link :to="`/petdetail/${pet.id}`">
                                <img :src="getImageUrl(pet.photo)" alt="Item Image" class="ranking-image" />
                            </router-link>
                            <div class="ranking-details">
                                <h4 class="ranking-name">{{ pet.petname }}</h4>
                                <p class="ranking-score">宠宠币: {{ pet.petCoin }}</p>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus'

onMounted(() => {
    postget()
    petget()
    userget()
    donateget()
})

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
        console.log(res.data);
        donatelist.value = res.data.data
    })
}
//#endregion

function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }

</script>
<style scoped>
.ranking {
    background-color: #f2f2f2;
    border-radius: 4px;
    padding: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.ranking-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
}

.ranking-list {
    list-style: none;
    padding: 0;
}

.ranking-list li {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.ranking-number {
    font-size: 16px;
    font-weight: bold;
    color: #ff5e3a;
    margin-right: 10px;
}

.ranking-info {
    display: flex;
    align-items: center;
}

.ranking-image {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    margin-right: 10px;
}

.ranking-details {
    flex: 1;
}

.ranking-name {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 5px;
}

.ranking-score {
    font-size: 14px;
    color: #888;
}

.parent {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    grid-template-rows: repeat(5, 1fr);
    grid-column-gap: 10px;
    grid-row-gap: 0px;
    margin-top: 20px;
}

.div1 {
    grid-area: 1 / 2 / 6 / 3;
}

.div2 {
    grid-area: 1 / 3 / 6 / 4;
}

.div3 {
    grid-area: 1 / 4 / 6 / 5;

}

.div4 {
    grid-area: 1 / 5 / 6 / 6;
}
</style>