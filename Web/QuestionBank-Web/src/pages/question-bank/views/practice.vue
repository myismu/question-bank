<script setup lang="ts">
import type { ActivityAnswerDTO, ActivityRecordDTO } from "../apis/type"
import { useUserStore } from "@/pinia/stores/user"
import { ArrowLeft, ArrowRight, Check, Close, Document, Grid, RefreshLeft } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox, ElNotification } from "element-plus"
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import {
  createActivityRecord,
  deleteActivityAnswersByRecordId,
  finishActivityRecord,
  saveOrUpdateBatch,
  selectActivityRecords,
  updateActivityRecord
} from "../apis/activityRecords"
import { getPublicQuestionBankById } from "../apis/QuestionBank"
import { getQuestionsByBankId } from "../apis/Questions"
import { mapToQuestion } from "../apis/type"
import AnswerViewer from "../component/AnswerViewer.vue"

const userStore = useUserStore()
const userId: string = userStore.userId

interface Option {
  key: string
  content: string
}

interface Question {
  id?: string
  type: string
  content: string
  options: Option[]
  answer: string[][]
  analysis: string
}

interface BankInfo {
  id: string | null
  name: string
  difficulty: string
  questionCount: number
  questions: Question[]
}

const router = useRouter()
const route = useRoute()

const bankInfo = ref<BankInfo>({
  id: null,
  name: "",
  difficulty: "",
  questionCount: 0,
  questions: []
})

const practiceSettings = ref({
  questionCount: 10,
  mode: "sequence",
  autoNextCorrect: true
})

// 练习核心状态
const practiceStarted = ref(false)
const currentIndex = ref(0)
const selectedAnswer = ref<string | string[]>("")
const showAnswer = ref(false)
const questionStatus = ref<{ [key: string]: string }>({})
const answerHistory = ref<{ [key: string]: string | string[] }>({})
const isCompleted = ref(false)

// 当前题目信息
const currentQuestion = computed(() => {
  if (!bankInfo.value.questions.length) {
    return {
      type: "",
      content: "",
      options: [],
      answer: [],
      analysis: ""
    } as Question
  }
  return bankInfo.value.questions[currentIndex.value]
})

// 答题卡实时进度与对错统计
const answeredCount = computed(() => Object.keys(questionStatus.value).length)
const correctCount = computed(() =>
  Object.values(questionStatus.value).filter(status => status === "answered").length
)
const wrongCount = computed(() =>
  Object.values(questionStatus.value).filter(status => status === "wrong").length
)
const unansweredCount = computed(() =>
  Math.max(0, bankInfo.value.questions.length - answeredCount.value)
)

const progressText = computed(() => `${answeredCount.value}/${bankInfo.value.questions.length}`)

const progressPercentage = computed(() => {
  const total = practiceSettings.value.questionCount || bankInfo.value.questions.length
  if (!total) return 0
  return Math.min(100, (answeredCount.value / total) * 100)
})

function progressFormat(percentage: number) {
  return `${percentage.toFixed(0)}%`
}

// 按题型分组的题号索引
const questionTypeGroups = computed(() => {
  const groups: Record<string, number[]> = {}
  bankInfo.value.questions.forEach((q: Question, idx: number) => {
    if (!groups[q.type]) groups[q.type] = []
    groups[q.type].push(idx)
  })
  return groups
})

const groupedQuestionOrder = computed(() => bankInfo.value.questions.map((_q, idx) => idx))

const currentPosition = computed(() => groupedQuestionOrder.value.indexOf(currentIndex.value))
const canGoPrev = computed(() => currentPosition.value > 0)

// 判断是否所有题目都已作答
const allAnswered = computed(() =>
  practiceSettings.value.questionCount > 0
  && Object.keys(questionStatus.value).length === practiceSettings.value.questionCount
  && Object.values(questionStatus.value).every((s: string) => s === "answered" || s === "wrong")
)

// 弹窗控制状态
const handInDialogVisible = ref(false)
const handsidebarDialogVisible = ref(false)
const exitDialogVisible = ref(false)
const finishDialogVisible = ref(false)
const hasClearedHistory = ref(false)

// 当前活动记录 ID 与持久化控制句柄
const currentActivityId = ref<string>("")
let autoSaveInterval: ReturnType<typeof setInterval> | null = null
let unwatch: (() => void) | null = null
let savedByRouteGuard = false

const questionTypeOrder = ["单选题", "多选题", "判断题", "填空题", "简答题"]

// 难度数字转中文
function mapDifficultyLabel(num: number | undefined): string {
  switch (num) {
    case 1:
      return "简单"
    case 2:
      return "中等"
    case 3:
    case 4:
      return "困难"
    default:
      return ""
  }
}

// 难度样式类
function getDifficultyClass(difficulty: string): string {
  const map: Record<string, string> = {
    简单: "diff-easy",
    中等: "diff-medium",
    困难: "diff-hard"
  }
  return map[difficulty] || "diff-default"
}

// 页面隐藏时保存答题记录
function handleVisibilityChange() {
  if (document.visibilityState === "hidden" && practiceStarted.value && currentActivityId.value) {
    saveAnswersToServer()
  }
}

// 浏览器刷新/关闭前提醒
function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (practiceStarted.value && currentActivityId.value) {
    const message = "您有未保存的答题记录，确认离开吗？"
    event.preventDefault()
    event.returnValue = message
    return message
  }
}

// 网络恢复时自动补传答题记录
async function handleOnline() {
  if (practiceStarted.value && currentActivityId.value) {
    await saveAnswersToServer()
  }
}

onMounted(() => {
  if (bankInfo.value.questions.length === 0) {
    getBankInfo(true)
  }

  // 路由离开前自动持久化当前答题记录
  unwatch = router.beforeEach(async (_to, from) => {
    if (from.path.includes("practice") && practiceStarted.value && currentActivityId.value) {
      try {
        savedByRouteGuard = true
        await saveAnswersToServer()
        return true
      } catch (err) {
        console.error("路由离开时保存答题记录失败:", err)
        return true
      }
    }
    return true
  })

  document.addEventListener("visibilitychange", handleVisibilityChange)
  window.addEventListener("beforeunload", handleBeforeUnload)
  window.addEventListener("online", handleOnline)

  ElNotification({
    title: "练习模式提示",
    message: "可自定义题型与抽题数量，支持实时自动保存进度与错题自动归档。",
    type: "info",
    duration: 4000
  })
})

