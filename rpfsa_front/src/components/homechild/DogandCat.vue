<template>
    <!-- 筛选和搜索 -->
    <div style="margin: 15px;">
        <el-input v-model="searchinfo.con" placeholder="请输入" style="width: 400px;">
            <template #prepend>
                <el-select v-model="searchinfo.col" placeholder="选择" style="width: 80px">
                    <el-option label="名字" value="petname" />
                    <el-option label="品种" value="variety" />
                </el-select>
            </template>
            <template #append>
                <el-button @click="search">搜索</el-button>
            </template>
        </el-input>
    </div>
    <div style="margin: 15px;">
        <el-radio-group v-model="classic" @change="classicchange">
            <el-radio-button :label="0">小狗</el-radio-button>
            <el-radio-button :label="1">小猫</el-radio-button>
            <el-radio-button :label="2">其他</el-radio-button>
        </el-radio-group>
    </div>
    <div style="margin: 15px;">
        <el-radio-group v-model="showchange" @change="typechange()">
            <el-radio-button :label="0">平台流浪</el-radio-button>
            <el-radio-button :label="1">私人上传</el-radio-button>
        </el-radio-group>
    </div>
    <div style="margin: 15px;">
        <el-button type="primary" plain @click="flush">刷新</el-button>
    </div>
    <div v-if="showchange == 1" style="margin: 15px;">
        <el-button type="primary" plain @click="tanchu">添加</el-button>
    </div>
    <!-- 分页 -->
    <div class="action-bar">
        <div style="margin:10px 20px;">
            <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next"
                :page-count="pages" @current-change="handleCurrentChange" />
        </div>
    </div>
    <!-- 流浪猫狗列表 -->
    <div class="animal-cards">
        <div class="animal-card" v-for="pet in pets" :key="pet.id">
            <router-link :to="`/petdetail/${pet.id}`">
                <div class="animal-image">
                    <img :src="getImageUrl(pet.photo)" alt="Animal Image" />
                </div>
            </router-link>
            <div class="animal-details">
                <h3>{{ pet.petname }}</h3>
                <!-- <span>
                    <el-tag v-if="pet.isAdopt == 1" type="success">已领养</el-tag>
                    <el-tag v-else-if="pet.isAdopt == 0" type="warning">流浪中</el-tag>
                    <el-tag v-else>领养审核中</el-tag>
                </span> -->
            </div>
        </div>
    </div>
    <!-- 私人添加弹层 -->
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
                <div style="margin-left: 25px;">
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
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue';
import axios from 'axios'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus';

onMounted(() => {
    petsget()
})

//#region 条件查询
//筛选
const classic = ref(null)
function classicchange() {
    showchange.value = null
    conditionquery('classic', classic.value)
}
//切换
const showchange = ref(null)
function typechange() {
    classic.value = null
    conditionquery('type', showchange.value)
}
//搜索信息
const searchinfo = reactive({
    col: null,
    con: null
})
//模糊搜索
function search() {
    conditionquery(searchinfo.col, searchinfo.con)
}
//刷新
function flush() {
    petsget()
    searchinfo.col = null
    searchinfo.con = null
    showchange.value = null
    classic.value = null
}
//条件查询函数
function conditionquery(column, condition) {
    axios.get(`/pet/${currentpage.value}/${column}/${condition}`)
        .then(res => {
            if (res.data.code == 200) {
                pets.value = res.data.data.records
                pages.value = res.data.data.pages
            } else {
                ElMessage.error(res.data.msg)
            }
        }).catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 分页查找
const currentpage = ref(1)
const pages = ref(0)
const pets = ref([])
function petsget() {
    axios.get(`pet/page/${currentpage.value}`)
        .then(res => {
            if (res.data.code == 200) {
                pets.value = res.data.data.records
                pages.value = res.data.data.pages
            } else ElMessage.warning(res.data.msg)
        }).catch(err => {
            console.error(err);
        })
}
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) petsget()
    else search()
}

//#endregion

//当前登录对象信息
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))

//#region 用户添加私人上传
function tanchu() {
    if (currentUser != null) {
        if (currentUser.comPermiss) {
            dialogFormVisible.value = true
            petinfo.uid = currentUser.id
        } else {
            ElMessage.warning('由于你的违规权限已被限制！')
        }
    } else {
        ElMessage.warning('对不起你还未登录....请登录账户')
    }
}
const petinfo = reactive({
    petname: null,
    age: null,
    sex: null,
    photo: null,
    classic: null,
    variety: null,
    isVaccine: null,
    describtion: null,
    type: 1,
    uid: null
})
const dialogFormVisible = ref(false)
function addpet() {
    uploadRef.value.submit()
}
function cancelclear() {
    dialogFormVisible.value = false
    petinfo.petname = null,
        petinfo.age = null,
        petinfo.sex = null,
        petinfo.photo = null,
        petinfo.classic = null,
        petinfo.variety = null,
        petinfo.isVaccine = null,
        petinfo.describtion = null
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

function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }

</script>

<style scoped>
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

<style scoped>
.action-bar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-bottom: 20px;
}

.animal-cards {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
}

.animal-card {
    width: 300px;
    height: 350px;
    margin: 10px;
    border: 1px solid #ccc;
    border-radius: 8px;
    padding: 10px;
    background-color: #f7f7f7;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.animal-card:hover {
    transform: translateY(-5px);
}


.animal-image img {
    width: 300px;
    height: 215px;
    border-radius: 4px;
}

.animal-details {
    margin-top: 10px;
}

.animal-details h3 {
    font-size: 18px;
    margin-bottom: 5px;
}
</style>


