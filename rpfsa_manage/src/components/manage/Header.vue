<template>
  <div class="head">
    <div class="l-s"></div>
    <div class="r-p">
      <el-dropdown>
        <div class="grzx">{{ adminname }} </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/home/center"><el-dropdown-item>个人中心</el-dropdown-item></router-link>
            <el-dropdown-item @click="exit">退出</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const adminname = sessionStorage.getItem('adminname')
const router = useRouter()

function exit() {
  sessionStorage.clear()
  router.replace('/')
}
</script>

<style scoped>
/* ===== 顶栏：色值与侧栏统一（--panel-dark），头部弱化装饰 ===== */
.grzx {
  font-size: 15px;
  color: #d7dce8;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  height: 60px;
  padding: 0 14px;
  border-radius: 8px;
  outline: none;
  transition: background-color .2s ease, color .2s ease;
}

.grzx:hover {
  background-color: rgba(255, 255, 255, .08);
  color: #ffffff;
}

.head {
  height: 60px;
  background-color: var(--panel-dark);
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
}

.l-s {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 8px;
  color: #aeb4c5;
  font-size: 14px;
}

.l-s::before {
  content: '流浪猫狗之家 · 后台管理';
  letter-spacing: .5px;
}

.r-p {
  margin-right: 12px;
}

.r-p img {
  height: 40px;
  width: 40px;
  border-radius: 50%;
}

a {
  text-decoration: none;
}
</style>