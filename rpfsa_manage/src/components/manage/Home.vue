<template>
    <div class="common-layout">
        <el-container>
            <el-aside width="200px">
                <Aside></Aside>
            </el-aside>
            <el-container>
                <el-header style="padding: 0 0 0 0;">
                    <Header></Header>
                </el-header>
                <el-main style="padding: 5px 0 0 5px;">
                    <router-view></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>
<script setup>
import Aside from './Aside.vue';
import Header from './Header.vue';


</script>

<style></style>