<script setup lang="ts">
import type { QuestionBank } from "./apis/types"
import defaulttupian from "@@/assets/images/questionBank/f-head05.png"
import { Collection, Histogram, Notebook } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { getAllPublicQuestionBanks } from "./apis/QuestionBank"
import { getQuestionsByPage } from "./apis/Questions"

const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL

const carouselSlides = [
  {
    id: 1,
    title: "探索个性化学习新境界",
    subtitle: "海量公共题库 · 智能错题分析 · 随时轻松练习",
    image: "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?q=80&w=1200&auto=format&fit=crop",
    btnText: "立即启程"
  },
  {
    id: 2,
    title: "搭建你的专属知识库",
    subtitle: "一键导入 · 多维标签 · 便捷组卷",
    image: "https://images.unsplash.com/photo-1517048676732-d65bc937f952?q=80&w=1200&auto=format&fit=crop",
    btnText: "创建题库"
  },
  {
    id: 3,
    title: "洞察每一次学习数据",
    subtitle: "数据可视化图表 · 短板精准定位 · 提升肉眼可见",
    image: "https://images.unsplash.com/photo-1456406644174-8ddd4cd52a06?q=80&w=1200&auto=format&fit=crop",
    btnText: "查看学习统计"
  },
  {
    id: 4,
    title: "智能错题本自动收集",
    subtitle: "自动归档错题 · 定期回顾推送 · 拒绝重复犯错",
    image: "https://images.unsplash.com/photo-1531482615713-2afd69097998?q=80&w=1200&auto=format&fit=crop",
    btnText: "访问错题本"
  }
]

function handleSlideClick(slide: any) {
  if (slide.id === 3) {
    goToStatistics()
  } else if (slide.id === 4) {
    goToWrongQuestions()
  } else {
    goToQuestionBank()
  }
}

function getFullImageUrl(url: string | undefined) {
  if (!url) return defaulttupian
  if (url.startsWith("http://") || url.startsWith("https://")) return url
  if (url.startsWith("/img/")) return IMAGE_BASE_URL + url
  return defaulttupian
}

const popularBanks = ref<QuestionBank[]>([])

onMounted(async () => {
  try {
    popularBanks.value = await getAllPublicQuestionBanks()
  } catch {

  }
})

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

const previewDialogVisible = ref(false)
const selectedBank = ref<typeof popularBanks.value[0] | null>(null)

function showBankPreview(bank: QuestionBank) {
  selectedBank.value = bank
  previewDialogVisible.value = true
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
}

function goToQuestionDetail(bankId: string | undefined) {
  if (!bankId) return
  router.push({ name: "BankDetail", query: { id: bankId, from: "popular" } })
}

const router = useRouter()

function goToStatistics() {
  router.push({ name: "Statistics" })
}
function goToWrongQuestions() {
  router.push({ name: "WrongQuestions" })
}

function goToQuestionBank() {
  router.push({ path: "/question-bank/createBank", query: { from: "home" } })
}
function onImgError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target.src !== defaulttupian) {
    target.src = defaulttupian
  }
}
</script>

