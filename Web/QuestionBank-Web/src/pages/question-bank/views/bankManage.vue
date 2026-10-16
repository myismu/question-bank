<script setup lang="ts">
import type { PublicQuestionBankDetail, PublicQuestionDTO, UpdateQuestionBankDTO } from "../apis/type"
import { Camera, Delete, Edit, Loading, Plus } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { deleteImageApi, uploadImageApi } from "@/common/apis/users/index.ts"
import { getQuestionBankByIdAndAllQuestions, updateQuestionBank } from "../apis/QuestionBank"
import { addQuestion, batchAddQuestions, deleteQuestion as deleteQuestionApi, updateQuestion } from "../apis/Questions"
import FillAnswerEditor from "../component/FillAnswerEditor.vue"
import MultiAnswerEditor from "../component/MultiAnswerEditor.vue"

const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL
const route = useRoute()
const router = useRouter()

// 题库信息
const bankInfo = ref<PublicQuestionBankDetail>({
  id: "",
  userId: "",
  name: "",
  userName: "",
  description: "",
  coverImage: "",
  type: "",
  difficulty: 1,
  questionCount: 0,
  isCollected: false,
  questionsDTOList: []
})

// 标记是否是初始化
const isInitializing = ref(true)

// 编辑题目对话框
const editDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const currentQuestion = ref<PublicQuestionDTO>({
  id: "",
  bankId: "",
  title: "",
  type: "single",
  difficulty: 1,
  sortOrder: 0,
  options: {} as Record<string, string>,
  answer: [[]],
  analysis: "",
  chapterId: "0"
})

// 分页参数
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 筛选参数
const filterParams = ref({
  title: "",
  type: "",
  analysis: "",
  difficulty: undefined as number | undefined
})

const batchAddDialogVisible = ref(false)
const batchQuestionText = ref("")
const parsedQuestions = ref<any[]>([])

// 题库信息编辑相关
const editBankInfo = ref({
  name: "",
  description: "",
  coverImage: ""
})
const isBankInfoEditing = ref(false)
const isBankInfoChanged = ref(false)

// 临时上传的图片，用于处理取消编辑时的清理
const tempUploadedImage = ref("")

// 图片上传相关
const imageUploadRef = ref<HTMLInputElement>()
const isUploadingImage = ref(false)

// 获取题库信息和题目列表
async function getBankInfo() {
  const bankId = route.query.bankId
  if (!bankId) {
    ElMessage.error("题库ID不能为空")
    router.push("/question-bank")
    return
  }

  try {
    const res = await getQuestionBankByIdAndAllQuestions({
      bankId: String(bankId),
      current: pagination.value.current,
      size: pagination.value.size,
      ...filterParams.value
    })

    if (res.data?.records?.[0]) {
      bankInfo.value = res.data.records[0]
      pagination.value = {
        current: res.data.current,
        size: res.data.size,
        total: res.data.total
      }
      // 数据加载完成后，将初始化标记设为 false
      isInitializing.value = false
    }
  } catch (error) {
    ElMessage.error("获取题库信息失败")
    console.log(error)
  }
}

// 重置筛选
function resetFilter() {
  filterParams.value = {
    title: "",
    type: "",
    analysis: "",
    difficulty: undefined
  }
  pagination.value.current = 1
  getBankInfo()
}

// 应用筛选
function applyFilter() {
  pagination.value.current = 1
  getBankInfo()
}

// 处理页码变化
function handleCurrentChange(page: number) {
  pagination.value.current = page
  getBankInfo()
}

// 处理每页条数变化
function handleSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.current = 1
  getBankInfo()
}

// 删除题目
async function handleDeleteQuestion(questionId: string) {
  currentQuestion.value.id = questionId
  deleteDialogVisible.value = true
}

// 确认删除题目
async function confirmDeleteQuestion() {
  try {
    const success = await deleteQuestionApi(currentQuestion.value.id)
    if (success) {
      ElMessage({
        type: "success",
        message: "删除成功"
      })
      getBankInfo()
    } else {
      ElMessage({
        type: "error",
        message: "删除失败，请稍后重试"
      })
    }
  } catch (error) {
    ElMessage({
      type: "error",
      message: "删除失败，请稍后重试"
    })
    console.log(error)
  } finally {
    deleteDialogVisible.value = false
  }
}

// 取消删除题目
function cancelDeleteQuestion() {
  deleteDialogVisible.value = false
}