onBeforeUnmount(() => {
  if (unwatch) {
    unwatch()
    unwatch = null
  }
  stopAutoSave()
  document.removeEventListener("visibilitychange", handleVisibilityChange)
  window.removeEventListener("beforeunload", handleBeforeUnload)
  window.removeEventListener("online", handleOnline)

  if (practiceStarted.value && currentActivityId.value && !savedByRouteGuard) {
    saveAnswersToServer()
  }
})

// 获取题库信息、题目列表并尝试恢复未完成的历史练习记录
async function getBankInfo(shouldQueryHistory = true) {
  const bankId = String(route.query.bank || "")
  if (!bankId) {
    ElMessage.error("题库不存在")
    setTimeout(() => {
      router.push("/question-bank")
    }, 1000)
    return
  }
  try {
    const [questions, detailResult] = await Promise.allSettled([
      getQuestionsByBankId(bankId),
      getPublicQuestionBankById(bankId)
    ])

    if (questions.status !== "fulfilled") {
      throw questions.reason
    }

    const bankDetail = detailResult.status === "fulfilled" ? detailResult.value : null
    bankInfo.value = {
      id: bankId,
      name: bankDetail?.name || "专项练习",
      difficulty: mapDifficultyLabel(bankDetail?.difficulty),
      questionCount: questions.value.length,
      questions: questions.value.map(mapToQuestion)
    }

    // 按题型顺序排列题目
    bankInfo.value.questions.sort((a, b) =>
      questionTypeOrder.indexOf(a.type) - questionTypeOrder.indexOf(b.type)
    )

    practiceSettings.value.questionCount = questions.value.length

    // 查询是否存在未完成的历史练习记录
    if (shouldQueryHistory) {
      try {
        const activityRecord = await selectActivityRecords(bankId, "practice")
        if (activityRecord && activityRecord.id) {
          currentActivityId.value = activityRecord.id
          practiceStarted.value = true

          if (activityRecord.activityAnswersList && activityRecord.activityAnswersList.length > 0) {
            bankInfo.value.questions = bankInfo.value.questions.filter(q =>
              activityRecord.questionIds?.includes(q.id as string)
            )

            activityRecord.activityAnswersList.forEach((activityAnswer: any) => {
              const index = bankInfo.value.questions.findIndex((q: any) => String(q.id) === activityAnswer.questionId)
              if (index !== -1) {
                answerHistory.value[activityAnswer.questionId] = activityAnswer.userAnswer
                questionStatus.value[activityAnswer.questionId] = activityAnswer.isCorrect === 1 ? "answered" : "wrong"
              }
            })

            currentIndex.value = activityRecord.currentQuestionIndex || 0
            resetSelectedAnswer()
            const curId = String(bankInfo.value.questions[currentIndex.value]?.id || "")
            showAnswer.value = questionStatus.value[curId] === "answered" || questionStatus.value[curId] === "wrong"
          }

          const questionIds = activityRecord.questionIds || []
          const questionMap = new Map(bankInfo.value.questions.map(q => [String(q.id), q]))
          bankInfo.value.questions = questionIds
            .map(id => questionMap.get(String(id)))
            .filter((q): q is typeof bankInfo.value.questions[0] => !!q)

          practiceSettings.value.questionCount = bankInfo.value.questions.length
          startAutoSave()
        }
      } catch (e) {
        console.error("获取活动记录失败", e)
      }
    }

    bankInfo.value.questions.sort((a, b) =>
      questionTypeOrder.indexOf(a.type) - questionTypeOrder.indexOf(b.type)
    )
  } catch (error) {
    console.error("题库加载失败:", error)
    ElMessage.error("题库加载失败")
    setTimeout(() => {
      router.push("/question-bank")
    }, 1000)
  }
}

// 每 60 秒自动保存一次答题进度
function startAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
  }
  autoSaveInterval = setInterval(async () => {
    if (practiceStarted.value && currentActivityId.value) {
      try {
        await saveAnswersToServer()
      } catch (err) {
        console.error("自动保存答题记录失败:", err)
      }
    }
  }, 60000)
}

function stopAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
    autoSaveInterval = null
  }
}

// 批量保存已作答题目到后端
async function saveAnswersToServer() {
  if (!currentActivityId.value || !practiceStarted.value) return

  try {
    const answers: ActivityAnswerDTO[] = []

    bankInfo.value.questions.forEach((q: any) => {
      if (questionStatus.value[String(q.id)] === "answered" || questionStatus.value[String(q.id)] === "wrong") {
        if (!q.id) return

        answers.push({
          recordId: currentActivityId.value,
          questionId: String(q.id),
          userAnswer: Array.isArray(answerHistory.value[String(q.id)])
            ? answerHistory.value[String(q.id)] as string[]
            : [answerHistory.value[String(q.id)] as string],
          isCorrect: questionStatus.value[String(q.id)] === "answered" ? 1 : 0,
          answerTime: new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000)
            .toISOString()
            .slice(0, 19)
            .replace("T", " ")
        })
      }
    })

    if (answers.length > 0) {
      await saveOrUpdateBatch(answers)
    }
  } catch (e) {
    console.error("保存答题记录失败", e)
  }
}

// 分题型抽取数量配置
const selectedQuestionCounts = reactive<{ [key: string]: number }>({
  single: 0,
  multiple: 0,
  fill: 0,
  judge: 0,
  short: 0
})

function updateSelectedQuestionCount(type: keyof typeof selectedQuestionCounts, count: number) {
  selectedQuestionCounts[type] = count
}

