<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listCategories, saveCategory, deleteCategory } from '../../api/category'
import { formatDateTime } from '../../utils/date'

const loading = ref(false)
const list = ref([])
const dlg = ref(false)
const form = ref({ id: null, name: '', description: '', icon: 'Document', sortOrder: 0 })

async function load() {
  loading.value = true
  try {
    const res = await listCategories()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { id: null, name: '', description: '', icon: 'Folder', sortOrder: 0 }
  dlg.value = true
}

function openEdit(row) {
  form.value = { ...row, sortOrder: row.sortOrder ?? 0 }
  dlg.value = true
}

async function save() {
  await saveCategory(form.value)
  dlg.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`删除分类「${row.name}」？`, '提示')
  await deleteCategory(row.id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-title">知识分类</div>
    <el-card shadow="hover" class="box">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" title="分类" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.box {
  border-radius: 16px;
}
</style>
