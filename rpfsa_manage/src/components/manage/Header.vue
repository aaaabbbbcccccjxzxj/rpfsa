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
.grzx {
  font-size: 20px;
  margin-top: 20px;
  color: rgb(155, 143, 147);
  border: none;
}

.head {
  height: 60px;
  line-height: 60px;
  background-color: #2c3346;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.l-s {
  margin-left: 10px;
}

.r-p {
  margin-right: 20px;
}

.r-p img {
  margin-top: 10px;
  height: 40px;
  width: 40px;
  border-radius: 50%;

}

.example-showcase .el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}

a {
  text-decoration: none;
}
</style>