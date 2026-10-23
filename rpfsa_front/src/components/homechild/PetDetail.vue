<template>
    <div class="page-container fade-up">
        <!-- 平台流浪管理 -->
        <div class="pet-hero soft-card" v-if="currentpet.type == 0">
            <div class="pet-photo">
                <el-carousel v-if="photoList.length > 1" height="420px" class="pet-carousel">
                    <el-carousel-item v-for="(img, i) in photoList" :key="i">
                        <img :src="resolveImg(img)" alt="Pet Image" class="carousel-img">
                    </el-carousel-item>
                </el-carousel>
                <img v-else-if="currentpet.photo != undefined" :src="resolveImg(currentpet.photo)" alt="Pet Image">
            </div>
            <div class="pet-info">
                <div class="pet-title-row">
                    <h1 class="pet-name">{{ currentpet.petname }}</h1>
                    <el-tag v-if="currentpet.isAdopt == 1" type="success" size="large" effect="light" round>已领养</el-tag>
                    <el-tag v-else-if="currentpet.isAdopt == 0" type="warning" size="large" effect="light" round>流浪中</el-tag>
                    <el-tag v-else size="large" effect="light" round>领养审核中</el-tag>
                    <button class="fav-btn" :class="{ favored: favored }" :disabled="favLoading" @click="toggleFav">
                        {{ favored ? '❤️ 已收藏' : '🤍 收藏' }}
                    </button>
                </div>
                <p class="pet-desc">{{ currentpet.describtion }}</p>
                <div class="pet-fields">
                    <div class="field"><span class="field-label">性别</span><span class="field-value">{{
                        sexText(currentpet.sex) }}</span></div>
                    <div class="field"><span class="field-label">年龄</span><span class="field-value">{{ currentpet.age
                            }}</span></div>
                    <div class="field">
                        <span class="field-label">疫苗接种</span>
                        <el-tag v-if="currentpet.isVaccine == 1" type="success" size="small" round>已接种</el-tag>
                        <el-tag v-else type="warning" size="small" round>未接种</el-tag>
                    </div>
                    <div class="field"><span class="field-label">添加时间</span><span class="field-value">{{
                        currentpet.createTime }}</span></div>
                    <div class="field"><span class="field-label">宠宠币</span><span class="field-value coin">{{
                        currentpet.petCoin }}</span></div>
                </div>
                <div class="action-buttons" v-if="currentpet.isAdopt == 0">
                    <button class="btn btn-rescue" :disabled="donateSubmitting" @click="salvage()">
                        {{ donateSubmitting ? '处理中...' : '💝 爱心救助' }}
                    </button>
                    <button class="btn btn-adopt" :disabled="adoptSubmitting" @click="adopt()">
                        {{ adoptSubmitting ? '处理中...' : '🏠 申请领养' }}
                    </button>
                </div>
                <router-link :to="`/diary/${currentpet.id}`" v-if="currentpet.isAdopt == 1" class="diary-link">
                    查看日记动态 →
                </router-link>
            </div>
        </div>
        <!-- 私人上传猫狗 -->
        <div class="pet-hero soft-card" v-else>
            <div class="pet-photo">
                <el-carousel v-if="photoList.length > 1" height="420px" class="pet-carousel">
                    <el-carousel-item v-for="(img, i) in photoList" :key="i">
                        <img :src="resolveImg(img)" alt="Pet Image" class="carousel-img">
                    </el-carousel-item>
                </el-carousel>
                <img v-else-if="currentpet.photo != undefined" :src="resolveImg(currentpet.photo)" alt="Pet Image">
            </div>
            <div class="pet-info">
                <div class="pet-title-row">
                    <h1 class="pet-name">{{ currentpet.petname }}</h1>
                    <el-tag type="info" size="large" effect="light" round>私人上传</el-tag>
                    <button class="fav-btn" :class="{ favored: favored }" :disabled="favLoading" @click="toggleFav">
                        {{ favored ? '❤️ 已收藏' : '🤍 收藏' }}
                    </button>
                </div>
                <el-popover placement="bottom" :title="`昵称:${currentpet.user.nickname}`" :width="220" trigger="click">
                    <template #default>
                        电话：{{ currentpet.user.phone }} <br>
                        邮箱：{{ currentpet.user.email }} <br>
                        领养个数：{{ currentpet.user.howAdopt }} <br>
                        宠宠币：{{ currentpet.user.petCoin }}
                    </template>
                    <template #reference>
                        <p class="owner-line">主人：<span class="owner-name">{{ currentpet.user.nickname }}</span></p>
                    </template>
                </el-popover>
                <p class="pet-desc">{{ currentpet.describtion }}</p>
                <div class="pet-fields">
                    <div class="field"><span class="field-label">性别</span><span class="field-value">{{
                        sexText(currentpet.sex) }}</span></div>
                    <div class="field"><span class="field-label">年龄</span><span class="field-value">{{ currentpet.age
                            }}</span></div>
                    <div class="field"><span class="field-label">品种</span><span class="field-value">{{
                        currentpet.variety }}</span></div>
                    <div class="field">
                        <span class="field-label">疫苗接种</span>
                        <el-tag v-if="currentpet.isVaccine == 1" type="success" size="small" round>已接种</el-tag>
                        <el-tag v-else type="warning" size="small" round>未接种</el-tag>
                    </div>
                    <div class="field"><span class="field-label">添加时间</span><span class="field-value">{{
                        currentpet.createTime }}</span></div>
                </div>
                <router-link :to="`/diary/${currentpet.id}`" class="diary-link">查看日记动态 →</router-link>
            </div>
        </div>
        <!-- 评论区 -->
        <div class="comment-section soft-card">
            <div class="section-head">
                <h3 class="section-title">评论区</h3>
            </div>
            <div class="comment-list">
                <div v-for="comment in currentpet.comments" :key="comment.id" class="comment">
                    <div class="comment-header">
                        <!-- 点头像 -->
                        <el-popover placement="bottom" :title="`昵称:${comment.user.nickname}`" :width="220"
                            trigger="click">
                            电话：{{ comment.user.phone }} <br>
                            邮箱：{{ comment.user.email }} <br>
                            领养个数：{{ comment.user.howAdopt }} <br>
                            宠宠币：{{ comment.user.petCoin }}
                            <template #reference>
                                <img v-if="comment.user.photo != undefined" class="avatar"
                                    :src="resolveImg(comment.user.photo)" alt="用户头像">
                            </template>
                        </el-popover>
                        <div class="comment-meta">
                            <span class="comment-author">{{ comment.user.nickname == null ? comment.user.username
                                : comment.user.nickname }}
                            </span>
                            <span class="comment-time">{{ comment.createTime }}</span>
                            <el-link type="primary" class="reply-link" @click="addlevelTwo(comment.id)">回复</el-link>
                        </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}
                        <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                            @confirm="commentdelete(comment.id, comment.level)">
                            <template #reference>
                                <span v-if="currentUser != null && comment.uid == currentUser.id"
                                    class="delete-link"><el-link type="danger">删除</el-link></span>
                            </template>
                        </el-popconfirm>
                    </div>
                    <!-- 子评论 -->
                    <div class="sub-comment" v-for="leveltwocon in comment.commentList" :key="leveltwocon.id">
                        <div class="sub-comment-head">
                            <el-popover placement="bottom" :title="`昵称:${leveltwocon.user.nickname}`" :width="220"
                                trigger="click">
                                电话：{{ leveltwocon.user.phone }} <br>
                                邮箱：{{ leveltwocon.user.email }} <br>
                                领养个数：{{ leveltwocon.user.howAdopt }} <br>
                                宠宠币：{{ leveltwocon.user.petCoin }}
                                <template #reference>
                                    <img v-if="leveltwocon.user.photo != undefined" class="avatarleveltwo"
                                        :src="resolveImg(leveltwocon.user.photo)" alt="用户头像">
                                </template>
                            </el-popover>
                            <span class="sub-author">{{ leveltwocon.user.nickname == null ?
                                leveltwocon.user.username
                                : leveltwocon.user.nickname }}
                            </span>
                            <span class="sub-time">{{ leveltwocon.createTime }}</span>
                        </div>
                        <div class="sub-content">{{ leveltwocon.content }}
                            <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认"
                                cancelButtonText="取消" @confirm="commentdelete(leveltwocon.id, leveltwocon.level)">
                                <template #reference>
                                    <span v-if="currentUser != null && leveltwocon.uid == currentUser.id"
                                        class="delete-link"><el-link type="danger">删除</el-link></span>
                                </template>
                            </el-popconfirm>
                        </div>
                    </div>
                </div>
                <div v-if="!currentpet.comments || currentpet.comments.length === 0" class="empty-state">
                    还没有评论，来抢沙发～
                </div>
            </div>
            <div class="comment-input">
                <input v-model="currcomment" type="text" placeholder="说点什么...." @keyup.enter="confirmcomment">
                <button @click="confirmcomment">发表</button>
            </div>
        </div>
    </div>
    <!-- 捐助弹层 -->
    <el-dialog title="捐助选择" v-model="dialogFormVisible" style="border-radius: 20px;" :show-close="false">
        <div class="container">
            <div class="rescue-items">
                <div class="rescue-item" v-for="food in foodlist" :key="food.id">
                    <div class="item-image">
                        <img :src="resolveImg(food.picture)" alt="rescue item image">
                    </div>
                    <div class="item-info">
                        <div class="item-name">{{ food.foodname }} 库存:{{ food.inventory }}
                            单价:{{ food.price }}</div>
                        <div class="item-quantity">
                            <el-input-number v-model="food.tempCount" :min="0" :max="food.inventory" size="small"
                                @change="computertotalprice" />
                        </div>
                    </div>
                </div>
            </div>
            <div class="total-price">
                <span class="label">总价：</span>
                <span class="price">{{ totalprice }} 元</span>
            </div>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="cancel()">取消</el-button>
                <el-button type="primary" @click="salvageconfirm()">
                    捐助
                </el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 领养弹层 -->
    <el-dialog title="领养信息" v-model="dialogAdoptVisible" style="width: 600px;">
        <el-form>
            <el-form-item label="领养原因:">
                <el-input type="textarea" v-model="reason" :rows="3" placeholder="你为什么想领养这只小可爱呢?" />
            </el-form-item>
            <el-form-item label="所在地区:">
                <elui-china-area-dht @change="onChange"></elui-china-area-dht>
            </el-form-item>
            <el-form-item label="详细地址:">
                <el-input type="text" v-model="adressdetail" style="width: 350px;" />
            </el-form-item>
            <el-form-item>
                <el-checkbox v-model="agreeProtocol">
                    我已阅读并同意
                    <el-link type="primary" @click="protocolVisible = true">《领养协议与回访承诺》</el-link>
                </el-checkbox>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogAdoptVisible = false">取消</el-button>
                <el-button type="primary" :disabled="!agreeProtocol" :loading="adoptSubmitting" @click="confirmadopt">
                    确定
                </el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 领养协议全文 -->
    <el-dialog v-model="protocolVisible" title="领养协议与回访承诺" width="560px" style="border-radius: 16px;">
        <div class="protocol-text">
            <p>1. 领养人须年满 18 周岁，有稳定的住所与收入，能为宠物提供安全、卫生的生活环境。</p>
            <p>2. 领养后不得以任何理由遗弃、虐待、转卖宠物；如确实无法继续饲养，请联系平台协助退回，不得私自处理。</p>
            <p>3. 领养成功后 7 天内，领养人须在平台发布至少一篇回访日记（含近期照片），30 天内配合平台回访。</p>
            <p>4. 请按时为宠物接种疫苗、驱虫，生病及时就医。</p>
            <p>5. 平台有权对违反协议的账号采取限制措施，包括但不限于限制领养权限、社区权限。</p>
        </div>
        <template #footer>
            <el-button type="primary" @click="protocolVisible = false">我已阅读</el-button>
        </template>
    </el-dialog>
