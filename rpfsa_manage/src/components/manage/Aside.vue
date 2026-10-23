<template>
  <el-menu class="el-menu-vertical-demo" active-text-color="#ff9670" background-color="#252b3b" text-color="#aeb4c5"
    style="height: 100vh; border: none" :router="true">
    <!-- LOGO -->
    <div class="brand-row">
      <el-image src="/logo.png" style="width: 30px;height: 30px;border-radius: 8px;"></el-image>
      <b class="brand-title">猫狗之家后台</b>
    </div>
    <!-- 用户 -->
    <el-sub-menu index="1">
      <template #title>
        <el-icon>
          <UserFilled />
        </el-icon>
        用户管理
      </template>
      <el-menu-item index="/home/user">用户信息</el-menu-item>
    </el-sub-menu>
    <!-- 猫狗 -->
    <el-sub-menu index="2">
      <template #title>
        <el-icon>
          <Chicken />
        </el-icon>
        猫狗管理
      </template>
      <el-menu-item index="/home/pet">猫狗信息</el-menu-item>
    </el-sub-menu>
    <!-- 救助领养 -->
    <el-sub-menu index="3">
      <template #title>
        <el-icon>
          <setting />
        </el-icon>
        救助管理
      </template>
      <el-menu-item index="/home/adopt">领养管理</el-menu-item>
      <el-menu-item index="/home/food">救助品管理</el-menu-item>
      <el-menu-item index="/home/donate">救助记录</el-menu-item>
      <el-menu-item index="/home/recharge">充值记录</el-menu-item>
    </el-sub-menu>
    <!-- 帖子 -->
    <el-sub-menu index="4">
      <template #title>
        <el-icon>
          <Chicken />
        </el-icon>
        公告管理
      </template>
      <el-menu-item index="/home/notice">公告信息</el-menu-item>
      <el-menu-item index="/home/post">发帖信息</el-menu-item>
      <el-menu-item index="/home/comment">评论管理</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { Chicken, Setting, UserFilled } from '@element-plus/icons-vue'

</script>

<style>
/* ===== 深色侧栏：色值与 Header 统一（--panel-dark），激活色与品牌橙呼应 ===== */
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 100vh;
  background-color: var(--panel-dark);
}

/* 品牌行 */
.brand-row {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, .08);
}

.brand-title {
  font-size: 17px;
  color: #ffffff;
  letter-spacing: 1px;
  white-space: nowrap;
}

/* 菜单项：更柔和的 hover 与激活底色 */
.el-menu-vertical-demo .el-menu-item {
  margin: 4px 10px;
  border-radius: 8px;
  min-width: auto;
}

.el-menu-vertical-demo .el-menu-item:hover {
  background-color: rgba(255, 255, 255, .06);
}

.el-menu-vertical-demo .el-menu-item.is-active {
  background-color: rgba(255, 107, 53, .16);
  color: #ff9670 !important;
}

/* 子菜单弹层（折叠时）同样式 */
.el-menu--vertical .el-menu {
  border-radius: 8px;
}
</style>
