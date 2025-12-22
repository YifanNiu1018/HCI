<template>
  <div class="settings-container">
    <el-button
      class="back-button"
      @click="$router.push('/')"
      :icon="ArrowLeft"
    >
      返回主页
    </el-button>

    <el-card class="settings-card">
      <template #header>
        <div class="settings-header">
          <h2>账号设置</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="settings-form"
      >
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="100" :src="avatarUrl">
              {{ form.username?.charAt(0) || userStore.user?.username?.charAt(0) }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="small">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="请输入邮箱"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="saving">
            保存修改
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '@/api'
import { getImageUrl } from '@/utils/image'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  username: '',
  email: '',
  avatar: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/upload/image`

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const avatarUrl = computed(() => {
  const avatar = form.avatar || userStore.user?.avatar
  if (!avatar) return ''
  return getImageUrl(avatar)
})

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  if (userStore.user) {
    form.username = userStore.user.username
    form.email = userStore.user.email
    form.avatar = userStore.user.avatar || ''
  } else {
    await userStore.fetchUserInfo()
    if (userStore.user) {
      form.username = userStore.user.username
      form.email = userStore.user.email
      form.avatar = userStore.user.avatar || ''
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const response = await api.put('/user/info', {
          username: form.username.trim(),
          email: form.email.trim(),
          avatar: form.avatar
        })
        
        // 更新本地用户信息
        const oldUsername = userStore.user?.username
        userStore.user = response.data
        ElMessage.success('保存成功')
        
        // 如果用户名改变了，需要重新登录（因为JWT token包含用户名）
        if (oldUsername && response.data.username !== oldUsername) {
          ElMessageBox.confirm('用户名已更改，需要重新登录', '提示', {
            confirmButtonText: '确定',
            showCancelButton: false,
            type: 'warning'
          }).then(() => {
            userStore.logout()
            router.push('/login')
          })
        } else {
          // 刷新用户信息
          await userStore.fetchUserInfo()
          // 重新加载表单数据
          await loadUserInfo()
        }
      } catch (error: any) {
        const errorMessage = error.response?.data?.message || '保存失败'
        ElMessage.error(errorMessage)
      } finally {
        saving.value = false
      }
    }
  })
}

const handleReset = () => {
  if (userStore.user) {
    form.username = userStore.user.username
    form.email = userStore.user.email
    form.avatar = userStore.user.avatar || ''
  }
  formRef.value?.clearValidate()
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarError = (error: any) => {
  console.error('头像上传错误:', error)
  ElMessage.error('头像上传失败，请重试')
}

const handleAvatarSuccess = (response: any) => {
  console.log('上传响应:', response)
  // Element Plus Upload 组件使用原生 XMLHttpRequest
  // 后端返回 JSON: {"url": "/uploads/filename.jpg"}
  // response 可能是字符串（如果后端返回字符串）或对象（如果后端返回JSON）
  let imageUrl = ''
  
  if (typeof response === 'string') {
    // 如果是字符串，尝试解析为JSON
    try {
      const parsed = JSON.parse(response)
      imageUrl = parsed.url || parsed
    } catch {
      // 如果不是JSON，直接使用字符串
      imageUrl = response
    }
  } else if (response && typeof response === 'object') {
    // 如果是对象，提取url字段
    imageUrl = response.url || response.data?.url || response.body || response
  }
  
  if (imageUrl) {
    form.avatar = imageUrl
    ElMessage.success('头像上传成功')
  } else {
    console.error('无法解析上传响应:', response)
    ElMessage.error('头像上传失败：无法获取图片路径')
  }
}
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.settings-card {
  margin-bottom: 24px;
}

.settings-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.settings-form {
  padding: 20px 0;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: inline-block;
}

.settings-form :deep(.el-form-item__label) {
  font-weight: 500;
}

@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }

  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

