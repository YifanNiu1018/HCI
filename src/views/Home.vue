<template>
  <div class="home-container">
    <div class="banner">
      <h1>探索美食世界</h1>
      <p>发现美味，分享快乐</p>
    </div>

    <div v-if="searchKeyword" class="search-results">
      <div class="search-result-header">
        <h2>搜索结果：{{ searchKeyword }}</h2>
        <el-button text @click="clearSearch">返回首页</el-button>
      </div>
      
      <!-- 搜索结果列表 -->
      <div v-loading="dishesStore.loading || notesStore.loading" class="content-grid">
        <!-- 搜索到的菜品 -->
        <el-card
          v-for="dish in dishesStore.dishes"
          :key="'dish-' + dish.id"
          class="dish-card"
          shadow="hover"
        >
          <div class="dish-image-wrapper" @click="goToDetail(dish.id)">
            <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />
            <div class="favorite-button-wrapper">
              <el-button
                v-if="userStore.isLoggedIn"
                :icon="dish.isFavorite ? StarFilled : Star"
                circle
                :class="['favorite-button', { 'is-favorite': dish.isFavorite }]"
                size="small"
                @click.stop="handleToggleFavorite(dish.id, dish.isFavorite || false)"
                :loading="favoriteLoading[dish.id]"
              />
            </div>
          </div>
          <div class="dish-info" @click="goToDetail(dish.id)">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <p class="dish-description">{{ dish.description }}</p>
            <div class="dish-meta">
              <div class="dish-meta-left">
                <el-tag :type="getDifficultyType(dish.difficulty)" size="small" class="meta-tag">
                  <el-icon><TrendCharts /></el-icon>
                  {{ dish.difficulty }}
                </el-tag>
                <el-tag size="small" class="meta-tag">
                  <el-icon><Clock /></el-icon>
                  {{ dish.cookingTime }}分钟
                </el-tag>
                <el-tag size="small" class="meta-tag">
                  <el-icon><User /></el-icon>
                  {{ dish.servings }}人份
                </el-tag>
              </div>
              <span class="dish-category">{{ dish.category }}</span>
            </div>
          </div>
        </el-card>

        <!-- 搜索到的笔记 -->
        <el-card
          v-for="note in notesStore.publicNotes"
          :key="'note-' + note.id"
          class="dish-card note-card"
          shadow="hover"
        >
          <div class="dish-image-wrapper" @click="goToNoteDetail(note.id)">
            <img :src="getImageUrl(note.image)" :alt="note.name" class="dish-image" />
            <div class="favorite-button-wrapper">
              <el-button
                v-if="userStore.isLoggedIn"
                :icon="note.isFavorite ? StarFilled : Star"
                circle
                :class="['favorite-button', { 'is-favorite': note.isFavorite }]"
                size="small"
                @click.stop="handleToggleNoteFavorite(note.id, note.isFavorite || false)"
                :loading="noteFavoriteLoading[note.id]"
              />
            </div>
          </div>
          <div class="dish-info" @click="goToNoteDetail(note.id)">
            <h3 class="dish-name">{{ note.name }}</h3>
            <p class="dish-description">{{ note.description }}</p>
            <div class="dish-meta">
              <span class="dish-category">by {{ note.author.username }}</span>
              <div v-if="note.tags && note.tags.length > 0" class="note-tags">
                <el-tag
                  v-for="tag in note.tags.slice(0, 2)"
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

      <el-empty 
        v-if="!dishesStore.loading && !notesStore.loading && dishesStore.dishes.length === 0 && notesStore.publicNotes.length === 0" 
        description="没有找到相关结果" 
      />
    </div>

    <div v-else class="home-sections">
      <!-- 热门搜索 - 放在内容区域顶部，紧凑设计 -->
      <div class="hot-search-section">
        <span class="hot-search-label">热门搜索：</span>
        <div class="hot-search-tags">
          <span
            v-for="tag in hotSearchTags"
            :key="tag"
            class="hot-search-tag"
            @click="handleHotSearch(tag)"
          >
            {{ tag }}
          </span>
        </div>
      </div>
      <!-- 菜谱推荐 -->
      <div class="section-container">
        <div class="section-header">
          <h2>推荐菜谱</h2>
          <el-button type="primary" :icon="Refresh" @click="refreshDishes">换一批</el-button>
        </div>
        <div v-loading="dishesStore.loading" class="content-grid dishes-grid">
          <el-card
            v-for="dish in displayedDishes"
            :key="'dish-' + dish.id"
            class="dish-card"
            shadow="hover"
          >
            <div class="dish-image-wrapper" @click="goToDetail(dish.id)">
              <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />
              <div class="favorite-button-wrapper">
                <el-button
                  v-if="userStore.isLoggedIn"
                  :icon="dish.isFavorite ? StarFilled : Star"
                  circle
                  :class="['favorite-button', { 'is-favorite': dish.isFavorite }]"
                  size="small"
                  @click.stop="handleToggleFavorite(dish.id, dish.isFavorite || false)"
                  :loading="favoriteLoading[dish.id]"
                />
              </div>
            </div>
            <div class="dish-info" @click="goToDetail(dish.id)">
              <h3 class="dish-name">{{ dish.name }}</h3>
              <p class="dish-description">{{ dish.description }}</p>
              <div class="dish-meta">
                <div class="dish-meta-left">
                  <el-tag :type="getDifficultyType(dish.difficulty)" size="small" class="meta-tag">
                    <el-icon><TrendCharts /></el-icon>
                    {{ dish.difficulty }}
                  </el-tag>
                  <el-tag size="small" class="meta-tag">
                    <el-icon><Clock /></el-icon>
                    {{ dish.cookingTime }}分钟
                  </el-tag>
                  <el-tag size="small" class="meta-tag">
                    <el-icon><User /></el-icon>
                    {{ dish.servings }}人份
                  </el-tag>
                </div>
                <span class="dish-category">{{ dish.category }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 笔记推荐 -->
      <div class="section-container">
        <div class="section-header">
          <h2>推荐笔记</h2>
          <el-button type="primary" :icon="Refresh" @click="refreshNotes">换一批</el-button>
        </div>
        <div v-loading="notesStore.loading" class="content-grid notes-grid">
          <el-card
            v-for="note in displayedNotes"
            :key="'note-' + note.id"
            class="dish-card note-card"
            shadow="hover"
          >
            <div class="dish-image-wrapper" @click="goToNoteDetail(note.id)">
              <img :src="getImageUrl(note.image)" :alt="note.name" class="dish-image" />
              <div class="favorite-button-wrapper">
                <el-button
                  v-if="userStore.isLoggedIn"
                  :icon="note.isFavorite ? StarFilled : Star"
                  circle
                  :class="['favorite-button', { 'is-favorite': note.isFavorite }]"
                  size="small"
                  @click.stop="handleToggleNoteFavorite(note.id, note.isFavorite || false)"
                  :loading="noteFavoriteLoading[note.id]"
                />
              </div>
            </div>
            <div class="dish-info" @click="goToNoteDetail(note.id)">
              <h3 class="dish-name">{{ note.name }}</h3>
              <p class="dish-description">{{ note.description }}</p>
              <div class="dish-meta">
                <span class="dish-category">by {{ note.author.username }}</span>
                <div v-if="note.tags && note.tags.length > 0" class="note-tags">
                  <el-tag
                    v-for="tag in note.tags.slice(0, 2)"
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { useNotesStore } from '@/stores/notes'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Clock, Star, StarFilled, User, TrendCharts, Refresh } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const dishesStore = useDishesStore()
const notesStore = useNotesStore()
const userStore = useUserStore()
const searchKeyword = ref<string>((route.query.keyword as string) || '')
const favoriteLoading = reactive<Record<number, boolean>>({})
const noteFavoriteLoading = reactive<Record<number, boolean>>({})
const allDishes = ref<any[]>([])
const allNotes = ref<any[]>([])
const displayedDishes = ref<any[]>([])
const displayedNotes = ref<any[]>([])

