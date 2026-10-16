<script setup lang="ts">
import type { ActivityAnswerDTO, ActivityRecordDTO } from "../apis/type"
import { Document, Warning, Clock } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox, ElNotification } from "element-plus"
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useUserStore } from "@/pinia/stores/user"
import { createActivityRecord, deleteActivityAnswersByRecordId, finishActivityRecord, saveOrUpdateBatch, selectActivityRecords, updateActivityRecord } from "../apis/activityRecords"
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
  score: number
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

// 考试设置
const examSettings = ref({
  duration: 60, // 考试时长（分钟）
  questionCount: 0, // 题目总数
  totalScore: 0, // 总分
  autoNextCorrect: true // 自动下一题：true为自动，false为手动
})

// 考试状态
const examStarted = ref(false)
const examFinished = ref(false)
const timeRemaining = ref(0) // 剩余时间（秒）
const timer = ref<any>(null)

// 当前题目的索引
const currentIndex = ref(0)

// 用户选择的答案
const selectedAnswer = ref<string | string[]>("")

// 控制是否显示答案（考试结束后才显示）
const showAnswer = ref(false)

// 记录每个题目的答题状态
const questionStatus = ref<{ [key: string]: string }>({})

// 记录用户的答题历史
const answerHistory = ref<{ [key: string]: string | string[] }>({})

// 记录每个题目的得分
const questionScores = ref<{ [key: string]: number }>({})

// 当前活动ID
const currentActivityId = ref<string>("")

// 自动保存答题记录
let autoSaveInterval: any = null

// 计算属性，返回当前题目的详细信息
const currentQuestion = computed(() => {
  if (!bankInfo.value.questions.length) {
    return {
      type: "",
      content: "",
      options: [],
      answer: [],
      analysis: "",
      score: 0
    } as Question
  }
  return bankInfo.value.questions[currentIndex.value]
})

// 考试进度
const progressText = computed(() => {
  const answered = Object.keys(questionStatus.value).length
  return `${answered}/${bankInfo.value.questions.length}`
})

// 进度百分比
const progressPercentage = computed(() => {
  const totalAnswered = Object.keys(questionStatus.value).length
  return (totalAnswered / examSettings.value.questionCount) * 100
})

