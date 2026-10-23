<template>
    <div class="page-container fade-up">
        <!-- 置顶公告 -->
        <div class="pinned-card soft-card">
            <div class="section-head">
                <h3 class="section-title">📌 置顶公告</h3>
            </div>
            <ul class="content-list">
                <li v-for="pinnpost in pinnedpost.slice(0, 6)" :key="pinnpost.id">
                    <RouterLink :to="`/postdetail/${pinnpost.id}`">{{ pinnpost.title }}</RouterLink>
                    <p class="timestamp">{{ pinnpost.createTime }}</p>
                </li>
                <li v-if="pinnedpost.length === 0" class="empty-state">暂无置顶公告</li>
            </ul>
        </div>
        <!-- 筛选和搜索 -->
        <div class="toolbar soft-card">
            <el-radio-group v-model="classic" @change="classicchange" class="seg">
                <el-radio-button :label="5">咨询领养</el-radio-button>
                <el-radio-button :label="4">寻找猫狗</el-radio-button>
                <el-radio-button :label="6">分享生活</el-radio-button>
                <el-radio-button :label="7">其他</el-radio-button>
            </el-radio-group>
            <el-input v-model="titlecont" placeholder="请输入标题" class="toolbar-search"></el-input>
            <el-button :icon="Search" type="primary" @click="fsearch" circle />
            <el-button @click="flush">刷新</el-button>
            <div class="spacer"></div>
            <el-button type="primary" :icon="DocumentAdd" round @click="islogin">发帖子</el-button>
        </div>
        <!-- 分页 -->
        <div class="pager-bar">
            <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next"
                :page-count="pages" @current-change="handleCurrentChange" />
        </div>
        <!-- 帖子列表 -->
        <div class="post-list">
            <div class="post" v-for="post in postlist" :key="post.id">
                <div class="post-header">
                    <div class="author-avatar">
                        <el-popover placement="bottom"
                            :title="`昵称:${post.user.nickname || post.user.username}`" :width="220" trigger="click">
                            <template #default>
                                电话：{{ post.user.phone }} <br>
                                邮箱：{{ post.user.email }} <br>
                                领养个数：{{ post.user.howAdopt }} <br>
                                宠宠币：{{ post.user.petCoin }}
                            </template>
                            <template #reference>
                                <img :src="resolveImg(post.user.photo)" alt="未上传头像" />
                            </template>
                        </el-popover>
                    </div>
                    <div class="post-title-wrapper">
                        <h3 class="post-title">
                            <RouterLink :to="`/postdetail/${post.id}`">{{ post.title }}</RouterLink>
                        </h3>

                        <p class="post-date">{{ post.createTime }}</p>
                        <p class="post-comment">评论数:{{ post.commentCount }}</p>
                    </div>
                </div>
                <div class="post-footer">
                    <div class="post-author">{{ post.user.nickname || post.user.username }}</div>
                </div>
            </div>
        </div>
    </div>
    <!-- 添加帖子弹层 -->
    <el-dialog v-model="isaddpoot" title="编辑帖子" top="25px">
        <el-form label-position="top">
            <el-form-item label="标题:">
                <el-input v-model="postinfo.title" placeholder="请输入标题" />
            </el-form-item>
            <el-form-item label="类型：">
                <el-select v-model="postinfo.classic" placeholder="选择分类">
                    <el-option label="寻找猫狗" :value="4" />
                    <el-option label="咨询领养" :value="5" />
                    <el-option label="分享生活" :value="6" />
                    <el-option label="其他" :value="7" />
                </el-select>
            </el-form-item>
            <QuillEditor :modules="modules" v-model:content="postinfo.content" theme="snow" :toolbar="toolbarinfo"
                placeholder="输入帖子内容" contentType="html" style="height: 350px;" />
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="isaddpoot = false">取消</el-button>
                <el-button type="primary" @click="addpost">
                    发表
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { QuillEditor } from '@vueup/vue-quill'
import BlotFormatter from 'quill-blot-formatter'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import { DocumentAdd, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus';
import { resolveImg } from '@/utils/url';

onMounted(() => {
    userpostget()
    pinnedpostget()
})

//#region 搜索
const titlecont = ref(null)
function fsearch() {
    if (titlecont.value == null) ElMessage.warning('不能为空！')
    else conditionquery('title', titlecont.value)
    classic.value = null
}
//#endregion

//#region 筛选
const classic = ref(null)
function classicchange() {
    titlecont.value = null
    conditionquery('classic', classic.value)
}
//#endregion

//#region 刷新
function flush() {
    titlecont.value = null
    classic.value = null
    userpostget()
}
//#endregion

//#region 条件查询函数
function conditionquery(col, con) {
    axios.get(`post/${currentpage.value}/0/${col}/${con}`)
        .then(res => {
            if (res.data.code == 200) {
                postlist.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.error('结果为空')
        })
        .catch(err => {
        })
}
//#endregion

//#region 富文本编辑
//富文本图片大小调整
const modules = {
    name: 'blotFormatter',
    module: BlotFormatter,
    options: {},
}

//工具栏
const toolbarinfo = [
    ['bold', 'italic', 'underline', 'strike', 'code-block', { 'header': 1 }, { 'header': 2 },
        { 'list': 'ordered' }, { 'list': 'bullet' }, { 'script': 'sub' }, { 'script': 'super' }],
    [{ 'size': ['small', false, 'large', 'huge'] }],  // custom dropdown
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ 'color': [] }, { 'background': [] }, 'image'],
    [{ 'align': [] }, 'clean'],
]
//#endregion

