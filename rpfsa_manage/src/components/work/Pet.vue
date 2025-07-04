<template>
    <!-- 操作栏 -->
    <div>
        <el-button type="primary" plain @click="tanchu" style="margin-right: 30px;">
            添加猫狗
        </el-button>
        <el-input v-model="searchinfo.con" placeholder="请输入" class="input-with-select" style="width: 300px;">
            <template #prepend>
                <el-select v-model="searchinfo.col" placeholder="选择" style="width: 90px">
                    <el-option label="宠物名" value="petname" />
                    <el-option label="品种" value="variety" />
                </el-select>
            </template>
        </el-input>
        <el-button :icon="Search" type="success" @click="fsearch" style="margin-left: 5px;" />
        <el-button :icon="Refresh" type="warning" @click="flush" style="margin-left: 20px;" />
    </div>
    <!-- 表格数据 -->
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column prop="petname" label="宠物名" min-width="auto" align="center" />
        <el-table-column prop="age" label="年龄" min-width="auto" align="center" />
        <el-table-column prop="photo" label="照片" min-width="auto" align="center">
            <template #="scope">
                <el-image style="width: 65px; height: 45px;" :src="getImageUrl(scope.row.photo)" />
            </template>
        </el-table-column>
        <el-table-column prop="sex" label="性别" min-width="auto" align="center">
            <template #="scope">
                {{ scope.row.sex == 1 ? '公' : scope.row.sex == 2 ? '不详' : '母' }}
            </template>
        </el-table-column>
        <el-table-column prop="classic" label="种类" min-width="auto" align="center">
            <template #="scope">
                {{ scope.row.classic ? '猫' : '狗' }}
            </template>
        </el-table-column>
        <el-table-column label="类型" min-width="auto" align="center">
            <template #="scope">
                {{ scope.row.type ? '私人上传' : '流浪平台' }}
            </template>
        </el-table-column>
        <el-table-column prop="variety" label="品种" min-width="auto" align="center" />
        <el-table-column prop="petCoin" label="宠宠币" min-width="auto" align="center" />
        <el-table-column prop="isVaccine" label="疫苗" min-width="auto" align="center">
            <template #="scope">
                <el-tag v-if="scope.row.isVaccine" type="success" disable-transitions="true">已接种</el-tag>
                <el-tag v-else type="danger" disable-transitions="true">未接种</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="isAdopt" label="状态" min-width="auto" align="center">
            <template #="scope">
                <el-tag v-if="scope.row.isAdopt == 1" type="success" effect="dark" disable-transitions="true">已被领养</el-tag>
                <el-tag v-else-if="scope.row.isAdopt == 2" effect="dark" disable-transitions="true">正在申请</el-tag>
                <el-tag v-else type="warning" effect="dark" disable-transitions="true">未被领养</el-tag>
            </template>
        </el-table-column>

        <el-table-column prop="createTime" label="添加时间" min-width="150" align="center" />
        <el-table-column fixed="right" label="操作" min-width="180" align="center">
            <template #="scope">
                <el-button type="primary" :icon="Expand" circle @click="editpet(scope.row)" />
                <el-button type="danger" :icon="Delete" circle @click="deletepet(scope.row.id)" />
                <el-button :disabled="scope.row.isAdopt != 1 && scope.row.type != 1" type="warning" :icon="Postcard" circle
                    @click="querydiarylist(scope.row.id)" />
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
    <el-dialog v-model="dialogFormVisible" title="填写猫狗信息" style="border-radius: 20px; width: 600px;">
        <el-form label-position="left" label-width="80px" :model="petinfo" style="max-width: 450px">
            <el-form-item class="efi" label="名字:">
                <el-input v-model="petinfo.petname" />
            </el-form-item>
            <el-form-item class="efi" label="年龄:">
                <el-input v-model="petinfo.age" type="number" />
            </el-form-item>
            <el-form-item class="efi" label="性别:">
                <el-radio-group v-model="petinfo.sex" class="ml-4">
                    <el-radio label="1" size="large">公</el-radio>
                    <el-radio label="0" size="large">母</el-radio>
                    <el-radio label="2" size="large">不详</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="种类:">
                <el-radio-group v-model="petinfo.classic" class="ml-4">
                    <el-radio label="0" size="large" border>小狗</el-radio>
                    <el-radio label="1" size="large" border>小猫</el-radio>
                    <el-radio label="2" size="large" border>其他</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="品种:">
                <el-input v-model="petinfo.variety" />
            </el-form-item>
            <el-form-item class="efi" label="疫苗:">
                <el-radio-group v-model="petinfo.isVaccine" class="ml-4">
                    <el-radio :label="0" size="large">未接种</el-radio>
                    <el-radio :label="1" size="large">已接种</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="照片:">
                <el-upload ref="uploadRef" class="avatar-uploader" action="http://localhost/upload" :show-file-list="false"
                    :on-success="handleAvatarSuccess" :on-change="changeAvatar" :auto-upload="false"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
                <div style="margin-left: 30px;">
                    <el-input v-model="petinfo.describtion" :rows="6" type="textarea" placeholder="请输入描述信息" resize="none" />
                </div>
            </el-form-item>
        </el-form>
        <template #footer>
            <span>
                <el-button @click="cancelclear">取消</el-button>
                <el-button type="primary" @click="addpet">添加</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 详情||修改 -->
    <el-dialog v-model="dialogFormVisible_x" title="修改猫狗信息" style="border-radius: 20px; width: 650px;">
        <el-form label-position="left" label-width="80px" :model="petinfo_x.value" style="max-width: 450px">
            <el-form-item class="efi" label="ID:">
                <el-input v-model="petinfo_x.value.id" disabled />
            </el-form-item>
            <el-form-item class="efi" label="创建时间:">
                <el-input v-model="petinfo_x.value.createTime" disabled />
            </el-form-item>
            <el-form-item class="efi" label="宠物名:">
                <el-input v-model="petinfo_x.value.petname" />
            </el-form-item>
            <el-form-item class="efi" label="性别:">
                <el-radio-group v-model="petinfo_x.value.sex" class="ml-4">
                    <el-radio :label="1" size="large">公</el-radio>
                    <el-radio :label="0" size="large">母</el-radio>
                    <el-radio :label="2" size="large">不详</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="种类:">
                <el-radio-group v-model="petinfo_x.value.classic" class="ml-4">
                    <el-radio :label="0" size="large" border>小狗</el-radio>
                    <el-radio :label="1" size="large" border>小猫</el-radio>
                    <el-radio :label="2" size="large" border>其他</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="品种:">
                <el-input v-model="petinfo_x.value.variety" />
            </el-form-item>
            <el-form-item class="efi" label="疫苗:">
                <el-radio-group v-model="petinfo_x.value.isVaccine" class="ml-4">
                    <el-radio :label="0" size="large">未接种</el-radio>
                    <el-radio :label="1" size="large">已接种</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="状态:">
                <el-radio-group v-model="petinfo_x.value.isAdopt" class="ml-4">
                    <el-radio :label="0" size="large">未被领养</el-radio>
                    <el-radio :label="1" size="large">已被领养</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item class="efi" label="宠宠币:">
                <el-input type="number" v-model="petinfo_x.value.petCoin" style="width: 100px;" />
            </el-form-item>
            <!-- <el-form-item class="efi" label="照片:">
                    <el-upload ref="uploadRef" class="avatar-uploader" action="http://localhost/upload"
                        :show-file-list="false" :on-success="handleAvatarSuccess" :on-change="changeAvatar"
                        :auto-upload="false" :before-upload="beforeAvatarUpload">
                        <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                    <div style="margin-left: 50px;">
                        <el-input v-model="petinfo.describtion" :rows="6" type="textarea" placeholder="请输入描述信息"
                            resize="none" />
                    </div>
                </el-form-item> -->

        </el-form>
        <template #footer>
            <span>
                <el-button @click="dialogFormVisible_x = false">取消</el-button>
                <el-button type="primary" @click="updatepet">修改</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 动态列表 -->
    <el-dialog v-model="diarysVisible" title="动态列表">
        <el-scrollbar height="300px">
            <div v-for="diary in diarylist" :key="diary.id">
                简介：<el-link type="primary" @click="showdiary(diary)">{{ diary.title }}</el-link> &nbsp 时间：{{
                    diary.createTime }}
            </div>
        </el-scrollbar>

    </el-dialog>
    <!-- 动态详情 -->
    <el-dialog v-model="diaryVisible" title="动态内容">
        <el-scrollbar height="420px">
            <div class="ql-editor" v-html="diary.value.diary"></div>
        </el-scrollbar>
    </el-dialog>
