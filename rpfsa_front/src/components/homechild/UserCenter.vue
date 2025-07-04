<template>
    <!-- 展示用户个人信息 -->
    <div v-if="currentUser != null" class="personal-center">
        <h1 class="title">个人中心</h1>
        <div style="text-align: center;"><el-avatar :size="75" :src="getimgurl(currentUser.photo)" /></div>
        <div class="user-info">
            <div class="info-item">
                <span class="label">用户名:</span>
                <span class="value">{{ currentUser.username }}</span>
            </div>
            <div class="info-item">
                <span class="label">昵称:</span>
                <span class="value">{{ currentUser.nickname }}</span>
            </div>
            <div class="info-item">
                <span class="label">邮箱:</span>
                <span class="value">{{ currentUser.email }}</span>
            </div>
            <div class="info-item">
                <span class="label">手机号:</span>
                <span class="value">{{ currentUser.phone }}</span>
            </div>
            <div class="info-item">
                <span class="label">权限:</span>
                <span class="value"><el-switch v-model="currentUser.comPermiss" disabled :active-value="1"
                        :inactive-value="0" style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949" /></span>
            </div>
            <div class="info-item">
                <span class="label">宠宠币:</span>
                <span class="value">{{ currentUser.petCoin }}</span>
            </div>
        </div>
        <div class="button-group">
            <button class="button" @click="isupdate = true">
                <i class="fas fa-user-edit"></i> 修改信息
            </button>
            <el-badge :value="adoptnews" class="item">
                <button class="button" @click="readmyadopt">
                    <i class="fas fa-paw"></i> 我的领养
                </button>
            </el-badge>
            <el-badge :value="donatenews" class="item">
                <button class="button" @click="readmydonate">
                    <i class="fas fa-life-ring"></i> 我的救助
                </button>
            </el-badge>
            <button class="button" @click="mypost">
                <i class="fas fa-sticky-note"></i> 我的帖子
            </button>
            <el-badge :value="messagenews" class="item">
                <button class="button" @click="readmymessage">
                    <i class="fas fa-envelope"></i> 咨询反馈
                </button>
            </el-badge>
            <button class="button" @click="mypetdia">
                <i class="fas fa-envelope"></i> 我的猫狗
            </button>
        </div>
    </div>
    <!-- 我的领养记录 -->
    <el-dialog v-model="showmyadopt" title="领养列表" @close="removeadoptbadge">
        <el-scrollbar height="420px">
            <div v-for="adopt in  adoptlist " :key="adopt.id">
                {{ `宠物名称:${adopt.petname}` }}
                {{ ` 您申请的原因:${adopt.reason} ` }}
                <div :class="isred(adopt.isRead)">{{ `目前状态:${adopt.state == 2 ? '审核中' : adopt.state ? '已通过申请' : '申请失败'}
                                    通知:${adopt.ponReason} ` }}</div>
                <br>
                <hr>
            </div>
            <!-- <div class="adoption-record" v-for="adopt in  adoptlist " :key="adopt.id">
                <div class="pet-avatar">
                    <img :src="getimgurl(adopt.pet.photo)" alt="宠物头像">
                </div>
                <div class="record-details">
                    <div class="pet-name">{{ adopt.petname }}</div>
                    <div class="application-reason">{{ adopt.reason }}</div>
                    <div class="user-address">{{ adopt.address }}</div>
                    <div class="application-time">{{ adopt.applyTime }}</div>
                    <div class="status">{{ adopt.status }}</div>
                    <div class="feedback">{{ adopt.ponReason }}</div>
                </div>
            </div> -->
        </el-scrollbar>
    </el-dialog>
    <!-- 我的贡献记录 -->
    <el-dialog v-model="showmydonate" @close="removedonatebadge">
        <el-scrollbar height="420px">
            <div v-for=" donate  in  mydonates " :key="donate.id">
                救助宠物:{{ donate.pet.petname }}<br>
                捐助品:<div v-for=" food  in  donate.donateList " :key="food.id">
                    <div v-if="food.tempCount != 0">
                        {{ food.foodname }} 个数:{{ food.tempCount }} ,共{{ food.price * food.tempCount }}宠宠币
                    </div>
                </div>
                <span style="float: right;">总价:{{ donate.totalPrice }}宠宠币</span><br>
                <span :class="isred(donate.isRead)" style="float: right;">目前状态:{{ donate.state ? '已完成分配' : '未完成分配' }}</span>
                <br>
                <hr>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 我的帖子 -->
    <el-dialog v-model="showmypost">
        <el-scrollbar height="420px">
            <div v-for=" post  in  myposts " :key="post.id">
                <h4>标题:<RouterLink :to="`/postdetail/${post.id}`">{{ post.title }}</RouterLink>
                </h4> 评论数:{{ post.commentCount }}
                <el-link type="danger" @click="delelemypost(post.id)">删除</el-link>
                <hr>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 消息对话 -->
    <el-dialog v-model="showmymessage" @close="removemessagebadge" @open="gun" style="border-radius: 20px; width: 650px;">
        <div id="messagesroll" class="el-dialog-div">
            <div v-for=" message  in  messagelist " :key="message.id">

                <div v-if="message.content != null" class="admin-message-container">
                    <div class="message-sender">管理员:</div>
                    <div class="message-content">{{ message.content }}</div>
                    <div class="message-info">
                        <div class="message-time">{{ message.createTime }}</div>
                    </div>
                </div>

                <div v-else class="user-message-container">
                    <div class="message-sender">用户:</div>
                    <div class="message-content">{{ message.userReply }}</div>
                    <div class="message-info">
                        <div class="message-time">{{ message.createTime }}</div>
                    </div>
                </div>

            </div>
        </div>

        <template #footer>
            <el-input v-model="sendinfo.userReply" type="textarea" :rows="3" style="margin-bottom: 20px;"
                placeholder="向管理员发送消息" />
            <span>
                <el-button @click="showmymessage = false">关闭</el-button>
                <el-button type="primary" @click="sendmessage">发送</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 我的猫狗信息弹层 -->
    <el-dialog v-model="showmypet">
        <el-scrollbar height="400px">
            <div v-for=" pet  in  mypetslist " :key="pet.id">
                <router-link :to="`/petdetail/${pet.id}`">
                    <el-avatar :size="50" :src="getimgurl(pet.photo)" />
                </router-link>
                {{ pet.petname }}
                <span v-if="pet.type == 1" style="float: right;">我的上传</span>
                <span v-else style="float: right;">我的领养</span>
                <router-link :to="`/diary/${pet.id}`"><el-link type="primary">日记动态</el-link></router-link>
                <hr>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 修改个人信息弹窗 -->
    <el-dialog v-model="isupdate" title="修改个人信息">
        <el-form label-position="left" label-width="100px" :model="currentUser" style="max-width: 460px">
            <el-form-item label="用户名:">
                <el-input v-model="currentUser.username" />
            </el-form-item>
            <el-form-item label="昵称:">
                <el-input v-model="currentUser.nickname" />
            </el-form-item>
            <el-form-item label="邮箱:">
                <el-input v-model="currentUser.email" />
            </el-form-item>
            <el-form-item label="手机号:">
                <el-input v-model="currentUser.phone" />
            </el-form-item>
            <el-form-item label="头像:">
                <el-upload ref="updateRef" class="avatar-uploader" action="http://localhost/upload" :show-file-list="false"
                    :on-success="updateAvatarSuccess" :on-change="changeAvatar" :auto-upload="false"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <img v-else :src="`http://localhost/img/${currentUser.photo}`" class="avatar" />
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="clearupdate">取消</el-button>
                <el-button type="primary" @click="updateconfirm">修改</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ref, reactive, inject } from 'vue';

