<template>
    <div class="page-container fade-up" v-if="currentUser != null">
        <!-- ===== 资料横幅：品牌渐变底 ===== -->
        <div class="profile-banner">
            <div class="banner-bg"></div>
            <div class="banner-inner">
                <div class="avatar-wrap">
                    <el-avatar :size="92" :src="resolveImg(currentUser.photo)" class="profile-avatar" />
                </div>
                <div class="profile-main">
                    <h1 class="profile-name">{{ currentUser.nickname || currentUser.username }}</h1>
                    <div class="profile-tags">
                        <span class="tag">用户名：{{ currentUser.username }}</span>
                        <span class="tag" v-if="currentUser.email">📧 {{ currentUser.email }}</span>
                        <span class="tag" v-if="currentUser.phone">📱 {{ currentUser.phone }}</span>
                    </div>
                </div>
                <div class="profile-stats">
                    <div class="stat">
                        <div class="stat-value coin">{{ currentUser.petCoin }}</div>
                        <div class="stat-label">宠宠币</div>
                    </div>
                    <div class="stat">
                        <div class="stat-value">
                            <el-switch v-model="currentUser.comPermiss" disabled :active-value="1" :inactive-value="0"
                                style="--el-switch-on-color: #2fbf71; --el-switch-off-color: #ff4949" />
                        </div>
                        <div class="stat-label">社区权限</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== 功能宫格 ===== -->
        <div class="feature-grid">
            <button class="feature-card" @click="isupdate = true">
                <span class="feature-icon">✏️</span>
                <span class="feature-name">修改信息</span>
                <span class="feature-desc">资料与头像</span>
            </button>
            <el-badge :value="adoptnews" class="feature-badge">
                <button class="feature-card" @click="readmyadopt">
                    <span class="feature-icon">🐾</span>
                    <span class="feature-name">我的领养</span>
                    <span class="feature-desc">申请进度</span>
                </button>
            </el-badge>
            <el-badge :value="donatenews" class="feature-badge">
                <button class="feature-card" @click="readmydonate">
                    <span class="feature-icon">💝</span>
                    <span class="feature-name">我的救助</span>
                    <span class="feature-desc">捐助记录</span>
                </button>
            </el-badge>
            <button class="feature-card" @click="mypost">
                <span class="feature-icon">📝</span>
                <span class="feature-name">我的帖子</span>
                <span class="feature-desc">发帖管理</span>
            </button>
            <el-badge :value="messagenews" class="feature-badge">
                <button class="feature-card" @click="readmymessage">
                    <span class="feature-icon">✉️</span>
                    <span class="feature-name">咨询反馈</span>
                    <span class="feature-desc">联系管理员</span>
                </button>
            </el-badge>
            <button class="feature-card" @click="openWallet">
                <span class="feature-icon">💰</span>
                <span class="feature-name">我的钱包</span>
                <span class="feature-desc">签到 / 充值</span>
            </button>
            <button class="feature-card" @click="openFavorites">
                <span class="feature-icon">⭐</span>
                <span class="feature-name">我的收藏</span>
                <span class="feature-desc">心仪的猫狗</span>
            </button>
            <button class="feature-card" @click="mypetdia">
                <span class="feature-icon">🐱</span>
                <span class="feature-name">我的猫狗</span>
                <span class="feature-desc">领养与上传</span>
            </button>
        </div>
    </div>
    <!-- 我的领养记录 -->
    <el-dialog v-model="showmyadopt" title="领养列表" @close="removeadoptbadge">
        <el-scrollbar height="420px">
            <div v-for="adopt in  adoptlist " :key="adopt.id" class="record-item">
                <div class="record-line">
                    <span class="record-key">宠物名称</span>{{ adopt.petname }}
                </div>
                <div class="record-line">
                    <span class="record-key">申请原因</span>{{ adopt.reason }}
                </div>
                <div class="record-line" :class="isred(adopt.isRead)">
                    <span class="record-key">目前状态</span>
                    <el-tag :type="adopt.state == 2 ? 'warning' : adopt.state ? 'success' : 'danger'" size="small" round>
                        {{ adopt.state == 2 ? '审核中' : adopt.state ? '已通过申请' : '申请失败' }}
                    </el-tag>
                    <el-button v-if="adopt.state == 2" size="small" type="danger" plain round
                        :loading="cancelAdoptId === adopt.id" @click="cancelAdopt(adopt.id)"
                        style="margin-left: 10px;">取消申请</el-button>
                </div>
                <div class="record-line" :class="isred(adopt.isRead)">
                    <span class="record-key">通知</span>{{ adopt.ponReason }}
                </div>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 我的贡献记录 -->
    <el-dialog v-model="showmydonate" title="我的救助记录" @close="removedonatebadge">
        <el-scrollbar height="420px">
            <div v-for=" donate  in  mydonates " :key="donate.id" class="record-item">
                <div class="record-line"><span class="record-key">救助宠物</span>{{ donate.pet.petname }}</div>
                <div class="record-line">
                    <span class="record-key">捐助品</span>
                    <span>
                        <template v-for=" food  in  donate.donateList " :key="food.id">
                            <template v-if="food.tempCount != 0">
                                {{ food.foodname }} × {{ food.tempCount }}（{{ food.price * food.tempCount }}币）
                                <br>
                            </template>
                        </template>
                    </span>
                </div>
                <div class="record-line">
                    <span class="record-key">总价</span>{{ donate.totalPrice }} 宠宠币
                </div>
                <div class="record-line" :class="isred(donate.isRead)">
                    <span class="record-key">目前状态</span>
                    <el-tag :type="donate.state ? 'success' : 'warning'" size="small" round>
                        {{ donate.state ? '已完成分配' : '未完成分配' }}
                    </el-tag>
                </div>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 我的帖子 -->
    <el-dialog v-model="showmypost" title="我的帖子">
        <el-scrollbar height="420px">
            <div v-for=" post  in  myposts " :key="post.id" class="record-item post-row">
                <div class="record-line">
                    <RouterLink :to="`/postdetail/${post.id}`" class="post-title-link">{{ post.title }}</RouterLink>
                    <span class="post-comment-count">评论 {{ post.commentCount }}</span>
                    <el-link type="danger" @click="delelemypost(post.id)">删除</el-link>
                </div>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 消息对话 -->
    <el-dialog v-model="showmymessage" title="咨询反馈" @close="removemessagebadge" @open="gun"
        style="border-radius: 20px; width: 650px;">
        <div id="messagesroll" class="el-dialog-div chat-area">
            <div v-if="messagelist.length === 0" class="empty-state">还没有对话，向管理员发送第一条消息吧～</div>
            <div v-for=" message  in  messagelist " :key="message.id">
                <!-- 管理员消息（左侧） -->
                <div v-if="message.content != null" class="chat-row">
                    <div class="chat-avatar admin-avatar">管</div>
                    <div class="chat-bubble">
                        <div class="chat-sender">管理员</div>
                        <div class="chat-content">{{ message.content }}</div>
                        <div class="chat-time">{{ message.createTime }}</div>
                    </div>
                </div>
                <!-- 我的消息（右侧） -->
                <div v-else class="chat-row mine">
                    <div class="chat-bubble mine">
                        <div class="chat-content">{{ message.userReply }}</div>
                        <div class="chat-time mine">{{ message.createTime }}</div>
                    </div>
                    <el-avatar :size="38" :src="resolveImg(currentUser.photo)" class="chat-avatar" />
                </div>
            </div>
        </div>

        <template #footer>
            <el-input v-model="sendinfo.userReply" type="textarea" :rows="3" style="margin-bottom: 20px;"
                placeholder="向管理员发送消息" @keyup.ctrl.enter="sendmessage" />
            <span>
                <el-button @click="showmymessage = false">关闭</el-button>
                <el-button type="primary" :loading="messageSending" @click="sendmessage">发送</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 我的猫狗信息弹层 -->
    <el-dialog v-model="showmypet" title="我的猫狗">
        <el-scrollbar height="400px">
            <div v-for=" pet  in  mypetslist " :key="pet.id" class="record-item pet-row">
                <router-link :to="`/petdetail/${pet.id}`">
                    <el-avatar :size="50" :src="resolveImg(pet.photo)" />
                </router-link>
                <span class="pet-row-name">{{ pet.petname }}</span>
                <el-tag v-if="pet.type == 1" size="small" type="info" round>我的上传</el-tag>
                <el-tag v-else size="small" type="success" round>我的领养</el-tag>
                <router-link :to="`/diary/${pet.id}`" class="diary-link">日记动态</router-link>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 我的钱包 -->
    <el-dialog v-model="showWallet" title="我的钱包" width="520px" style="border-radius: 18px;">
        <div class="wallet-overview">
            <div class="wallet-balance">
                <div class="wallet-num">{{ wallet.petCoin ?? currentUser.petCoin }}</div>
                <div class="wallet-label">当前宠宠币</div>
            </div>
            <el-button v-if="!wallet.signedToday" type="primary" round :loading="signing" @click="sign">
                每日签到 +{{ wallet.signCoin || 10 }}
            </el-button>
            <el-button v-else round disabled>今日已签到 ✓</el-button>
        </div>
        <el-divider content-position="left">充值（1 元 = 10 宠宠币 · 演示支付）</el-divider>
        <div class="recharge-grid">
            <button v-for="amt in [6, 12, 30, 68, 128]" :key="amt" class="recharge-item"
                :class="{ active: rechargeAmount === amt }" @click="rechargeAmount = amt">
                <div class="recharge-coin">{{ amt * 10 }} 币</div>
                <div class="recharge-amt">¥{{ amt }}</div>
            </button>
        </div>
        <el-button type="primary" class="recharge-btn" :loading="recharging" :disabled="!rechargeAmount"
            @click="recharge">立即充值</el-button>
        <el-divider content-position="left">充值记录</el-divider>
        <el-scrollbar height="180px">
            <div v-if="recharges.length === 0" class="empty-state">暂无充值记录</div>
            <div v-for="r in recharges" :key="r.id" class="record-item recharge-row">
                <span>+{{ r.coin }} 宠宠币</span>
                <span class="recharge-row-amt">¥{{ r.amount }}</span>
                <span class="recharge-row-time">{{ r.createTime }}</span>
            </div>
        </el-scrollbar>
    </el-dialog>
    <!-- 我的收藏 -->
    <el-dialog v-model="showFavorites" title="我的收藏" width="560px" style="border-radius: 18px;">
        <el-scrollbar height="420px">
            <div v-if="favorites.length === 0" class="empty-state">还没有收藏，去流浪之家看看吧～</div>
            <div v-for="fav in favorites" :key="fav.id" class="record-item pet-row">
                <router-link :to="`/petdetail/${fav.pid}`" @click="showFavorites = false">
                    <el-avatar :size="50" :src="resolveImg(fav.pet.photo)" />
                </router-link>
                <span class="pet-row-name">{{ fav.pet.petname }}</span>
                <el-tag v-if="fav.pet.isAdopt == 1" type="success" size="small" round>已领养</el-tag>
                <el-tag v-else-if="fav.pet.isAdopt == 0" type="warning" size="small" round>流浪中</el-tag>
                <el-tag v-else size="small" round>审核中</el-tag>
                <el-button size="small" text type="danger" :loading="unfavId === fav.pid"
                    @click="toggleFavorite(fav.pid)" style="margin-left: auto;">取消收藏</el-button>
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
                <el-upload ref="updateRef" class="avatar-uploader" :action="uploadUrl" :headers="uploadHeaders()"
                    :on-error="uploadError" :show-file-list="false" :on-success="updateAvatarSuccess" :on-change="changeAvatar"
                    :auto-upload="false" :before-upload="beforeAvatarUpload">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <img v-else :src="resolveImg(currentUser.photo)" class="avatar" />
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
import { resolveImg, resolveUploadUrl, uploadHeaders, uploadError } from '@/utils/url';

