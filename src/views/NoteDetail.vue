<template>
  <div v-loading="loading" class="detail-container">
    <el-button
      class="back-button"
      @click="$router.back()"
      :icon="ArrowLeft"
    >
      返回
    </el-button>

    <div v-if="note" class="detail-content">
      <div class="detail-header">
        <div class="detail-image-wrapper">
          <img
            :src="getImageUrl(note.image)"
            :alt="note.name"
            class="detail-image"
          />
        </div>
        <div class="detail-info">
          <h1 class="detail-title">{{ note.name }}</h1>
          <div class="note-author">
            <el-avatar :size="32" :src="note.author.avatar">
              {{ note.author.username?.charAt(0) }}
            </el-avatar>
            <span class="author-name">{{ note.author.username }}</span>
            <el-tag :type="note.isPublic ? 'success' : 'info'" size="small">
              {{ note.isPublic ? '公开' : '私密' }}
            </el-tag>
          </div>
          <p class="detail-description">{{ note.description }}</p>
          <div class="detail-meta">
            <el-tag size="large">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(note.createdAt) }}
            </el-tag>
            <el-tag
              v-for="tag in note.tags"
              :key="tag"
              size="large"
              style="margin-left: 8px"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="detail-sections">
        <el-card class="section-card">
          <template #header>
            <h3>
              <el-icon><ShoppingBag /></el-icon>
              所需食材
            </h3>
          </template>
          <ul class="ingredients-list">
            <li v-for="(ingredient, index) in note.ingredients" :key="index">
              <el-icon><Check /></el-icon>
              {{ ingredient }}
            </li>
          </ul>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <h3>
              <el-icon><Document /></el-icon>
              制作步骤
            </h3>
          </template>
          <ol class="steps-list">
            <li v-for="(step, index) in note.steps" :key="index">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">{{ step }}</div>
            </li>
          </ol>
        </el-card>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="笔记不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useNotesStore } from '@/stores/notes'
import { ArrowLeft, Calendar, ShoppingBag, Check, Document } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Note } from '@/stores/notes'
import api from '@/api'

const route = useRoute()
const notesStore = useNotesStore()
const note = ref<Note | null>(null)
const loading = ref(false)

onMounted(async () => {
  const noteId = parseInt(route.params.id as string)
  if (noteId) {
    await fetchNote(noteId)
  }
})

const fetchNote = async (id: number) => {
  loading.value = true
  try {
    // 先尝试从公开笔记中获取
    await notesStore.fetchPublicNotes()
    const publicNote = notesStore.publicNotes.find(n => n.id === id)
    if (publicNote) {
      note.value = publicNote
      return
    }
    
    // 如果不是公开的，尝试获取用户自己的笔记
    await notesStore.fetchMyNotes()
    const myNote = notesStore.notes.find(n => n.id === id)
    if (myNote) {
      note.value = myNote
      return
    }
    
    // 如果都找不到，尝试直接请求
    const response = await api.get(`/notes/${id}`)
    note.value = response.data
  } catch (error) {
    console.error('获取笔记详情失败:', error)
  } finally {
    loading.value = false
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
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
}

.back-button {
  margin-bottom: 24px;
}

.detail-content {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.detail-header {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.detail-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
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

.detail-title {
  font-size: 32px;
  color: #333;
  margin-bottom: 16px;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.author-name {
  font-size: 14px;
  color: #666;
}

.detail-description {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
  flex: 1;
}

.detail-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  border-radius: 12px;
}

.section-card :deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.section-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: #333;
  margin: 0;
}

.ingredients-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ingredients-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  color: #333;
}

.ingredients-list li:last-child {
  border-bottom: none;
}

.ingredients-list li .el-icon {
  color: #67c23a;
  font-size: 18px;
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
  border-bottom: 1px solid #f0f0f0;
  counter-increment: step-counter;
}

.steps-list li:last-child {
  border-bottom: none;
}

.step-number {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.step-content {
  flex: 1;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  padding-top: 4px;
}

@media (max-width: 768px) {
  .detail-header {
    grid-template-columns: 1fr;
  }

  .detail-image-wrapper {
    height: 300px;
  }
}
</style>