// 根据配置抽取题目并创建练习活动记录
async function startPractice() {
  const totalQuestionCount = Object.values(selectedQuestionCounts).reduce((total, count) => total + count, 0)
  if (totalQuestionCount <= 0) {
    ElMessage.warning("请至少选择 1 道题目后再开始练习")
    return
  }

  practiceSettings.value.questionCount = totalQuestionCount
  bankInfo.value.questionCount = totalQuestionCount

  const pool = [...bankInfo.value.questions]
  const selectedQuestions = practiceSettings.value.mode === "random"
    ? pool.sort(() => Math.random() - 0.5)
    : pool

  const selected: Question[] = []
  Object.entries(selectedQuestionCounts).forEach(([type, count]) => {
    if (count > 0) {
      const questionsOfType = selectedQuestions.filter(q => q.type === type)
      selected.push(...questionsOfType.slice(0, count))
    }
  })

  bankInfo.value.questions = selected

  const record: ActivityRecordDTO = {
    userId,
    bankId: bankInfo.value.id!,
    type: "practice",
    startTime: "",
    endTime: "",
    status: "in_progress",
    questionCount: selected.length,
    currentQuestionIndex: 0,
    questionIds: selected.map(q => q.id).filter((id): id is string => id !== undefined),
    questionTypes: selected.map(q => q.type)
  }

  currentActivityId.value = await createActivityRecord(record)
  practiceStarted.value = true
  questionStatus.value = {}
  answerHistory.value = {}
  currentIndex.value = 0
  showAnswer.value = false
  selectedAnswer.value = ""

  startAutoSave()
}

// 更新当前题号索引到服务器
async function updateCurrentQuestionIndex() {
  if (currentActivityId.value) {
    await updateActivityRecord({
      id: currentActivityId.value,
      userId,
      bankId: bankInfo.value.id!,
      type: "practice",
      startTime: "",
      endTime: "",
      status: "in_progress",
      currentQuestionIndex: currentIndex.value
    })
  }
}

// 根据当前题目类型恢复或重置已选答案
function resetSelectedAnswer() {
  const currentQuestionObj = bankInfo.value.questions[currentIndex.value]
  if (!currentQuestionObj?.id) return
  const currentQuestionId = currentQuestionObj.id as string

  if (currentQuestion.value.type === "填空题" || currentQuestion.value.type === "多选题") {
    selectedAnswer.value = Array.isArray(answerHistory.value[currentQuestionId])
      ? [...(answerHistory.value[currentQuestionId] as string[])]
      : []
  } else {
    selectedAnswer.value = answerHistory.value[currentQuestionId]
      ? answerHistory.value[currentQuestionId][0]
      : ""
  }
}

function isSelected(key: string) {
  if (currentQuestion.value.type === "多选题") {
    return Array.isArray(selectedAnswer.value) && selectedAnswer.value.includes(key)
  }
  return selectedAnswer.value === key
}

// 选项点击事件：单选与判断题选中即刻判分，多选题切换勾选状态
function selectOption(key: string) {
  if (showAnswer.value) return
  if (currentQuestion.value.type === "多选题") {
    if (!Array.isArray(selectedAnswer.value)) selectedAnswer.value = []
    const arr = selectedAnswer.value as string[]
    selectedAnswer.value = arr.includes(key)
      ? arr.filter(k => k !== key)
      : [...arr, key]
  } else {
    selectedAnswer.value = key
    if (["单选题", "判断题"].includes(currentQuestion.value.type)) {
      submitAnswer()
    }
  }
}

// 五种题型判题逻辑
function checkAnswer(userAnswer: string | string[], correctAnswers: string[][]) {
  if (Array.isArray(userAnswer) && currentQuestion.value.type === "多选题") {
    const userAnswerSet = new Set(userAnswer)
    const correctAnswerSet = new Set(correctAnswers.flat())
    return userAnswerSet.size === correctAnswerSet.size
      && [...userAnswerSet].every(answer => correctAnswerSet.has(answer))
  } else if (currentQuestion.value.type === "填空题") {
    const userAnswers = Array.isArray(userAnswer) ? userAnswer : [userAnswer]
    if (userAnswers.length !== correctAnswers.length) {
      return false
    }
    return correctAnswers.every((correctAnswerGroup, index) =>
      correctAnswerGroup
        .map(ans => ans.replace(/\s/g, ""))
        .includes(userAnswers[index].replace(/\s/g, ""))
    )
  } else if (currentQuestion.value.type === "简答题") {
    return true
  } else {
    if (typeof userAnswer === "string") {
      return correctAnswers[0].includes(userAnswer)
    }
    return false
  }
}

// 提交当前题目答案
async function submitAnswer() {
  if (
    (currentQuestion.value.type === "单选题" && !selectedAnswer.value)
    || (currentQuestion.value.type === "多选题" && (!Array.isArray(selectedAnswer.value) || selectedAnswer.value.length === 0))
    || (currentQuestion.value.type === "填空题" && (!Array.isArray(selectedAnswer.value) || (selectedAnswer.value as string[]).some(ans => !ans || ans.trim() === "")))
  ) {
    return
  }

  const currentQuestionId = bankInfo.value.questions[currentIndex.value].id as string
  answerHistory.value[currentQuestionId] = selectedAnswer.value
  const isCorrect = checkAnswer(selectedAnswer.value, currentQuestion.value.answer)
  questionStatus.value[currentQuestionId] = isCorrect ? "answered" : "wrong"

  showAnswer.value = true
  if (isCorrect) {
    ElMessage.success("回答正确")
  } else {
    ElMessage.error("回答有误，可查看下方解析")
  }

  if (practiceSettings.value.autoNextCorrect && isCorrect) {
    setTimeout(() => {
      nextQuestion()
    }, 800)
  }
}

// 提交整次练习记录（完成或放弃）
async function submitPracticeRecord(status: string) {
  if (!currentActivityId.value) return
  try {
    await saveAnswersToServer()
    await finishActivityRecord({
      id: currentActivityId.value,
      userId,
      bankId: bankInfo.value.id!,
      type: "practice",
      startTime: "",
      endTime: "",
      status
    })
    stopAutoSave()
  } catch (e) {
    console.error("提交练习记录失败", e)
    ElMessage.warning("提交练习记录失败")
  }
}

