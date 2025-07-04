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
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column prop="username" label="用户名" width="100" align="center"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="100" align="center"></el-table-column>
        <el-table-column prop="user.email" label="邮箱" width="180" align="center"></el-table-column>
        <el-table-column prop="user.phone" label="电话" width="130" align="center"></el-table-column>
        <el-table-column prop="petname" label="动物" width="100" align="center"></el-table-column>
        <el-table-column label="种类" width="80" align="center">
            <template #="scope">
                {{ scope.row.pet.classic == 1 ? '小猫' : '小狗' }}
            </template>
        </el-table-column>
        <el-table-column prop="pet.variety" label="品种" width="120" align="center"></el-table-column>
        <el-table-column prop="pet.isVaccine" label="疫苗" width="80" align="center">
            <template #="scope">
                <el-tag v-if="scope.row.pet.isVaccine" type="success" disable-transitions="true">已接种</el-tag>
                <el-tag v-else type="danger" disable-transitions="true">未接种</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" align="center"></el-table-column>
        <el-table-column label="审核状态" width="100" :filters="[
            { text: '通过', value: '1' },
            { text: '审核中', value: '2' },
            { text: '未通过', value: '0' },
        ]" :filter-method="filterTag" filter-placement="bottom-end" :filter-multiple="false" align="center">
            <template #="scope">
                <el-tag v-if="scope.row.state == 1" type="success">通过</el-tag>
                <el-tag v-else-if="scope.row.state == 2" type="warning">审核中</el-tag>
                <el-tag v-else type="danger">未通过</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220" align="center">
            <template #="scope">
                <el-button type="primary" size="small" @click="detail(scope.row)">详细</el-button>
                <el-button :disabled="scope.row.state != 2" type="success" size="small"
                    @click="senddata(scope.row)">审核</el-button>
            </template>
        </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin:10px 20px; float:right;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>

    <!-- 弹层标签 -->
    <el-dialog v-model="dialogFormVisible" title="审核操作">
        <el-form>
            <el-form-item label="发送用户通知:">
                <el-input type="textarea" :rows="4" v-model="adoptinfo.ponreason" />
            </el-form-item>
            <el-form-item label="是否通过审核:">
                <el-radio-group v-model="adoptinfo.ispass" fill="green">
                    <el-radio-button :label="1" size="large">通过</el-radio-button>
                </el-radio-group>
                <el-radio-group v-model="adoptinfo.ispass" fill="red" style="margin-left: 50px;">
                    <el-radio-button :label="0" size="large">不通过</el-radio-button>
                </el-radio-group>

            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="clear">取消</el-button>
                <el-button type="primary" @click="passornot">确定</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 申请详细 -->
    <el-dialog v-model="dialogadoptVisible" title="申请详细">
        <div>申请人:{{ copyadoptinfo.user.nickname }}</div>
        <div>申请手机号:{{ copyadoptinfo.user.phone }}</div>
        <div>申请原因:{{ copyadoptinfo.reason }}</div>
        <div>申请地址:{{ copyadoptinfo.adress }}</div>
        <div>申请时间:{{ copyadoptinfo.applyTime }}</div>
        <div>回复通知:{{ copyadoptinfo.ponReason }}</div>
    </el-dialog>
</template>
  
<script setup>
import axios from 'axios'
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import { Search, Refresh } from '@element-plus/icons-vue'

onMounted(() => {
    adoptget()
})

//#region 模糊查询
const searchinfo = reactive({
    col: null,
    con: null
})

//刷新
function flush() {
    searchinfo.col = null
    searchinfo.con = null
    adoptget()
}
function fsearch() {
    axios.get(`/adopt/${currentpage.value}/${searchinfo.col}/${searchinfo.con}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.error(res.data.msg)
        }).catch(err => {
            console.error(err);
        })
}
//#endregion

//筛选标签
function filterTag(value, row) {
    return row.state == value
}

//#region 申请详细 
const dialogadoptVisible = ref(false)
const copyadoptinfo = ref()
function detail(data) {
    dialogadoptVisible.value = true
    copyadoptinfo.value = data
}


//#endregion

//#region 审核
const dialogFormVisible = ref(false)
const adoptinfo = reactive({
    ponreason: null,
    ispass: null,
    id: null,
    uid: null,
    pid: null,
})
const userhowadopt = ref()
function senddata(data) {
    dialogFormVisible.value = true
    adoptinfo.id = data.id
    adoptinfo.uid = data.user.id
    adoptinfo.pid = data.pet.id
    userhowadopt.value = data.user.howAdopt
}
function passornot() {
    console.log(adoptinfo.ispass);
    axios.patch('/adopt', {
        id: adoptinfo.id,
        state: adoptinfo.ispass,
        ponReason: adoptinfo.ponreason,
        isRead: 1,
    }).then(() => {
        ElMessage.success('通知已发送！')
        adoptget()
        console.log(adoptinfo.ispass);
        axios.patch('/pet', {
            id: adoptinfo.pid,
            isAdopt: adoptinfo.ispass,
            uid: adoptinfo.ispass ? adoptinfo.uid : 0
        }).then(() => {
            console.log(adoptinfo.ispass);
            if (adoptinfo.ispass) {
                axios.patch('/user', {
                    id: adoptinfo.uid,
                    howAdopt: userhowadopt.value + 1
                })
            }
            clear()
        })
    }).catch(() => {
        ElMessage.error('服务器故障,请稍后！')
    })
}

function clear() {
    dialogFormVisible.value = false
    adoptinfo.ponreason = null
    adoptinfo.ispass = null
}
//#endregion

//#region 分页数据
//表格数据
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

//分页
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) adoptget()
    else fsearch()
}
// 查询申请列表
function adoptget() {
    axios.get(`/adopt/${currentpage.value}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.warning(res.data.msg)
        })
}
//#endregion

</script>
<style scoped>
.avatar-uploader .avatar {
    width: 120px;
    height: 140px;
    display: block;
}

.el-button--text {
    margin-right: 15px;
}

.el-select {
    width: 300px;
}

.el-input {
    width: 300px;
}

.dialog-footer button:first-child {
    margin-right: 10px;
}
</style>

<style>
.cancelbtn {
    background-color: #de4b0c !important;
    color: white !important;

}

.cancelbtn :hover {
    color: white !important;
}

.confirmbtn {
    background-color: green !important;
}

.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 120px;
    height: 140px;
    text-align: center;
}
</style>
  