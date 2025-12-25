<template>
  <div class="find-dishes-container">
    <el-button class="back-button" @click="$router.push('/discover')" :icon="ArrowLeft">
      返回发现页
    </el-button>

    <div class="page-header">
      <div class="header-icon find">
        <el-icon><Search /></el-icon>
      </div>
      <div class="header-content">
        <h1>根据食材找菜品</h1>
        <p>输入你已有的食材，系统会智能推荐可以制作的菜品，并提示哪些食材易过期</p>
      </div>
    </div>

    <el-card class="main-card" shadow="hover">
      <div class="input-section">
        <el-input
          v-model="ingredientsInput"
          type="textarea"
          :rows="4"
          placeholder="请输入你已有的食材，多个食材用逗号或换行分隔&#10;例如：鸡胸肉, 番茄, 鸡蛋, 葱, 姜, 蒜"
          @keyup.enter.ctrl="handleFindDishes"
        />
        <el-button
          type="primary"
          size="large"
          :loading="dishesLoading"
          @click="handleFindDishes"
          class="action-button"
        >
          查找菜品
        </el-button>
      </div>

      <!-- 易过期食材提示 -->
      <div v-if="perishableTips.veryPerishable.length > 0 || perishableTips.perishable.length > 0" class="perishable-tips">
        <h4 class="tips-title">
          <el-icon><Warning /></el-icon>
          易过期食材提醒
        </h4>
        <div class="tips-content">
          <div v-if="perishableTips.veryPerishable.length > 0" class="tip-item urgent">
            <span class="warning-label">极易过期：</span>
            <span class="warning-items">{{ perishableTips.veryPerishable.join('、') }}</span>
          </div>
          <div v-if="perishableTips.perishable.length > 0" class="tip-item normal">
            <span class="warning-label">易过期：</span>
            <span class="warning-items">{{ perishableTips.perishable.join('、') }}</span>
          </div>
          <p class="tip-suggestion">💡 建议优先选择使用这些食材的菜品，避免浪费！</p>
        </div>
      </div>

      <!-- 推荐菜品 -->
      <div v-if="matchedDishes.length > 0" class="result-section">
        <h3>推荐菜品</h3>
        <div class="dishes-grid">
          <el-card
            v-for="dish in matchedDishes"
            :key="dish.id"
            class="dish-card"
            shadow="hover"
          >
            <div class="dish-card-content">
              <div class="dish-image-wrapper" @click="goToDishDetail(dish.id)">
                <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />
              </div>
              <div class="dish-info-wrapper">
                <div class="dish-header">
                  <h4 @click="goToDishDetail(dish.id)" class="dish-title">{{ dish.name }}</h4>
                  <p class="dish-desc">{{ dish.description }}</p>
                </div>
                
                <div class="dish-ingredients-section">
                  <!-- 主要食材 -->
                  <div v-if="(dish as any).missingMain && (dish as any).missingMain.length > 0" class="ingredients-category">
                    <div class="category-header">
                      <el-icon class="category-icon"><Food /></el-icon>
                      <span class="category-name">主要食材</span>
                    </div>
                    <div class="ingredients-list">
                      <span
                        v-for="(item, index) in (dish as any).missingMain"
                        :key="'main-' + index"
                        class="ingredient-item"
                      >
                        {{ item }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 基础调料 -->
                  <div v-if="(dish as any).missingBasics && (dish as any).missingBasics.length > 0" class="ingredients-category">
                    <div class="category-header">
                      <el-icon class="category-icon"><Goods /></el-icon>
                      <span class="category-name">基础调料</span>
                      <span class="progress-info">
                        已有 {{ (dish as any).matchCount || 0 }} 种食材 (还需 {{ (dish as any).missingIngredients.length }} 种)
                      </span>
                    </div>
                    <div class="ingredients-list">
                      <span
                        v-for="(item, index) in (dish as any).missingBasics"
                        :key="'basic-' + index"
                        class="ingredient-item"
                      >
                        {{ item }}
                      </span>
                    </div>
                  </div>
                  
                  <!-- 食材齐全提示 -->
                  <div v-if="(dish as any).missingIngredients.length === 0" class="complete-notice">
                    ✅ 食材齐全，可以直接制作！
                  </div>
                  
                  <!-- 匹配度标签和操作按钮 -->
                  <div class="match-rate-badge">
                    <el-tag :type="getMatchRateType((dish as any).matchRate)" size="small">
                      匹配度 {{ Math.round((dish as any).matchRate * 100) }}%
                    </el-tag>
                    <el-button
                      type="primary"
                      size="small"
                      :icon="ShoppingCart"
                      @click.stop="goToShopByDish(dish.name)"
                      class="shop-button"
                    >
                      去购买食材
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, Warning, Food, Goods, ShoppingCart } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Dish } from '@/stores/dishes'

