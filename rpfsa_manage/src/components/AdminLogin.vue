<template>
  <div class="container">
    <div class="center">
      <div class="login">
        <div class="title">猫狗之家后台管理系统</div>
        <div class="inputtext"><input ref="name" v-model="adminname" placeholder="管理员账号" type="text" class="input"
            autofocus></div>
        <div class="inputtext"><input v-model="password" placeholder="管理员密码" type="password" class="input"
            @keyup.enter="login"></div>
        <div class="inputtext">
          <el-button @click="login">登录</el-button>
        </div>
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
      sessionStorage.setItem('adminname', adminname.value)
      router.push('/home/center')
      ElMessage.success(res.data.msg)
    } else {
      ElMessage.error(res.data.msg)
      clear()
    }
  }).catch(() => ElMessage.error('对不起,服务器检修中,请稍后重试~_~!'))
}
</script>
  
<style scoped>
.container {
  width: 100vw;
  height: 100vh;
  background-image: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
}

/*设置在页面居中效果*/
.center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.login {
  width: 400px;
  height: 300px;
  /*border-radius: 5%;*/
  /*background-image: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);*/
  border-radius: 45px;
  background: linear-gradient(315deg, #64b8be, #77dae2);
  box-shadow: -24px -24px 49px #5ca9af,
    24px 24px 49px #82eff7;
}

.title {
  text-align: center;
  font-size: 25px;
  padding-top: 25px;
  padding-bottom: 25px;
}

.inputtext {
  text-align: center;
  padding-top: 15px;
}

.input {
  line-height: 28px;
  border: 2px solid transparent;
  border-bottom-color: #3f3d3d;
  padding: .2rem 0;
  outline: none;
  background-color: transparent;
  color: #0d0c22;
  transition: .3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.input:focus,
input:hover {
  outline: none;
  padding: .2rem 1rem;
  border-radius: 1rem;
  border-color: #4e0db6;
}

.input::placeholder {
  color: #182322;
}

.input:focus::placeholder {
  opacity: 0;
  transition: opacity .3s;
}

button {
  font-family: monospace;
  background-color: #ffffff;
  color: #050e0f;
  border: none;
  border-radius: 8px;
  width: 80px;
  height: 35px;
  transition: .3s;
}

button:hover {
  background-color: #3b82f6;
  box-shadow: 0 0 0 5px #3b83f65f;
  color: #fff;
}
</style>