const uploadUrl = resolveUploadUrl()

//#region 修改信息
const isupdate = ref(false)
const updateRef = ref(null)
const imageUrl = ref()

// 后端修改
function updateuser() {
    // 只提交允许修改的字段。
    // 改造前是把整个 currentUser（含 comPermiss、petCoin）回传，后端 updateById 无条件接受，
    // 用户可以自行提权、改积分；现在这些字段由服务端保护，无法经本接口变更。
    axios.patch('/user', {
        username: currentUser.username,
        nickname: currentUser.nickname,
        email: currentUser.email,
        phone: currentUser.phone,
        photo: currentUser.photo
    }).then((res) => {
        if (res.data.code == 200) {
            // 用服务端返回的权威数据回填本地状态与会话缓存
            Object.assign(currentUser, res.data.data)
            sessionStorage.setItem('user', JSON.stringify(currentUser))
            ElMessage.success(res.data.msg)
        } else {
            ElMessage.error(res.data.msg)
        }
    }).finally(() => clearupdate())
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
                mypost()
            })
            .catch(err => {
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
            mypetslist.value = res.data.data
        })
        .catch(err => {
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
        })
}
function readmymessage() {
    mymessage(currentUser.id)
    showmymessage.value = true
}
//#endregion

//#region 发送消息函数（messageSending 防重复发送）
const messageSending = ref(false)
const sendinfo = reactive({
    uid: null,
    userReply: null,
    isRead: 1
})
function sendmessage() {
    if (!sendinfo.userReply || !sendinfo.userReply.trim()) {
        ElMessage.warning('消息内容不能为空')
        return
    }
    if (messageSending.value) return
    messageSending.value = true
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
        }).finally(() => {
            messageSending.value = false
            sendinfo.userReply = null
        })
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

