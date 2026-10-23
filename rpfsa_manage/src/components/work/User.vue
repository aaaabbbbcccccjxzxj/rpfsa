<template>
    <div class="page-head">
        <h2 class="page-title">用户管理</h2>
        <span class="page-desc">平台注册用户的信息、权限与消息</span>
    </div>
    <div class="panel">
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
        <el-table-column prop="nickname" label="昵称" min-width="180" align="center" />
        <el-table-column prop="photo" label="头像" width="80" align="center">
            <template #="scope">
                <el-image style="width: 50px; height: 55px;" :src="resolveImg(scope.row.photo)"
                    :preview-src-list="[resolveImg(scope.row.photo)]" preview-teleported="true" />
            </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" />
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
                <el-badge :value="scope.row.noReadCount || undefined" class="item">
                    <el-button style="margin-left: 12px; margin-bottom: 10px;" type="info" :icon="ChatDotSquare"
                        size="small" @click="sendmessageopen(scope.row.id, scope.$index)" />
                </el-badge>
            </template>
        </el-table-column>
    </el-table>
    <!-- 分页 -->
    <div class="pager-row">
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
                <el-upload ref="uploadRef" class="avatar-uploader" :action="uploadUrl" :headers="uploadHeaders()"
                    :on-error="uploadError" :show-file-list="false" :on-success="handleAvatarSuccess" :on-change="changeAvatar"
                    :auto-upload="false" :before-upload="beforeAvatarUpload">
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
        :title="chatUser ? `与 ${chatUser.nickname || chatUser.username} 的对话` : '对话'"
        @close="removenewmessage()">
        <!-- 展示message列表 -->
        <div id="messagesroll" class="el-dialog-div chat-area">
            <div v-if="messagelist.length === 0" class="chat-empty">暂无对话记录</div>
            <div v-for="message in messagelist" :key="message.id">
                <!-- 管理员消息（右侧） -->
                <div v-if="message.content != null" class="chat-row mine">
                    <div class="chat-bubble mine">
                        <div class="chat-content">{{ message.content }}</div>
                        <div class="chat-time mine">{{ message.createTime }}</div>
                    </div>
                    <div class="chat-avatar admin-avatar">管</div>
                </div>
                <!-- 用户消息（左侧） -->
                <div v-else class="chat-row">
                    <el-avatar :size="38" :src="resolveImg(chatUser && chatUser.photo)" class="chat-avatar" />
                    <div class="chat-bubble">
                        <div class="chat-sender">{{ chatUser ? (chatUser.nickname || chatUser.username) : '用户' }}</div>
                        <div class="chat-content">{{ message.userReply }}</div>
                        <div class="chat-time">{{ message.createTime }}</div>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <el-input v-model="sendinfo.content" type="textarea" :rows="3" style="margin-bottom: 20px;"
                placeholder="回复用户消息（Ctrl+Enter 发送）" @keyup.ctrl.enter="sendmessage" />
            <span>
                <el-button @click="dialogsendVisible = false">关闭</el-button>
                <el-button type="primary" :loading="messageSending" @click="sendmessage">发送</el-button>
            </span>
        </template>
    </el-dialog>
    </div>
</template>
  
<script setup >
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue';
import { Expand, Delete, ChatDotSquare, Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resolveImg, resolveUploadUrl, uploadHeaders, uploadError } from '@/utils/url'

const uploadUrl = resolveUploadUrl()

onMounted(() => {
    userget()
})