</template>
<script setup>
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, defineProps, onMounted, ref, reactive } from 'vue';
import { EluiChinaAreaDht } from 'elui-china-area-dht'
import { resolveImg } from '@/utils/url';
// 地址选择器
const chinaData = new EluiChinaAreaDht.ChinaArea().chinaAreaflat

//#region 评论
const currcomment = ref('')
/**
 * 删除评论。
 * 二级评论走 /comment/{id}，一级评论走 /comment/t1/{id}（服务端会连带删除其子评论）。
 * 评论数已由后端在同一事务内维护，前端不再回写 commentCount，
 * 因而也消除了"读当前值再减 N"这种并发下必然漂移的写法。
 */
function commentdelete(commid, commlevel) {
    const url = commlevel ? `/comment/${commid}` : `/comment/t1/${commid}`
    axios.delete(url).then(res => {
        if (res.data.code == 200) {
            ElMessage.success(res.data.msg)
            getcurpet()
        } else {
            ElMessage.error(res.data.msg)
        }
    }).catch(() => { /* 网络异常已由全局拦截器统一提示 */ })
}
// 添加二级评论
function addlevelTwo(cid) {
    ElMessageBox.prompt('提示', {
        confirmButtonText: '评论',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '在此输入您的评论',
        type: 'info',
    }).then(({ value }) => {
        addcomment(value, cid)
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
// 添加一级评论
function confirmcomment() {
    addcomment(currcomment.value, 0)
}
//公用添加评论函数
function addcomment(commenttext, cid) {
    if (currentUser != null) {
        if (commenttext != '' && commenttext != null) {
            if (currentUser.comPermiss) {
                // 发表评论。uid 由后端从令牌取，不再由前端提交；
                // 评论数由 CommentService 在同事务内维护。
                axios.post('/comment', {
                    poid: 0,
                    pid: Number(pid),
                    content: commenttext,
                    level: cid
                }).then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success('评论成功😊')
                    } else {
                        ElMessage.error(res.data.msg)
                    }
                })
                    .catch(() => {
                        ElMessage.error('评论失败,服务器故障🤨')
                    }).finally(() => {
                        getcurpet()
                    })
            } else {
                ElMessage.warning('您的评论权限已被限制！')
            }
        } else ElMessage.warning('评论不能为空！')
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}

//#endregion

onMounted(() => {
    getcurpet()
    loadFavState()
})

//#region 获得当前流浪猫狗
const { pid } = defineProps(['pid'])
const currentpet = ref({
    user: {
        nickname: null
    }
})
function getcurpet() {
    axios.get(`/pet/${pid}`).then((res) => {
        if (res.data.code == 200) {
            currentpet.value = res.data.data
        } else {
            ElMessage.warning(res.data.msg)
        }
    })
}
//#endregion

const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))