// 编辑题目
function handleEditQuestion(question: PublicQuestionDTO) {
  console.log("编辑的题目:", question) // 调试信息
  currentQuestion.value = {
    ...question,
    options: { ...question.options },
    // 确保将答案格式化为 string[][]
    answer: Array.isArray(question.answer)
      ? question.answer.map(ans => Array.isArray(ans) ? ans : [ans]) // 确保每个答案都是数组
      : [[question.answer]]
  }
  editDialogVisible.value = true
}

// 重新排序选项
function reorderOptions() {
  const oldOptions = { ...currentQuestion.value.options }
  const newOptions: Record<string, string> = {}
  const keys = Object.keys(oldOptions).sort()

  // 重新分配选项键
  keys.forEach((_, index) => {
    const newKey = String.fromCharCode(65 + index)
    newOptions[newKey] = oldOptions[keys[index]]
  })

  // 更新选项
  currentQuestion.value.options = newOptions

  // 更新答案中的选项键
  if (currentQuestion.value.type === "multiple") {
    currentQuestion.value.answer = currentQuestion.value.answer.flat().map((oldKey) => {
      const index = keys.indexOf(oldKey)
      return [String.fromCharCode(65 + index)]
    })
  } else if (currentQuestion.value.type === "single") {
    const oldKey = currentQuestion.value.answer[0][0] // 取第一个答案
    if (oldKey) {
      const index = keys.indexOf(oldKey)
      currentQuestion.value.answer[0] = [String.fromCharCode(65 + index)] // 确保是 string[]
    }
  }
}

// 删除选项
function handleDeleteOption(key: string) {
  const { [key]: _, ...rest } = currentQuestion.value.options
  currentQuestion.value.options = rest
  // 更新答案，移除被删除选项的答案
  currentQuestion.value.answer = currentQuestion.value.answer.filter((a: string[]) => !a.includes(key))
  // 重新排序选项
  reorderOptions()
}

// 添加选项
function handleAddOption() {
  const keys = Object.keys(currentQuestion.value.options)
  const nextKey = String.fromCharCode(65 + keys.length)
  currentQuestion.value.options[nextKey] = ""
}

// 获取排序后的选项键
function getSortedOptionKeys() {
  return Object.keys(currentQuestion.value.options).sort((a, b) => a.localeCompare(b))
}

// 保存编辑
async function handleSaveEdit() {
  try {
    // 将答案中的 & 符号分割为多个答案
    currentQuestion.value.answer = currentQuestion.value.answer.map(ansGroup =>
      ansGroup.join("&").split("&").map(ans => ans.trim())
    )

    const questionData = {
      ...currentQuestion.value,
      bankId: String(currentQuestion.value.bankId),
      id: currentQuestion.value.id || "",
      answer: currentQuestion.value.answer, // 确保是 string[][]
      options: currentQuestion.value.options || {},
      sortOrder: Number(currentQuestion.value.sortOrder),
      difficulty: Number(currentQuestion.value.difficulty)
    }

    // 发送到后端的数据
    console.log("发送到后端的数据:", JSON.stringify(questionData, null, 2))

    let success: boolean
    if (currentQuestion.value.id) {
      success = await updateQuestion(questionData)
    } else {
      success = await addQuestion(questionData)
    }

    if (success) {
      ElMessage.success(currentQuestion.value.id ? "修改成功" : "添加成功")
      editDialogVisible.value = false
      getBankInfo()
    } else {
      ElMessage.error(currentQuestion.value.id ? "修改失败" : "添加失败")
    }
  } catch (error) {
    console.error("保存失败:", error)
    ElMessage.error(currentQuestion.value.id ? "修改失败" : "添加失败")
  }
}

// 新增题目
function handleAddQuestion() {
  currentQuestion.value = {
    id: "",
    bankId: String(route.query.bankId),
    title: "",
    type: "single",
    difficulty: 1,
    sortOrder: bankInfo.value.questionsDTOList.length + 1,
    options: {},
    answer: [[]],
    analysis: "",
    chapterId: "0"
  }
  editDialogVisible.value = true
}

// 返回题库列表
function goBack() {
  router.push("/question-bank")
}