// 热门搜索标签
const hotSearchTags = ref<string[]>([
  '川菜', '粤菜', '家常菜', '海鲜', '红烧肉', '宫保鸡丁', '麻婆豆腐', '糖醋排骨'
])

// 随机选择最多8个（两排，每排4个）
const MAX_DISPLAY = 8

// 随机打乱数组
function shuffleArray<T>(array: T[]): T[] {
  const shuffled = [...array]
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = shuffled[i]!
    shuffled[i] = shuffled[j]!
    shuffled[j] = temp!
  }
  return shuffled
}

// 随机选择显示的菜品
function selectRandomDishes() {
  if (allDishes.value.length === 0) return
  const shuffled = shuffleArray(allDishes.value)
  displayedDishes.value = shuffled.slice(0, Math.min(MAX_DISPLAY, shuffled.length))
}

// 随机选择显示的笔记
function selectRandomNotes() {
  if (allNotes.value.length === 0) return
  const shuffled = shuffleArray(allNotes.value)
  displayedNotes.value = shuffled.slice(0, Math.min(MAX_DISPLAY, shuffled.length))
}

// 刷新菜品
function refreshDishes() {
  selectRandomDishes()
}

// 刷新笔记
function refreshNotes() {
  selectRandomNotes()
}

onMounted(async () => {
  if (searchKeyword.value) {
    await dishesStore.fetchDishes(searchKeyword.value)
    await notesStore.fetchPublicNotes(searchKeyword.value)
    allDishes.value = dishesStore.dishes
    allNotes.value = notesStore.publicNotes
  } else {
    await dishesStore.fetchDishes()
    await notesStore.fetchPublicNotes()
    allDishes.value = dishesStore.dishes
    allNotes.value = notesStore.publicNotes
    selectRandomDishes()
    selectRandomNotes()
  }
})

