import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  followingCount?: number
  followersCount?: number
  isFollowing?: boolean
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  async function login(username: string, password: string) {
    try {
      const response = await api.post('/auth/login', { username, password })
      token.value = response.data.token
      user.value = response.data.user
      if (token.value) {
        localStorage.setItem('token', token.value)
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      }
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '登录失败' }
    }
  }

  async function register(username: string, email: string, password: string) {
    try {
      const response = await api.post('/auth/register', { username, email, password })
      token.value = response.data.token
      user.value = response.data.user
      if (token.value) {
        localStorage.setItem('token', token.value)
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      }
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '注册失败' }
    }
  }

  async function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    delete api.defaults.headers.common['Authorization']
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      const response = await api.get('/user/info')
      user.value = response.data
    } catch (error) {
      await logout()
    }
  }

  // 初始化时检查token
  if (token.value) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    fetchUserInfo()
  }

  return {
    user,
    token,
    isLoggedIn,
    login,
    register,
    logout,
    fetchUserInfo
  }
})

