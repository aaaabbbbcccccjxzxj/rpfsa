<template>
    <!-- 操作栏 -->
    <div>
        <el-button type="primary" plain @click="isaddpoot = true" style="margin-right: 30px;">
            添加
        </el-button>
        <el-input v-model="titlecont" placeholder="请输入标题" style="width: 300px;"></el-input>
        <el-button :icon="Search" type="success" @click="fsearch" style="margin-left: 5px;" />
        <el-button :icon="Refresh" type="warning" @click="flush" style="margin-left: 20px;" />
    </div>
    <!-- 表格数据 -->
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column label="帖子标题" width="180" align="center">
            <template #="scope">
                <el-text truncated>{{ scope.row.title }}</el-text>
            </template>
        </el-table-column>
        <el-table-column label="帖子分类" width="100" align="center" :filters="[
            { text: '置顶公告', value: '3' },
            { text: '寻找猫狗', value: '4' },
            { text: '咨询领养', value: '5' },
            { text: '生活分享', value: '6' },
            { text: '其他', value: '7' },
        ]" :filter-method="filterTag" filter-placement="bottom-end" :filter-multiple="false">
            <template #="scope">
                {{ classicshow(scope.row.classic) }}
            </template>
        </el-table-column>
        <el-table-column label="帖子内容" width="140" align="center">
            <template #="scope">
                <el-link type="primary" @click="showdetail(scope.row.content)">查看详情</el-link>
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发帖时间" width="170" align="center"></el-table-column>
        <el-table-column prop="commentCount" label="评论数" width="120" align="center"></el-table-column>
        <el-table-column label="评论详细" width="150" align="center">
            <template #="scope">
                <el-link type="warning" @click="commentdetail(scope.row.comments)">查看详情</el-link>
            </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #="scope">
                <el-button type="primary" :icon="Expand" circle :disabled="scope.row.uid == 0"
                    @click="whopost(scope.row)" />
                <el-button type="danger" :icon="Delete" circle @click="deletepost(scope.row.id)" />
            </template>
        </el-table-column>
    </el-table>
    <!-- 分页 -->
    <div style="margin:10px 20px; float:right;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="pdialogvisible" title="帖子详情">
        <el-scrollbar height="420px">
            <div class="ql-editor" v-html="postdetail"></div>
        </el-scrollbar>
    </el-dialog>

    <el-dialog v-model="cdialogvisible" title="评论详情">
        <div class="el-dialog-div">
            <div v-for="comment in commentslist" :key="comment.id" style="margin-top: 15px;">
                <hr>
                用户:{{ comment.user.nickname == null ? comment.user.username : comment.user.nickname }} <br>
                评论内容:{{ comment.content }} <br>
                评论时间:{{ comment.createTime }}
            </div>
        </div>
    </el-dialog>

    <el-dialog v-model="isaddpoot" title="添加公告" top="25px">
        <el-form label-position="left">
            <el-form-item label="标题:">
                <el-input v-model="postinfo.title" placeholder="请输入标题" />
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

    <el-dialog v-model="ddialogvisible" title="详情">
        <h3>发帖人信息</h3>
        <div>用户名：{{ whospost.user.username }}</div>
        <div>昵称：{{ whospost.user.nickname }}</div>
        <div>时间：{{ whospost.createTime }}</div>
    </el-dialog>
</template>

<script setup>
import axios from 'axios'
import { Delete, Search, Refresh } from '@element-plus/icons-vue'
import 'quill/dist/quill.core.css'
import { ref, onMounted, reactive } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import BlotFormatter from 'quill-blot-formatter'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Expand } from '@element-plus/icons'

//#region 模糊查询
const titlecont = ref(null)
function fsearch() {
    if (titlecont.value == null) ElMessage.warning('不能为空！')
    else conditionquery('title', titlecont.value)
}

function flush() {
    titlecont.value = null
    postget()
}

function conditionquery(col, con) {
    axios.get(`post/${currentpage.value}/0/${col}/${con}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.error('查询结果为空')
        }).catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 某帖子基本信息
const ddialogvisible = ref(false)
const whospost = ref({})
function whopost(post) {
    whospost.value = post
    ddialogvisible.value = true
}
//#endregion

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
    [{ 'size': ['small', false, 'large', 'huge'] }],
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ 'color': [] }, { 'background': [] }, 'image'],
    [{ 'align': [] }, 'clean'],
]
//#region 添加
const isaddpoot = ref(false)
const postinfo = reactive({
    title: null,
    content: null,
    uid: 0,
    isPinned: 1,
    type: 0,
    classic: 3
})
function addpost() {
    axios.post('/post', postinfo)
        .then(res => {
            isaddpoot.value = false
            console.log(res)
            postget()
        })
        .catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 删除
function deletepost(id) {
    ElMessageBox.confirm('PS:该操作不可撤销', '删除操作', {
        type: 'warning',
        lockScroll: false
    }).then(() => {
        axios.delete(`/post/${id}`)
            .then(res => {
                console.log(res)
                postget()
                ElMessage.success('删除成功')
            })
            .catch(err => {
                console.error(err);
            })

    }).catch(() => {
        ElMessage.info('已取消')
    })

}
//#endregion

onMounted(() => {
    postget()
})

//#region post列表
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

//分页
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (titlecont.value == null) postget()
    else conditionquery('title', titlecont.value)
}
function postget() {
    axios.get(`post/${currentpage.value}/0`)
        .then(res => {
            tableData.value = res.data.data.records
            pages.value = res.data.data.pages
        })
        .catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 帖子详情
const pdialogvisible = ref(false)
const postdetail = ref()
function showdetail(val) {
    postdetail.value = val
    pdialogvisible.value = true
}
//#endregion

//#region 评论列表详细
const cdialogvisible = ref(false)
const commentslist = ref([])
function commentdetail(val) {
    commentslist.value = val
    cdialogvisible.value = true
}
//#endregion

//#region 类别template和筛选标签
function classicshow(value) {
    let classic = ''
    if (value == 3) {
        classic = '置顶公告'
    } else if (value == 4) {
        classic = '寻找猫狗'
    } else if (value == 5) {
        classic = '咨询领养'
    } else if (value == 6) {
        classic = '生活分享'
    } else {
        classic = '其他'
    }
    return classic
}
function filterTag(value, row) {
    console.log(row.classic, value);
    return row.classic == value
}
//#endregion

</script>

<style scoped>
.el-dialog-div {
    height: 60vh;
    overflow: auto;
}
</style>