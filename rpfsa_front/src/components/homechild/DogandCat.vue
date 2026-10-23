<template>
    <div class="page-container">
        <!-- 工具栏：关键词 + 分类 + 来源 + 组合筛选收进卡片 -->
        <div class="toolbar soft-card">
            <el-input v-model="keyword" placeholder="输入名字或品种关键词" class="search-input" clearable
                @keyup.enter="applyFilter" @clear="applyFilter">
                <template #append>
                    <el-button @click="applyFilter">搜索</el-button>
                </template>
            </el-input>
            <el-radio-group v-model="filters.classic" @change="applyFilter" class="seg">
                <el-radio-button :label="null">全部</el-radio-button>
                <el-radio-button :label="0">小狗</el-radio-button>
                <el-radio-button :label="1">小猫</el-radio-button>
                <el-radio-button :label="2">其他</el-radio-button>
            </el-radio-group>
            <el-radio-group v-model="filters.type" @change="applyFilter" class="seg">
                <el-radio-button :label="null">全部来源</el-radio-button>
                <el-radio-button :label="0">平台流浪</el-radio-button>
                <el-radio-button :label="1">私人上传</el-radio-button>
            </el-radio-group>
            <div class="spacer"></div>
            <el-button v-if="filters.type === 1" type="primary" plain @click="tanchu">添加</el-button>
            <el-button @click="flush">重置</el-button>
        </div>
        <!-- 组合筛选行 -->
        <div class="filter-row soft-card">
            <span class="filter-label">性别</span>
            <el-select v-model="filters.sex" placeholder="不限" clearable class="filter-select" @change="applyFilter">
                <el-option label="公" :value="1" />
                <el-option label="母" :value="0" />
                <el-option label="不详" :value="2" />
            </el-select>
            <span class="filter-label">年龄</span>
            <el-select v-model="filters.ageRange" placeholder="不限" clearable class="filter-select" @change="applyFilter">
                <el-option label="1 岁以下" :value="[0, 1]" />
                <el-option label="1~3 岁" :value="[1, 3]" />
                <el-option label="3~5 岁" :value="[3, 5]" />
                <el-option label="5 岁以上" :value="[5, 99]" />
            </el-select>
            <span class="filter-label">疫苗</span>
            <el-select v-model="filters.isVaccine" placeholder="不限" clearable class="filter-select" @change="applyFilter">
                <el-option label="已接种" :value="1" />
                <el-option label="未接种" :value="0" />
            </el-select>
            <span class="filter-label">状态</span>
            <el-select v-model="filters.isAdopt" placeholder="不限" clearable class="filter-select" @change="applyFilter">
                <el-option label="流浪中" :value="0" />
                <el-option label="已领养" :value="1" />
                <el-option label="审核中" :value="2" />
            </el-select>
        </div>
        <!-- 分页 -->
        <div class="action-bar">
            <el-pagination v-model:current-page="currentpage" :background="true" layout="prev, pager, next"
                :page-count="pages" @current-change="handleCurrentChange" />
        </div>
        <!-- 流浪猫狗列表 -->
        <div class="animal-cards">
            <div class="animal-card soft-card" v-for="pet in pets" :key="pet.id">
                <router-link :to="`/petdetail/${pet.id}`" class="animal-link">
                    <div class="animal-image">
                        <img :src="resolveImg(pet.photo)" alt="Animal Image" />
                    </div>
                </router-link>
                <div class="animal-details">
                    <h3>{{ pet.petname }}</h3>
                    <span>
                        <el-tag v-if="pet.isAdopt == 1" type="success" size="small" round>已领养</el-tag>
                        <el-tag v-else-if="pet.isAdopt == 0" type="warning" size="small" round>流浪中</el-tag>
                        <el-tag v-else size="small" round>审核中</el-tag>
                    </span>
                </div>
            </div>
        </div>
        <div v-if="pets.length === 0" class="empty-state">暂无符合条件的猫狗，换个条件试试～</div>
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
                <el-upload ref="uploadRef" class="avatar-uploader" :action="uploadUrl" :headers="uploadHeaders()"
                    :on-error="uploadError" :show-file-list="false" :on-success="handleAvatarSuccess" :on-change="changeAvatar"
                    :auto-upload="false" :before-upload="beforeAvatarUpload">
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
    </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue';