<template>
  <el-main class="page-main">
    <div class="page-container">
      <!-- 轮播图/横幅区 -->
      <div class="hero-banner">
        <el-carousel :interval="5000" type="card" height="320px" indicator-position="outside">
          <el-carousel-item v-for="slide in carouselSlides" :key="slide.id">
            <div class="hero-item" :style="{ backgroundImage: `url(${slide.image})` }">
              <div class="hero-overlay"></div>
              <div class="hero-content">
                <h2>{{ slide.title }}</h2>
                <p>{{ slide.subtitle }}</p>
                <el-button color="#ffffff" text bg class="hero-btn" @click="handleSlideClick(slide)">
                  {{ slide.btnText }} <el-icon class="ml-1"><Collection /></el-icon>
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 核心功能入口区 -->
      <div class="section-container">
        <h3 class="section-title">核心功能区</h3>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="8" :md="8" :lg="8" class="mb-4">
            <div class="action-card" @click="goToQuestionBank">
              <div class="action-icon-wrapper bg-blue">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="action-info">
                <h4>创建专属题库</h4>
                <p>构建知识体系，随时组卷发布</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8" :md="8" :lg="8" class="mb-4">
            <div class="action-card" @click="goToStatistics">
              <div class="action-icon-wrapper bg-cyan">
                <el-icon><Histogram /></el-icon>
              </div>
              <div class="action-info">
                <h4>深度学习统计</h4>
                <p>分析练习数据，挖掘学习短板</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8" :md="8" :lg="8" class="mb-4">
            <div class="action-card" @click="goToWrongQuestions">
              <div class="action-icon-wrapper bg-orange">
                <el-icon><Notebook /></el-icon>
              </div>
              <div class="action-info">
                <h4>智能错题本</h4>
                <p>自动收录错题，针对性复习巩固</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 公共题库推荐区 -->
      <div class="section-container mt-8">
        <div class="section-header">
          <h3 class="section-title">精选公共题库</h3>
          <el-button type="primary" link>查看更多</el-button>
        </div>
        <el-row :gutter="24">
          <el-col
            :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
            v-for="bank in popularBanks"
            :key="bank.id"
            class="mb-6"
          >
            <div class="pub-bank-card" @click="showBankPreview(bank)">
              <div class="bank-img-box">
                <img :src="getFullImageUrl(bank.image)" class="bank-img" @error="onImgError">
                <div class="bank-meta-overlay">
                  <el-tag :type="getDifficultyType(bank.difficulty)" effect="dark" size="small" round>
                    {{ bank.difficulty }}
                  </el-tag>
                </div>
              </div>
              <div class="bank-content">
                <h4 class="bank-name" :title="bank.name">{{ bank.name }}</h4>
                <p class="bank-desc" :title="bank.description">{{ bank.description || '暂无简介' }}</p>
                <div class="bank-footer">
                  <div class="author">
                    <el-avatar :size="20" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                    <span class="author-name">{{ bank.userName || '未知' }}</span>
                  </div>
                  <div class="count">
                    <span class="num">{{ bank.questionCount }}</span> 题
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 题库预览弹窗 -->
      <el-dialog
        v-model="previewDialogVisible"
        :title="selectedBank?.name"
        width="800px"
        class="modern-dialog"
        destroy-on-close
        :scrollbar="false"
        :lock-scroll="false"
      >
        <div class="preview-scroll-area" v-if="selectedBank">
          <!-- 弹窗顶栏信息 -->
          <div class="preview-header">
            <img :src="getFullImageUrl(selectedBank.image)" class="preview-hero-img" @error="onImgError">
            <div class="preview-hero-meta">
              <el-tag size="large" :type="getDifficultyType(selectedBank.difficulty)" effect="light" round>
                难度 | {{ selectedBank.difficulty }}
              </el-tag>
              <div class="stat-item">
                <span class="stat-val">{{ selectedBank.questionCount }}</span>
                <span class="stat-lbl">道题目</span>
              </div>
              <div class="stat-item">
                <span class="stat-lbl">创建者</span>
                <span class="stat-val author-txt">{{ selectedBank.userName || '匿名作者' }}</span>
              </div>
            </div>
          </div>
          <div class="preview-desc-box">
            <h4>题库简介</h4>
            <p>{{ selectedBank.description || '该题库暂未填写详细介绍。' }}</p>
          </div>

          <!-- 预览题目 -->
          <div class="preview-q-list">
            <h4>部分题目预览</h4>
            <div v-for="(question, index) in selectedBank.previewQuestions" :key="index" class="q-card">
              <div class="q-header">
                <el-tag type="info" effect="plain" size="small">{{ question.type }}</el-tag>
                <el-tag :type="getDifficultyType(question.difficulty)" size="small" style="margin-left: 8px;">
                  {{ question.difficulty }}
                </el-tag>
              </div>
              <div class="q-body">
                <div class="q-content">{{ index + 1 }}. {{ question.content }}</div>
                <div v-if="question.options" class="q-options">
                  <div v-for="option in question.options" :key="option.key" class="opt-item">
                    <span class="opt-key">{{ option.key }}</span>
                    <span class="opt-text">{{ option.content }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="q-empty" v-if="!selectedBank.previewQuestions || selectedBank.previewQuestions.length === 0">
              <el-empty description="暂无预览数据或题库为空" :image-size="80" />
            </div>
          </div>
        </div>
        <template #footer>
          <div class="dialog-foot">
            <el-button round size="large" @click="previewDialogVisible = false">稍微考虑</el-button>
            <el-button type="primary" round size="large" @click="goToQuestionDetail(selectedBank?.id)">
              马上开始挑战
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </el-main>
</template>

<style scoped>
/* 全局基础与容器 */
.page-main {
  background-color: var(--el-bg-color-page, #f3f5f9);
  min-height: 100vh;
  padding: 24px;
}
.page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* 标题规范 */
.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1d1e23);
  margin: 0 0 20px 0;
  position: relative;
  padding-left: 14px;
}
.section-title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: var(--el-color-primary);
  border-radius: 2px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-header .section-title {
  margin-bottom: 0;
}

