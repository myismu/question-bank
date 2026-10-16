<script setup lang="ts">
import type { ActivityAnswerDTO, ActivityRecordDTO } from "../apis/type"
import { Document, Warning } from "@element-plus/icons-vue"
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
// 定义练习是否开始的状态
const practiceStarted = ref(false)

// 当前题目的索引
const currentIndex = ref(0)

// 用户选择的答案，可以是字符串或字符串数组（用于多选题）
const selectedAnswer = ref<string | string[]>("")

// 控制是否显示答案
const showAnswer = ref(false)

// 记录每个题目的答题状态，使用题目ID作为键
const questionStatus = ref<{ [key: string]: string }>({})

// 记录用户的答题历史，保存每道题的答案
const answerHistory = ref<{ [key: string]: string | string[] }>({})

// 标记练习是否完成
const isCompleted = ref(false)

// 计算属性，返回当前题目的详细信息
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
// 答题卡进度
const progressText = computed(() => {
  const answered = Object.keys(questionStatus.value).length
  return `${answered}/${bankInfo.value.questions.length}`
})
// 进度百分比
const progressPercentage = computed(() => {
  const totalAnswered = Object.keys(questionStatus.value).length
  return (totalAnswered / practiceSettings.value.questionCount) * 100
})
function progressFormat(percentage: number) {
  return `${percentage.toFixed(0)}%`
}
const questionTypeGroups = computed(() => {
  const groups: Record<string, number[]> = {}
  bankInfo.value.questions.forEach((q: Question, idx: number) => {
    if (!groups[q.type]) groups[q.type] = []
    groups[q.type].push(idx)
  })
  return groups
})
const groupedQuestionOrder = computed(() => {
  return bankInfo.value.questions.map((q, idx) => idx) // 使用排序后的索引
})
let _practiceEndTime = ""

// 判断是否所有题目都已作答
const allAnswered = computed(() =>
  Object.keys(questionStatus.value).length === practiceSettings.value.questionCount
  && Object.values(questionStatus.value).every((s: string) => s === "answered" || s === "wrong")
)

const handInDialogVisible = ref(false)

// 答题卡弹窗
const handsidebarDialogVisible = ref(false)

// 退出练习弹窗
const exitDialogVisible = ref(false)
const finishDialogVisible = ref(false)

const hasClearedHistory = ref(false)

// 页面加载时自动恢复本地缓存
onMounted(() => {
  if (bankInfo.value.questions.length === 0) {
    getBankInfo(true)
  }

  // 添加路由离开守卫
  unwatch = router.beforeEach(async (to: any, from: any) => {
    // 如果当前页面是练习页面并且正在进行练习
    if (from.path.includes("practice") && practiceStarted.value && currentActivityId.value) {
      // 保存答题记录到服务器
      console.log("路由离开，保存答题记录到服务器")
      try {
        savedByRouteGuard = true // 标记已经通过路由守卫保存
        await saveAnswersToServer()
        return true
      } catch (err) {
        console.error("路由离开时保存答题记录失败:", err)
        return true
      }
    }
    return true
  })

  // 添加可见性变化监听
  document.addEventListener("visibilitychange", handleVisibilityChange)
  // 如果是练习未开始状态则显示通知


    ElNotification({
      title: "练习模式提示",
      message: "练习模式可对题型、题目数量、题目顺序进行自定义选择，交卷结束后可查看错题本",
      type: "success"
    })

})

// 监听刷新/关闭页面事件自动保存
window.addEventListener("beforeunload", (event) => {
  if (practiceStarted.value && currentActivityId.value) {
    // 添加提示
    const message = "您有未保存的答题记录，确认离开吗？"
    event.preventDefault()
    event.returnValue = message

    // 只保存到本地缓存

    return message
  }
})

// 当前活动ID
const currentActivityId = ref<string>("")
// 自动保存答题记录
let autoSaveInterval: any = null
// 路由守卫移除函数
let unwatch: (() => void) | null = null
// 标记是否通过路由守卫保存过数据
let savedByRouteGuard = false

// 添加页面可见性变化事件处理器
function handleVisibilityChange() {
  // 如果页面隐藏，保存答题记录
  if (document.visibilityState === "hidden" && practiceStarted.value && currentActivityId.value) {
    console.log("页面隐藏，保存答题记录到服务器")
    saveAnswersToServer()
  }
}

