<template>
  <div class="home-container">
    <div class="banner">
      <h1>探索美食世界</h1>
      <p>发现美味，分享快乐</p>
    </div>

    <div class="search-section" v-if="!searchKeyword">
      <el-input
        v-model="localSearchKeyword"
        placeholder="搜索菜品、食材..."
        size="large"
        class="home-search"
        @keyup.enter="handleSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="searchKeyword" class="search-result-header">
      <h2>搜索结果：{{ searchKeyword }}</h2>
      <el-button text @click="clearSearch">返回首页</el-button>
    </div>

    <div v-loading="dishesStore.loading || notesStore.loading" class="content-grid">
      <!-- 系统菜品 -->
      <el-card
        v-for="dish in dishesStore.dishes"
        :key="'dish-' + dish.id"
        class="dish-card"
        shadow="hover"
        @click="goToDetail(dish.id)"
      >
        <div class="dish-image-wrapper">
          <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />
          <div class="dish-overlay">
            <el-tag :type="getDifficultyType(dish.difficulty)" class="difficulty-tag">
              {{ dish.difficulty }}
            </el-tag>
            <el-tag class="time-tag">
              <el-icon><Clock /></el-icon>
              {{ dish.cookingTime }}分钟
            </el-tag>
          </div>
        </div>
        <div class="dish-info">
          <h3 class="dish-name">{{ dish.name }}</h3>
          <p class="dish-description">{{ dish.description }}</p>
          <div class="dish-meta">
            <span class="dish-category">{{ dish.category }}</span>
            <span class="dish-servings">{{ dish.servings }}人份</span>
          </div>
        </div>
      </el-card>

      <!-- 用户公开笔记 -->
      <el-card
        v-for="note in notesStore.publicNotes"
        :key="'note-' + note.id"
        class="dish-card note-card"
        shadow="hover"
        @click="goToNoteDetail(note.id)"
      >
        <div class="dish-image-wrapper">
          <img :src="getImageUrl(note.image)" :alt="note.name" class="dish-image" />
          <div class="dish-overlay">
            <el-tag type="info" class="difficulty-tag">用户笔记</el-tag>
          </div>
        </div>
        <div class="dish-info">
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

    <el-empty v-if="!dishesStore.loading && !notesStore.loading && dishesStore.dishes.length === 0 && notesStore.publicNotes.length === 0" description="暂无内容" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { useNotesStore } from '@/stores/notes'
import { Search, Clock } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const dishesStore = useDishesStore()
const notesStore = useNotesStore()
const searchKeyword = ref<string>((route.query.keyword as string) || '')
const localSearchKeyword = ref('')

onMounted(() => {
  if (searchKeyword.value) {
    localSearchKeyword.value = searchKeyword.value
    dishesStore.fetchDishes(searchKeyword.value)
    notesStore.fetchPublicNotes()
  } else {
    dishesStore.fetchDishes()
    notesStore.fetchPublicNotes()
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  searchKeyword.value = (newKeyword as string) || ''
  localSearchKeyword.value = searchKeyword.value
  if (searchKeyword.value) {
    dishesStore.fetchDishes(searchKeyword.value)
    notesStore.fetchPublicNotes()
  } else {
    dishesStore.fetchDishes()
    notesStore.fetchPublicNotes()
  }
})

const handleSearch = () => {
  if (localSearchKeyword.value.trim()) {
    router.push({ query: { keyword: localSearchKeyword.value } })
  } else {
    clearSearch()
  }
}

const clearSearch = () => {
  localSearchKeyword.value = ''
  router.push({ query: {} })
  dishesStore.fetchDishes()
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
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 64px);
  width: 100%;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 60px 20px;
  text-align: center;
  width: 100%;
  margin: 0 0 32px 0;
}

.banner h1 {
  font-size: 36px;
  margin-bottom: 12px;
}

.banner p {
  font-size: 18px;
  opacity: 0.9;
}

.search-section {
  margin-bottom: 32px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 24px;
}

.home-search {
  max-width: 600px;
  margin: 0 auto;
}

.search-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 24px;
}

.search-result-header h2 {
  font-size: 20px;
  color: #333;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
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
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.dish-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
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

.dish-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.difficulty-tag,
.time-tag {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
}

.dish-info {
  padding: 16px;
}

.dish-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
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
  font-size: 12px;
  color: #999;
}

.dish-category {
  padding: 4px 8px;
  background: #f0f0f0;
  border-radius: 4px;
}
</style>

