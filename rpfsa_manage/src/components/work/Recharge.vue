<template>
    <div class="page-head">
        <h2 class="page-title">充值记录</h2>
        <span class="page-desc">用户宠宠币充值明细</span>
    </div>
    <div class="panel">
        <!-- 列表数据 -->
        <el-table :data="tableData" style="width: 100%;" height="570" stripe highlight-current-row>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column label="用户" min-width="160" align="center">
                <template #="scope">
                    {{ scope.row.user ? (scope.row.user.nickname || scope.row.user.username) : '已注销' }}
                </template>
            </el-table-column>
            <el-table-column prop="amount" label="充值金额（元）" width="160" align="center" />
            <el-table-column prop="coin" label="到账宠宠币" width="140" align="center">
                <template #="scope">
                    <span style="color: var(--brand-600); font-weight: 700;">+{{ scope.row.coin }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="充值时间" min-width="200" align="center" />
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

onMounted(() => {
    rechargeget()
})

//#region 分页数据
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

function handleCurrentChange(c) {
    currentpage.value = c
    rechargeget()
}

function rechargeget() {
    axios.get(`/wallet/admin/${currentpage.value}`).then(res => {
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
</script>