// 上一题
function prevQuestion() {
  const order = groupedQuestionOrder.value
  const curIdxInOrder = order.indexOf(currentIndex.value)
  if (curIdxInOrder > 0) {
    const prevIdx = order[curIdxInOrder - 1]
    currentIndex.value = prevIdx
    resetSelectedAnswer()
    const prevQuestionId = bankInfo.value.questions[prevIdx].id as string
    showAnswer.value = questionStatus.value[prevQuestionId] === "answered"
      || questionStatus.value[prevQuestionId] === "wrong"
  }
}

// 下一题
function nextQuestion() {
  const order = groupedQuestionOrder.value
  const curIdxInOrder = order.indexOf(currentIndex.value)

  if (curIdxInOrder < order.length - 1) {
    const nextIdx = order[curIdxInOrder + 1]
    currentIndex.value = nextIdx
    resetSelectedAnswer()

    const nextQuestionId = bankInfo.value.questions[nextIdx].id as string
    showAnswer.value = questionStatus.value[nextQuestionId] === "answered"
      || questionStatus.value[nextQuestionId] === "wrong"
  } else {
    finishDialogVisible.value = true
  }
}

const currentQuestionIndexCache = ref(0)

// 点击答题卡跳转到指定题号
function goToQuestion(index: number) {
  if (index === currentIndex.value) return
  currentIndex.value = index
  currentQuestionIndexCache.value = index
  resetSelectedAnswer()
  const targetId = String(bankInfo.value.questions[index].id)
  showAnswer.value = questionStatus.value[targetId] === "answered"
    || questionStatus.value[targetId] === "wrong"
  scheduleUpdateCurrentQuestionIndex()
}

function scheduleUpdateCurrentQuestionIndex() {
  if (autoSaveInterval) return
  autoSaveInterval = setInterval(async () => {
    if (currentActivityId.value) {
      await updateCurrentQuestionIndex()
    }
  }, 60000)
}

// 退出与交卷操作
function exitPractice() {
  exitDialogVisible.value = true
}

function saveAndExitPractice() {
  isCompleted.value = false
  exitDialogVisible.value = false
  router.push({ name: "BankDetail", query: { id: bankInfo.value.id } })
}

async function confirmExitPractice() {
  exitDialogVisible.value = false
  await saveAnswersToServer()
  await submitPracticeRecord("abandoned")
  savedByRouteGuard = true
  router.push({ path: "/question-bank/bankDetail", query: { id: bankInfo.value.id } })
}

function cancelExitPractice() {
  exitDialogVisible.value = false
}

function goBack() {
  savedByRouteGuard = true
  router.push({ path: "/question-bank/bankDetail", query: { id: bankInfo.value.id } })
}

function isSingleCorrect(optionKey: string) {
  if (currentQuestion.value.type === "多选题") {
    return currentQuestion.value.answer.some((arr: string[]) => Array.isArray(arr) && arr.includes(optionKey))
  }
  return Array.isArray(currentQuestion.value.answer[0]) && currentQuestion.value.answer[0].includes(optionKey)
}

function isJudgeCorrect(optionKey: string) {
  return Array.isArray(currentQuestion.value.answer[0]) && currentQuestion.value.answer[0].includes(optionKey)
}

function handInPaper() {
  if (!allAnswered.value) {
    ElMessage.warning("请先完成所有题目")
    return
  }
  handInDialogVisible.value = true
}

async function confirmHandIn() {
  handInDialogVisible.value = false
  isCompleted.value = true
  await saveAnswersToServer()
  await submitPracticeRecord("completed")

  ElMessageBox.confirm(
    "您已完成所有题目，是否返回题库详情？",
    "交卷成功",
    {
      confirmButtonText: "返回题库详情",
      cancelButtonText: "留下查看解析",
      type: "success"
    }
  ).then(() => {
    router.push({ name: "BankDetail", query: { id: bankInfo.value.id } })
  }).catch(() => {})

  savedByRouteGuard = true
}

// 清除当前练习记录重新作答
async function clearHistory() {
  try {
    await ElMessageBox.confirm(
      "确定要清除当前已作答的记录并重新开始吗？",
      "重置练习记录",
      {
        confirmButtonText: "确定重置",
        cancelButtonText: "取消",
        type: "warning"
      }
    )
    questionStatus.value = {}
    answerHistory.value = {}
    selectedAnswer.value = ""
    currentIndex.value = 0
    showAnswer.value = false
    hasClearedHistory.value = true
    stopAutoSave()
    await deleteActivityAnswersByRecordId(currentActivityId.value)
    ElMessage.success("答题记录已清除，可以重新开始练习")
  } catch (error) {
    if (error !== "cancel") {
      console.error("清除历史记录失败:", error)
      ElMessage.error("清除失败，请稍后重试")
    }
  }
}

const selectedAnswerString = computed({
  get: () => typeof selectedAnswer.value === "string" ? selectedAnswer.value : "",
  set: (val: string) => selectedAnswer.value = val
})

const selectedAnswerArray = computed({
  get: () => Array.isArray(selectedAnswer.value) ? selectedAnswer.value : [],
  set: (val: string[]) => selectedAnswer.value = val
})

const reciteMode = ref(false)

const questionTypeList = computed(() => {
  const typeMap: Record<string, number> = {}
  bankInfo.value.questions.forEach((q) => {
    if (!typeMap[q.type]) typeMap[q.type] = 0
    typeMap[q.type]++
  })
  return Object.entries(typeMap).map(([type, count]) => ({ type, count }))
})

const examSettings = reactive({
  duration: 60,
  autoNextCorrect: true,
  types: {} as Record<string, { count: number, score: number, max: number }>
})

// 实时汇总用户当前配置的抽题总数
const selectedTotalCount = computed(() =>
  Object.values(examSettings.types).reduce((sum, item) => sum + (item.count || 0), 0)
)