const router = useRouter()
const dishesStore = useDishesStore()

const ingredientsInput = ref('')
const dishesLoading = ref(false)
const matchedDishes = ref<Array<Dish & { missingIngredients: string[], matchRate: number }>>([])
const perishableTips = ref<{ veryPerishable: string[], perishable: string[] }>({
  veryPerishable: [],
  perishable: []
})

// 易过期食材分类
const perishableIngredients: Record<string, 'very' | 'normal'> = {
  '青菜': 'very', '菠菜': 'very', '生菜': 'very', '韭菜': 'very', '豆芽': 'very',
  '豆腐': 'very', '嫩豆腐': 'very', '鱼': 'very', '虾': 'very', '蟹': 'very',
  '贝': 'very', '扇贝': 'very', '鱿鱼': 'very', '带鱼': 'very', '草鱼': 'very',
  '鲈鱼': 'very', '牛奶': 'very', '酸奶': 'very',
  '番茄': 'normal', '黄瓜': 'normal', '茄子': 'normal', '青椒': 'normal',
  '胡萝卜': 'normal', '土豆': 'normal', '白菜': 'normal', '萝卜': 'normal',
  '鸡肉': 'normal', '鸡胸肉': 'normal', '鸡腿': 'normal', '猪肉': 'normal',
  '五花肉': 'normal', '里脊': 'normal', '排骨': 'normal'
}

// 基础调料关键词
const basicIngredientKeywords = [
  '盐', '糖', '油', '酱油', '生抽', '老抽', '醋', '料酒', '蚝油', '豆瓣酱',
  '辣椒', '花椒', '八角', '桂皮', '香叶', '姜', '蒜', '葱', '香菜', '胡椒粉',
  '五香粉', '孜然', '芝麻', '香油', '淀粉', '面粉', '水淀粉'
]

const isPerishable = (ingredient: string): 'very' | 'normal' | null => {
  for (const [key, type] of Object.entries(perishableIngredients)) {
    if (ingredient.includes(key)) {
      return type
    }
  }
  return null
}

const handleFindDishes = async () => {
  if (!ingredientsInput.value.trim()) {
    ElMessage.warning('请输入食材名称')
    return
  }

  dishesLoading.value = true
  try {
    const userIngredients = ingredientsInput.value
      .split(/[,\n]/)
      .map(ing => ing.trim())
      .filter(ing => ing.length > 0)

    // 检查易过期食材
    const veryPerishable: string[] = []
    const perishable: string[] = []
    userIngredients.forEach(ing => {
      const type = isPerishable(ing)
      if (type === 'very') {
        veryPerishable.push(ing)
      } else if (type === 'normal') {
        perishable.push(ing)
      }
    })
    perishableTips.value = { veryPerishable, perishable }

    await dishesStore.fetchDishes()

    const results: Array<Dish & { missingIngredients: string[], matchRate: number, matchCount: number }> = []

    dishesStore.dishes.forEach(dish => {
      const dishIngredients = dish.ingredients.map(ing => {
        const parts = ing.split(/\s+/)
        return parts.slice(0, -1).join(' ') || ing
      })

      const userIngredientNames = userIngredients.map(ing => ing.toLowerCase())
      let matchCount = 0
      const missing: string[] = []
      const missingMain: string[] = []
      const missingBasics: string[] = []
      let perishableScore = 0

      dishIngredients.forEach(ingredientName => {
        const hasIngredient = userIngredientNames.some(userIng =>
          ingredientName.toLowerCase().includes(userIng.toLowerCase()) ||
          userIng.toLowerCase().includes(ingredientName.toLowerCase()) ||
          ingredientName.split(/\s+/).some(part => userIngredientNames.includes(part.toLowerCase()))
        )
        if (hasIngredient) {
          matchCount++
          // 计算易过期食材得分
          const type = isPerishable(ingredientName)
          if (type === 'very') {
            perishableScore += 3
          } else if (type === 'normal') {
            perishableScore += 1
          }
        } else {
          missing.push(ingredientName)
          // 分类缺少的食材
          const isBasic = basicIngredientKeywords.some(keyword => ingredientName.includes(keyword))
          if (isBasic) {
            missingBasics.push(ingredientName)
          } else {
            missingMain.push(ingredientName)
          }
        }
      })

      if (matchCount > 0) {
        const matchRate = matchCount / dishIngredients.length
        results.push({
          ...dish,
          missingIngredients: missing,
          missingMain,
          missingBasics,
          matchRate,
          matchCount,
          perishableScore
        } as any)
      }
    })

    // 按匹配度排序：优先使用易过期食材的菜品 > 匹配数量 > 匹配率 > 缺少食材数量
    results.sort((a, b) => {
      const aScore = (a as any).perishableScore || 0
      const bScore = (b as any).perishableScore || 0
      if (bScore !== aScore) {
        return bScore - aScore // 使用易过期食材多的在前
      }
      if (b.matchCount !== a.matchCount) {
        return b.matchCount - a.matchCount // 匹配数量多的在前
      }
      if (Math.abs(a.matchRate - b.matchRate) > 0.01) {
        return b.matchRate - a.matchRate // 匹配率高的在前
      }
      return a.missingIngredients.length - b.missingIngredients.length // 缺少少的在前
    })

    matchedDishes.value = results.slice(0, 15) // 显示更多推荐（15个），提供更多灵感

    if (results.length === 0) {
      ElMessage.info('未找到匹配的菜品，请尝试添加更多食材')
    } else {
      ElMessage.success(`找到 ${results.length} 道相关菜品`)
    }
  } catch (error) {
    ElMessage.error('查找菜品失败')
    matchedDishes.value = []
  } finally {
    dishesLoading.value = false
  }
}