// 剩余时间格式化
const timeFormatted = computed(() => {
  const minutes = Math.floor(timeRemaining.value / 60)
  const seconds = timeRemaining.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 判断是否所有题目都已作答
const allAnswered = computed(() =>
  Object.keys(questionStatus.value).length === examSettings.value.questionCount
)

// 总得分
const totalScore = computed(() => {
  return Object.values(questionScores.value).reduce((sum, score) => sum + score, 0)
})

// 弹窗控制
const handInDialogVisible = ref(false)
const exitDialogVisible = ref(false)
const finishDialogVisible = ref(false)

// 页面加载时初始化
onMounted(() => {
  if (bankInfo.value.questions.length === 0) {
    getBankInfo(true)
  }

  // 添加页面可见性变化监听
  document.addEventListener("visibilitychange", handleVisibilityChange)
})

// 页面卸载时清理
onBeforeUnmount(() => {
  stopTimer()
  stopAutoSave()
  document.removeEventListener("visibilitychange", handleVisibilityChange)
})

// 页面可见性变化处理
function handleVisibilityChange() {
  if (document.visibilityState === "hidden" && examStarted.value && currentActivityId.value) {
    console.log("页面隐藏，保存答题记录到服务器")
    saveAnswersToServer()
  }
}

// 获取题库信息
async function getBankInfo(shouldQueryHistory = true) {
  const bankId = String(route.query.bank)
  if (!bankId) {
    ElMessage.error("题库不存在")
    setTimeout(() => {
      router.push("/question-bank")
    }, 1000)
    return
  }

  try {
    const questions = await getQuestionsByBankId(bankId)
    bankInfo.value = {
      id: bankId,
      name: "", // 可通过其他接口补充题库名
      difficulty: "", // 可通过其他接口补充难度
      questionCount: questions.length,
      questions: questions.map(mapToQuestion).map(q => ({
        ...q,
        score: 1 // 默认每题1分
      }))
    }

    // 设置考试题目数量
    examSettings.value.questionCount = questions.length
    examSettings.value.totalScore = questions.length // 默认每题1分

    // 查询是否有历史考试记录
    if (shouldQueryHistory) {
      try {
        const activityRecord = await selectActivityRecords(bankId, "exam")
        console.log("查询到的历史考试记录:", activityRecord)
        if (activityRecord && activityRecord.id) {
          // 有历史记录，恢复状态
          currentActivityId.value = activityRecord.id
          examStarted.value = true

          // 恢复答题记录
          if (activityRecord.activityAnswersList && activityRecord.activityAnswersList.length > 0) {
            activityRecord.activityAnswersList.forEach((activityAnswer: any) => {
              const index = bankInfo.value.questions.findIndex((q: any) => String(q.id) === activityAnswer.questionId)
              if (index !== -1) {
                answerHistory.value[activityAnswer.questionId] = activityAnswer.userAnswer
                questionStatus.value[activityAnswer.questionId] = "answered"
              }
            })
            currentIndex.value = activityRecord.currentQuestionIndex || 0
            resetSelectedAnswer()
          }

          // 恢复考试时间
          if (activityRecord.startTime) {
            const startTime = new Date(activityRecord.startTime)
            const now = new Date()
            const elapsedSeconds = Math.floor((now.getTime() - startTime.getTime()) / 1000)
            const remainingSeconds = Math.max(0, examSettings.value.duration * 60 - elapsedSeconds)

            if (remainingSeconds > 0) {
              timeRemaining.value = remainingSeconds
              startTimer()
            } else {
              // 考试时间已到，自动交卷
              autoSubmitExam()
            }
          }

          // 启动自动保存
          startAutoSave()
        }
      } catch (e) {
        console.error("获取考试记录失败", e)
      }
    }
  } catch {
    ElMessage.error("题库加载失败")
    setTimeout(() => {
      router.push("/question-bank")
    }, 1000)
  }
}

// 开始考试
async function startExam() {
  // 创建考试记录
  const record: ActivityRecordDTO = {
    userId,
    bankId: bankInfo.value.id!,
    type: "exam",
    startTime: new Date().toISOString().slice(0, 19).replace("T", " "),
    endTime: "",
    status: "in_progress",
    questionCount: examSettings.value.questionCount,
    currentQuestionIndex: 0,
    questionIds: bankInfo.value.questions.map(q => q.id).filter((id): id is string => id !== undefined),
    questionTypes: bankInfo.value.questions.map(q => q.type)
  }

  currentActivityId.value = await createActivityRecord(record)
  examStarted.value = true
  timeRemaining.value = examSettings.value.duration * 60

  // 启动计时器
  startTimer()

  // 启动自动保存
  startAutoSave()

  ElNotification({
    title: "考试开始",
    message: `考试时长 ${examSettings.value.duration} 分钟，请认真答题`,
    type: "success"
  })
}

// 启动计时器
function startTimer() {
  stopTimer()
  timer.value = setInterval(() => {
    timeRemaining.value--

    if (timeRemaining.value <= 0) {
      stopTimer()
      autoSubmitExam()
    }
  }, 1000)
}

// 停止计时器
function stopTimer() {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

// 自动交卷（时间到）
function autoSubmitExam() {
  ElNotification({
    title: "考试时间到",
    message: "考试时间已结束，系统将自动交卷",
    type: "warning"
  })
  submitExam()
}

// 启动自动保存
function startAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
  }

  autoSaveInterval = window.setInterval(async () => {
    if (examStarted.value && currentActivityId.value) {
      console.log("定时保存答题记录到服务器")
      try {
        await saveAnswersToServer()
      } catch (err) {
        console.error("自动保存答题记录失败:", err)
      }
    }
  }, 60000) // 每60秒保存一次
}

// 停止自动保存
function stopAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
    autoSaveInterval = null
  }
}