//#region 修改信息
const isupdate = ref(false)
const updateRef = ref(null)
const imageUrl = ref()

// 后端修改
function updateuser() {
    axios.patch('/user', currentUser).then((res) => {
        if (res.data.code == 200) {
            //修改成功后更新当前session
            sessionStorage.setItem("user", JSON.stringify(currentUser))
            ElMessage.success(res.data.msg)
        } else ElMessage.error(res.data.msg)
    }).catch(() => ElMessage.error('服务器故障！！'))
        .finally(() => clearupdate())
}
// 清空取消
function clearupdate() {
    isupdate.value = false
    imageUrl.value = null
}
// 头像上传成功更新信息
function updateAvatarSuccess(response) {
    currentUser.photo = response
    updateuser()
}
// 头像展示
function changeAvatar(uploadFile) {
    imageUrl.value = URL.createObjectURL(uploadFile.raw)
}
// 头像检测
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
// 修改信息确认
function updateconfirm() {
    // 执行上传图片函数
    if (!imageUrl.value) {
        // 不执行上传图片
        updateuser()
    } else {
        updateRef.value.submit()
    }
}
//#endregion

//#region 我的领养
const showmyadopt = ref(false)
const adoptlist = ref([])
function myadopt(uid) {
    axios.get(`/user/myadopt/${uid}`)
        .then(res => {
            if (res.data.code == 200) {
                adoptlist.value = res.data.data
            } else ElMessage.warning(res.data.msg)
        })
}
//领养列表展示
function readmyadopt() {
    myadopt(currentUser.id)
    showmyadopt.value = true

}

//#endregion

//#region newmessage已读
//领养通知
const adoptnews = inject('adoptnews')
function removeadoptbadge() {
    axios.patch(`/adopt/${currentUser.id}`)
    adoptnews.value = undefined
}
// 捐献通知
const donatenews = inject('donatenews')
function removedonatebadge() {
    axios.patch(`/donate/${currentUser.id}`)
    donatenews.value = undefined
}
//新消息提醒
const messagenews = inject('messagenews')
function removemessagebadge() {
    //修改已读
    axios.patch(`/message/${currentUser.id}/user`)
    messagenews.value = undefined
}

//#endregion

