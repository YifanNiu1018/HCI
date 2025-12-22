<template>
  <div class="categories-container">
    <div class="categories-header">
      <h1>菜品分类</h1>
      <p>探索不同菜系的特色美食</p>
    </div>

    <div v-loading="loading" class="categories-grid">
      <el-card
        v-for="category in categories"
        :key="category"
        class="category-card"
        shadow="hover"
        @click="goToCategory(category)"
      >
        <div class="category-icon">
          <el-icon><Food /></el-icon>
        </div>
        <h3 class="category-name">{{ category }}</h3>
        <p class="category-count">{{ getCategoryCount(category) }} 道菜品</p>
      </el-card>
    </div>

    <el-empty v-if="!loading && categories.length === 0" description="暂无分类" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { Food } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const dishesStore = useDishesStore()
const categories = ref<string[]>([])
const loading = ref(false)
const categoryCounts = ref<Record<string, number>>({})

onMounted(async () => {
  loading.value = true
  try {
    categories.value = await dishesStore.fetchCategories()
    // 获取每个分类的菜品数量
    await Promise.all(
      categories.value.map(async (category) => {
        try {
          const response = await api.get(`/dishes/category/${encodeURIComponent(category)}`)
          categoryCounts.value[category] = response.data.length
        } catch (error) {
          categoryCounts.value[category] = 0
        }
      })
    )
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
})

const goToCategory = (category: string) => {
  router.push(`/category/${encodeURIComponent(category)}`)
}

const getCategoryCount = (category: string) => {
  return categoryCounts.value[category] || 0
}
</script>

<style scoped>
.categories-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.categories-header {
  text-align: center;
  margin-bottom: 48px;
}

.categories-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.categories-header p {
  font-size: 16px;
  color: #666;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 16px;
  text-align: center;
  padding: 32px 24px;
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.category-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32px;
}

.category-name {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.category-count {
  font-size: 14px;
  color: #999;
  margin: 0;
}
</style>

