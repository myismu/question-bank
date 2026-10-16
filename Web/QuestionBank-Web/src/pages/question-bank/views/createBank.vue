<script setup lang="ts">
import type { CreateQuestionBankDTO, PublicQuestionDTO } from "../apis/type"
import { Back, QuestionFilled, UploadFilled } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { deleteImageApi, uploadImageApi } from "@/common/apis/users/index.ts"
import { useUserStore } from "@/pinia/stores/user"
import { createQuestionBank } from "../apis/QuestionBank"

const router = useRouter()
const route = useRoute()
const formRef = ref()
const activeStep = ref(0)
const formData = ref({
  name: "",
  description: "",
  difficulty: "",
  coverImage: "",
  type: "public",
  tags: [] as string[],
  chapter: {
    title: "",
    sortOrder: 1
  }
})

const tagOptions = [
  "数学",
  "物理",
  "化学",
  "生物",
  "计算机",
  "英语",
  "语文",
  "历史",
  "地理"
]

const rules = {
  name: [
    { required: true, message: "请输入题库名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  description: [
    { required: true, message: "请输入题库描述", trigger: "blur" },
    { min: 10, max: 500, message: "长度在 10 到 500 个字符", trigger: "blur" }
  ],
  difficulty: [
    { required: true, message: "请选择难度等级", trigger: "change" }
  ],
  type: [
    { required: true, message: "请选择题库类型", trigger: "change" }
  ]
}

const importType = ref("text")
const questionText = ref("")
const parsedQuestions = ref<PublicQuestionDTO[]>([])

// 新增一个本地预览地址
const localCoverPreview = ref("")

// 解析题目文本，将其转换为题目对象数组
function parseQuestions(text: string) {
  const questions: any[] = []
  const lines = text.split("\n")
  let currentQuestion: any = null

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue
    if (["单选题", "多选题", "判断题", "填空题", "简答题"].includes(line)) continue

    // 开始新题
    if (/^\d+[.、]/.test(line)) {
      if (currentQuestion) {
        questions.push(currentQuestion) // ✅ 在上一题结束时直接推入
      }
      const title = line.replace(/^\d+[.、]/, "").trim()
      currentQuestion = {
        title,
        options: {},
        answer: [],
        analysis: ""
      }
      continue
    }

    if (!currentQuestion) continue

    // 选项
    if (/^[A-Z][.、]/.test(line)) {
      const [key, value] = line.split(/[.、]/).map(s => s.trim())
      currentQuestion.options[key] = value
      continue
    }

    // 答案
    if (line.startsWith("答案:")) {
      const rawAnswer = line.replace("答案:", "").trim()
      const tempType = autoDetectType({ ...currentQuestion, answer: [[rawAnswer]] })
      currentQuestion.type = tempType
      currentQuestion.answer = parseAnswer(rawAnswer, tempType)
      continue
    }

    // 解析
    if (line.startsWith("解析:")) {
      currentQuestion.analysis = line.replace("解析:", "").trim()
    }
  }

  // ✅ 最后一题别忘了推进去
  if (currentQuestion) {
    questions.push(currentQuestion)
  }

  console.log("questions:", questions)
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

// 自动检测题型，根据题目的内容返回题型
function autoDetectType(q: any): string {
  console.log("进行type判断的值:", q.answer)
  const answer = Array.isArray(q.answer) && Array.isArray(q.answer[0]) ? q.answer[0][0] : ""

  if (["对", "错", "正确", "错误", "true", "false"].includes(answer)) return "judge" // 判断题
  if (Object.keys(q.options).length > 0) {
    if (/^[A-Z]{2,}$/.test(answer.replace(/,/g, "")) || answer.includes(",")) return "multiple" // 多选题
    if (/^[A-Z]$/.test(answer)) return "single" // 单选题
  }
  if (/（.*?）|\(.*?\)/.test(q.title)) return "fill" // 填空题

  return "short" // 简答题
}

// 监听题目文本的变化，解析新的题目
watch(questionText, (newValue) => {
  if (newValue) {
    parsedQuestions.value = parseQuestions(newValue) // 解析题目
    console.log("parsedQuestions:", parsedQuestions.value)
  } else {
    parsedQuestions.value = [] // 清空解析结果
  }
})

// 处理文件选择，读取文件内容并设置到题目文本中
function handleFileChange(file: any) {
  const reader = new FileReader()
  reader.onload = (e: any) => {
    questionText.value = e.target.result // 设置读取的文件内容
  }
  reader.readAsText(file.raw) // 以文本格式读取文件
}

// 根据题型返回对应的标签类型
function getQuestionTypeTag(type: string): "primary" | "success" | "warning" | "info" | "danger" {
  const types: Record<string, "primary" | "success" | "warning" | "info" | "danger"> = {
    single: "primary",
    multiple: "success",
    judge: "warning",
    fill: "info",
    short: "danger"
  }
  return types[type] || "info" // 默认返回信息类型
}

// 根据题型返回对应的题型名称
function getQuestionTypeName(type: string) {
  const types: Record<string, string> = {
    single: "单选题",
    multiple: "多选题",
    judge: "判断题",
    fill: "填空题",
    short: "简答题"
  }
  return types[type] || "未知类型" // 默认返回未知类型
}

// 处理封面图片的选择，上传图片并设置到表单数据中
async function handleImageChange(file: any) {
  // 1. 本地预览
  const rawFile = file.raw || file
  localCoverPreview.value = URL.createObjectURL(rawFile)

  // 2. 如果已存在封面图片，先删除
  if (formData.value.coverImage) {
    try {
      await deleteImageApi(formData.value.coverImage)
      formData.value.coverImage = "" // 清空本地记录
    } catch {
      ElMessage.warning("原图片删除失败，但不影响新图片上传")
    }
  }

  // 3. 上传新图片
  try {
    const res = await uploadImageApi(rawFile)
    const imageUrl = res.data
    formData.value.coverImage = imageUrl // 这个用于最终提交
    ElMessage.success("图片上传成功")
  } catch {
    ElMessage.error("图片上传失败")
  }
}

// 前往下一步，检查是否至少添加了一道题目
function nextStep() {
  if (parsedQuestions.value.length === 0) {
    ElMessage.warning("请至少添加一道题目") // 提示用户添加题目
    return
  }
  activeStep.value++ // 增加当前步骤
}

// 返回上一步
function prevStep() {
  activeStep.value-- // 减少当前步骤
}

// 提交表单，创建题库
async function submitForm() {
  if (!formRef.value) return // 检查表单引用是否存在
  try {
    await formRef.value.validate() // 验证表单
    // 组装题库参数
    const userStore = useUserStore()
    // 题库难度映射
    const difficultyMap: Record<string, number> = { 简单: 1, 中等: 2, 困难: 3 }

    // 题目设置：
    // 保证每道题有 chapterId 字段，前端传 '0' 或 ''，后端分配实际章节id
    const questionsDTOList = parsedQuestions.value.map(q => ({
      ...q,
      chapterId: q.chapterId || "0"
    }))

    // 只传单个章节对象，如果没有标题则不传 chapters 字段
    const chaptersDTO = formData.value.chapter.title
      ? { title: formData.value.chapter.title, sortOrder: formData.value.chapter.sortOrder }
      : undefined

    const payload: CreateQuestionBankDTO = {
      userId: userStore && userStore.userId ? userStore.userId : "",
      name: formData.value.name,
      description: formData.value.description,
      coverImage: formData.value.coverImage,
      type: formData.value.type,
      difficulty: difficultyMap[formData.value.difficulty] || 1,
      questionsDTOList,
      chaptersDTO
    }

    await createQuestionBank(payload) // 调用 API 创建题库
    ElMessage.success("题库创建成功！") // 提示用户创建成功
    goToQuestionBank() // 跳转到题库管理页面
  } catch (error) {
    console.error("请检查表单填写是否正确:", error) // 打印错误信息
    ElMessage.error("请检查表单填写是否正确") // 提示用户检查表单
  }
}

// 跳转到首页
function goToHome() {
  router.push({ name: "Home" })
}

// 跳转到题库管理页面
function goToQuestionBank() {
  router.push({ name: "QuestionBank" })
}

// 根据来源路径返回
function handleBack() {
  const from = route.query.from
  console.log("当前路由参数：", route.query)
  console.log("from参数值：", from)

  // 如果没有from参数，默认返回题库管理页面
  if (!from) {
    console.log("没有from参数，默认返回题库管理")
    goToQuestionBank()
    return
  }

  console.log("from === \"home\" 的判断结果：", from === "home")
  if (from === "home") {
    console.log("准备跳转到首页")
    goToHome()
  } else {
    console.log("准备跳转到题库管理")
    goToQuestionBank()
  }
}
</script>

<template>
  <div class="main-content">
    <div class="form-container">
      <el-button @click="handleBack" :icon="Back">
        返回{{ route.query.from === 'home' ? '首页' : '题库管理' }}
      </el-button>
      <h2 class="form-title">
        创建新题库
      </h2>
      <!-- 步骤条 -->
      <div class="steps-container">
        <el-steps :value="activeStep" finish-status="success" simple>
          <el-step title="题目录入" description="导入或输入题目" />
          <el-step title="题库信息" description="填写题库基本信息" />
        </el-steps>
      </div>
      <!-- 步骤内容 -->
      <div class="step-content">
        <!-- 第一步：题目录入 -->
        <div v-if="activeStep === 0">
          <el-form-item label="导入方式">
            <el-radio-group v-model="importType">
              <el-radio label="text">
                文本导入
              </el-radio>
              <el-radio label="file">
                文件导入
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <!-- 文本导入 -->
          <el-form-item v-if="importType === 'text'" label="题目文本">
            <div class="import-tip">
              <el-popover placement="right" :width="400" trigger="hover" popper-class="format-tip-popover">
                <template #reference>
                  <el-button type="primary" plain size="small">
                    <el-icon><QuestionFilled /></el-icon>
                    查看格式说明
                  </el-button>
                </template>
                <template #default>
                  <div class="format-tip-content">
                    <h4>格式说明</h4>
                    <p>1. 试题需要有序号，支持：1. 或1、</p>
                    <p>2. 答案、解析后面要有冒号，如：答案：</p>
                    <p>3. 选项后面需要点或顿号，如：A. 或A、</p>
                    <p>4. 答案另起一行，如：答案：A</p>
                    <p>5. 填空题有多个答案用 | 隔开，如：春 | 夏 | 秋</p>
                    <p>6. 判断题支持：正确、错误、对、错</p>
                    <p>7. 简答题等，答案中如有（1）（2）等，编辑成一行导入，不要分段</p>
                  </div>
                </template>
              </el-popover>
            </div>

          </el-form-item>
          <div class="question-input-container">
            <div class="question-input">
              <el-input
                v-model="questionText"
                type="textarea"
                :rows="20"
                placeholder="
1.驾驶人有下列哪种违法行为一次记6分
A、使用其他车辆行驶证
B、饮酒后驾驶机动车
C、车速超过规定时速50%以上
D、违法占用应急车道行驶
答案:D
解析:请仔细阅读交规

2.驾驶人有下列哪种违法行为一次记6分？
A、使用其他车辆行驶证
B、饮酒后驾驶机动车
C、车速超过规定时速50%以上
D、违法占用应急车道行驶
答案:ABCD
解析:请仔细阅读交规

3.国际象棋起源于英国吗？
答案:对
解析:无

4.我国古典四大名著是（）（）（）（）
答案:红楼梦|水浒传|三国演义|西游记
解析:无

5.如何保持身体健康？
答案:规律饮食、坚持锻炼，早睡早起，定期体检。
解析:无"
                class="input-textarea"
              />
            </div>
            <div class="question-preview">
              <template v-if="parsedQuestions.length > 0">
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
                    <span v-for="(ans, ansIndex) in question.answer" :key="ansIndex">
                        {{ ans.join(', ') }}<span v-if="ansIndex < question.answer.length - 1"> | </span>
                      </span>
                  </div>
                  <div v-if="question.analysis" class="question-explanation">
                    <strong>解析：</strong>{{ question.analysis }}
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无解析的题目" />
            </div>
          </div>
          <!-- 文件导入 -->
          <el-form-item v-if="importType === 'file'" label="题目文件">
            <el-upload
              class="upload-demo"
              drag
              value="#"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="handleFileChange"
              :limit="1"
              accept=".txt,.doc,.docx"
            >
              <el-icon class="el-icon--upload">
                <UploadFilled />
              </el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 txt、doc、docx 格式，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          <div class="step-actions">
            <el-button @click="goToQuestionBank">
              取消
            </el-button>
            <el-button type="primary" @click="nextStep">
              下一步
            </el-button>
          </div>
        </div>
        <!-- 第二步：题库信息 -->
        <div v-if="activeStep === 1">
          <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" label-position="right">
            <el-form-item label="题库名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入题库名称" />
            </el-form-item>
            <el-form-item label="题库描述" prop="description">
              <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入题库描述" />
            </el-form-item>
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="formData.difficulty" placeholder="请选择难度等级" style="width: 100%">
                <el-option label="简单" value="简单" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
            <el-form-item label="章节标题">
              <el-input
                v-model="formData.chapter.title"
                placeholder="请输入章节标题（可选）"
                style="width: 220px;"
              />
            </el-form-item>
            <el-form-item label="章节排序">
              <el-input-number
                v-model="formData.chapter.sortOrder"
                :min="1"
                style="width: 100px;"
              />
            </el-form-item>
            <el-form-item label="题库封面" prop="coverImage">
              <div class="cover-upload-row">
                <div class="cover-upload-col">
                  <el-upload
                    class="upload-demo"
                    drag
                    value="#"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleImageChange"
                  >
                    <el-icon class="el-icon--upload">
                      <UploadFilled />
                    </el-icon>
                    <div class="el-upload__text">
                      将文件拖到此处，或<em>点击上传</em>
                    </div>
                    <template #tip>
                      <div class="el-upload__tip">
                        支持 jpg/png 格式，建议尺寸 800x400
                      </div>
                    </template>
                  </el-upload>
                </div>
                <div class="cover-preview-col" v-if="localCoverPreview || formData.coverImage">
                  <img
                    :src="localCoverPreview || formData.coverImage"
                    class="preview-image"
                    alt="题库封面预览"
                  >
                  <div class="preview-tip">
                    封面预览
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="题库类型" prop="type">
              <el-radio-group v-model="formData.type">
                <el-radio label="public">
                  公开题库
                </el-radio>
                <el-radio label="private">
                  私有题库
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入标签"
                style="width: 100%"
              >
                <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
              </el-select>
            </el-form-item>
            <div class="step-actions">
              <el-button @click="prevStep">
                上一步
              </el-button>
              <el-button type="primary" @click="submitForm">
                创建题库
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 876px) {
  .main-content {
    max-width: 100% !important;
    padding: 3px !important;
  }
}
body {
  background-color: #f5f7fa;
}
.main-content {
  flex: 1;
  padding: 20px;
  max-width: 80%;
  margin: 0 auto;
  width: 100%;
}
.form-container {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  max-width: 1600px;
  margin: 0 auto;
}
.form-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
  padding-top: 10px;
}
.steps-container {
  margin-bottom: 30px;
}
.step-content {
  margin-top: 20px;
}
.step-actions {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
}
.upload-demo {
  width: 100%;
  max-width: 220px;
}
.upload-demo .el-upload {
  width: 100%;
}
.upload-demo .el-upload-dragger {
  width: 100%;
  height: 120px;
}
.preview-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  transition: transform 0.2s;
  background: #f5f7fa;
  display: block;
}
.question-input-container {
  display: flex;
  gap: 30px;
  margin-top: 10px;
  min-height: 600px;
  width: 100%;
}
.question-input {
  flex: 1.5;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  min-height: 600px;
}
.input-textarea {
  max-height: 600px;
  overflow-y: auto;
}

