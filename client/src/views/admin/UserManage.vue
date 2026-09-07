<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchUserPage, saveUser, deleteUser } from '../../api/user'
import { formatDateTime } from '../../utils/date'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const dlg = ref(false)
const form = ref({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 'USER',
  status: 1,
})

async function load() {
  loading.value = true
  try {
    const res = await fetchUserPage({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { id: null, username: '', password: '123456', realName: '', role: 'USER', status: 1 }
  dlg.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    role: row.role,
    status: row.status,
  }
  dlg.value = true
}

async function save() {
  await saveUser(form.value)
  dlg.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '提示')
  await deleteUser(row.id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-title">用户管理</div>
    <el-card class="box" shadow="hover">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/姓名" clearable style="width: 220px" @clear="load" />
        <el-button type="primary" @click="() => { page = 1; load() }">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openCreate">新增用户</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '禁用' }}</template>
        </el-table-column>
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

    <el-dialog v-model="dlg" title="用户信息" width="420px" destroy-on-close>
      <el-form :model="form" label-width="88px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码' : '密码'">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '留空则不修改' : ''" />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
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
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.box {
  border-radius: 16px;
}
.pg {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