// 一键全选或清空各题型抽题数量
function selectAllTypeCounts() {
  Object.values(examSettings.types).forEach((item) => {
    item.count = item.max
  })
}

function clearAllTypeCounts() {
  Object.values(examSettings.types).forEach((item) => {
    item.count = 0
  })
}

watch(() => bankInfo.value.questions, (questions) => {
  const typeMap: Record<string, number> = {}
  questions.forEach((q) => {
    if (!typeMap[q.type]) typeMap[q.type] = 0
    typeMap[q.type]++
  })
  const newTypes: Record<string, { count: number, score: number, max: number }> = {}
  for (const [type, count] of Object.entries(typeMap)) {
    newTypes[type] = examSettings.types[type]
      ? { ...examSettings.types[type], max: count, count: Math.min(examSettings.types[type].count, count) }
      : { count, score: 1, max: count }
  }
  examSettings.types = newTypes
}, { immediate: true })

watch(() => examSettings.types, (newTypes) => {
  for (const [type, settings] of Object.entries(newTypes)) {
    updateSelectedQuestionCount(type as keyof typeof selectedQuestionCounts, settings.count)
  }
  const totalQuestionCount = Object.values(newTypes).reduce((total, settings) => total + settings.count, 0)
  practiceSettings.value.questionCount = totalQuestionCount
  bankInfo.value.questionCount = totalQuestionCount
}, { deep: true })
</script>

