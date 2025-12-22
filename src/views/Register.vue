<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <el-icon class="logo-icon"><Food /></el-icon>
        <h2>创建账户</h2>
        <p>加入我们，探索美食世界</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（至少6位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账户？</span>
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Food, User, Message, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const result = await userStore.register(
        registerForm.username,
        registerForm.email,
        registerForm.password
      )
      loading.value = false
      
      if (result.success) {
        ElMessage.success('注册成功')
        router.push('/')
      } else {
        ElMessage.error(result.message || '注册失败')
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 50%, #ff6b6b 100%);
  background-size: 200% 200%;
  animation: gradientShift 10s ease infinite;
  padding: 20px;
  position: relative;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.register-box {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 20px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 56px;
  color: #ff6b6b;
  margin-bottom: 20px;
  filter: drop-shadow(0 2px 4px rgba(255, 107, 107, 0.3));
}

.register-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 10px;
  letter-spacing: -0.5px;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-top: 32px;
}

.register-button {
  width: 100%;
  margin-top: 8px;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.register-footer .el-link {
  margin-left: 8px;
}
</style>

