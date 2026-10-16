<template>
  <div class="bank-tags-manage">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="标签名" prop="tagName">
          <el-input v-model="queryParams.tagName" placeholder="请输入标签名" clearable />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="queryParams.bankId" placeholder="请输入题库ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <el-button type="primary" @click="handleAdd">新增标签</el-button>
      </template>

      <!-- 标签列表 -->
      <el-table v-loading="loading" :data="tagList">
        <el-table-column type="index" label="序号" width="50" />
        <el-table-column prop="tagName" label="标签名" />
        <el-table-column prop="bankId" label="题库ID" />
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleUpdate(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 标签表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="400px" append-to-body>
      <el-form ref="tagFormRef" :model="tagForm" :rules="rules" label-width="80px">
        <el-form-item label="标签名" prop="tagName">
          <el-input v-model="tagForm.tagName" placeholder="请输入标签名" />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="tagForm.bankId" placeholder="请输入题库ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormItemRule } from 'element-plus'
import { getBankTagListApi, createBankTagApi, updateBankTagApi, deleteBankTagApi } from './apis/BankTags'
import type { BankTags } from './apis/type'

// 查询参数
const queryParams = reactive<BankTags.BankTagQuery>({
  current: 1,
  size: 10,
  tagName: '',
  bankId: ''
})

// 标签列表数据
const tagList = ref<BankTags.BankTagInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tagFormRef = ref<FormInstance>()
const tagForm = reactive<BankTags.BankTagForm>({
  tagName: '',
  bankId: ''
})

// 表单校验规则
const rules = {
  tagName: [
    { required: true, message: "请输入标签名", trigger: "blur" },
    { min: 1, max: 20, message: "长度在 1 到 20 个字符", trigger: "blur" }
  ] as FormItemRule[],
  bankId: [
    { required: true, message: "请输入题库ID", trigger: "blur" }
  ] as FormItemRule[]
}

// 获取标签列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getBankTagListApi(queryParams)
    if (res.code === 200) {
      tagList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error("获取标签列表失败:", error)
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置按钮
const resetQuery = () => {
  queryParams.tagName = ''
  queryParams.bankId = ''
  handleQuery()
}

// 新增按钮
const handleAdd = () => {
  dialogTitle.value = '新增标签'
  dialogVisible.value = true
  tagForm.id = undefined
  tagForm.tagName = ''
  tagForm.bankId = ''
}

// 修改按钮
const handleUpdate = (row: BankTags.BankTagInfo) => {
  dialogTitle.value = '编辑标签'
  dialogVisible.value = true
  Object.assign(tagForm, row)
}

// 删除按钮
const handleDelete = (row: BankTags.BankTagInfo) => {
  ElMessageBox.confirm("确认要删除该标签吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    draggable: true,
    closeOnClickModal: false,
    closeOnPressEscape: false,
    showClose: true,
    center: true
  }).then(async () => {
    try {
      const res = await deleteBankTagApi(row.id)
      if (res.code === 200) {
        ElMessage.success(res.message)
        getList()
      }
    } catch (error) {
      console.error("删除标签失败:", error)
    }
  }).catch(() => {
    ElMessage.info("已取消删除")
  })
}

// 提交表单
const submitForm = async () => {
  if (!tagFormRef.value) return
  await tagFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (tagForm.id) {
          const res = await updateBankTagApi(tagForm.id, tagForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        } else {
          const res = await createBankTagApi(tagForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        }
      } catch (error) {
        console.error("提交表单失败:", error)
      }
    }
  })
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

// 页码改变
const handleCurrentChange = (val: number) => {
  queryParams.current = val
  getList()
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.bank-tags-manage {
  padding: 20px;
}

.search-card {
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

.dialog-footer {
  text-align: right;
}
</style>
