<template>
  <div class="comment-item">
    <div class="comment-header">
      <div class="comment-author">
        <el-avatar :size="32" :src="getImageUrl(comment.author.avatar || '')">
          {{ comment.author.username?.charAt(0) }}
        </el-avatar>
        <div class="author-info">
          <span class="author-name">{{ comment.author.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
      </div>
      <div v-if="isMyComment" class="comment-actions">
        <el-button
          text
          type="danger"
          size="small"
          @click="handleDelete"
        >
          删除
        </el-button>
      </div>
    </div>
    <div class="comment-content">
      {{ comment.content }}
    </div>
    <div v-if="userStore.isLoggedIn" class="comment-footer">
      <el-button
        text
        size="small"
        @click="showReplyForm = !showReplyForm"
      >
        <el-icon><ChatLineRound /></el-icon>
        回复
      </el-button>
    </div>

    <!-- 回复表单 -->
    <div v-if="showReplyForm" class="reply-form">
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="2"
        placeholder="写下你的回复..."
        maxlength="2000"
        show-word-limit
      />
      <div class="reply-form-actions">
        <el-button size="small" @click="cancelReply">取消</el-button>
        <el-button type="primary" size="small" @click="submitReply" :loading="replying">
          回复
        </el-button>
      </div>
    </div>

    <!-- 回复列表 -->
    <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
      <div
        v-for="reply in comment.replies"
        :key="reply.id"
        class="reply-item"
      >
        <div class="reply-header">
          <el-avatar :size="24" :src="getImageUrl(reply.author.avatar || '')">
            {{ reply.author.username?.charAt(0) }}
          </el-avatar>
          <div class="reply-author-info">
            <span class="reply-author-name">{{ reply.author.username }}</span>
            <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
          </div>
          <div v-if="isMyReply(reply)" class="reply-actions">
            <el-button
              text
              type="danger"
              size="small"
              @click="handleDeleteReply(reply.id)"
            >
              删除
            </el-button>
          </div>
        </div>
        <div class="reply-content">
          {{ reply.content }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useCommentsStore } from '@/stores/comments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatLineRound } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Comment } from '@/stores/comments'

const props = defineProps<{
  comment: Comment
  dishId?: number
  noteId?: number
}>()

const emit = defineEmits<{
  reply: [parentId: number, content: string]
  delete: [commentId: number]
}>()

const userStore = useUserStore()
const commentsStore = useCommentsStore()
const showReplyForm = ref(false)
const replyText = ref('')
const replying = ref(false)

const isMyComment = userStore.isLoggedIn && userStore.user?.id === props.comment.author.id

const isMyReply = (reply: Comment) => {
  return userStore.isLoggedIn && userStore.user?.id === reply.author.id
}

const formatTime = (timeString: string) => {
  const date = new Date(timeString)
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
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const submitReply = async () => {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replying.value = true
  try {
    emit('reply', props.comment.id, replyText.value.trim())
    replyText.value = ''
    showReplyForm.value = false
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replying.value = false
  }
}

const cancelReply = () => {
  replyText.value = ''
  showReplyForm.value = false
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？删除后所有回复也会被删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete', props.comment.id)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteReply = async (replyId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    let result
    if (props.dishId) {
      result = await commentsStore.deleteComment(props.dishId, replyId)
      if (result.success) {
        await commentsStore.fetchComments(props.dishId)
      }
    } else if (props.noteId) {
      result = await commentsStore.deleteNoteComment(props.noteId, replyId)
      if (result.success) {
        await commentsStore.fetchNoteComments(props.noteId)
      }
    }
    
    if (result && result.success) {
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result?.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.comment-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-footer {
  margin-top: 8px;
}

.reply-form {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reply-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.replies-list {
  margin-top: 16px;
  padding-left: 48px;
  border-left: 2px solid #e9ecef;
}

.reply-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.reply-author-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-actions {
  display: flex;
  gap: 8px;
}

.reply-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