// 处理题库类型修改
async function handleTypeChange(value: string | number | boolean) {
  // 如果是初始化，不触发修改
  if (isInitializing.value) {
    return
  }
  try {
    const { questionsDTOList, ...updateData } = bankInfo.value
    const updatedBank: UpdateQuestionBankDTO = {
      ...updateData,
      type: value as string
    }
    await updateQuestionBank(updatedBank)
    bankInfo.value.type = value as string
    ElMessage.success("修改成功")
  } catch (error) {
    ElMessage.error("修改失败，请稍后重试")
    console.log(error)
  }
}

// 处理难度等级修改
async function handleDifficultyChange(value: number) {
  // 如果是初始化，不触发修改
  if (isInitializing.value) {
    return
  }
  try {
    const { questionsDTOList, ...updateData } = bankInfo.value
    const updatedBank: UpdateQuestionBankDTO = {
      ...updateData,
      difficulty: value
    }
    await updateQuestionBank(updatedBank)
    bankInfo.value.difficulty = value
    ElMessage.success("修改成功")
  } catch (error) {
    ElMessage.error("修改失败，请稍后重试")
    console.log(error)
  }
}

function openBatchAddDialog() {
  batchAddDialogVisible.value = true
}

function updateParsedQuestions() {
  if (!batchQuestionText.value) {
    parsedQuestions.value = []
    return
  }
  parsedQuestions.value = parseQuestions(batchQuestionText.value)
}

async function submitBatchAdd() {
  if (!batchQuestionText.value) {
    ElMessage.warning("请至少输入一道题目")
    return
  }

  const parsed = parseQuestions(batchQuestionText.value)
  console.log("parsed", parsed)

  if (parsed.length === 0) {
    ElMessage.warning("解析题目失败，请检查格式")
    return
  }

  // 处理答案格式
  const questionsDTOList = parsed.map((q, idx) => {
    let options: Record<string, string> = {}
    if (q.options && Object.keys(q.options).length > 0) {
      options = { ...q.options }
    }
    return {
      id: "", // 如果需要在后端生成ID，可以留空
      bankId: String(route.query.bankId), // 添加当前题库的ID
      type: q.type === "essay" ? "short" : q.type,
      title: q.title,
      options,
      answer: q.answer,
      analysis: q.explanation || "",
      difficulty: 1, // 根据需要设置难度
      sortOrder: idx + 1,
      chapterId: "0" // 默认章节ID
    }
  })

  try {
    const success = await batchAddQuestions(questionsDTOList)
    if (success) {
      ElMessage.success("批量新增题目成功")
      batchAddDialogVisible.value = false
      getBankInfo()
    } else {
      ElMessage.error("批量新增题目失败，请稍后重试")
    }
  } catch (error) {
    ElMessage.error("批量新增题目失败，请稍后重试")
    console.log(error)
  }
}
// 解析题目
function parseQuestions(text: string) {
  const questions: any[] = []
  const lines = text.split("\n")
  let currentQuestion: any = null
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue
    if (["单选题", "多选题", "判断题", "填空题", "简答题"].includes(line)) continue
    if (/^\d+[.、]/.test(line)) {
      if (currentQuestion) {
        // currentQuestion.type = autoDetectType(currentQuestion)
        // console.log("currentQuestion.type", currentQuestion.type)
        questions.push(currentQuestion)
      }
      const title = line.replace(/^\d+[.、]/, "").trim()
      currentQuestion = { title, options: {}, answer: [], explanation: "" }
      continue
    }
    if (!currentQuestion) continue
    if (/^[A-Z][.、]/.test(line)) {
      const [key, value] = line.split(/[.、]/).map(s => s.trim())
      currentQuestion.options[key] = value
    }
    if (line.startsWith("答案:")) {
      const rawAnswer = line.replace("答案:", "").trim()
      const tempType = autoDetectType({ ...currentQuestion, answer: [[rawAnswer]] })
      console.log("currentQuestion.type2", tempType)
      currentQuestion.type = tempType
      currentQuestion.answer = parseAnswer(rawAnswer, tempType)
      continue
    }
    if (line.startsWith("解析:")) {
      currentQuestion.explanation = line.replace("解析:", "").trim()
    }
  }

  questions.push(currentQuestion)

  return questions
}
// ✅ 统一解析答案字段为 string[][] 格式
function parseAnswer(raw: string, type: string): string[][] {
  if (!raw) return [[]]

  switch (type) {
    case "multiple":
      return raw.split("").map(c => [c.trim()]) // ABCD → [["A"],["B"],["C"],["D"]]
    case "fill":
      return raw.split("|").map(group => group.split("&").map(s => s.trim())) // 空一:A&B | 空二:C → [["A","B"],["C"]]
    case "judge":
      return [[raw === "对" || raw === "正确" ? "true" : "false"]]
    case "single":
    case "short":
    default:
      return [[raw.trim()]]
  }
}
function autoDetectType(q: any) {
  // 确保 q.answer 是字符串
  const answer = Array.isArray(q.answer) ? q.answer.flat().join(",") : (typeof q.answer === "string" ? q.answer : "")

  if (["对", "错", "正确", "错误"].includes(answer)) return "judge"

  const optionKeys = Object.keys(q.options)
  if (optionKeys.length > 0) {
    if (/^[A-Z]{2,}$/.test(answer.replace(/,/g, "")) || answer.includes(",")) return "multiple"
    if (/^[A-Z]$/.test(answer)) return "single"
  }

  if (/（.*?）|\(.*?\)/.test(q.title) || answer.includes("|")) return "fill"
  return "short"
}

