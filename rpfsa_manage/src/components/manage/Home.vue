<template>
    <div class="common-layout">
        <el-container>
            <el-aside width="200px" class="aside-shell">
                <Aside></Aside>
            </el-aside>
            <el-container>
                <el-header class="header-shell" style="padding: 0 0 0 0;">
                    <Header></Header>
                </el-header>
                <el-main class="main-shell">
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

<style>
/* ===== 后台布局壳：深色侧栏 + 浅灰工作区 + 充分留白 ===== */
.aside-shell {
    /* el-aside 自带滚动与高度，交给内部菜单处理 */
    transition: width .2s ease;
}

.header-shell {
    box-shadow: 0 1px 4px rgba(31, 37, 51, .08);
    z-index: 10;
}

.main-shell {
    padding: 20px !important;
    background: var(--bg);
}

/* 小屏：侧栏收窄，内容区边距缩小 */
@media (max-width: 768px) {
    .main-shell {
        padding: 12px !important;
    }
}
</style>