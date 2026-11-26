<script setup lang="ts">
import type { BankProgressDTO, StudyStatisticsDTO, StudyTrendDTO } from "../apis/types"
import { ArrowLeft, ArrowRight, Clock } from "@element-plus/icons-vue"
import * as echarts from "echarts"
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getBankProgress, getStudyStatistics, getStudyTrend } from "../apis/statistics"

const router = useRouter()

// 学习统计核心数据
const studyStatistics = ref<StudyStatisticsDTO>({
  totalPracticeCount: 0,
  totalExamCount: 0,
  correctCount: 0,
  wrongCount: 0,
  accuracyRate: 0
})

const studyTrend = ref<StudyTrendDTO[]>([])
const bankProgress = ref<BankProgressDTO[]>([])
const loading = ref(true)
const trendChart = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 窗口缩放时自适应重绘图表
function handleResize() {
  chartInstance?.resize()
}

// 加载全部统计数据
async function loadStatistics() {
  loading.value = true
  try {
    const [statisticsData, trendData, progressData] = await Promise.all([
      getStudyStatistics(),
      getStudyTrend(),
      getBankProgress()
    ])

    studyStatistics.value = statisticsData
    studyTrend.value = trendData || []
    bankProgress.value = progressData || []

    initTrendChart()
  } catch (error) {
    console.error("加载统计数据失败:", error)
  } finally {
    loading.value = false
  }
}

// 初始化近 7 天学习趋势双轴图（柱状做题量 + 折线正确率）
function initTrendChart() {
  nextTick(() => {
    if (!trendChart.value || studyTrend.value.length === 0) return
    if (chartInstance) {
      chartInstance.dispose()
    }
    chartInstance = echarts.init(trendChart.value)

    const dates = studyTrend.value.map(item => item.date.substring(5))
    const questionCounts = studyTrend.value.map(item => item.questionCount)
    const accuracyRates = studyTrend.value.map(item =>
      item.questionCount > 0 ? Math.round((item.correctCount / item.questionCount) * 100) : 0
    )

    const option: echarts.EChartsOption = {
      grid: { top: 44, right: 44, bottom: 28, left: 48 },
      tooltip: {
        trigger: "axis",
        backgroundColor: "#ffffff",
        borderColor: "#e2e8f0",
        borderWidth: 1,
        textStyle: { color: "#0f172a", fontSize: 13 },
        padding: [10, 14]
      },
      legend: {
        data: ["做题数量", "正确率"],
        top: 0,
        right: 0,
        itemGap: 20,
        itemWidth: 12,
        itemHeight: 8,
        textStyle: { color: "#475569", fontSize: 12 }
      },
      xAxis: {
        type: "category",
        data: dates,
        axisLine: { lineStyle: { color: "#e2e8f0" } },
        axisLabel: { color: "#64748b", fontSize: 12 },
        axisTick: { show: false }
      },
      yAxis: [
        {
          type: "value",
          name: "题数",
          position: "left",
          minInterval: 1,
          nameTextStyle: { color: "#64748b", fontSize: 12 },
          axisLabel: { color: "#64748b", fontSize: 12 },
          splitLine: { lineStyle: { color: "#f1f5f9", type: "dashed" } }
        },
        {
          type: "value",
          name: "正确率",
          position: "right",
          min: 0,
          max: 100,
          nameTextStyle: { color: "#64748b", fontSize: 12 },
          axisLabel: { formatter: "{value}%", color: "#64748b", fontSize: 12 },
          splitLine: { show: false }
        }
      ],
      series: [
        {
          name: "做题数量",
          type: "bar",
          barMaxWidth: 28,
          itemStyle: { color: "#2563eb", borderRadius: [4, 4, 0, 0] },
          data: questionCounts
        },
        {
          name: "正确率",
          type: "line",
          yAxisIndex: 1,
          smooth: true,
          symbol: "circle",
          symbolSize: 7,
          itemStyle: { color: "#059669" },
          lineStyle: { width: 2.5, color: "#059669" },
          data: accuracyRates
        }
      ]
    }
    chartInstance.setOption(option)
  })
}

function goToHome() {
  router.push("/")
}

function goToBankDetail(bankId: string) {
  router.push({ name: "BankDetail", query: { id: bankId } })
}

// 进度状态样式类
function getProgressClass(percentage: number): string {
  if (percentage >= 80) return "status-good"
  if (percentage >= 50) return "status-mid"
  return "status-start"
}

function getProgressStatus(percentage: number): string {
  if (percentage >= 90) return "优秀"
  if (percentage >= 70) return "良好"
  if (percentage >= 50) return "进行中"
  if (percentage >= 20) return "待巩固"
  return "刚开始"
}

function formatLastStudyTime(lastStudyTime?: string): string {
  if (!lastStudyTime) return "暂无记录"
  try {
    const date = new Date(lastStudyTime)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (days <= 0) return "今天"
    if (days === 1) return "昨天"
    if (days < 7) return `${days} 天前`
    if (days < 30) return `${Math.floor(days / 7)} 周前`
    return date.toLocaleDateString()
  } catch {
    return "暂无记录"
  }
}

