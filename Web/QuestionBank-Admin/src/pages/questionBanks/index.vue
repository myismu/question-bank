<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import type { QuestionBanksDTO, QuestionBanksPageParams, QuestionBanksForm } from "./apis/type"
import {
  getQuestionBanksPage,
  getQuestionBankById,
  createQuestionBank,
  updateQuestionBank,
  deleteQuestionBank
} from "./apis"

// 分页数据
const tableData = ref<QuestionBanksDTO[]>([])
const total = ref(0)
const loading = ref(false)
const pageParams = reactive<QuestionBanksPageParams>({ current: 1, size: 10 })

// 查询表单
const searchForm = reactive({
  name: "",
  userId: undefined as string | number | undefined,
  type: undefined as "public" | "private" | undefined,
  difficulty: undefined as number | undefined
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<QuestionBanksDTO | null>(null)

// 编辑弹窗
const editDialogVisible = ref(false)
const editData = ref<QuestionBanksForm>({
  name: "",
  description: "",
  coverImage: "",
  type: "public",
  difficulty: 1
})
const isEdit = ref(false)

// 获取分页数据
function fetchTableData() {
  loading.value = true
  getQuestionBanksPage({
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
  searchForm.name = ""
  searchForm.userId = undefined
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
function handleView(row: QuestionBanksDTO) {
  getQuestionBankById(row.id).then(res => {
    detailData.value = res.data
    detailDialogVisible.value = true
  })
}

// 新增
function handleAdd() {
  isEdit.value = false
  editData.value = {
    name: "",
    description: "",
    coverImage: "",
    type: "public",
    difficulty: 1
  }
  editDialogVisible.value = true
}

// 编辑
function handleEdit(row: QuestionBanksDTO) {
  isEdit.value = true
  editData.value = { ...row }
  editDialogVisible.value = true
}

// 删除
function handleDelete(row: QuestionBanksDTO) {
  ElMessageBox.confirm("确定要删除这个题库吗？", "提示", { type: "warning" })
    .then(() => {
      deleteQuestionBank(row.id).then(() => {
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
      ElMessage.error("题库ID不能为空")
      return
    }
    updateQuestionBank(editData.value.id, editData.value).then(() => {
      ElMessage.success("修改成功")
      editDialogVisible.value = false
      fetchTableData()
    })
  } else {
    // 新增模式
    createQuestionBank(editData.value).then(() => {
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
  <div class="question-banks-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="题库名称">
          <el-input v-model="searchForm.name" placeholder="题库名称" />
        </el-form-item>
        <el-form-item label="题库类型">
          <el-select v-model="searchForm.type" placeholder="题库类型" clearable>
            <el-option label="公开" value="public" />
            <el-option label="私有" value="private" />
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
      <el-button type="primary" @click="handleAdd">新增题库</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="name" label="题库名称" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          {{ scope.row.type === "public" ? "公开" : "私有" }}
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column prop="questionCount" label="题目数量" width="100" />
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
    <el-dialog v-model="detailDialogVisible" title="题库详情" width="600px">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="题库名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="题库描述">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="封面图片">
          <el-image
            v-if="detailData.coverImage"
            :src="detailData.coverImage"
            style="width: 100px; height: 100px"
          />
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ detailData.type === "public" ? "公开" : "私有" }}
        </el-descriptions-item>
        <el-descriptions-item label="难度">{{ detailData.difficulty }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ detailData.questionCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑题库' : '新增题库'" width="600px">
      <el-form :model="editData" label-width="100px">
        <el-form-item label="题库名称" required>
          <el-input v-model="editData.name" />
        </el-form-item>
        <el-form-item label="题库描述" required>
          <el-input v-model="editData.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面图片" required>
          <el-input v-model="editData.coverImage" />
        </el-form-item>
        <el-form-item label="题库类型" required>
          <el-select v-model="editData.type">
            <el-option label="公开" value="public" />
            <el-option label="私有" value="private" />
          </el-select>
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
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.question-banks-container {
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
}
</style>
