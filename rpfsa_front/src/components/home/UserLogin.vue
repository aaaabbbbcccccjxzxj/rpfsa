<template>
    <div><el-button type="success" @click="goback">返回网站</el-button></div>
    <!-- 登录div -->
    <div v-show="isdisplay">
        <div class="lr-page">
            <div class="lr-card">
                <h2>流浪猫狗之家</h2>
                <form class="lr-form">
                    <div class="form-group">
                        <label>用户名:</label>&nbsp
                        <input v-model="user.username" type="text" placeholder="请输入用户名" required>
                    </div>
                    <div class="form-group">
                        <label>密码:</label>&nbsp&nbsp&nbsp&nbsp
                        <input v-model="user.password" type="password" placeholder="请输入密码" @keyup.enter="login" required />
                    </div>
                    <!-- 在表单内的button的type默认为submit,如果与其他元素共同触发同一个函数,会引起多次提交,所有要修改类型 -->
                    <button type="button" class="lr-button" @click="login">登录</button>
                    <el-link type="primary" @click="isdisplay = false">去注册</el-link>
                </form>
            </div>
        </div>
    </div>
    <!-- 注册div -->
    <div v-show="!isdisplay">
        <div class="lr-page">
            <div class="lr-card">
                <h2>流浪猫狗之家</h2>
                <el-form ref="ruleFormRef" class="lr-form" label-width="90px" label-position="right" status-icon
                    :rules="rules" :model="user">
                    <el-form-item label="用户名:" prop="username">
                        <el-input v-model="user.username" type="text" id="name" placeholder="请输入用户名" />
                    </el-form-item>

                    <el-form-item label="手机号:" prop="phone">
                        <el-input v-model="user.phone" type="text" placeholder="请输入手机号" />
                    </el-form-item>

                    <!-- <el-form-item label="邮箱:" prop="email">
                        <el-input v-model="user.email" type="email" placeholder="请输入邮箱" />
                    </el-form-item> -->

                    <el-form-item label="密码:" prop="password">
                        <el-input v-model="user.password" type="password" placeholder="请输入密码" />
                    </el-form-item>

                    <el-form-item label="确认密码:" prop="confpassword">
                        <el-input v-model="user.confpassword" type="password" placeholder="确认密码" />
                    </el-form-item>

                    <button class="lr-button" @click="registerandlogin(ruleFormRef)">注册</button>
                    <el-link type="primary" @click="isdisplay = true">返回登录</el-link>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus';

//#region 返回网站
const router = useRouter()
function goback() {
    router.push('/')
}
//#endregion

//#region 注册表单检测规则
let validPassword = (rule, value, callback) => {
    let reg = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{4,20}$/
    if (!reg.test(value)) {
        callback(new Error('密码必须是由4-20位字母+数字组合'))
    } else {
        callback()
    }
};

var validConfPassowrd = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
        // password 是表单上绑定的字段
    } else if (value !== user.password) {
        callback(new Error('两次输入密码不一致!'))
    } else {
        callback()
    }
}
var validUsername = (rule, value, callback) => {
    if (value) {
        if (/[\u4E00-\u9FA5]/g.test(value)) {
            callback(new Error("不能输入汉字"));
        } else {
            // 验证通过
            callback();
        }
        callback();
    }
}
const rules = reactive({
    username: [
        { required: true, message: '用户名不能为空！', trigger: 'blur' },
        { validator: validUsername, trigger: 'blur' }
    ],
    password: [
        { required: true, message: '密码不能为空！', trigger: 'blur' },
        { validator: validPassword, trigger: 'blur' }
    ],
    confpassword: [
        { required: true, validator: validConfPassowrd, trigger: 'change' }
    ],
    phone: [
        { required: true, message: '手机号不能为空！', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
    ],
    // email: [
    //     { type: "string", required: true, message: '请输入邮箱地址', trigger: 'blur' },
    //     { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
    // ]
})
//#endregion

const ruleFormRef = ref(null)
const isdisplay = ref(true)
const user = reactive({
    username: '',
    password: '',
    confpassword: '',
    phone: '',
})

//#region 用户登录
function login() {
    if ((user.username == '') || user.password == '') {
        ElMessage.warning('输入不能为空！')
    } else {
        axios.post('/user/login', user).then(res => {
            if (res.data.code == 200) {
                ElMessage.success(res.data.msg)
                setTimeout(() => {
                    sessionStorage.setItem('user', JSON.stringify(res.data.data))
                    router.push('/')
                }, 1500);
            } else ElMessage.error(res.data.msg)
        }).catch(() => ElMessage.error('对不起，服务器检修中，稍后再试~.~!'))
    }
}
//#endregion

//#region 用户注册
function registerandlogin(formEl) {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            axios.post('/user', user).then(res => {
                if (res.data.code == 200) {
                    ElMessage.success('注册成功！请登录')
                    isdisplay.value = true
                } else ElMessage.error(res.data.msg)
            }).catch(() => ElMessage.error('对不起，服务器检修中，稍后再试~.~!'))
        } else {
            ElMessage.warning('请完善注册表单！')
        }
    })
}
//#endregion

</script>
<style scoped>
.lr-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
}

.lr-card {
    max-width: 400px;
    padding: 40px;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    border-radius: 4px;
}

h2 {
    text-align: center;
    margin-bottom: 30px;
    font-size: 24px;
    color: #333;
}

.lr-form {
    /* display: ; */
    flex-direction: column;
}

.form-group {
    margin-bottom: 20px;
}

label {
    font-size: 14px;
    font-weight: bold;
    color: #555;
}

.lr-button {
    margin-left: 65px;
    margin-right: 5px;
    padding: 6px;
    background-color: #007bff;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
    width: 80px;
}

.lr-button:hover {
    background-color: #0056b3;
}

input[type="text"],
input[type="email"],
input[type="password"] {
    padding: 10px;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    color: #555;
    background-color: #f5f5f5;
    transition: background-color 0.3s;
}

input[type="text"]:focus,
input[type="email"]:focus,
input[type="password"]:focus {
    background-color: #e0e0e0;
    outline: none;
}
</style>
