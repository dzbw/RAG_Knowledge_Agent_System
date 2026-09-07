<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { fetchDocumentPage, uploadDocument, deleteDocument } from '../../api/document'
import { listCategories } from '../../api/category'
import { formatDateTime } from '../../utils/date'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const categoryId = ref(null)
const categories = ref([])

const uploadOpen = ref(false)
const uploadCat = ref(null)
const uploadTitle = ref('')
const fileList = ref([])
const uploading = ref(false)

async function loadCats() {
  const res = await listCategories()
  categories.value = res.data
}

async function load() {
  loading.value = true
  try {
    const res = await fetchDocumentPage({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      categoryId: categoryId.value || undefined,
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openUpload() {
  uploadCat.value = categoryId.value || (categories.value[0]?.id ?? null)
  uploadTitle.value = ''
  fileList.value = []
  uploadOpen.value = true
}

async function doUpload() {
  const f = fileList.value[0]?.raw
  if (!f || !uploadCat.value) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', f)
    fd.append('categoryId', uploadCat.value)
    if (uploadTitle.value) fd.append('title', uploadTitle.value)
    await uploadDocument(fd)
    uploadOpen.value = false
    load()
  } finally {
    uploading.value = false
  }
}

async function del(row) {
  await ElMessageBox.confirm(`删除文档「${row.title}」及向量？`, '提示')
  await deleteDocument(row.id)
  load()
}

const DOC_STATUS_CN = {
  PROCESSING: '处理中',
  SUCCESS: '解析成功',
  FAIL: '解析失败',
}

function formatDocStatus(status) {
  if (status == null || status === '') return '—'
  return DOC_STATUS_CN[status] ?? status
}

function categoryName(id) {
  if (id == null) return '—'
  const c = categories.value.find((x) => x.id === id)
  return c?.name ?? '—'
}

onMounted(async () => {
  await loadCats()
  load()
})
</script>

<template>
  <div>
    <div class="page-title">文档管理</div>
    <el-card shadow="hover" class="box">
      <div class="toolbar">
        <el-select v-model="categoryId" clearable placeholder="分类筛选" style="width: 180px" @change="() => { page = 1; load() }">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="标题/文件名" clearable style="width: 200px" @clear="load" />
        <el-button type="primary" @click="() => { page = 1; load() }">查询</el-button>
        <el-button type="success" :icon="Upload" @click="openUpload">上传解析</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="fileName" label="文件" min-width="140" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column label="文档类别" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ formatDocStatus(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="vectorCount" label="向量块" width="90" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pg"
        background
        layout="total, prev, pager, next"
        :total="total"
        v-model:page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </el-card>

    <el-dialog v-model="uploadOpen" title="上传知识文件" width="460px">
      <el-alert type="info" :closable="false" show-icon title="支持 txt / pdf / doc / docx / md，将自动解析并向量化写入 Redis" />
      <el-form label-width="80px" style="margin-top: 16px">
        <el-form-item label="分类">
          <el-select v-model="uploadCat" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="uploadTitle" placeholder="可选，默认文件名" />
        </el-form-item>
        <el-form-item label="文件">
          <el-upload v-model:file-list="fileList" :auto-upload="false" :limit="1" drag>
            <el-icon class="el-icon--upload" :size="48"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽或点击选择</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadOpen = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">开始上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.box {
  border-radius: 16px;
}
.pg {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
