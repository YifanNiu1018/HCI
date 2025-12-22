<template>
  <div class="category-detail-container">
    <div class="category-header">
      <el-button class="back-button" @click="$router.back()" :icon="ArrowLeft">
        返回
      </el-button>
      <h1>{{ categoryName }}</h1>
      <p v-if="dishesStore.dishes.length > 0">共 {{ dishesStore.dishes.length }} 道菜品</p>
    </div>

    <div v-loading="dishesStore.loading" class="content-grid">
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
    </div>

    <el-empty
      v-if="!dishesStore.loading && dishesStore.dishes.length === 0"
      description="该分类下暂无菜品"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, Star, StarFilled, User, TrendCharts } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const dishesStore = useDishesStore()
const userStore = useUserStore()
const favoriteLoading = reactive<Record<number, boolean>>({})
const categoryName = ref('')

const loadCategoryDishes = async (category: string) => {
  categoryName.value = decodeURIComponent(category)
  await dishesStore.fetchDishesByCategory(categoryName.value)
}

onMounted(async () => {
  const category = route.params.category as string
  await loadCategoryDishes(category)
})

watch(
  () => route.params.category,
  async (newCategory) => {
    if (newCategory) {
      await loadCategoryDishes(newCategory as string)
    }
  }
)

const goToDetail = (dishId: number) => {
  router.push(`/dish/${dishId}`)
}

const handleToggleFavorite = async (dishId: number, isFavorite: boolean) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  favoriteLoading[dishId] = true
  try {
    await dishesStore.toggleFavorite(dishId)
    ElMessage.success(isFavorite ? '取消收藏成功' : '收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading[dishId] = false
  }
}

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    简单: 'success',
    中等: 'warning',
    困难: 'danger'
  }
  return map[difficulty] || 'info'
}
</script>

<style scoped>
.category-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.category-header {
  margin-bottom: 32px;
}

.back-button {
  margin-bottom: 16px;
}

.category-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.category-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.dish-card {
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.dish-image-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
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
  padding: 20px;
  background: #fff;
}

.dish-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 42px;
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
</style>

