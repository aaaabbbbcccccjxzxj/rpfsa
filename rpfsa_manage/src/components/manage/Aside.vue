<template>
  <el-menu class="el-menu-vertical-demo" active-text-color="#ffd04b" background-color="#2f4050" text-color="#fff"
    style="height: 101vh  ;border:none" :router="true">
    <!-- LOGO -->
    <div style="height: 60px;line-height: 60px;text-align: center">
      <el-image src="/logo.png" style="width: 30px;height: 30px;position: relative;top: 7px;margin-right: 5px"></el-image>
      <b style="font-size: 20px;color: white">猫狗之家后台</b>
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
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { Chicken, Setting, UserFilled } from '@element-plus/icons-vue'

</script>

<style>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
</style>
