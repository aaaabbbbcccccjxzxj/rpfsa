<template>
    <!-- 第一行 -->
    <el-row style="padding-bottom: 10px;">
        <!-- 基本信息 -->
        <el-col :span="12" style="padding-right: 10px;">
            <el-card style="height: 320px;">
                <template #header>
                    <div class="card-header">
                        <span>基本信息统计</span>
                    </div>
                </template>
                <div class="text item">已注册用户人数:<span class="info">{{ allinfo.userCount }}人</span></div>
                <div class="text item">已登记猫狗总数:<span class="info">{{ allinfo.petCount }}只</span></div>
                <div class="text item">社区帖子总数&nbsp:<span class="info">{{ allinfo.postCount }}条</span></div>
            </el-card>
        </el-col>
        <!-- 统计 -->
        <el-col :span="12">
            <el-card style="height: 320px;">
                <div ref="statis" style="height: 300px"></div>
            </el-card>
        </el-col>
    </el-row>
    <!-- 第二行 -->
    <el-row>
        <!-- 猫狗统计 -->
        <el-col :span="13" style="padding-right: 10px;">
            <el-card>
                <div ref="pet" style="height: 380px"></div>
            </el-card>
        </el-col>
        <!-- 公告查看 -->
        <el-col :span="11">
            <el-card class="box-card">
                <template #header>
                    <div class="card-header">
                        <span>网站公告</span>
                        <RouterLink to="/home/notice"><el-button class="button" text>更新网站公告</el-button></RouterLink>
                    </div>
                </template>
                <div v-for="o, index in webannounce.slice(0, 6)" :key="o.id" class="text item">
                    <el-text truncated size="large" style="width: 300px;">{{ index + 1 + ':' + o.title }}</el-text>
                    <el-text size="small" style="margin-left: 120px;">{{ o.createTime }}</el-text>
                </div>
            </el-card>
        </el-col>
    </el-row>
</template>
<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import * as echarts from 'echarts';

onMounted(() => {
    allinfoget()
    getallpost()
})
setTimeout(() => {
    petchartinit()
    statischartinit()
}, 300)

//#region 所有数据
const allinfo = ref({})
function allinfoget() {
    axios.get('/admin').then((res) => {
        allinfo.value = res.data.data
    }).catch((err) => {
        console.log(err);
    })
}
//#endregion

//#region 猫狗统计数据
const pet = ref(null)
function petchartinit() {
    const myChart = echarts.init(pet.value);
    //往这里用后端返回的代码就行了
    let option = {
        title: {
            text: `流浪猫狗类型统计`,
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                type: 'pie',
                radius: '50%',
                data: [
                    { value: allinfo.value.type0petCount, name: '平台流浪' },
                    { value: allinfo.value.type1petCount, name: '私人上传' },
                    { value: allinfo.value.state0petCount, name: '流浪中' },
                    { value: allinfo.value.state2petCount, name: '审核中' },
                    { value: allinfo.value.state1petCount, name: '已领养' }
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    }
    myChart.setOption(option)
}
//#endregion

//#region 未处理信息统计
const statis = ref(null)
function statischartinit() {
    const myChart = echarts.init(statis.value);
    //往这里用后端返回的代码就行了
    let option = {
        title: {
            text: `统计与未处理信息`,
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        xAxis: {
            type: 'category',
            data: ['领养成功', '捐助成功', '未处理领养', '未处理捐助', '未处理消息']
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                data: [allinfo.value.haveAdoptCount, allinfo.value.haveDonateCount, allinfo.value.adoptCount, allinfo.value.donateCount, allinfo.value.messageCount],
                type: 'bar',
            }
        ]
    };
    myChart.setOption(option)
}
//#endregion

//#region 网站公告列表
const webannounce = ref([])
function getallpost() {
    axios.get('/post/alltypeone/0').then((res) => {
        webannounce.value = res.data.data
        console.log(webannounce.value);
    })
}
//#endregion

</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: larger;
}

.text {
    font-size: 18px;
}

.info {
    font-size: 20px;
    font-weight: bold;
    margin-left: 15px;
}

.item {
    margin-bottom: 31px;
}
</style>