.question-preview {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #f8f9fa;
  max-height: 600px;
  overflow-y: auto;
}
.question-input .el-textarea__inner {
  height: 100%;
  min-height: 600px;
  font-size: 14px;
  line-height: 1.6;
  font-family: "Courier New", Courier, monospace;
  width: 100%;
}
.question-item {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}
.question-item:last-child {
  margin-bottom: 0;
}
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #ebeef5;
}
.question-title {
  font-weight: bold;
  color: #303133;
}
.question-options {
  margin: 10px 0;
  padding-left: 20px;
}
.question-option {
  margin: 5px 0;
  color: #606266;
}
.question-answer {
  margin: 10px 0;
  padding: 8px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}
.question-explanation {
  margin: 10px 0;
  padding: 8px;
  background-color: #f4f4f5;
  border-radius: 4px;
  color: #909399;
}
.import-tip {
  margin-bottom: 10px;
}
.format-tip-content {
  padding: 10px;
}
.format-tip-content h4 {
  margin: 0 0 10px 0;
  color: #303133;
}
.format-tip-content p {
  margin: 5px 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}
.format-tip-popover {
  max-height: 400px;
  overflow-y: auto;
}
.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 8px;
}
.chapter-card {
  padding: 10px 16px;
  border-radius: 6px;
  background: #f8f9fa;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  transition: box-shadow 0.2s;
}
.chapter-card-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.chapter-title-input {
  flex: 2;
}
.chapter-sort-input {
  width: 90px;
}
.chapter-delete-btn {
  margin-left: auto;
}
.chapter-add-btn {
  width: 100%;
  margin-top: 4px;
}
.cover-upload-row {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  width: 100%;
}
.cover-upload-col {
  flex: 0 0 220px;
  min-width: 180px;
  max-width: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.cover-preview-col {
  flex: 1 1 0;
  min-width: 260px;
  max-width: 700px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: #fafbfc;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow-x: auto;
  text-align: left;
}
.cover-preview-col .preview-image {
  max-width: 100%;
  max-height: 220px;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  transition: transform 0.2s;
  background: #f5f7fa;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.cover-preview-col .preview-tip {
  position: absolute;
  bottom: 10px;
  right: 20px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 13px;
  padding: 2px 10px;
  border-radius: 12px;
  pointer-events: none;
}
</style>