<template>
  <div class="practice-page">
    <!-- 阶段一：练习设置界面 -->
    <section v-if="!practiceStarted" class="setup-shell">
      <div class="setup-card">
        <div class="setup-header">
          <div>
            <div class="setup-eyebrow">
              练习配置
            </div>
            <h1 class="setup-title">
              {{ bankInfo.name || "专项练习" }}
            </h1>
          </div>
          <div class="setup-badges">
            <span class="meta-pill">
              <el-icon><Document /></el-icon>
              <span>题库共 {{ bankInfo.questions.length }} 题</span>
            </span>
            <span v-if="bankInfo.difficulty" class="diff-badge" :class="getDifficultyClass(bankInfo.difficulty)">
              {{ bankInfo.difficulty }}
            </span>
          </div>
        </div>

        <div v-if="bankInfo.questions.length > 0" class="setup-body">
          <!-- 练习顺序选择 -->
          <div class="setup-block">
            <div class="block-head">
              <h3>出题顺序</h3>
              <span class="block-desc">按题库原序逐题攻克，或打乱顺序模拟真实应试</span>
            </div>
            <div class="mode-selector">
              <button
                type="button"
                class="mode-option"
                :class="{ 'is-active': practiceSettings.mode === 'sequence' }"
                @click="practiceSettings.mode = 'sequence'"
              >
                <span class="mode-title">顺序练习</span>
                <span class="mode-desc">按题型与原题序依次练习</span>
              </button>
              <button
                type="button"
                class="mode-option"
                :class="{ 'is-active': practiceSettings.mode === 'random' }"
                @click="practiceSettings.mode = 'random'"
              >
                <span class="mode-title">随机练习</span>
                <span class="mode-desc">随机打乱题目顺序强化记忆</span>
              </button>
            </div>
          </div>

          <!-- 分题型数量抽取 -->
          <div class="setup-block">
            <div class="block-head-row">
              <div>
                <h3>分题型抽题数量</h3>
                <span class="block-desc">本次预计练习 <strong class="count-highlight">{{ selectedTotalCount }}</strong> 题</span>
              </div>
              <div class="quick-select-actions">
                <button type="button" class="link-btn" @click="selectAllTypeCounts">
                  全部选满
                </button>
                <button type="button" class="link-btn" @click="clearAllTypeCounts">
                  清空数量
                </button>
              </div>
            </div>

            <div class="type-grid">
              <div v-for="item in questionTypeList" :key="item.type" class="type-item">
                <div class="type-info">
                  <span class="type-name">{{ item.type }}</span>
                  <span class="type-max">可选上限 {{ item.count }} 题</span>
                </div>
                <el-input-number
                  v-model="examSettings.types[item.type].count"
                  :min="0"
                  :max="item.count"
                  :step="1"
                  controls-position="right"
                  size="default"
                />
              </div>
            </div>
          </div>

          <div class="setup-footer">
            <button type="button" class="btn-secondary" @click="goBack">
              返回题库
            </button>
            <button
              type="button"
              class="btn-primary"
              :disabled="selectedTotalCount <= 0"
              @click="startPractice"
            >
              <span>开始练习（{{ selectedTotalCount }} 题）</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>

        <div v-else class="setup-loading">
          <el-skeleton :rows="5" animated />
        </div>
      </div>
    </section>

    <!-- 阶段二：专注刷题主界面 -->
    <section v-else class="workspace-grid">
      <!-- 左侧题目主卡片 -->
      <div class="question-card">
        <!-- 顶部工具栏 -->
        <div class="question-topbar">
          <div class="topbar-left">
            <span class="type-badge">{{ currentQuestion.type }}</span>
            <span class="index-indicator">
              第 <strong>{{ currentPosition + 1 }}</strong> / {{ bankInfo.questions.length }} 题
            </span>
          </div>

          <div class="topbar-right">
            <div class="switch-controls">
              <label class="switch-label">
                <span>背题</span>
                <el-switch
                  v-model="reciteMode"
                  size="small"
                  style="--el-switch-on-color: #10b981; --el-switch-off-color: #cbd5e1;"
                />
              </label>
              <label class="switch-label">
                <span>答对切题</span>
                <el-switch
                  v-model="practiceSettings.autoNextCorrect"
                  size="small"
                  style="--el-switch-on-color: #2563eb; --el-switch-off-color: #cbd5e1;"
                />
              </label>
            </div>

            <div class="topbar-buttons">
              <button
                v-if="allAnswered && !isCompleted"
                type="button"
                class="btn-success btn-sm"
                @click="handInPaper"
              >
                交卷完成
              </button>
              <button
                v-if="allAnswered"
                type="button"
                class="btn-primary btn-sm"
                @click="goBack"
              >
                返回题库
              </button>
              <button
                v-if="!allAnswered"
                type="button"
                class="btn-ghost btn-sm"
                @click="clearHistory"
              >
                <el-icon><RefreshLeft /></el-icon>
                <span>重置</span>
              </button>
              <button
                v-if="!allAnswered"
                type="button"
                class="btn-ghost btn-sm"
                @click="exitPractice"
              >
                退出
              </button>
            </div>
          </div>
        </div>

        <!-- 题干内容 -->
        <div class="question-stem">
          <span class="stem-num">{{ currentPosition + 1 }}.</span>
          <span class="stem-text">{{ currentQuestion.content }}</span>
        </div>

        <!-- 客观题选项区（单选 / 多选 / 判断） -->
        <div v-if="['单选题', '多选题', '判断题'].includes(currentQuestion.type)" class="options-list">
          <template v-if="currentQuestion.options.length">
            <button
              v-for="option in currentQuestion.options"
              :key="option.key"
              type="button"
              class="option-card"
              :class="{
                'is-selected': isSelected(option.key),
                'is-correct': (showAnswer || reciteMode) && isSingleCorrect(option.key),
                'is-wrong': (showAnswer || reciteMode) && isSelected(option.key) && !isSingleCorrect(option.key),
                'is-disabled': reciteMode,
              }"
              @click="reciteMode ? null : selectOption(option.key)"
            >
              <span class="opt-key">{{ option.key }}</span>
              <span class="opt-content">{{ option.content }}</span>
              <el-icon v-if="(showAnswer || reciteMode) && isSingleCorrect(option.key)" class="opt-status-icon">
                <Check />
              </el-icon>
              <el-icon
                v-else-if="(showAnswer || reciteMode) && isSelected(option.key) && !isSingleCorrect(option.key)"
                class="opt-status-icon"
              >
                <Close />
              </el-icon>
            </button>
          </template>

          <template v-else-if="currentQuestion.type === '判断题'">
            <button
              v-for="option in [{ key: 'true', label: 'T', content: '正确' }, { key: 'false', label: 'F', content: '错误' }]"
              :key="option.key"
              type="button"
              class="option-card"
              :class="{
                'is-selected': isSelected(option.key),
                'is-correct': (showAnswer || reciteMode) && isJudgeCorrect(option.key),
                'is-wrong': (showAnswer || reciteMode) && isSelected(option.key) && !isJudgeCorrect(option.key),
                'is-disabled': reciteMode,
              }"
              @click="reciteMode ? null : selectOption(option.key)"
            >
              <span class="opt-key">{{ option.label }}</span>
              <span class="opt-content">{{ option.content }}</span>
              <el-icon v-if="(showAnswer || reciteMode) && isJudgeCorrect(option.key)" class="opt-status-icon">
                <Check />
              </el-icon>
              <el-icon
                v-else-if="(showAnswer || reciteMode) && isSelected(option.key) && !isJudgeCorrect(option.key)"
                class="opt-status-icon"
              >
                <Close />
              </el-icon>
            </button>
          </template>
        </div>

        <!-- 主观题作答区（填空 / 简答） -->
        <div v-else-if="['填空题', '简答题'].includes(currentQuestion.type)" class="subjective-area">
          <template v-if="currentQuestion.type === '填空题'">
            <div v-for="(_blank, idx) in currentQuestion.answer.length" :key="idx" class="fill-row">
              <span class="fill-label">第 {{ idx + 1 }} 空</span>
              <el-input
                v-model="selectedAnswerArray[idx]"
                placeholder="请输入该空答案"
                :disabled="showAnswer || reciteMode"
                class="fill-input"
              />
            </div>
          </template>
          <template v-else>
            <el-input
              v-model="selectedAnswerString"
              type="textarea"
              :rows="5"
              placeholder="请输入您的解答思路或答案要点"
              :disabled="showAnswer || reciteMode"
            />
          </template>
        </div>

        <!-- 底部切题与提交栏 -->
        <div class="question-bottombar">
          <div class="bottom-left">
            <button
              type="button"
              class="btn-secondary"
              :disabled="!canGoPrev"
              @click="prevQuestion"
            >
              <el-icon><ArrowLeft /></el-icon>
              <span>上一题</span>
            </button>
            <button
              type="button"
              class="btn-ghost mobile-sheet-btn"
              @click="handsidebarDialogVisible = true"
            >
              <el-icon><Grid /></el-icon>
              <span>答题卡</span>
            </button>
          </div>

          <div class="bottom-right">
            <button
              v-if="['多选题', '填空题', '简答题'].includes(currentQuestion.type)"
              type="button"
              class="btn-primary"
              :disabled="(!selectedAnswer || (Array.isArray(selectedAnswer) && selectedAnswer.length === 0)) || showAnswer || reciteMode"
              @click="submitAnswer"
            >
              提交本题
            </button>

            <button
              type="button"
              class="btn-secondary"
              :disabled="!(showAnswer || reciteMode)"
              @click="nextQuestion"
            >
              <span>{{ currentPosition === bankInfo.questions.length - 1 ? "完成练习" : "下一题" }}</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>

        <!-- 答案与解析卡片 -->
        <transition name="el-fade-in-linear">
          <div v-if="showAnswer || reciteMode" class="analysis-panel">
            <div class="analysis-head">
              答案与解析
            </div>
            <div class="analysis-row">
              <span class="analysis-key">标准答案</span>
              <div class="analysis-val">
                <AnswerViewer :answer="currentQuestion.answer" :type="currentQuestion.type" />
              </div>
            </div>
            <div class="analysis-row">
              <span class="analysis-key">题目解析</span>
              <p class="analysis-text">
                {{ currentQuestion.analysis || "暂无详细解析" }}
              </p>
            </div>
          </div>
        </transition>
      </div>

      <!-- 右侧吸顶答题卡 -->
      <aside class="sheet-sidebar">
        <div class="sheet-header">
          <span class="sheet-title">答题卡</span>
          <span class="sheet-progress-text">{{ progressText }}</span>
        </div>

        <el-progress
          :percentage="progressPercentage"
          :format="progressFormat"
          :stroke-width="6"
          color="#2563eb"
        />

        <!-- 实时对错统计 -->
        <div class="sheet-stats">
          <div class="sheet-stat-item">
            <span class="stat-num is-correct">{{ correctCount }}</span>
            <span class="stat-name">答对</span>
          </div>
          <div class="sheet-stat-item">
            <span class="stat-num is-wrong">{{ wrongCount }}</span>
            <span class="stat-name">答错</span>
          </div>
          <div class="sheet-stat-item">
            <span class="stat-num">{{ unansweredCount }}</span>
            <span class="stat-name">未答</span>
          </div>
        </div>

        <!-- 状态图例 -->
        <div class="sheet-legend">
          <span class="legend-item"><i class="dot dot-current" />当前</span>
          <span class="legend-item"><i class="dot dot-correct" />正确</span>
          <span class="legend-item"><i class="dot dot-wrong" />错误</span>
          <span class="legend-item"><i class="dot dot-idle" />未答</span>
        </div>

        <!-- 按题型分组的题号网格 -->
        <div class="sheet-groups">
          <div v-for="(indexes, type) in questionTypeGroups" :key="type" class="sheet-group">
            <div class="sheet-group-title">
              {{ type }}（{{ indexes.length }}）
            </div>
            <div class="sheet-grid">
              <button
                v-for="index in indexes"
                :key="index"
                type="button"
                class="sheet-num"
                :class="{
                  'is-current': currentIndex === index,
                  'is-answered': questionStatus[String(bankInfo.questions[index].id)] === 'answered',
                  'is-wrong': questionStatus[String(bankInfo.questions[index].id)] === 'wrong',
                }"
                @click="goToQuestion(index)"
              >
                {{ groupedQuestionOrder.indexOf(index) + 1 }}
              </button>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <!-- 移动端答题卡弹窗 -->
    <el-dialog
      v-model="handsidebarDialogVisible"
      title="答题卡"
      width="92vw"
      :append-to-body="true"
      class="practice-dialog"
    >
      <div class="mobile-sheet">
        <div class="sheet-stats">
          <div class="sheet-stat-item">
            <span class="stat-num is-correct">{{ correctCount }}</span>
            <span class="stat-name">答对</span>
          </div>
          <div class="sheet-stat-item">
            <span class="stat-num is-wrong">{{ wrongCount }}</span>
            <span class="stat-name">答错</span>
          </div>
          <div class="sheet-stat-item">
            <span class="stat-num">{{ unansweredCount }}</span>
            <span class="stat-name">未答</span>
          </div>
        </div>
        <div class="sheet-groups" style="max-height: 52vh;">
          <div v-for="(indexes, type) in questionTypeGroups" :key="type" class="sheet-group">
            <div class="sheet-group-title">
              {{ type }}（{{ indexes.length }}）
            </div>
            <div class="sheet-grid">
              <button
                v-for="index in indexes"
                :key="index"
                type="button"
                class="sheet-num"
                :class="{
                  'is-current': currentIndex === index,
                  'is-answered': questionStatus[String(bankInfo.questions[index].id)] === 'answered',
                  'is-wrong': questionStatus[String(bankInfo.questions[index].id)] === 'wrong',
                }"
                @click="goToQuestion(index); handsidebarDialogVisible = false"
              >
                {{ groupedQuestionOrder.indexOf(index) + 1 }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <button type="button" class="btn-primary" @click="handsidebarDialogVisible = false">
          关闭
        </button>
      </template>
    </el-dialog>

    <!-- 确认交卷弹窗 -->
    <el-dialog v-model="handInDialogVisible" title="确认交卷" width="420px" :append-to-body="true">
      <p class="dialog-text">
        您已完成全部 {{ bankInfo.questions.length }} 道题目（答对 {{ correctCount }} 题，答错 {{ wrongCount }} 题），确定要提交本次练习记录吗？
      </p>
      <template #footer>
        <div class="dialog-actions">
          <button type="button" class="btn-secondary" @click="handInDialogVisible = false">
            再检查一下
          </button>
          <button type="button" class="btn-success" @click="confirmHandIn">
            确定交卷
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 退出练习弹窗 -->
    <el-dialog v-model="exitDialogVisible" title="退出练习" width="420px" :append-to-body="true">
      <p class="dialog-text">
        当前已完成 {{ answeredCount }} / {{ bankInfo.questions.length }} 题，是否保留本次练习进度以便下次继续？
      </p>
      <template #footer>
        <div class="dialog-actions">
          <button type="button" class="btn-ghost" @click="cancelExitPractice">
            取消
          </button>
          <button type="button" class="btn-danger-outline" @click="confirmExitPractice">
            放弃进度
          </button>
          <button type="button" class="btn-primary" @click="saveAndExitPractice">
            保存并退出
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ======== 页面整体（融入 Layout 的 #f8fafc 背景，去除多余装饰光球） ======== */
.practice-page {
  color: #0f172a;
}

/* 通用按钮体系 */
.btn-primary,
.btn-secondary,
.btn-success,
.btn-ghost,
.btn-danger-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  line-height: 1.2;
  transition:
    background-color 0.16s ease,
    border-color 0.16s ease,
    color 0.16s ease;
}