// 保存答题记录到服务器
async function saveAnswersToServer() {
  if (!currentActivityId.value || !examStarted.value) return

  try {
    const answers: ActivityAnswerDTO[] = []

    bankInfo.value.questions.forEach((q: any) => {
      // 只保存已作答的题目
      if (questionStatus.value[String(q.id)] === "answered") {
        if (!q.id) {
          console.error("题目ID不存在:", q)
          return
        }

        const userAnswer = answerHistory.value[String(q.id)]
        const isCorrect = userAnswer ? checkAnswer(userAnswer, q.answer, q.type) ? 1 : 0 : 0

        answers.push({
          recordId: currentActivityId.value,
          questionId: String(q.id),
          userAnswer: Array.isArray(userAnswer)
            ? userAnswer as string[]
            : [userAnswer as string],
          isCorrect: isCorrect, // 根据答题正确性设置：1为正确，0为错误
          answerTime: new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000)
            .toISOString()
            .slice(0, 19)
            .replace("T", " ")
        })
      }
    })

    if (answers.length > 0) {
      await saveOrUpdateBatch(answers)
      console.log("答题记录已保存到服务器")
    }
  } catch (e) {
    console.error("保存答题记录失败", e)
  }
}

// 设置当前题目的已选答案
function resetSelectedAnswer() {
  const currentQuestionId = bankInfo.value.questions[currentIndex.value].id as string

  if (currentQuestion.value.type === "填空题") {
    selectedAnswer.value = Array.isArray(answerHistory.value[currentQuestionId])
      ? [...(answerHistory.value[currentQuestionId] as string[])]
      : []
  } else if (currentQuestion.value.type === "多选题") {
    selectedAnswer.value = Array.isArray(answerHistory.value[currentQuestionId])
      ? [...(answerHistory.value[currentQuestionId] as string[])]
      : []
  } else {
    // 单选题和判断题：直接获取保存的答案
    const savedAnswer = answerHistory.value[currentQuestionId]
    if (savedAnswer) {
      // 判断题保存的答案可能是数组形式，需要处理
      if (Array.isArray(savedAnswer)) {
        selectedAnswer.value = savedAnswer[0] || ""
      } else {
        selectedAnswer.value = savedAnswer
      }
    } else {
      selectedAnswer.value = ""
    }
  }
}

// 判断某个选项是否被选中
function isSelected(key: string) {
  if (currentQuestion.value.type === "多选题") {
    return Array.isArray(selectedAnswer.value) && selectedAnswer.value.includes(key)
  }
  return selectedAnswer.value === key
}

// 处理选项点击事件
function selectOption(key: string) {
  if (examFinished.value) return // 考试结束后不可再选

  if (currentQuestion.value.type === "多选题") {
    if (!Array.isArray(selectedAnswer.value)) selectedAnswer.value = []
    const arr = selectedAnswer.value as string[]
    if (arr.includes(key)) {
      selectedAnswer.value = arr.filter(k => k !== key)
    } else {
      selectedAnswer.value = [...arr, key]
    }
  } else {
    // 单选题/判断题：直接赋值
    selectedAnswer.value = key
    // 单选题/判断题选中后自动保存答案
    if (["单选题", "判断题"].includes(currentQuestion.value.type)) {
      saveAnswer()
    }
  }
}

// 保存当前题目答案
function saveAnswer() {
  const currentQuestionId = bankInfo.value.questions[currentIndex.value].id as string
  answerHistory.value[currentQuestionId] = selectedAnswer.value
  questionStatus.value[currentQuestionId] = "answered"

  ElMessage.success("答案已保存")

  // 自动下一题
  if (examSettings.value.autoNextCorrect) {
    // 延迟800毫秒后执行
    setTimeout(() => {
      nextQuestion()
    }, 800)
  }
}

// 下一题
function nextQuestion() {
  if (currentIndex.value < bankInfo.value.questions.length - 1) {
    currentIndex.value++
    resetSelectedAnswer()
  }
}

// 上一题
function prevQuestion() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetSelectedAnswer()
  }
}

// 跳转到指定题目
function jumpToQuestion(index: number) {
  currentIndex.value = index
  resetSelectedAnswer()
}