//#region 用户posts
const postlist = ref([])
function userpostget() {
    conditionquery('uid', 0)
}
//#endregion

//#region 置顶公告
const pinnedpost = ref([])
function pinnedpostget() {
    axios.get(`post/1/0/classic/3`)
        .then(res => {
            pinnedpost.value = res.data.data.records
        })
        .catch(err => {
        })
}
//#endregion

//拿到usersession
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
//判断是否登录函数
function islogin() {
    if (currentUser != null) {
        if (currentUser.comPermiss) {
            // 发帖人由后端从令牌解析，前端不再提交 uid
            isaddpoot.value = true
        } else {
            ElMessage.warning('由于违规你的权限已被限制')
        }
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}

//#region 添加帖子（posting 防重复提交）
const isaddpoot = ref(false)
const posting = ref(false)
const postinfo = reactive({
    title: '',
    content: '',
    uid: null,
    type: 0,
    classic: null,
})
function addpost() {
    if (postinfo.title == '' || postinfo.content == '' || postinfo.classic == null) {
        ElMessage.warning('标题、类别、内容都不能为空！！')
    } else {
        ElMessageBox.confirm('您确定要发布吗？', '确认发布', {
            cancelButtonText: '继续编辑',
            confirmButtonText: '确定',
            type: 'info'
        }).then(() => {
            if (posting.value) return
            posting.value = true
            axios.post('/post', postinfo)
                .then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success(res.data.msg)
                    } else ElMessage.error(res.data.msg)
                })
                .catch(err => {
                }).finally(() => {
                    posting.value = false
                    isaddpoot.value = false
                    userpostget()
                })
        }).catch(() => { })
    }
}
//#endregion

//#region 分页
const currentpage = ref(1)
const pages = ref(0)

function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (titlecont.value != null) conditionquery('title', titlecont.value)
    else if (classic.value != null) conditionquery('classic', classic.value)
    else userpostget()
}
//#endregion

</script>

<style scoped>
/* ===== 置顶公告卡 ===== */
.pinned-card {
    padding: 20px 24px;
    margin-bottom: 18px;
}

.content-list {
    list-style: none;
    margin: 0;
    padding: 0;
}

.content-list li {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    border: 1px solid var(--line);
    padding: 10px 14px;
    margin-bottom: 10px;
    border-radius: var(--radius-sm);
    background: #fff;
    transition: box-shadow .25s ease, transform .25s ease, background-color .2s ease;
}

.content-list li:hover {
    box-shadow: var(--shadow-sm);
    background: var(--brand-100);
}

.content-list li a {
    flex: 1;
    min-width: 0;
    text-decoration: none;
    color: var(--ink-1);
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.content-list li a:hover {
    color: var(--brand);
}

.timestamp {
    margin: 0;
    flex-shrink: 0;
    color: var(--ink-3);
    font-size: 12px;
}

/* ===== 工具栏 ===== */
.toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    margin-bottom: 16px;
}

.toolbar-search {
    width: 260px;
    max-width: 100%;
}

.spacer {
    flex: 1;
}

.pager-bar {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.author-avatar {
    width: 60px;
    height: 60px;
    overflow: hidden;
    border-radius: 50%;
    margin-right: 10px;
    flex-shrink: 0;
    border: 1px solid var(--line);
}

.author-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: pointer;
}

.post-title-wrapper {
    flex: 1;
    min-width: 0;
}

/* ===== 帖子卡片：白卡 + 品牌描边 hover ===== */
.post-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    grid-gap: 20px;
    margin-bottom: 20px;
}

.post {
    border: 1px solid var(--line);
    border-radius: var(--radius);
    padding: 20px;
    background-color: #ffffff;
    box-shadow: var(--shadow-sm);
    transition: box-shadow .25s ease, transform .25s ease;
}

.post:hover {
    box-shadow: var(--shadow);
    transform: translateY(-3px);
}

.post-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.post-title {
    font-size: 19px;
    margin: 0;
    font-weight: 600;
}

.post-title a {
    text-decoration: none;
    color: inherit;
}

.post-title a:hover {
    color: var(--brand);
}

.post-date {
    font-size: 13px;
    color: var(--ink-3);
    margin: 0;
}

.post-comment {
    font-size: 14px;
    color: var(--ink-2);
    margin: 4px 0;
    line-height: 1.6;
}

.post-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    color: var(--ink-3);
    border-top: 1px dashed var(--line);
    padding-top: 10px;
}

.post-author {
    font-weight: 600;
    color: var(--ink-2);
}

.dialog-footer button:first-child {
    margin-right: 10px;
}
</style>