const questionTypeOrder = ["单选题", "多选题", "判断题", "填空题", "简答题"]

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
      questions: questions.map(mapToQuestion)
    }

    // 排序题目
    bankInfo.value.questions.sort((a, b) => {
      return questionTypeOrder.indexOf(a.type) - questionTypeOrder.indexOf(b.type)
    })

    // 设置默认练习题目数为题库实际题目数量
    practiceSettings.value.questionCount = questions.length

    // 查询是否有历史活动记录
    if (shouldQueryHistory) {
      try {
        const activityRecord = await selectActivityRecords(bankId, "practice")
        console.log("查询到的历史活动记录:", activityRecord)
        if (activityRecord && activityRecord.id) {
          // 有历史记录，恢复状态
          currentActivityId.value = activityRecord.id
          practiceStarted.value = true

          // 恢复答题记录
          if (activityRecord.activityAnswersList && activityRecord.activityAnswersList.length > 0) {
            bankInfo.value.questions = bankInfo.value.questions.filter(q => activityRecord.questionIds?.includes(q.id as string))

            activityRecord.activityAnswersList.forEach((activityAnswer: any) => {
              const index = bankInfo.value.questions.findIndex((q: any) => String(q.id) === activityAnswer.questionId)
              if (index !== -1) {
                answerHistory.value[activityAnswer.questionId] = activityAnswer.userAnswer
                questionStatus.value[activityAnswer.questionId] = activityAnswer.isCorrect === 1 ? "answered" : "wrong"
              }
            })
            // 设置当前题目索引为第一个未答题的索引，如果全部已答则设为0
            currentIndex.value = activityRecord.currentQuestionIndex || 0
            // 重置当前选中答案
            resetSelectedAnswer()
            // 恢复显示答案
            showAnswer.value = questionStatus.value[String(bankInfo.value.questions[currentIndex.value].id)] === "answered" || questionStatus.value[String(bankInfo.value.questions[currentIndex.value].id)] === "wrong"
          }
          // 使用 questionIds 更新题目列表
          const questionIds = activityRecord.questionIds || []

          const questionMap = new Map(bankInfo.value.questions.map(q => [String(q.id), q]))
          bankInfo.value.questions = questionIds
            .map(id => questionMap.get(String(id)))
            .filter((q): q is typeof bankInfo.value.questions[0] => !!q)

          // 更新答题卡数量
          practiceSettings.value.questionCount = bankInfo.value.questions.length

          // 启动自动保存
          startAutoSave()

        }
      } catch (e) {
        console.error("获取活动记录失败", e)
        // 无历史记录或查询失败，继续显示设置页面
      }
    }

    // 在获取题目后，添加以下代码
    bankInfo.value.questions.sort((a, b) => {
      return questionTypeOrder.indexOf(a.type) - questionTypeOrder.indexOf(b.type)
    })
  } catch {
    ElMessage.error("题库加载失败")
    setTimeout(() => {
      router.push("/question-bank")
    }, 1000)
  }
}

// 修改startAutoSave函数，设置为每1分钟保存一次
function startAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
  }

  // 设置为每1分钟保存一次
  autoSaveInterval = window.setInterval(async () => {
    if (practiceStarted.value && currentActivityId.value) {
      console.log("定时保存答题记录到服务器")
      try {
        await saveAnswersToServer()
      } catch (err) {
        console.error("自动保存答题记录失败:", err)
      }
    }
  }, 60000) // 每60秒保存一次

  console.log("已启动自动保存答题记录")
}

// 停止自动保存
function stopAutoSave() {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
    autoSaveInterval = null
    console.log("已停止自动保存答题记录")
  }
}