// 判题逻辑（考试结束后使用）
function checkAnswer(userAnswer: string | string[], correctAnswers: string[][], questionType: string) {
  if (Array.isArray(userAnswer) && questionType === "多选题") {
    const userAnswerSet = new Set(userAnswer)
    const correctAnswerSet = new Set(correctAnswers.flat())
    return userAnswerSet.size === correctAnswerSet.size && [...userAnswerSet].every(answer => correctAnswerSet.has(answer))
  } else if (questionType === "填空题") {
    const userAnswers = Array.isArray(userAnswer) ? userAnswer : [userAnswer]
    if (userAnswers.length !== correctAnswers.length) return false
    return correctAnswers.every((correctAnswerGroup, index) =>
      correctAnswerGroup.map(ans => ans.replace(/\s/g, "")).includes(userAnswers[index].replace(/\s/g, ""))
    )
  } else if (questionType === "简答题") {
    return true
  } else if (questionType === "判断题") {
    if (typeof userAnswer === "string") {
      // 判断题的正确答案可能是 "对" 或 "错"，也可能是 "true" 或 "false"
      const correctAnswer = correctAnswers[0][0].toLowerCase()
      const userAnswerNormalized = userAnswer.toLowerCase()

      // 处理不同的表示方式
      if (correctAnswer === "对" || correctAnswer === "true" || correctAnswer === "正确") {
        return userAnswerNormalized === "true" || userAnswerNormalized === "对" || userAnswerNormalized === "正确"
      } else if (correctAnswer === "错" || correctAnswer === "false" || correctAnswer === "错误") {
        return userAnswerNormalized === "false" || userAnswerNormalized === "错" || userAnswerNormalized === "错误"
      }

      return correctAnswer === userAnswerNormalized
    }
    return false
  } else {
    // 单选题
    if (typeof userAnswer === "string") {
      return correctAnswers[0].includes(userAnswer)
    }
    return false
  }
}

// 交卷
async function submitExam() {
  stopTimer()
  stopAutoSave()

  // 先保存所有答题记录到服务器
  if (currentActivityId.value) {
    try {
      const answers: ActivityAnswerDTO[] = []

      bankInfo.value.questions.forEach((q: any) => {
        // 保存所有题目，包括未作答的题目（用户答案为空）
        if (!q.id) {
          console.error("题目ID不存在:", q)
          return
        }

        const userAnswer = answerHistory.value[String(q.id)]
        const isCorrect = userAnswer ? checkAnswer(userAnswer, q.answer, q.type) ? 1 : 0 : 0

        answers.push({
          recordId: currentActivityId.value,
          questionId: String(q.id),
          userAnswer: Array.isArray(userAnswer)
            ? userAnswer as string[]
            : [userAnswer as string],
          isCorrect: isCorrect, // 根据答题正确性设置：1为正确，0为错误
          answerTime: new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000)
            .toISOString()
            .slice(0, 19)
            .replace("T", " ")
        })
      })

      if (answers.length > 0) {
        await saveOrUpdateBatch(answers)
        console.log("交卷前答题记录已保存到服务器")
      }
    } catch (e) {
      console.error("保存答题记录失败", e)
    }
  }

  // 计算得分
  bankInfo.value.questions.forEach((q: any) => {
    const userAnswer = answerHistory.value[String(q.id)]
    if (userAnswer) {
      const isCorrect = checkAnswer(userAnswer, q.answer, q.type)
      questionScores.value[String(q.id)] = isCorrect ? q.score : 0
    } else {
      questionScores.value[String(q.id)] = 0
    }
  })

  // 更新活动记录状态
  if (currentActivityId.value) {
    await finishActivityRecord({
      id: currentActivityId.value,
      userId,
      bankId: bankInfo.value.id!,
      type: "exam",
      startTime: "",
      endTime: new Date().toISOString().slice(0, 19).replace("T", " "),
      status: "completed",
      questionCount: examSettings.value.questionCount,
      currentQuestionIndex: currentIndex.value
    })
  }

  examFinished.value = true
  showAnswer.value = true

  ElNotification({
    title: "考试结束",
    message: `您的得分：${totalScore.value}/${examSettings.value.totalScore}`,
    type: "success"
  })
}

