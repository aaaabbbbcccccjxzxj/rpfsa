<template>
    <!-- 操作栏 -->
    <div>
        <!-- 添加用户 -->
        <el-button type="primary" plain @click="dialogFormVisible = true" style="margin-right: 30px;">
            添加用户
        </el-button>

        <!-- 搜索和刷新 -->
        <el-input v-model="searchinfo.con" placeholder="请输入" class="input-with-select" style="width: 300px;">
            <template #prepend>
                <el-select v-model="searchinfo.col" placeholder="选择" style="width: 90px">
                    <el-option label="用户名" value="username" />
                    <el-option label="昵称" value="nickname" />
                    <el-option label="电话" value="phone" />
                    <el-option label="邮箱" value="email" />
                </el-select>
            </template>
        </el-input>
        <el-button :icon="Search" type="success" @click="fsearch" style="margin-left: 5px;" />
        <el-button :icon="Refresh" type="warning" @click="flush" style="margin-left: 20px;" />
    </div>
    <!-- 列表数据 -->
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column prop="username" label="用户名" width="100" align="center" />
        <el-table-column prop="nickname" label="昵称" width="180" align="center" />
        <el-table-column prop="photo" label="头像" width="auto" align="center">
            <template #="scope">
                <el-image style="width: 50px; height: 55px;" :src="getImageUrl(scope.row.photo)"
                    :preview-src-list="[getImageUrl(scope.row.photo)]" preview-teleported="true" />
            </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" align="center" />
        <el-table-column prop="phone" label="电话" width="120" align="center" />
        <el-table-column prop="comPermiss" label="权限" width="80" align="center">
            <template #="scope">
                <el-switch v-model="scope.row.comPermiss" :active-value="1" :inactive-value="0"
                    style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                    @change="closecomment($event, scope.row.id)" />
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column prop="petCoin" label="宠宠币" width="80" align="center" />
        <el-table-column prop="howAdopt" label="已领养" width="80" align="center" />
        <el-table-column fixed="right" label="操作" width="200" align="center">
            <template #="scope">
                <el-button type="primary" :icon="Expand" size="small" @click="edituser(scope.row)" />
                <el-button type="danger" :icon="Delete" size="small" @click="deleteuser(scope.row.id)" />
                <el-badge :value="scope.row.newmessage" class="item">
                    <el-button style="margin-left: 12px; margin-bottom: 10px;" type="info" :icon="ChatDotSquare"
                        size="small" @click="sendmessageopen(scope.row.id, scope.$index)" />
                </el-badge>
            </template>
        </el-table-column>
    </el-table>
    <!-- 分页 -->
    <div style="margin:10px 20px; float:right;">
        <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next" :page-count="pages"
            @current-change="handleCurrentChange" />
    </div>
    <!-- 弹层标签 -->
    <!-- 添加 -->
    <el-dialog v-model="dialogFormVisible" title="填写用户信息" style="border-radius: 20px; width: 650px;">
        <el-form label-position="left" label-width="80px" :model="userinfo" style="max-width: 450px">
            <el-form-item label="用户名:">
                <el-input v-model="userinfo.username" required="true" />
            </el-form-item>
            <el-form-item label="密码:">
                <el-input v-model="userinfo.password" />
            </el-form-item>
            <el-form-item label="手机号:">
                <el-input v-model="userinfo.phone" />
            </el-form-item>
            <el-form-item label="头像:">
                <el-upload ref="uploadRef" class="avatar-uploader" action="http://localhost/upload" :show-file-list="false"
                    :on-success="handleAvatarSuccess" :on-change="changeAvatar" :auto-upload="false"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <span>
                <el-button @click="cancelclear">取消</el-button>
                <el-button type="primary" @click="adduser">添加</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 详情||修改 -->
    <el-dialog v-model="dialogFormVisible_x" title="修改用户信息" style="border-radius: 20px; width: 650px;">
        <el-form label-position="left" label-width="80px" :model="userinfo_x.value" style="max-width: 450px">
            <el-form-item label="ID:">
                <el-input v-model="userinfo_x.value.id" disabled />
            </el-form-item>
            <el-form-item label="创建时间:">
                <el-input v-model="userinfo_x.value.createTime" disabled />
            </el-form-item>
            <el-form-item label="用户名:">
                <el-input v-model="userinfo_x.value.username" />
            </el-form-item>
            <el-form-item label="密码:">
                <el-input v-model="userinfo_x.value.password" />
            </el-form-item>
            <el-form-item label="昵称:">
                <el-input v-model="userinfo_x.value.nickname" />
            </el-form-item>
            <el-form-item label="手机号:">
                <el-input v-model="userinfo_x.value.phone" disabled />
            </el-form-item>
            <el-form-item label="邮箱:">
                <el-input v-model="userinfo_x.value.email" disabled />
            </el-form-item>
            <el-form-item label="宠宠币:">
                <el-input type="number" v-model="userinfo_x.value.petCoin" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span>
                <el-button @click="dialogFormVisible_x = false">取消</el-button>
                <el-button type="primary" @click="updateuser">修改</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 发送message -->
    <el-dialog v-model="dialogsendVisible" @open="gun" style="border-radius: 20px; width: 650px;"
        @close="removenewmessage()">
        <!-- 展示message列表 -->
        <div id="messagesroll" class="el-dialog-div">
            <div v-for="message in messagelist" :key="message.id">
                <span v-if="message.content != null" style="text-align: right;">
                    <div style="margin-right: 20px;font-size: larger;">{{ message.content }}</div>
                    <div style="margin-right: 20px;">{{ message.createTime }} </div>
                    <hr>
                </span>
                <span v-else>
                    <div :class="isred(message.isRead)">
                        <div> 用户:</div>
                        <div style="font-size: larger;">{{ message.userReply }}</div>
                        <div>{{ message.createTime }}</div>
                    </div>

                    <hr>
                </span>
            </div>
        </div>
        <template #footer>
            <el-input v-model="sendinfo.content" type="textarea" :rows="3" style="margin-bottom: 20px;" />
            <span>
                <el-button @click="dialogsendVisible = false">关闭</el-button>
                <el-button type="primary" @click="sendmessage">发送</el-button>
            </span>
        </template>
    </el-dialog>
