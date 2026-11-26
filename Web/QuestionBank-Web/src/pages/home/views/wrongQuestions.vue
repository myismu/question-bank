<script setup lang="ts">
import type { BankSimpleInfoDTO, PublicWrongQuestionsDTO } from "../apis/types"
import { ArrowLeft, Check, Delete, Search } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import {
  getDistinctBanksByUserId,
  getPageWrongQuestionsList,
  getWrongQuestionStats,
  removeWrongQuestion
} from "../apis/WrongQuestions"
import AnswerViewer from "../component/AnswerViewer.vue"

const router = useRouter()

// 错题列表与分页状态
const publicWrongQuestions = ref<PublicWrongQuestionsDTO[]>([])
const pageNum = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const loading = ref(false)

// 筛选条件
const questionBankId = ref<string | undefined>(undefined)
const questionType = ref<string | undefined>(undefined)
const status = ref<string | undefined>(undefined)

// 下拉数据源与顶部统计
const bankSimpleInfoDTOs = ref<BankSimpleInfoDTO[]>([])
const questionTypes = ["单选题", "多选题", "判断题", "填空题", "简答题"]
const stats = ref({ total: 0, today: 0, reviewed: 0, unreviewed: 0 })

// 英文题型与中文展示映射
const questionTypeMap: Record<string, string> = {
  single: "单选题",
  multiple: "多选题",
  fill: "填空题",
  judge: "判断题",
  short: "简答题"
}

const hasActiveFilters = computed(() =>
  Boolean(questionBankId.value || questionType.value || status.value)
)

// 判断某个选项键是否属于正确答案
function isOptionCorrect(qDTO: PublicWrongQuestionsDTO, key: string): boolean {
  const answer = qDTO.questions.answer
  if (!Array.isArray(answer)) return false
  return answer.some(arr => (Array.isArray(arr) ? arr.includes(key) : arr === key))
}

// 获取错题分页列表
async function fetchWrongQuestions() {
  loading.value = true
  try {
    const typeInEnglish = Object.keys(questionTypeMap).find(key => questionTypeMap[key] === questionType.value) || ""
    const result = await getPageWrongQuestionsList(
      pageNum.value,
      pageSize.value,
      questionBankId.value,
      typeInEnglish,
      status.value
    )
    if (result.code === 200) {
      totalCount.value = result.data.total ?? 0
      if (result.data.records && result.data.records.length > 0) {
        publicWrongQuestions.value = result.data.records.map((q: PublicWrongQuestionsDTO) => {
          q.questions.type = questionTypeMap[q.questions.type] || q.questions.type || "未知题型"
          return q
        })
      } else {
        publicWrongQuestions.value = []
      }
    } else {
      ElMessage.error("获取错题列表失败")
    }
  } catch (error) {
    console.error("获取错题列表失败:", error)
    ElMessage.error("获取错题列表失败")
  } finally {
    loading.value = false
  }
}

// 获取顶部错题统计
async function fetchWrongQuestionStats() {
  try {
    const result = await getWrongQuestionStats()
    if (result.code === 200 && result.data) {
      stats.value = result.data
    }
  } catch (error) {
    console.error("获取错题统计信息失败:", error)
  }
}

// 获取包含错题的题库列表（供筛选下拉框使用）
async function fetchDistinctBanksByUserId() {
  try {
    const result = await getDistinctBanksByUserId()
    if (result.code === 200 && result.data) {
      bankSimpleInfoDTOs.value = result.data
    }
  } catch (error) {
    console.error("获取错题题库列表失败:", error)
  }
}

// 筛选条件变化时回到第 1 页再查询
function handleFilterChange() {
  pageNum.value = 1
  fetchWrongQuestions()
}

// 重置筛选条件
function resetFilters() {
  questionBankId.value = undefined
  questionType.value = undefined
  status.value = undefined
  pageNum.value = 1
  fetchWrongQuestions()
}

