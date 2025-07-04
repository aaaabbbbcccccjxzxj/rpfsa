<template>
    <!-- 添加 -->
    <el-button type="primary" plain @click="tanchu" style="margin-right: 30px;">
        添加救助品
    </el-button>
    <!-- 救助信息弹层 -->
    <el-dialog v-model="dialogFormVisible" title="填写救助品信息" style="border-radius: 20px; width: 600px;">
        <el-form label-position="left" label-width="80px" :model="foodinfo" style="max-width: 450px">
            <el-form-item class="efi" label="名字:">
                <el-input v-model="foodinfo.foodname" />
            </el-form-item>
            <el-form-item class="efi" label="库存:">
                <el-input v-model="foodinfo.inventory" type="number" />
            </el-form-item>
            <el-form-item class="efi" label="价格:">
                <el-input v-model="foodinfo.price" type="number" />
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
            </el-form-item>
        </el-form>
        <template #footer>
            <span>
                <el-button @click="cancelclear">取消</el-button>
                <el-button type="primary" @click="addfood">添加</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- 表格数据 -->
    <el-table :data="tableData" style="width: 100%;padding-top: 5px;" height="570" stripe highlight-current-row>
        <el-table-column prop="foodname" label="名字" width="150" align="center"></el-table-column>
        <el-table-column label="图片" width="150" align="center">
            <template #="scope">
                <el-image :src="`http://localhost/img/${scope.row.picture}`" />
            </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="150" align="center"></el-table-column>
        <el-table-column prop="inventory" label="库存" width="180" align="center"></el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #="scope">
                <el-button type="primary" @click="editfood(scope.row)">修改</el-button>
                <el-button type="danger" :icon="Delete" @click="deletefood(scope.row.id)" />
            </template>
        </el-table-column>
    </el-table>
    <!-- 修改信息弹层 -->
    <el-dialog v-model="dialogeditVisible" title="修改信息" style="border-radius: 20px; width: 600px;">
        <el-form label-position="left" label-width="80px" :model="copyfood.value" style="max-width: 450px">
            <el-form-item class="efi" label="名字:">
                <el-input v-model="copyfood.value.foodname" />
            </el-form-item>
            <el-form-item class="efi" label="库存:">
                <el-input v-model="copyfood.value.inventory" type="number" />
            </el-form-item>
            <el-form-item class="efi" label="价格:">
                <el-input v-model="copyfood.value.price" type="number" />
            </el-form-item>
            <el-form-item class="efi" label="照片:">
                <el-upload ref="updateRef" class="avatar-uploader" action="http://localhost/upload" :show-file-list="false"
                    :on-success="updateAvatarSuccess" :on-change="changeAvatar" :auto-upload="false"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <img v-else :src="`http://localhost/img/${copyfood.value.picture}`" class="avatar" />
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <span>
                <el-button @click="clearcopy">取消</el-button>
                <el-button type="primary" @click="editconfirm">修改</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import axios from 'axios'
import { reactive, ref, onMounted } from 'vue';
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

onMounted(() => {
    foodget()
})

//#region 获取救助品列表
const tableData = ref([])
function foodget() {
    axios.get(`/food`)
        .then(res => {
            if (res.data.code == 200) tableData.value = res.data.data
            else ElMessage.warning(res.data.msg)
        }).catch(err => {
            console.error(err);
        })
}
//#endregion

//#region 修改
const dialogeditVisible = ref(false)
const copyfood = reactive({})
//打开弹层
function editfood(info) {
    copyfood.value = info
    dialogeditVisible.value = true
}
function editconfirm() {
    // 执行上传图片函数
    if (!imageUrl.value) {
        // 不执行上传图片
        updatefood()
    } else {
        updateRef.value.submit()
    }
}
// 取消
function clearcopy() {
    dialogeditVisible.value = false
    imageUrl.value = null
}

const updateRef = ref(null)

function updateAvatarSuccess(response) {
    copyfood.value.picture = response
    updatefood()
}
function updatefood() {
    axios.patch('/food', copyfood.value)
        .then(res => {
            if (res.data.code == 200) ElMessage.success(res.data.msg)
            else ElMessage.error(res.data.msg)
        }).catch(err => {
            ElMessage.error('服务器故障！')
        }).finally(() => {
            foodget()
            clearcopy()
        })
}
//#endregion

//#region 删除
function deletefood(foodid) {
    ElMessageBox.confirm('你确定要删除吗？该操作不可撤回哦!', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        axios.delete(`/food/${foodid}`).then((res) => {
            if (res.data.code == 200) ElMessage.success(res.data.msg)
            else ElMessage.error(res.data.msg)
        })
        foodget()
    }).catch(() => {
        ElMessage.info('已取消')
    })
}
//#endregion

//#region 添加猫狗
function tanchu() {
    dialogFormVisible.value = true;
}

const dialogFormVisible = ref(false)
const foodinfo = reactive({
    foodname: '',
    inventory: 0,
    price: 0,
    picture: '',
})

function cancelclear() {
    dialogFormVisible.value = false
    foodinfo.foodname = '',
        foodinfo.inventory = 0,
        foodinfo.price = 0,
        foodinfo.picture = '',
        imageUrl.value = null
}

function addfood() {
    uploadRef.value.submit()
}

//上传图片
const imageUrl = ref()
const uploadRef = ref(null)
function handleAvatarSuccess(response) {
    foodinfo.picture = response
    axios.post('/food', foodinfo)
        .then(res => {
            if (res.data.code == 200) ElMessage.success(res.data.msg)
            else ElMessage.error(res.data.msg)
        }).catch(err => {
            ElMessage.error('服务器故障')
        }).finally(() => {
            foodget()
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
    width: 120px;
    height: 140px;
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
    width: 120px;
    height: 140px;
    text-align: center;
}
</style>