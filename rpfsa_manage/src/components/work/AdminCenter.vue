<template>
    <div class="dash fade-up">
        <!-- ===== 统计数字卡 ===== -->
        <div class="stat-grid">
            <div class="stat-card stat-orange">
                <div class="stat-icon">👥</div>
                <div class="stat-body">
                    <div class="stat-num">{{ allinfo.userCount ?? '-' }}</div>
                    <div class="stat-name">已注册用户（人）</div>
                </div>
            </div>
            <div class="stat-card stat-blue">
                <div class="stat-icon">🐾</div>
                <div class="stat-body">
                    <div class="stat-num">{{ allinfo.petCount ?? '-' }}</div>
                    <div class="stat-name">已登记猫狗（只）</div>
                </div>
            </div>
            <div class="stat-card stat-green">
                <div class="stat-icon">📝</div>
                <div class="stat-body">
                    <div class="stat-num">{{ allinfo.postCount ?? '-' }}</div>
                    <div class="stat-name">社区帖子（条）</div>
                </div>
            </div>
            <div class="stat-card stat-purple">
                <div class="stat-icon">⏳</div>
                <div class="stat-body">
                    <div class="stat-num">{{ pendingTotal }}</div>
                    <div class="stat-name">待处理事项</div>
                </div>
            </div>
        </div>

        <!-- ===== 图表区 ===== -->
        <el-row :gutter="16">
            <el-col :span="12" :xs="24">
                <div class="panel chart-panel">
                    <div ref="statis" class="chart-box"></div>
                </div>
            </el-col>
            <el-col :span="12" :xs="24">
                <div class="panel chart-panel">
                    <div ref="pet" class="chart-box"></div>
                </div>
            </el-col>
        </el-row>

        <!-- ===== 趋势图 ===== -->
        <div class="panel chart-panel" style="margin-bottom: 16px;">
            <div ref="trend" class="chart-box chart-box-wide"></div>
        </div>

        <!-- ===== 公告区 ===== -->
        <div class="panel notice-panel">
            <div class="panel-head">
                <span class="panel-title">网站公告</span>
                <RouterLink to="/home/notice"><el-button text type="primary">更新网站公告 →</el-button></RouterLink>
            </div>
            <div v-for="o, index in webannounce.slice(0, 6)" :key="o.id" class="notice-row">
                <span class="notice-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <el-text truncated class="notice-title">{{ o.title }}</el-text>
                <span class="notice-time">{{ o.createTime }}</span>
            </div>
            <div v-if="webannounce.length === 0" class="notice-empty">暂无公告</div>
        </div>
    </div>
</template>
<script setup>
import axios from 'axios';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import * as echarts from 'echarts';

// 图表实例：组件卸载时必须销毁，否则画布与事件监听会一直残留
let petChart = null
let statisChart = null
let trendChart = null

onMounted(async () => {
    getallpost()
    await allinfoget()
    // 数据就绪后再初始化图表，替代原先"硬等 300ms"的做法
    petchartinit()
    statischartinit()
    trendchartinit()
})

onUnmounted(() => {
    if (petChart) {
        petChart.dispose()
    }
    if (statisChart) {
        statisChart.dispose()
    }
    if (trendChart) {
        trendChart.dispose()
    }
})

//#region 所有数据
const allinfo = ref({})
function allinfoget() {
    // 统计接口由 /admin 调整为 /admin/stats，并与用户管理接口一起纳入管理员角色校验
    return axios.get('/admin/stats').then((res) => {
        if (res.data.code == 200) {
            allinfo.value = res.data.data
        }
    })
}
// 待处理合计：未处理领养 + 未处理捐助 + 未处理消息
const pendingTotal = computed(() => {
    const v = allinfo.value
    const sum = (v.adoptCount || 0) + (v.donateCount || 0) + (v.messageCount || 0)
    return allinfo.value.adoptCount === undefined ? '-' : sum
})
//#endregion

//#region 猫狗统计数据
const pet = ref(null)
function petchartinit() {
    petChart = echarts.init(pet.value);
    //往这里用后端返回的代码就行了
    let option = {
        color: ['#ff8a50', '#5b8ff9', '#ffb54d', '#b37feb', '#2fbf71'],
        title: {
            text: `流浪猫狗类型统计`,
            left: 'center',
            textStyle: { fontSize: 15, color: '#1f2533' }
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left',
            textStyle: { color: '#4a5163' }
        },
        series: [
            {
                type: 'pie',
                radius: ['42%', '68%'],
                center: ['60%', '52%'],
                itemStyle: {
                    borderRadius: 6,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { color: '#4a5163' },
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
                        shadowColor: 'rgba(0, 0, 0, 0.3)'
                    }
                }
            }
        ]
    }
    petChart.setOption(option)
}
//#endregion

