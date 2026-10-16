<script setup lang="ts">
import type { BankSimpleInfoDTO, PublicWrongQuestionsDTO, WrongQuestionStatsVO } from "../apis/types"
import { ElMessage, ElMessageBox } from "element-plus"
import { onMounted, ref } from "vue"
import { getDistinctBanksByUserId, getPageWrongQuestionsList, getWrongQuestionStats, removeWrongQuestion } from "../apis/WrongQuestions"
import AnswerViewer from "../component/AnswerViewer.vue"
import { Back, Search, Delete, DocumentDelete, List, SuccessFilled, WarningFilled } from "@element-plus/icons-vue"

// 定义一个PublicWrongQuestionsDTO的类型
const publicWrongQuestions = ref<PublicWrongQuestionsDTO[]>([])

const wrongQuestionStatsVO = ref<WrongQuestionStatsVO>()
// 筛选条件,参数：pageNum, pageSize, questionBankId, questionType, status
const pageNum = ref<number>(1)
const pageSize = ref<number>(10)
const questionBankId = ref<string | undefined>(undefined)
const questionType = ref<string | undefined>(undefined)
const status = ref<string | undefined>(undefined)

// 题库列表
const bankSimpleInfoDTOs = ref<BankSimpleInfoDTO[]>([])

// 题型列表
const questionTypes = ref<string[]>(["单选题", "多选题", "判断题"])

// 统计数据
const stats = ref({ total: 0, today: 0, reviewed: 0, unreviewed: 0 })

// 题型映射
const questionTypeMap: Record<string, string> = {
  single: "单选题",
  multiple: "多选题",
  fill: "填空题",
  judge: "判断题",
  short: "简答题"
}

// 添加分页功能
const totalPages = ref<number>(0)
const loading = ref(false)

// 页面加载时获取数据
onMounted(async () => {
  await fetchWrongQuestions()
  await fetchWrongQuestionStats()
  await fetchDistinctBanksByUserId()
})

// 获取错题列表
async function fetchWrongQuestions() {
  loading.value = true
  try {
    const typeInEnglish = Object.keys(questionTypeMap).find(key => questionTypeMap[key] === questionType.value) || ""
    const result = await getPageWrongQuestionsList(pageNum.value, pageSize.value, questionBankId.value, typeInEnglish, status.value)
    if (result.code === 200) {
      totalPages.value = Math.ceil(result.data.total / pageSize.value)
      if (result.data.records && result.data.records.length > 0) {
        publicWrongQuestions.value = result.data.records.map((q: PublicWrongQuestionsDTO) => {
          q.questions.type = questionTypeMap[q.questions.type] || "未知题型"
          return q
        })
      } else {
        publicWrongQuestions.value = []
        ElMessage.warning("没有找到符合条件的错题")
      }
    } else {
      ElMessage.error("获取错题列表失败")
    }
  } finally {
    loading.value = false
  }
}

// 获取错题统计信息
async function fetchWrongQuestionStats() {
  const result = await getWrongQuestionStats()
  if (result.code === 200) {
    wrongQuestionStatsVO.value = result.data
    stats.value = result.data
  } else {
    ElMessage.error("获取错题统计信息失败")
  }
}

// 获取个人错题的题库列表
async function fetchDistinctBanksByUserId() {
  const result = await getDistinctBanksByUserId()
  if (result.code === 200) {
    bankSimpleInfoDTOs.value = result.data
  } else {
    ElMessage.error("获取题库列表失败")
  }
}

// 移除错题
function removeQuestion(wrongQuestionsDTO: PublicWrongQuestionsDTO) {
  ElMessageBox.confirm(
    "确定要从错题本中彻底移除这道题目吗？",
    "移除提示",
    {
      confirmButtonText: "确认移除",
      cancelButtonText: "暂不移除",
      type: "warning",
      confirmButtonClass: "el-button--danger"
    }
  ).then(async () => {
    const result = await removeWrongQuestion(wrongQuestionsDTO.id)
    if (result.code === 200) {
      const index = publicWrongQuestions.value.findIndex(q => q.id === wrongQuestionsDTO.id)
      if (index !== -1) {
        publicWrongQuestions.value.splice(index, 1)
        ElMessage.success("已成功移出错题本")
        fetchWrongQuestionStats() // 更新顶部统计数据
      }
    } else {
      ElMessage.error("移除失败")
    }
  }).catch(() => {})
}

