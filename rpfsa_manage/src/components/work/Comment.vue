<template>
    <div class="page-head">
        <h2 class="page-title">评论管理</h2>
        <span class="page-desc">社区与宠物评论的检索与清理</span>
    </div>
    <div class="panel">
        <!-- 工具栏 -->
        <div style="margin-bottom: 12px;">
            <el-input v-model="searchCon" placeholder="按评论内容搜索" style="width: 300px;" clearable
                @keyup.enter="fsearch" @clear="flush" />
            <el-button :icon="Search" type="primary" @click="fsearch" style="margin-left: 8px;" />
            <el-button :icon="Refresh" @click="flush" style="margin-left: 8px;">重置</el-button>
        </div>
        <!-- 列表数据 -->
        <el-table :data="tableData" style="width: 100%;" height="570" stripe highlight-current-row>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column label="评论者" width="140" align="center">
                <template #="scope">
                    {{ scope.row.user ? (scope.row.user.nickname || scope.row.user.username) : '已注销' }}
                </template>
            </el-table-column>
            <el-table-column prop="content" label="评论内容" min-width="240" align="center">
                <template #="scope">
                    <el-text truncated>{{ scope.row.content }}</el-text>
                </template>
            </el-table-column>
            <el-table-column label="归属" width="200" align="center">
                <template #="scope">
                    <el-tag size="small" :type="scope.row.poid ? 'primary' : 'warning'" effect="light" round>
                        {{ scope.row.targetTitle }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="层级" width="90" align="center">
                <template #="scope">
                    {{ scope.row.level === 0 ? '一级评论' : '二级评论' }}
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发表时间" width="170" align="center" />
            <el-table-column fixed="right" label="操作" width="120" align="center">
                <template #="scope">
                    <el-button type="danger" :icon="Delete" size="small" @click="deleteComment(scope.row)" />
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页 -->
        <div class="pager-row">
            <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next"
                :page-count="pages" @current-change="handleCurrentChange" />
        </div>
    </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { Delete, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

onMounted(() => {
    commentget()
})

//#region 搜索
const searchCon = ref('')
function fsearch() {
    currentpage.value = 1
    commentget()
}
function flush() {
    searchCon.value = ''
    currentpage.value = 1
    commentget()
}
//#endregion

//#region 分页数据
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

function handleCurrentChange(c) {
    currentpage.value = c
    commentget()
}

function commentget() {
    const url = searchCon.value
        ? `/comment/page/${currentpage.value}/content/${encodeURIComponent(searchCon.value)}`
        : `/comment/page/${currentpage.value}`
    axios.get(url).then(res => {
        if (res.data.code === 200) {
            tableData.value = res.data.data.records
            pages.value = res.data.data.pages
        } else {
            tableData.value = []
            pages.value = 0
        }
    }).catch(() => {})
}
//#endregion

//#region 删除
function deleteComment(row) {
    ElMessageBox.confirm('一级评论会连带删除其下所有回复，确定删除吗？', '删除评论', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        // 一级评论走 t1 接口连带删子评论，二级评论走普通接口
        const url = row.level === 0 ? `/comment/t1/${row.id}` : `/comment/${row.id}`
        axios.delete(url).then(res => {
            if (res.data.code === 200) {
                ElMessage.success('删除成功')
                commentget()
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(() => {})
    }).catch(() => {})
}
//#endregion
</script>
