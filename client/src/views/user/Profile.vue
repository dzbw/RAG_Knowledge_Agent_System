<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchMe, updateProfile, changePassword, uploadAvatar } from '../../api/user'
import { formatDateTime } from '../../utils/date'
import { fileUrl } from '../../utils/files'
import { avatarFallbackBg } from '../../utils/avatarFallback'
import { useUserStore } from '../../stores/user'

const profile = ref({})
const pwd = ref({ old: '', neu: '', neu2: '' })
const userStore = useUserStore()
const avatarUploading = ref(false)

const profileAvStyle = computed(() =>
  profile.value.avatar ? undefined : avatarFallbackBg(profile.value.username),
)

async function load() {
  const res = await fetchMe()
  profile.value = res.data
}

async function saveProfile() {
  await updateProfile({
    realName: profile.value.realName,
  })
  ElMessage.success('已保存')
  if (userStore.user) {
    userStore.setLogin(userStore.token, {
      ...userStore.user,
      realName: profile.value.realName,
    })
  }
  load()
}

async function onAvatarRequest(option) {
  avatarUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', option.file)
    const res = await uploadAvatar(fd)
    const path = res.data
    profile.value.avatar = path
    if (userStore.user) {
      userStore.setLogin(userStore.token, {
        ...userStore.user,
        avatar: path,
      })
    }
    ElMessage.success('头像已更新')
    option.onSuccess(res)
  } catch (e) {
    option.onError(e)
  } finally {
    avatarUploading.value = false
  }
}

async function savePwd() {
  if (!pwd.value.neu || pwd.value.neu.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwd.value.neu !== pwd.value.neu2) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  await changePassword(pwd.value.old, pwd.value.neu)
  ElMessage.success('密码已修改')
  pwd.value = { old: '', neu: '', neu2: '' }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-title">个人中心</div>
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="box">
          <template #header>基本资料</template>
          <el-form label-width="100px">
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar
                  :size="88"
                  :src="profile.avatar ? fileUrl(profile.avatar) : undefined"
                  class="av av-text"
                  :style="profileAvStyle"
                >
                  <span class="av-fallback">{{ (profile.realName || profile.username || '?').slice(0, 1) }}</span>
                </el-avatar>
                <div class="avatar-actions">
                  <el-upload
                    :show-file-list="false"
                    accept=".jpg,.jpeg,.png,.gif,.webp"
                    :http-request="onAvatarRequest"
                    :disabled="avatarUploading"
                  >
                    <el-button type="primary" :loading="avatarUploading">
                      <el-icon class="btn-ic"><Plus /></el-icon>
                      上传头像
                    </el-button>
                  </el-upload>
                  <div class="hint">支持 jpg、png、gif、webp，建议正方形图片</div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ profile.role }}</el-tag>
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="profile.realName" />
            </el-form-item>
            <el-form-item label="注册时间">
              {{ formatDateTime(profile.createTime) }}
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="hover" class="box">
          <template #header>修改密码</template>
          <el-form label-width="100px">
            <el-form-item label="原密码"><el-input v-model="pwd.old" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwd.neu" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="pwd.neu2" type="password" show-password /></el-form-item>
            <el-form-item>
              <el-button type="danger" plain @click="savePwd">更新密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.box {
  border-radius: 16px;
  margin-bottom: 16px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.av {
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.65);
  font-family: ui-sans-serif, system-ui, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.av-text :deep(img) {
  object-fit: cover;
}
.av-fallback {
  font-size: 30px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.22);
}
.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn-ic {
  margin-right: 4px;
  vertical-align: middle;
}
.hint {
  font-size: 12px;
  color: #94a3b8;
}
</style>
