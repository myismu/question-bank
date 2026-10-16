<template>
  <div class="wrong-questions-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="题目ID">
          <el-input v-model="searchForm.questionId" placeholder="请输入题目ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <el-button type="primary" @click="handleAdd">新增错题</el-button>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="questionId" label="题目ID" width="100" />
        <el-table-column prop="errorCount" label="错误次数" width="100" />
        <el-table-column prop="lastErrorTime" label="最后错误时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.lastErrorTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.updatedAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增错题' : dialogType === 'edit' ? '编辑错题' : '查看错题'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="dialogType === 'view'"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="题目ID" prop="questionId">
          <el-input v-model="form.questionId" placeholder="请输入题目ID" />
        </el-form-item>
        <el-form-item label="错误次数" prop="errorCount">
          <el-input-number v-model="form.errorCount" :min="0" />
        </el-form-item>
        <el-form-item label="最后错误时间" prop="lastErrorTime">
          <el-date-picker
            v-model="form.lastErrorTime"
            type="datetime"
            placeholder="请选择最后错误时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            v-if="dialogType !== 'view'"
            type="primary"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import type { FormInstance } from "element-plus"
import {
  getWrongQuestionsPage,
  createWrongQuestion,
  updateWrongQuestion,
  deleteWrongQuestion,
  getWrongQuestionById
} from "./apis"
import type { WrongQuestionsDTO, WrongQuestionsForm, WrongQuestionsPageParams } from "./apis/type"

// 分页数据
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive<WrongQuestionsPageParams>({
  current: 1,
  size: 10,
  userId: "",
  questionId: ""
})

// 表格数据
const tableData = ref<WrongQuestionsDTO[]>([])

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref<"add" | "edit" | "view">("add")
const formRef = ref<FormInstance>()
const form = reactive<WrongQuestionsForm>({
  userId: "",
  questionId: "",
  errorCount: 0,
  lastErrorTime: new Date().toISOString()
})

// 表单校验规则
const rules = {
  userId: [{ required: true, message: "请输入用户ID", trigger: "blur" }],
  questionId: [{ required: true, message: "请输入题目ID", trigger: "blur" }],
  errorCount: [{ required: true, message: "请输入错误次数", trigger: "blur" }],
  lastErrorTime: [{ required: true, message: "请选择最后错误时间", trigger: "change" }]
}

// 获取数据
const fetchData = async () => {
  try {
    const res = await getWrongQuestionsPage({
      ...searchForm,
      current: page.current,
      size: page.size
    })
    tableData.value = res.data.records
    page.total = res.data.total
  } catch (error) {
    console.error("获取数据失败:", error)
    ElMessage.error("获取数据失败")
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.userId = ""
  searchForm.questionId = ""
  handleSearch()
}

// 分页
const handleSizeChange = (val: number) => {
  page.size = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  page.current = val
  fetchData()
}

// 查看
const handleView = async (row: WrongQuestionsDTO) => {
  try {
    const res = await getWrongQuestionById(row.id)
    Object.assign(form, res.data)
    dialogType.value = "view"
    dialogVisible.value = true
  } catch (error) {
    console.error("获取详情失败:", error)
    ElMessage.error("获取详情失败")
  }
}

// 新增
const handleAdd = () => {
  dialogType.value = "add"
  Object.assign(form, {
    userId: "",
    questionId: "",
    errorCount: 0,
    lastErrorTime: new Date().toISOString()
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: WrongQuestionsDTO) => {
  try {
    const res = await getWrongQuestionById(row.id)
    Object.assign(form, res.data)
    dialogType.value = "edit"
    dialogVisible.value = true
  } catch (error) {
    console.error("获取详情失败:", error)
    ElMessage.error("获取详情失败")
  }
}

// 删除
const handleDelete = (row: WrongQuestionsDTO) => {
  ElMessageBox.confirm("确定要删除该错题记录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  })
    .then(async () => {
      try {
        await deleteWrongQuestion(row.id)
        ElMessage.success("删除成功")
        fetchData()
      } catch (error) {
        console.error("删除失败:", error)
        ElMessage.error("删除失败")
      }
    })
    .catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === "add") {
          await createWrongQuestion(form)
          ElMessage.success("新增成功")
        } else {
          await updateWrongQuestion(form.id!, form)
          ElMessage.success("修改成功")
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error("提交失败:", error)
        ElMessage.error("提交失败")
      }
    }
  })
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.wrong-questions-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>