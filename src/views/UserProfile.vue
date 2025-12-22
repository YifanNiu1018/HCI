<template>
  <div class="user-profile-container">
    <el-button
      class="back-button"
      @click="$router.back()"
      :icon="ArrowLeft"
    >
      返回
    </el-button>
    
    <div v-loading="loading" class="profile-content">
      <el-card v-if="user" class="profile-card">
        <template #header>
          <div class="profile-header">
            <h2>{{ user.username }} 的个人主页</h2>
          </div>
        </template>

        <div class="user-info">
          <el-avatar :size="80" :src="getImageUrl(user.avatar || '')" class="user-avatar">
            {{ user.username?.charAt(0) }}
          </el-avatar>
          <div class="user-details">
            <h3>{{ user.username }}</h3>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ user.followingCount || 0 }}</div>
              <div class="stat-label">关注</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ user.followersCount || 0 }}</div>
              <div class="stat-label">粉丝</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ notes.length }}</div>
              <div class="stat-label">笔记</div>
            </div>
          </div>
          <div class="user-actions" v-if="userStore.isLoggedIn && userStore.user?.id !== user.id">
            <el-button
              :type="user.isFollowing ? 'info' : 'primary'"
              @click="handleToggleFollow"
              :loading="followLoading"
            >
              {{ user.isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-button
              type="success"
              @click="goToMessages"
            >
              <el-icon><Message /></el-icon>
              发私信
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 公开笔记 -->
      <el-card class="content-card">
        <template #header>
          <div class="notes-header">
            <h3>
              <el-icon><Document /></el-icon>
              公开笔记 ({{ notes.length }})
            </h3>
          </div>
        </template>

        <div v-loading="notesLoading" class="content-area">
          <div v-if="notes.length > 0" class="items-grid">
            <el-card
              v-for="note in notes"
              :key="note.id"
              class="item-card note-card"
              shadow="hover"
              @click="goToNoteDetail(note.id)"
            >
              <div class="item-image-wrapper">
                <img :src="getImageUrl(note.image)" :alt="note.name" class="item-image" />
                <div class="item-overlay">
                  <el-button
                    v-if="userStore.isLoggedIn"
                    :type="note.isFavorite ? 'danger' : 'default'"
                    :icon="note.isFavorite ? StarFilled : Star"
                    circle
                    size="small"
                    @click.stop="handleToggleFavorite(note.id, note.isFavorite || false)"
                    :loading="favoriteLoading[note.id]"
                  />
                </div>
              </div>
              <div class="item-info">
                <h4 class="item-name">{{ note.name }}</h4>
                <p class="item-description">{{ note.description }}</p>
                <div class="item-meta">
                  <span class="note-date">{{ formatDate(note.createdAt) }}</span>
                  <div v-if="note.tags && note.tags.length > 0" class="note-tags">
                    <el-tag
                      v-for="tag in note.tags.slice(0, 3)"
                      :key="tag"
                      size="small"
                      style="margin-right: 4px"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
          <el-empty v-else description="该用户还没有发布公开笔记" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotesStore } from '@/stores/notes'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, Star, StarFilled, Message } from '@element-plus/icons-vue'
import type { User } from '@/stores/user'
import type { Note } from '@/stores/notes'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notesStore = useNotesStore()

const user = ref<User | null>(null)
const notes = ref<Note[]>([])
const loading = ref(false)
const notesLoading = ref(false)
const followLoading = ref(false)
const favoriteLoading = ref<Record<number, boolean>>({})

onMounted(async () => {
  const userId = parseInt(route.params.userId as string)
  if (userId) {
    await loadUserProfile(userId)
    await loadUserNotes(userId)
  }
})

const loadUserProfile = async (userId: number) => {
  loading.value = true
  try {
    const response = await api.get(`/user/${userId}`)
    user.value = response.data
  } catch (error: any) {
    ElMessage.error('加载用户信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const loadUserNotes = async (userId: number) => {
  notesLoading.value = true
  try {
    const response = await api.get(`/user/${userId}/notes`)
    notes.value = response.data
  } catch (error) {
    console.error('加载用户笔记失败:', error)
    notes.value = []
  } finally {
    notesLoading.value = false
  }
}

const handleToggleFollow = async () => {
  if (!user.value) return
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  followLoading.value = true
  try {
    if (user.value.isFollowing) {
      await api.delete(`/follow/${user.value.id}`)
      user.value.isFollowing = false
      user.value.followersCount = (user.value.followersCount || 0) - 1
      ElMessage.success('取消关注成功')
    } else {
      await api.post(`/follow/${user.value.id}`)
      user.value.isFollowing = true
      user.value.followersCount = (user.value.followersCount || 0) + 1
      ElMessage.success('关注成功')
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.response?.data?.error || '操作失败'
    ElMessage.error(errorMessage)
  } finally {
    followLoading.value = false
  }
}

const handleToggleFavorite = async (noteId: number, isFavorite: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  favoriteLoading.value[noteId] = true
  try {
    const result = await notesStore.toggleFavorite(noteId)
    if (result.success) {
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isFavorite = result.isFavorite
      }
      ElMessage.success(result.isFavorite ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value[noteId] = false
  }
}

const goToNoteDetail = (noteId: number) => {
  router.push(`/note/${noteId}`)
}

const goToMessages = () => {
  if (!user.value) return
  router.push(`/messages?userId=${user.value.id}`)
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  margin-bottom: 0;
}

.profile-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
  justify-content: space-between;
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.user-details h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.user-details p {
  color: #666;
  font-size: 14px;
}

.user-stats {
  display: flex;
  gap: 32px;
  align-items: center;
  padding-left: 32px;
  border-left: 1px solid #e0e0e0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: default;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.user-actions {
  margin-left: auto;
}

.content-card {
  margin-top: 0;
}

.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notes-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  color: #333;
}

.content-area {
  min-height: 200px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.item-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
  background: #f5f5f5;
}

.item-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.item-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.item-info {
  padding: 16px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-description {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
  flex-wrap: wrap;
  gap: 8px;
}

.note-date {
  font-size: 12px;
  color: #999;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 16px;
  }

  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .user-stats {
    border-left: none;
    border-top: 1px solid #e0e0e0;
    padding-left: 0;
    padding-top: 16px;
    width: 100%;
    gap: 24px;
  }

  .user-actions {
    margin-left: 0;
    width: 100%;
  }

  .items-grid {
    grid-template-columns: 1fr;
  }
}
</style>

