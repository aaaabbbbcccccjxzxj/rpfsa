<template>
    <!-- post头部 -->
    <div class="post-header">
        <h1 class="post-title">{{ currentpost.title }}</h1>
        <div class="post-meta">
            <span class="post-date">发表时间：{{ currentpost.createTime }}</span>
            <span class="post-author">作者：{{ currentpost.user == null ? '管理员' : currentpost.user.nickname }}</span>
        </div>
    </div>
    <!-- post内容 -->
    <div class="post-content">
        <p class="ql-editor" v-html="currentpost.content"></p>
    </div>
    <!-- 评论区 -->
    <div class="comment-section" v-if="currentpost.isPinned == 0">
        <h2>评论</h2>
        <div class="comment-list">
            <div v-for="comment in currentpost.comments" :key="comment.id" class="comment">
                <div class="comment-header">
                    <!-- 点头像 -->
                    <el-popover placement="bottom" :title="`昵称:${comment.user.nickname}`" :width="220" trigger="click">
                        电话：{{ comment.user.phone }} <br>
                        邮箱：{{ comment.user.email }} <br>
                        领养个数：{{ comment.user.howAdopt }} <br>
                        宠宠币：{{ comment.user.petCoin }}
                        <template #reference>
                            <img class="avatar" :src="`http://localhost/img/${comment.user.photo}`" alt="用户头像">
                        </template>
                    </el-popover>
                    <div>
                        <span class="comment-author">{{ comment.user.nickname == null ? comment.user.username
                            : comment.user.nickname }}
                        </span>
                        <span class="comment-time">{{ comment.createTime }}</span>
                        <span style="margin-left: 20px;"><el-link type="primary"
                                @click="addlevelTwo(comment.id)">回复</el-link></span>
                    </div>
                </div>
                <div class="comment-content">{{ comment.content }}
                    <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                        @confirm="commentdelete(comment.id, comment.level)">
                        <template #reference>
                            <span v-if="currentUser != null && comment.uid == currentUser.id" style="margin: 15px;"><el-link
                                    type="danger">删除</el-link></span>
                        </template>
                    </el-popconfirm>
                </div>
                <hr>
                <!-- 子评论 -->
                <div v-for="leveltwocon in comment.commentList" :key="leveltwocon.id">
                    <div class="comment-content">
                        <el-popover placement="bottom" :title="`昵称:${leveltwocon.user.nickname}`" :width="220"
                            trigger="click">
                            电话：{{ leveltwocon.user.phone }} <br>
                            邮箱：{{ leveltwocon.user.email }} <br>
                            领养个数：{{ leveltwocon.user.howAdopt }} <br>
                            宠宠币：{{ leveltwocon.user.petCoin }}
                            <template #reference>
                                <img class="avatarleveltwo" :src="`http://localhost/img/${leveltwocon.user.photo}`"
                                    alt="用户头像">
                            </template>
                        </el-popover>
                        <div>
                            <span>{{ leveltwocon.user.nickname == null ?
                                leveltwocon.user.username
                                : leveltwocon.user.nickname }}
                            </span>
                            <span>{{ leveltwocon.createTime }}</span>
                        </div>
                    </div>
                    <div class="comment-content">{{ leveltwocon.content }}
                        <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                            @confirm="commentdelete(leveltwocon.id, leveltwocon.level)">
                            <template #reference>
                                <span v-if="currentUser != null && leveltwocon.uid == currentUser.id"
                                    style="margin: 15px;"><el-link type="danger">删除</el-link></span>
                            </template>
                        </el-popconfirm>
                    </div>
                </div>
            </div>
        </div>
        <div class="comment-input">
            <input v-model="currcomment" type="text" placeholder="说点什么....">
            <button @click="confirmcomment">发表</button>
        </div>
    </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus';
import 'quill/dist/quill.core.css'
import { defineProps, ref, reactive, onMounted } from 'vue'
import axios from 'axios';

onMounted(() => {
    getcurpost()
})

//#region 评论功能
const currcomment = ref('')
function commentdelete(commid, commlevel) {
    if (commlevel) {
        //二级评论,直接删
        axios.delete(`/comment/${commid}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    axios.patch('/post', {
                        id: poid,
                        commentCount: currentpost.value.commentCount - 1
                    })
                    getcurpost()
                    //删除post的commentCount
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
                console.error(err);
            })
    } else {
        //一级评论,条件删除
        axios.delete(`/comment/t1/${commid}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    axios.patch('/post', {
                        id: poid,
                        commentCount: currentpost.value.commentCount - res.data.data
                    })
                    getcurpost()
                    //删除post的commentCount
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
                console.error(err);
            })
    }
}
function addlevelTwo(cid) {
    ElMessageBox.prompt('提示', {
        confirmButtonText: '评论',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '在此输入您的评论',
        type: 'info',
    }).then(({ value }) => {
        console.log(value, cid);
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
                //发表评论
                axios.post('/comment', {
                    uid: currentUser.id,
                    poid: currentpost.value.id, //帖子有值
                    pid: 0, //poid有值，则这个为0 ,要不就是这个有值，那个为0
                    content: commenttext,
                    level: cid
                }).then(() => {
                    //该条post的评论数加1
                    axios.patch('/post', {
                        id: poid,
                        commentCount: currentpost.value.commentCount + 1
                    })
                    ElMessage.success('评论成功😊')
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
            console.error(err);
        })
}
//#endregion


//拿到usersession
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
</script>

<style scoped>
.post-header {
    background-color: #f2f2f2;
    padding: 5px;
    border-radius: 5px;
    /* margin-bottom: 10px; */
    text-align: center;
}

.post-title {
    font-size: 24px;
    margin-bottom: 10px;
}

.post-meta {
    color: #999;
    font-size: 14px;
}

.post-meta span {
    margin-right: 10px;
}

.post-content {
    font-size: 16px;
    line-height: 1.5;
    width: 1200px;
    margin: 0px auto 50px;
    background-color: #f2f2f2;
}

.comment-section {
    margin-top: 20px;
    font-family: Arial, sans-serif;
}

.comment-list {
    margin-bottom: 20px;
}

.comment {
    border-radius: 4px;
    background-color: #f0f0f0;
    padding: 10px;
    margin-bottom: 10px;
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 5px;
}

.avatar {
    width: 45px;
    /* 调整头像的大小 */
    height: 45px;
    border-radius: 50%;
    /* 使头像显示为圆形 */
    margin-right: 10px;
    cursor: pointer;
}

.avatarleveltwo {
    width: 25px;
    /* 调整头像的大小 */
    height: 25px;
    border-radius: 50%;
    /* 使头像显示为圆形 */
    margin-right: 10px;
    cursor: pointer;
}

.comment-author {
    font-weight: bold;
}

.comment-time {
    font-size: 12px;
    color: #777;
    margin-left: 20px;
}

.comment-content {
    margin-bottom: 5px;
}

.comment-input input[type="text"] {
    width: 50%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    margin-bottom: 20px;
}

.comment-input button {
    padding: 8px 16px;
    background-color: #007bff;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: all .3s;
}

.comment-input button:hover {
    background-color: #2b4e73;
}
</style>