// 性别数字 → 文案
function sexText(sex) {
    return sex == 1 ? '公' : sex == 0 ? '母' : '不详'
}

//#region 收藏
const favored = ref(false)
const favLoading = ref(false)
function loadFavState() {
    if (currentUser == null) return
    axios.get(`/favorite/check/${pid}`).then(res => {
        if (res.data.code === 200) favored.value = !!res.data.data
    }).catch(() => {})
}
function toggleFav() {
    if (currentUser == null) {
        ElMessage.warning('对不起你还未登录....请登录账户')
        return
    }
    if (favLoading.value) return
    favLoading.value = true
    axios.post(`/favorite/${pid}`).then(res => {
        if (res.data.code === 200) {
            favored.value = !!res.data.data
            ElMessage.success(res.data.msg)
        } else {
            ElMessage.warning(res.data.msg)
        }
    }).catch(() => {}).finally(() => favLoading.value = false)
}
//#endregion

//#region 相册轮播：主图 + 相册图
const photoList = computed(() => {
    const list = []
    if (currentpet.value.photo) list.push(currentpet.value.photo)
    if (Array.isArray(currentpet.value.photos)) {
        currentpet.value.photos.forEach(p => {
            if (p.url && p.url !== currentpet.value.photo) list.push(p.url)
        })
    }
    return list
})
//#endregion

