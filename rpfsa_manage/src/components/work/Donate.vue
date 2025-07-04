<template>
    <!-- 搜索和刷新 -->
    <el-input v-model="searchinfo.con" placeholder="请输入" class="input-with-select" style="width: 300px;">
        <template #prepend>
            <el-select v-model="searchinfo.col" placeholder="选择" style="width: 90px">
                <el-option label="用户名" value="username" />
                <el-option label="用户昵称" value="nickname" />
                <el-option label="宠物名" value="petname" />
            </el-select>
        </template>
    </el-input>
    <el-button :icon="Search" type="success" @click="fsearch" style="margin-left: 5px;" />
    <el-button :icon="Refresh" type="warning" @click="flush" style="margin-left: 20px;" />

    <!-- 表格数据 -->
    <el-table :data="donates" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column prop="username" label="用户名" width="150" align="center"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" align="center"></el-table-column>
        <el-table-column prop="petname" label="宠物名" width="150" align="center"></el-table-column>

        <el-table-column label="捐助详细" width="150" align="center">
            <template #="scope">
                <el-link type="primary" @click="listdetail(scope.row.donateList, scope.row.totalPrice)">详细</el-link>
            </template>
        </el-table-column>
        <el-table-column label="状态" width="150" align="center">
            <template #="scope">
                <el-tag v-if="scope.row.state" type="success">已完成分配</el-tag>
                <el-tag v-else type="warning">未完成分配</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="捐助日期" width="180" align="center"></el-table-column>

        <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #="scope">
                <el-button type="success" :icon="Check" circle @click="updatestate(scope.row.id)"
                    :disabled="scope.row.state" />
            </template>
        </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin:10px 20px; float:right;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>

    <!-- 捐助详情弹层 -->
    <el-dialog v-model="dialogTableVisible" title="捐助详细">
        <div v-for="don in donatel" :key="don.id">
            <div v-if="don.tempCount != 0">
                {{ don.foodname }}:
                单价:{{ don.price }}
                数量:{{ don.tempCount }}
                共:{{ don.price * don.tempCount }} 宠宠币
            </div>
        </div>
        <div style="float: right;">一共{{ totalprcie }}宠宠币</div>
    </el-dialog>
</template>

<script setup>
import axios from 'axios';
import { onMounted, ref, reactive } from 'vue';
import { Check } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue'

//#region 模糊查询
const searchinfo = reactive({
    col: null,
    con: null
})

//刷新
function flush() {
    searchinfo.col = null
    searchinfo.con = null
    donateget()
}
function fsearch() {
    axios.get(`/donate/${currentpage.value}/${searchinfo.col}/${searchinfo.con}`)
        .then(res => {
            if (res.data.code == 200) {
                donates.value = res.data.data.records
                pages.value = res.data.data.pages
                for (let i = 0; i < donates.value.length; i++) donates.value[i].donateList = JSON.parse(donates.value[i].donateList)
            } else ElMessage.error(res.data.msg)
        }).catch(err => {
            console.error(err);
        })
}
//#endregion

onMounted(() => {
    donateget()
})

//#region 展示详细
const dialogTableVisible = ref(false)
const donatel = ref([])
const totalprcie = ref(0)
function listdetail(list, total) {
    donatel.value = list
    totalprcie.value = total
    dialogTableVisible.value = true
}
//#endregion

//#region 记录列表
function donateget() {
    axios.get(`donate/page/${currentpage.value}`).then((res) => {
        if (res.data.code == 200) {
            console.log(res.data.data);
            pages.value = res.data.data.pages
            donates.value = res.data.data.records
            for (let i = 0; i < donates.value.length; i++) donates.value[i].donateList = JSON.parse(donates.value[i].donateList)
        } else ElMessage.warning(res.data.msg)
    })
}

const donates = ref([])
const currentpage = ref(1)
const pages = ref(0)


function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) donateget()
    else fsearch()
}
//#endregion

//#region 分配库存
function updatestate(donid) {
    ElMessageBox.confirm(
        '确定该捐助品已分配完成吗?月底会查账的哦!',
        '完成分配确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(() => {
            axios.patch('/donate', {
                id: donid,
                state: 1,
                isRead: 1
            }).then((res) => {
                donateget()
            })
        })
        .catch(() => {
            ElMessage.info('已取消')
        })

}
//#endregion

</script>

<style></style>