onMounted(() => {
  loadStatistics()
  window.addEventListener("resize", handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<template>
  <div class="stats-page">
    <!-- 顶部返回与页眉 -->
    <header class="page-header">
      <button type="button" class="back-btn" @click="goToHome">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </button>
      <div class="header-text">
        <h1 class="page-title">
          学习数据统计
        </h1>
        <p class="page-subtitle">
          从练习频次、正确率趋势与题库完成度三个维度复盘学习进展
        </p>
      </div>
    </header>

    <!-- 四项核心 KPI 指标卡 -->
    <section class="kpi-grid" v-loading="loading">
      <div class="kpi-card">
        <span class="kpi-label">总练习次数</span>
        <div class="kpi-value">
          {{ studyStatistics.totalPracticeCount }}
        </div>
        <span class="kpi-foot">累计完成的专项练习</span>
      </div>

      <div class="kpi-card">
        <span class="kpi-label">总考试次数</span>
        <div class="kpi-value">
          {{ studyStatistics.totalExamCount }}
        </div>
        <span class="kpi-foot">累计完成的模拟考试</span>
      </div>

      <div class="kpi-card">
        <span class="kpi-label">累计答对题数</span>
        <div class="kpi-value is-emerald">
          {{ studyStatistics.correctCount }}
        </div>
        <span class="kpi-foot">答错 {{ studyStatistics.wrongCount }} 题</span>
      </div>

      <div class="kpi-card">
        <span class="kpi-label">平均正确率</span>
        <div class="kpi-value is-accent">
          {{ studyStatistics.accuracyRate.toFixed(1) }}<span class="kpi-unit">%</span>
        </div>
        <span class="kpi-foot">综合答题准确表现</span>
      </div>
    </section>

    <!-- 最近 7 天学习趋势图 -->
    <section class="panel-card" v-loading="loading">
      <div class="panel-head">
        <div>
          <h2 class="section-heading">
            最近 7 天学习趋势
          </h2>
          <p class="section-desc">
            每日做题数量与当日正确率变化对照
          </p>
        </div>
      </div>

      <div v-if="!loading && studyTrend.length === 0" class="panel-empty">
        <el-empty description="近 7 天暂无学习趋势数据" :image-size="72" />
      </div>
      <div v-else ref="trendChart" class="trend-chart" />
    </section>

    <!-- 我的题库学习进度 -->
    <section class="progress-section" v-loading="loading">
      <div class="section-bar">
        <h2 class="section-heading">
          我的题库进度
        </h2>
        <span class="section-count">共 {{ bankProgress.length }} 套</span>
      </div>

      <div v-if="bankProgress.length > 0" class="progress-grid">
        <article
          v-for="bank in bankProgress"
          :key="bank.bankId"
          class="progress-card"
          @click="goToBankDetail(bank.bankId)"
        >
          <div class="pc-head">
            <h3 class="pc-name" :title="bank.bankName">
              {{ bank.bankName }}
            </h3>
            <span class="pc-percent">{{ bank.progressPercentage.toFixed(1) }}%</span>
          </div>

          <div class="pc-sub">
            已学 {{ bank.completedCount }} / {{ bank.totalCount }} 题
          </div>

          <div class="pc-bar-track">
            <div
              class="pc-bar-fill"
              :style="{ width: `${Math.min(100, Math.max(0, bank.progressPercentage || 0))}%` }"
            />
          </div>

          <div class="pc-foot">
            <span class="status-badge" :class="getProgressClass(bank.progressPercentage)">
              {{ getProgressStatus(bank.progressPercentage) }}
            </span>
            <div class="pc-foot-right">
              <span class="pc-time">
                <el-icon><Clock /></el-icon>
                <span>{{ formatLastStudyTime(bank.lastStudyTime) }}</span>
              </span>
              <el-icon class="pc-arrow">
                <ArrowRight />
              </el-icon>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="panel-card panel-empty">
        <el-empty description="暂无题库学习记录，去挑一套题库开始练习吧" :image-size="76" />
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ======== 页面整体（融入 Layout 的 #f8fafc 背景） ======== */
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #0f172a;
}

/* 顶部返回与页眉 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.16s ease;
}

.back-btn:hover {
  border-color: #cbd5e1;
  color: #0f172a;
  background-color: #f8fafc;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

/* ======== 四列 KPI 指标卡（克制清晰，无花哨色块） ======== */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.kpi-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.15;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.kpi-value.is-accent {
  color: #2563eb;
}

.kpi-value.is-emerald {
  color: #059669;
}

.kpi-unit {
  font-size: 16px;
  font-weight: 600;
  margin-left: 2px;
}

.kpi-foot {
  font-size: 12px;
  color: #94a3b8;
  font-variant-numeric: tabular-nums;
}

/* ======== 通用白卡面板与图表区 ======== */
.panel-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px 28px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-heading {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.section-desc {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748b;
}

.trend-chart {
  width: 100%;
  height: 340px;
}

.panel-empty {
  padding: 24px 0;
}

/* ======== 题库进度区 ======== */
.progress-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-bar {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.section-count {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.progress-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.progress-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.06);
  transform: translateY(-1px);
}

.pc-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.pc-name {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pc-percent {
  font-size: 15px;
  font-weight: 700;
  color: #2563eb;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.pc-sub {
  font-size: 12px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.pc-bar-track {
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background-color: #f1f5f9;
  overflow: hidden;
}

.pc-bar-fill {
  height: 100%;
  border-radius: 999px;
  background-color: #2563eb;
  transition: width 0.3s ease;
}

.pc-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
}

.status-good {
  background-color: #ecfdf5;
  color: #047857;
}

.status-mid {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.status-start {
  background-color: #f1f5f9;
  color: #64748b;
}

.pc-foot-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.pc-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.pc-arrow {
  font-size: 13px;
  color: #94a3b8;
  transition:
    transform 0.16s ease,
    color 0.16s ease;
}

.progress-card:hover .pc-arrow {
  color: #2563eb;
  transform: translateX(2px);
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .progress-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 640px) {
  .progress-grid {
    grid-template-columns: 1fr;
  }

  .panel-card {
    padding: 18px 16px;
  }
}
</style>
