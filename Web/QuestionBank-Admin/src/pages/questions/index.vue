<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import type { QuestionsDTO, QuestionsPageParams, QuestionsForm } from "./apis/type"
import {
  getQuestionsPage,
  getQuestionById,
  createQuestion,
  updateQuestion,
  deleteQuestion
} from "./apis"

// 分页数据
const tableData = ref<QuestionsDTO[]>([])
const total = ref(0)
const loading = ref(false)
const pageParams = reactive<QuestionsPageParams>({ current: 1, size: 10 })

// 查询表单
const searchForm = reactive({
  title: "",
  bankId: undefined as string | number | undefined,
  type: undefined as "single" | "multiple" | "fill" | "judge" | "short" | undefined,
  difficulty: undefined as number | undefined
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<QuestionsDTO | null>(null)

// 编辑弹窗
const editDialogVisible = ref(false)
const editData = ref<QuestionsForm>({
  bankId: "",
  type: "single",
  title: "",
  options: [],
  answer: "",
  analysis: "",
  difficulty: 1,
  sortOrder: 0
})
const isEdit = ref(false)

// 获取分页数据
function fetchTableData() {
  loading.value = true
  getQuestionsPage({
    ...pageParams,
    ...searchForm
  })
    .then(res => {
      tableData.value = res.data.records
      total.value = res.data.total
    })
    .finally(() => {
      loading.value = false
    })
}

// 搜索
function handleSearch() {
  pageParams.current = 1
  fetchTableData()
}

// 重置
function handleReset() {
  searchForm.title = ""
  searchForm.bankId = undefined
  searchForm.type = undefined
  searchForm.difficulty = undefined
  handleSearch()
}

// 分页切换
function handleCurrentChange(val: number) {
  pageParams.current = val
  fetchTableData()
}

// 查看详情
function handleView(row: QuestionsDTO) {
  getQuestionById(row.id).then(res => {
    detailData.value = res.data
    detailDialogVisible.value = true
  })
}

// 新增
function handleAdd() {
  isEdit.value = false
  editData.value = {
    bankId: "",
    type: "single",
    title: "",
    options: [],
    answer: "",
    analysis: "",
    difficulty: 1,
    sortOrder: 0
  }
  editDialogVisible.value = true
}

// 编辑
function handleEdit(row: QuestionsDTO) {
  isEdit.value = true
  editData.value = { ...row }
  editDialogVisible.value = true
}

// 删除
function handleDelete(row: QuestionsDTO) {
  ElMessageBox.confirm("确定要删除这道题目吗？", "提示", { type: "warning" })
    .then(() => {
      deleteQuestion(row.id).then(() => {
        ElMessage.success("删除成功")
        fetchTableData()
      })
    })
    .catch(() => {})
}

// 保存（新增/编辑）
function handleSave() {
  if (isEdit.value) {
    // 编辑模式
    if (!editData.value.id) {
      ElMessage.error("题目ID不能为空")
      return
    }
    updateQuestion(editData.value.id, editData.value).then(() => {
      ElMessage.success("修改成功")
      editDialogVisible.value = false
      fetchTableData()
    })
  } else {
    // 新增模式
    createQuestion(editData.value).then(() => {
      ElMessage.success("新增成功")
      editDialogVisible.value = false
      fetchTableData()
    })
  }
}

onMounted(() => {
  fetchTableData()
})
</script>

<template>
  <div class="questions-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="题目名称">
          <el-input v-model="searchForm.title" placeholder="题目名称" />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" placeholder="题目类型" clearable>
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="填空题" value="fill" />
            <el-option label="判断题" value="judge" />
            <el-option label="简答题" value="short" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="难度" clearable>
            <el-option label="1" :value="1" />
            <el-option label="2" :value="2" />
            <el-option label="3" :value="3" />
            <el-option label="4" :value="4" />
            <el-option label="5" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增题目</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="title" label="题目名称" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          {{
            scope.row.type === "single" ? "单选题" :
            scope.row.type === "multiple" ? "多选题" :
            scope.row.type === "fill" ? "填空题" :
            scope.row.type === "judge" ? "判断题" :
            "简答题"
          }}
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">详情</el-button>
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageParams.current"
      :page-size="pageParams.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handleCurrentChange"
      class="pagination"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="题目详情" width="600px">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="题目名称">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="题目类型">
          {{
            detailData.type === "single" ? "单选题" :
            detailData.type === "multiple" ? "多选题" :
            detailData.type === "fill" ? "填空题" :
            detailData.type === "judge" ? "判断题" :
            "简答题"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="选项">
          <div v-for="(option, index) in detailData.options" :key="index">
            {{ option.label }}: {{ option.value }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="答案">{{ detailData.answer }}</el-descriptions-item>
        <el-descriptions-item label="解析">{{ detailData.analysis }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ detailData.difficulty }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ detailData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="600px">
      <el-form :model="editData" label-width="100px">
        <el-form-item label="题库ID" required>
          <el-input v-model="editData.bankId" />
        </el-form-item>
        <el-form-item label="题目类型" required>
          <el-select v-model="editData.type">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="填空题" value="fill" />
            <el-option label="判断题" value="judge" />
            <el-option label="简答题" value="short" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目名称" required>
          <el-input v-model="editData.title" />
        </el-form-item>
        <el-form-item label="选项" required>
          <div v-for="(option, index) in editData.options" :key="index" class="option-item">
            <el-input v-model="option.label" placeholder="选项标签" style="width: 200px" />
            <el-input v-model="option.value" placeholder="选项内容" style="width: 300px" />
            <el-button type="danger" @click="editData.options.splice(index, 1)">删除</el-button>
          </div>
          <el-button type="primary" @click="editData.options.push({ label: '', value: '' })">添加选项</el-button>
        </el-form-item>
        <el-form-item label="答案" required>
          <el-input v-model="editData.answer" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="解析" required>
          <el-input v-model="editData.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="难度" required>
          <el-select v-model="editData.difficulty">
            <el-option label="1" :value="1" />
            <el-option label="2" :value="2" />
            <el-option label="3" :value="3" />
            <el-option label="4" :value="4" />
            <el-option label="5" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" required>
          <el-input-number v-model="editData.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.questions-container {
  padding: 30px;
  .search-bar {
    margin-bottom: 20px;
  }
  .toolbar {
    margin-bottom: 10px;
  }
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
  .option-item {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
  }
}
</style>
