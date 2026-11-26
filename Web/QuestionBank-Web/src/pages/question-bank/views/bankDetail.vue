<script setup lang="ts">
import defaultCover from "@@/assets/images/questionBank/f-head05.png"
import { ArrowLeft, ArrowRight, Collection, Edit, Reading } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { addUserQuestionBank, getPublicQuestionBankById } from "../apis/QuestionBank"
import { getQuestionsByPage } from "../apis/Questions"
import { mapToQuestion } from "../apis/type"

const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL

interface Option {
  key: string
  content: string
}

interface Question {
  type: string
  content: string
  options?: Option[]
}

interface BankInfo {
  id: string | null
  name: string
  description: string
  difficulty: string
  questionCount: number
  image: string
  questionTypes: string[]
  questions: Question[]
  isCollected: boolean
  userName: string
  type: string
  coverImage: string
}

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const collecting = ref(false)
const selectedType = ref("全部")
const fromPopular = ref(false)

const bankInfo = ref<BankInfo>({
  id: null,
  name: "",
  description: "",
  difficulty: "",
  questionCount: 0,
  image: "",
  questionTypes: [],
  questions: [],
  isCollected: false,
  userName: "",
  type: "",
  coverImage: ""
})

// 题型筛选选项列表（在动态题型前加上“全部”）
const typeFilterOptions = computed(() => ["全部", ...bankInfo.value.questionTypes])

// 按题型过滤预览题目
const filteredQuestions = computed((): Question[] => {
  if (selectedType.value === "全部") {
    return bankInfo.value.questions
  }
  return bankInfo.value.questions.filter(q => q.type === selectedType.value)
})

// 拼接封面图片完整 URL
function getFullImageUrl(url: string | undefined) {
  if (!url) return defaultCover
  if (url.startsWith("http://") || url.startsWith("https://")) return url
  if (url.startsWith("/img/")) return IMAGE_BASE_URL + url
  return defaultCover
}

// 封面加载失败时回退到默认封面
function onImgError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target.src !== defaultCover) {
    target.src = defaultCover
  }
}

function mapDifficulty(num: number): string {
  switch (num) {
    case 1:
      return "简单"
    case 2:
      return "中等"
    case 3:
    case 4:
      return "困难"
    default:
      return "未知"
  }
}

function getDifficultyClass(difficulty: string): string {
  const map: Record<string, string> = {
    简单: "diff-easy",
    中等: "diff-medium",
    困难: "diff-hard"
  }
  return map[difficulty] || "diff-default"
}

// 加载题库详情与前 10 道预览题目
async function getBankInfo() {
  const bankId = String(route.query.id || "")
  if (!bankId) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const data = await getPublicQuestionBankById(bankId)
    bankInfo.value = {
      id: String(data.id),
      name: data.name,
      description: data.description,
      difficulty: mapDifficulty(data.difficulty),
      questionCount: data.questionCount ?? 0,
      image: data.coverImage,
      questionTypes: [],
      questions: [],
      isCollected: data.isCollected ?? false,
      userName: data.userName,
      type: data.type,
      coverImage: data.coverImage
    }
    const pageResult = await getQuestionsByPage(bankId, 1, 10)
    const mappedQuestions = (pageResult.records || []).map(mapToQuestion)
    bankInfo.value.questions = mappedQuestions
    bankInfo.value.questionTypes = Array.from(new Set(mappedQuestions.map(q => q.type)))
  } catch (error) {
    console.error("获取题库详情失败:", error)
    ElMessage.error("获取题库详情失败")
  } finally {
    loading.value = false
  }
}

// 进入练习模式或模拟考试
function selectMode(mode: "practice" | "exam") {
  if (!bankInfo.value.id) return
  if (mode === "practice") {
    router.push({ path: "/question-bank/practice", query: { bank: bankInfo.value.id } })
  } else {
    router.push({ path: "/question-bank/exam", query: { bank: bankInfo.value.id } })
  }
}

function goBack() {
  if (fromPopular.value) {
    router.push("/")
  } else {
    router.push({ path: "/question-bank" })
  }
}

