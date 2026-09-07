<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function onSubmit() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await login(form.value)
    store.setLogin(res.data.token, res.data.user)
    ElMessage.success('欢迎回来')
    if (res.data.user.role === 'ADMIN') {
      router.push('/admin/dashboard')
    } else {
      router.push('/user/chat')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="bg-blob b1" />
    <div class="bg-blob b2" />
    <div class="login-card glass-card">
      <div class="brand">
        <div class="logo">AI</div>
        <div>
          <h1>Java1234 RAG 企业知识库</h1>
          <p>智能问答 · Spring AI 2.0 · Ollama</p>
        </div>
      </div>
      <el-form :model="form" class="form" size="large" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="btn" round :loading="loading" native-type="submit" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="hint">演示账号：admin / user1 · 密码均为 123456</div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(125deg, #0f172a, #1e1b4b 40%, #312e81);
  position: relative;
  overflow: hidden;
}
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}
.b1 {
  width: 320px;
  height: 320px;
  background: #6366f1;
  top: -80px;
  right: -40px;
}
.b2 {
  width: 280px;
  height: 280px;
  background: #a855f7;
  bottom: -60px;
  left: -40px;
}
.login-card {
  position: relative;
  z-index: 1;
  width: min(420px, 92vw);
  padding: 36px 32px 28px;
  color: #e2e8f0;
}
.brand {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 28px;
}
.logo {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 18px;
  color: #fff;
}
.brand h1 {
  margin: 0;
  font-size: 1.15rem;
  color: #fff;
}
.brand p {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.75;
}
.form :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.45);
  box-shadow: none;
  border: 1px solid rgba(148, 163, 184, 0.25);
}
.btn {
  width: 100%;
  height: 46px;
  font-weight: 600;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border: none;
  margin-top: 8px;
}
.hint {
  margin-top: 18px;
  font-size: 12px;
  opacity: 0.65;
  text-align: center;
}
</style>
