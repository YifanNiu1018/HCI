<template>
  <div class="messages-container">
    <el-button
      class="back-button"
      @click="$router.push('/')"
      :icon="ArrowLeft"
    >
      返回主页
    </el-button>

    <div class="messages-layout">
      <!-- 对话列表 -->
      <el-card class="conversations-card">
        <template #header>
          <div class="conversations-header">
            <h2>私信</h2>
            <el-badge :value="messagesStore.unreadCount" :hidden="messagesStore.unreadCount === 0">
              <el-icon><Message /></el-icon>
            </el-badge>
          </div>
        </template>

        <div v-loading="messagesStore.loading" class="conversations-list">
          <div
            v-for="conversation in messagesStore.conversations"
            :key="conversation.otherUser.id"
            class="conversation-item"
            :class="{ active: messagesStore.currentOtherUserId === conversation.otherUser.id }"
            @click="openConversation(conversation.otherUser.id)"
          >
            <el-avatar :size="50" :src="getImageUrl(conversation.otherUser.avatar || '')">
              {{ conversation.otherUser.username?.charAt(0) }}
            </el-avatar>
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="conversation-username">{{ conversation.otherUser.username }}</span>
                <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
              </div>
              <div class="conversation-preview">
                <span class="conversation-content">{{ conversation.lastMessage.content }}</span>
                <el-badge
                  v-if="conversation.unreadCount > 0"
                  :value="conversation.unreadCount"
                  class="unread-badge"
                />
              </div>
            </div>
          </div>
          <el-empty v-if="messagesStore.conversations.length === 0" description="暂无对话" />
        </div>
      </el-card>

      <!-- 对话详情 -->
      <el-card class="conversation-detail-card" v-if="messagesStore.currentOtherUserId">
        <template #header>
          <div class="conversation-detail-header">
            <div class="other-user-info">
              <el-avatar :size="40" :src="getImageUrl(otherUser?.avatar || '')">
                {{ otherUser?.username?.charAt(0) }}
              </el-avatar>
              <span class="other-username">{{ otherUser?.username }}</span>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="goToUserProfile(messagesStore.currentOtherUserId!)"
            >
              查看主页
            </el-button>
          </div>
        </template>

        <div class="messages-list" ref="messagesListRef">
          <div
            v-for="message in messagesStore.currentConversation"
            :key="message.id"
            class="message-item"
            :class="{ 'message-sent': isSentByMe(message) }"
          >
            <el-avatar
              v-if="!isSentByMe(message)"
              :size="32"
              :src="getImageUrl(message.sender.avatar || '')"
              class="message-avatar"
            >
              {{ message.sender.username?.charAt(0) }}
            </el-avatar>
            <div class="message-content-wrapper">
              <div class="message-content">
                {{ message.content }}
              </div>
              <div class="message-time">{{ formatTime(message.createdAt) }}</div>
            </div>
            <el-avatar
              v-if="isSentByMe(message)"
              :size="32"
              :src="getImageUrl(userStore.user?.avatar || '')"
              class="message-avatar"
            >
              {{ userStore.user?.username?.charAt(0) }}
            </el-avatar>
          </div>
          <div v-if="messagesStore.currentConversation.length === 0" class="no-messages">
            <el-empty description="开始对话吧" />
          </div>
        </div>

        <div class="message-input-area">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.ctrl.enter="handleSendMessage"
            maxlength="1000"
            show-word-limit
          />
          <div class="message-actions">
            <el-button type="primary" @click="handleSendMessage" :loading="sending">
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-card v-else class="conversation-detail-card empty-state">
        <el-empty description="选择一个对话开始聊天" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessagesStore, type Conversation } from '@/stores/messages'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Message } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Message as MessageType } from '@/stores/messages'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messagesStore = useMessagesStore()

const newMessage = ref('')
const sending = ref(false)
const messagesListRef = ref<HTMLElement | null>(null)

const otherUser = computed(() => {
  if (!messagesStore.currentOtherUserId) return null
  const conversation = messagesStore.conversations.find(
    c => c.otherUser.id === messagesStore.currentOtherUserId
  )
  return conversation?.otherUser || null
})

onMounted(async () => {
  await messagesStore.fetchConversations()
  await messagesStore.fetchUnreadCount()
  
  // 检查URL参数中是否有userId，如果有则打开该对话
  const userId = route.query.userId as string
  if (userId) {
    const userIdNum = parseInt(userId)
    if (!isNaN(userIdNum)) {
      await openConversation(userIdNum)
    }
  }
  
  // 定期刷新未读数量
  setInterval(() => {
    messagesStore.fetchUnreadCount()
  }, 30000) // 每30秒刷新一次
})

watch(() => messagesStore.currentConversation, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

const openConversation = async (userId: number) => {
  await messagesStore.fetchConversation(userId)
  await messagesStore.fetchUnreadCount()
  scrollToBottom()
}

const handleSendMessage = async () => {
  if (!newMessage.value.trim() || !messagesStore.currentOtherUserId) {
    return
  }

  sending.value = true
  try {
    const result = await messagesStore.sendMessage(
      messagesStore.currentOtherUserId,
      newMessage.value.trim()
    )
    if (result.success) {
      newMessage.value = ''
      scrollToBottom()
    } else {
      ElMessage.error(result.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const isSentByMe = (message: MessageType) => {
  return message.sender.id === userStore.user?.id
}

const scrollToBottom = () => {
  if (messagesListRef.value) {
    messagesListRef.value.scrollTop = messagesListRef.value.scrollHeight
  }
}

const formatTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
}
</script>

<style scoped>
.messages-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.messages-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  height: calc(100vh - 120px);
}

.conversations-card,
.conversation-detail-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.conversations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversations-header h2 {
  margin: 0;
  font-size: 20px;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #e6f4ff;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.conversation-content {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-badge {
  flex-shrink: 0;
}

.conversation-detail-card {
  display: flex;
  flex-direction: column;
}

.conversation-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.other-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.other-username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.message-sent {
  flex-direction: row-reverse;
}

.message-content-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item.message-sent .message-content-wrapper {
  align-items: flex-end;
}

.message-content {
  padding: 10px 14px;
  background-color: #f0f0f0;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-item.message-sent .message-content {
  background-color: #409eff;
  color: #fff;
}

.message-time {
  font-size: 11px;
  color: #999;
  padding: 0 4px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-input-area {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}

.message-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

@media (max-width: 768px) {
  .messages-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .conversations-card {
    max-height: 300px;
  }

  .conversation-detail-card {
    min-height: 500px;
  }

  .message-content-wrapper {
    max-width: 75%;
  }
}
</style>