// 收藏当前题库
async function handleCollect() {
  if (!bankInfo.value.id || bankInfo.value.isCollected || collecting.value) return
  collecting.value = true
  try {
    const res = await addUserQuestionBank(String(bankInfo.value.id))
    if (res.code === 200) {
      bankInfo.value.isCollected = true
      ElMessage.success("收藏成功")
    }
  } catch (error) {
    console.error("收藏题库失败:", error)
  } finally {
    collecting.value = false
  }
}

onMounted(() => {
  fromPopular.value = route.query.from === "popular"
  getBankInfo()
  window.scrollTo(0, 0)
})
</script>

<template>
  <div class="bank-detail-page">
    <!-- 顶部极简返回条 -->
    <div class="top-nav">
      <button type="button" class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ fromPopular ? "返回首页" : "返回我的题库" }}</span>
      </button>
    </div>

    <!-- 题库概览头部卡片 -->
    <section class="hero-card">
      <div v-if="loading" class="hero-skeleton">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else class="hero-layout">
        <div class="hero-cover">
          <img
            :src="getFullImageUrl(bankInfo.coverImage)"
            :alt="bankInfo.name"
            class="cover-img"
            @error="onImgError"
          >
        </div>

        <div class="hero-main">
          <div class="hero-head">
            <div class="hero-title-group">
              <div class="hero-badges">
                <span class="vis-badge" :class="bankInfo.type === 'public' ? 'vis-public' : 'vis-private'">
                  {{ bankInfo.type === "public" ? "公开题库" : "私有题库" }}
                </span>
                <span v-if="bankInfo.difficulty" class="diff-badge" :class="getDifficultyClass(bankInfo.difficulty)">
                  难度 · {{ bankInfo.difficulty }}
                </span>
              </div>
              <h1 class="bank-title">
                {{ bankInfo.name || "未命名题库" }}
              </h1>
            </div>

            <button
              type="button"
              class="collect-btn"
              :class="{ 'is-collected': bankInfo.isCollected }"
              :disabled="bankInfo.isCollected || collecting"
              @click="handleCollect"
            >
              <el-icon><Collection /></el-icon>
              <span>{{ bankInfo.isCollected ? "已收藏" : "收藏题库" }}</span>
            </button>
          </div>

          <p class="bank-desc">
            {{ bankInfo.description || "该题库暂未填写简介，可直接开始练习或浏览下方题目预览。" }}
          </p>

          <div class="hero-meta">
            <div class="meta-item">
              <span class="meta-label">创建者</span>
              <span class="meta-value">{{ bankInfo.userName || "平台精选" }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <span class="meta-label">题目总数</span>
              <span class="meta-value">{{ bankInfo.questionCount }} 题</span>
            </div>
            <div v-if="bankInfo.questionTypes.length > 0" class="meta-divider" />
            <div v-if="bankInfo.questionTypes.length > 0" class="meta-item">
              <span class="meta-label">包含题型</span>
              <span class="meta-value">{{ bankInfo.questionTypes.join(" / ") }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 选择学习模式 -->
    <section class="mode-section">
      <h2 class="section-heading">
        选择学习模式
      </h2>
      <div class="mode-grid">
        <div class="mode-card" @click="selectMode('practice')">
          <div class="mode-icon is-blue">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="mode-body">
            <h3>练习模式</h3>
            <p>不限时长，支持按题型抽题、背题模式与实时查看答案解析，适合系统巩固知识点。</p>
          </div>
          <el-icon class="mode-arrow">
            <ArrowRight />
          </el-icon>
        </div>

        <div class="mode-card" @click="selectMode('exam')">
          <div class="mode-icon is-emerald">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="mode-body">
            <h3>模拟考试</h3>
            <p>模拟真实考场环境，限时作答、统一交卷判分，全面检验当前掌握程度。</p>
          </div>
          <el-icon class="mode-arrow">
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </section>

    <!-- 题目预览区 -->
    <section class="preview-section">
      <div class="preview-header">
        <div class="preview-title-group">
          <h2 class="section-heading">
            题目预览
          </h2>
          <span class="preview-count">展示前 {{ filteredQuestions.length }} 题</span>
        </div>

        <div v-if="bankInfo.questionTypes.length > 0" class="filter-pills" role="tablist" aria-label="按题型筛选">
          <button
            v-for="type in typeFilterOptions"
            :key="type"
            type="button"
            class="filter-pill"
            :class="{ 'is-active': selectedType === type }"
            @click="selectedType = type"
          >
            {{ type }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="preview-skeleton">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else-if="filteredQuestions.length > 0" class="preview-list">
        <article
          v-for="(question, index) in filteredQuestions"
          :key="index"
          class="question-item"
        >
          <div class="q-header">
            <span class="q-index">{{ index + 1 }}</span>
            <span class="q-type">{{ question.type }}</span>
          </div>

          <div class="q-content">
            {{ question.content }}
          </div>

          <div v-if="question.options && question.options.length > 0" class="q-options">
            <div
              v-for="option in question.options"
              :key="option.key"
              class="opt-row"
            >
              <span class="opt-key">{{ option.key }}</span>
              <span class="opt-text">{{ option.content }}</span>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="preview-empty">
        <el-empty description="暂无题目或当前题型下没有预览数据" :image-size="72" />
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ======== 页面整体（融入 Layout 的 #f8fafc 底色，无多余光球与外框） ======== */
.bank-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #0f172a;
}

/* 顶部返回按钮 */
.top-nav {
  display: flex;
  align-items: center;
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

/* ======== 题库头部信息卡片 ======== */
.hero-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 28px 32px;
}

.hero-layout {
  display: flex;
  gap: 28px;
  align-items: stretch;
}

.hero-cover {
  width: 240px;
  aspect-ratio: 16 / 10;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background-color: #f1f5f9;
  align-self: flex-start;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hero-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.hero-title-group {
  min-width: 0;
}

.hero-badges {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.vis-badge,
.diff-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.vis-public {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.vis-private {
  background-color: #f1f5f9;
  color: #64748b;
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

.bank-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
  line-height: 1.3;
}

.collect-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #2563eb;
  background-color: #2563eb;
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.16s ease;
}

.collect-btn:hover:not(:disabled) {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.collect-btn.is-collected,
.collect-btn:disabled {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  color: #64748b;
  cursor: default;
}

.bank-desc {
  margin: 0 0 20px 0;
  font-size: 14px;
  line-height: 1.65;
  color: #475569;
  flex: 1;
}

.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.meta-label {
  font-size: 12px;
  color: #64748b;
}

.meta-value {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.meta-divider {
  width: 1px;
  height: 14px;
  background-color: #e2e8f0;
}

/* ======== 通用分区标题（无蓝色竖条） ======== */
.section-heading {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: #0f172a;
}

/* ======== 选择学习模式 ======== */
.mode-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.mode-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.mode-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.06);
  transform: translateY(-1px);
}

.mode-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.mode-icon.is-blue {
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  color: #2563eb;
}

.mode-icon.is-emerald {
  background-color: #ecfdf5;
  border: 1px solid #d1fae5;
  color: #059669;
}

.mode-body {
  flex: 1;
  min-width: 0;
}

.mode-body h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.mode-body p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #64748b;
}

.mode-arrow {
  color: #94a3b8;
  font-size: 16px;
  flex-shrink: 0;
  transition:
    transform 0.18s ease,
    color 0.18s ease;
}

.mode-card:hover .mode-arrow {
  color: #2563eb;
  transform: translateX(3px);
}

/* ======== 题目预览区 ======== */
.preview-section {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 24px 28px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.preview-title-group {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.preview-count {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.filter-pills {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
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

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.question-item {
  padding: 18px 20px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.q-index {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background-color: #0f172a;
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.q-type {
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  padding: 2px 8px;
  border-radius: 5px;
}

.q-content {
  font-size: 15px;
  font-weight: 500;
  line-height: 1.65;
  color: #0f172a;
  margin-bottom: 12px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opt-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  font-size: 13px;
}

.opt-key {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background-color: #f1f5f9;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.opt-text {
  color: #334155;
  line-height: 1.55;
}

.preview-empty {
  padding: 20px 0;
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 820px) {
  .hero-layout {
    flex-direction: column;
    gap: 20px;
  }

  .hero-cover {
    width: 100%;
    max-width: 320px;
  }

  .mode-grid {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 600px) {
  .hero-card,
  .preview-section {
    padding: 20px 16px;
  }

  .hero-head {
    flex-direction: column;
  }
}
</style>
