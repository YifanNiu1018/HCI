<template>
  <div class="dishes-container">
    <div class="dishes-header">
      <h1>菜谱大全</h1>
      <p>探索各种美味菜谱</p>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <el-button
        :type="selectedCategory === 'all' ? 'primary' : ''"
        @click="selectCategory('all')"
        class="category-btn"
      >
        全部
      </el-button>
      <el-button
        v-for="category in categories"
        :key="category"
        :type="selectedCategory === category ? 'primary' : ''"
        @click="selectCategory(category)"
        class="category-btn"
      >
        {{ category }}
      </el-button>
    </div>

    <!-- 菜品列表 -->
    <div v-loading="dishesStore.loading" class="dishes-grid">
      <el-card
        v-for="dish in dishesStore.dishes"
        :key="'dish-' + dish.id"
        class="dish-card"
        shadow="hover"
        @click="goToDetail(dish.id)"
      >
        <div class="dish-image-wrapper">
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
        <div class="dish-info">
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

    <el-empty v-if="!dishesStore.loading && dishesStore.dishes.length === 0" description="暂无菜谱" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Clock, Star, StarFilled, User, TrendCharts } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const dishesStore = useDishesStore()
const userStore = useUserStore()
const favoriteLoading = ref<Record<number, boolean>>({})
const categories = ref<string[]>([])
const selectedCategory = ref<string>('all')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const totalPages = ref(0)
const allDishesData = ref<any[]>([])

onMounted(async () => {
  // 从路由参数初始化
  const categoryParam = route.query.category as string
  const pageParam = route.query.page as string
  
  if (categoryParam) {
    selectedCategory.value = categoryParam
  }
  if (pageParam) {
    currentPage.value = parseInt(pageParam) || 1
  }
  
  await loadCategories()
  await loadDishes()
})

watch(() => route.query.category, (newCategory) => {
  if (newCategory !== undefined) {
    selectedCategory.value = newCategory as string || 'all'
    currentPage.value = 1
    loadDishes()
  }
})

watch(() => route.query.page, (newPage) => {
  if (newPage) {
    currentPage.value = parseInt(newPage as string) || 1
    loadDishes()
  }
})

const loadCategories = async () => {
  try {
    categories.value = await dishesStore.fetchCategories()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadDishes = async () => {
  try {
    dishesStore.loading = true
    
    // 先获取所有数据（根据分类）
    let allDishes: any[] = []
    
    if (selectedCategory.value !== 'all') {
      // 获取指定分类的菜品
      const response = await api.get(`/dishes/category/${encodeURIComponent(selectedCategory.value)}`)
      allDishes = response.data
    } else {
      // 获取所有菜品
      const response = await api.get('/dishes')
      allDishes = Array.isArray(response.data) ? response.data : []
    }
    
    allDishesData.value = allDishes
    total.value = allDishes.length
    totalPages.value = Math.ceil(total.value / pageSize.value)
    
    // 前端分页
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    dishesStore.dishes = allDishes.slice(startIndex, endIndex)
  } catch (error) {
    console.error('加载菜谱失败:', error)
    dishesStore.dishes = []
    total.value = 0
    totalPages.value = 0
  } finally {
    dishesStore.loading = false
  }
}

const selectCategory = (category: string) => {
  selectedCategory.value = category
  currentPage.value = 1
  const query: any = { page: 1 }
  if (category !== 'all') {
    query.category = category
  }
  router.push({ query })
  loadDishes()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  router.push({ query: { ...route.query, page } })
  loadDishes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (dishId: number) => {
  router.push(`/dish/${dishId}`)
}

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

const handleToggleFavorite = async (dishId: number, isFavorite: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  favoriteLoading.value[dishId] = true
  try {
    const newFavoriteStatus = await dishesStore.toggleFavorite(dishId)
    const dish = dishesStore.dishes.find(d => d.id === dishId)
    if (dish) {
      dish.isFavorite = newFavoriteStatus
    }
    ElMessage.success(newFavoriteStatus ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value[dishId] = false
  }
}
</script>

<style scoped>
.dishes-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.dishes-header {
  text-align: center;
  margin-bottom: 32px;
}

.dishes-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.dishes-header p {
  font-size: 16px;
  color: #666;
}

.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-btn {
  border-radius: 20px;
  padding: 8px 20px;
}

.dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
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
  display: inline-flex;
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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

