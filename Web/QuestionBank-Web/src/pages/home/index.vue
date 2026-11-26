<script setup lang="ts">
import type { BankProgressDTO, QuestionBank, StudyStatisticsDTO, WrongQuestionStatsVO } from "./apis/types"
import { useUserStore } from "@/pinia/stores/user"
import defaultCover from "@@/assets/images/questionBank/f-head05.png"
import { ArrowRight, Check, Close, Collection, Histogram, Notebook, Search } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getAllPublicQuestionBanks } from "./apis/QuestionBank"
import { getQuestionsByPage } from "./apis/Questions"
import { getBankProgress, getStudyStatistics } from "./apis/statistics"
import { getWrongQuestionStats } from "./apis/WrongQuestions"

const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL
const router = useRouter()
const userStore = useUserStore()

// 是否已登录（响应式计算属性，类似派生字段）
const isLoggedIn = computed(() => Boolean(userStore.token))

// 公共题库列表与加载状态
const popularBanks = ref<QuestionBank[]>([])
const banksLoading = ref(true)

// 已登录用户的真实学习概览数据
const studyStats = ref<StudyStatisticsDTO | null>(null)
const recentProgress = ref<BankProgressDTO | null>(null)
const wrongStats = ref<WrongQuestionStatsVO | null>(null)

// 题库广场筛选与展开状态
const searchKeyword = ref("")
const activeDifficulty = ref("全部")
const showAllBanks = ref(false)
const difficultyOptions = ["全部", "简单", "中等", "困难"]

// 首屏交互式示例题卡（未登录时展示产品核心刷题体验，用户可直接点选体验）
const demoQuestion = {
  tag: "计算机网络 · 单选题",
  difficulty: "中等",
  title: "HTTP 响应状态码 401 与 403 的核心区别在于？",
  options: [
    { key: "A", text: "401 表示请求路径不存在，403 表示请求方法不允许" },
    { key: "B", text: "401 表示未通过身份认证，403 表示已认证但无访问权限" },
    { key: "C", text: "401 属于客户端超时，403 属于服务端内部异常" },
    { key: "D", text: "两者语义完全一致，仅由不同浏览器决定返回码" }
  ],
  correctKey: "B",
  analysis: "401 Unauthorized 强调缺少或未通过有效身份凭证；403 Forbidden 表示服务端已识别身份但拒绝授权访问。"
}
const selectedDemoKey = ref<string>("B")

// 公共题库真实汇总统计（直接从后端返回的题库列表聚合计算）
const totalBankCount = computed(() => popularBanks.value.length)
const totalQuestionCount = computed(() =>
  popularBanks.value.reduce((sum, bank) => sum + (bank.questionCount || 0), 0)
)

// 根据关键字和难度过滤后的题库列表
const filteredBanks = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return popularBanks.value.filter((bank) => {
    const matchDifficulty = activeDifficulty.value === "全部" || bank.difficulty === activeDifficulty.value
    const matchKeyword = !keyword
      || bank.name.toLowerCase().includes(keyword)
      || (bank.description || "").toLowerCase().includes(keyword)
    return matchDifficulty && matchKeyword
  })
})

// 实际展示的题库列表：无筛选且未展开时默认显示前 8 个，避免页面过长
const displayedBanks = computed(() => {
  const hasFilter = searchKeyword.value.trim().length > 0 || activeDifficulty.value !== "全部"
  if (hasFilter || showAllBanks.value) {
    return filteredBanks.value
  }
  return filteredBanks.value.slice(0, 8)
})

const hasMoreBanks = computed(() => {
  const hasFilter = searchKeyword.value.trim().length > 0 || activeDifficulty.value !== "全部"
  return !hasFilter && filteredBanks.value.length > 8
})

// 正确率格式化展示（兼容 0~1 小数与 0~100 百分数两种常见后端返回格式）
const formattedAccuracy = computed(() => {
  const rate = studyStats.value?.accuracyRate
  if (rate === undefined || rate === null) return "--"
  const normalized = rate <= 1 && rate > 0 ? rate * 100 : rate
  return `${Math.round(normalized)}%`
})

