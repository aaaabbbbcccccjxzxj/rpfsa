<template>
    <div>
        <div style="text-align: right;">
            <el-button v-if="currentUser != null && currentUser.id == currentpet.uid" @click="isadddiary = true">添加动态
            </el-button>
        </div>
        <!-- <div v-for="diary in diarylist" :key="diary.id">
        <el-link type="primary" @click="showdiary(diary)">{{ diary.title }}</el-link>
        <el-link v-if="currentUser != null && currentUser.id == currentpet.uid" type="danger"
            @click="deletediary(diary.id)">删除
        </el-link> &nbsp 时间:{{ diary.createTime }}
    </div> -->
        <div v-for="diary in diarylist" :key="diary.id" class="diary-item">
            <span class="diary-title" @click="showdiary(diary)">{{ diary.title }}</span>
            <el-link v-if="currentUser != null && currentUser.id == currentpet.uid" type="danger"
                @click="deletediary(diary.id)">删除
            </el-link>
            <div class="diary-time">{{ diary.createTime }}</div>
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

        <div class="ql-editor" v-html="diary.value.diary"></div>

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

//#region 添加动态
const isadddiary = ref(false)
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
        ElMessageBox.confirm('您确定要发布吗？', '确认发布', {
            cancelButtonText: '继续编辑',
            confirmButtonText: '确定',
            type: 'info'
        }).then(() => {
            diaryinfo.uid = currentUser.id
            console.log(diaryinfo);
            axios.post('/journal', diaryinfo)
                .then(res => {
                    if (res.data.code == 200) {
                        ElMessage.success(res.data.msg)
                    } else ElMessage.error(res.data.msg)
                    isadddiary.value = false
                })
                .catch(err => {
                    console.error(err);
                }).finally(() => {
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
        console.log(res.data.data);
        diarylist.value = res.data.data
    }).catch((err) => {
        console.log(err);
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
        console.log(id);
        axios.delete(`/journal/${id}`)
            .then(res => {
                if (res.data.code == 200) {
                    ElMessage.success(res.data.msg)
                } else ElMessage.error(res.data.msg)
            })
            .catch(err => {
                console.error(err);
            }).finally(() => {
                getdiatylist()
            })
    }).catch(() => ElMessage.info('已取消'))
}
//#endregion

const diaryVisible = ref(false)
const diary = reactive({})
function showdiary(dia) {
    diary.value = dia
    diaryVisible.value = true
}


const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
function getimgurl(url) { return new URL(`img/${url}`, 'http://localhost/') }

</script>

<style scoped>
.diary-item {
    background-color: #f5f5f5;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.diary-item:hover {
    transform: translateY(-5px);
}

.diary-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    margin-right: 30px;
    color: #333333;
    cursor: pointer;
}

.diary-time {
    margin-top: 10px;
    font-size: 14px;
    color: #666666;
}

.diary-item {
    animation: fadeIn 0.5s ease;
}
</style>