<template>
    <!-- 置顶公告 -->
    <div style="height: 200px;">
        <h3>置顶公告</h3>
        <ul class="content-list">
            <li v-for="pinnpost in pinnedpost.slice(0, 6)" :key="pinnpost.id">
                <RouterLink :to="`/postdetail/${pinnpost.id}`">{{ pinnpost.title }}</RouterLink>
                <p class="timestamp">{{ pinnpost.createTime }}</p>
            </li>
        </ul>
    </div>
    <hr>
    <!-- 筛选和搜索 -->
    <div style="margin: 10px">
        <el-radio-group v-model="classic" @change="classicchange">
            <el-radio-button :label="5">咨询领养</el-radio-button>
            <el-radio-button :label="4">寻找猫狗</el-radio-button>
            <el-radio-button :label="6">分享生活</el-radio-button>
            <el-radio-button :label="7">其他</el-radio-button>
        </el-radio-group>
        <el-input v-model="titlecont" placeholder="请输入标题" style="width: 300px;"></el-input>
        <el-button :icon="Search" type="success" @click="fsearch" style="margin-left: 5px;" />
        <el-button @click="flush" style="margin: 10px;">刷新</el-button>
        <el-button type="success" :icon="DocumentAdd" circle size="large" @click="islogin" style="float:right" />
    </div>
    <!-- 分页 -->
    <div style="margin:10px 20px;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>
    <!-- 帖子列表 -->
    <div class="post-list">
        <div class="post" v-for="post in postlist" :key="post.id">
            <div class="post-header">
                <div class="author-avatar">
                    <el-popover placement="bottom" :title="`昵称:`" :width="220" trigger="click">
                        <template #>
                            电话：{{ post.user.phone }} <br>
                            邮箱：{{ post.user.email }} <br>
                            领养个数：{{ post.user.howAdopt }} <br>
                            宠宠币：{{ post.user.petCoin }}
                        </template>
                        <template #reference>
                            <img :src="`http://localhost/img/${post.user.photo}`" alt="未上传头像" />
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
                <div class="post-author">{{ }}</div>
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
            console.error(err);
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
            console.error(err);
        })
}
//#endregion

//拿到usersession
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
//判断是否登录函数
function islogin() {
    if (currentUser != null) {
        if (currentUser.comPermiss) {
            isaddpoot.value = true
            postinfo.uid = currentUser.id
        } else {
            ElMessage.warning('由于违规你的权限已被限制')
        }
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}

//#region 添加帖子
const isaddpoot = ref(false)
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
            axios.post('/post', postinfo)
                .then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success(res.data.msg)
                    } else ElMessage.error(res.data.msg)
                })
                .catch(err => {
                    console.error(err);
                }).finally(() => {
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
.content-list li {
    background-color: #f5f5f5;
    padding: 10px;
    margin-bottom: 10px;
    border-radius: 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.3s ease;
    width: 650px;

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



.author-avatar {
    width: 60px;
    height: 60px;
    overflow: hidden;
    border-radius: 50%;
    margin-right: 10px;

}

.author-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: pointer;
}

.post-title-wrapper {
    flex: 1;
}

.post-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    grid-gap: 20px;
    margin-bottom: 20px;
}

.post {
    border: 1px solid #ccc;
    border-radius: 8px;
    padding: 20px;
    background-color: #f7f7f7;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.post-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.post-title {
    font-size: 20px;
    margin: 0;
}

.post-title a {
    text-decoration: none;
    color: inherit;
}


.post-date {
    font-size: 14px;
    color: #888;
    margin: 0;
}

.post-comment {
    font-size: 15px;
    color: #797676;
    margin: 4;
}


.post-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    color: #888;
}

.post-author {
    font-weight: bold;
}


.dialog-footer button:first-child {
    margin-right: 10px;
}
</style>
