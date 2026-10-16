<template>
  <div class="chapter-container">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="章节标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入章节标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input
            v-model="queryParams.bankId"
            placeholder="请输入题库ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="operate-card">
      <el-button type="primary" icon="Plus" @click="handleAdd">新增章节</el-button>
    </el-card>

    <!-- 章节列表 -->
    <el-card>
      <el-table v-loading="loading" :data="chapterList" border>
        <el-table-column label="ID" prop="id" align="center" width="80" />
        <el-table-column label="题库ID" prop="bankId" align="center" width="100" />
        <el-table-column label="章节标题" prop="title" align="center" min-width="200" />
        <el-table-column label="创建时间" prop="createdAt" align="center" width="180" />
        <el-table-column label="更新时间" prop="updatedAt" align="center" width="180" />
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="500px"
      append-to-body
      @close="cancel"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="form.bankId" placeholder="请输入题库ID" />
        </el-form-item>
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入章节标题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getChapterListApi, createChapterApi, updateChapterApi, deleteChapterApi } from './apis/QuestionChapters'
import type { QuestionChapters } from './apis/type'

// 响应式数据
const loading = ref(false)
const chapterList = ref<QuestionChapters.ChapterInfo[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<QuestionChapters.ChapterQuery>({
  current: 1,
  size: 10,
  title: '',
  bankId: ''
})

// 表单数据
const form = reactive<QuestionChapters.ChapterForm>({
  bankId: '',
  title: '',
  description: ''
})

// 对话框配置
const dialog = reactive({
  visible: false,
  title: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  bankId: [
    { required: true, message: '题库ID不能为空', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '章节标题不能为空', trigger: 'blur' }
  ]
})

// 表单引用
const formRef = ref<FormInstance>()
const queryFormRef = ref<FormInstance>()

// 获取章节列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getChapterListApi(queryParams)
    if (res.code === 200) {
      chapterList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取章节列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  if (queryFormRef.value) {
    queryFormRef.value.resetFields()
  }
  handleQuery()
}

// 新增章节
const handleAdd = () => {
  dialog.visible = true
  dialog.title = '新增章节'
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: undefined,
    bankId: '',
    title: '',
    description: ''
  })
}

// 编辑章节
const handleEdit = (row: QuestionChapters.ChapterInfo) => {
  dialog.visible = true
  dialog.title = '编辑章节'
  Object.assign(form, {
    id: row.id,
    bankId: row.bankId,
    title: row.title,
    description: row.description
  })
}

// 删除章节
const handleDelete = (row: QuestionChapters.ChapterInfo) => {
  ElMessageBox.confirm(
    `确认删除章节"${row.title}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteChapterApi(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (form.id) {
          res = await updateChapterApi(form.id, form)
        } else {
          res = await createChapterApi(form)
        }

        if (res.code === 200) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialog.visible = false
          getList()
        } else {
          ElMessage.error(res.message || (form.id ? '修改失败' : '新增失败'))
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后重试')
      }
    }
  })
}

// 取消操作
const cancel = () => {
  dialog.visible = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 页面加载时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.chapter-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.operate-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  text-align: right;
}
</style>