// 拼接题库封面完整 URL
function getFullImageUrl(url: string | undefined) {
  if (!url) return defaultCover
  if (url.startsWith("http://") || url.startsWith("https://")) return url
  if (url.startsWith("/img/")) return IMAGE_BASE_URL + url
  return defaultCover
}

// 图片加载失败时降级到默认封面，防止死循环
function onImgError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target.src !== defaultCover) {
    target.src = defaultCover
  }
}

// 难度样式类映射（用于低饱和度浅底徽章）
function getDifficultyClass(difficulty: string): string {
  const map: Record<string, string> = {
    简单: "diff-easy",
    中等: "diff-medium",
    困难: "diff-hard"
  }
  return map[difficulty] || "diff-default"
}

// 兼容 Element Plus Tag 组件的类型映射（用于预览弹窗）
function getDifficultyType(
  difficulty: string
): "success" | "warning" | "info" | "primary" | "danger" {
  const types: Record<string, "success" | "warning" | "danger"> = {
    简单: "success",
    中等: "warning",
    困难: "danger"
  }
  return types[difficulty] || "info"
}

// 重置题库筛选条件
function resetFilters() {
  searchKeyword.value = ""
  activeDifficulty.value = "全部"
}

// 平滑滚动至公共题库广场
function scrollToBankSquare() {
  const el = document.getElementById("bank-square")
  el?.scrollIntoView({ behavior: "smooth", block: "start" })
}

// 题库预览弹窗状态与题目懒加载
const previewDialogVisible = ref(false)
const previewLoading = ref(false)
const selectedBank = ref<QuestionBank | null>(null)

function showBankPreview(bank: QuestionBank) {
  selectedBank.value = bank
  previewDialogVisible.value = true
  previewLoading.value = true
  getQuestionsByPage(bank.id)
    .then((page) => {
      const records = page.records || []
      if (selectedBank.value && selectedBank.value.id === bank.id) {
        selectedBank.value.previewQuestions = records
      }
    })
    .catch(() => {
      ElMessage.error("获取题库题目失败")
    })
    .finally(() => {
      previewLoading.value = false
    })
}

// 路由跳转方法
function goToQuestionDetail(bankId: string | undefined) {
  if (!bankId) return
  router.push({ name: "BankDetail", query: { id: bankId, from: "popular" } })
}

function goToLogin() {
  router.push({ path: "/user/login", query: { type: "login" } })
}

function goToStatistics() {
  router.push({ name: "Statistics" })
}

function goToWrongQuestions() {
  router.push({ name: "WrongQuestions" })
}

function goToCreateBank() {
  router.push({ path: "/question-bank/createBank", query: { from: "home" } })
}

function goToContinuePractice(bankId: string) {
  router.push({ name: "Practice", query: { bank: bankId } })
}

// 拉取已登录用户的学习概览数据（并行请求，单接口失败不影响整体渲染）
async function loadPersonalDashboard() {
  if (!isLoggedIn.value) return
  const [statsRes, progressRes, wrongRes] = await Promise.allSettled([
    getStudyStatistics(),
    getBankProgress(),
    getWrongQuestionStats()
  ])

  if (statsRes.status === "fulfilled" && statsRes.value) {
    studyStats.value = statsRes.value
  }
  if (progressRes.status === "fulfilled" && Array.isArray(progressRes.value) && progressRes.value.length > 0) {
    recentProgress.value = progressRes.value[0]
  }
  if (wrongRes.status === "fulfilled" && wrongRes.value?.data) {
    wrongStats.value = wrongRes.value.data
  }
}

onMounted(async () => {
  banksLoading.value = true
  try {
    popularBanks.value = await getAllPublicQuestionBanks()
  } catch (error) {
    console.error("获取公共题库列表失败:", error)
  } finally {
    banksLoading.value = false
  }
  await loadPersonalDashboard()
})
</script>

