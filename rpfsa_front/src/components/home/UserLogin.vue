<template>
    <div class="back-row"><el-button round @click="goback">← 返回网站</el-button></div>
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
                    <button type="button" class="lr-button" :disabled="loginLoading" @click="login">
                        {{ loginLoading ? '登录中...' : '登录' }}
                    </button>
                    <el-link type="primary" @click="isdisplay = false">去注册</el-link>
                    <el-link type="info" class="forgot-link" @click="forgotVisible = true">忘记密码？</el-link>
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

                    <button class="lr-button" :disabled="registerLoading" @click="registerandlogin(ruleFormRef)">
                        {{ registerLoading ? '注册中...' : '注册' }}
                    </button>
                    <el-link type="primary" @click="isdisplay = true">返回登录</el-link>
                </el-form>
            </div>
        </div>
    </div>
    <!-- 找回密码弹窗 -->
    <el-dialog v-model="forgotVisible" title="找回密码" width="440px" style="border-radius: 16px;">
        <el-steps :active="resetStep" simple finish-status="success" style="margin-bottom: 22px;">
            <el-step title="验证邮箱" />
            <el-step title="重置密码" />
        </el-steps>
        <!-- 第一步：邮箱 + 发验证码 -->
        <template v-if="resetStep === 0">
            <el-form label-position="top">
                <el-form-item label="注册邮箱">
                    <el-input v-model="resetForm.email" placeholder="请输入注册时填写的邮箱" />
                </el-form-item>
                <el-alert v-if="resetForm.echoCode" type="success" :closable="false" show-icon
                    :title="`演示模式：验证码为 ${resetForm.echoCode}（10 分钟内有效）`" style="margin-bottom: 12px;" />
            </el-form>
        </template>
        <!-- 第二步：验证码 + 新密码 -->
        <template v-else>
            <el-form label-position="top">
                <el-alert v-if="resetForm.echoCode" type="success" :closable="false" show-icon
                    :title="`演示模式：验证码为 ${resetForm.echoCode}（10 分钟内有效）`" style="margin-bottom: 14px;" />
                <el-form-item label="验证码">
                    <el-input v-model="resetForm.code" placeholder="请输入 6 位验证码" maxlength="6" />
                </el-form-item>
                <el-form-item label="新密码">
                    <el-input v-model="resetForm.newPassword" type="password" placeholder="6~32 位新密码" show-password />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <template v-if="resetStep === 0">
                <el-button @click="forgotVisible = false">取消</el-button>
                <el-button type="primary" :loading="codeSending" @click="sendCode">获取验证码</el-button>
            </template>
            <template v-else>
                <el-button @click="resetStep = 0">上一步</el-button>
                <el-button type="primary" :loading="resetting" @click="resetPassword">确认重置</el-button>
            </template>
        </template>
    </el-dialog>
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

//#region 用户登录（loginLoading 防重复提交）
const loginLoading = ref(false)
function login() {
    if (loginLoading.value) return
    if ((user.username == '') || user.password == '') {
        ElMessage.warning('输入不能为空！')
        return
    }
    loginLoading.value = true
    axios.post('/user/login', { username: user.username, password: user.password }).then(res => {
        if (res.data.code == 200) {
            ElMessage.success(res.data.msg)
            // 只保存令牌与展示所需信息：后端返回的 user 已不含密码，
            // 令牌则用于后续请求的鉴权头（见 utils/request.js）
            sessionStorage.setItem('token', res.data.data.token)
            sessionStorage.setItem('user', JSON.stringify(res.data.data.user))
            setTimeout(() => router.push('/'), 1000)
        } else {
            ElMessage.error(res.data.msg)
        }
    }).catch(() => { /* 网络异常已由全局拦截器统一提示 */ })
        .finally(() => loginLoading.value = false)
}
//#endregion

//#region 用户注册（registerLoading 防重复提交）
const registerLoading = ref(false)
function registerandlogin(formEl) {
    if (!formEl || registerLoading.value) return
    formEl.validate((valid) => {
        if (valid) {
            registerLoading.value = true
            // 注册接口独立为 /user/register，且只提交白名单字段；
            // 权限位与宠宠币由服务端赋初值，前端不再（也无法）干预
            axios.post('/user/register', {
                username: user.username,
                password: user.password,
                phone: user.phone
            }).then(res => {
                if (res.data.code == 200) {
                    ElMessage.success('注册成功！请登录')
                    isdisplay.value = true
                } else {
                    ElMessage.error(res.data.msg)
                }
            }).catch(() => { /* 网络异常已由全局拦截器统一提示 */ })
                .finally(() => registerLoading.value = false)
        } else {
            ElMessage.warning('请完善注册表单！')
        }
    })
}
//#endregion

