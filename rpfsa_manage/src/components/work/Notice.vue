<template>
    <!-- 添加首页公告 -->
    <div>
        <el-button type="primary" plain @click="isaddpoot = true" style="margin-right: 30px;">
            添加
        </el-button>
    </div>
    <!-- 表格数据 -->
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column label="公告标题" width="300" align="center">
            <template #="scope">
                <el-text truncated>{{ scope.row.title }}</el-text>
            </template>
        </el-table-column>
        <el-table-column label="公告内容" width="140" align="center">
            <template #="scope">
                <el-link type="primary" @click="showdetail(scope.row.content)">查看详情</el-link>
            </template>
        </el-table-column>
        <el-table-column label="公告类型" width="140" :filters="[
            { text: '网站公告', value: '0' },
            { text: '领养指南', value: '1' },
            { text: '近期新闻', value: '2' },
        ]" :filter-method="filterTag" filter-placement="bottom-end" :filter-multiple="false" align="center">
            <template #="scope">
                {{ scope.row.classic == 0 ? '网站公告' : scope.row.classic == 1 ? '领养指南' : '近期新闻' }}
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发表时间" width="170" align="center"></el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #="scope">
                <el-button type="danger" :icon="Delete" circle @click="deletepost(scope.row.id)" />
            </template>
        </el-table-column>
    </el-table>
    <!-- 分页 -->
    <div style="margin:10px 20px; float:right;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>
    <!-- 详细弹层 -->
    <el-dialog v-model="pdialogvisible" title="公告详情">
        <div class="el-dialog-div">
            <div class="ql-editor" v-html="postdetail"></div>
        </div>
    </el-dialog>
    <!-- 添加弹层 -->
    <el-dialog v-model="isaddpoot" title="公告编辑" top="25px">
        <el-form label-position="left">
            <el-form-item label="标题:">
                <el-input v-model="postinfo.title" placeholder="请输入标题" />
            </el-form-item>
            <el-form-item label="类型：">
                <el-select v-model="postinfo.classic" placeholder="选择分类">
                    <el-option label="网站公告" :value="0" />
                    <el-option label="领养指南" :value="1" />
                    <el-option label="近期新闻" :value="2" />
                </el-select>
            </el-form-item>
            <QuillEditor :modules="modules" v-model:content="postinfo.content" theme="snow" :toolbar="toolbarinfo"
                placeholder="输入公告内容" contentType="html" style="height: 350px;" />
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="cancelclear()">取消</el-button>
                <el-button type="primary" @click="addpost">
                    发表
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import axios from 'axios'
import { Delete } from '@element-plus/icons-vue'
import 'quill/dist/quill.core.css'
import { ref, onMounted, reactive } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import BlotFormatter from 'quill-blot-formatter'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage, ElMessageBox } from 'element-plus'

onMounted(() => {
    postget()
})

//筛选标签
function filterTag(value, row) {
    console.log(row.classic, value);
    return row.classic == value
}

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

//#region 添加公告
const isaddpoot = ref(false)
const postinfo = reactive({
    title: null,
    content: null,
    uid: 0,
    isPinned: 0,
    type: 1,
    classic: null  //0:网站公告,1:领养指南,2:近期新闻
})
function cancelclear() {
    isaddpoot.value = false
    postinfo.title = null,
        postinfo.content = '',
        postinfo.uid = 0,
        postinfo.isPinned = 0,
        postinfo.type = 1,
        postinfo.classic = null
}
function addpost() {
    axios.post('/post', postinfo)
        .then(() => {
            cancelclear()
            postget()
        })
        .catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 删除公告
function deletepost(id) {
    ElMessageBox.confirm('PS:该操作不可撤销', '删除操作', {
        type: 'warning',
        lockScroll: false
    }).then(() => {
        axios.delete(`/post/${id}`)
            .then(res => {
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

//#region 公告列表
function postget() {
    axios.get(`post/${currentpage.value}/1`)
        .then(res => {
            tableData.value = res.data.data.records
            pages.value = res.data.data.pages
        })
        .catch(err => {
            console.error(err);
        })
}
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

//分页
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    postget()
}
//#endregion

//#region 公告详细
const pdialogvisible = ref(false)
const postdetail = ref()
function showdetail(val) {
    postdetail.value = val
    pdialogvisible.value = true
}
//#endregion



</script>

<style scoped>
.el-dialog-div {
    height: 60vh;
    overflow: auto;
}
</style>