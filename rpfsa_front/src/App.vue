<template>
  <router-view v-if="isactive"></router-view>
</template>
<script setup>
import { nextTick, provide, ref } from 'vue';

//#region 页面快速刷新 
const isactive = ref(true)
provide('reload', reload)

function reload() {
  isactive.value = false
  nextTick(() => {
    isactive.value = true
  })
}
//#endregion

</script>



<style scoped></style>