.btn-primary {
  background-color: #2563eb;
  color: #ffffff;
  border: 1px solid #2563eb;
}

.btn-primary:hover:not(:disabled) {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-success {
  background-color: #059669;
  color: #ffffff;
  border: 1px solid #059669;
}

.btn-success:hover:not(:disabled) {
  background-color: #047857;
  border-color: #047857;
}

.btn-secondary {
  background-color: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #f8fafc;
  border-color: #94a3b8;
  color: #0f172a;
}

.btn-ghost {
  background-color: transparent;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-ghost:hover:not(:disabled) {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.btn-danger-outline {
  background-color: #ffffff;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-danger-outline:hover:not(:disabled) {
  background-color: #fef2f2;
  border-color: #fca5a5;
}

button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

/* ======== 阶段一：练习设置卡片 ======== */
.setup-shell {
  max-width: 820px;
  margin: 0 auto;
}

.setup-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 32px 36px;
}

.setup-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 24px;
}

.setup-eyebrow {
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  margin-bottom: 6px;
}

.setup-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.setup-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background-color: #f1f5f9;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.diff-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
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

.setup-body {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.setup-block h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.block-head {
  margin-bottom: 12px;
}

.block-head-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 14px;
}

.block-desc {
  font-size: 13px;
  color: #64748b;
}

.count-highlight {
  color: #2563eb;
  font-variant-numeric: tabular-nums;
}

.quick-select-actions {
  display: flex;
  gap: 12px;
}

.link-btn {
  background: transparent;
  border: none;
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.mode-selector {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mode-option {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  cursor: pointer;
  text-align: left;
  transition: all 0.16s ease;
}

.mode-option:hover {
  border-color: #cbd5e1;
}

.mode-option.is-active {
  background-color: #eff6ff;
  border-color: #2563eb;
}

.mode-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.mode-option.is-active .mode-title {
  color: #1d4ed8;
}

.mode-desc {
  font-size: 12px;
  color: #64748b;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.type-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

.type-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.type-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.type-max {
  font-size: 12px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.setup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.setup-loading {
  padding: 12px 0;
}

/* ======== 阶段二：刷题工作台（左题目 + 右吸顶答题卡） ======== */
.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  align-items: start;
}

.question-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 28px 32px;
}

.question-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.type-badge {
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  color: #1d4ed8;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.index-indicator {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.index-indicator strong {
  color: #0f172a;
  font-size: 15px;
}

.topbar-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
}

.switch-controls {
  display: flex;
  align-items: center;
  gap: 14px;
}

.switch-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
}

.topbar-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 题干排版 */
.question-stem {
  font-size: 17px;
  line-height: 1.75;
  color: #0f172a;
  font-weight: 500;
  margin-bottom: 24px;
  word-break: break-word;
}

.stem-num {
  font-weight: 700;
  color: #2563eb;
  margin-right: 8px;
  font-variant-numeric: tabular-nums;
}

/* 选项列表 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
}

.option-card {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 13px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #334155;
  font-size: 15px;
  text-align: left;
  cursor: pointer;
  transition: all 0.16s ease;
}

.option-card:hover:not(.is-disabled) {
  border-color: #cbd5e1;
  background-color: #f8fafc;
}

.option-card.is-selected {
  border-color: #2563eb;
  background-color: #eff6ff;
  color: #1e40af;
}

.option-card.is-correct {
  border-color: #86efac;
  background-color: #f0fdf4;
  color: #166534;
}

.option-card.is-wrong {
  border-color: #fca5a5;
  background-color: #fef2f2;
  color: #991b1b;
}

.option-card.is-disabled {
  cursor: default;
}

.opt-key {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.option-card.is-selected .opt-key {
  background-color: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.option-card.is-correct .opt-key {
  background-color: #16a34a;
  border-color: #16a34a;
  color: #ffffff;
}

.option-card.is-wrong .opt-key {
  background-color: #dc2626;
  border-color: #dc2626;
  color: #ffffff;
}

.opt-content {
  flex: 1;
  line-height: 1.6;
}

.opt-status-icon {
  font-size: 16px;
  margin-top: 4px;
  flex-shrink: 0;
}

/* 主观题区 */
.subjective-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 28px;
}

