<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, Document, Cpu, ChatDotRound } from '@element-plus/icons-vue'
import StatCard from '../../components/StatCard.vue'
import { fetchOverview } from '../../api/stats'

const loading = ref(true)
const overview = ref({})

let charts = []

async function load() {
  loading.value = true
  try {
    const res = await fetchOverview()
    overview.value = res.data
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  disposeCharts()
  const qa = overview.value.qaByDay || []
  const reg = overview.value.userRegByDay || []
  const cats = overview.value.categoryDocShare || []

  const elLine = document.getElementById('chart-line')
  const elBar = document.getElementById('chart-bar')
  const elPie = document.getElementById('chart-pie')
  const elHBar = document.getElementById('chart-hbar')

  if (elLine) {
    const c = echarts.init(elLine)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 28, bottom: 28 },
      xAxis: { type: 'category', data: qa.map((x) => x.date), axisLine: { lineStyle: { color: '#94a3b8' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } } },
      series: [
        {
          name: '问答次数',
          type: 'line',
          smooth: true,
          data: qa.map((x) => x.count),
          areaStyle: { color: 'rgba(99,102,241,0.12)' },
          lineStyle: { width: 3, color: '#6366f1' },
        },
      ],
    })
    charts.push(c)
  }

  if (elBar) {
    const c = echarts.init(elBar)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 28, bottom: 28 },
      xAxis: { type: 'category', data: reg.map((x) => x.date) },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
      series: [
        {
          name: '新用户',
          type: 'bar',
          data: reg.map((x) => x.count),
          itemStyle: {
            borderRadius: [8, 8, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#a855f7' },
              { offset: 1, color: '#6366f1' },
            ]),
          },
        },
      ],
    })
    charts.push(c)
  }

  if (elPie) {
    const c = echarts.init(elPie)
    c.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: '68%',
          data: cats.map((x) => ({ name: x.name, value: x.value })),
          label: { formatter: '{b}\n{d}%' },
        },
      ],
    })
    charts.push(c)
  }

  if (elHBar) {
    const c = echarts.init(elHBar)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 100, right: 28, top: 20, bottom: 20 },
      xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
      yAxis: { type: 'category', data: cats.map((x) => x.name), axisLine: { show: false } },
      series: [
        {
          type: 'bar',
          data: cats.map((x) => x.value),
          itemStyle: {
            borderRadius: [0, 8, 8, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#6366f1' },
              { offset: 1, color: '#ec4899' },
            ]),
          },
        },
      ],
    })
    charts.push(c)
  }

  window.addEventListener('resize', resizeAll)
}

function resizeAll() {
  charts.forEach((c) => c.resize())
}

function disposeCharts() {
  window.removeEventListener('resize', resizeAll)
  charts.forEach((c) => c.dispose())
  charts = []
}

onMounted(load)
onBeforeUnmount(disposeCharts)
</script>

<template>
  <div class="dash" v-loading="loading">
    <div class="page-title">数据概览</div>
    <p class="sub">Java1234 RAG 企业知识库问答系统 · 实时监控</p>

    <el-row :gutter="16" class="cards">
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          label="用户总数"
          :value="overview.userTotal ?? 0"
          :icon="User"
          gradient="linear-gradient(135deg,#6366f1,#8b5cf6)"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          label="知识文档"
          :value="overview.documentTotal ?? 0"
          :icon="Document"
          gradient="linear-gradient(135deg,#0ea5e9,#6366f1)"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          label="向量片段"
          :value="overview.vectorTotal ?? 0"
          :icon="Cpu"
          gradient="linear-gradient(135deg,#10b981,#0ea5e9)"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          label="今日问答"
          :value="overview.qaToday ?? 0"
          :icon="ChatDotRound"
          gradient="linear-gradient(135deg,#f59e0b,#ec4899)"
        />
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>近 7 日 · 助手回答次数</span></template>
          <div id="chart-line" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>近 7 日 · 新注册用户</span></template>
          <div id="chart-bar" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>分类文档数量占比</span></template>
          <div id="chart-pie" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>分类文档数量（条形）</span></template>
          <div id="chart-hbar" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dash {
  padding: 4px 4px 32px;
}
.sub {
      color: #64748b;
      margin: 6px 0 20px;
}
.cards {
  margin-bottom: 8px;
}
.chart-card {
  border-radius: 16px;
      margin-bottom: 16px;
}
.chart-box {
  height: 280px;
  width: 100%;
}
</style>
