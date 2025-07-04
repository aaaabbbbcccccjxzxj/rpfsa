<template>
    <!-- 平台流浪管理 -->
    <div class="pet-details" v-if="currentpet.type == 0">
        <div class="pet-image">
            <img v-if="currentpet.photo != undefined" :src="getImageUrl(currentpet.photo)" alt="Pet Image">
        </div>
        <div class="pet-info">
            <h2 class="pet-name">{{ currentpet.petname }}</h2>
            <p><strong>性别:</strong> {{ currentpet.sex }}</p>
            <p><strong>年龄:</strong> {{ currentpet.age }}</p>
            <p><strong>疫苗接种:</strong>
                <span>
                    <el-tag v-if="currentpet.isVaccine == 1" type="success">已接种</el-tag>
                    <el-tag v-else="currentpet.isVaccine == 0" type="warning">未接种</el-tag>
                </span>
            </p>
            <p><strong>添加时间:</strong> {{ currentpet.createTime }}</p>
            <p><strong>目前状态:</strong>
                <span>
                    <el-tag v-if="currentpet.isAdopt == 1" type="success">已领养</el-tag>
                    <el-tag v-else-if="currentpet.isAdopt == 0" type="warning">流浪中</el-tag>
                    <el-tag v-else>领养审核中</el-tag>
                </span>
            </p>
            <div class="action-buttons" v-if="currentpet.isAdopt == 0">
                <button class="rescue-button" @click="salvage()">救助</button>
                <button class="adoption-button" @click="adopt()">领养</button>
            </div>
            <p><strong>宠宠币:</strong> {{ currentpet.petCoin }}</p>
            <div>{{ currentpet.describtion }}</div>
            <router-link :to="`/diary/${currentpet.id}`">
                <el-link v-if="currentpet.isAdopt == 1" type="primary">日记动态</el-link>
            </router-link>
        </div>
    </div>
    <!-- 私人上传猫狗 -->
    <div class="pet-details" v-else>
        <div class="pet-image">
            <img v-if="currentpet.photo != undefined" :src="getImageUrl(currentpet.photo)" alt="Pet Image">
        </div>
        <div class="pet-info">
            <h2 class="pet-name">{{ currentpet.petname }}</h2>
            <el-popover placement="bottom" :title="`昵称:${currentpet.user.nickname}`" :width="220" trigger="click">
                <template #>
                    电话：{{ currentpet.user.phone }} <br>
                    邮箱：{{ currentpet.user.email }} <br>
                    领养个数：{{ currentpet.user.howAdopt }} <br>
                    宠宠币：{{ currentpet.user.petCoin }}
                </template>
                <template #reference>
                    <p><strong>主人:</strong><span style="cursor: pointer;">{{ currentpet.user.nickname }}</span></p>
                </template>
            </el-popover>
            <p><strong>性别:</strong> {{ currentpet.sex }}</p>
            <p><strong>年龄:</strong> {{ currentpet.age }}</p>
            <p><strong>品种:</strong> {{ currentpet.vatriety }}</p>
            <p><strong>疫苗接种:</strong>
                <span>
                    <el-tag v-if="currentpet.isVaccine == 1" type="success">已接种</el-tag>
                    <el-tag v-else="currentpet.isVaccine == 0" type="warning">未接种</el-tag>
                </span>
            </p>
            <p><strong>添加时间:</strong> {{ currentpet.createTime }}</p>

            <div>{{ currentpet.describtion }}</div>
            <router-link :to="`/diary/${currentpet.id}`">
                <el-link type="primary"> 日记动态</el-link>
            </router-link>
        </div>
    </div>
    <!-- 评论区 -->
    <div class="comment-section">
        <h2>评论</h2>
        <div class="comment-list">
            <div v-for="comment in currentpet.comments" :key="comment.id" class="comment">
                <div class="comment-header">
                    <!-- 点头像 -->
                    <el-popover placement="bottom" :title="`昵称:${comment.user.nickname}`" :width="220" trigger="click">
                        电话：{{ comment.user.phone }} <br>
                        邮箱：{{ comment.user.email }} <br>
                        领养个数：{{ comment.user.howAdopt }} <br>
                        宠宠币：{{ comment.user.petCoin }}
                        <template #reference>
                            <img v-if="comment.user.photo != undefined" class="avatar"
                                :src="`http://localhost/img/${comment.user.photo}`" alt="用户头像">
                        </template>
                    </el-popover>
                    <div>
                        <span class="comment-author">{{ comment.user.nickname == null ? comment.user.username
                            : comment.user.nickname }}
                        </span>
                        <span class="comment-time">{{ comment.createTime }}</span>
                        <span style="margin-left: 20px;"><el-link type="primary"
                                @click="addlevelTwo(comment.id)">回复</el-link></span>
                    </div>
                </div>
                <div class="comment-content">{{ comment.content }}
                    <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                        @confirm="commentdelete(comment.id, comment.level)">
                        <template #reference>
                            <span v-if="currentUser != null && comment.uid == currentUser.id" style="margin: 15px;"><el-link
                                    type="danger">删除</el-link></span>
                        </template>
                    </el-popconfirm>
                </div>
                <hr>
                <!-- 子评论 -->
                <div v-for="leveltwocon in comment.commentList" :key="leveltwocon.id">
                    <div class="comment-content">
                        <el-popover placement="bottom" :title="`昵称:${leveltwocon.user.nickname}`" :width="220"
                            trigger="click">
                            电话：{{ leveltwocon.user.phone }} <br>
                            邮箱：{{ leveltwocon.user.email }} <br>
                            领养个数：{{ leveltwocon.user.howAdopt }} <br>
                            宠宠币：{{ leveltwocon.user.petCoin }}
                            <template #reference>
                                <img v-if="comment.user.photo != undefined" class="avatarleveltwo"
                                    :src="`http://localhost/img/${leveltwocon.user.photo}`" alt="用户头像">
                            </template>
                        </el-popover>
                        <div>
                            <span>{{ leveltwocon.user.nickname == null ?
                                leveltwocon.user.username
                                : leveltwocon.user.nickname }}
                            </span>
                            <span>{{ leveltwocon.createTime }}</span>
                        </div>
                    </div>
                    <div class="comment-content">{{ leveltwocon.content }}
                        <el-popconfirm title="你确定要删除该评论吗？" width="200" confirmButtonText="确认" cancelButtonText="取消"
                            @confirm="commentdelete(leveltwocon.id, leveltwocon.level)">
                            <template #reference>
                                <span v-if="currentUser != null && leveltwocon.uid == currentUser.id"
                                    style="margin: 15px;"><el-link type="danger">删除</el-link></span>
                            </template>
                        </el-popconfirm>
                    </div>
                </div>
            </div>
        </div>
        <div class="comment-input">
            <input v-model="currcomment" type="text" placeholder="说点什么....">
            <button @click="confirmcomment">发表</button>
        </div>
    </div>
    <!-- 捐助弹层 -->
    <el-dialog title="捐助选择" v-model="dialogFormVisible" style="border-radius: 20px;" :show-close="false">
        <div class="container">
            <div class="rescue-items">
                <div class="rescue-item" v-for="food in foodlist" :key="food.id">
                    <div class="item-image">
                        <img :src="`http://localhost/img/${food.picture}`" alt="rescue item image">
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
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogAdoptVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmadopt">
                    确定
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>
<script setup>
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { defineProps, onMounted, ref, reactive } from 'vue';
import { EluiChinaAreaDht } from 'elui-china-area-dht'
// 地址选择器
const chinaData = new EluiChinaAreaDht.ChinaArea().chinaAreaflat