//#region 我的钱包（签到 + 充值）
const showWallet = ref(false)
const wallet = ref({})
const recharges = ref([])
const signing = ref(false)
const recharging = ref(false)
const rechargeAmount = ref(null)

function openWallet() {
    showWallet.value = true
    rechargeAmount.value = null
    loadWallet()
}

function loadWallet() {
    axios.get('/wallet/overview').then(res => {
        if (res.data.code === 200) {
            wallet.value = res.data.data
            currentUser.petCoin = res.data.data.petCoin
            sessionStorage.setItem('user', JSON.stringify(currentUser))
        }
    })
    axios.get('/wallet/recharges').then(res => {
        if (res.data.code === 200) recharges.value = res.data.data
        else recharges.value = []
    })
}

function sign() {
    if (signing.value) return
    signing.value = true
    axios.post('/wallet/sign').then(res => {
        if (res.data.code === 200) {
            ElMessage.success(res.data.msg)
            loadWallet()
        } else {
            ElMessage.warning(res.data.msg)
        }
    }).catch(() => {}).finally(() => signing.value = false)
}

function recharge() {
    if (!rechargeAmount.value || recharging.value) return
    ElMessageBox.confirm(`模拟支付 ¥${rechargeAmount.value}，将获得 ${rechargeAmount.value * 10} 宠宠币`, '充值确认', {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'info'
    }).then(() => {
        recharging.value = true
        axios.post('/wallet/recharge', { amount: rechargeAmount.value }).then(res => {
            if (res.data.code === 200) {
                ElMessage.success(res.data.msg)
                loadWallet()
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(() => {}).finally(() => recharging.value = false)
    }).catch(() => {})
}
//#endregion

//#region 我的收藏
const showFavorites = ref(false)
const favorites = ref([])
const unfavId = ref(null)

function openFavorites() {
    showFavorites.value = true
    loadFavorites()
}

function loadFavorites() {
    axios.get('/favorite/my').then(res => {
        if (res.data.code === 200) favorites.value = res.data.data
        else favorites.value = []
    })
}

function toggleFavorite(pid) {
    if (unfavId.value) return
    unfavId.value = pid
    axios.post(`/favorite/${pid}`).then(res => {
        if (res.data.code === 200) {
            ElMessage.success(res.data.msg)
            loadFavorites()
        }
    }).catch(() => {}).finally(() => unfavId.value = null)
}
//#endregion

//#region 取消领养申请
const cancelAdoptId = ref(null)
function cancelAdopt(id) {
    ElMessageBox.confirm('确定取消该领养申请吗？', '取消申请', {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
    }).then(() => {
        cancelAdoptId.value = id
        axios.delete(`/adopt/cancel/${id}`).then(res => {
            if (res.data.code === 200) {
                ElMessage.success(res.data.msg)
                myadopt(currentUser.id)
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(() => {}).finally(() => cancelAdoptId.value = null)
    }).catch(() => {})
}
//#endregion

</script>

<style scoped>
/* ===== 资料横幅：品牌渐变 ===== */
.profile-banner {
    position: relative;
    border-radius: var(--radius-lg);
    overflow: hidden;
    margin-bottom: 24px;
    box-shadow: var(--shadow);
}

.banner-bg {
    position: absolute;
    inset: 0;
    background:
        radial-gradient(500px 220px at 85% 0%, rgba(255, 255, 255, .22) 0%, transparent 60%),
        var(--brand-gradient);
}

.banner-inner {
    position: relative;
    display: flex;
    align-items: center;
    gap: 22px;
    padding: 34px 36px;
    flex-wrap: wrap;
}

.avatar-wrap {
    flex-shrink: 0;
}

.profile-avatar {
    border: 3px solid rgba(255, 255, 255, .75);
    box-shadow: 0 6px 18px rgba(0, 0, 0, .18);
    background: #fff;
    overflow: hidden;
}

/* 头像图片必须裁剪进圆形（el-avatar 默认 cover，这里兜底防占位图溢出） */
.profile-avatar :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.profile-main {
    flex: 1;
    min-width: 200px;
}

.profile-name {
    margin: 0 0 10px;
    font-size: 26px;
    color: #fff;
    letter-spacing: .5px;
}

.profile-tags {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}

.profile-tags .tag {
    font-size: 13px;
    color: #fff;
    background: rgba(255, 255, 255, .18);
    border: 1px solid rgba(255, 255, 255, .28);
    padding: 4px 12px;
    border-radius: 999px;
    backdrop-filter: blur(4px);
}

.profile-stats {
    display: flex;
    gap: 28px;
}

.stat {
    text-align: center;
    color: #fff;
}

.stat-value {
    font-size: 26px;
    font-weight: 700;
    line-height: 1.2;
}

.stat-label {
    font-size: 12px;
    opacity: .85;
    margin-top: 4px;
}

/* ===== 功能宫格 ===== */
.feature-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
    gap: 16px;
}

.feature-badge {
    display: block;
}

/* 角标收进卡片内侧，避免悬出卡片右缘 */
.feature-badge :deep(.el-badge__content) {
    right: 18px;
    top: 12px;
    border: none;
    box-shadow: 0 2px 8px rgba(255, 77, 79, .35);
}

/* el-badge 根元素撑满 grid 格，保证带角标的卡与普通卡同宽 */
.feature-badge {
    width: 100%;
}

.feature-card {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 24px 12px 20px;
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    box-shadow: var(--shadow-sm);
    cursor: pointer;
    transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
    font-family: var(--font);
}

.feature-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow);
    border-color: var(--brand-200);
}

.feature-icon {
    font-size: 30px;
    line-height: 1;
    margin-bottom: 6px;
}

.feature-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--ink-1);
}

.feature-desc {
    font-size: 12px;
    color: var(--ink-3);
}

/* ===== 弹层内的记录条目 ===== */
.record-item {
    padding: 12px 4px;
    border-bottom: 1px dashed var(--line);
}

.record-item:last-child {
    border-bottom: none;
}

.record-line {
    display: flex;
    align-items: baseline;
    gap: 8px;
    font-size: 14px;
    color: var(--ink-2);
    line-height: 1.9;
    flex-wrap: wrap;
}

.record-key {
    color: var(--ink-3);
    font-size: 13px;
    flex-shrink: 0;
}

.record-key::after {
    content: '：';
}

.post-row .record-line {
    align-items: center;
    justify-content: space-between;
}

.post-title-link {
    color: var(--ink-1);
    font-weight: 600;
    text-decoration: none;
}

.post-title-link:hover {
    color: var(--brand);
}

.post-comment-count {
    font-size: 12px;
    color: var(--ink-3);
}

.pet-row {
    display: flex;
    align-items: center;
    gap: 14px;
}

.pet-row-name {
    font-weight: 600;
    color: var(--ink-1);
}

.diary-link {
    margin-left: auto;
    color: var(--brand-600);
    font-size: 13px;
    text-decoration: none;
}

.diary-link:hover {
    color: var(--brand-700);
}

.red {
    color: red;
    font-weight: 600;
}

/* ===== 消息气泡（聊天界面） ===== */
.chat-area {
    background: #f7f8fb;
    border-radius: var(--radius);
    padding: 16px 14px;
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
    background: var(--green-100);
    color: var(--green);
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
    background: var(--brand-gradient);
    color: #fff;
    border-radius: 16px 4px 16px 16px;
    box-shadow: 0 4px 14px rgba(255, 107, 53, .28);
}

.chat-sender {
    font-size: 12px;
    font-weight: 600;
    color: var(--green);
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

.el-dialog-div {
    height: 40vh;
    overflow: auto;
}

/* ===== 钱包 ===== */
.wallet-overview {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px 20px;
    border-radius: var(--radius);
    background: var(--brand-gradient-soft);
    border: 1px solid var(--brand-200);
    margin-bottom: 8px;
}

.wallet-num {
    font-size: 32px;
    font-weight: 700;
    color: var(--brand-600);
    line-height: 1.1;
}

.wallet-label {
    font-size: 12px;
    color: var(--ink-3);
    margin-top: 4px;
}

.recharge-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
    margin-bottom: 14px;
}

.recharge-item {
    border: 1px solid var(--line);
    border-radius: var(--radius-sm);
    background: #fff;
    padding: 10px 4px;
    cursor: pointer;
    text-align: center;
    transition: border-color .2s ease, background-color .2s ease;
    font-family: var(--font);
}

.recharge-item:hover {
    border-color: var(--brand-300);
}

.recharge-item.active {
    border-color: var(--brand);
    background: var(--brand-100);
}

.recharge-coin {
    font-size: 14px;
    font-weight: 700;
    color: var(--brand-600);
}

.recharge-amt {
    font-size: 12px;
    color: var(--ink-3);
    margin-top: 2px;
}

.recharge-btn {
    width: 100%;
}

.recharge-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
    color: var(--ink-1);
}

.recharge-row-amt {
    color: var(--ink-3);
    font-size: 13px;
}

.recharge-row-time {
    margin-left: auto;
    color: var(--ink-3);
    font-size: 12px;
}

.avatar-uploader .avatar {
    width: 120px;
    height: 140px;
    display: block;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
    .banner-inner {
        padding: 24px 20px;
    }

    .profile-stats {
        width: 100%;
        justify-content: space-around;
    }
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
