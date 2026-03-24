<template>
  <div class="template-page">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-select
            v-model="categoryFilter"
            placeholder="全部分类"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.value"
              :label="cat.label"
              :value="cat.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" :icon="Refresh" @click="loadData">刷新</el-button>
        </el-col>
        <el-col :span="14" style="text-align: right">
          <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建模板</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="templates"
        v-loading="loading"
        stripe
        row-key="id"
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="60" />

        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getCategoryTagType(row.category)" effect="plain">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="160" />

        <el-table-column label="内容" min-width="240">
          <template #default="{ row }">
            <span class="content-preview">
              {{ row.content?.length > 50 ? row.content.substring(0, 50) + '...' : row.content }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">
            <span class="sort-val">{{ row.sortOrder ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" text @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模板' : '新建模板'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="template-form"
      >
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.value"
              :label="cat.label"
              :value="cat.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入模板标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入模板内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number
                v-model="form.sortOrder"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="form.statusBool"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { templateApi, type QuestionTemplate } from '@/api/template'

const loading = ref(false)
const submitting = ref(false)
const templates = ref<QuestionTemplate[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const categoryFilter = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const categoryOptions = [
  { label: '民法', value: '民法' },
  { label: '刑法', value: '刑法' },
  { label: '劳动法', value: '劳动法' },
  { label: '合同法', value: '合同法' },
  { label: '婚姻家庭', value: '婚姻家庭' },
  { label: '其他', value: '其他' },
]

const defaultForm = () => ({
  id: undefined as number | undefined,
  category: '',
  title: '',
  content: '',
  sortOrder: 0,
  statusBool: true,
})

const form = reactive(defaultForm())

const rules: FormRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await templateApi.getList({
      current: currentPage.value,
      size: pageSize.value,
      category: categoryFilter.value || undefined,
    })
    templates.value = res.records || []
    total.value = res.total || 0
  } catch {
    templates.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, string> = {
    民法: '',
    刑法: 'danger',
    劳动法: 'warning',
    合同法: 'success',
    婚姻家庭: 'info',
    其他: 'info',
  }
  return typeMap[category] || ''
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const openCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEditDialog = (row: QuestionTemplate) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    category: row.category,
    title: row.title,
    content: row.content,
    sortOrder: row.sortOrder ?? 0,
    statusBool: row.status === 1,
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload: QuestionTemplate = {
      title: form.title,
      content: form.content,
      category: form.category,
      sortOrder: form.sortOrder,
      status: form.statusBool ? 1 : 0,
    }
    if (isEdit.value && form.id !== undefined) {
      await templateApi.update(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await templateApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误由 request 拦截器处理
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: QuestionTemplate) => {
  try {
    await templateApi.toggle(row.id!)
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`已${row.status === 1 ? '启用' : '禁用'}`)
  } catch {}
}

const deleteTemplate = async (row: QuestionTemplate) => {
  try {
    await ElMessageBox.confirm(`确认删除模板「${row.title}」？`, '提示', { type: 'warning' })
    await templateApi.delete(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.template-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border: 1px solid #ede8de;
  border-radius: 12px;
}

.content-preview {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.sort-val {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 600;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.template-form {
  padding: 8px 0;
}
</style>