//#region 评论
const currcomment = ref('')
// 删除评论
function commentdelete(commid, commlevel) {
    if (commlevel) {
        //二级评论,直接删
        axios.delete(`/comment/${commid}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    axios.patch('/pet', {
                        id: pid,
                        commentCount: currentpet.value.commentCount - 1
                    })
                    getcurpet()
                    //删除post的commentCount
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
                console.error(err);
            })
    } else {
        //一级评论,条件删除
        axios.delete(`/comment/t1/${commid}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                    axios.patch('/pet', {
                        id: pid,
                        commentCount: currentpet.value.commentCount - res.data.data
                    })
                    getcurpet()
                    //删除post的commentCount
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
                console.error(err);
            })
    }
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
                //发表评论
                axios.post('/comment', {
                    uid: currentUser.id,
                    poid: 0,
                    pid: pid,
                    content: commenttext,
                    level: cid
                }).then(() => {
                    //该条post的评论数加1
                    axios.patch('/pet', {
                        id: pid,
                        commentCount: currentpet.value.commentCount + 1
                    })
                    ElMessage.success('评论成功😊')
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

function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))

//#region 救助

//总价计算
const totalprice = ref(0)
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
                console.error(err);
            })
        dialogFormVisible.value = true
        //发送宠宠币请求
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}
// 确认救助
function salvageconfirm() {
    if (totalprice.value == 0) {
        //没选救助品
        ElMessage.warning('对不起,不能捐助为空！！')
    } else {
        if (totalprice.value <= currentUser.petCoin) {
            ElMessageBox.confirm('确认要完成此次捐助吗?', '捐助确认', {
                cancelButtonText: '取消',
                confirmButtonText: '确认',
                type: 'warning'
            }).then(() => {
                const total = totalprice.value //不知道为什么,totalprice.value在两层axios后值会变成0,原因不明
                //宠物axios
                axios.patch('/pet', {
                    id: pid,
                    petCoin: currentpet.value.petCoin + total
                }).then(() => {
                    //用户axios
                    axios.patch('/user', {
                        id: currentUser.id,
                        petCoin: currentUser.petCoin - total
                    }).then(() => {
                        const donatelist = ref([])
                        //food库存更新
                        for (let i = 0; i < foodlist.value.length; i++) {
                            //创建每一个对象添加到donatelist中
                            donatelist.value.push(foodlist.value[i])
                            //发送所有inventory请求,修改库存
                            axios.patch('/food', {
                                id: foodlist.value[i].id,
                                inventory: foodlist.value[i].inventory - foodlist.value[i].tempCount
                            }).then(() => { })
                        }
                        // 更新用户session
                        currentUser.petCoin -= total
                        sessionStorage.setItem("user", JSON.stringify(currentUser))
                        ElMessage.success('感谢您！你的爱心比会有所回报')
                        //创建捐助记录
                        axios.post('/donate', {
                            uid: currentUser.id,
                            pid: pid,
                            username: currentUser.username,
                            nickname: currentUser.nickname,
                            petname: currentpet.value.petname,
                            donateList: JSON.stringify(donatelist.value),
                            totalPrice: total
                        })
                            .then(res => {
                                console.log(res)
                            })
                            .catch(err => {
                                console.error(err);
                            })
                    })
                }).finally(() => {
                    getcurpet()
                    cancel()
                })
            }).catch(() => {
                ElMessage.info('已取消')
            })

        }
        else ElMessage.warning(`对不起,您目前的宠宠币为: ${currentUser.petCoin} ,请降低您的爱心.`)
    }

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
function confirmadopt() {
    ad.value += adressdetail.value
    ElMessageBox.confirm('确认要领养该流浪动物吗?', '领养确认', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        //发送请求创建领养表
        axios.post('/adopt', {
            uid: currentUser.id,
            username: currentUser.username,
            nickname: currentUser.nickname,
            pid: pid,
            petname: currentpet.value.petname,
            reason: reason.value,
            adress: ad.value,
        }).then(() => {
            //修改pet状态
            axios.patch('/pet', {
                id: pid,
                isAdopt: 2,
                uid: currentUser.id
            }).then(() => {
                getcurpet()
                ElMessage.success('已成功申请,注意查看消息通知')
            })
        }).catch(err => {
            console.error(err);
        })
        dialogAdoptVisible.value = false
    }).catch(() => {
        ElMessage.info('已取消')
    })
}

