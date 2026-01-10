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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 70px);
}

.back-button {
  margin-bottom: 20px;
}

.messages-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  height: calc(100vh - 140px);
}

.conversations-card,
.conversation-detail-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.conversations-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%);
  color: #fff;
  padding: 16px 20px;
  border-bottom: none;
}

.conversations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversations-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.conversations-header .el-badge {
  color: #fff;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  background: #fff;
}

.conversations-list::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s;
  margin-bottom: 4px;
  position: relative;
}

.conversation-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s;
}

.conversation-item:hover {
  background: linear-gradient(90deg, rgba(255, 107, 107, 0.08) 0%, rgba(255, 140, 66, 0.05) 100%);
}

.conversation-item:hover::before {
  height: 60%;
}

.conversation-item.active {
  background: linear-gradient(90deg, rgba(255, 107, 107, 0.15) 0%, rgba(255, 140, 66, 0.1) 100%);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2);
}

.conversation-item.active::before {
  height: 80%;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.conversation-username {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.conversation-time {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
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
  background: #fff;
}

.conversation-detail-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
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
  font-weight: 600;
  color: #333;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
}

.messages-list::-webkit-scrollbar {
  width: 6px;
}

.messages-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.messages-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  animation: messageSlideIn 0.3s ease;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.message-sent {
  flex-direction: row-reverse;
}

.message-content-wrapper {
  max-width: 65%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-item.message-sent .message-content-wrapper {
  align-items: flex-end;
}

.message-content {
  padding: 12px 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8e8e8;
  position: relative;
}

.message-content::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: -8px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 8px 8px 0 0;
  border-color: #f8f9fa transparent transparent transparent;
}

.message-item.message-sent .message-content {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.message-item.message-sent .message-content::before {
  left: auto;
  right: -8px;
  border-width: 8px 0 0 8px;
  border-color: #ff6b6b transparent transparent transparent;
}

.message-time {
  font-size: 11px;
  color: #999;
  padding: 0 6px;
}

.message-avatar {
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #fff;
}

.message-input-area {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}

.message-input-area :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s;
}

.message-input-area :deep(.el-textarea__inner):focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.1);
}

.message-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.message-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  transition: all 0.3s;
}

.message-actions :deep(.el-button--primary):hover {
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.4);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.no-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
}

@media (max-width: 768px) {
  .messages-container {
    padding: 16px;
  }

  .messages-layout {
    grid-template-columns: 1fr;
    height: auto;
    gap: 16px;
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

  .conversations-card :deep(.el-card__header) {
    padding: 12px 16px;
  }

  .conversation-detail-card :deep(.el-card__header) {
    padding: 12px 16px;
  }
}
</style>

