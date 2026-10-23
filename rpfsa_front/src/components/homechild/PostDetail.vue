<template>
    <div class="page-container fade-up">
        <!-- 文章卡 -->
        <article class="post-card soft-card">
            <header class="post-header">
                <h1 class="post-title">{{ currentpost.title }}</h1>
                <div class="post-meta">
                    <span class="meta-item">🕐 {{ currentpost.createTime }}</span>
                    <span class="meta-item">✍️ {{ currentpost.user == null ? '管理员' : currentpost.user.nickname
                        }}</span>
                </div>
            </header>
            <div class="post-content">
                <p class="ql-editor" v-html="currentpost.content"></p>
            </div>
            <!-- 点赞 -->
            <footer class="post-actions" v-if="currentpost.isPinned == 0">
                <button class="like-btn" :class="{ liked: currentpost.liked }" :disabled="likeLoading" @click="toggleLike">
                    <span class="like-icon">{{ currentpost.liked ? '❤️' : '🤍' }}</span>
                    <span>{{ currentpost.likeCount || 0 }}</span>
                </button>
                <span class="like-tip" v-if="!currentpost.liked">点赞可为作者 +2 宠宠币</span>
            </footer>
        </article>
        <!-- 评论区 -->
        <div class="comment-section soft-card" v-if="currentpost.isPinned == 0">
            <div class="section-head">
                <h3 class="section-title">评论区</h3>
            </div>
            <div class="comment-list">
                <div v-for="comment in currentpost.comments" :key="comment.id" class="comment">
                    <div class="comment-header">
                        <!-- 点头像 -->
                        <el-popover placement="bottom" :title="`昵称:${comment.user.nickname}`" :width="220"
                            trigger="click">
                            电话：{{ comment.user.phone }} <br>
                            邮箱：{{ comment.user.email }} <br>
                            领养个数：{{ comment.user.howAdopt }} <br>
                            宠宠币：{{ comment.user.petCoin }}
                            <template #reference>
                                <img class="avatar" :src="resolveImg(comment.user.photo)" alt="用户头像">
                            </template>
                        </el-popover>
                        <div class="comment-meta">
                            <span class="comment-author">{{ comment.user.nickname == null ? comment.user.username
                                : comment.user.nickname }}
                            </span>
                            <span class="comment-time">{{ comment.createTime }}</span>
                            <el-link type="primary" class="reply-link" @click="addlevelTwo(comment.id)">回复</el-link>
                        </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}
                        <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                            @confirm="commentdelete(comment.id, comment.level)">
                            <template #reference>
                                <span v-if="currentUser != null && comment.uid == currentUser.id"
                                    class="delete-link"><el-link type="danger">删除</el-link></span>
                            </template>
                        </el-popconfirm>
                    </div>
                    <!-- 子评论 -->
                    <div class="sub-comment" v-for="leveltwocon in comment.commentList" :key="leveltwocon.id">
                        <div class="sub-comment-head">
                            <el-popover placement="bottom" :title="`昵称:${leveltwocon.user.nickname}`" :width="220"
                                trigger="click">
                                电话：{{ leveltwocon.user.phone }} <br>
                                邮箱：{{ leveltwocon.user.email }} <br>
                                领养个数：{{ leveltwocon.user.howAdopt }} <br>
                                宠宠币：{{ leveltwocon.user.petCoin }}
                                <template #reference>
                                    <img class="avatarleveltwo" :src="resolveImg(leveltwocon.user.photo)" alt="用户头像">
                                </template>
                            </el-popover>
                            <span class="sub-author">{{ leveltwocon.user.nickname == null ?
                                leveltwocon.user.username
                                : leveltwocon.user.nickname }}
                            </span>
                            <span class="sub-time">{{ leveltwocon.createTime }}</span>
                        </div>
                        <div class="sub-content">{{ leveltwocon.content }}
                            <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认"
                                cancelButtonText="取消" @confirm="commentdelete(leveltwocon.id, leveltwocon.level)">
                                <template #reference>
                                    <span v-if="currentUser != null && leveltwocon.uid == currentUser.id"
                                        class="delete-link"><el-link type="danger">删除</el-link></span>
                                </template>
                            </el-popconfirm>
                        </div>
                    </div>
                </div>
                <div v-if="!currentpost.comments || currentpost.comments.length === 0" class="empty-state">
                    还没有评论，来抢沙发～
                </div>
            </div>
            <div class="comment-input">
                <input v-model="currcomment" type="text" placeholder="说点什么...." @keyup.enter="confirmcomment">
                <button @click="confirmcomment">发表</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus';