//#region 救助

//总价计算
const totalprice = ref(0)
const donateSubmitting = ref(false)
const adoptSubmitting = ref(false)
function computertotalprice() {
    totalprice.value = 0
    for (let i = 0; i < foodlist.value.length; i++) {
        totalprice.value += foodlist.value[i].price * foodlist.value[i].tempCount
    }
}
// 救助品展示
const dialogFormVisible = ref(false)
const foodlist = ref([])
function salvage() {
    if (currentUser != null) {
        // 拿到foodlist用于展示
        axios.get('/food')
            .then(res => {
                foodlist.value = res.data.data
            })
            .catch(err => {
            })
        dialogFormVisible.value = true
        //发送宠宠币请求
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}
/**
 * 确认捐助。
 * <p>
 * 改造前这里串行发 4~8 个请求（改宠物币 → 改用户币 → 循环改每个救助品库存 → 写捐助记录），
 * 任何一步失败都会留下"扣了钱却没有记录"之类的脏数据；金额与余额判断也全在前端，可被篡改。
 * <p>
 * 现在只提交"救助哪只宠物、选了哪些救助品各几件"，服务端按库中单价重新计价，
 * 并在单个事务内完成扣款、扣库存、给宠物加币、写入记录。
 */
function salvageconfirm() {
    // 只取真正选中了数量的救助品
    const items = foodlist.value
        .filter(food => food.tempCount > 0)
        .map(food => ({ foodId: food.id, count: food.tempCount }))
    if (items.length === 0) {
        ElMessage.warning('对不起,不能捐助为空！！')
        return
    }
    // 前端预校验仅为即时反馈，真正的余额校验以服务端为准
    if (totalprice.value > currentUser.petCoin) {
        ElMessage.warning(`对不起,您目前的宠宠币为: ${currentUser.petCoin} ,请降低您的爱心.`)
        return
    }
    if (donateSubmitting.value) return
    ElMessageBox.confirm('确认要完成此次捐助吗?', '捐助确认', {
        cancelButtonText: '取消',
        confirmButtonText: '确认',
        type: 'warning'
    }).then(() => {
        donateSubmitting.value = true
        axios.post('/donate/commit', { petId: Number(pid), items }).then(res => {
            if (res.data.code == 200) {
                ElMessage.success(res.data.msg)
                // 余额已在服务端扣减，回读一次同步展示值与会话缓存
                axios.get(`/user/${currentUser.id}`).then(userRes => {
                    if (userRes.data.code == 200) {
                        Object.assign(currentUser, userRes.data.data)
                        sessionStorage.setItem('user', JSON.stringify(currentUser))
                    }
                })
            } else {
                ElMessage.error(res.data.msg)
            }
        }).finally(() => {
            donateSubmitting.value = false
            getcurpet()
            cancel()
        })
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
//取消清空
function cancel() {
    totalprice.value = 0
    dialogFormVisible.value = false
}
//#endregion

//#region 领养

// 地址拼接
const adressdetail = ref('')
const ad = ref('')
function onChange(e) {
    const one = chinaData[e[0]]
    const two = chinaData[e[1]]
    const three = chinaData[e[2]]
    ad.value = `${one.label}/${two.label}/${three.label}/`
}

// 领养弹层
const dialogAdoptVisible = ref(false)
function adopt() {
    if (currentUser != null) {
        if (currentUser.comPermiss) {
            dialogAdoptVisible.value = true
        } else {
            ElMessage.warning('由于违规你的权限已被限制！')
        }
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }

}

// 确认领养
const reason = ref('')
const agreeProtocol = ref(false)
const protocolVisible = ref(false)
function confirmadopt() {
    if (!agreeProtocol.value) {
        ElMessage.warning('请先阅读并同意领养协议')
        return
    }
    if (adoptSubmitting.value) return
    ad.value += adressdetail.value
    ElMessageBox.confirm('确认要领养该流浪动物吗?', '领养确认', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        adoptSubmitting.value = true
        // 申请记录与宠物状态流转（置为"审核中"）已在后端同一事务内完成，
        // 前端不再单独发 PATCH /pet；username / nickname / petname 也由服务端从库里取，
        // 前端无需（也无法）提交这几个冗余字段。
        axios.post('/adopt', {
            pid: Number(pid),
            reason: reason.value,
            adress: ad.value,
        }).then(res => {
            if (res.data.code == 200) {
                ElMessage.success(res.data.msg)
                getcurpet()
            } else {
                ElMessage.error(res.data.msg)
            }
        }).finally(() => {
            adoptSubmitting.value = false
            dialogAdoptVisible.value = false
        })
    }).catch(() => {
        ElMessage.info('已取消')
    })
}

//#endregion

</script>

<style scoped>
/* ===== 宠物主卡：左图右信息 ===== */
.pet-hero {
    display: grid;
    grid-template-columns: 420px 1fr;
    gap: 36px;
    padding: 32px;
    margin-bottom: 24px;
}

.pet-photo {
    border-radius: var(--radius-lg);
    overflow: hidden;
    background: var(--brand-gradient-soft);
    box-shadow: var(--shadow-sm);
}

.pet-photo img {
    width: 100%;
    height: 420px;
    object-fit: cover;
    display: block;
    transition: transform .5s ease;
}

.pet-photo:hover img {
    transform: scale(1.05);
}

/* 相册轮播 */
.pet-carousel {
    width: 100%;
    border-radius: var(--radius-lg);
}

.pet-carousel .carousel-img {
    width: 100%;
    height: 420px;
    object-fit: cover;
    display: block;
}

.pet-carousel :deep(.el-carousel__button) {
    width: 22px;
    height: 4px;
    border-radius: 4px;
}

/* 收藏按钮 */
.fav-btn {
    padding: 8px 18px;
    border-radius: 999px;
    border: 1px solid var(--brand-200);
    background: #fff;
    color: var(--ink-2);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all .2s ease;
    font-family: var(--font);
}

.fav-btn:hover {
    border-color: var(--brand);
    color: var(--brand);
}

.fav-btn.favored {
    background: var(--brand-100);
    border-color: var(--brand);
    color: var(--brand-600);
}

.fav-btn:disabled {
    opacity: .6;
    cursor: not-allowed;
}

.pet-title-row {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-wrap: wrap;
}

.pet-name {
    font-size: 30px;
    margin: 0;
    color: var(--ink-1);
    letter-spacing: .5px;
}

.pet-desc {
    margin: 14px 0 20px;
    color: var(--ink-2);
    font-size: 15px;
    line-height: 1.8;
}

.pet-fields {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px 20px;
    padding: 18px;
    background: #fafbfe;
    border: 1px solid var(--line);
    border-radius: var(--radius);
    margin-bottom: 22px;
}

.field {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
}

.field-label {
    color: var(--ink-3);
    flex-shrink: 0;
}

.field-value {
    color: var(--ink-1);
    font-weight: 600;
}

.field-value.coin {
    color: var(--brand-600);
}

.owner-line {
    margin: 10px 0 0;
    font-size: 14px;
    color: var(--ink-3);
}

.owner-name {
    color: var(--brand-600);
    font-weight: 600;
    cursor: pointer;
}

/* ===== 操作按钮：渐变胶囊 ===== */
.action-buttons {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.btn {
    padding: 13px 34px;
    border: none;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: transform .2s ease, box-shadow .2s ease;
}

.btn-rescue {
    background: var(--brand-gradient);
    color: #fff;
    box-shadow: var(--shadow-brand);
}

.btn-adopt {
    background: linear-gradient(135deg, #34d399 0%, #2fbf71 100%);
    color: #fff;
    box-shadow: 0 8px 24px rgba(47, 191, 113, .3);
}

.btn:hover {
    transform: translateY(-2px);
}

.btn-rescue:hover {
    box-shadow: 0 12px 28px rgba(255, 107, 53, .42);
}

.btn-adopt:hover {
    box-shadow: 0 12px 28px rgba(47, 191, 113, .42);
}

.diary-link {
    display: inline-block;
    margin-top: 16px;
    color: var(--brand-600);
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
}

.diary-link:hover {
    color: var(--brand-700);
}

/* ===== 评论区 ===== */
.comment-section {
    padding: 24px 28px;
}

.comment-list {
    margin-bottom: 20px;
}

.comment {
    border: 1px solid var(--line);
    border-radius: var(--radius);
    background-color: #fff;
    padding: 16px 18px;
    margin-bottom: 14px;
    transition: box-shadow .25s ease;
}

.comment:hover {
    box-shadow: var(--shadow-sm);
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.comment-meta {
    display: flex;
    align-items: baseline;
    gap: 12px;
    flex-wrap: wrap;
}

.avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    margin-right: 12px;
    cursor: pointer;
    border: 2px solid var(--brand-100);
    object-fit: cover;
}

.avatarleveltwo {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    margin-right: 8px;
    cursor: pointer;
    object-fit: cover;
}

.comment-author {
    font-weight: 600;
    color: var(--ink-1);
}

.comment-time {
    font-size: 12px;
    color: var(--ink-3);
}

.reply-link {
    font-size: 13px;
}

.comment-content {
    margin: 4px 0 4px 56px;
    color: var(--ink-2);
    line-height: 1.7;
    font-size: 14px;
}

.delete-link {
    margin-left: 15px;
}

/* 子评论缩进块 */
.sub-comment {
    margin: 10px 0 0 56px;
    padding: 10px 14px;
    background: #fafbfe;
    border-left: 3px solid var(--brand-200);
    border-radius: var(--radius-sm);
}

.sub-comment-head {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
}

.sub-author {
    font-size: 13px;
    font-weight: 600;
    color: var(--ink-1);
}

.sub-time {
    font-size: 12px;
    color: var(--ink-3);
    margin-left: 10px;
}

.sub-content {
    font-size: 13px;
    color: var(--ink-2);
    line-height: 1.6;
}

/* ===== 评论输入条 ===== */
.comment-input {
    display: flex;
    gap: 12px;
}

.comment-input input[type="text"] {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid var(--line);
    border-radius: 999px;
    font-size: 14px;
    background: #fafbfe;
    color: var(--ink-1);
    outline: none;
    transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease;
}

.comment-input input[type="text"]:focus {
    background: #fff;
    border-color: var(--brand);
    box-shadow: 0 0 0 3px var(--brand-100);
}

.comment-input button {
    padding: 0 28px;
    background: var(--brand-gradient);
    color: #fff;
    border: none;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    box-shadow: var(--shadow-brand);
    transition: transform .2s ease, box-shadow .2s ease;
}

.comment-input button:hover {
    transform: translateY(-1px);
    box-shadow: 0 10px 24px rgba(255, 107, 53, .4);
}

/* ===== 捐助弹层 ===== */
.container {
    position: relative;
}

.total-price {
    float: right;
    display: flex;
    align-items: center;
    background-color: #fff;
    padding: 8px 16px;
    border-radius: 999px;
    box-shadow: var(--shadow-sm);
}

.label {
    font-weight: bold;
    margin-right: 5px;
}

.price {
    font-weight: bold;
    color: var(--brand-600);
    font-size: 18px;
}

.rescue-items {
    display: flex;
    flex-wrap: wrap;
}

.rescue-item {
    display: flex;
    align-items: center;
    margin-right: 20px;
    margin-bottom: 20px;
    padding: 12px;
    border: 1px solid var(--line);
    border-radius: var(--radius);
    width: 300px;
    transition: box-shadow .25s ease;
}

.rescue-item:hover {
    box-shadow: var(--shadow-sm);
}

.item-image {
    flex-shrink: 0;
    width: 80px;
    height: 80px;
    margin-right: 16px;
    overflow: hidden;
    border-radius: var(--radius-sm);
}

.item-image img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-info {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
}

.item-name {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 10px;
    color: var(--ink-1);
}

.item-quantity {
    display: flex;
    align-items: center;
}

/* 领养协议文本 */
.protocol-text {
    font-size: 14px;
    color: var(--ink-2);
    line-height: 2;
}

.protocol-text p {
    margin: 0 0 8px;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
    .pet-hero {
        grid-template-columns: 1fr;
        padding: 20px;
    }

    .pet-photo img {
        height: 300px;
    }

    .pet-fields {
        grid-template-columns: 1fr;
    }

    .comment-content,
    .sub-comment {
        margin-left: 0;
    }
}
</style>