// 保存答题记录到服务器
async function saveAnswersToServer() {
  if (!currentActivityId.value || !practiceStarted.value) return

  try {
    const answers: ActivityAnswerDTO[] = []

    bankInfo.value.questions.forEach((q: any) => {
      // 只保存已作答的题目
      if (questionStatus.value[String(q.id)] === "answered" || questionStatus.value[String(q.id)] === "wrong") {
        // 确保题目ID存在
        if (!q.id) {
          console.error("题目ID不存在:", q)
          return
        }

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
      console.log(`准备保存${answers.length}道题的答题记录到服务器`)
      await saveOrUpdateBatch(answers)
      // await updateCurrentQuestionIndex()
      console.log("答题记录已保存到服务器")
    } else {
      console.log("没有需要保存的答题记录")
    }
  } catch (e) {
    console.error("保存答题记录失败", e)
  }
}

const selectedQuestionCounts = reactive<{ [key: string]: number }>({
  single: 0,
  multiple: 0,
  fill: 0,
  judge: 0,
  short: 0
})

// 在选择题目数量时更新选择数量
function updateSelectedQuestionCount(type: keyof typeof selectedQuestionCounts, count: number) {
  selectedQuestionCounts[type] = count
}

// 在选择题目数量时调用 updateSelectedQuestionCount 函数
updateSelectedQuestionCount("single", selectedQuestionCounts.single)
updateSelectedQuestionCount("multiple", selectedQuestionCounts.multiple)
updateSelectedQuestionCount("fill", selectedQuestionCounts.fill)
updateSelectedQuestionCount("judge", selectedQuestionCounts.judge)
updateSelectedQuestionCount("short", selectedQuestionCounts.short)

// 在开始练习时，根据 selectedQuestionCounts 选择题目
async function startPractice() {
  // 计算所有题型的总数量
  const totalQuestionCount = Object.values(selectedQuestionCounts).reduce((total, count) => total + count, 0)
  practiceSettings.value.questionCount = totalQuestionCount
  bankInfo.value.questionCount = totalQuestionCount // 更新题目数量

  // 按顺序或随机选择题目
  let selectedQuestions: Question[] = []
  const pool = [...bankInfo.value.questions]
  if (practiceSettings.value.mode === "random") {
    // 随机打乱
    selectedQuestions = pool.sort(() => Math.random() - 0.5)
  } else {
    // 按顺序
    selectedQuestions = pool
  }

  // 根据题型选择题目
  const selected: Question[] = []
  Object.entries(selectedQuestionCounts).forEach(([type, count]) => {
    if (count > 0) {
      const questionsOfType = selectedQuestions.filter(q => q.type === type)
      console.log(`筛选出 ${questionsOfType.length} 道 ${type} 题，用户需要 ${count} 道`)
      selected.push(...questionsOfType.slice(0, count))
    }
  })

  console.log("最终选择的题目：", selected)

  // 更新 bankInfo 的问题列表
  bankInfo.value.questions = selected

  // 创建活动记录
  const record: ActivityRecordDTO = {
    userId,
    bankId: bankInfo.value.id!,
    type: "practice",
    startTime: "",
    endTime: "",
    status: "in_progress",
    questionCount: selected.length, // 使用更新后的值
    currentQuestionIndex: 0,
    questionIds: selected.map(q => q.id).filter((id): id is string => id !== undefined),
    questionTypes: selected.map(q => q.type)
  }
  console.log("创建活动记录时的题目数量:", record.questionCount)
  currentActivityId.value = await createActivityRecord(record)
  // 开始练习初始化题目状态
  practiceStarted.value = true
  questionStatus.value = {}
  answerHistory.value = {}

  // 启动自动保存
  startAutoSave()
}

// 更新活动记录的current_question_index
async function updateCurrentQuestionIndex() {
  if (currentActivityId.value) {
    await updateActivityRecord({
      id: currentActivityId.value,
      userId, // 添加 userId
      bankId: bankInfo.value.id!, // 添加 bankId
      type: "practice", // 添加 type
      startTime: "", // 添加 startTime
      endTime: "", // 添加 endTime
      status: "in_progress", // 添加 status
      currentQuestionIndex: currentIndex.value // 更新当前题目索引
    })
  }
}

// 设置当前题目的已选答案（根据题型初始化 selectedAnswer）
function resetSelectedAnswer() {
  // 根据当前的题目索引，获取真实的题目ID，方便查询答案
  const currentQuestionId = bankInfo.value.questions[currentIndex.value].id as string

  if (currentQuestion.value.type === "填空题") {
    // 填空题：selectedAnswer 为 string[]，长度与标准答案一致
    selectedAnswer.value = Array.isArray(answerHistory.value[currentQuestionId])
      ? [...(answerHistory.value[currentQuestionId] as string[])]
      : []
    console.log("填空题当前题目ID：", currentQuestionId)
    console.log("填空题查询出的答案：", selectedAnswer.value)
    console.log("填空题查询answerHistory的答案：", answerHistory.value[currentQuestionId])
  } else if (currentQuestion.value.type === "多选题") {
    // 多选题：selectedAnswer 为数组
    selectedAnswer.value = Array.isArray(answerHistory.value[currentQuestionId])
      ? [...(answerHistory.value[currentQuestionId] as string[])]
      : []
    console.log("多选题当前题目ID：", currentQuestionId)
    console.log("多选题查询出的答案：", selectedAnswer.value)
    console.log("多选题查询answerHistory的答案：", answerHistory.value[currentQuestionId])
  } else {
    // 单选题/判断题/简答题：selectedAnswer 为字符串
    selectedAnswer.value = answerHistory.value[currentQuestionId] ? answerHistory.value[currentQuestionId][0] : ""
    console.log("单选题/判断题/简答题当前题目ID：", currentQuestionId)
    console.log("单选题/判断题/简答题查询出的答案：", selectedAnswer.value)
    console.log("单选题/判断题/简答题查询answerHistory的答案：", answerHistory.value[currentQuestionId])
  }
}

// 判断某个选项是否被选中（多选题为数组，其他为字符串）
function isSelected(key: string) {
  if (currentQuestion.value.type === "多选题") {
    return Array.isArray(selectedAnswer.value) && selectedAnswer.value.includes(key)
  }
  return selectedAnswer.value === key
}

// 处理选项点击事件，根据题型切换选中状态，单选/判断题选中后自动提交
function selectOption(key: string) {
  if (showAnswer.value) return // 已显示答案时不可再选
  if (currentQuestion.value.type === "多选题") {
    // 多选题：切换选中状态
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
    // 单选题/判断题选中后自动判分
    if (["单选题", "判断题"].includes(currentQuestion.value.type)) {
      submitAnswer()
    }
  }
}

// 判题逻辑：根据题型判断用户答案是否正确
function checkAnswer(userAnswer: string | string[], correctAnswers: string[][]) {
  console.log("当前题目类型：", currentQuestion.value.type)
  console.log("用户的答案:", userAnswer)
  console.log("正确答案:", correctAnswers)
  if (Array.isArray(userAnswer) && currentQuestion.value.type === "多选题") {
    // 多选题逻辑
    const userAnswerSet = new Set(userAnswer)
    const correctAnswerSet = new Set(correctAnswers.flat()) // 将正确答案展平为一维数组
    return userAnswerSet.size === correctAnswerSet.size && [...userAnswerSet].every(answer => correctAnswerSet.has(answer))
  } else if (currentQuestion.value.type === "填空题") {
    // ✅ 填空题判题逻辑：每空一个正确答案组，用户答案只需出现在对应组中即可

    const userAnswers = Array.isArray(userAnswer) ? userAnswer : [userAnswer]

    if (userAnswers.length !== correctAnswers.length) {
      return false // 空数不一致，直接错误
    }

    // 每个空的答案，是否出现在对应的答案组中
    return correctAnswers.every((correctAnswerGroup, index) =>
  correctAnswerGroup
    .map(ans => ans.replace(/\s/g, ""))
    .includes(userAnswers[index].replace(/\s/g, ""))
);

  } else if (currentQuestion.value.type === "简答题") {
    // 简答题直接返回 true
    return true
  } else {
    // 单选题和判断题的判定逻辑
    if (typeof userAnswer === "string") {
      return correctAnswers[0].includes(userAnswer)
    }
    return false // 如果 userAnswer 不是字符串，返回 false
  }
}

// 提交当前题目的答案并判断对错，更新答题状态和历史
async function submitAnswer() {
  // 校验是否已作答，未作答则直接返回
  if (
    (currentQuestion.value.type === "单选题" && !selectedAnswer.value)
    || (currentQuestion.value.type === "多选题" && (!Array.isArray(selectedAnswer.value) || selectedAnswer.value.length === 0))
    || (currentQuestion.value.type === "填空题" && (!Array.isArray(selectedAnswer.value) || (selectedAnswer.value as string[]).some(ans => !ans || ans.trim() === "")))
  ) {
    return
  }

  // 保存本题作答历史
  const currentQuestionId = bankInfo.value.questions[currentIndex.value].id as string
  answerHistory.value[currentQuestionId] = selectedAnswer.value
  // 判题逻辑
  const isCorrect = checkAnswer(selectedAnswer.value, currentQuestion.value.answer)
  questionStatus.value[currentQuestionId] = isCorrect ? "answered" : "wrong" // 使用题目ID更新状态
  console.log("当前题目ID currentQuestionId:", currentQuestionId)
  console.log("当前题目状态 questionStatus.value:", questionStatus.value)
  console.log("记录用户的答题历史 answerHistory.value:", answerHistory.value)

  // 显示答案和解析
  showAnswer.value = true // 显示答案
  if (isCorrect) {
    ElMessage.success()
  } else {
    ElMessage.error()
  }

  // 下一题
  if (practiceSettings.value.autoNextCorrect) {
    // 延迟0.5s后执行
    setTimeout(() => {
      nextQuestion()
    }, 800)
  }
}

// 修改submitPracticeRecord函数，使用活动记录API
async function submitPracticeRecord(status: string) {
  if (!currentActivityId.value) return

  // 转换为本地时区的时间格式
  _practiceEndTime = new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 19)
    .replace("T", " ")

  try {
    console.log("开始提交练习记录...")

    // 先保存所有答题记录
    await saveAnswersToServer()

    // 更新活动状态
    console.log("更新活动状态为:", status)
    await finishActivityRecord({
      id: currentActivityId.value,
      userId, // 使用实际的 userId
      bankId: bankInfo.value.id!,
      type: "practice",
      startTime: "",
      endTime: "",
      status
    })

    // 停止自动保存
    stopAutoSave()
    console.log("练习记录提交成功")
  } catch (e) {
    console.error("提交练习记录失败", e)
    ElMessage.warning("提交练习记录失败")
  }
}

// 添加页面卸载前的处理
onBeforeUnmount(() => {
  // 移除路由守卫
  if (unwatch) {
    unwatch()
    unwatch = null
  }

  // 停止自动保存
  stopAutoSave()

  // 移除可见性事件监听器
  document.removeEventListener("visibilitychange", handleVisibilityChange)

  // 如果练习已开始但未完成，尝试保存当前状态
  // 但是如果是路由切换导致的组件卸载，不要重复保存
  if (practiceStarted.value && currentActivityId.value && !savedByRouteGuard) {
    console.log("组件卸载，保存答题记录到服务器")
    // 使用普通的异步方式保存
    saveAnswersToServer()
  } else {
    console.log("组件卸载，但已通过路由守卫保存过数据，不重复保存")
  }
})

// 添加网络恢复事件监听
window.addEventListener("online", async () => {
  if (practiceStarted.value && currentActivityId.value) {
    await saveAnswersToServer()
  }
})

// 获取下一题
function nextQuestion() {
  const order = groupedQuestionOrder.value
  const curIdxInOrder = order.indexOf(currentIndex.value)

  if (curIdxInOrder < order.length - 1) {
    const nextIdx = order[curIdxInOrder + 1]
    currentIndex.value = nextIdx
    resetSelectedAnswer()

    // 根据题目状态决定是否显示答案
    const nextQuestionId = bankInfo.value.questions[nextIdx].id as string
    showAnswer.value = questionStatus.value[nextQuestionId] === "answered" || questionStatus.value[nextQuestionId] === "wrong"
  } else {
    finishDialogVisible.value = true
  }
}

// 本地缓存当前题目索引
const currentQuestionIndexCache = ref(0) // 本地缓存当前题目索引

// 跳转到指定题目
function goToQuestion(index: number) {
  if (index === currentIndex.value) return
  currentIndex.value = index
  currentQuestionIndexCache.value = index // 更新本地缓存
  resetSelectedAnswer()
  showAnswer.value = questionStatus.value[String(bankInfo.value.questions[index].id)] === "answered" || questionStatus.value[String(bankInfo.value.questions[index].id)] === "wrong"

  // 这里可以调用一个函数来定时更新
  scheduleUpdateCurrentQuestionIndex()
}

// 定时更新当前题目索引
function scheduleUpdateCurrentQuestionIndex() {
  // 如果已经设置了定时器，则不再设置
  if (autoSaveInterval) return

  autoSaveInterval = setInterval(async () => {
    if (currentActivityId.value) {
      await updateCurrentQuestionIndex() // 更新到服务器
    }
  }, 60000) // 每60秒更新一次
}

// 退出练习弹窗
function exitPractice() {
  exitDialogVisible.value = true
}

// 保存并退出（不清除本地缓存）
function saveAndExitPractice() {
  isCompleted.value = false
  exitDialogVisible.value = false

  // 标记将通过路由守卫保存
  // savedByRouteGuard = true

  // 不清除缓存，直接跳转
  router.push({ name: "BankDetail", query: { id: bankInfo.value.id } })
}

// 放弃并退出（清除本地缓存）
async function confirmExitPractice() {
  exitDialogVisible.value = false
  // 先保存答题记录
  await saveAnswersToServer()
  await submitPracticeRecord("abandoned")

  // 标记已保存，避免路由守卫重复保存
  savedByRouteGuard = true

  router.push({ path: "/question-bank/bankDetail", query: { id: bankInfo.value.id } })
}

// 取消退出练习
function cancelExitPractice() {
  exitDialogVisible.value = false
}

// 返回上一页
function goBack() {
  // 标记将通过路由守卫保存
  savedByRouteGuard = true
  router.push({ path: "/question-bank/bankDetail", query: { id: bankInfo.value.id } })
}

// 判断单选题/多选题选项是否为正确答案（二维数组）
function isSingleCorrect(optionKey: string) {
  if (currentQuestion.value.type === "多选题") {
    // 多选题：只要标准答案的任意子数组包含该选项即可
    return currentQuestion.value.answer.some((arr: string[]) => Array.isArray(arr) && arr.includes(optionKey))
  } else {
    // 单选/判断题：只判断第一个子数组
    return Array.isArray(currentQuestion.value.answer[0]) && currentQuestion.value.answer[0].includes(optionKey)
  }
}

// 判断判断题选项是否为正确答案（二维数组）
function isJudgeCorrect(optionKey: string) {
  return Array.isArray(currentQuestion.value.answer[0]) && currentQuestion.value.answer[0].includes(optionKey)
}

// 提交试卷
function handInPaper() {
  if (!allAnswered.value) {
    ElMessage.warning("请先完成所有题目")
    return
  }
  handInDialogVisible.value = true
}

// 确认交卷
async function confirmHandIn() {
  handInDialogVisible.value = false
  isCompleted.value = true
  // 先保存答题记录
  await saveAnswersToServer()
  await submitPracticeRecord("completed")

  // 显示确认对话框
  ElMessageBox.confirm(
    "您已完成所有题目，是否返回题库详情？",
    "确认交卷",
    {
      confirmButtonText: "返回题库详情",
      cancelButtonText: "留下查看解析",
      type: "warning"
    }
  ).then(() => {
    // 用户选择返回
    router.push({ name: "BankDetail", query: { id: bankInfo.value.id } })
  }).catch(() => {
    // 用户选择查看解析
    console.log("用户选择查看解析")
  })

  // 标记已保存，避免路由守卫重复保存
  savedByRouteGuard = true
}

// 清除历史记录
async function clearHistory() {
  // 清空答题记录但保持数组长度
  questionStatus.value = {}
  answerHistory.value = {}
  selectedAnswer.value = ""
  currentIndex.value = 0
  showAnswer.value = false
  hasClearedHistory.value = true

  // 停止自动保存
  stopAutoSave()

  // 只清除活动记录和题目信息
  await deleteActivityAnswersByRecordId(currentActivityId.value)
  console.log("用户手动清除历史记录时清除缓存")
  ElMessage.success("答题记录已清除，您可以重新开始答题。")
  // 不重新加载题库，保持用户选择的题目数量
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

const selectedQuestionTypes = ref<string[]>(["single", "multiple", "fill", "judge", "short"]) // 默认选择所有题型
const filteredQuestions = computed(() => {
  return bankInfo.value.questions.filter(q => selectedQuestionTypes.value.includes(q.type))
})

function updateQuestionCount() {
  practiceSettings.value.questionCount = filteredQuestions.value.length
}

watch(selectedQuestionTypes, updateQuestionCount)

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
  bankInfo.value.questionCount = totalQuestionCount // 更新题目数量
  console.log("更新后的题目数量:", totalQuestionCount)
}, { deep: true })
</script>