//#region 搜索功能(模糊查询)
const searchinfo = reactive({
    col: null,
    con: null
})
function fsearch() {
    // 用户管理接口已从 /user/** 收敛到 /admin/users/**，以便做管理员角色隔离
    axios.get(`/admin/users/${currentpage.value}/${searchinfo.col}/${searchinfo.con}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else {
                ElMessage.warning(res.data.msg)
            }
        })
}
//刷新
function flush() {
    searchinfo.col = null
    searchinfo.con = null
    userget()
}
//#endregion

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
    // 未读消息数（noReadCount）已由分页接口用一次聚合查询带回，
    // 原先这里是"每一行单独发一个请求"的前端 N+1，一页 10 行就是 11 个请求。
    axios.get(`/admin/users/${currentpage.value}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else {
                ElMessage.warning(res.data.msg)
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
    axios.patch('/admin/users', userinfo_x.value).then((res) => {
        if (res.data.code == 200) {
            ElMessage.success(res.data.msg)
            userget()
        } else {
            ElMessage.error(res.data.msg)
        }
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
        axios.delete(`/admin/users/${id}`)
            .then((res) => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    userget()
                } else {
                    ElMessage.error(res.data.msg)
                }
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
const chatUser = ref(null)
const messageSending = ref(false)
function sendmessageopen(userid, index) {
    tempindex.value = index
    sendinfo.uid = userid
    chatUser.value = tableData.value[index] || null
    //查询该用户的所有message
    axios.get(`/message/${userid}`)
        .then(res => {
            messagelist.value = res.data.data
        })
        .catch(err => {
        })
    dialogsendVisible.value = true
}
const tempindex = ref()
function removenewmessage() {
    axios.patch(`/message/${sendinfo.uid}/admin`)
    // 直接改本行数据即可，不再依赖 Reflect.set 这种非常规写法
    const row = tableData.value[tempindex.value]
    if (row) {
        row.noReadCount = undefined
    }
}
function sendmessage() {
    if (!sendinfo.content || !sendinfo.content.trim()) {
        ElMessage.warning('消息内容不能为空')
        return
    }
    if (messageSending.value) return
    messageSending.value = true
    axios.post('/message', sendinfo)
        .then(() => {
            sendmessageopen(sendinfo.uid, tempindex.value)
            gun()
        })
        .catch(err => {
        }).finally(() => {
            messageSending.value = false
            sendinfo.content = null
        })
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
    axios.post('/admin/users', userinfo)
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
    // 变更评论权限。带上 username 是因为该接口的更新逻辑以整行数据为准。
    const row = tableData.value.find(item => item.id === id)
    axios.patch('/admin/users', {
        id: id,
        username: row ? row.username : null,
        comPermiss: newval
    }).then(() => {
        if (newval) ElMessage.success(`评论权限已打开\^o^/`)
        else ElMessage.warning('评论权限已关闭ಥ_ಥ')
    }).catch(() => { /* 失败提示已由全局拦截器统一给出 */ })
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

/* ===== 消息气泡（与用户端同款聊天界面） ===== */
.chat-area {
    background: #f7f8fb;
    border-radius: var(--radius);
    padding: 16px 14px;
}

.chat-empty {
    text-align: center;
    color: var(--ink-3);
    font-size: 13px;
    padding: 30px 0;
}

.chat-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 16px;
}

.chat-row.mine {
    flex-direction: row-reverse;
}

.chat-avatar {
    flex-shrink: 0;
    border: 2px solid #fff;
    box-shadow: var(--shadow-sm);
}

.admin-avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background: var(--brand-100);
    color: var(--brand-600);
    font-size: 14px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
}

.chat-bubble {
    max-width: 72%;
    background: #fff;
    border-radius: 4px 16px 16px 16px;
    padding: 10px 14px 6px;
    box-shadow: var(--shadow-sm);
}

.chat-bubble.mine {
    background: linear-gradient(135deg, #ff8a50 0%, #ff6b35 55%, #f25b26 100%);
    color: #fff;
    border-radius: 16px 4px 16px 16px;
    box-shadow: 0 4px 14px rgba(255, 107, 53, .28);
}

.chat-sender {
    font-size: 12px;
    font-weight: 600;
    color: var(--brand-600);
    margin-bottom: 4px;
}

.chat-content {
    font-size: 14px;
    line-height: 1.65;
    word-break: break-word;
    white-space: pre-wrap;
}

.chat-time {
    font-size: 11px;
    color: var(--ink-3);
    margin-top: 4px;
    text-align: right;
}

.chat-time.mine {
    color: rgba(255, 255, 255, .75);
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
  