// 移除单道错题
function removeQuestion(wrongQuestionsDTO: PublicWrongQuestionsDTO) {
  ElMessageBox.confirm(
    "确定要从错题本中移出这道题目吗？",
    "移出错题本",
    {
      confirmButtonText: "确认移出",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(async () => {
    const result = await removeWrongQuestion(wrongQuestionsDTO.id)
    if (result.code === 200) {
      const index = publicWrongQuestions.value.findIndex(q => q.id === wrongQuestionsDTO.id)
      if (index !== -1) {
        publicWrongQuestions.value.splice(index, 1)
        totalCount.value = Math.max(0, totalCount.value - 1)
        ElMessage.success("已移出错题本")
        fetchWrongQuestionStats()
      }
    } else {
      ElMessage.error("移出失败")
    }
  }).catch(() => {})
}

function goToHome() {
  router.push("/")
}

function handleCurrentChange(val: number) {
  pageNum.value = val
  fetchWrongQuestions()
}

function handleSizeChange(val: number) {
  pageSize.value = val
  pageNum.value = 1
  fetchWrongQuestions()
}

onMounted(async () => {
  await Promise.all([
    fetchWrongQuestions(),
    fetchWrongQuestionStats(),
    fetchDistinctBanksByUserId()
  ])
})
</script>

<template>
  <div class="wrong-page">
    <!-- 顶部返回与页眉 -->
    <header class="page-header">
      <button type="button" class="back-btn" @click="goToHome">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </button>
      <div class="header-text">
        <h1 class="page-title">
          智能错题本
        </h1>
        <p class="page-subtitle">
          自动收录练习与考试中的错题，按题库、题型与复习状态定向攻克薄弱知识点
        </p>
      </div>
    </header>

    <!-- 四列紧凑统计指标卡 -->
    <section class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">错题总数</span>
        <div class="kpi-value">
          {{ stats.total }}
        </div>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">待复习</span>
        <div class="kpi-value is-warn">
          {{ stats.unreviewed }}
        </div>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">已复习</span>
        <div class="kpi-value is-emerald">
          {{ stats.reviewed }}
        </div>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">本日新增</span>
        <div class="kpi-value is-accent">
          {{ stats.today }}
        </div>
      </div>
    </section>

    <!-- 紧凑横向筛选栏 -->
    <section class="filter-bar">
      <div class="filter-controls">
        <el-select
          v-model="questionBankId"
          placeholder="全部题库"
          clearable
          class="filter-select"
          @change="handleFilterChange"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <el-option
            v-for="bank in bankSimpleInfoDTOs"
            :key="bank.bankId"
            :label="bank.bankName"
            :value="bank.bankId"
          />
        </el-select>

        <el-select
          v-model="questionType"
          placeholder="全部题型"
          clearable
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option v-for="type in questionTypes" :key="type" :label="type" :value="type" />
        </el-select>

        <div class="status-pills" role="group" aria-label="按复习状态筛选">
          <button
            type="button"
            class="status-pill"
            :class="{ 'is-active': !status }"
            @click="status = undefined; handleFilterChange()"
          >
            全部状态
          </button>
          <button
            type="button"
            class="status-pill"
            :class="{ 'is-active': status === 'pending' }"
            @click="status = 'pending'; handleFilterChange()"
          >
            待复习
          </button>
          <button
            type="button"
            class="status-pill"
            :class="{ 'is-active': status === 'reviewed' }"
            @click="status = 'reviewed'; handleFilterChange()"
          >
            已复习
          </button>
        </div>
      </div>

      <button v-if="hasActiveFilters" type="button" class="reset-btn" @click="resetFilters">
        重置筛选
      </button>
    </section>

    <!-- 错题卡片列表 -->
    <section class="question-list" v-loading="loading">
      <template v-if="publicWrongQuestions.length > 0">
        <article v-for="(qDTO, index) in publicWrongQuestions" :key="qDTO.id" class="q-card">
          <div class="q-header">
            <div class="q-meta">
              <span class="q-index">{{ (pageNum - 1) * pageSize + index + 1 }}</span>
              <span class="q-type-badge">{{ qDTO.questions.type }}</span>
              <span
                class="q-status-badge"
                :class="qDTO.status === 'reviewed' ? 'is-reviewed' : 'is-pending'"
              >
                {{ qDTO.status === "reviewed" ? "已复习" : "待复习" }}
              </span>
              <span class="q-error-badge">失误 {{ qDTO.errorCount }} 次</span>
            </div>

            <button type="button" class="remove-btn" @click="removeQuestion(qDTO)">
              <el-icon><Delete /></el-icon>
              <span>移出</span>
            </button>
          </div>

          <div class="q-title">
            {{ qDTO.questions.title }}
          </div>

          <div v-if="qDTO.questions.options" class="q-options">
            <div
              v-for="(option, key) in qDTO.questions.options"
              :key="key"
              class="opt-row"
              :class="{ 'is-correct': isOptionCorrect(qDTO, String(key)) }"
            >
              <span class="opt-key">{{ key }}</span>
              <span class="opt-text">{{ option }}</span>
              <el-icon v-if="isOptionCorrect(qDTO, String(key))" class="opt-check">
                <Check />
              </el-icon>
            </div>
          </div>

          <div class="q-analysis">
            <div class="analysis-row">
              <span class="analysis-label">标准答案</span>
              <div class="analysis-val">
                <AnswerViewer :answer="qDTO.questions.answer" :type="qDTO.questions.type" />
              </div>
            </div>
            <div v-if="qDTO.questions.analysis" class="analysis-row">
              <span class="analysis-label">题目解析</span>
              <p class="analysis-text">
                {{ qDTO.questions.analysis }}
              </p>
            </div>
          </div>
        </article>
      </template>

      <div v-else class="empty-panel">
        <el-empty
          :description="hasActiveFilters ? '没有找到符合当前筛选条件的错题' : '目前没有错题记录，继续保持'"
          :image-size="76"
        >
          <button v-if="hasActiveFilters" type="button" class="reset-btn" @click="resetFilters">
            重置筛选条件
          </button>
        </el-empty>
      </div>
    </section>

    <!-- 底部分页 -->
    <div v-if="totalCount > 0" class="pagination-bar">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCount"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
/* ======== 页面整体（融入 Layout 的 #f8fafc 背景） ======== */
.wrong-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #0f172a;
}

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

/* ======== 四列 KPI 统计卡 ======== */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kpi-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.15;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.kpi-value.is-warn {
  color: #d97706;
}

.kpi-value.is-emerald {
  color: #059669;
}

.kpi-value.is-accent {
  color: #2563eb;
}

/* ======== 横向筛选栏 ======== */
.filter-bar {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-select {
  width: 210px;
}

.filter-select :deep(.el-select__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.status-pills {
  display: inline-flex;
  align-items: center;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}

.status-pill {
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

.status-pill:hover {
  color: #0f172a;
}

.status-pill.is-active {
  background-color: #ffffff;
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.reset-btn {
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background-color: #ffffff;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.16s ease;
}

.reset-btn:hover {
  border-color: #94a3b8;
  color: #0f172a;
  background-color: #f8fafc;
}

/* ======== 错题列表 ======== */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.q-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px 24px;
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 14px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.q-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
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

.q-type-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 600;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  color: #1d4ed8;
}

.q-status-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
}

.q-status-badge.is-pending {
  background-color: #fffbeb;
  color: #b45309;
}

.q-status-badge.is-reviewed {
  background-color: #ecfdf5;
  color: #047857;
}

.q-error-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
  background-color: #fef2f2;
  color: #b91c1c;
  font-variant-numeric: tabular-nums;
}

.remove-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border-radius: 7px;
  border: 1px solid transparent;
  background-color: transparent;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.16s ease;
}

.remove-btn:hover {
  background-color: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

.q-title {
  font-size: 16px;
  font-weight: 500;
  line-height: 1.65;
  color: #0f172a;
  margin-bottom: 14px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.opt-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  font-size: 14px;
  color: #334155;
}

.opt-row.is-correct {
  background-color: #f0fdf4;
  border-color: #86efac;
  color: #166534;
}

.opt-key {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.opt-row.is-correct .opt-key {
  background-color: #16a34a;
  border-color: #16a34a;
  color: #ffffff;
}

.opt-text {
  flex: 1;
  line-height: 1.55;
}

.opt-check {
  font-size: 16px;
  margin-top: 3px;
  flex-shrink: 0;
}

/* 解析区 */
.q-analysis {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.analysis-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.analysis-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  min-width: 56px;
  padding-top: 2px;
  flex-shrink: 0;
}

.analysis-val {
  flex: 1;
}

.analysis-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #334155;
  white-space: pre-wrap;
}

.empty-panel {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 40px 20px;
}

/* 分页器 */
.pagination-bar {
  display: flex;
  justify-content: center;
  padding-top: 4px;
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 960px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 640px) {
  .filter-select {
    width: 100%;
  }

  .q-card {
    padding: 16px;
  }
}
</style>