<template>
  <div class="home-page">
    <!-- 首屏区域：未登录展示产品价值与交互示例卡，已登录切换为个人学习工作台 -->
    <section class="hero-card">
      <div v-if="!isLoggedIn" class="hero-grid">
        <div class="hero-left">
          <div class="hero-eyebrow">
            <span class="eyebrow-dot" />
            <span>个性化在线题库与刷题工作台</span>
          </div>
          <h1 class="hero-title">
            系统化刷题与复盘，<br>让每一次练习都有迹可循
          </h1>
          <p class="hero-subtitle">
            支持自建题库、专项练习、模拟考试与自动错题归档。围绕知识点构建你的专属题库体系，用真实数据追踪备考进度。
          </p>
          <div class="hero-actions">
            <button type="button" class="btn-primary" @click="scrollToBankSquare">
              <span>浏览公共题库</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
            <button type="button" class="btn-secondary" @click="goToLogin">
              登录 / 开启个人题库
            </button>
          </div>
          <div class="hero-metrics">
            <div class="metric-item">
              <span class="metric-value">{{ banksLoading ? "--" : totalBankCount }}</span>
              <span class="metric-label">公开精选题库</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">{{ banksLoading ? "--" : totalQuestionCount }}</span>
              <span class="metric-label">累计可练题目</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">5 种</span>
              <span class="metric-label">主流客观与主观题型</span>
            </div>
          </div>
        </div>

        <!-- 右侧交互式题卡预览：替代外部网图，直观呈现产品核心体验 -->
        <div class="hero-right">
          <div class="demo-panel">
            <div class="demo-panel-bar">
              <div class="demo-meta">
                <span class="demo-tag">{{ demoQuestion.tag }}</span>
                <span class="demo-diff">{{ demoQuestion.difficulty }}</span>
              </div>
              <span class="demo-hint">点击选项体验</span>
            </div>
            <div class="demo-question-title">
              {{ demoQuestion.title }}
            </div>
            <div class="demo-options">
              <button
                v-for="opt in demoQuestion.options"
                :key="opt.key"
                type="button"
                class="demo-option"
                :class="{
                  'is-correct': selectedDemoKey === opt.key && opt.key === demoQuestion.correctKey,
                  'is-wrong': selectedDemoKey === opt.key && opt.key !== demoQuestion.correctKey,
                }"
                @click="selectedDemoKey = opt.key"
              >
                <span class="demo-opt-key">{{ opt.key }}</span>
                <span class="demo-opt-text">{{ opt.text }}</span>
                <el-icon v-if="selectedDemoKey === opt.key && opt.key === demoQuestion.correctKey" class="demo-opt-icon">
                  <Check />
                </el-icon>
                <el-icon v-else-if="selectedDemoKey === opt.key" class="demo-opt-icon">
                  <Close />
                </el-icon>
              </button>
            </div>
            <div class="demo-analysis">
              <span class="analysis-label">参考解析</span>
              <p>{{ demoQuestion.analysis }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 已登录态：个人学习概览工作台 -->
      <div v-else class="dashboard-grid">
        <div class="dashboard-left">
          <div class="hero-eyebrow">
            <span class="eyebrow-dot" />
            <span>个人学习工作台</span>
          </div>
          <h1 class="dashboard-title">
            欢迎回来，{{ userStore.username || "同学" }}
          </h1>
          <p class="dashboard-subtitle">
            保持稳定的练习节奏，优先复习薄弱错题，随时进入模拟考场检验成果。
          </p>
          <div class="kpi-grid">
            <div class="kpi-box">
              <span class="kpi-label">累计练习次数</span>
              <span class="kpi-value">{{ studyStats?.totalPracticeCount ?? 0 }}</span>
            </div>
            <div class="kpi-box">
              <span class="kpi-label">模拟考试次数</span>
              <span class="kpi-value">{{ studyStats?.totalExamCount ?? 0 }}</span>
            </div>
            <div class="kpi-box">
              <span class="kpi-label">综合正确率</span>
              <span class="kpi-value is-accent">{{ formattedAccuracy }}</span>
            </div>
            <div class="kpi-box">
              <span class="kpi-label">待复习错题</span>
              <span class="kpi-value" :class="{ 'is-warn': (wrongStats?.unreviewed ?? 0) > 0 }">
                {{ wrongStats?.unreviewed ?? 0 }}
              </span>
            </div>
          </div>
        </div>

        <div class="dashboard-right">
          <div v-if="recentProgress" class="progress-panel">
            <div class="progress-panel-head">
              <span class="progress-badge">最近学习进度</span>
              <span class="progress-ratio">{{ recentProgress.completedCount }} / {{ recentProgress.totalCount }} 题</span>
            </div>
            <h3 class="progress-bank-name" :title="recentProgress.bankName">
              {{ recentProgress.bankName }}
            </h3>
            <div class="progress-bar-track">
              <div
                class="progress-bar-fill"
                :style="{ width: `${Math.min(100, Math.max(0, recentProgress.progressPercentage || 0))}%` }"
              />
            </div>
            <div class="progress-panel-foot">
              <span class="progress-percent">已完成 {{ Math.round(recentProgress.progressPercentage || 0) }}%</span>
              <button type="button" class="btn-primary btn-sm" @click="goToContinuePractice(recentProgress.bankId)">
                <span>继续练习</span>
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>

          <div v-else class="progress-panel is-empty">
            <span class="progress-badge">开始今日练习</span>
            <h3 class="progress-bank-name">
              暂无进行中的题库记录
            </h3>
            <p class="progress-empty-desc">
              从下方公共题库中挑选一套感兴趣的题目开始练习，系统会自动记录你的进度与错题。
            </p>
            <button type="button" class="btn-primary btn-sm" @click="scrollToBankSquare">
              <span>挑一套题库开始</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷工具条：收敛为紧凑统一的三列功能入口 -->
    <section class="tools-section">
      <div class="tools-grid">
        <div class="tool-card" @click="goToCreateBank">
          <div class="tool-icon">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="tool-body">
            <div class="tool-title-row">
              <h3>创建专属题库</h3>
            </div>
            <p>自定义章节与题型，构建个人或团队知识库</p>
          </div>
          <el-icon class="tool-arrow">
            <ArrowRight />
          </el-icon>
        </div>

        <div class="tool-card" @click="goToStatistics">
          <div class="tool-icon">
            <el-icon><Histogram /></el-icon>
          </div>
          <div class="tool-body">
            <div class="tool-title-row">
              <h3>学习数据统计</h3>
            </div>
            <p>追踪每日练习趋势、正确率曲线与各题库进度</p>
          </div>
          <el-icon class="tool-arrow">
            <ArrowRight />
          </el-icon>
        </div>

        <div class="tool-card" @click="goToWrongQuestions">
          <div class="tool-icon">
            <el-icon><Notebook /></el-icon>
          </div>
          <div class="tool-body">
            <div class="tool-title-row">
              <h3>智能错题本</h3>
              <span v-if="isLoggedIn && (wrongStats?.unreviewed ?? 0) > 0" class="tool-badge">
                待复习 {{ wrongStats?.unreviewed }}
              </span>
            </div>
            <p>自动归档练习错题，按薄弱点集中复习巩固</p>
          </div>
          <el-icon class="tool-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </section>

    <!-- 公共题库广场：支持关键字搜索与难度筛选 -->
    <section id="bank-square" class="square-section">
      <div class="square-header">
        <div class="square-title-group">
          <h2 class="section-heading">
            公共题库广场
          </h2>
          <span class="section-count">{{ filteredBanks.length }} 套</span>
        </div>

        <div class="square-toolbar">
          <div class="filter-pills" role="tablist" aria-label="按难度筛选题库">
            <button
              v-for="diff in difficultyOptions"
              :key="diff"
              type="button"
              class="filter-pill"
              :class="{ 'is-active': activeDifficulty === diff }"
              @click="activeDifficulty = diff"
            >
              {{ diff }}
            </button>
          </div>

          <el-input
            v-model="searchKeyword"
            placeholder="搜索题库名称或简介"
            clearable
            class="square-search"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 骨架屏：数据加载中占位，避免大片空白 -->
      <div v-if="banksLoading" class="bank-grid">
        <div v-for="n in 4" :key="n" class="bank-skeleton-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 150px;" />
              <div style="padding: 16px;">
                <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 10px;" />
                <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 100%;" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 题库卡片网格 -->
      <div v-else-if="displayedBanks.length > 0" class="bank-grid">
        <article
          v-for="bank in displayedBanks"
          :key="bank.id"
          class="bank-card"
          @click="showBankPreview(bank)"
        >
          <div class="bank-cover">
            <img :src="getFullImageUrl(bank.image)" :alt="bank.name" class="bank-cover-img" @error="onImgError">
          </div>

          <div class="bank-body">
            <div class="bank-meta-row">
              <span class="diff-badge" :class="getDifficultyClass(bank.difficulty)">
                {{ bank.difficulty }}
              </span>
              <span class="bank-qcount">{{ bank.questionCount }} 题</span>
            </div>

            <h3 class="bank-name" :title="bank.name">
              {{ bank.name }}
            </h3>
            <p class="bank-desc" :title="bank.description">
              {{ bank.description || "暂无题库简介，点击预览查看包含的题目内容。" }}
            </p>

            <div class="bank-footer">
              <span class="bank-author">{{ bank.userName || "平台精选" }}</span>
              <span class="bank-action">
                预览题目
                <el-icon><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="square-empty">
        <el-empty
          :description="popularBanks.length === 0 ? '暂未获取到公共题库数据' : '没有找到符合筛选条件的题库'"
          :image-size="76"
        >
          <el-button v-if="popularBanks.length > 0" @click="resetFilters">
            重置筛选条件
          </el-button>
        </el-empty>
      </div>

      <!-- 展开/收起更多题库 -->
      <div v-if="hasMoreBanks" class="square-more">
        <button type="button" class="btn-secondary" @click="showAllBanks = !showAllBanks">
          {{ showAllBanks ? "收起部分题库" : `查看全部 ${filteredBanks.length} 套题库` }}
        </button>
      </div>
    </section>

    <!-- 题库预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="selectedBank?.name"
      width="780px"
      class="preview-dialog"
      destroy-on-close
      :scrollbar="false"
      :lock-scroll="false"
    >
      <div v-if="selectedBank" class="preview-scroll-area">
        <div class="preview-header">
          <img :src="getFullImageUrl(selectedBank.image)" :alt="selectedBank.name" class="preview-hero-img" @error="onImgError">
          <div class="preview-hero-meta">
            <div class="preview-tags">
              <span class="diff-badge" :class="getDifficultyClass(selectedBank.difficulty)">
                难度 · {{ selectedBank.difficulty }}
              </span>
              <span class="preview-author-tag">创建者：{{ selectedBank.userName || "平台精选" }}</span>
            </div>
            <div class="preview-stat-row">
              <span class="stat-val">{{ selectedBank.questionCount }}</span>
              <span class="stat-lbl">道可练习题目</span>
            </div>
            <p class="preview-desc-text">
              {{ selectedBank.description || "该题库暂未填写详细介绍。" }}
            </p>
          </div>
        </div>

        <div class="preview-q-list">
          <h4 class="preview-sub-title">
            部分题目预览
          </h4>
          <div v-if="previewLoading" class="preview-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <template v-else-if="selectedBank.previewQuestions && selectedBank.previewQuestions.length > 0">
            <div v-for="(question, index) in selectedBank.previewQuestions" :key="index" class="q-card">
              <div class="q-header">
                <el-tag type="info" effect="plain" size="small">
                  {{ question.type }}
                </el-tag>
                <el-tag :type="getDifficultyType(question.difficulty)" effect="light" size="small">
                  {{ question.difficulty }}
                </el-tag>
              </div>
              <div class="q-content">
                {{ index + 1 }}. {{ question.content }}
              </div>
              <div v-if="question.options && question.options.length > 0" class="q-options">
                <div v-for="option in question.options" :key="option.key" class="opt-item">
                  <span class="opt-key">{{ option.key }}</span>
                  <span class="opt-text">{{ option.content }}</span>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="q-empty">
            <el-empty description="暂无预览题目或题库为空" :image-size="72" />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-foot">
          <button type="button" class="btn-secondary" @click="previewDialogVisible = false">
            暂不练习
          </button>
          <button type="button" class="btn-primary" @click="goToQuestionDetail(selectedBank?.id)">
            <span>进入题库详情</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ======== 页面整体布局（消除外层多余灰框，直接融入 Layout 的 #f8fafc 底色） ======== */
.home-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
  color: #0f172a;
}