// 题目类型标签和名称的获取函数
function getQuestionTypeTag(type: string): "primary" | "success" | "warning" | "info" | "danger" {
  const types: Record<string, "primary" | "success" | "warning" | "info" | "danger"> = {
    single: "primary",
    multiple: "success",
    judge: "warning",
    fill: "info",
    short: "danger"
  }
  return types[type] || "info"
}

function getQuestionTypeName(type: string) {
  const types: Record<string, string> = {
    single: "单选题",
    multiple: "多选题",
    judge: "判断题",
    fill: "填空题",
    short: "简答题"
  }
  return types[type] || "未知类型"
}

function startEditBankInfo() {
  editBankInfo.value = {
    name: bankInfo.value.name,
    description: bankInfo.value.description,
    coverImage: bankInfo.value.coverImage
  }
  isBankInfoEditing.value = true
  isBankInfoChanged.value = false
  // 清空临时图片变量
  tempUploadedImage.value = ""
}

function cancelEditBankInfo() {
  // 如果用户上传了临时图片但取消编辑，则删除临时图片
  if (tempUploadedImage.value && tempUploadedImage.value !== bankInfo.value.coverImage) {
    deleteImageApi(tempUploadedImage.value).catch((error) => {
      console.warn("删除临时图片失败:", error)
    })
  }

  // 恢复原始的题库信息
  editBankInfo.value = {
    name: bankInfo.value.name,
    description: bankInfo.value.description,
    coverImage: bankInfo.value.coverImage
  }

  isBankInfoEditing.value = false
  isBankInfoChanged.value = false
  // 清空临时图片
  tempUploadedImage.value = ""
}

function onBankInfoInput() {
  isBankInfoChanged.value
    = editBankInfo.value.name !== bankInfo.value.name
      || editBankInfo.value.description !== bankInfo.value.description
      || editBankInfo.value.coverImage !== bankInfo.value.coverImage
}

async function saveBankInfo() {
  if (!isBankInfoChanged.value) return
  try {
    // 如果用户上传了新图片，且原题库有图片，则删除旧图片
    if (editBankInfo.value.coverImage
      && editBankInfo.value.coverImage !== bankInfo.value.coverImage
      && bankInfo.value.coverImage) {
      try {
        await deleteImageApi(bankInfo.value.coverImage)
        console.log("旧图片删除成功:", bankInfo.value.coverImage)
      } catch (deleteError) {
        console.warn("删除旧图片失败，但继续保存题库信息:", deleteError)
        // 删除旧图片失败不影响保存题库信息，只记录警告
      }
    }

    const { questionsDTOList, ...updateData } = bankInfo.value
    const updatedBank: UpdateQuestionBankDTO = {
      ...updateData,
      name: editBankInfo.value.name,
      description: editBankInfo.value.description,
      coverImage: editBankInfo.value.coverImage
    }
    await updateQuestionBank(updatedBank)
    ElMessage.success("题库信息修改成功")
    isBankInfoEditing.value = false
    // 清空临时图片变量
    tempUploadedImage.value = ""
    getBankInfo()
  } catch (error) {
    ElMessage.error("题库信息修改失败")
    console.log(error)
  }
}

// 触发图片上传
function triggerImageUpload() {
  if (!isBankInfoEditing.value) return
  imageUploadRef.value?.click()
}

