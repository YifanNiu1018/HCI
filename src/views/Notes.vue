<template>
  <div class="notes-container">
    <div class="notes-header">
      <h1>美食笔记</h1>
      <p>分享你的美食故事</p>
    </div>

    <!-- 笔记瀑布流 -->
    <div v-loading="notesStore.loading" class="notes-masonry">
      <div
        v-for="note in notesStore.publicNotes"
        :key="'note-' + note.id"
        class="note-item"
        @click="goToNoteDetail(note.id)"
      >
        <div class="note-image-wrapper">
          <img :src="getImageUrl(note.image)" :alt="note.name" class="note-image" />
          <div class="note-overlay">
            <el-button
              v-if="userStore.isLoggedIn"
              :icon="note.isFavorite ? StarFilled : Star"
              circle
              :class="['note-favorite-btn', { 'is-favorite': note.isFavorite }]"
              size="small"
              @click.stop="handleToggleFavorite(note.id, note.isFavorite || false)"
              :loading="noteFavoriteLoading[note.id]"
            />
          </div>
        </div>
        <div class="note-content">
          <h3 class="note-title">{{ note.name }}</h3>
          <p class="note-description">{{ note.description }}</p>
          <div class="note-footer">
            <div class="note-author">
              <el-avatar :size="24" :src="note.author.avatar" class="author-avatar">
                {{ note.author.username?.charAt(0) }}
              </el-avatar>
              <span class="author-name">{{ note.author.username }}</span>
            </div>
            <div v-if="note.tags && note.tags.length > 0" class="note-tags">
              <el-tag
                v-for="tag in note.tags.slice(0, 3)"
                :key="tag"
                size="small"
                class="note-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="totalPages > 1">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>

    <el-empty v-if="!notesStore.loading && notesStore.publicNotes.length === 0" description="暂无笔记" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotesStore } from '@/stores/notes'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const notesStore = useNotesStore()
const userStore = useUserStore()
const noteFavoriteLoading = ref<Record<number, boolean>>({})
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const totalPages = ref(0)

onMounted(async () => {
  await loadNotes()
})

watch(() => route.query.page, (newPage) => {
  if (newPage) {
    currentPage.value = parseInt(newPage as string) || 1
    loadNotes()
  }
})

const loadNotes = async () => {
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await api.get('/notes/public', { params })
    if (response.data.content) {
      // 分页响应
      notesStore.publicNotes = response.data.content
      total.value = response.data.totalElements
      totalPages.value = response.data.totalPages
    } else {
      // 普通列表响应
      notesStore.publicNotes = response.data
      total.value = response.data.length
      totalPages.value = Math.ceil(total.value / pageSize.value)
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  router.push({ query: { ...route.query, page } })
  loadNotes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToNoteDetail = (noteId: number) => {
  router.push(`/note/${noteId}`)
}

const handleToggleFavorite = async (noteId: number, isFavorite: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  noteFavoriteLoading.value[noteId] = true
  try {
    const result = await notesStore.toggleFavorite(noteId)
    if (result.success) {
      ElMessage.success(result.isFavorite ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    noteFavoriteLoading.value[noteId] = false
  }
}
</script>

<style scoped>
.notes-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.notes-header {
  text-align: center;
  margin-bottom: 32px;
}

.notes-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.notes-header p {
  font-size: 16px;
  color: #666;
}

/* 网格布局 - 一行4个 */
.notes-masonry {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

@media (max-width: 1200px) {
  .notes-masonry {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .notes-masonry {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .notes-masonry {
    grid-template-columns: 1fr;
  }
}

.note-item {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.note-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.note-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f5f5f5;
}

.note-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.note-item:hover .note-image {
  transform: scale(1.05);
}

.note-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.note-favorite-btn {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.note-favorite-btn:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: scale(1.1);
}

.note-favorite-btn.is-favorite {
  background: rgba(255, 107, 107, 0.9);
  color: #fff;
}

.note-content {
  padding: 16px;
}

.note-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.note-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  flex-shrink: 0;
}

.author-name {
  font-size: 12px;
  color: #999;
}

.note-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.note-tag {
  font-size: 11px;
  padding: 2px 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