import 'quill/dist/quill.core.css'
import { defineProps, ref, reactive, onMounted } from 'vue'
import axios from 'axios';
import { resolveImg } from '@/utils/url';

onMounted(() => {
    getcurpost()
})

//#region 评论功能
const currcomment = ref('')
/**
 * 删除评论。评论数由后端在同一事务内维护，前端不再回写 commentCount
 * （原写法是"读出当前值再减 N"，并发或重试都会导致计数漂移）。
 */
function commentdelete(commid, commlevel) {
    const url = commlevel ? `/comment/${commid}` : `/comment/t1/${commid}`
    axios.delete(url).then(res => {
        if (res.data.code == 200) {
            ElMessage.success(res.data.msg)
            getcurpost()
        } else {
            ElMessage.error(res.data.msg)
        }
    }).catch(() => { /* 网络异常已由全局拦截器统一提示 */ })
}
function addlevelTwo(cid) {
    ElMessageBox.prompt('提示', {
        confirmButtonText: '评论',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '在此输入您的评论',
        type: 'info',
    }).then(({ value }) => {
        addcomment(value, cid)
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
function confirmcomment() {
    addcomment(currcomment.value, 0)
}
function addcomment(commenttext, cid) {
    if (currentUser != null) {
        if (commenttext != '' && commenttext != null) {
            if (currentUser.comPermiss) {
                // 发表评论。poid 与 pid 二者必有一个为 0，表示评论挂在帖子还是宠物上；
                // uid 与评论数均由服务端处理
                axios.post('/comment', {
                    poid: Number(poid),
                    pid: 0,
                    content: commenttext,
                    level: cid
                }).then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success('评论成功😊')
                    } else {
                        ElMessage.error(res.data.msg)
                    }
                })
                    .catch(() => {
                        ElMessage.error('评论失败,服务器故障🤨')
                    }).finally(() => {
                        getcurpost()
                        currcomment.value = ''
                    })
            } else {
                ElMessage.warning('您的评论权限已被限制！')
            }
        } else ElMessage.warning('评论不能为空！')

    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}
//#endregion

//#region 拿到当前post
const { poid } = defineProps(['poid'])
const currentpost = ref({})
function getcurpost() {
    axios.get(`/post/${poid}`)
        .then(res => {
            currentpost.value = res.data.data
        })
        .catch(err => {
        })
}
//#endregion


//拿到usersession
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))

//#region 点赞
const likeLoading = ref(false)
function toggleLike() {
    if (currentUser == null) {
        ElMessage.warning('对不起你还未登录....请登录账户')
        return
    }
    if (likeLoading.value) return
    likeLoading.value = true
    axios.post(`/post/${poid}/like`).then(res => {
        if (res.data.code === 200) {
            currentpost.value.liked = res.data.data
            currentpost.value.likeCount = (currentpost.value.likeCount || 0) + (res.data.data ? 1 : -1)
            ElMessage.success(res.data.msg)
        } else {
            ElMessage.warning(res.data.msg)
        }
    }).catch(() => {}).finally(() => likeLoading.value = false)
}
//#endregion
</script>

<style scoped>
/* ===== 文章卡：窄栏居中，阅读友好 ===== */
.post-card {
    max-width: 860px;
    margin: 0 auto 24px;
    padding: 36px 44px;
}

.post-header {
    text-align: center;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--line);
    margin-bottom: 24px;
}

.post-title {
    font-size: 28px;
    margin: 0 0 14px;
    color: var(--ink-1);
    line-height: 1.4;
}

