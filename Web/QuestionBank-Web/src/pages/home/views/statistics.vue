<script setup lang="ts">
import { ArrowUp, Back, Collection, Clock, Document } from "@element-plus/icons-vue"
import * as echarts from "echarts"
import { nextTick, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getStudyStatistics, getStudyTrend, getBankProgress } from "../apis/statistics"
import type { StudyStatisticsDTO, StudyTrendDTO, BankProgressDTO } from "../apis/types"

const router = useRouter()

// 响应式数据
const studyStatistics = ref<StudyStatisticsDTO>({
  totalPracticeCount: 0,
  totalExamCount: 0,
  correctCount: 0,
  wrongCount: 0,
  accuracyRate: 0
})

const studyTrend = ref<StudyTrendDTO[]>([])
const bankProgress = ref<BankProgressDTO[]>([])
const loading = ref(false)
const trendChart = ref<HTMLDivElement | null>(null)

// 加载统计数据
async function loadStatistics() {
  loading.value = true
  try {
    const [statisticsData, trendData, progressData] = await Promise.all([
      getStudyStatistics(),
      getStudyTrend(),
      getBankProgress()
    ])

    studyStatistics.value = statisticsData
    studyTrend.value = trendData
    bankProgress.value = progressData

    // 初始化图表
    initTrendChart()
  } catch (error) {
    console.error("加载统计数据失败:", error)
  } finally {
    loading.value = false
  }
}

// 初始化趋势图表
function initTrendChart() {
  nextTick(() => {
    if (trendChart.value && studyTrend.value.length > 0) {
      const myChart = echarts.init(trendChart.value)

      // 提取图表数据
      const dates = studyTrend.value.map(item => item.date.substring(5)) // 只显示月-日
      const questionCounts = studyTrend.value.map(item => item.questionCount)
      const accuracyRates = studyTrend.value.map(item =>
        item.questionCount > 0 ? Math.round((item.correctCount / item.questionCount) * 100) : 0
      )

      const option = {
        grid: { top: 40, right: 40, bottom: 40, left: 50 },
        tooltip: { trigger: "axis", padding: [12, 16], borderRadius: 8, borderWidth: 0, boxShadow: '0 4px 16px rgba(0,0,0,0.1)' },
        legend: { data: ["做题数量", "正确率"], top: 0, itemGap: 24, itemWidth: 16 },
        xAxis: { type: "category", data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#6b7280' }, axisTick: { show: false } },
        yAxis: [
          { type: "value", name: "题目数量", position: "left", splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } }, nameTextStyle: { color: '#6b7280' }, axisLabel: { color: '#6b7280' } },
          { type: "value", name: "正确率", position: "right", axisLabel: { formatter: "{value}%", color: '#6b7280' }, splitLine: { show: false }, nameTextStyle: { color: '#6b7280' }, min: 0, max: 100 }
        ],
        series: [
          { name: "做题数量", type: "bar", barWidth: '30%', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#60a5fa' }]), borderRadius: [4, 4, 0, 0] }, data: questionCounts },
          { name: "正确率", type: "line", yAxisIndex: 1, smooth: true, symbolSize: 8, itemStyle: { color: '#10b981', shadowColor: 'rgba(16,185,129,0.3)', shadowBlur: 8 }, lineStyle: { width: 3 }, data: accuracyRates }
        ]
      }
      myChart.setOption(option)
    }
  })
}

function goToHome() {
  router.push("/")
}

// 获取进度类型
function getProgressType(percentage: number) {
  if (percentage >= 80) return "success"
  if (percentage >= 50) return "warning"
  return "danger"
}

// 获取进度状态文本
function getProgressStatus(percentage: number) {
  if (percentage >= 90) return "优秀"
  if (percentage >= 70) return "良好"
  if (percentage >= 50) return "一般"
  if (percentage >= 20) return "待提高"
  return "刚开始"
}