//#endregion

</script>

<style scoped>
.comment-section {
    margin-top: 20px;
    font-family: Arial, sans-serif;
}

.comment-list {
    margin-bottom: 20px;
}

.comment {
    border-radius: 4px;
    background-color: #f0f0f0;
    padding: 10px;
    margin-bottom: 10px;
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 5px;
}

.avatar {
    width: 45px;
    /* 调整头像的大小 */
    height: 45px;
    border-radius: 50%;
    /* 使头像显示为圆形 */
    margin-right: 10px;
    cursor: pointer;
}

.avatarleveltwo {
    width: 25px;
    /* 调整头像的大小 */
    height: 25px;
    border-radius: 50%;
    /* 使头像显示为圆形 */
    margin-right: 10px;
    cursor: pointer;
}

.comment-author {
    font-weight: bold;
}

.comment-time {
    font-size: 12px;
    color: #777;
    margin-left: 20px;
}

.comment-content {
    margin-bottom: 5px;
}

.pet-details {
    margin-top: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    animation: fadeIn 1s;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.pet-image {
    width: 300px;
    height: 300px;
    margin-bottom: 20px;
    border-radius: 50%;
    overflow: hidden;
}

.pet-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s;
}

.pet-image:hover img {
    transform: scale(1.2);
}

.pet-name {
    font-size: 24px;
    margin-bottom: 10px;
}

.pet-info p {
    margin-bottom: 5px;
}

.action-buttons {
    margin-top: 20px;
}

.rescue-button,
.adoption-button {
    padding: 10px 20px;
    margin-right: 10px;
    border: none;
    background-color: #ecdddd;
    color: #333;
    cursor: pointer;
    transition: background-color 0.3s;
    border-radius: 20px;
}

.rescue-button:hover,
.adoption-button:hover {
    background-color: #eacfcf;
}


/* */
.container {
    position: relative;
}

.total-price {
    float: right;
    display: flex;
    align-items: center;
    background-color: #fff;
    padding: 6px;
    border-radius: 5px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.label {
    font-weight: bold;
    margin-right: 5px;
}

.price {
    font-weight: bold;
    color: #ff001e;
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
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
    width: 300px;
}

.item-image {
    flex-shrink: 0;
    width: 80px;
    height: 80px;
    margin-right: 20px;
    overflow: hidden;
    border-radius: 5px;
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
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
}

.item-quantity {
    display: flex;
    align-items: center;
}

.comment-input input[type="text"] {
    width: 50%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    margin-bottom: 20px;
}

.comment-input button {
    padding: 8px 16px;
    background-color: #007bff;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: all .3s;
}

.comment-input button:hover {
    background-color: #2b4e73;
}
</style>