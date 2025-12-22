import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export interface Message {
  id: number
  sender: {
    id: number
    username: string
    email: string
    avatar?: string
  }
  receiver: {
    id: number
    username: string
    email: string
    avatar?: string
  }
  content: string
  isRead: boolean
  createdAt: string
}

export interface Conversation {
  otherUser: {
    id: number
    username: string
    email: string
    avatar?: string
  }
  lastMessage: Message
  unreadCount: number
  lastMessageTime: string
}

export const useMessagesStore = defineStore('messages', () => {
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Message[]>([])
  const currentOtherUserId = ref<number | null>(null)
  const unreadCount = ref(0)
  const loading = ref(false)

  async function fetchConversations() {
    loading.value = true
    try {
      const response = await api.get('/messages/conversations')
      conversations.value = response.data
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '加载对话列表失败' }
    } finally {
      loading.value = false
    }
  }

  async function fetchConversation(userId: number) {
    loading.value = true
    try {
      const response = await api.get(`/messages/conversation/${userId}`)
      currentConversation.value = response.data
      currentOtherUserId.value = userId
      // 刷新对话列表以更新未读数量
      await fetchConversations()
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '加载对话失败' }
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(receiverId: number, content: string) {
    try {
      const response = await api.post('/messages', {
        receiverId,
        content
      })
      // 将新消息添加到当前对话
      if (currentOtherUserId.value === receiverId) {
        currentConversation.value.push(response.data)
      }
      // 刷新对话列表
      await fetchConversations()
      return { success: true, message: response.data }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '发送消息失败' }
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await api.get('/messages/unread-count')
      unreadCount.value = response.data.count || 0
      return unreadCount.value
    } catch (error) {
      unreadCount.value = 0
      return 0
    }
  }

  function clearCurrentConversation() {
    currentConversation.value = []
    currentOtherUserId.value = null
  }

  return {
    conversations,
    currentConversation,
    currentOtherUserId,
    unreadCount,
    loading,
    fetchConversations,
    fetchConversation,
    sendMessage,
    fetchUnreadCount,
    clearCurrentConversation
  }
})

