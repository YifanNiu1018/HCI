<template>
  <div class="nutrition-container">
    <el-button class="back-button" @click="$router.push('/discover')" :icon="ArrowLeft">
      返回发现页
    </el-button>

    <div class="page-header">
      <div class="header-icon nutrition">
        <el-icon><TrendCharts /></el-icon>
      </div>
      <div class="header-content">
        <h1>营养信息查询</h1>
        <p>输入菜品名称，查看卡路里、蛋白质、脂肪等详细营养信息，并自动计算总和</p>
      </div>
    </div>

    <el-card class="main-card" shadow="hover">
      <div class="input-section">
        <el-input
          v-model="nutritionInput"
          type="textarea"
          :rows="4"
          placeholder="请输入菜品名称，多个菜品用逗号或换行分隔&#10;例如：宫保鸡丁, 番茄鸡蛋, 红烧肉"
          @keyup.enter.ctrl="handleGetNutrition"
        />
        <el-button
          type="primary"
          size="large"
          :loading="nutritionLoading"
          @click="handleGetNutrition"
          class="action-button"
        >
          查询营养信息
        </el-button>
      </div>

      <div v-if="nutritionResults.length > 0" class="result-section">
        <h3>营养信息</h3>
        
        <!-- 菜品营养列表 -->
        <div class="nutrition-dishes-list">
          <div
            v-for="(dish, index) in nutritionResults"
            :key="dish.id || index"
            class="nutrition-dish-item"
          >
            <div class="nutrition-dish-header">
              <img
                :src="getImageUrl(dish.image)"
                :alt="dish.name"
                class="nutrition-dish-image"
              />
              <div class="nutrition-dish-info">
                <h4 class="nutrition-dish-name">{{ dish.name }}</h4>
                <span class="nutrition-dish-servings" v-if="dish.servings">
                  {{ dish.servings }}人份
                </span>
              </div>
            </div>
            <div class="nutrition-values">
              <div class="nutrition-item">
                <span class="nutrition-label">卡路里</span>
                <span class="nutrition-value calories">{{ dish.calories || 0 }} 大卡</span>
              </div>
              <div class="nutrition-item">
                <span class="nutrition-label">蛋白质</span>
                <span class="nutrition-value protein">{{ dish.protein || 0 }}g</span>
              </div>
              <div class="nutrition-item">
                <span class="nutrition-label">脂肪</span>
                <span class="nutrition-value fat">{{ dish.fat || 0 }}g</span>
              </div>
              <div class="nutrition-item">
                <span class="nutrition-label">碳水化合物</span>
                <span class="nutrition-value carbs">{{ dish.carbohydrates || 0 }}g</span>
              </div>
              <div class="nutrition-item" v-if="dish.fiber">
                <span class="nutrition-label">膳食纤维</span>
                <span class="nutrition-value fiber">{{ dish.fiber }}g</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 营养总和 -->
        <div class="nutrition-total">
          <h3 class="total-title">
            <el-icon><DataAnalysis /></el-icon>
            营养总和
          </h3>
          <div class="total-values">
            <div class="total-item">
              <span class="total-label">总卡路里</span>
              <span class="total-value calories">{{ totalNutrition.calories }} 大卡</span>
            </div>
            <div class="total-item">
              <span class="total-label">总蛋白质</span>
              <span class="total-value protein">{{ totalNutrition.protein.toFixed(1) }}g</span>
            </div>
            <div class="total-item">
              <span class="total-label">总脂肪</span>
              <span class="total-value fat">{{ totalNutrition.fat.toFixed(1) }}g</span>
            </div>
            <div class="total-item">
              <span class="total-label">总碳水化合物</span>
              <span class="total-value carbs">{{ totalNutrition.carbohydrates.toFixed(1) }}g</span>
            </div>
            <div class="total-item" v-if="totalNutrition.fiber > 0">
              <span class="total-label">总膳食纤维</span>
              <span class="total-value fiber">{{ totalNutrition.fiber.toFixed(1) }}g</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { ElMessage } from 'element-plus'