.post-meta {
    color: var(--ink-3);
    font-size: 13px;
    display: flex;
    justify-content: center;
    gap: 20px;
    flex-wrap: wrap;
}

.post-content {
    font-size: 16px;
    line-height: 1.9;
    color: var(--ink-2);
}

.post-content .ql-editor {
    padding: 0;
}

/* ===== 点赞 ===== */
.post-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 28px;
    padding-top: 20px;
    border-top: 1px dashed var(--line);
    justify-content: center;
}

.like-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 26px;
    border-radius: 999px;
    border: 1px solid var(--brand-200);
    background: #fff;
    color: var(--ink-2);
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all .22s ease;
    font-family: var(--font);
}

.like-btn:hover {
    border-color: var(--brand);
    transform: translateY(-2px);
}

.like-btn.liked {
    background: var(--brand-gradient);
    border-color: transparent;
    color: #fff;
    box-shadow: var(--shadow-brand);
}

.like-btn:disabled {
    opacity: .6;
    cursor: not-allowed;
    transform: none;
}

.like-icon {
    font-size: 17px;
    line-height: 1;
}

.like-tip {
    font-size: 12px;
    color: var(--ink-3);
}

/* ===== 评论区 ===== */
.comment-section {
    max-width: 860px;
    margin: 0 auto;
    padding: 24px 28px;
}

.comment-list {
    margin-bottom: 20px;
}

.comment {
    border: 1px solid var(--line);
    border-radius: var(--radius);
    background-color: #fff;
    padding: 16px 18px;
    margin-bottom: 14px;
    transition: box-shadow .25s ease;
}

.comment:hover {
    box-shadow: var(--shadow-sm);
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.comment-meta {
    display: flex;
    align-items: baseline;
    gap: 12px;
    flex-wrap: wrap;
}

.avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    margin-right: 12px;
    cursor: pointer;
    border: 2px solid var(--brand-100);
    object-fit: cover;
}

.avatarleveltwo {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    margin-right: 8px;
    cursor: pointer;
    object-fit: cover;
}

.comment-author {
    font-weight: 600;
    color: var(--ink-1);
}

.comment-time {
    font-size: 12px;
    color: var(--ink-3);
}

.reply-link {
    font-size: 13px;
}

.comment-content {
    margin: 4px 0 4px 56px;
    color: var(--ink-2);
    line-height: 1.7;
    font-size: 14px;
}

.delete-link {
    margin-left: 15px;
}

/* 子评论缩进块 */
.sub-comment {
    margin: 10px 0 0 56px;
    padding: 10px 14px;
    background: #fafbfe;
    border-left: 3px solid var(--brand-200);
    border-radius: var(--radius-sm);
}

.sub-comment-head {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
}

.sub-author {
    font-size: 13px;
    font-weight: 600;
    color: var(--ink-1);
}

.sub-time {
    font-size: 12px;
    color: var(--ink-3);
    margin-left: 10px;
}

.sub-content {
    font-size: 13px;
    color: var(--ink-2);
    line-height: 1.6;
}

/* ===== 评论输入条 ===== */
.comment-input {
    display: flex;
    gap: 12px;
}

.comment-input input[type="text"] {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid var(--line);
    border-radius: 999px;
    font-size: 14px;
    background: #fafbfe;
    color: var(--ink-1);
    outline: none;
    transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease;
}

.comment-input input[type="text"]:focus {
    background: #fff;
    border-color: var(--brand);
    box-shadow: 0 0 0 3px var(--brand-100);
}

.comment-input button {
    padding: 0 28px;
    background: var(--brand-gradient);
    color: #fff;
    border: none;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    box-shadow: var(--shadow-brand);
    transition: transform .2s ease, box-shadow .2s ease;
}

.comment-input button:hover {
    transform: translateY(-1px);
    box-shadow: 0 10px 24px rgba(255, 107, 53, .4);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
    .post-card {
        padding: 24px 18px;
    }

    .post-title {
        font-size: 22px;
    }

    .comment-content,
    .sub-comment {
        margin-left: 0;
    }
}
</style>