import axios from 'axios'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus';
import { resolveImg, resolveUploadUrl, uploadHeaders, uploadError } from '@/utils/url';

// 上传地址与图片地址统一从这里取，不再硬编码 http://localhost
const uploadUrl = resolveUploadUrl()

onMounted(() => {
    petsget()
})

//#region 组合筛选（走后端 /pet/filter，所有条件服务端组装）
const keyword = ref('')
const filters = reactive({
    classic: null,
    type: null,
    sex: null,
    ageRange: null,
    isVaccine: null,
    isAdopt: null
})

function applyFilter() {
    currentpage.value = 1
    loadPets()
}

function flush() {
    keyword.value = ''
    filters.classic = null
    filters.type = null
    filters.sex = null
    filters.ageRange = null
    filters.isVaccine = null
    filters.isAdopt = null
    currentpage.value = 1
    loadPets()
}
//#endregion

//#region 分页查找
const currentpage = ref(1)
const pages = ref(0)
const pets = ref([])

function loadPets() {
    const params = { current: currentpage.value }
    if (keyword.value) params.keyword = keyword.value
    if (filters.classic !== null) params.classic = filters.classic
    if (filters.type !== null) params.type = filters.type
    if (filters.sex !== null) params.sex = filters.sex
    if (filters.isVaccine !== null) params.isVaccine = filters.isVaccine
    if (filters.isAdopt !== null) params.isAdopt = filters.isAdopt
    if (filters.ageRange) {
        params.minAge = filters.ageRange[0]
        params.maxAge = filters.ageRange[1]
    }
    axios.get('/pet/filter', { params })
        .then(res => {
            if (res.data.code == 200) {
                pets.value = res.data.data.records
                pages.value = res.data.data.pages
            } else {
                pets.value = []
                pages.value = 0
            }
        }).catch(() => {})
}

function petsget() {
    loadPets()
}

function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    loadPets()
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
/* ===== 工具栏：一条卡片装下全部筛选与操作 ===== */
.toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    padding: 14px 16px;
    margin-bottom: 20px;
}

.search-input {
    width: 320px;
    max-width: 100%;
}

.seg {
    flex-shrink: 0;
}

.spacer {
    flex: 1;
}

/* ===== 组合筛选行 ===== */
.filter-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    margin-top: -8px;
    margin-bottom: 20px;
}

.filter-label {
    font-size: 13px;
    color: var(--ink-3);
    font-weight: 600;
}

.filter-select {
    width: 110px;
}

.action-bar {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 22px;
}

/* ===== 宠物卡片：栅格自适应，替代原先固定 300px 宽的 flex ===== */
.animal-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
}

.animal-card {
    padding: 10px;
    cursor: pointer;
}

.animal-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow);
}

.animal-link {
    display: block;
    text-decoration: none;
}

.animal-image {
    border-radius: var(--radius-sm);
    overflow: hidden;
    background: #f1f3f9;
}

.animal-image img {
    width: 100%;
    height: 190px;
    object-fit: cover;
    display: block;
    transition: transform .3s ease;
}

.animal-card:hover .animal-image img {
    transform: scale(1.04);
}

.animal-details {
    margin-top: 10px;
    padding: 0 4px 4px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.animal-details h3 {
    font-size: 16px;
    margin: 0;
    color: var(--ink-1);
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* ===== 小屏：卡片更密、图片更矮 ===== */
@media (max-width: 576px) {
    .animal-cards {
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        gap: 12px;
    }

    .animal-image img {
        height: 140px;
    }

    .toolbar {
        padding: 12px;
    }
}
</style>