// 格式化最后学习时间
function formatLastStudyTime(lastStudyTime?: string) {
  if (!lastStudyTime) return "暂无记录"
  
  try {
    const date = new Date(lastStudyTime)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) return "今天"
    if (days === 1) return "昨天"
    if (days < 7) return `${days}天前`
    if (days < 30) return `${Math.floor(days / 7)}周前`
    return date.toLocaleDateString()
  } catch {
    return "暂无记录"
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <el-main class="page-main">
    <div class="page-container">
      <div class="page-header">
        <el-button @click="goToHome" icon="Back" size="large" circle type="primary" plain class="back-btn"></el-button>
        <div class="header-titles">
          <h2>数据统计</h2>
          <p>多维图表，清晰掌握学习进度</p>
        </div>
      </div>

      <!-- 核心指标卡片 -->
      <el-row :gutter="24" class="stat-cards" v-loading="loading">
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-blue"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">总练习次数</span>
              <div class="stat-val">{{ studyStatistics.totalPracticeCount }}</div>
              <div class="stat-trend trend-up">
                <el-icon><ArrowUp /></el-icon> <span>累计练习</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-purple"><el-icon><Collection /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">总考试次数</span>
              <div class="stat-val">{{ studyStatistics.totalExamCount }}</div>
              <div class="stat-trend trend-up">
                <el-icon><ArrowUp /></el-icon> <span>累计考试</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-green"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">正确题目数</span>
              <div class="stat-val">{{ studyStatistics.correctCount }}</div>
              <div class="stat-trend trend-up">
                <el-icon><ArrowUp /></el-icon> <span>正确作答</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-orange"><el-icon><Clock /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">平均正确率</span>
              <div class="stat-val">{{ studyStatistics.accuracyRate.toFixed(1) }}<span class="text-sm">%</span></div>
              <div class="stat-trend trend-up">
                <el-icon><ArrowUp /></el-icon> <span>学习表现</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 学习趋势图 -->
      <div class="chart-section mb-8" v-loading="loading">
        <div class="section-title-wrap">
          <h3 class="section-title">最近7天学习趋势</h3>
        </div>
        <div class="chart-card">
          <div ref="trendChart" style="height: 360px; width: 100%;" />
        </div>
      </div>

      <!-- 题库学习情况 -->
      <div class="bank-progress-section" v-loading="loading">
        <div class="section-title-wrap">
          <h3 class="section-title">我的题库进度</h3>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="12" :lg="8" v-for="bank in bankProgress" :key="bank.bankId" class="mb-6">
            <div class="modern-bank-card">
              <div class="bc-header">
                <div class="bc-icon"><el-icon><Collection /></el-icon></div>
                <div class="bc-title-wrap">
                  <h4>{{ bank.bankName }}</h4>
                  <div class="bc-stats">已学 {{ bank.completedCount }} / {{ bank.totalCount }} 题</div>
                </div>
                <div class="bc-percent">{{ bank.progressPercentage.toFixed(1) }}%</div>
              </div>
              <div class="bc-progress">
                <el-progress :percentage="bank.progressPercentage" :color="bank.progressColor" :show-text="false" :stroke-width="8" />
              </div>
              <div class="bc-footer">
                <el-tag :type="getProgressType(bank.progressPercentage)" round effect="light" size="small">
                  {{ getProgressStatus(bank.progressPercentage) }}
                </el-tag>
                <div class="bc-time"><el-icon><Clock /></el-icon>{{ formatLastStudyTime(bank.lastStudyTime) }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div v-if="bankProgress.length === 0" class="empty-state">
          <div class="empty-icon"><el-icon><Document /></el-icon></div>
          <p class="empty-title">暂无题库学习记录</p>
          <p class="empty-desc">赶紧参加练习或者考试，记录你的进步吧！</p>
        </div>
      </div>
    </div>
  </el-main>
</template>

<style scoped>
/* 全局页面 */
.page-main {
  background-color: var(--el-bg-color-page, #f3f5f9);
  min-height: 100vh;
  padding: 24px;
}
.page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}
.back-btn {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}
.header-titles h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1d1e23);
  margin: 0 0 4px 0;
}
.header-titles p {
  font-size: 14px;
  color: var(--el-text-color-secondary, #6b7280);
  margin: 0;
}

/* 工具 */
.mb-5 { margin-bottom: 20px; }
.mb-6 { margin-bottom: 24px; }
.mb-8 { margin-bottom: 32px; }
.text-sm { font-size: 60%; margin-left: 2px; }

/* 核心指标卡片 */
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--el-border-color-light);
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}
.icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  flex-shrink: 0;
}
.bg-blue { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.bg-purple { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; }
.bg-green { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.bg-orange { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }

.stat-info { flex: 1; }
.stat-title {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.stat-val {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.1;
  margin-bottom: 8px;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}
.trend-up { color: #10b981; }
.trend-up span { color: var(--el-text-color-regular); }

/* 标题 */
.section-title-wrap {
  margin-bottom: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
  padding-left: 14px;
  position: relative;
}
.section-title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: var(--el-color-primary);
  border-radius: 2px;
}

/* 图表容器 */
.chart-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  border: 1px solid var(--el-border-color-light);
}

/* 进度卡片 */
.modern-bank-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.modern-bank-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-5);
}
.bc-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.bc-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(79, 172, 254, 0.3);
}
.bc-title-wrap {
  flex: 1;
  overflow: hidden;
}
.bc-title-wrap h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bc-stats {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.bc-percent {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-color-primary);
}
.bc-progress {
  margin-bottom: auto;
  padding-bottom: 20px;
}
.bc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed var(--el-border-color-light);
}
.bc-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  border: 1px dashed var(--el-border-color-dark);
}
.empty-icon {
  width: 80px;
  height: 80px;
  background: var(--el-fill-color-light);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 36px;
  color: var(--el-text-color-placeholder);
  margin: 0 auto 20px;
}
.empty-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.empty-desc {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .page-main { padding: 16px; }
  .header-titles h2 { font-size: 20px; }
  .stat-val { font-size: 24px; }
}
</style>