//#region 找回密码
const forgotVisible = ref(false)
const resetStep = ref(0)
const codeSending = ref(false)
const resetting = ref(false)
const resetForm = reactive({
    email: '',
    code: '',
    newPassword: '',
    echoCode: ''
})

function sendCode() {
    if (!resetForm.email) {
        ElMessage.warning('请输入注册邮箱')
        return
    }
    codeSending.value = true
    axios.post('/user/password/code', { email: resetForm.email }).then(res => {
        if (res.data.code === 200) {
            resetForm.echoCode = res.data.data.code
            ElMessage.success(res.data.msg)
            resetStep.value = 1
        } else {
            ElMessage.error(res.data.msg)
        }
    }).catch(() => {}).finally(() => codeSending.value = false)
}

function resetPassword() {
    if (!resetForm.code || !resetForm.newPassword) {
        ElMessage.warning('请填写验证码和新密码')
        return
    }
    resetting.value = true
    axios.post('/user/password/reset', {
        email: resetForm.email,
        code: resetForm.code,
        newPassword: resetForm.newPassword
    }).then(res => {
        if (res.data.code === 200) {
            ElMessage.success(res.data.msg)
            forgotVisible.value = false
            resetStep.value = 0
            resetForm.code = ''
            resetForm.newPassword = ''
            resetForm.echoCode = ''
        } else {
            ElMessage.error(res.data.msg)
        }
    }).catch(() => {}).finally(() => resetting.value = false)
}
//#endregion

</script>
<style scoped>
/* 返回按钮：固定在左上角 */
.back-row {
    position: fixed;
    top: 16px;
    left: 16px;
    z-index: 10;
}

/* ===== 全屏品牌背景：暖橙渐变 + 装饰光斑 ===== */
.lr-page {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    padding: 40px 16px;
    background:
        radial-gradient(600px 320px at 12% 8%, var(--brand-200) 0%, transparent 60%),
        radial-gradient(520px 300px at 88% 92%, #ffedd8 0%, transparent 62%),
        linear-gradient(160deg, #fffaf6 0%, var(--bg) 70%);
}

/* ===== 登录卡 ===== */
.lr-card {
    width: 100%;
    max-width: 420px;
    padding: 40px 36px;
    background-color: #fff;
    border: 1px solid var(--line);
    box-shadow: var(--shadow);
    border-radius: var(--radius-lg);
}

h2 {
    text-align: center;
    margin: 0 0 28px;
    font-size: 24px;
    color: var(--ink-1);
    letter-spacing: 1px;
}

.lr-form {
    flex-direction: column;
}

.form-group {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
}

label {
    font-size: 14px;
    font-weight: 600;
    color: var(--ink-2);
    flex-shrink: 0;
}

/* ===== 主按钮：品牌橙 + 悬浮上移 ===== */
.lr-button {
    display: inline-block;
    padding: 10px 28px;
    background-color: var(--brand);
    color: #fff;
    border: none;
    border-radius: var(--radius-sm);
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all .25s ease;
    margin: 4px 12px 4px 0;
}

.lr-button:hover {
    background-color: var(--brand-600);
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(255, 107, 53, .35);
}

.lr-button:active {
    transform: translateY(0);
}

.lr-button:disabled {
    opacity: .6;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

.forgot-link {
    margin-left: 12px;
}

/* ===== 原生输入框：统一品牌 focus 环 ===== */
input[type="text"],
input[type="email"],
input[type="password"] {
    flex: 1;
    padding: 10px 12px;
    border: 1px solid var(--line);
    border-radius: var(--radius-sm);
    font-size: 14px;
    color: var(--ink-1);
    background-color: #fafbfe;
    transition: border-color .2s ease, background-color .2s ease, box-shadow .2s ease;
}

input[type="text"]:focus,
input[type="email"]:focus,
input[type="password"]:focus {
    background-color: #fff;
    border-color: var(--brand);
    box-shadow: 0 0 0 3px var(--brand-100);
    outline: none;
}

@media (max-width: 480px) {
    .lr-card {
        padding: 28px 20px;
    }
}
</style>
