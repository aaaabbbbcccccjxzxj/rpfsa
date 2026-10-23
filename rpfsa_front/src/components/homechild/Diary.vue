<template>
    <div class="page-container fade-up">
        <div class="diary-card soft-card">
            <div class="section-head">
                <h3 class="section-title">日记动态</h3>
                <el-button v-if="currentUser != null && currentUser.id == currentpet.uid" type="primary" round
                    size="small" @click="isadddiary = true">
                    + 添加动态
                </el-button>
            </div>
            <!-- 时间线 -->
            <div class="timeline">
                <div v-for="diary in diarylist" :key="diary.id" class="timeline-item">
                    <div class="timeline-dot"></div>
                    <div class="timeline-body">
                        <div class="timeline-row">
                            <span class="diary-title" @click="showdiary(diary)">{{ diary.title }}</span>
                            <el-link v-if="currentUser != null && currentUser.id == currentpet.uid" type="danger"
                                class="diary-delete" @click="deletediary(diary.id)">删除
                            </el-link>
                        </div>
                        <div class="diary-time">{{ diary.createTime }}</div>
                    </div>
                </div>
                <div v-if="diarylist.length === 0" class="empty-state">还没有动态，期待第一篇日记～</div>
            </div>
        </div>
    </div>
    <!-- 添加动态弹层 -->
    <el-dialog v-model="isadddiary" title="编辑" top="25px">
        <el-form label-position="top">
            <el-form-item label="标题:">
                <el-input v-model="diaryinfo.title" placeholder="请输入标题" />
            </el-form-item>
            <QuillEditor :modules="modules" v-model:content="diaryinfo.diary" theme="snow" :toolbar="toolbarinfo"
                placeholder="输入动态内容" contentType="html" style="height: 350px;" />
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="isadddiary = false">取消</el-button>
                <el-button type="primary" @click="adddiary()">
                    发布
                </el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 动态详情 -->
    <el-dialog v-model="diaryVisible" title="动态内容" fullscreen>

        <div class="ql-editor" v-html="diary.diary"></div>

    </el-dialog>
</template>

<script setup>
import { QuillEditor } from '@vueup/vue-quill'
import BlotFormatter from 'quill-blot-formatter'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import axios from 'axios';
import { defineProps, onMounted, ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import 'quill/dist/quill.core.css'

onMounted(() => {
    getdiatylist()
    getcurpet()
})

//#region 富文本编辑
//富文本图片大小调整
const modules = {
    name: 'blotFormatter',
    module: BlotFormatter,
    options: {},
}

//工具栏
const toolbarinfo = [
    ['bold', 'italic', 'underline', 'strike', 'code-block', { 'header': 1 }, { 'header': 2 },
        { 'list': 'ordered' }, { 'list': 'bullet' }, { 'script': 'sub' }, { 'script': 'super' }],
    [{ 'size': ['small', false, 'large', 'huge'] }],  // custom dropdown
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ 'color': [] }, { 'background': [] }, 'image'],
    [{ 'align': [] }, 'clean'],
]
//#endregion

//#region 添加动态（publishing 防重复提交；回访日记奖励由后端判定发放）
const isadddiary = ref(false)
const publishing = ref(false)
const diaryinfo = reactive({
    uid: null,
    pid: pid,
    title: '',
    diary: '',
})
function adddiary() {
    if (diaryinfo.title == '' || diaryinfo.diary == '') {
        ElMessage.warning('标题和内容不能为空！')
    } else {
        ElMessageBox.confirm('您确定要发布吗？领养宠物的回访日记可获得 +5 宠宠币奖励', '确认发布', {
            cancelButtonText: '继续编辑',
            confirmButtonText: '确定',
            type: 'info'
        }).then(() => {
            if (publishing.value) return
            publishing.value = true
            diaryinfo.uid = currentUser.id
            axios.post('/journal', diaryinfo)
                .then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success(res.data.msg)
                    } else ElMessage.error(res.data.msg)
                    isadddiary.value = false
                })
                .catch(err => {
                }).finally(() => {
                    publishing.value = false
                    getdiatylist()
                })
        }).catch(() => { })
    }
}
//#endregion

//#region 拿到pet和diarylist
const { pid } = defineProps(['pid'])
const diarylist = ref([])
function getdiatylist() {
    axios.get(`/journal/${pid}`).then((res) => {
        diarylist.value = res.data.data
    }).catch((err) => {
    })
}
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

//#region 删除动态
function deletediary(id) {
    ElMessageBox.confirm('该操作不可撤销哦！', '您确定要删除吗?', {
        cancelButtonText: '取消',
        confirmButtonText: '确定',
        type: 'warning'
    }).then(() => {
        axios.delete(`/journal/${id}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
            }).finally(() => {
                getdiatylist()
            })
    }).catch(() => ElMessage.info('已取消'))
}
//#endregion

const diaryVisible = ref(false)
// 用 ref 存放"当前查看的动态"。原写法是 reactive({}) 再挂一个 value 属性，
// 模板里又写 diary.value.diary —— 能跑但语义完全错误，稍一改动就会静默失效。
const diary = ref({})
function showdiary(dia) {
    diary.value = dia
    diaryVisible.value = true
}


const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))

</script>

<style scoped>
.diary-card {
    max-width: 860px;
    margin: 0 auto;
    padding: 26px 30px;
}

/* 让"添加动态"按钮贴到标题行右侧 */
.section-head {
    display: flex;
    align-items: center;
}

.section-head .el-button {
    margin-left: auto;
}

/* ===== 时间线 ===== */
.timeline {
    position: relative;
    padding-left: 22px;
}

.timeline::before {
    content: '';
    position: absolute;
    left: 6px;
    top: 6px;
    bottom: 6px;
    width: 2px;
    border-radius: 2px;
    background: linear-gradient(to bottom, var(--brand-200), var(--line));
}

.timeline-item {
    position: relative;
    padding-bottom: 20px;
}

.timeline-dot {
    position: absolute;
    left: -22px;
    top: 7px;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: #fff;
    border: 3px solid var(--brand);
    box-shadow: 0 0 0 3px var(--brand-100);
}

.timeline-body {
    background: #fafbfe;
    border: 1px solid var(--line);
    border-radius: var(--radius);
    padding: 14px 18px;
    transition: box-shadow .25s ease, transform .25s ease;
}

.timeline-body:hover {
    box-shadow: var(--shadow-sm);
    transform: translateX(3px);
}

.timeline-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.diary-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--ink-1);
    cursor: pointer;
    transition: color .2s ease;
}

.diary-title:hover {
    color: var(--brand);
}

.diary-delete {
    flex-shrink: 0;
    font-size: 13px;
}

.diary-time {
    margin-top: 6px;
    font-size: 12px;
    color: var(--ink-3);
}
</style>