import { ArrowLeft, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Dish } from '@/stores/dishes'

const router = useRouter()
const dishesStore = useDishesStore()

const nutritionInput = ref('')
const nutritionLoading = ref(false)
const nutritionResults = ref<Array<Dish & { calories?: number, protein?: number, fat?: number, carbohydrates?: number, fiber?: number }>>([])

const handleGetNutrition = async () => {
  if (!nutritionInput.value.trim()) {
    ElMessage.warning('请输入菜品名称')
    return
  }

  nutritionLoading.value = true
  try {
    const dishNames = nutritionInput.value
      .split(/[,\n]/)
      .map(name => name.trim())
      .filter(name => name.length > 0)

    if (dishNames.length === 0) {
      ElMessage.warning('请输入有效的菜品名称')
      return
    }

    await dishesStore.fetchDishes()
    
    const matchedDishes = dishesStore.dishes.filter(dish =>
      dishNames.some(name => dish.name.includes(name) || name.includes(dish.name))
    )

    if (matchedDishes.length === 0) {
      ElMessage.warning('未找到匹配的菜品，请检查输入')
      nutritionResults.value = []
      return
    }

    nutritionResults.value = matchedDishes
    ElMessage.success(`已找到 ${matchedDishes.length} 道菜品`)
  } catch (error) {
    ElMessage.error('查询营养信息失败')
    nutritionResults.value = []
  } finally {
    nutritionLoading.value = false
  }
}

const totalNutrition = computed(() => {
  const total = {
    calories: 0,
    protein: 0,
    fat: 0,
    carbohydrates: 0,
    fiber: 0
  }

  nutritionResults.value.forEach(dish => {
    total.calories += dish.calories || 0
    total.protein += dish.protein || 0
    total.fat += dish.fat || 0
    total.carbohydrates += dish.carbohydrates || 0
    total.fiber += dish.fiber || 0
  })

  return total
})
</script>

<style scoped>
.nutrition-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  padding: 32px;
  background: linear-gradient(135deg, #e6a23c 0%, #f0a020 100%);
  border-radius: 20px;
  color: #fff;
}

.header-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
}

.header-content h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
}

.header-content p {
  font-size: 16px;
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
}

.main-card {
  border-radius: 16px;
  overflow: hidden;
}

.input-section {
  margin-bottom: 32px;
}

.action-button {
  width: 100%;
  margin-top: 16px;
  height: 48px;
  font-size: 16px;
}

.result-section {
  margin-top: 32px;
}

.result-section h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 24px 0;
}

.nutrition-dishes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.nutrition-dish-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #409eff;
}

.nutrition-dish-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  align-items: center;
}

.nutrition-dish-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.nutrition-dish-info {
  flex: 1;
}

.nutrition-dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.nutrition-dish-servings {
  font-size: 12px;
  color: #999;
}

.nutrition-values {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.nutrition-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background: #ffffff;
  border-radius: 6px;
  text-align: center;
}

.nutrition-label {
  font-size: 12px;
  color: #666;
}

.nutrition-value {
  font-size: 16px;
  font-weight: 600;
}

.nutrition-value.calories {
  color: #f56c6c;
}

.nutrition-value.protein {
  color: #409eff;
}

.nutrition-value.fat {
  color: #e6a23c;
}

.nutrition-value.carbs {
  color: #67c23a;
}

.nutrition-value.fiber {
  color: #909399;
}

.nutrition-total {
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-radius: 12px;
  border: 2px solid #409eff;
}

.total-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  margin: 0 0 16px 0;
}

.total-title .el-icon {
  font-size: 20px;
}

.total-values {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.total-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #ffffff;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.total-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.total-value {
  font-size: 20px;
  font-weight: 700;
}

.total-value.calories {
  color: #f56c6c;
}

.total-value.protein {
  color: #409eff;
}

.total-value.fat {
  color: #e6a23c;
}

.total-value.carbs {
  color: #67c23a;
}

.total-value.fiber {
  color: #909399;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .nutrition-values {
    grid-template-columns: repeat(2, 1fr);
  }

  .total-values {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