//#region 未处理信息统计
const statis = ref(null)
function statischartinit() {
    statisChart = echarts.init(statis.value);
    //往这里用后端返回的代码就行了
    let option = {
        title: {
            text: `统计与未处理信息`,
            left: 'center',
            textStyle: { fontSize: 15, color: '#1f2533' }
        },
        tooltip: {
            trigger: 'item'
        },
        grid: { left: 40, right: 24, top: 56, bottom: 32 },
        xAxis: {
            type: 'category',
            data: ['领养成功', '捐助成功', '未处理领养', '未处理捐助', '未处理消息'],
            axisLabel: { color: '#4a5163' }
        },
        yAxis: {
            type: 'value',
            axisLabel: { color: '#4a5163' },
            splitLine: { lineStyle: { color: '#eceef5' } }
        },
        series: [
            {
                data: [allinfo.value.haveAdoptCount, allinfo.value.haveDonateCount, allinfo.value.adoptCount, allinfo.value.donateCount, allinfo.value.messageCount],
                type: 'bar',
                barWidth: 34,
                itemStyle: {
                    borderRadius: [6, 6, 0, 0],
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: '#ff8a50' },
                            { offset: 1, color: '#f25b26' }
                        ]
                    }
                }
            }
        ]
    };
    statisChart.setOption(option)
}
//#endregion

//#region 近 6 个月运营趋势
const trend = ref(null)
function trendchartinit() {
    axios.get('/admin/trends').then(res => {
        if (res.data.code !== 200) return
        const t = res.data.data
        trendChart = echarts.init(trend.value)
        trendChart.setOption({
            color: ['#ff6b35', '#3b76e1', '#23a86a'],
            title: {
                text: '近 6 个月运营趋势',
                left: 'center',
                textStyle: { fontSize: 15, color: '#1f2533' }
            },
            tooltip: { trigger: 'axis' },
            legend: { bottom: 0, textStyle: { color: '#4a5163' } },
            grid: { left: 46, right: 30, top: 50, bottom: 46 },
            xAxis: {
                type: 'category',
                data: t.months,
                axisLabel: { color: '#4a5163' }
            },
            yAxis: {
                type: 'value',
                axisLabel: { color: '#4a5163' },
                splitLine: { lineStyle: { color: '#eceef5' } }
            },
            series: [
                {
                    name: '领养通过',
                    type: 'line',
                    smooth: true,
                    data: t.adoptCounts,
                    areaStyle: { opacity: .12 }
                },
                {
                    name: '新增用户',
                    type: 'line',
                    smooth: true,
                    data: t.userCounts,
                    areaStyle: { opacity: .12 }
                },
                {
                    name: '捐助宠宠币',
                    type: 'line',
                    smooth: true,
                    data: t.donateCoins,
                    areaStyle: { opacity: .12 }
                }
            ]
        })
    }).catch(() => {})
}
//#endregion

//#region 网站公告列表
const webannounce = ref([])
function getallpost() {
    axios.get('/post/alltypeone/0').then((res) => {
        webannounce.value = res.data.data
    })
}
//#endregion

</script>

<style scoped>
.dash {
    padding: 4px;
}

/* ===== 统计数字卡：四色渐变 ===== */
.stat-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 16px;
}

.stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 22px 24px;
    border-radius: var(--radius-lg);
    color: #fff;
    box-shadow: var(--shadow);
    transition: transform .22s ease, box-shadow .22s ease;
}

.stat-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
}

.stat-orange {
    background: linear-gradient(135deg, #ff8a50 0%, #f25b26 100%);
}

.stat-blue {
    background: linear-gradient(135deg, #69a4ff 0%, #3b76e1 100%);
}

.stat-green {
    background: linear-gradient(135deg, #4ed39a 0%, #23a86a 100%);
}

.stat-purple {
    background: linear-gradient(135deg, #b78af0 0%, #8b5cf6 100%);
}

.stat-icon {
    font-size: 34px;
    line-height: 1;
    width: 58px;
    height: 58px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, .2);
    border-radius: 16px;
    flex-shrink: 0;
}

.stat-num {
    font-size: 30px;
    font-weight: 700;
    line-height: 1.2;
}

.stat-name {
    font-size: 13px;
    opacity: .9;
    margin-top: 2px;
}

/* ===== 图表卡 ===== */
.chart-panel {
    padding: 18px;
}

.chart-box {
    height: 340px;
}

.chart-box-wide {
    height: 300px;
}

/* ===== 公告卡 ===== */
.notice-panel {
    padding: 6px 22px 14px;
}

.panel-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid var(--line);
    margin-bottom: 8px;
}

.panel-title {
    font-weight: 700;
    font-size: 16px;
    color: var(--ink-1);
}

.notice-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 11px 4px;
    border-bottom: 1px dashed var(--line);
    font-size: 14px;
}

.notice-row:last-child {
    border-bottom: none;
}

.notice-index {
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    font-size: 12px;
    font-weight: 700;
    border-radius: 6px;
    background: #eef0f6;
    color: var(--ink-2);
}

.notice-index.top {
    background: var(--brand-100);
    color: var(--brand-600);
}

.notice-title {
    flex: 1;
    min-width: 0;
    color: var(--ink-1);
}

.notice-time {
    flex-shrink: 0;
    font-size: 12px;
    color: var(--ink-3);
}

.notice-empty {
    padding: 24px 0;
    text-align: center;
    color: var(--ink-3);
    font-size: 13px;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
    .stat-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 576px) {
    .stat-grid {
        grid-template-columns: minmax(0, 1fr);
    }
}
</style>