// 退出考试
function exitExam() {
  ElMessageBox.confirm(
    "确定要退出考试吗？退出后考试进度将丢失",
    "退出考试",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(async () => {
    // 删除考试记录
    if (currentActivityId.value) {
      await deleteActivityAnswersByRecordId(currentActivityId.value)
    }
  if (currentActivityId.value) {
    await finishActivityRecord({
      id: currentActivityId.value,
      userId,
      bankId: bankInfo.value.id!,
      type: "exam",
      startTime: "",
      endTime: new Date().toISOString().slice(0, 19).replace("T", " "),
      status: "abando",
      questionCount: examSettings.value.questionCount,
      currentQuestionIndex: currentIndex.value
    })
  }
    stopTimer()
    stopAutoSave()
    examStarted.value = false
    examFinished.value = false

    ElMessage.success("已退出考试")
  }).catch(() => {
    // 用户取消
  })
}

// 返回题库
function goBack() {
  router.push("/question-bank")
}

// 格式化时间显示
function progressFormat(percentage: number) {
  return `${percentage.toFixed(0)}%`
}

const selectedAnswerString = computed({
  get: () => typeof selectedAnswer.value === "string" ? selectedAnswer.value : "",
  set: (val: string) => selectedAnswer.value = val
})

const selectedAnswerArray = computed({
  get: () => Array.isArray(selectedAnswer.value) ? selectedAnswer.value : [],
  set: (val: string[]) => selectedAnswer.value = val
})
</script>

<template>
  <el-main>
    <div class="content-container">
      <!-- 考试设置界面 -->
      <div v-if="!examStarted && !examFinished" class="exam-settings">
        <div class="bank-info">
          <div class="bank-title">
            {{ bankInfo.name }}
          </div>
          <div class="bank-meta">
            <div class="meta-item">
              <el-icon><Document /></el-icon>
              <span>题目总数：{{ bankInfo.questionCount }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Warning /></el-icon>
              <span>难度：{{ bankInfo.difficulty }}</span>
            </div>
          </div>
        </div>

        <el-form :model="examSettings" class="exam-form" v-if="bankInfo.questionCount > 0">
          <el-form-item label="考试时长">
            <el-input-number
              v-model="examSettings.duration"
              :min="10"
              :max="180"
              :step="5"
              style="width: 200px;"
            />
            <span style="margin-left: 8px;">分钟</span>
          </el-form-item>

          <el-form-item label="自动下一题">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-switch
                v-model="examSettings.autoNextCorrect"
                inline-prompt
                active-text="手动下一题"
                inactive-text="自动下一题"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="startExam" style="margin-right: 10px;">
              开始考试
            </el-button>
            <el-button @click="goBack">
              返回题库
            </el-button>
          </el-form-item>
        </el-form>

        <div v-else class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
      </div>

      <!-- 考试进行中界面 -->
      <div v-else-if="examStarted && !examFinished" class="exam-interface">
        <!-- 考试头部信息 -->
        <div class="exam-header">
          <div class="exam-info">
            <div class="time-remaining">
              <el-icon><Clock /></el-icon>
              <span>剩余时间：{{ timeFormatted }}</span>
            </div>
            <div class="progress-info">
              <span>进度：{{ progressText }}</span>
            </div>
          </div>
          <div class="exam-actions">
            <el-button type="warning" @click="handInDialogVisible = true">
              交卷
            </el-button>
            <el-button type="info" @click="exitDialogVisible = true">
              退出考试
            </el-button>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-bar">
          <el-progress
            :percentage="progressPercentage"
            :format="progressFormat"
            :stroke-width="8"
          />
        </div>

        <div class="exam-content">
          <!-- 题目区域 -->
          <div class="question-area">
            <div class="question-header">
              <div class="question-type">
                {{ currentQuestion.type }}
              </div>
            </div>

            <div class="question-content" style="white-space: pre-wrap;">
              {{ currentQuestion.content }}
            </div>

            <!-- 选项区域 -->
            <div class="options-area">
              <!-- 单选题/多选题 -->
              <div v-if="['单选题', '多选题'].includes(currentQuestion.type)" class="options-list">
                <div
                  v-for="option in currentQuestion.options"
                  :key="option.key"
                  class="option-item"
                  :class="{ selected: isSelected(option.key) }"
                  @click="selectOption(option.key)"
                >
                  <span class="option-key">{{ option.key }}.</span>
                  <span class="option-content">{{ option.content }}</span>
                </div>
              </div>

              <!-- 判断题 -->
              <div v-else-if="currentQuestion.type === '判断题'" class="options-list">
                <div
                  v-for="option in [{ key: 'true', content: '对' }, { key: 'false', content: '错' }]"
                  :key="option.key"
                  class="option-item"
                  :class="{ selected: isSelected(option.key) }"
                  @click="selectOption(option.key)"
                >
                  <span class="option-content">{{ option.content }}</span>
                </div>
              </div>

              <!-- 填空题 -->
              <div v-else-if="currentQuestion.type === '填空题'" class="fill-blanks">
                <div v-for="(_, index) in currentQuestion.answer" :key="index" class="fill-item">
                  <el-input
                    v-model="selectedAnswerArray[index]"
                    :placeholder="`请输入第${index + 1}空答案`"
                    style="margin-bottom: 10px;"
                  />
                </div>
              </div>

              <!-- 简答题 -->
              <div v-else-if="currentQuestion.type === '简答题'" class="short-answer">
                <el-input
                  v-model="selectedAnswerString"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入您的答案"
                />
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="question-actions">
              <el-button @click="prevQuestion" :disabled="currentIndex === 0">
                上一题
              </el-button>
              <el-button type="primary" @click="saveAnswer">
                保存答案
              </el-button>
              <el-button @click="nextQuestion" :disabled="currentIndex === bankInfo.questions.length - 1">
                下一题
              </el-button>
            </div>

            <!-- 自动下一题开关 -->
            <div class="question-switch" style="position: absolute; left: 20px; bottom: 20px; z-index: 2; display: flex; align-items: center; gap: 18px;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-switch
                  v-model="examSettings.autoNextCorrect"
                  inline-prompt
                  active-text="手动下一题"
                  inactive-text="自动下一题"
                />
              </div>
            </div>
          </div>

          <!-- 答题卡 -->
          <div class="sidebar">
            <div class="progress-info">
              <div class="progress-text">
                {{ progressText }}
              </div>
              <el-progress
                :percentage="progressPercentage"
                :format="progressFormat"
              />
            </div>
            <div class="question-grid-grouped">
              <div class="question-type-group">
                <div class="type-title">
                  全部题目
                </div>
                <div class="question-grid">
                  <div
                    v-for="(question, index) in bankInfo.questions"
                    :key="question.id"
                    class="question-number"
                    :class="{
                      answered: questionStatus[String(question.id)] === 'answered',
                      current: index === currentIndex
                    }"
                    @click="jumpToQuestion(index)"
                  >
                    {{ index + 1 }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 考试结束界面 -->
      <div v-else-if="examFinished" class="exam-result">
        <div class="result-header">
          <h2>考试结果</h2>
          <div class="score-info">
            <div class="total-score">
              总分：{{ totalScore }}/{{ examSettings.totalScore }}
            </div>
            <div class="score-percentage">
              正确率：{{ ((totalScore / examSettings.totalScore) * 100).toFixed(1) }}%
            </div>
          </div>
        </div>

        <!-- 题目回顾 -->
        <div class="question-review">
          <div
            v-for="(question, index) in bankInfo.questions"
            :key="question.id"
            class="review-item"
          >
            <div class="review-header">
              <span class="question-index">第{{ index + 1 }}题</span>
              <span class="question-type">{{ question.type }}</span>
              <span class="question-score" :class="{ correct: questionScores[String(question.id)] > 0 }">
                得分：{{ questionScores[String(question.id)] }}/{{ question.score }}
              </span>
            </div>

            <div class="review-content">
              <div class="question-text">{{ question.content }}</div>

              <div class="user-answer">
                <strong>您的答案：</strong>
                <span>{{ answerHistory[String(question.id)] || '未作答' }}</span>
              </div>

              <div v-if="showAnswer" class="correct-answer">
                <strong>正确答案：</strong>
                <span>{{ question.answer.flat().join(', ') }}</span>
              </div>

              <div v-if="showAnswer && question.analysis" class="answer-analysis">
                <strong>解析：</strong>
                <span>{{ question.analysis }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="result-actions">
          <el-button type="primary" @click="goBack">
            返回题库
          </el-button>
        </div>
      </div>
    </div>

    <!-- 交卷确认对话框 -->
    <el-dialog
      v-model="handInDialogVisible"
      title="交卷确认"
      width="400px"
    >
      <div style="text-align: center;">
        <p>确定要交卷吗？交卷后将无法继续修改答案。</p>
        <p>已答题：{{ Object.keys(questionStatus).length }}/{{ examSettings.questionCount }}</p>
      </div>
      <template #footer>
        <el-button @click="handInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handInDialogVisible = false; submitExam()">
          确定交卷
        </el-button>
      </template>
    </el-dialog>

    <!-- 退出考试确认对话框 -->
    <el-dialog
      v-model="exitDialogVisible"
      title="退出考试确认"
      width="400px"
    >
      <div style="text-align: center;">
        <p>确定要退出考试吗？退出后考试进度将丢失。</p>
      </div>
      <template #footer>
        <el-button @click="exitDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="exitDialogVisible = false; exitExam()">
          确定退出
        </el-button>
      </template>
    </el-dialog>
  </el-main>
</template>

<style scoped>
.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.exam-settings, .exam-interface, .exam-result {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.bank-info {
  margin-bottom: 30px;
}

.bank-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.bank-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.time-remaining {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.progress-bar {
  margin-bottom: 30px;
}

.exam-content {
  display: flex;
  gap: 20px;
}

.question-area {
  flex: 1;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  padding-bottom: 90px; /* 给底部按钮留空间 */
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-type {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.question-score {
  color: #67c23a;
  font-weight: bold;
}

.question-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
}

.option-item.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.option-key {
  font-weight: bold;
  margin-right: 8px;
}

.question-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.sidebar {
  width: 400px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  /* 保证sidebar本身高度自适应 */
  display: flex;
  flex-direction: column;
  /* 可选：最大高度，防止极端拉长 */
  max-height: 700px;
}

.progress-info {
  text-align: center;
  margin-bottom: 20px;
}

.progress-text {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.question-grid-grouped {
  /* 固定高度，超出滚动 */
  max-height: 800px;
  overflow-y: auto;
  padding-right: 4px; /* 防止滚动条遮挡 */
}

.question-type-group {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px;
}

.type-title {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.question-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.question-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-number:hover {
  background-color: #f5f7fa;
}

.question-number.current {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

.question-number.answered {
  background-color: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.sheet-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 10px;
}

.question-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-number:hover {
  border-color: #409eff;
}

.question-number.answered {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.question-number.current {
  border-color: #409eff;
  background-color: #409eff;
  color: white;
}

.result-header {
  text-align: center;
  margin-bottom: 30px;
}

.score-info {
  margin-top: 20px;
}

.total-score {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.score-percentage {
  font-size: 18px;
  color: #666;
  margin-top: 10px;
}

.review-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.question-index {
   font-weight: bold;
}

.question-score.correct {
  color: #67c23a;
}

.question-score:not(.correct) {
  color: #f56c6c;
}

.review-content {
  line-height: 1.6;
}

.question-text {
  margin-bottom: 15px;
  font-size: 16px;
}

.user-answer, .correct-answer, .answer-analysis {
  margin-bottom: 10px;
}

.result-actions {
  text-align: center;
  margin-top: 30px;
}

.loading-state {
  text-align: center;
  padding: 40px 0;
}

.exam-form {
  max-width: 400px;
  margin: 0 auto;
}

.exam-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.progress-info {
  font-size: 16px;
  color: #666;
}

.exam-actions {
  display: flex;
  gap: 10px;
}

.fill-blanks {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.short-answer {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .content-container {
    padding: 10px;
  }

  .exam-settings, .exam-interface, .exam-result {
    padding: 20px;
  }

  .exam-header {
    flex-direction: column;
    gap: 15px;
  }

  .exam-info {
    flex-direction: column;
    gap: 10px;
  }

  .bank-meta {
    flex-direction: column;
    gap: 10px;
  }

  .review-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .exam-content {
    flex-direction: column;
    gap: 20px;
  }

  .sidebar {
    width: 100%;
    max-height: 300px;
    display: none !important;
  }

  .question-grid {
    grid-template-columns: repeat(auto-fill, minmax(35px, 1fr));
    gap: 8px;
  }

  .question-number {
    width: 35px;
    height: 35px;
  }
}
</style>