<template>
  <div class="practice-wrapper">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <el-main class="main-content">
      <div class="page-container">
        <!-- 练习设置界面 -->
        <div v-if="!practiceStarted" class="practice-settings glass-card">
          <div class="bank-info">
            <h2 class="bank-title">{{ bankInfo.name }}</h2>
            <div class="bank-meta">
              <div class="meta-tag">
                <el-icon><Document /></el-icon>
                <span>题目总数：{{ bankInfo.questionCount }}</span>
              </div>
              <div class="meta-tag" :class="'difficulty-' + bankInfo.difficulty">
                <el-icon><Warning /></el-icon>
                <span>难度：{{ bankInfo.difficulty }}</span>
              </div>
            </div>
          </div>
          <el-form :model="practiceSettings" class="practice-form" v-if="bankInfo.questionCount > 0">
            <div class="settings-section">
              <h3 class="section-title">练习设置</h3>
              
              <el-form-item label="练习模式" class="custom-form-item">
                <el-radio-group v-model="practiceSettings.mode">
                  <el-radio-button label="sequence">顺序练习</el-radio-button>
                  <el-radio-button label="random">随机练习</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <h4 class="sub-title">分题型数量抽取</h4>
              <div class="type-settings-grid">
                <div v-for="item in questionTypeList" :key="item.type" class="type-setting-item">
                  <span class="type-label">{{ item.type }}</span>
                  <div class="type-control">
                    <el-input-number
                      v-model="examSettings.types[item.type].count"
                      :min="0"
                      :max="item.count"
                      :step="1"
                      controls-position="right"
                      size="default"
                    />
                    <span class="type-count">/ {{ item.count }} 题</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="action-bar centered">
              <el-button @click="goBack" class="btn-secondary" size="large">返回题库</el-button>
              <el-button type="primary" class="btn-primary" size="large" @click="startPractice">开始练习</el-button>
            </div>
          </el-form>
          <div v-else class="loading-state">
            <el-skeleton :rows="4" animated />
          </div>
        </div>

        <!-- 练习界面 -->
        <div v-else class="practice-interface">
          <div class="question-area glass-card">
            
            <div class="question-header">
              <div class="type-badge">{{ currentQuestion.type }}</div>
              <div class="header-actions">
                <div class="switch-group">
                  <el-switch
                    v-model="reciteMode"
                    inline-prompt
                    style="--el-switch-on-color: #10b981; --el-switch-off-color: #3b82f6"
                    active-text="背题模式"
                    inactive-text="练习模式"
                  />
                  <el-switch
                    v-model="practiceSettings.autoNextCorrect" 
                    inline-prompt 
                    style="--el-switch-on-color: #f59e0b; --el-switch-off-color: #94a3b8"
                    active-text="自动切题"
                    inactive-text="手动切题"
                  />
                </div>
                <!-- Action Buttons -->
                <el-button v-if="!allAnswered" plain type="info" size="small" @click="exitPractice">退出练习</el-button>
                <el-button v-if="allAnswered && !isCompleted" type="success" size="small" @click="handInPaper">交卷</el-button>
                <el-button v-if="allAnswered" type="primary" size="small" @click="goBack">返回题库</el-button>
                <el-button v-if="!allAnswered" plain type="warning" size="small" @click="clearHistory">清除记录</el-button>
              </div>
            </div>

            <div class="question-content">
              <span class="q-num">{{ groupedQuestionOrder.indexOf(currentIndex) + 1 }}.</span>
              <span class="q-text">{{ currentQuestion.content }}</span>
            </div>

            <!-- 选项题 -->
            <div class="answer-options" v-if="['单选题', '多选题', '判断题'].includes(currentQuestion.type)">
              <template v-if="currentQuestion.options.length">
                <div
                  v-for="option in currentQuestion.options"
                  :key="option.key"
                  class="option-item"
                  :class="{
                    selected: isSelected(option.key),
                    correct: (showAnswer || reciteMode) && isSingleCorrect(option.key),
                    wrong: (showAnswer || reciteMode) && isSelected(option.key) && !isSingleCorrect(option.key),
                    disabled: reciteMode,
                  }"
                  @click="reciteMode ? null : selectOption(option.key)"
                >
                  <span class="opt-prefix">{{ option.key }}.</span>
                  <span class="opt-text">{{ option.content }}</span>
                </div>
              </template>
              <template v-else-if="currentQuestion.type === '判断题'">
                <div
                  v-for="option in [{ key: 'true', content: '对' }, { key: 'false', content: '错' }]"
                  :key="option.key"
                  class="option-item"
                  :class="{
                    selected: isSelected(option.key),
                    correct: (showAnswer || reciteMode) && isJudgeCorrect(option.key),
                    wrong: (showAnswer || reciteMode) && isSelected(option.key) && !isJudgeCorrect(option.key),
                    disabled: reciteMode,
                  }"
                  @click="reciteMode ? null : selectOption(option.key)"
                >
                  <span class="opt-text">{{ option.content }}</span>
                </div>
              </template>
            </div>

            <!-- 填空题/简答题 -->
            <div class="answer-options" v-else-if="['填空题', '简答题'].includes(currentQuestion.type)">
              <template v-if="currentQuestion.type === '填空题'">
                <div v-for="(blank, idx) in currentQuestion.answer.length" :key="idx" class="input-row">
                  <span class="blank-label">第 {{ idx + 1 }} 空：</span>
                  <el-input
                    v-model="selectedAnswerArray[idx]"
                    :placeholder="`请输入答案`"
                    :disabled="showAnswer || reciteMode"
                    class="custom-input"
                  />
                </div>
              </template>
              <template v-else>
                <el-input
                  type="textarea"
                  :rows="5"
                  v-model="selectedAnswerString"
                  placeholder="请输入您的解答"
                  :disabled="showAnswer || reciteMode"
                  class="custom-textarea"
                />
              </template>
              
              <div class="mobile-submit" v-if="['多选题', '填空题', '简答题'].includes(currentQuestion.type)">
                <el-button
                  type="primary"
                  @click="submitAnswer"
                  :disabled="(!selectedAnswer || (Array.isArray(selectedAnswer) && selectedAnswer.length === 0)) || showAnswer || reciteMode"
                >
                  提交本题
                </el-button>
              </div>
            </div>

            <!-- 底部操作栏 -->
            <div class="question-actions">
              <el-button
                v-if="['多选题', '填空题', '简答题'].includes(currentQuestion.type)"
                type="primary"
                class="desktop-only-btn"
                @click="submitAnswer"
                :disabled="(!selectedAnswer || (Array.isArray(selectedAnswer) && selectedAnswer.length === 0)) || showAnswer || reciteMode"
              >
                提交答案
              </el-button>
              
              <el-button @click="handsidebarDialogVisible = true" class="mobile-only-btn" plain>
                查看答题卡
              </el-button>
              
              <el-button
                type="primary"
                plain
                @click="nextQuestion"
                :disabled="!(showAnswer || reciteMode)"
              >
                下一题
              </el-button>
            </div>

            <!-- 答案区 -->
            <transition name="el-zoom-in-top">
              <div v-if="showAnswer || reciteMode" class="question-analysis">
                <div class="analysis-header">答案与解析</div>
                <div class="answer-box">
                  <span class="label">标准答案：</span>
                  <AnswerViewer :answer="currentQuestion.answer" :type="currentQuestion.type" />
                </div>
                <div class="analysis-box">
                  <span class="label">题目解析：</span>
                  <p class="analysis-text">{{ currentQuestion.analysis ? currentQuestion.analysis : '暂无详细解析' }}</p>
                </div>
              </div>
            </transition>
          </div>

          <!-- 侧边栏/答题卡 (PC端) -->
          <div class="sidebar glass-card">
            <div class="progress-info">
              <div class="progress-header">
                <span class="progress-title">答题进度</span>
                <span class="progress-text">{{ progressText }}</span>
              </div>
              <el-progress
                :percentage="progressPercentage"
                :format="progressFormat"
                :stroke-width="8"
                color="#3b82f6"
              />
            </div>
            
            <div class="question-grid-grouped">
              <div v-for="(indexes, type) in questionTypeGroups" :key="type" class="question-type-group">
                <div class="type-title">{{ type }}</div>
                <div class="question-grid">
                  <div
                    v-for="index in indexes"
                    :key="index"
                    class="question-number"
                    :class="{
                      current: currentIndex === index,
                      answered: questionStatus[String(bankInfo.questions[index].id)] === 'answered',
                      wrong: questionStatus[String(bankInfo.questions[index].id)] === 'wrong',
                    }"
                    @click="goToQuestion(index)"
                  >
                    {{ groupedQuestionOrder.indexOf(index) + 1 }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-main>

    <!-- 弹窗部分 -->
    <el-dialog
      v-model="handsidebarDialogVisible"
      title="答题卡"
      width="90vw"
      :append-to-body="true"
      :modal-append-to-body="false"
      custom-class="modern-dialog"
    >
      <div class="sidebar-phone">
        <div class="progress-info">
          <div class="progress-header">
            <span>进度</span>
            <span class="progress-text">{{ progressText }}</span>
          </div>
          <el-progress :percentage="progressPercentage" :format="progressFormat" />
        </div>
        <div class="question-grid-grouped" style="max-height: 50vh; overflow-y: auto;">
          <div v-for="(indexes, type) in questionTypeGroups" :key="type" class="question-type-group">
            <div class="type-title">{{ type }}</div>
            <div class="question-grid">
              <div
                v-for="index in indexes"
                :key="index"
                class="question-number"
                :class="{
                    current: currentIndex === index,
                    answered: questionStatus[String(bankInfo.questions[index].id)] === 'answered',
                    wrong: questionStatus[String(bankInfo.questions[index].id)] === 'wrong',
                }"
                @click="goToQuestion(index); handsidebarDialogVisible = false"
              >
                {{ groupedQuestionOrder.indexOf(index) + 1 }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handsidebarDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handInDialogVisible" title="确认交卷" width="400" :append-to-body="true">
      <div class="dialog-body-text">您已完成所有题目，确定要立即交卷并查看成绩吗？</div>
      <template #footer>
        <el-button @click="handInDialogVisible = false">取消</el-button>
        <el-button type="success" @click="confirmHandIn">确定交卷</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exitDialogVisible" title="退出练习" width="400" :append-to-body="true">
      <div class="dialog-body-text">是否需要保存当前的练习进度？</div>
      <template #footer>
        <el-button @click="cancelExitPractice">取消</el-button>
        <el-button type="danger" plain @click="confirmExitPractice">放弃进度</el-button>
        <el-button type="primary" @click="saveAndExitPractice">保存并退出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 基础与布局 */
.practice-wrapper {
  min-height: 100vh;
  background-color: #f8fafc;
  position: relative;
  overflow-x: hidden;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

.bg-decoration {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 500px;
  overflow: hidden; z-index: 0; pointer-events: none;
}
.blob {
  position: absolute; filter: blur(80px); opacity: 0.4;
  border-radius: 50%; animation: float 12s ease-in-out infinite alternate;
}
.blob-1 {
  top: -100px; left: -100px; width: 400px; height: 400px;
  background: linear-gradient(135deg, #e0e7ff 0%, #bae6fd 100%);
}
.blob-2 {
  top: 100px; right: -50px; width: 350px; height: 350px;
  background: linear-gradient(135deg, #fce7f3 0%, #ddd6fe 100%);
  animation-delay: -5s;
}

@keyframes float {
  0% { transform: translateY(0) scale(1); }
  100% { transform: translateY(30px) scale(1.05); }
}

.main-content {
  position: relative; z-index: 1; padding: 24px;
}
.page-container {
  max-width: 1200px; margin: 0 auto;
}
.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  box-shadow: 0 10px 30px -10px rgba(0,0,0,0.05);
}

/* 练习设置部分 */
.practice-settings {
  max-width: 800px; margin: 0 auto; padding: 40px;
}
.bank-info {
  text-align: center; margin-bottom: 40px;
}
.bank-title {
  font-size: 28px; font-weight: 800; color: #0f172a; margin: 0 0 16px 0;
}
.bank-meta {
  display: flex; justify-content: center; gap: 16px;
}
.meta-tag {
  display: inline-flex; align-items: center; gap: 6px;
  background: #f1f5f9; color: #64748b; padding: 6px 12px;
  border-radius: 8px; font-size: 14px; font-weight: 500;
}
.difficulty-简单 { color: #059669; background: #d1fae5; }
.difficulty-中等 { color: #d97706; background: #fef3c7; }
.difficulty-困难 { color: #dc2626; background: #fee2e2; }

.section-title {
  font-size: 18px; font-weight: 700; color: #1e293b; margin-bottom: 24px;
}
.sub-title {
  font-size: 15px; font-weight: 600; color: #475569; margin: 24px 0 16px;
}

.type-settings-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;
}
.type-setting-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #f8fafc; padding: 12px 16px; border-radius: 12px; border: 1px solid #e2e8f0;
}
.type-label { font-weight: 600; color: #334155; }
.type-control { display: flex; align-items: center; gap: 8px; }
.type-count { color: #94a3b8; font-size: 13px; width: 50px; text-align: right; }

.action-bar.centered {
  margin-top: 40px; display: flex; justify-content: center; gap: 20px;
}
.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb); border: none; padding: 0 32px;
  color: white; border-radius: 8px;
}
.btn-primary:hover {
  opacity: 0.9;
}

/* 练习界面 */
.practice-interface {
  display: flex; gap: 24px; align-items: flex-start;
}

.question-area {
  flex: 1; padding: 32px; position: relative;
}

.question-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #f1f5f9;
}
.type-badge {
  background: #e0e7ff; color: #4338ca; padding: 6px 14px;
  border-radius: 8px; font-size: 14px; font-weight: 700;
}
.header-actions {
  display: flex; gap: 12px; align-items: center; flex-wrap: wrap; justify-content: flex-end;
}
.switch-group {
  display: flex; gap: 12px; margin-right: 8px;
}

.question-content {
  font-size: 18px; line-height: 1.7; color: #1e293b; font-weight: 500; margin-bottom: 32px;
}
.q-num {
  font-weight: 800; color: #3b82f6; margin-right: 8px; font-size: 20px;
}

.answer-options {
  display: flex; flex-direction: column; gap: 12px; margin-bottom: 32px;
}
.option-item {
  padding: 16px 20px; background: white; border: 2px solid #e2e8f0;
  border-radius: 12px; cursor: pointer; transition: all 0.2s;
  display: flex; align-items: flex-start; gap: 12px;
  font-size: 16px; color: #475569;
}
.option-item:hover:not(.disabled) {
  border-color: #cbd5e1; background: #f8fafc;
}
.option-item.selected {
  border-color: #3b82f6; background: #eff6ff; color: #1d4ed8;
}
.option-item.correct {
  border-color: #10b981; background: #ecfdf5; color: #047857;
}
.option-item.wrong {
  border-color: #ef4444; background: #fef2f2; color: #b91c1c;
}
.option-item.disabled {
  cursor: not-allowed; opacity: 0.8;
}

.opt-prefix { font-weight: 700; width: 24px; flex-shrink: 0; }
.opt-text { flex: 1; line-height: 1.5; }

.input-row {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
}
.blank-label { font-weight: 600; color: #475569; white-space: nowrap; }
.custom-input { max-width: 300px; }
.custom-textarea { width: 100%; max-width: 600px; }

.question-actions {
  display: flex; justify-content: flex-end; gap: 16px; padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

/* 答案解析区 */
.question-analysis {
  margin-top: 24px; padding: 24px; background: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0;
}
.analysis-header {
  font-weight: 700; font-size: 16px; color: #0f172a; margin-bottom: 16px;
}
.answer-box {
  margin-bottom: 12px; display: flex; align-items: flex-start; gap: 8px;
}
.label { font-weight: 600; color: #475569; flex-shrink: 0; }
.analysis-box p { margin: 0; color: #334155; line-height: 1.6; white-space: pre-wrap; }

/* 侧边栏进度 */
.sidebar {
  width: 320px; padding: 24px; flex-shrink: 0; align-self: flex-start;
  max-height: calc(100vh - 80px); display: flex; flex-direction: column;
}
.progress-info {
  margin-bottom: 24px;
}
.progress-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
}
.progress-title { font-weight: 700; color: #1e293b; }
.progress-text { font-weight: 800; color: #3b82f6; }

.question-grid-grouped {
  flex: 1; overflow-y: auto; padding-right: 8px;
}
.question-grid-grouped::-webkit-scrollbar { width: 4px; }
.question-grid-grouped::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 2px; }

.question-type-group {
  margin-bottom: 20px;
}
.type-title {
  font-size: 14px; font-weight: 700; color: #64748b; margin-bottom: 12px;
}
.question-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px;
}
.question-number {
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; font-weight: 600; font-size: 14px; color: #64748b;
  background: #f1f5f9; border: 1px solid transparent; cursor: pointer; transition: all 0.2s;
}
.question-number:hover { border-color: #cbd5e1; }
.question-number.current { background: #3b82f6; color: white; box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3); }
.question-number.answered { background: #ecfdf5; border-color: #34d399; color: #047857; }
.question-number.wrong { background: #fef2f2; border-color: #f87171; color: #b91c1c; }

/* 响应式 */
.mobile-only-btn { display: none; }
.mobile-submit { display: none; }
.dialog-body-text { font-size: 15px; color: #475569; padding: 10px 0; }

@media (max-width: 992px) {
  .practice-interface { flex-direction: column; }
  .sidebar { width: 100%; max-height: none; }
  .question-grid { grid-template-columns: repeat(8, 1fr); }
}

@media (max-width: 768px) {
  .page-container { padding: 12px; }
  .practice-settings { padding: 24px; }
  .question-area { padding: 20px; }
  .header-actions { justify-content: flex-start; }
  
  .desktop-only-btn { display: none; }
  .mobile-only-btn { display: block; }
  .mobile-submit { display: block; margin-top: 16px; }
  
  .question-grid { grid-template-columns: repeat(5, 1fr); }
  
  .switch-group { flex-direction: column; gap: 8px; align-items: flex-start; margin-bottom: 8px; width: 100%; }
}
</style>
