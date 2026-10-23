<template>
  <div class="container">
    <div class="center">
      <div class="login">
        <div class="brand-row">
          <img src="/logo.png" alt="logo" class="brand-logo" />
          <div class="title">猫狗之家后台管理系统</div>
        </div>
        <div class="inputtext"><input ref="name" v-model="adminname" placeholder="管理员账号" type="text" class="input"
            autofocus></div>
        <div class="inputtext"><input v-model="password" placeholder="管理员密码" type="password" class="input"
            @keyup.enter="login"></div>
        <div class="inputtext">
          <el-button @click="login" class="login-btn">登录</el-button>
        </div>
        <div class="tip">仅管理员可登录 · 与用户端账号体系相互独立</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>


import { ElMessage } from "element-plus";
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const router = useRouter();
const adminname = ref('')
const password = ref('')

const name = ref()

function clear() {
  adminname.value = ''
  password.value = ''
  name.value.focus()
}

function login() {
  axios.post('/admin/login', {
    adminname: adminname.value,
    password: password.value
  }).then((res) => {
    if (res.data.code == 200) {
      // 保存令牌（后续 /admin/** 请求的鉴权头）与账号名（Header 展示用）
      sessionStorage.setItem('adminToken', res.data.data.token)
      sessionStorage.setItem('adminname', res.data.data.adminname)
      router.push('/home/center')
      ElMessage.success(res.data.msg)
    } else {
      ElMessage.error(res.data.msg)
      clear()
    }
  }).catch(() => { /* 网络异常已由全局拦截器统一提示 */ })
}
</script>

<style scoped>
/* ===== 全屏背景：与用户端登录页同一套品牌渐变 ===== */
.container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(640px 360px at 10% 6%, var(--brand-200) 0%, transparent 60%),
    radial-gradient(560px 320px at 90% 94%, #ffedd8 0%, transparent 62%),
    linear-gradient(160deg, #fffaf6 0%, var(--bg) 70%);
}

/* 居中交由 flex 完成，替代原先的绝对定位写法 */
.center {
  width: 100%;
  display: flex;
  justify-content: center;
}

/* ===== 登录卡：白卡片 + 品牌细节 ===== */
.login {
  width: 420px;
  max-width: calc(100vw - 32px);
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  padding: 40px 40px 28px;
  box-sizing: border-box;
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 30px;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 10px;
}

.title {
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--ink-1);
  letter-spacing: .5px;
}

.inputtext {
  display: flex;
  justify-content: center;
  padding-top: 14px;
}

/* ===== 输入框：描边 + 品牌 focus 环，替换原先的拟物风 ===== */
.input {
  width: 100%;
  line-height: 24px;
  padding: 10px 14px;
  box-sizing: border-box;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  outline: none;
  background-color: #fafbfe;
  color: var(--ink-1);
  font-size: 14px;
  transition: border-color .2s ease, background-color .2s ease, box-shadow .2s ease;
}

.input:focus {
  background-color: #fff;
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-100);
}

.input::placeholder {
  color: var(--ink-3);
}

/* ===== 登录按钮：品牌橙 ===== */
.login-btn {
  width: 100%;
  height: 42px;
  margin-top: 22px;
  font-weight: 600;
  font-size: 15px;
  color: #fff;
  background-color: var(--brand);
  border: none;
  border-radius: var(--radius-sm);
  transition: background-color .2s ease, box-shadow .2s ease;
}

.login-btn:hover {
  background-color: var(--brand-600);
  box-shadow: 0 6px 16px rgba(255, 107, 53, .35);
}

/* 底部提示 */
.tip {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: var(--ink-3);
}
</style>
