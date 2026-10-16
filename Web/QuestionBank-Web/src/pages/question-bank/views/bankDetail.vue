<script setup lang="ts">
import { Collection, Document, Edit, Reading, Warning, ArrowLeft, View as ViewIcon } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { addUserQuestionBank, getPublicQuestionBankById } from "../apis/QuestionBank"
import { getQuestionsByBankId, getQuestionsByPage } from "../apis/Questions"
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
  viewCount?: number
  starCount?: number
  isPublic?: boolean
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
const selectedMode = ref("")
const selectedType = ref("")
const fromPopular = ref(false)

const filteredQuestions = computed((): Question[] => {
  let questions = bankInfo.value.questions
  if (selectedType.value) {
    questions = questions.filter(q => q.type === selectedType.value)
  }
  return questions
})

async function getBankInfo() {
  const bankId = String(route.query.id)
  if (!bankId) return
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
  } catch {
    bankInfo.value = {
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
    }
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

function selectMode(mode: string) {
  selectedMode.value = mode
  if (mode === "practice") {
    router.push({ path: "/question-bank/practice", query: { bank: bankInfo.value.id } })
  } else if (mode === "exam") {
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

function handleCollect() {
  if (!bankInfo.value.id) return
  addUserQuestionBank(String(bankInfo.value.id))
    .then((res) => {
      if (res.code === 200) {
        bankInfo.value.isCollected = true
        ElMessage.success("收藏成功！")
      }
    })
}

onMounted(() => {
  fromPopular.value = route.query.from === "popular"
  getBankInfo()
  window.scrollTo(0, 0)
})
</script>

<template>
  <div class="bank-detail-wrapper">
    <!-- 背景装饰 (玻璃拟态的柔和渐变) -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <main class="main-content">
      <div class="page-container">
        <!-- 返回操作条 -->
        <div class="top-bar">
          <el-button @click="goBack" class="btn-back" plain>
            <el-icon><ArrowLeft /></el-icon>
            {{ fromPopular ? '返回首页' : '返回题库列表' }}
          </el-button>
        </div>

        <!-- 头部信息卡片 -->
        <div class="hero-card">
          <div class="hero-cover">
            <el-image
              v-if="bankInfo.coverImage"
              :src="IMAGE_BASE_URL + bankInfo.coverImage"
              fit="cover"
              class="cover-img"
              :preview-src-list="[IMAGE_BASE_URL + bankInfo.coverImage]"
              preview-teleported
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon :size="32"><Document /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="cover-placeholder">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="type-badge">{{ bankInfo.type === 'public' ? '公开' : '私有' }}</div>
          </div>

          <div class="hero-content">
            <div class="hero-header">
              <h1 class="bank-title">{{ bankInfo.name || '加载中...' }}</h1>
              <el-button
                class="btn-collect"
                :class="{ 'is-collected': bankInfo.isCollected }"
                @click="handleCollect"
                :disabled="bankInfo.isCollected"
              >
                <el-icon><Collection /></el-icon>
                {{ bankInfo.isCollected ? '已收藏' : '收藏题库' }}
              </el-button>
            </div>

            <p class="bank-desc">{{ bankInfo.description || '暂无描述' }}</p>

            <div class="meta-tags">
              <div class="meta-tag">
                <el-icon><Document /></el-icon>
                <span>作者：{{ bankInfo.userName }}</span>
              </div>
              <div class="meta-tag" :class="'difficulty-' + bankInfo.difficulty">
                <el-icon><Warning /></el-icon>
                <span>难度：{{ bankInfo.difficulty }}</span>
              </div>
              <div class="meta-tag">
                <el-icon><ViewIcon /></el-icon>
                <span>题目总数：{{ bankInfo.questionCount }} 题</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 模式选择 -->
        <section class="mode-section">
          <h2 class="section-title">选择学习模式</h2>
          <div class="mode-grid">
            <div 
              class="mode-card practice-card" 
              :class="{ active: selectedMode === 'practice' }" 
              @click="selectMode('practice')"
            >
              <div class="card-bg"></div>
              <div class="card-content">
                <div class="icon-wrapper">
                  <el-icon><Reading /></el-icon>
                </div>
                <div>
                  <h3 class="mode-title">练习模式</h3>
                  <p class="mode-desc">无时间限制，作答后即时查看解析，适合巩固知识点。</p>
                </div>
              </div>
            </div>

            <div 
              class="mode-card exam-card" 
              :class="{ active: selectedMode === 'exam' }" 
              @click="selectMode('exam')"
            >
              <div class="card-bg"></div>
              <div class="card-content">
                <div class="icon-wrapper">
                  <el-icon><Edit /></el-icon>
                </div>
                <div>
                  <h3 class="mode-title">模拟考试</h3>
                  <p class="mode-desc">模拟真实考试环境，限时交卷，全面评估掌握程度。</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 题目预览 -->
        <section class="preview-section">
          <div class="section-header">
            <h2 class="section-title">题目预览</h2>
            <div class="filter-wrap">
              <el-select 
                v-model="selectedType" 
                placeholder="全部题型" 
                clearable
                class="type-select"
              >
                <el-option
                  v-for="type in bankInfo.questionTypes"
                  :key="type"
                  :label="type"
                  :value="type"
                />
              </el-select>
            </div>
          </div>

          <div class="preview-list">
            <div 
              v-for="(question, index) in filteredQuestions" 
              :key="index" 
              class="question-item"
            >
              <div class="q-header">
                <span class="q-number">{{ index + 1 }}</span>
                <span class="q-type">{{ question.type }}</span>
              </div>
              <div class="q-content">{{ question.content }}</div>
              
              <div v-if="question.options && question.options.length > 0" class="q-options">
                <div 
                  v-for="option in question.options" 
                  :key="option.key" 
                  class="option-item"
                >
                  <span class="opt-key">{{ option.key }}.</span>
                  <span class="opt-content">{{ option.content }}</span>
                </div>
              </div>
            </div>
            
            <el-empty 
              v-if="filteredQuestions.length === 0" 
              description="暂无题目或没有符合筛选条件的题目"
            />
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<style scoped>
/* 字体与全局设置 */
.bank-detail-wrapper {
  min-height: 100vh;
  background-color: #f8fafc;
  position: relative;
  overflow-x: hidden;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  padding-bottom: 60px;
}

/* 玻璃拟态装饰背景 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 600px;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}
.blob {
  position: absolute;
  filter: blur(90px);
  opacity: 0.4;
  border-radius: 50%;
  animation: float 12s ease-in-out infinite alternate;
}
.blob-1 {
  top: -150px;
  left: -100px;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
}
.blob-2 {
  top: 50px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  animation-delay: -6s;
}

@keyframes float {
  0% { transform: translateY(0) scale(1); }
  100% { transform: translateY(40px) scale(1.05); }
}

/* 容器布局 */
.main-content {
  position: relative;
  z-index: 1;
  padding-top: 32px;
}
.page-container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 顶部操作条 */
.top-bar {
  margin-bottom: 24px;
}
.btn-back {
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
  color: #475569;
  font-weight: 500;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-back:hover {
  background: #ffffff;
  color: #3b82f6;
  border-color: #bfdbfe;
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05);
}

/* 英雄头部卡片 */
.hero-card {
  display: flex;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  gap: 40px;
  box-shadow: 0 20px 40px -15px rgba(0,0,0,0.05);
  margin-bottom: 48px;
}

.hero-cover {
  width: 240px;
  height: 160px;
  flex-shrink: 0;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 12px 24px rgba(0,0,0,0.06);
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.hero-cover:hover .cover-img {
  transform: scale(1.08);
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(15, 23, 42, 0.65);
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255,255,255,0.15);
}

.hero-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.hero-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.bank-title {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  line-height: 1.25;
  letter-spacing: -0.5px;
}
.btn-collect {
  border-radius: 10px;
  font-weight: 600;
  padding: 12px 20px;
  border: none;
  background: linear-gradient(135deg, #f59e0b, #ea580c);
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.25);
}
.btn-collect:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(245, 158, 11, 0.35);
}
.btn-collect.is-collected {
  background: #f1f5f9;
  color: #64748b;
  box-shadow: none;
}

.bank-desc {
  color: #475569;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 28px;
  flex: 1;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
}
.meta-tag .el-icon {
  font-size: 16px;
}
.difficulty-简单 { color: #059669; background: #d1fae5; border-color: #a7f3d0; }
.difficulty-中等 { color: #d97706; background: #fef3c7; border-color: #fde68a; }
.difficulty-困难 { color: #dc2626; background: #fee2e2; border-color: #fecaca; }

/* 章节标题 */
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.3px;
}
.section-title::before {
  content: '';
  display: block;
  width: 5px;
  height: 22px;
  background: linear-gradient(180deg, #3b82f6, #2563eb);
  border-radius: 3px;
}

/* 模式选择 */
.mode-section {
  margin-bottom: 48px;
}
.mode-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.mode-card {
  position: relative;
  background: white;
  border-radius: 20px;
  padding: 32px;
  cursor: pointer;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02), 0 2px 4px -1px rgba(0,0,0,0.02);
}
.mode-card .card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.4s ease;
  z-index: 0;
}
.practice-card .card-bg { background: linear-gradient(135deg, #eff6ff, #e0e7ff); }
.exam-card .card-bg { background: linear-gradient(135deg, #f0fdf4, #dcfce7); }

.mode-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.05), 0 8px 10px -6px rgba(0,0,0,0.01);
}
.mode-card:hover .card-bg {
  opacity: 1;
}

.card-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24px;
}
.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
  background: #f8fafc;
  transition: all 0.4s ease;
}
.practice-card .icon-wrapper { color: #3b82f6; }
.exam-card .icon-wrapper { color: #10b981; }

.mode-card:hover .practice-card .icon-wrapper { background: white; box-shadow: 0 8px 16px rgba(59, 130, 246, 0.15); transform: scale(1.05); }
.mode-card:hover .exam-card .icon-wrapper { background: white; box-shadow: 0 8px 16px rgba(16, 185, 129, 0.15); transform: scale(1.05); }

.mode-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}
.mode-desc {
  margin: 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.5;
}

/* 预览部分 */
.preview-section {
  background: white;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.02), 0 4px 6px -4px rgba(0,0,0,0.02);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.type-select {
  width: 180px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-item {
  padding: 28px;
  background: #fbfbfc;
  border-radius: 16px;
  border: 1px solid #f1f5f9;
  transition: all 0.3s;
}
.question-item:hover {
  border-color: #e2e8f0;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}
.q-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.q-number {
  background: #0f172a;
  color: white;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 700;
}
.q-type {
  background: #e0e7ff;
  color: #4338ca;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
}
.q-content {
  font-size: 17px;
  color: #334155;
  line-height: 1.6;
  margin-bottom: 20px;
  font-weight: 500;
}
.q-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-left: 0;
}
.option-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 12px 16px;
  border-radius: 10px;
  background: white;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}
.option-item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}
.opt-key {
  font-weight: 700;
  color: #475569;
  background: #f1f5f9;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 14px;
}
.opt-content {
  color: #475569;
  font-size: 15px;
  line-height: 28px;
}

/* 响应式适配 */
@media (max-width: 860px) {
  .hero-card {
    flex-direction: column;
    padding: 24px;
    gap: 24px;
  }
  .hero-cover {
    width: 100%;
    height: 200px;
  }
  .hero-header {
    flex-direction: column;
    gap: 16px;
  }
  .btn-collect {
    width: 100%;
  }
  .mode-grid {
    grid-template-columns: 1fr;
  }
  .preview-section {
    padding: 24px;
  }
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .section-header .section-title {
    margin: 0;
  }
}
</style>