watch(() => route.query.keyword, async (newKeyword) => {
  searchKeyword.value = (newKeyword as string) || ''
  if (searchKeyword.value) {
    await dishesStore.fetchDishes(searchKeyword.value)
    await notesStore.fetchPublicNotes(searchKeyword.value)
    allDishes.value = dishesStore.dishes
    allNotes.value = notesStore.publicNotes
  } else {
    await dishesStore.fetchDishes()
    await notesStore.fetchPublicNotes()
    allDishes.value = dishesStore.dishes
    allNotes.value = notesStore.publicNotes
    selectRandomDishes()
    selectRandomNotes()
  }
})

const clearSearch = () => {
  router.push({ query: {} })
  dishesStore.fetchDishes()
}

const handleHotSearch = (tag: string) => {
  router.push({ name: 'Home', query: { keyword: tag } })
}

const goToDetail = (id: number) => {
  router.push(`/dish/${id}`)
}

const goToNoteDetail = (id: number) => {
  router.push(`/note/${id}`)
}

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

const handleToggleFavorite = async (dishId: number, currentFavoriteStatus: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  favoriteLoading[dishId] = true
  try {
    const newFavoriteStatus = await dishesStore.toggleFavorite(dishId)
    // 确保状态正确更新
    const dish = dishesStore.dishes.find(d => d.id === dishId)
    if (dish) {
      dish.isFavorite = newFavoriteStatus
    }
    ElMessage.success(newFavoriteStatus ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading[dishId] = false
  }
}

const handleToggleNoteFavorite = async (noteId: number, currentFavoriteStatus: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  noteFavoriteLoading[noteId] = true
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
    noteFavoriteLoading[noteId] = false
  }
}
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 64px);
  width: 100%;
}

.banner {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 50%, #ff6b6b 100%);
  background-size: 200% 200%;
  animation: gradientShift 8s ease infinite;
  color: #fff;
  padding: 80px 20px;
  text-align: center;
  width: 100%;
  margin: 0 0 40px 0;
  position: relative;
  overflow: hidden;
}

.banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><circle cx="50" cy="50" r="2" fill="rgba(255,255,255,0.1)"/></svg>');
  opacity: 0.3;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.banner h1 {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.banner p {
  font-size: 20px;
  opacity: 0.95;
  font-weight: 300;
  position: relative;
  z-index: 1;
}

.search-results {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.search-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0;
}

.search-result-header h2 {
  font-size: 20px;
  color: #333;
}

.home-sections {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 热门搜索 - 紧凑设计，放在内容区域顶部 */
.hot-search-section {
  margin-bottom: 32px;
  padding: 16px 24px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hot-search-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.hot-search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.hot-search-tag {
  cursor: pointer;
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  transition: all 0.2s;
  user-select: none;
  white-space: nowrap;
}

.hot-search-tag:hover {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
}

.section-container {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.note-card {
  border-left: 3px solid #409eff;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.dish-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  background: #fff;
}

.dish-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: #e0e0e0;
}

.dish-image-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
}

.dish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.dish-card:hover .dish-image {
  transform: scale(1.05);
}

.favorite-button-wrapper {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.favorite-button {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.favorite-button:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: scale(1.1);
}

.favorite-button.is-favorite {
  background: rgba(255, 107, 107, 0.95);
  color: #fff;
}

.favorite-button.is-favorite:hover {
  background: rgba(255, 107, 107, 1);
}

.dish-info {
  padding: 16px;
  background: #fff;
}

.dish-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.3px;
}

.dish-description {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.dish-meta-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.meta-tag {
  display: inflex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  white-space: nowrap;
}

.meta-tag .el-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.dish-category {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}
:deep(.meta-tag) {
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
}
:deep(.el-card__body) {
  padding: 0;
}
:deep(.meta-tag .el-tag__content) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>