</template>
  
<script setup >
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue';
import { Expand, Delete, ChatDotSquare, Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

onMounted(() => {
    userget()
})

//#region 搜索功能(模糊查询)
const searchinfo = reactive({
    col: null,
    con: null
})
function fsearch() {
    axios.get(`/user/${currentpage.value}/${searchinfo.col}/${searchinfo.con}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
                for (let i = 0; i < tableData.value.length; i++) {
                    axios.get(`/message/noread/${tableData.value[i].id}/admin`).then(res => {
                        Reflect.set(tableData.value[i], "newmessage", res.data.data != 0 ? res.data.data : undefined)
                    })
                }
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(err => {
            console.error(err);
        })
}
//刷新
function flush() {
    searchinfo.col = null
    searchinfo.con = null
    userget()
}
//#endregion

//vue3访问静态资源
function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }

//#region 用户展示
//表格数据
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)

//翻页
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) userget()
    else fsearch()
}

// 分页查询用户列表
function userget() {
    axios.get(`/user/page/${currentpage.value}`)
        .then(res => {
            tableData.value = res.data.data.records
            pages.value = res.data.data.pages
            for (let i = 0; i < tableData.value.length; i++) {
                //可以增加属性,卧槽,牛逼
                axios.get(`/message/noread/${tableData.value[i].id}/admin`).then(res => {
                    Reflect.set(tableData.value[i], "newmessage", res.data.data != 0 ? res.data.data : undefined)
                })
            }
        })
}

//#endregion

//#region 修改用户
const dialogFormVisible_x = ref(false)
const userinfo_x = reactive({})
function edituser(user) {
    userinfo_x.value = user
    dialogFormVisible_x.value = true
}
function updateuser() {
    axios.patch('/user', userinfo_x.value).then((res) => {
        if (res.data.code == 200) {
            ElMessage.success(res.data.msg)
            userget()
        } else ElMessage.error(res.data.msg)
    })
    dialogFormVisible_x.value = false
}
//#endregion

//#region 删除用户
function deleteuser(id) {
    ElMessageBox.confirm('你确定要删除吗？该操作不可撤回哦!', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        axios.delete(`/user/${id}`)
            .then((res) => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    userget()
                } else ElMessage.error(res.data.msg)
            })
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
//#endregion

//#region 消息发送
const dialogsendVisible = ref(false)
const sendinfo = reactive({
    uid: null,
    content: null,
    isRead: 1
})
function gun() {
    setTimeout(() => {
        var messagesroll = document.getElementById("messagesroll")
        messagesroll.scrollTop = messagesroll.scrollHeight
    }, 80)
}
const messagelist = ref([])
function sendmessageopen(userid, index) {
    tempindex.value = index
    sendinfo.uid = userid
    //查询该用户的所有message
    axios.get(`/message/${userid}`)
        .then(res => {
            messagelist.value = res.data.data
        })
        .catch(err => {
            console.error(err);
        })
    dialogsendVisible.value = true
}
const tempindex = ref()
function removenewmessage() {
    axios.patch(`/message/${sendinfo.uid}/admin`)
    console.log(tableData.value[tempindex.value]);
    tableData.value[tempindex.value].newmessage = undefined
    console.log(tableData.value[tempindex.value].newmessage);
}
function sendmessage() {
    axios.post('/message', sendinfo)
        .then(() => {
            sendmessageopen(sendinfo.uid, tempindex.value)
            gun()
        })
        .catch(err => {
            console.error(err);
        }).finally(() => sendinfo.content = null)
}
function isred(is) {
    let color = ''
    if (is) color = 'red'
    return color
}
//#endregion

//#region 添加用户
const dialogFormVisible = ref(false)
const userinfo = reactive({
    username: '',
    password: '',
    phone: '',
    photo: '',
})
function adduser() {
    if (imageUrl.value == '' || userinfo.username == '' || userinfo.password == '' || userinfo.phone == '') {
        ElMessage.warning('请完善表单！！')
    }
    else uploadRef.value.submit()
}
function cancelclear() {
    dialogFormVisible.value = false
    userinfo.username = ''
    userinfo.password = ''
    userinfo.phone = ''
    userinfo.photo = ''
    imageUrl.value = null
}
//上传头像
const imageUrl = ref('')
const uploadRef = ref(null)
function handleAvatarSuccess(response) {
    userinfo.photo = response
    axios.post('/user', userinfo)
        .then((res) => {
            if (res.data.code == 200) {
                ElMessage.success(res.data.msg)
            } else ElMessage.error(res.data.msg)
        }).catch(() => ElMessage.error('服务器故障'))
        .then(() => {
            cancelclear()
            userget()
        })
}
function changeAvatar(uploadFile) {
    imageUrl.value = URL.createObjectURL(uploadFile.raw)
}
function beforeAvatarUpload(rawFile) {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('上传的头像必须为jpg/png格式!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
    }
    return true
}
//#endregion

//#region 权限切换
function closecomment(newval, id) {
    //发送数据改变评论权限
    axios.patch('/user', {
        id: id,
        comPermiss: newval
    }).then(() => {
        if (newval) ElMessage.success(`评论权限已打开\^o^/`)
        else ElMessage.warning('评论权限已关闭ಥ_ಥ')
    }).catch(() => ElMessage.error('对不起,服务器故障！'))
}

//#endregion

</script>

<style scoped>
.red {
    color: red;
    font-size: large;
    font-weight: 600;
}

.item {
    margin-top: 10px;
    margin-right: 40px;
}

.avatar-uploader .avatar {
    width: 120px;
    height: 140px;
    display: block;
}

.input-with-select {
    background-color: var(--el-fill-color-blank);
}

.el-dialog-div {
    height: 40vh;
    overflow: auto;
}
</style>

<style>
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
  