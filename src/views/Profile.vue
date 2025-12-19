<template>
  <div class="profile-container">
    <el-button
      class="back-button"
      @click="$router.push('/')"
      :icon="ArrowLeft"
    >
      返回主页
    </el-button>
    
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <div class="user-info">
        <el-avatar :size="80" :src="userStore.user?.avatar" class="user-avatar">
          {{ userStore.user?.username?.charAt(0) }}
        </el-avatar>
        <div class="user-details">
          <h3>{{ userStore.user?.username }}</h3>
          <p>{{ userStore.user?.email }}</p>
        </div>
      </div>

      <!-- Tab切换栏 -->
      <div class="profile-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="我的收藏" name="favorites">
            <template #label>
              <span class="tab-label">
                <el-icon><Star /></el-icon>
                我的收藏
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="我的笔记" name="notes">
            <template #label>
              <span class="tab-label">
                <el-icon><Document /></el-icon>
                我的笔记
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 我的收藏 -->
    <el-card v-if="activeTab === 'favorites'" class="content-card">
      <div v-loading="loading" class="content-area">
        <div v-if="favorites.length > 0" class="items-grid">
          <el-card
            v-for="dish in favorites"
            :key="dish.id"
            class="item-card"
            shadow="hover"
            @click="goToDetail(dish.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(dish.image)" :alt="dish.name" class="item-image" />
              <div class="item-overlay">
                <el-button
                  type="danger"
                  :icon="StarFilled"
                  circle
                  size="small"
                  @click.stop="handleRemoveFavorite(dish.id)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ dish.name }}</h4>
              <p class="item-description">{{ dish.description }}</p>
              <div class="item-meta">
                <el-tag size="small">
                  <el-icon><Clock /></el-icon>
                  {{ dish.cookingTime }}分钟
                </el-tag>
                <el-tag size="small" :type="getDifficultyType(dish.difficulty)">
                  {{ dish.difficulty }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无收藏的菜品" />
      </div>
    </el-card>

    <!-- 我的笔记 -->
    <el-card v-if="activeTab === 'notes'" class="content-card">
      <template #header>
        <div class="notes-header">
          <h3>
            <el-icon><Document /></el-icon>
            我的笔记
          </h3>
          <el-button type="primary" :icon="Edit" @click="$router.push('/note/create')">
            发布笔记
          </el-button>
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
                <div class="visibility-switch">
                  <el-switch
                    v-model="note.isPublic"
                    active-text="公开"
                    inactive-text="私密"
                    @change="handleToggleVisibility(note.id, note.isPublic)"
                    @click.stop
                  />
                </div>
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click.stop="handleDeleteNote(note.id)"
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
        <el-empty v-else description="暂无笔记，快去发布你的做菜笔记吧！" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDishesStore } from '@/stores/dishes'
import { useNotesStore } from '@/stores/notes'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Clock, Document, Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'
import type { Dish } from '@/stores/dishes'
import type { Note } from '@/stores/notes'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const dishesStore = useDishesStore()
const notesStore = useNotesStore()
const favorites = ref<Dish[]>([])
const notes = ref<Note[]>([])
const loading = ref(false)
const notesLoading = ref(false)
const activeTab = ref<string>('favorites')

onMounted(async () => {
  // 根据路由参数设置初始tab
  const tab = route.query.tab as string
  if (tab === 'notes' || tab === 'favorites') {
    activeTab.value = tab
  }
  
  await loadFavorites()
  await loadNotes()
})

// 监听路由变化
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'notes' || newTab === 'favorites') {
    activeTab.value = newTab as string
  }
})

const handleTabChange = (tabName: string) => {
  // 更新URL参数但不刷新页面
  router.replace({ query: { ...route.query, tab: tabName } })
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const data = await dishesStore.fetchFavorites()
    favorites.value = data
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/dish/${id}`)
}

const handleRemoveFavorite = async (dishId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await dishesStore.toggleFavorite(dishId)
    favorites.value = favorites.value.filter(dish => dish.id !== dishId)
    ElMessage.success('取消收藏成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadNotes = async () => {
  notesLoading.value = true
  try {
    await notesStore.fetchMyNotes()
    notes.value = notesStore.notes
  } catch (error) {
    ElMessage.error('加载笔记列表失败')
  } finally {
    notesLoading.value = false
  }
}

const goToNoteDetail = (id: number) => {
  router.push(`/note/${id}`)
}

const handleToggleVisibility = async (noteId: number, isPublic: boolean) => {
  try {
    const result = await notesStore.updateNoteVisibility(noteId, isPublic)
    if (result.success) {
      ElMessage.success(isPublic ? '已设置为公开' : '已设置为私密')
      // 更新本地状态
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isPublic = isPublic
      }
    } else {
      // 如果失败，恢复原状态
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isPublic = !isPublic
      }
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    // 如果失败，恢复原状态
    const note = notes.value.find(n => n.id === noteId)
    if (note) {
      note.isPublic = !isPublic
    }
    ElMessage.error('操作失败')
  }
}

const handleDeleteNote = async (noteId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await notesStore.deleteNote(noteId)
    if (result.success) {
      notes.value = notes.value.filter(note => note.id !== noteId)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 0 24px;
}

.back-button {
  margin-bottom: 24px;
}

.profile-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  color: #333;
}

.profile-tabs {
  margin-top: 24px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.content-card {
  border-radius: 12px;
}

.content-area {
  min-height: 200px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.item-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.item-card.note-card {
  border-left: 3px solid #409eff;
}

.item-image-wrapper {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f5f5;
}

.item-image {
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
  top: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.visibility-switch {
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 8px;
  border-radius: 4px;
  backdrop-filter: blur(10px);
}

.item-info {
  padding: 12px;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.user-avatar {
  flex-shrink: 0;
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
</style>