/* 通用按钮体系：利落 8px 圆角 + 靛蓝主色 */
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition:
    background-color 0.18s ease,
    border-color 0.18s ease,
    color 0.18s ease;
  line-height: 1.2;
}

.btn-primary {
  background-color: #2563eb;
  color: #ffffff;
  border: 1px solid #2563eb;
}

.btn-primary:hover {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-secondary {
  background-color: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover {
  background-color: #f8fafc;
  border-color: #94a3b8;
  color: #0f172a;
}

.btn-sm {
  padding: 8px 14px;
  font-size: 13px;
}

/* ======== 首屏卡片：极细描边 + 极浅点阵背景纹理 ======== */
.hero-card {
  background-color: #ffffff;
  background-image: radial-gradient(#e2e8f0 1px, transparent 1px);
  background-size: 24px 24px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 36px 40px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.95fr);
  gap: 40px;
  align-items: center;
}

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: 6px;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 16px;
}

.eyebrow-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #2563eb;
}

.hero-title {
  margin: 0 0 14px 0;
  font-size: 32px;
  line-height: 1.28;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.hero-subtitle {
  margin: 0 0 24px 0;
  font-size: 15px;
  line-height: 1.65;
  color: #475569;
  max-width: 520px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 28px;
}

.hero-metrics {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
}

.metric-divider {
  width: 1px;
  height: 28px;
  background-color: #e2e8f0;
}

/* ======== 右侧交互式题卡预览 ======== */
.demo-panel {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.demo-panel-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.demo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.demo-tag {
  font-size: 12px;
  font-weight: 600;
  color: #1e40af;
  background-color: #eff6ff;
  padding: 3px 8px;
  border-radius: 5px;
}

.demo-diff {
  font-size: 12px;
  font-weight: 500;
  color: #b45309;
  background-color: #fef3c7;
  padding: 3px 8px;
  border-radius: 5px;
}

.demo-hint {
  font-size: 12px;
  color: #94a3b8;
}

.demo-question-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.5;
  margin-bottom: 14px;
}

.demo-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.demo-option {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  color: #334155;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: all 0.16s ease;
}

.demo-option:hover {
  border-color: #cbd5e1;
  background-color: #f1f5f9;
}

.demo-option.is-correct {
  border-color: #86efac;
  background-color: #f0fdf4;
  color: #166534;
}

.demo-option.is-wrong {
  border-color: #fca5a5;
  background-color: #fef2f2;
  color: #991b1b;
}

.demo-opt-key {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.demo-option.is-correct .demo-opt-key {
  background-color: #16a34a;
  border-color: #16a34a;
  color: #ffffff;
}

.demo-option.is-wrong .demo-opt-key {
  background-color: #dc2626;
  border-color: #dc2626;
  color: #ffffff;
}

.demo-opt-text {
  flex: 1;
  line-height: 1.4;
}

.demo-opt-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.demo-analysis {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
}

.analysis-label {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 4px;
}

.demo-analysis p {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #475569;
}

/* ======== 已登录态：个人学习工作台 ======== */
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(0, 0.9fr);
  gap: 32px;
  align-items: stretch;
}

.dashboard-title {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.dashboard-subtitle {
  margin: 0 0 22px 0;
  font-size: 14px;
  color: #475569;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-box {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kpi-label {
  font-size: 12px;
  color: #64748b;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.kpi-value.is-accent {
  color: #2563eb;
}

.kpi-value.is-warn {
  color: #d97706;
}

.progress-panel {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 22px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}

.progress-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-badge {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  padding: 3px 8px;
  border-radius: 6px;
  width: fit-content;
}

.progress-ratio {
  font-size: 12px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.progress-bank-name {
  margin: 0 0 14px 0;
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-empty-desc {
  margin: 0 0 18px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
}

.progress-bar-track {
  width: 100%;
  height: 8px;
  background-color: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 16px;
}

.progress-bar-fill {
  height: 100%;
  background-color: #2563eb;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.progress-panel-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-percent {
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  font-variant-numeric: tabular-nums;
}

/* ======== 快捷工具条（三列紧凑横向卡片） ======== */
.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.tool-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.tool-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.06);
  transform: translateY(-1px);
}

.tool-icon {
  width: 40px;
  height: 40px;
  border-radius: 9px;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.tool-body {
  flex: 1;
  min-width: 0;
}

.tool-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.tool-body h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.tool-badge {
  font-size: 11px;
  font-weight: 600;
  color: #b45309;
  background-color: #fef3c7;
  padding: 1px 7px;
  border-radius: 999px;
  font-variant-numeric: tabular-nums;
}

.tool-body p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-arrow {
  color: #94a3b8;
  font-size: 15px;
  transition:
    transform 0.18s ease,
    color 0.18s ease;
}

.tool-card:hover .tool-arrow {
  color: #2563eb;
  transform: translateX(3px);
}

/* ======== 公共题库广场 ======== */
.square-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.square-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.square-title-group {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.section-heading {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: #0f172a;
}

.section-count {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.square-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-pills {
  display: inline-flex;
  align-items: center;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}

.filter-pill {
  border: none;
  background: transparent;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}

.filter-pill:hover {
  color: #0f172a;
}

.filter-pill.is-active {
  background-color: #ffffff;
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.square-search {
  width: 240px;
}

.square-search :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.square-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}

/* 题库卡片网格：4 列自适应 */
.bank-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.bank-skeleton-card,
.bank-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.bank-card {
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.bank-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  transform: translateY(-2px);
}

.bank-cover {
  width: 100%;
  aspect-ratio: 16 / 10;
  background-color: #f1f5f9;
  overflow: hidden;
  border-bottom: 1px solid #f1f5f9;
}

.bank-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.bank-card:hover .bank-cover-img {
  transform: scale(1.03);
}

.bank-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.bank-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.diff-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.diff-easy {
  background-color: #ecfdf5;
  color: #047857;
}

.diff-medium {
  background-color: #fffbeb;
  color: #b45309;
}

.diff-hard {
  background-color: #fef2f2;
  color: #b91c1c;
}

.diff-default {
  background-color: #f1f5f9;
  color: #475569;
}

.bank-qcount {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  font-variant-numeric: tabular-nums;
}

.bank-name {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-desc {
  margin: 0 0 14px 0;
  font-size: 13px;
  line-height: 1.55;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.bank-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.bank-author {
  font-size: 12px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.bank-action {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
}

.square-empty {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 36px 20px;
}

.square-more {
  display: flex;
  justify-content: center;
  padding-top: 4px;
}

/* ======== 题库预览弹窗 ======== */
:deep(.preview-dialog) {
  border-radius: 14px;
  overflow: hidden;
}

:deep(.preview-dialog .el-dialog__header) {
  padding: 18px 24px;
  margin-right: 0;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
}

:deep(.preview-dialog .el-dialog__body) {
  padding: 0;
}

.preview-scroll-area {
  max-height: 64vh;
  overflow-y: auto;
  padding: 20px 24px;
}

.preview-header {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.preview-hero-img {
  width: 200px;
  height: 126px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.preview-hero-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.preview-author-tag {
  font-size: 12px;
  color: #64748b;
}

.preview-stat-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-val {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.stat-lbl {
  font-size: 13px;
  color: #64748b;
}

.preview-desc-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
}

.preview-sub-title {
  margin: 0 0 14px 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.preview-loading {
  padding: 12px 0;
}

.q-card {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.q-content {
  font-size: 14px;
  font-weight: 500;
  color: #0f172a;
  line-height: 1.55;
  margin-bottom: 12px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opt-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
}

.opt-key {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background-color: #f1f5f9;
  color: #334155;
  font-weight: 600;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.opt-text {
  color: #334155;
  line-height: 1.55;
}

.dialog-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px 18px;
  border-top: 1px solid #e2e8f0;
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 1120px) {
  .bank-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media screen and (max-width: 960px) {
  .hero-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .bank-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 640px) {
  .hero-card {
    padding: 24px 20px;
  }

  .hero-title {
    font-size: 24px;
  }

  .bank-grid {
    grid-template-columns: 1fr;
  }

  .square-search {
    width: 100%;
  }

  .preview-header {
    flex-direction: column;
  }

  .preview-hero-img {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 10;
  }
}
</style>
