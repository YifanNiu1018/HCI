<template>
  <div v-loading="loading" class="note-modal-overlay" @click.self="handleClose">
    <div class="note-modal-container">
      <div v-if="note" class="note-modal-content">
        <el-button
          class="close-button"
          @click.stop="handleClose"
          :icon="ArrowLeft"
          circle
        />
        
        <div class="note-content-wrapper">
          <!-- 头部信息 -->
          <div class="detail-header">
            <div class="detail-image-wrapper">
              <img
                :src="getImageUrl(note.image)"
                :alt="note.name"
                class="detail-image"
              />
            </div>
            <div class="detail-info">
              <div class="note-author">
                <el-avatar 
                  :size="40" 
                  :src="note.author.avatar"
                  @click.stop="goToUserProfile(note.author.id)"
                  style="cursor: pointer;"
                >
                  {{ note.author.username?.charAt(0) }}
                </el-avatar>
                <div class="author-info">
                  <span 
                    class="author-name"
                    @click.stop="goToUserProfile(note.author.id)"
                    style="cursor: pointer; color: #409eff;"
                  >
                    {{ note.author.username }}
                  </span>
                  <span class="note-date">{{ formatDate(note.createdAt) }}</span>
                </div>
                <el-button
                  v-if="userStore.isLoggedIn && userStore.user?.id !== note.author.id"
                  :type="isFollowing ? 'info' : 'primary'"
                  size="small"
                  @click="handleToggleFollow"
                  :loading="followLoading"
                  style="margin-left: auto"
                >
                  {{ isFollowing ? '已关注' : '关注' }}
                </el-button>
                <el-tag :type="note.isPublic ? 'success' : 'info'" size="small" style="margin-left: auto">
                  {{ note.isPublic ? '公开' : '私密' }}
                </el-tag>
              </div>
              <h1 class="detail-title">{{ note.name }}</h1>
              <p class="detail-description">{{ note.description }}</p>
              <div class="detail-meta">
                <el-tag
                  v-for="tag in note.tags"
                  :key="tag"
                  size="default"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="detail-actions" v-if="note.isPublic && userStore.isLoggedIn">
                <el-button
                  type="danger"
                  size="default"
                  :icon="note.isFavorite ? StarFilled : Star"
                  @click="handleToggleFavorite"
                  :loading="favoriteLoading"
                >
                  {{ note.isFavorite ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 内容区域 - 左右分栏 -->
          <div class="detail-main-layout">
            <div class="detail-sections">
              <!-- 所需食材 -->
              <div class="content-section">
                <h2 class="section-title">
                  <el-icon><ShoppingBag /></el-icon>
                  所需食材
                </h2>
                <ul class="ingredients-list">
                  <li v-for="(ingredient, index) in note.ingredients" :key="index">
                    <el-icon><Check /></el-icon>
                    <span>{{ ingredient }}</span>
                  </li>
                </ul>
              </div>

              <!-- 制作步骤 -->
              <div class="content-section">
                <h2 class="section-title">
                  <el-icon><Document /></el-icon>
                  制作步骤
                </h2>
                <ol class="steps-list">
                  <li v-for="(step, index) in note.steps" :key="index">
                    <div class="step-number">{{ index + 1 }}</div>
                    <div class="step-content">{{ step }}</div>
                  </li>
                </ol>
              </div>
            </div>

            <!-- 评论区域 - 右侧 -->
            <div v-if="note.isPublic" class="comments-sidebar">
              <div class="comments-section">
                <h2 class="section-title">
                  <el-icon><ChatLineRound /></el-icon>
                  评论 ({{ commentsStore.comments.length }})
                </h2>

                <!-- 评论表单 - 可切换显示 -->
                <div v-if="showCommentForm" class="comment-form">
                  <div v-if="userStore.isLoggedIn">
                    <el-input
                      v-model="newComment"
                      type="textarea"
                      :rows="3"
                      placeholder="写下你的评论..."
                      maxlength="2000"
                      show-word-limit
                    />
                    <div class="comment-form-actions">
                      <el-button
                        text
                        size="small"
                        @click="showCommentForm = false"
                      >
                        取消
                      </el-button>
                      <el-button
                        type="primary"
                        @click="handleSubmitComment"
                        :loading="commentLoading"
                      >
                        发表评论
                      </el-button>
                    </div>
                  </div>
                  <div v-else class="comment-login-tip">
                    <el-button type="primary" @click="$router.push('/login')">
                      登录后发表评论
                    </el-button>
                  </div>
                </div>

                <!-- 评论列表 -->
                <div v-loading="commentsStore.loading" class="comments-list">
                  <div v-if="commentsStore.comments.length === 0" class="no-comments">
                    <el-empty description="暂无评论，快来发表第一条评论吧！" :image-size="100" />
                  </div>
                  <div v-else>
                    <div
                      v-for="comment in commentsStore.comments"
                      :key="comment.id"
                      class="comment-item"
                    >
                      <CommentItem
                        :comment="comment"
                        :note-id="note.id"
                        @reply="handleReply"
                        @delete="handleDeleteComment"
                      />
                    </div>
                  </div>
                </div>
                
                <!-- 底部评论输入栏 -->
                <div class="comment-footer-bar">
                  <div class="comment-input-wrapper">
                    <el-input
                      v-model="newComment"
                      type="textarea"
                      :rows="1"
                      :autosize="{ minRows: 1, maxRows: 3 }"
                      placeholder="写下你的评论..."
                      maxlength="2000"
                      @keyup.ctrl.enter="handleSubmitComment"
                      @keyup.meta.enter="handleSubmitComment"
                    />
                  </div>
                  <el-button
                    v-if="userStore.isLoggedIn"
                    type="primary"
                    @click="handleSubmitComment"
                    :loading="commentLoading"
                    :disabled="!newComment.trim()"
                  >
                    发布
                  </el-button>
                  <el-button
                    v-else
                    type="primary"
                    @click="$router.push('/login')"
                  >
                    登录
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="笔记不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotesStore } from '@/stores/notes'
import { useUserStore } from '@/stores/user'
import { useCommentsStore } from '@/stores/comments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Calendar, ShoppingBag, Check, Document, Star, StarFilled, ChatLineRound } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Note } from '@/stores/notes'
import api from '@/api'
import CommentItem from '@/components/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const notesStore = useNotesStore()
const userStore = useUserStore()
const commentsStore = useCommentsStore()
const note = ref<Note | null>(null)
const loading = ref(false)
const favoriteLoading = ref(false)
const commentLoading = ref(false)
const newComment = ref('')
const showCommentForm = ref(false)
const isFollowing = ref(false)
const followLoading = ref(false)

onMounted(async () => {
  const noteId = parseInt(route.params.id as string)
  if (noteId) {
    await fetchNote(noteId)
    if (note.value && note.value.isPublic) {
      await commentsStore.fetchNoteComments(noteId)
    }
    if (note.value && userStore.isLoggedIn && userStore.user?.id !== note.value.author.id) {
      await checkFollowStatus(note.value.author.id)
    }
  }
})

const fetchNote = async (id: number) => {
  loading.value = true
  try {
    // 直接调用API获取笔记详情，确保获取最新的收藏状态
    note.value = await notesStore.getNoteById(id)
  } catch (error: any) {
    console.error('获取笔记详情失败:', error)
    if (error.response?.status === 404 || error.response?.status === 403) {
      ElMessage.error('笔记不存在或无权访问')
    } else {
      ElMessage.error('获取笔记详情失败')
    }
  } finally {
    loading.value = false
  }
}

const checkFollowStatus = async (userId: number) => {
  try {
    const response = await api.get(`/follow/status/${userId}`)
    isFollowing.value = response.data
  } catch (error) {
    isFollowing.value = false
  }
}

const handleToggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  if (!note.value) return

  followLoading.value = true
  try {
    if (isFollowing.value) {
      await api.delete(`/follow/${note.value.author.id}`)
      isFollowing.value = false
      ElMessage.success('取消关注成功')
    } else {
      await api.post(`/follow/${note.value.author.id}`)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

const handleToggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  if (!note.value) return

  favoriteLoading.value = true
  try {
    const result = await notesStore.toggleFavorite(note.value.id)
    if (result.success && note.value) {
      // 更新当前笔记的收藏状态
      note.value.isFavorite = result.isFavorite
      ElMessage.success(result.isFavorite ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || '操作失败'
    ElMessage.error(errorMessage)
  } finally {
    favoriteLoading.value = false
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!note.value) return

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  commentLoading.value = true
  try {
    const result = await commentsStore.createNoteComment(
      note.value.id,
      newComment.value.trim()
    )
    if (result.success) {
      newComment.value = ''
      showCommentForm.value = false
      await commentsStore.fetchNoteComments(note.value.id)
      ElMessage.success('评论发表成功')
    } else {
      console.error('评论失败:', result.message)
      ElMessage.error(result.message || '评论失败')
    }
  } catch (error: any) {
    console.error('评论异常:', error)
    ElMessage.error(error.response?.data?.message || error.message || '评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleReply = async (parentId: number, content: string) => {
  if (!note.value) return

  commentLoading.value = true
  try {
    const result = await commentsStore.createNoteComment(
      note.value.id,
      content.trim(),
      parentId
    )
    if (result.success) {
      await commentsStore.fetchNoteComments(note.value.id)
      ElMessage.success('回复成功')
    } else {
      ElMessage.error(result.message || '回复失败')
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (commentId: number) => {
  if (!note.value) return

  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await commentsStore.deleteNoteComment(note.value.id, commentId)
    if (result.success) {
      await commentsStore.fetchNoteComments(note.value.id)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleClose = () => {
  router.back()
}

const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
}

const toggleCommentForm = () => {
  showCommentForm.value = !showCommentForm.value
  if (showCommentForm.value && !userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    showCommentForm.value = false
  }
}
</script>

<style scoped>
.note-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow-y: auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.note-modal-container {
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  position: relative;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.note-modal-content {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  position: relative;
}

.close-button {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.close-button:hover {
  background: rgba(255, 255, 255, 1);
}

.note-content-wrapper {
  max-height: 90vh;
  overflow-y: auto;
  padding: 24px;
}

/* 隐藏滚动条但保持滚动功能 */
.note-content-wrapper::-webkit-scrollbar {
  width: 0;
  display: none;
}

.note-content-wrapper {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.detail-header {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
  aspect-ratio: 4 / 3;
  width: 100%;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  display: flex;
  flex-direction: column;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.note-date {
  font-size: 12px;
  color: #999;
}

.detail-title {
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.4;
}

.detail-description {
  font-size: 15px;
  color: #666;
  line-height: 1.7;
  margin-bottom: 16px;
  flex: 1;
}

.detail-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.detail-actions {
  margin-top: auto;
}

.comments-section {
  margin-top: 0;
}

.comment-form {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.comment-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comment-login-tip {
  margin-bottom: 32px;
  padding: 24px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
}

.comments-list {
  min-height: 100px;
}

.no-comments {
  padding: 40px 0;
}

.comment-item {
  margin-bottom: 24px;
}

.comment-item:last-child {
  margin-bottom: 0;
}

.detail-main-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.comments-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
  max-height: calc(90vh - 200px);
}

.comments-section {
  background: transparent;
  border-radius: 12px;
  padding: 20px;
  border: none;
  position: relative;
  padding-bottom: 80px;
}

.comments-section .comments-list {
  max-height: calc(90vh - 400px);
  overflow-y: auto;
  overflow-x: hidden;
  margin-top: 16px;
  padding-right: 8px;
}

/* 隐藏评论区域滚动条但保持滚动功能 */
.comments-section .comments-list::-webkit-scrollbar {
  width: 0;
  display: none;
}

.comments-section .comments-list {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.comment-footer-bar {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  z-index: 10;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: transparent;
  padding: 12px;
  border-radius: 12px;
  box-shadow: none;
  border: none;
}

.comment-footer-bar :deep(.el-button) {
  box-shadow: none !important;
}

.comment-footer-bar :deep(.el-button:hover) {
  box-shadow: none !important;
}

.comment-footer-bar :deep(.el-button:focus) {
  box-shadow: none !important;
}

.comment-footer-bar :deep(.el-button.is-disabled) {
  box-shadow: none !important;
}

.comment-input-wrapper {
  flex: 1;
  min-width: 0;
}

.comment-input-wrapper :deep(.el-textarea__inner) {
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  resize: none;
  box-shadow: none;
}

.comment-input-wrapper :deep(.el-textarea__inner):focus {
  border-color: #409eff;
  box-shadow: none;
  outline: none;
}

.comment-input-wrapper :deep(.el-textarea__inner:hover) {
  border-color: #c0c4cc;
  box-shadow: none;
}


.content-section {
  background: #fff;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #ff6b6b;
}

.ingredients-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.ingredients-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #333;
  background: #f8f9fa;
  border-radius: 6px;
}

.ingredients-list li .el-icon {
  color: #67c23a;
  font-size: 16px;
  flex-shrink: 0;
}

.steps-list {
  list-style: none;
  padding: 0;
  margin: 0;
  counter-reset: step-counter;
}

.steps-list li {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  counter-increment: step-counter;
}

.steps-list li:last-child {
  border-bottom: none;
}

.step-number {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: #ff6b6b;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.step-content {
  flex: 1;
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  padding-top: 2px;
}

@media (max-width: 1024px) {
  .detail-main-layout {
    grid-template-columns: 1fr;
  }

  .comments-sidebar {
    position: static;
    margin-top: 24px;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .note-modal-container {
    padding: 16px;
    max-width: 100%;
  }

  .note-modal-content {
    padding: 16px;
  }

  .detail-header {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .detail-image-wrapper {
    aspect-ratio: 4 / 3;
    height: 250px;
  }

  .detail-title {
    font-size: 22px;
  }

  .detail-description {
    font-size: 14px;
  }

  .section-title {
    font-size: 16px;
  }

  .ingredients-list {
    grid-template-columns: 1fr;
  }

  .ingredients-list li,
  .steps-list li {
    padding: 16px 0;
  }

  .step-content {
    font-size: 14px;
  }

  .detail-actions .el-button {
    width: 100%;
    min-height: 44px;
  }

  .comment-footer-bar {
    flex-direction: column;
    gap: 12px;
  }

  .comment-footer-bar .el-button {
    width: 100%;
    min-height: 44px;
  }
}

@media (max-width: 480px) {
  .note-modal-container {
    padding: 12px;
  }

  .note-modal-content {
    padding: 12px;
  }

  .detail-image-wrapper {
    height: 200px;
  }

  .detail-title {
    font-size: 20px;
  }

  .section-title {
    font-size: 15px;
  }
}
</style>