</template>
  
<script setup>
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue';
import { Expand, Delete, Plus, Search, Refresh, Postcard } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'quill/dist/quill.core.css'

onMounted(() => {
    petget()
})

//#region 动态列表获取与查看详情
const diarysVisible = ref(false)
const diaryVisible = ref(false)
const diarylist = ref([])
const diary = reactive({})
function querydiarylist(pid) {
    axios.get(`/journal/${pid}`).then((res) => {
        if (res.data.code == 200) {
            diarylist.value = res.data.data
            diarysVisible.value = true
            console.log(diarylist.value);
        } else ElMessage.warning(res.data.msg)
    }).catch((err) => {
        console.log(err);
    })
}
function showdiary(dia) {
    diary.value = dia
    diaryVisible.value = true
}
//#endregion

//#region 搜索功能
const searchinfo = reactive({
    col: null,
    con: null
})
function fsearch() {
    axios.get(`/pet/${currentpage.value}/${searchinfo.col}/${searchinfo.con}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else {
                ElMessage.error(res.data.msg)
            }
        })
        .catch(err => {
            console.error(err);
        })
}
//刷新
function flush() {
    searchinfo.col = null
    searchinfo.con = null
    petget()
}
//#endregion

//#region 猫狗列表分页
const tableData = ref([])
const currentpage = ref(1)
const pages = ref(0)


function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) {
        //没条件,普通查
        petget()
    } else {
        fsearch()
    }
}
// 查询猫狗列表公共方法
function petget() {
    axios.get(`/pet/page/${currentpage.value}`)
        .then(res => {
            if (res.data.code == 200) {
                tableData.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.error(res.data.msg)
        }).catch(() => ElMessage.error('服务器故障'))
}
//#endregion

//#region 修改
const dialogFormVisible_x = ref(false)
const petinfo_x = reactive({})
function editpet(pet) {
    petinfo_x.value = pet
    dialogFormVisible_x.value = true
}
function updatepet() {
    axios.patch('/pet', petinfo_x.value).then((res) => {
        if (res.data.data) {
            ElMessage.success('修改成功')
            petget()
        }
    })
    dialogFormVisible_x.value = false
}
//#endregion

//#region 删除
function deletepet(id) {
    ElMessageBox.confirm('你确定要删除吗？该操作不可撤回哦!', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        axios.delete(`/pet/${id}`).then(() => petget())
        ElMessage.success('删除成功')
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
//#endregion

//#region 添加
function tanchu() {
    dialogFormVisible.value = true;
}
//添加猫狗
const dialogFormVisible = ref(false)
const petinfo = reactive({
    petname: '',
    age: null,
    sex: null,
    photo: '',
    classic: null,
    variety: '',
    isVaccine: null,
    describtion: ''
})

function addpet() {
    uploadRef.value.submit()
}
function cancelclear() {
    dialogFormVisible.value = false
    petinfo.petname = '',
        petinfo.age = null,
        petinfo.sex = null,
        petinfo.photo = '',
        petinfo.classic = null,
        petinfo.variety = '',
        petinfo.isVaccine = 0,
        petinfo.describtion = ''
    imageUrl.value = null
}
//上传照片
const imageUrl = ref('')
const uploadRef = ref(null)
function handleAvatarSuccess(response) {
    petinfo.photo = response
    axios.post('/pet', petinfo)
        .then(() => ElMessage.success('添加成功'))
        .catch(() => ElMessage.error('服务器故障'))
        .then(() => {
            petget()
            cancelclear()
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

//vue3访问静态资源
function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }

</script>
<style scoped>
.el-dialog-div {
    height: 60vh;
    overflow: auto;
}

.el-dialog-div-diarylist {
    height: 45vh;
    overflow: auto;
}

.avatar-uploader .avatar {
    width: 150px;
    height: 120px;
    display: block;
}

.efi {
    margin-bottom: 8px;
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
    width: 150px;
    height: 120px;
    text-align: center;
}
</style>
  