.fill-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fill-label {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  min-width: 64px;
}

.fill-input {
  max-width: 360px;
}

/* 底部切题栏 */
.question-bottombar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.bottom-left,
.bottom-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mobile-sheet-btn {
  display: none;
}

/* 答案解析卡片 */
.analysis-panel {
  margin-top: 24px;
  padding: 20px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.analysis-head {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.analysis-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.analysis-key {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  min-width: 60px;
  flex-shrink: 0;
  padding-top: 2px;
}

.analysis-val {
  flex: 1;
}

.analysis-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: #334155;
  white-space: pre-wrap;
}

/* ======== 右侧吸顶答题卡 ======== */
.sheet-sidebar {
  position: sticky;
  top: 88px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 20px;
  max-height: calc(100vh - 112px);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sheet-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.sheet-progress-text {
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  font-variant-numeric: tabular-nums;
}

.sheet-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.sheet-stat-item {
  background-color: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 8px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.stat-num.is-correct {
  color: #059669;
}

.stat-num.is-wrong {
  color: #dc2626;
}

.stat-name {
  font-size: 11px;
  color: #64748b;
}

.sheet-legend {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 11px;
  color: #64748b;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  display: inline-block;
}

.dot-current {
  background-color: #2563eb;
}

.dot-correct {
  background-color: #10b981;
}

.dot-wrong {
  background-color: #ef4444;
}

.dot-idle {
  background-color: #e2e8f0;
}

.sheet-groups {
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-right: 4px;
}

.sheet-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.sheet-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.sheet-num {
  aspect-ratio: 1;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.sheet-num:hover {
  border-color: #93c5fd;
  color: #1d4ed8;
}

.sheet-num.is-answered {
  background-color: #ecfdf5;
  border-color: #86efac;
  color: #047857;
}

.sheet-num.is-wrong {
  background-color: #fef2f2;
  border-color: #fca5a5;
  color: #b91c1c;
}

.sheet-num.is-current {
  background-color: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

/* 弹窗样式 */
.dialog-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: #475569;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.mobile-sheet {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 960px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .sheet-sidebar {
    display: none;
  }

  .mobile-sheet-btn {
    display: inline-flex;
  }
}

@media screen and (max-width: 680px) {
  .setup-card,
  .question-card {
    padding: 20px 16px;
  }

  .mode-selector,
  .type-grid {
    grid-template-columns: 1fr;
  }

  .question-bottombar {
    flex-wrap: wrap;
  }
}
</style>
