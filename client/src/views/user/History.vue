<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listSessions } from '../../api/chat'
import { formatDateTime } from '../../utils/date'

const router = useRouter()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listSessions()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openSession(row) {
  router.push({ path: '/user/chat', query: { sessionId: String(row.id) } })
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-title">会话历史</div>
    <p class="sub">点击查看可继续对话 · 时间格式 YYYY-MM-DD HH:mm:ss</p>
    <el-card shadow="hover" class="box">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSession(row)">打开</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.sub {
  color: #64748b;
  margin: 4px 0 16px;
}
.box {
  border-radius: 16px;
}
</style>