// 处理图片文件选择
async function handleImageSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件")
    return
  }

  // 验证文件大小 (限制为1MB)
  if (file.size > 1 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过1MB")
    return
  }

  try {
    isUploadingImage.value = true
    const response = await uploadImageApi(file)
    // 保存到临时变量，而不是直接保存到editBankInfo
    tempUploadedImage.value = response.data
    editBankInfo.value.coverImage = response.data
    onBankInfoInput() // 触发变更检测
    ElMessage.success("图片上传成功")
  } catch (error) {
    ElMessage.error("图片上传失败")
    console.error("图片上传错误:", error)
  } finally {
    isUploadingImage.value = false
    // 清空input值，允许重复选择同一文件
    target.value = ""
  }
}

onMounted(() => {
  getBankInfo()
})
</script>

<template>
  <div class="bank-manage">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <div class="left">
        <el-button @click="goBack" icon="Back">
          返回题库列表
        </el-button>
        <h2 class="bank-title">
          {{ bankInfo.name }}
        </h2>
      </div>
      <div class="right-btns">
        <el-button type="primary" size="large" @click="handleAddQuestion" class="modern-btn-primary">
          <el-icon class="mr-1"><Plus /></el-icon>
          新增单题
        </el-button>
        <el-button size="large" @click="openBatchAddDialog" class="modern-btn-secondary">
          批量导入
        </el-button>
      </div>
    </div>

    <!-- 题库信息 -->
    <div class="bank-info-card">
      <div class="bank-info-flex3">
        <div class="bank-cover">
          <el-image
            v-if="editBankInfo.coverImage || bankInfo.coverImage"
            :src="IMAGE_BASE_URL + (editBankInfo.coverImage || bankInfo.coverImage)"
            fit="cover"
            class="cover-img"
            :class="{ editing: isBankInfoEditing }"
            :preview-src-list="isBankInfoEditing ? [] : [IMAGE_BASE_URL + (editBankInfo.coverImage || bankInfo.coverImage)]"
            :preview-teleported="!isBankInfoEditing"
            @click="triggerImageUpload"
          >
            <template #error>
              <div class="cover-img-placeholder">
                无图片
              </div>
            </template>
            <!-- 编辑模式下的上传提示 -->
            <template v-if="isBankInfoEditing">
              <div class="upload-overlay">
                <div class="upload-icon">
                  <el-icon v-if="isUploadingImage">
                    <Loading />
                  </el-icon>
                  <el-icon v-else>
                    <Camera />
                  </el-icon>
                </div>
                <div class="upload-text">
                  {{ isUploadingImage ? '上传中...' : '点击更换图片' }}
                </div>
              </div>
            </template>
          </el-image>
          <div v-else class="cover-img-placeholder" :class="{ editing: isBankInfoEditing }" @click="triggerImageUpload">
            <template v-if="isBankInfoEditing">
              <div class="upload-placeholder">
                <el-icon v-if="isUploadingImage">
                  <Loading />
                </el-icon>
                <el-icon v-else>
                  <Plus />
                </el-icon>
                <div class="upload-text">
                  {{ isUploadingImage ? '上传中...' : '点击上传图片' }}
                </div>
              </div>
            </template>
            <template v-else>
              无图片
            </template>
          </div>

          <!-- 隐藏的文件输入框 -->
          <input
            ref="imageUploadRef"
            type="file"
            accept="image/*"
            style="display: none;"
            @change="handleImageSelect"
          >
        </div>
        <div class="bank-info-main">
          <div class="info-item">
            <span class="label">题库标题：</span>
            <template v-if="isBankInfoEditing">
              <el-input v-model="editBankInfo.name" @input="onBankInfoInput" style="width: 220px;" />
            </template>
            <template v-else>
              <span class="value">{{ bankInfo.name }}</span>
            </template>
          </div>
          <div class="info-item">
            <span class="label">题库描述：</span>
            <template v-if="isBankInfoEditing">
              <el-input
                v-model="editBankInfo.description"
                @input="onBankInfoInput"
                type="textarea"
                :rows="3"
                :autosize="{ minRows: 3, maxRows: 3 }"
                style="width: 350px; max-height: 72px; overflow-y: auto;"
              />
            </template>
            <template v-else>
              <span class="value">{{ bankInfo.description }}</span>
            </template>
          </div>
        </div>
        <div class="bank-info-side">
          <div class="info-item">
            <span class="label">题库类型：</span>
            <el-switch
              v-model="bankInfo.type"
              active-value="public"
              inactive-value="private"
              active-text="公开"
              inactive-text="私有"
              @change="handleTypeChange"
            />
          </div>
          <div class="info-item">
            <span class="label">难度等级：</span>
            <el-rate
              v-model="bankInfo.difficulty"
              :max="5"
              :texts="['简单', '较简单', '中等', '较难', '困难']"
              show-text
              @change="handleDifficultyChange"
            />
          </div>
          <div class="info-item">
            <span class="label">题目数量：</span>
            <span class="value">{{ bankInfo.questionCount }}</span>
          </div>
          <div class="bank-info-btns">
            <template v-if="isBankInfoEditing">
              <el-button type="primary" size="small" :disabled="!isBankInfoChanged" @click="saveBankInfo">
                保存
              </el-button>
              <el-button size="small" @click="cancelEditBankInfo">
                取消
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="small" @click="startEditBankInfo">
                编辑
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="question-list-container">
      <div class="filter-bar">
        <el-form :inline="true" :model="filterParams">
          <el-form-item label="题目标题">
            <el-input v-model="filterParams.title" placeholder="请输入题目标题" clearable />
          </el-form-item>
          <el-form-item label="题目类型">
            <el-select v-model="filterParams.type" placeholder="请选择题目类型" clearable style="width: 150px;">
              <el-option label="单选题" value="single" />
              <el-option label="多选题" value="multiple" />
              <el-option label="判断题" value="judge" />
              <el-option label="填空题" value="fill" />
              <el-option label="简答题" value="short" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目难度">
            <el-select v-model="filterParams.difficulty" placeholder="请选择难度" clearable style="width: 150px;">
              <el-option label="简单" :value="1" />
              <el-option label="较简单" :value="2" />
              <el-option label="中等" :value="3" />
              <el-option label="较难" :value="4" />
              <el-option label="困难" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目解析">
            <el-input v-model="filterParams.analysis" placeholder="请输入题目解析" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="applyFilter">
              筛选
            </el-button>
            <el-button @click="resetFilter">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <h3>题目列表</h3>
      <el-table :data="bankInfo.questionsDTOList" style="width: 100%">
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">
            {{ (pagination.current - 1) * pagination.size + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="题型" width="120">
          <template #default="{ row }">
            {{
              row.type === 'single' ? '单选题'
              : row.type === 'multiple' ? '多选题'
                : row.type === 'judge' ? '判断题'
                  : row.type === 'fill' ? '填空题'
                    : row.type === 'short' ? '简答题' : '未知题型'
            }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            {{ row.difficulty }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditQuestion(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDeleteQuestion(row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 编辑题目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑题目"
      width="60%"
      :close-on-click-modal="false"
      :append-to-body="true"
      :modal-append-to-body="false"
      :lock-scroll="false"
    >
      <el-form :model="currentQuestion">
        <el-form-item label="题目类型" style="width: 100px;">
          <el-select v-model="currentQuestion.type" placeholder="请选择题目类型">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="简答题" value="short" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容">
          <el-input
            v-model="currentQuestion.title"
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <el-form-item label="难度等级">
          <el-rate
            v-model="currentQuestion.difficulty"
            :max="5"
            :texts="['简单', '较简单', '中等', '较难', '困难']"
            show-text
          />
        </el-form-item>

        <el-form-item label="选项" v-if="['single', 'multiple'].includes(currentQuestion.type)">
          <div v-for="key in getSortedOptionKeys()" :key="key" class="option-item">
            <el-input v-model="currentQuestion.options[key]" placeholder="请输入选项内容">
              <template #prepend>
                {{ key }}
              </template>
            </el-input>
            <el-button type="danger" link @click="handleDeleteOption(key)">
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="handleAddOption">
            添加选项
          </el-button>
        </el-form-item>

        <el-form-item label="正确答案">
          <template v-if="currentQuestion.type === 'fill'">
            <FillAnswerEditor v-model:answer="currentQuestion.answer" />
          </template>
          <template v-else-if="currentQuestion.type === 'multiple'">
            <MultiAnswerEditor
              v-model:answer="currentQuestion.answer"
              :options="currentQuestion.options"
            />
          </template>
          <template v-else-if="currentQuestion.type === 'single'">
            <el-radio-group v-model="currentQuestion.answer[0][0]">
              <div v-for="(option, index) in getSortedOptionKeys()" :key="index">
                <el-radio :label="option">
                  {{ String.fromCharCode(65 + index) }}
                </el-radio>
              </div>
            </el-radio-group>
          </template>
          <template v-else-if="currentQuestion.type === 'judge'">
            <el-radio-group v-model="currentQuestion.answer[0][0]">
              <el-radio label="true">
                对
              </el-radio>
              <el-radio label="false">
                错
              </el-radio>
            </el-radio-group>
          </template>
          <template v-else>
            <el-input
              v-model="currentQuestion.answer[0][0]"
              type="textarea"
              :rows="3"
              placeholder="请输入答案"
            />
          </template>
        </el-form-item>

        <el-form-item label="解析">
          <el-input
            v-model="currentQuestion.analysis"
            type="textarea"
            :rows="3"
            placeholder="请输入题目解析"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除题目对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="警告"
      width="400"
      :append-to-body="true"
      :modal-append-to-body="false"
      :lock-scroll="false"
    >
      <span>确定要删除该题目吗？</span>
      <template #footer>
        <el-button @click="cancelDeleteQuestion">
          取消
        </el-button>
        <el-button type="danger" @click="confirmDeleteQuestion">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchAddDialogVisible"
      title="批量新增题目"
      width="80%"
      :close-on-click-modal="false"
      :append-to-body="true"
      :modal-append-to-body="false"
      :lock-scroll="false"
    >
      <div class="batch-add-container">
        <div class="input-section">
          <el-form>
            <el-form-item label="题目文本">
              <el-input
                v-model="batchQuestionText"
                type="textarea"
                :rows="30"
                placeholder="请输入题目文本"
                @input="updateParsedQuestions"
                class="input-textarea"
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="output-section">
          <h3>识别后的题目</h3>
          <div v-if="parsedQuestions.length > 0">
            <div v-for="(question, index) in parsedQuestions" :key="index" class="question-item">
              <div class="question-header">
                <span class="question-title">{{ index + 1 }}. {{ question.title }}</span>
                <el-tag size="small" :type="getQuestionTypeTag(question.type)">
                  {{ getQuestionTypeName(question.type) }}
                </el-tag>
              </div>
              <div v-if="question.options" class="question-options">
                <div v-for="(option, key) in question.options" :key="key" class="question-option">
                  {{ key }}. {{ option }}
                </div>
              </div>
              <div class="question-answer">
                <strong>答案：</strong>
                <span v-for="(ansGroup, ansIndex) in question.answer" :key="ansIndex">
                  {{ ansGroup.join(', ') }}<span v-if="ansIndex < question.answer.length - 1"> | </span>
                </span>
              </div>
              <div v-if="question.explanation" class="question-explanation">
                <strong>解析：</strong>{{ question.explanation }}
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无解析的题目" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchAddDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatchAdd">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ====== 布局与主容器 ====== */
.bank-manage {
  background-color: var(--el-bg-color-page, #f3f5f9);
  min-height: 100vh;
  padding: 24px;
}
.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.bank-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
}
.right-btns {
  display: flex;
  gap: 12px;
}
.modern-btn-primary {
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  transition: transform 0.2s, box-shadow 0.2s;
}
.modern-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}
.modern-btn-secondary {
  border-radius: 12px;
  font-weight: 600;
  border: 1px solid #cbd5e1;
  transition: transform 0.2s, box-shadow 0.2s;
}
.modern-btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.mr-1 { margin-right: 6px; }

/* ======== 重构 题库详情卡片 (Bento 卡片) ======== */
.bank-info-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
  border: 1px solid var(--el-border-color-light, #e2e8f0);
  transition: all 0.3s;
}
.bank-info-flex3 {
  display: flex;
  gap: 32px;
  align-items: flex-start;
  padding: 24px 32px;
}
.bank-cover {
  width: 160px;
  min-width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  position: relative;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.cover-img:hover {
  transform: scale(1.05);
}
.cover-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
  background: #f1f5f9;
  border-radius: 16px;
  transition: all 0.3s ease;
}
.cover-img-placeholder.editing {
  cursor: pointer;
  border: 2px dashed #cbd5e1;
  background: #f8fafc;
}
.cover-img-placeholder.editing:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
  color: #3b82f6;
}
.bank-info-main {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.bank-info-side {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}
.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.info-item .label {
  color: #64748b;
  font-weight: 600;
  min-width: 80px;
  font-size: 14px;
  line-height: 32px;
}
.info-item .value {
  color: #0f172a;
  font-size: 15px;
  font-weight: 500;
  line-height: 32px;
}
.bank-info-main .el-input {
  max-width: 400px;
}
:deep(.bank-info-main .el-input__wrapper), :deep(.bank-info-main .el-textarea__inner) {
  border-radius: 10px;
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.2s;
}
:deep(.bank-info-main .el-input__wrapper.is-focus), :deep(.bank-info-main .el-textarea__inner:focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4) inset;
}

/* ======== 搜索筛选区 ======== */
.filter-bar {
  margin-bottom: 24px;
  padding: 24px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
  border: 1px solid #e2e8f0;
}
.filter-bar .el-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.filter-bar .el-form-item {
  margin-bottom: 0;
  margin-right: 0;
}
:deep(.filter-bar .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 10px;
  height: 42px;
  padding: 0 16px;
  transition: all 0.2s;
}
:deep(.filter-bar .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4) inset !important;
}

/* ======== 题目列表 Table ======== */
h3 { margin-bottom: 20px; color: #0f172a; font-size: 20px; font-weight: 700; letter-spacing: -0.01em; }
.question-list-container {
  background: #fff;
  padding: 32px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
  border: 1px solid #e2e8f0;
}
:deep(.el-table) { border-radius: 12px; overflow: hidden; border: 1px solid #f1f5f9; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px solid #f1f5f9; color: #334155; }

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* ======== 表单项 ======== */
.option-item { display: flex; gap: 10px; margin-bottom: 12px; align-items: center; width: 100%; }
.option-item .el-input { flex: 1; }
.el-checkbox-group, .el-radio-group { display: flex; flex-wrap: wrap; gap: 12px; }
.el-checkbox, .el-radio { margin-right: 16px; margin-bottom: 0; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }

/* ======== 批量上传区 ======== */
.batch-add-container { display: flex; gap: 24px; padding: 12px; }
.input-section { flex: 1; margin: 0; }
.output-section { flex: 1; border-radius: 16px; background: #f8fafc; padding: 24px; max-height: 600px; overflow-y: auto; border: 1px solid #e2e8f0; }
.input-textarea :deep(.el-textarea__inner) { border-radius: 12px; padding: 16px; background: #fff; border: 1px solid #e2e8f0; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 14px; line-height: 1.6; }

/* ======== 预览的题卡 ======== */
.question-item { margin-bottom: 16px; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.02); }
.question-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px dashed #e2e8f0; }
.question-title { font-weight: 700; color: #0f172a; font-size: 16px; line-height: 1.5; padding-right: 16px; }
.question-options { margin: 12px 0; padding-left: 16px; border-left: 3px solid #cbd5e1; }
.question-option { margin: 8px 0; color: #475569; font-size: 15px; }
.question-answer { margin: 16px 0 12px; padding: 16px; background-color: #ecfdf5; border-radius: 10px; color: #059669; font-weight: 600; font-size: 15px; border: 1px solid #d1fae5; }
.question-explanation { margin: 12px 0 0; padding: 16px; background-color: #f8fafc; border-radius: 10px; color: #475569; font-size: 15px; border: 1px solid #e2e8f0; }

.bank-info-btns { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; width: 100%; }
.bank-info-btns .el-button { border-radius: 8px; font-weight: 500; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; width: 100%; height: 100%; }
.upload-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(2px); display: flex; flex-direction: column; align-items: center; justify-content: center; color: white; opacity: 0; transition: opacity 0.3s ease; border-radius: 16px; }
.cover-img.editing { cursor: pointer; transition: all 0.3s ease; }
.cover-img.editing:hover .upload-overlay { opacity: 1; }
.upload-icon { font-size: 28px; margin-bottom: 8px; }
.upload-text { font-size: 13px; font-weight: 600; text-align: center; }

@media screen and (max-width: 992px) {
  .bank-manage { padding: 16px; }
  .bank-info-flex3 { flex-direction: column; align-items: center; text-align: center; }
  .bank-cover { margin-bottom: 12px; }
  .info-item { justify-content: center; }
  .bank-info-side { width: 100%; align-items: center; }
  .operation-bar { flex-direction: column; gap: 16px; align-items: flex-start; }
  .left { width: 100%; justify-content: space-between; }
  .filter-bar .el-form { flex-direction: column; }
  .filter-bar .el-form-item { width: 100%; }
  .batch-add-container { flex-direction: column; }
  :deep(.filter-bar .el-select), :deep(.filter-bar .el-input) { width: 100%; }
}
</style>
