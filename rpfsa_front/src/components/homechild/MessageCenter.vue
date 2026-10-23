<template>
    <div class="page-container fade-up">
        <div class="msg-card soft-card">
            <div class="section-head">
                <h3 class="section-title">消息中心</h3>
                <el-button size="small" text type="primary" @click="readAll" :disabled="totalUnread === 0">
                    全部已读
                </el-button>
            </div>
            <el-tabs v-model="activeTab" class="msg-tabs">
                <!-- 系统消息 -->
                <el-tab-pane name="system">
                    <template #label>
                        <el-badge :value="msgUnread || undefined" :max="99">系统消息</el-badge>
                    </template>
                    <div v-if="systemMessages.length === 0" class="empty-state">暂无系统消息</div>
                    <div v-for="msg in systemMessages" :key="msg.id" class="msg-item" :class="{ unread: msg.isRead === 1 }">
                        <span class="msg-dot" v-if="msg.isRead === 1"></span>
                        <div class="msg-body">
                            <div class="msg-content">{{ msg.content }}</div>
                            <div class="msg-time">{{ msg.createTime }}</div>
                        </div>
                    </div>
                </el-tab-pane>
                <!-- 领养通知 -->
                <el-tab-pane name="adopt">
                    <template #label>
                        <el-badge :value="adoptUnread || undefined" :max="99">领养通知</el-badge>
                    </template>
                    <div v-if="adoptList.length === 0" class="empty-state">暂无领养通知</div>
                    <div v-for="a in adoptList" :key="a.id" class="msg-item" :class="{ unread: a.isRead === 1 }">
                        <span class="msg-dot" v-if="a.isRead === 1"></span>
                        <div class="msg-body">
                            <div class="msg-content">
                                您申请领养「{{ a.petname }}」：
                                <el-tag :type="a.state == 2 ? 'warning' : a.state == 1 ? 'success' : 'danger'"
                                    size="small" round>
                                    {{ a.state == 2 ? '审核中' : a.state == 1 ? '已通过' : '未通过' }}
                                </el-tag>
                            </div>
                            <div class="msg-sub" v-if="a.ponReason">审核备注：{{ a.ponReason }}</div>
                            <div class="msg-time">{{ a.applyTime }}</div>
                        </div>
                        <el-button v-if="a.state === 2" size="small" type="danger" plain round
                            :loading="cancelId === a.id" @click="cancelAdopt(a.id)">取消申请</el-button>
                    </div>
                </el-tab-pane>
                <!-- 捐助记录 -->
                <el-tab-pane name="donate">
                    <template #label>
                        <el-badge :value="donateUnread || undefined" :max="99">捐助记录</el-badge>
                    </template>
                    <div v-if="donateList.length === 0" class="empty-state">暂无捐助记录</div>
                    <div v-for="d in donateList" :key="d.id" class="msg-item" :class="{ unread: d.isRead === 1 }">
                        <span class="msg-dot" v-if="d.isRead === 1"></span>
                        <div class="msg-body">
                            <div class="msg-content">
                                捐助「{{ d.petname }}」共 {{ d.totalPrice }} 宠宠币
                                <el-tag :type="d.state === 1 ? 'success' : 'warning'" size="small" round>
                                    {{ d.state === 1 ? '已完成分配' : '分配中' }}
                                </el-tag>
                            </div>
                            <div class="msg-time">{{ d.createTime }}</div>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script setup>
import axios from 'axios'
import { computed, inject, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const currentUser = JSON.parse(sessionStorage.getItem('user'))

// 与导航栏共享的未读计数（provide/inject，已读后同步清零）
const adoptnews = inject('adoptnews')
const donatenews = inject('donatenews')
const messagenews = inject('messagenews')

const activeTab = ref('system')
const systemMessages = ref([])
const adoptList = ref([])
const donateList = ref([])

const adoptUnread = ref(0)
const donateUnread = ref(0)
const msgUnread = ref(0)
const totalUnread = computed(() => adoptUnread.value + donateUnread.value + msgUnread.value)

onMounted(() => {
    if (!currentUser) return
    loadAll()
})

function loadAll() {
    // 系统消息（管理员→用户）
    axios.get(`/message/${currentUser.id}`).then(res => {
        if (res.data.code === 200) {
            systemMessages.value = (res.data.data || []).filter(m => m.content != null)
        }
    })
    axios.get(`/message/noread/${currentUser.id}/user`).then(res => {
        if (res.data.code === 200) msgUnread.value = res.data.data || 0
    })
    // 领养通知
    axios.get(`/user/myadopt/${currentUser.id}`).then(res => {
        if (res.data.code === 200) adoptList.value = res.data.data || []
    })
    axios.get(`/adopt/noread/${currentUser.id}`).then(res => {
        if (res.data.code === 200) adoptUnread.value = res.data.data || 0
    })
    // 捐助记录
    axios.get(`/donate/${currentUser.id}`).then(res => {
        if (res.data.code === 200) donateList.value = res.data.data || []
    })
    axios.get(`/donate/noread/${currentUser.id}`).then(res => {
        if (res.data.code === 200) donateUnread.value = res.data.data || 0
    })
}

/** 取消审核中的领养申请 */
const cancelId = ref(null)
function cancelAdopt(id) {
    ElMessageBox.confirm('确定取消该领养申请吗？取消后宠物将重新进入待领养状态。', '取消申请', {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
    }).then(() => {
        cancelId.value = id
        axios.delete(`/adopt/cancel/${id}`).then(res => {
            if (res.data.code === 200) {
                ElMessage.success(res.data.msg)
                loadAll()
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(() => {}).finally(() => cancelId.value = null)
    }).catch(() => {})
}

/** 全部已读：三个通道各发一条批量已读 */
function readAll() {
    axios.patch(`/adopt/${currentUser.id}`)
    axios.patch(`/donate/${currentUser.id}`)
    axios.patch(`/message/${currentUser.id}/user`).then(() => {
        ElMessage.success('全部已读')
        loadAll()
        // 同步导航栏角标
        if (adoptnews) adoptnews.value = undefined
        if (donatenews) donatenews.value = undefined
        if (messagenews) messagenews.value = undefined
    })
}
</script>

<style scoped>
.msg-card {
    max-width: 860px;
    margin: 0 auto;
    padding: 24px 28px;
}

.section-head .el-button {
    margin-left: auto;
}

.msg-tabs :deep(.el-tabs__item) {
    font-size: 15px;
}

.msg-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 14px 12px;
    border-bottom: 1px dashed var(--line);
    transition: background-color .2s ease;
    border-radius: var(--radius-sm);
}

.msg-item:hover {
    background: var(--brand-100);
}

.msg-item:last-child {
    border-bottom: none;
}

.msg-item.unread {
    background: #fffaf5;
}

.msg-dot {
    flex-shrink: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--brand);
    margin-top: 7px;
}

.msg-body {
    flex: 1;
    min-width: 0;
}

.msg-content {
    font-size: 14px;
    color: var(--ink-1);
    line-height: 1.7;
}

.msg-sub {
    font-size: 13px;
    color: var(--ink-2);
    margin-top: 4px;
}

.msg-time {
    font-size: 12px;
    color: var(--ink-3);
    margin-top: 4px;
}

.msg-item .el-button {
    flex-shrink: 0;
    align-self: center;
}
</style>