//#region 我的捐助
const showmydonate = ref(false)
const mydonates = ref([])
function mydonate(uid) {
    axios.get(`/donate/${uid}`)
        .then(res => {
            mydonates.value = res.data.data
            for (let i = 0; i < mydonates.value.length; i++) {
                mydonates.value[i].donateList = JSON.parse(mydonates.value[i].donateList)
            }
        })
}
function readmydonate() {
    mydonate(currentUser.id)
    showmydonate.value = true
}

//#endregion

//#region 我的帖子
const myposts = ref([])
const showmypost = ref(false)
function mypost() {
    axios.get(`/post/my/${currentUser.id}`)
        .then(res => {
            myposts.value = res.data.data
            showmypost.value = true
        })
}
function delelemypost(id) {
    ElMessageBox.confirm('确定删除该帖子吗？', '删除帖子', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        axios.delete(`/post/${id}`)
            .then(res => {
                console.log(res)
                mypost()
            })
            .catch(err => {
                console.error(err);
            })
        ElMessage.success('删除成功！')
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
//#endregion

//#region 我的流浪猫狗
const showmypet = ref(false)
function mypetdia() {
    getmypets()
    showmypet.value = true
}
const mypetslist = ref([])
function getmypets() {
    axios.get(`/pet/mypets/${currentUser.id}`)
        .then(res => {
            console.log(res)
            mypetslist.value = res.data.data
        })
        .catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 对话消息展示
const showmymessage = ref(false)
const messagelist = ref([])
function mymessage(uid) {
    axios.get(`/message/${uid}`)
        .then(res => {
            messagelist.value = res.data.data
        })
        .catch(err => {
            console.error(err);
        })
}
function readmymessage() {
    mymessage(currentUser.id)
    showmymessage.value = true
}
//#endregion

//#region 发送消息函数
const sendinfo = reactive({
    uid: null,
    userReply: null,
    isRead: 1
})
function sendmessage() {
    sendinfo.uid = currentUser.id
    axios.post('/message', sendinfo)
        .then(res => {
            if (res.data.code == 200) {
                ElMessage.success(res.data.msg)
                readmymessage()
                gun()
            } else ElMessage.error(res.data.msg)

        })
        .catch(err => {
            console.error(err);
        }).finally(() => sendinfo.userReply = null)
}
//#endregion


// 滚动条沉底函数
function gun() {
    setTimeout(() => {
        var messagesroll = document.getElementById("messagesroll")
        messagesroll.scrollTop = messagesroll.scrollHeight
    }, 80)
}
// 新通知显红函数
function isred(is) {
    let color = ''
    if (is) color = 'red'
    return color
}

const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
function getimgurl(url) { return new URL(`img/${url}`, 'http://localhost/') }
</script> 

<style scoped>
.adoption-record {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}

.pet-avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 20px;
}

.pet-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.record-details {
    flex: 1;
}

.pet-name {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 5px;
}

.application-reason {
    font-size: 16px;
    margin-bottom: 5px;
}

.user-address {
    font-size: 14px;
    margin-bottom: 5px;
}

.application-time {
    font-size: 14px;
    color: gray;
    margin-bottom: 5px;
}

.status {
    font-size: 16px;
    margin-bottom: 5px;
}

.feedback {
    font-size: 14px;
    margin-bottom: 5px;
}






.red {
    color: red;
    font-size: large;
    font-weight: 600;
}

.personal-center {
    max-width: 650px;
    margin: 25px auto 105px;

    padding: 20px;
    background-color: #f2f2f2;
    border-radius: 5px;
    animation: fadeIn 0.5s ease-in;

}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

.title {
    text-align: center;
    margin-bottom: 20px;
}

.user-info {
    margin-bottom: 20px;
}

.info-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.label {
    font-weight: bold;
    margin-right: 5px;
}

.value {
    color: #777;
}

.button-group {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    margin-top: 20px;
}

.button {
    display: flex;
    align-items: center;
    margin: 10px;
    padding: 10px 12px;
    background-color: #3498db;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.button:hover {
    background-color: #217dbb;
}


.user-message-container {
    text-align: right;
    margin-bottom: 20px;
    margin-right: 20px;
}

.admin-message-container {
    text-align: left;
    margin-bottom: 20px;
}

.message-content {
    padding: 10px;
    border-radius: 5px;
    margin-bottom: 5px;
    color: #333;
    display: inline-block;
}

.message-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.user-message-container .message-info {
    justify-content: flex-end;
}

.message-time {
    color: #999;
    font-size: 12px;
}

.message-sender {
    font-weight: bold;
    margin-right: 10px;
    color: #555;
}

.user-message-container .message-sender {
    color: #3498db;
}

.admin-message-container .message-sender {
    color: #e67e22;
}

.user-message-container .message-content {
    background: #cbe1f0;
}

.admin-message-container .message-content {
    background: rgb(241, 241, 216);
}

.el-dialog-div {
    height: 40vh;
    overflow: auto;
}

.avatar-uploader .avatar {
    width: 120px;
    height: 140px;
    display: block;
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
</style>