/* 模块间距 */
.mb-4 { margin-bottom: 16px; }
.mb-6 { margin-bottom: 24px; }
.mt-8 { margin-top: 32px; }
.ml-1 { margin-left: 4px; }

/* ======== 轮播区 ======== */
.hero-banner {
  margin-bottom: 40px;
}
.hero-item {
  height: 100%;
  border-radius: 20px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.2) 60%, rgba(0, 0, 0, 0.4) 100%);
  border-radius: 20px;
}
.hero-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 0 20px;
}
.hero-content h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.hero-content p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 24px 0;
}
.hero-btn {
  font-weight: bold;
  border-radius: 20px;
  padding: 8px 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}
:deep(.el-carousel__mask) {
  border-radius: 20px;
}

/* ======== 功能卡片区 ======== */
.action-card {
  background: var(--el-bg-color, #ffffff);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--el-border-color-light, #ebeef5);
}
.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}
.action-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  flex-shrink: 0;
}
.bg-blue { background: rgba(64, 158, 255, 0.1); color: #409eff; }
.bg-cyan { background: rgba(54, 209, 220, 0.1); color: #36d1dc; }
.bg-orange { background: rgba(250, 140, 22, 0.1); color: #fa8c16; }
.action-info h4 {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.action-info p {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

/* ======== 公共题库卡片 ======== */
.pub-bank-card {
  background: var(--el-bg-color, #ffffff);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--el-border-color-light, #ebeef5);
  display: flex;
  flex-direction: column;
  height: 100%;
}
.pub-bank-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}
.bank-img-box {
  width: 100%;
  height: 160px;
  position: relative;
  background: var(--el-fill-color-light, #f5f7fa);
  overflow: hidden;
}
.bank-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.pub-bank-card:hover .bank-img {
  transform: scale(1.05);
}
.bank-meta-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}
.bank-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.bank-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1d1e23);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bank-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary, #6b7280);
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.bank-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-extra-light, #f0f2f5);
}
.author {
  display: flex;
  align-items: center;
  gap: 8px;
}
.author-name {
  font-size: 12px;
  color: var(--el-text-color-regular, #4b5563);
}
.count {
  font-size: 12px;
  color: var(--el-text-color-secondary, #9ca3af);
  background: var(--el-fill-color-light, #f3f4f6);
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}
.count .num {
  color: var(--el-color-primary, #3b82f6);
  font-weight: 600;
  font-size: 13px;
}

/* ======== 现代风弹框样式 ======== */
:deep(.modern-dialog) {
  border-radius: 20px;
  overflow: hidden;
}
:deep(.modern-dialog .el-dialog__header) {
  padding: 20px 24px 0;
  border-bottom: none;
  font-weight: 600;
}
:deep(.modern-dialog .el-dialog__body) {
  padding: 0;
}
.preview-scroll-area {
  max-height: 65vh;
  overflow-y: auto;
  padding: 20px 24px;
}
.preview-header {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  align-items: flex-start;
}
.preview-hero-img {
  width: 200px;
  height: 140px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.preview-hero-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
}
.stat-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.stat-val {
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1;
}
.stat-lbl {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.author-txt {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-color-primary);
}
.preview-desc-box {
  background: var(--el-fill-color-light, #f8fafc);
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 32px;
  border: 1px solid var(--el-border-color-light);
}
.preview-desc-box h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: var(--el-text-color-primary);
}
.preview-desc-box p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}
.preview-q-list h4 {
  font-size: 16px;
  margin: 0 0 16px 0;
  color: var(--el-text-color-primary);
  padding-left: 10px;
  position: relative;
}
.preview-q-list h4::before {
  content: "";
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: var(--el-color-primary);
  border-radius: 2px;
}
.q-card {
  background: #fff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.q-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.q-content {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
  line-height: 1.5;
}
.q-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.opt-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: var(--el-fill-color-light, #f8fafc);
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  transition: background 0.2s;
}
.opt-item:hover {
  background: var(--el-fill-color-lighter, #f1f5f9);
}
.opt-key {
  font-weight: 700;
  color: var(--el-text-color-secondary);
  background: #fff;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  flex-shrink: 0;
}
.opt-text {
  color: var(--el-text-color-regular);
  line-height: 1.6;
  padding-top: 1px;
}
.dialog-foot {
  padding: 16px 24px 20px;
  text-align: right;
  border-top: 1px solid var(--el-border-color-light);
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .page-main {
    padding: 16px;
  }
  .hero-content h2 {
    font-size: 20px;
  }
  .preview-header {
    flex-direction: column;
    gap: 16px;
  }
  .preview-hero-img {
    width: 100%;
    height: auto;
    aspect-ratio: 16/9;
  }
}
</style>