const getMatchRateType = (matchRate: number): string => {
  if (matchRate >= 0.8) return 'success'
  if (matchRate >= 0.5) return 'warning'
  return 'info'
}

const goToDishDetail = (id: number) => {
  router.push(`/dish/${id}`)
}

const goToShopByDish = (dishName: string) => {
  router.push({
    path: '/discover/shop-by-dish',
    query: { dish: dishName }
  })
}
</script>

<style scoped>
.find-dishes-container {
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
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
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

.perishable-tips {
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff9e6 100%);
  border-radius: 12px;
  border: 1px solid #ffe0e0;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #e6a23c;
  margin: 0 0 16px 0;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 4px solid;
}

.tip-item.urgent {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.tip-item.normal {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.warning-label {
  font-weight: 600;
  font-size: 15px;
  display: block;
  margin-bottom: 6px;
}

.tip-item.urgent .warning-label {
  color: #f56c6c;
}

.tip-item.normal .warning-label {
  color: #e6a23c;
}

.warning-items {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.tip-suggestion {
  margin: 12px 0 0 0;
  font-size: 14px;
  color: #666;
  font-style: italic;
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

.dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 24px;
}

.dish-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
}

.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.dish-card-content {
  display: flex;
  gap: 20px;
  padding: 0;
}

.dish-image-wrapper {
  flex-shrink: 0;
  width: 200px;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
  background: #f5f5f5;
}

.dish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.dish-card:hover .dish-image {
  transform: scale(1.05);
}

.dish-info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 16px 16px 0;
  min-width: 0;
}

.dish-header {
  margin-bottom: 16px;
}

.dish-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  cursor: pointer;
  transition: color 0.2s ease;
}

.dish-title:hover {
  color: #409eff;
}

.dish-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-ingredients-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ingredients-category {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.category-icon {
  font-size: 16px;
  color: #409eff;
}

.category-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.progress-info {
  font-size: 12px;
  color: #999;
  margin-left: auto;
  font-style: italic;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding-left: 24px;
  padding-top: 4px;
}

.ingredient-item {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  line-height: 1.5;
  padding: 8px 14px;
  background: #ffffff;
  border: 2px solid #d0d0d0;
  border-radius: 8px;
  margin: 0;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: 32px;
}

.ingredient-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -5px;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 0;
  background: transparent;
}

.ingredient-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(64, 158, 255, 0.15);
  color: #409eff;
}

/* 主要食材样式 */
.ingredients-category:first-of-type .ingredient-item {
  border-color: #d0d0d0;
  background: #ffffff;
}

.ingredients-category:first-of-type .ingredient-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
  color: #409eff;
}

/* 基础调料样式 */
.ingredients-category:last-of-type .ingredient-item {
  border-color: #e6a23c;
  background: #fffbf0;
}

.ingredients-category:last-of-type .ingredient-item:hover {
  border-color: #e6a23c;
  background: #fffbf0;
  color: #e6a23c;
}

.complete-notice {
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #67c23a;
}

.match-rate-badge {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.shop-button {
  margin-left: auto;
}

@media (max-width: 768px) {
  .dishes-grid {
    grid-template-columns: 1fr;
  }

  .dish-card-content {
    flex-direction: column;
  }

  .dish-image-wrapper {
    width: 100%;
    height: 200px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .dishes-grid {
    grid-template-columns: 1fr;
  }
}
</style>