// 跳转首页
function goToHome() {
  window.location.href = "/"
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
</script>

<template>
  <el-main class="page-main">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-button @click="goToHome" icon="Back" size="large" circle type="primary" plain class="back-btn"></el-button>
        <div class="header-titles">
          <h2>智能错题本</h2>
          <p>自动收录错题，针对性复习巩固薄弱知识点</p>
        </div>
      </div>

      <!-- 核心指标卡片 -->
      <el-row :gutter="24" class="stat-cards">
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-red"><el-icon><List /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">错题总数</span>
              <div class="stat-val">{{ stats.total }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-green"><el-icon><SuccessFilled /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">已复习</span>
              <div class="stat-val">{{ stats.reviewed }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-orange"><el-icon><WarningFilled /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">待复习</span>
              <div class="stat-val">{{ stats.unreviewed }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" class="mb-5">
          <div class="stat-card">
            <div class="icon-wrap bg-blue"><el-icon><DocumentDelete /></el-icon></div>
            <div class="stat-info">
              <span class="stat-title">本日新增</span>
              <div class="stat-val">{{ stats.today }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 统一过滤交互区 -->
      <div class="filter-section mb-6">
        <div class="filter-grid">
          <el-select v-model="questionBankId" placeholder="按题库筛选" clearable size="large" class="filter-item" @change="fetchWrongQuestions">
            <template #prefix><el-icon><Search /></el-icon></template>
            <el-option v-for="bank in bankSimpleInfoDTOs" :key="bank.bankId" :label="bank.bankName" :value="bank.bankId" />
          </el-select>
          <el-select v-model="questionType" placeholder="按题型筛选" clearable size="large" class="filter-item" @change="fetchWrongQuestions">
            <el-option v-for="type in questionTypes" :key="type" :label="type" :value="type" />
          </el-select>
          <el-select v-model="status" placeholder="按学习状态筛选" clearable size="large" class="filter-item" @change="fetchWrongQuestions">
            <el-option label="✅ 已掌握/已复习" value="reviewed" />
            <el-option label="⏱️ 待复习/需巩固" value="pending" />
          </el-select>
        </div>
      </div>

      <!-- 错题列表容器 -->
      <div class="question-list" v-loading="loading">
        <template v-if="publicWrongQuestions.length > 0">
          <div v-for="(qDTO, index) in publicWrongQuestions" :key="qDTO.id" class="q-card">
            <div class="q-header">
              <div class="q-tags">
                <el-tag type="primary" effect="plain" class="modern-tag">{{ qDTO.questions.type }}</el-tag>
                <el-tag :type="qDTO.status === 'reviewed' ? 'success' : 'warning'" effect="light" class="modern-tag ml-2">
                  <el-icon class="mr-1"><component :is="qDTO.status === 'reviewed' ? SuccessFilled : WarningFilled" /></el-icon>
                  {{ qDTO.status === 'reviewed' ? '已复习' : '待复习' }}
                </el-tag>
                <div class="q-error-count">
                  <span class="label">失误次数</span>
                  <span class="val">{{ qDTO.errorCount }}</span>
                </div>
              </div>
              <el-button type="danger" text bg size="small" @click="removeQuestion(qDTO)">
                <el-icon class="mr-1"><Delete /></el-icon>移出
              </el-button>
            </div>
            
            <div class="q-body">
              <div class="q-title"><span class="q-idx">{{ (pageNum - 1) * pageSize + index + 1 }}.</span> {{ qDTO.questions.title }}</div>
              <div class="q-options" v-if="qDTO.questions.options">
                <div
                  v-for="(option, key) in qDTO.questions.options"
                  :key="key"
                  class="opt-item"
                  :class="[
                    Array.isArray(qDTO.questions.answer) && qDTO.questions.answer.some(arr => arr.includes(key as string)) 
                    ? 'is-correct' 
                    : qDTO.questions.answer.flat().includes(key as never) 
                      ? 'is-correct' : ''
                  ]"
                >
                  <div class="opt-key">{{ key }}</div>
                  <div class="opt-text">{{ option }}</div>
                </div>
              </div>
            </div>

            <div class="q-analysis-box">
              <AnswerViewer :answer="qDTO.questions.answer" :type="qDTO.questions.type" />
            </div>
          </div>
        </template>
        
        <div v-else class="empty-state">
          <div class="empty-icon"><el-icon><SuccessFilled /></el-icon></div>
          <p class="empty-title">太棒了，目前没有符合条件的错题！</p>
          <p class="empty-desc">继续保持你的学习记录，知识点掌握得很扎实。</p>
        </div>
      </div>

      <!-- 底部现代分页器 -->
      <div class="pagination-wrapper mt-8" v-if="totalPages > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalPages * pageSize" 
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
  max-width: 1100px;
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

/* 间距工具 */
.mb-5 { margin-bottom: 20px; }
.mb-6 { margin-bottom: 24px; }
.mt-8 { margin-top: 32px; }
.ml-2 { margin-left: 8px; }
.mr-1 { margin-right: 4px; }

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
.bg-red { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.bg-green { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.bg-orange { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.bg-blue { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }

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
}

/* 过滤交互区 */
.filter-section {
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  border: 1px solid var(--el-border-color-light);
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
  background-color: var(--el-fill-color-light);
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  background-color: var(--el-bg-color);
}

/* 题目卡片 */
.q-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  border: 1px solid var(--el-border-color-light);
  transition: box-shadow 0.3s;
}
.q-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}
.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}
.q-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.modern-tag {
  border-radius: 6px;
  font-weight: 600;
  border: none;
}
.q-error-count {
  display: inline-flex;
  align-items: center;
  background: #fef2f2;
  border-radius: 6px;
  padding: 0 8px;
  height: 24px;
  border: 1px solid #fee2e2;
}
.q-error-count .label {
  font-size: 12px;
  color: #ef4444;
  margin-right: 6px;
}
.q-error-count .val {
  font-size: 13px;
  font-weight: 700;
  color: #b91c1c;
}
.q-body {
  margin-bottom: 20px;
}
.q-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary, #1d1e23);
  line-height: 1.6;
  margin-bottom: 16px;
}
.q-idx {
  color: var(--el-color-primary);
  font-weight: 700;
  margin-right: 4px;
}
.q-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.opt-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: var(--el-fill-color-light, #f8fafc);
  padding: 12px 16px;
  border-radius: 12px;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.opt-item:hover {
  background: var(--el-fill-color, #f1f5f9);
}
.opt-item.is-correct {
  background: #f0fdf4;
  border-color: #bbf7d0;
}
.opt-item.is-correct .opt-key {
  background: #22c55e;
  color: #fff;
  box-shadow: 0 4px 10px rgba(34,197,94,0.3);
}
.opt-item.is-correct .opt-text {
  color: #166534;
  font-weight: 500;
}
.opt-key {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
  color: var(--el-text-color-secondary);
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
  flex-shrink: 0;
  font-size: 14px;
}
.opt-text {
  padding-top: 3px;
  font-size: 15px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

/* 解析区 */
.q-analysis-box {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 20px;
  border-left: 4px solid var(--el-color-primary);
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
  background: #f0fdf4;
  color: #22c55e;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 40px;
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

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  font-weight: 600;
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .page-main { padding: 16px; }
  .header-titles h2 { font-size: 20px; }
  .stat-val { font-size: 24px; }
  .q-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .q-actions { width: 100%; display: flex; justify-content: flex-end; }
}
</style>
