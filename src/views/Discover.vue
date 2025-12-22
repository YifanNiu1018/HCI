<template>
  <div class="discover-container">
    <div class="discover-header">
      <h1>发现</h1>
      <p>智能匹配，让做菜更简单</p>
    </div>

    <div class="discover-cards">
      <!-- 根据菜品买菜 -->
      <el-card class="discover-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon"><ShoppingCart /></el-icon>
            <h2>根据菜品买菜</h2>
          </div>
        </template>
        <div class="card-content">
          <p class="card-description">输入你想要做的菜品，系统会为你整理出需要购买的食材清单</p>
          
          <div class="input-section">
            <el-input
              v-model="dishInput"
              type="textarea"
              :rows="3"
              placeholder="请输入菜品名称，多个菜品用逗号或换行分隔&#10;例如：宫保鸡丁, 番茄鸡蛋"
              @keyup.enter.ctrl="handleGetIngredients"
            />
            <el-button
              type="primary"
              size="large"
              :loading="ingredientsLoading"
              @click="handleGetIngredients"
              class="action-button"
            >
              生成购物清单
            </el-button>
          </div>

          <div v-if="categorizedIngredients.basics.length > 0 || categorizedIngredients.main.length > 0" class="result-section">
            <h3>购物清单</h3>
            
            <!-- 主要食材 -->
            <div v-if="categorizedIngredients.main.length > 0" class="ingredient-category">
              <h4 class="category-title">
                <el-icon><Food /></el-icon>
                主要食材
                <span class="category-count">
                  ({{ getUnselectedCount(categorizedIngredients.main) }}/{{ categorizedIngredients.main.length }} 待购买)
                </span>
              </h4>
              <div class="ingredients-list">
                <div
                  v-for="(ingredient, index) in categorizedIngredients.main"
                  :key="'main-' + index"
                  class="ingredient-item"
                  :class="{ 'ingredient-selected': isIngredientSelected(ingredient) }"
                  @click="toggleIngredient(ingredient)"
                >
                  <el-checkbox
                    :model-value="isIngredientSelected(ingredient)"
                    @change="() => toggleIngredient(ingredient)"
                    @click.stop
                  />
                  <el-tag
                    class="ingredient-tag main-ingredient"
                    size="large"
                  >
                    {{ ingredient }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 基础调料 -->
            <div v-if="categorizedIngredients.basics.length > 0" class="ingredient-category">
              <h4 class="category-title">
                <el-icon><Goods /></el-icon>
                基础调料
                <span class="category-count">
                  ({{ getUnselectedCount(categorizedIngredients.basics) }}/{{ categorizedIngredients.basics.length }} 待购买)
                </span>
              </h4>
              <div class="ingredients-list">
                <div
                  v-for="(ingredient, index) in categorizedIngredients.basics"
                  :key="'basic-' + index"
                  class="ingredient-item"
                  :class="{ 'ingredient-selected': isIngredientSelected(ingredient) }"
                  @click="toggleIngredient(ingredient)"
                >
                  <el-checkbox
                    :model-value="isIngredientSelected(ingredient)"
                    @change="() => toggleIngredient(ingredient)"
                    @click.stop
                  />
                  <el-tag
                    class="ingredient-tag basic-ingredient"
                    size="large"
                  >
                    {{ ingredient }}
                  </el-tag>
                </div>
              </div>
            </div>

            <el-button
              text
              @click="copyIngredientsList"
              class="copy-button"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制清单
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 根据食材找菜品 -->
      <el-card class="discover-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon"><Search /></el-icon>
            <h2>根据食材找菜品</h2>
          </div>
        </template>
        <div class="card-content">
          <p class="card-description">输入你已有的食材，系统会推荐可以做的菜品，并告诉你还需要准备什么</p>
          
          <div class="input-section">
            <el-input
              v-model="ingredientsInput"
              type="textarea"
              :rows="3"
              placeholder="请输入已有食材，多个食材用逗号或换行分隔&#10;例如：鸡蛋, 番茄, 葱"
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

          <div v-if="matchedDishes.length > 0" class="result-section">
            <h3>推荐菜品</h3>
            <div class="dishes-list">
              <el-card
                v-for="dish in matchedDishes"
                :key="dish.id"
                class="dish-result-card"
                shadow="hover"
                @click="goToDishDetail(dish.id)"
              >
                <div class="dish-result-content">
                  <div class="dish-result-image">
                    <img :src="getImageUrl(dish.image)" :alt="dish.name" />
                  </div>
                  <div class="dish-result-info">
                    <h4>{{ dish.name }}</h4>
                    <p class="dish-result-desc">{{ dish.description }}</p>
                    <div class="dish-result-missing">
                      <div class="match-info">
                        <el-tag type="success" size="small" style="margin-right: 8px">
                          已有 {{ (dish as any).matchCount || 0 }} 种食材
                        </el-tag>
                        <span v-if="dish.missingIngredients.length > 0" class="missing-count">
                          (还需 {{ dish.missingIngredients.length }} 种)
                        </span>
                      </div>
                      
                      <div v-if="dish.missingIngredients.length === 0" class="no-missing">
                        食材齐全，可以直接制作！
                      </div>
                      
                      <div v-else class="missing-ingredients-list">
                        <!-- 主要食材 -->
                        <div v-if="(dish as any).missingMain && (dish as any).missingMain.length > 0" class="missing-category">
                          <span class="missing-category-label">
                            <el-icon><Food /></el-icon>
                            主要食材：
                          </span>
                          <div class="missing-items">
                            <div
                              v-for="(item, index) in (dish as any).missingMain"
                              :key="'main-' + index"
                              class="missing-item"
                            >
                              {{ item }}
                            </div>
                          </div>
                        </div>
                        
                        <!-- 基础调料 -->
                        <div v-if="(dish as any).missingBasics && (dish as any).missingBasics.length > 0" class="missing-category">
                          <span class="missing-category-label">
                            <el-icon><Goods /></el-icon>
                            基础调料：
                          </span>
                          <div class="missing-items">
                            <div
                              v-for="(item, index) in (dish as any).missingBasics"
                              :key="'basic-' + index"
                              class="missing-item"
                            >
                              {{ item }}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Search, DocumentCopy, Food, Goods } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import type { Dish } from '@/stores/dishes'

const router = useRouter()
const dishesStore = useDishesStore()

const dishInput = ref('')
const ingredientsInput = ref('')
const categorizedIngredients = ref<{
  basics: string[]
  main: string[]
}>({
  basics: [],
  main: []
})
const selectedIngredients = ref<Set<string>>(new Set())
const matchedDishes = ref<Array<Dish & { missingIngredients: string[], matchRate: number }>>([])
const ingredientsLoading = ref(false)
const dishesLoading = ref(false)

// 基础调料关键词
const basicIngredientKeywords = [
  '盐', '糖', '醋', '油', '生抽', '老抽', '酱油', '料酒', '蚝油', '香油',
  '味精', '鸡精', '胡椒粉', '花椒', '八角', '桂皮', '香叶', '干辣椒',
  '豆瓣酱', '番茄酱', '甜面酱', '芝麻', '淀粉', '面粉', '生粉', '水'
]

const handleGetIngredients = async () => {
  if (!dishInput.value.trim()) {
    ElMessage.warning('请输入菜品名称')
    return
  }

  ingredientsLoading.value = true
  try {
    // 解析输入的菜品名称
    const dishNames = dishInput.value
      .split(/[,\n]/)
      .map(name => name.trim())
      .filter(name => name.length > 0)

    if (dishNames.length === 0) {
      ElMessage.warning('请输入有效的菜品名称')
      return
    }

    // 获取所有菜品
    await dishesStore.fetchDishes()
    
    // 查找匹配的菜品
    const matchedDishes = dishesStore.dishes.filter(dish =>
      dishNames.some(name => dish.name.includes(name) || name.includes(dish.name))
    )

    if (matchedDishes.length === 0) {
      ElMessage.warning('未找到匹配的菜品，请检查输入')
      categorizedIngredients.value = {
        basics: [],
        main: []
      }
      selectedIngredients.value = new Set()
      return
    }

    // 合并所有食材
    const allIngredients = new Set<string>()
    matchedDishes.forEach(dish => {
      dish.ingredients.forEach(ingredient => {
        // 提取食材名称（去除数量）
        const ingredientName = ingredient.split(/\s+/).slice(0, -1).join(' ') || ingredient
        if (ingredientName) {
          allIngredients.add(ingredientName)
        }
      })
    })

    // 分类食材
    const basics: string[] = []
    const main: string[] = []
    
    Array.from(allIngredients).forEach(ingredient => {
      const isBasic = basicIngredientKeywords.some(keyword => 
        ingredient.includes(keyword) || keyword.includes(ingredient)
      )
      
      if (isBasic) {
        basics.push(ingredient)
      } else {
        main.push(ingredient)
      }
    })

    categorizedIngredients.value = {
      basics: basics.sort(),
      main: main.sort()
    }
    
    // 重置选择状态
    selectedIngredients.value = new Set()
    
    const totalCount = basics.length + main.length
    ElMessage.success(`已找到 ${matchedDishes.length} 道菜品，共需要 ${totalCount} 种食材`)
  } catch (error) {
    ElMessage.error('获取食材清单失败')
  } finally {
    ingredientsLoading.value = false
  }
}

const handleFindDishes = async () => {
  if (!ingredientsInput.value.trim()) {
    ElMessage.warning('请输入已有食材')
    return
  }

  dishesLoading.value = true
  try {
    // 解析输入的食材
    const userIngredients = ingredientsInput.value
      .split(/[,\n]/)
      .map(ing => ing.trim().toLowerCase())
      .filter(ing => ing.length > 0)

    if (userIngredients.length === 0) {
      ElMessage.warning('请输入有效的食材名称')
      return
    }

    // 获取所有菜品
    await dishesStore.fetchDishes()
    
    // 匹配菜品 - 更宽松的条件，只要有部分食材匹配就推荐
    const results: Array<Dish & { 
      missingIngredients: string[], 
      missingBasics: string[],
      missingMain: string[],
      matchCount: number, 
      matchRate: number 
    }> = []
    
    dishesStore.dishes.forEach(dish => {
      const missingIngredients: string[] = []
      const missingBasics: string[] = []
      const missingMain: string[] = []
      let matchedCount = 0
      
      dish.ingredients.forEach(ingredient => {
        // 提取食材名称（去除数量）
        const ingredientParts = ingredient.split(/\s+/)
        const ingredientName = ingredientParts.slice(0, -1).join(' ').toLowerCase() || ingredient.toLowerCase()
        const ingredientFull = ingredient // 保留完整格式（包含数量）
        
        // 检查用户是否有这个食材
        const hasIngredient = userIngredients.some(userIng =>
          ingredientName.includes(userIng) || userIng.includes(ingredientName) ||
          ingredientName.split(/\s+/).some(part => userIngredients.includes(part))
        )
        
        if (hasIngredient) {
          matchedCount++
        } else if (ingredientName) {
          missingIngredients.push(ingredientFull)
          
          // 分类缺少的食材
          const isBasic = basicIngredientKeywords.some(keyword => 
            ingredientName.includes(keyword) || keyword.includes(ingredientName)
          )
          
          if (isBasic) {
            missingBasics.push(ingredientFull)
          } else {
            missingMain.push(ingredientFull)
          }
        }
      })

      // 计算匹配率
      const matchRate = dish.ingredients.length > 0 ? matchedCount / dish.ingredients.length : 0
      
      // 只要有至少1个食材匹配就推荐（更宽松的条件，提供做菜灵感）
      if (matchedCount > 0) {
        results.push({
          ...dish,
          missingIngredients: missingIngredients,
          missingBasics: missingBasics.sort(),
          missingMain: missingMain.sort(),
          matchCount: matchedCount,
          matchRate: matchRate
        })
      }
    })

    // 按匹配度排序：先按匹配数量（从多到少），再按匹配率（从高到低），最后按缺少食材数量（从少到多）
    results.sort((a, b) => {
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
      ElMessage.success(`找到 ${results.length} 道相关菜品，为您提供做菜灵感`)
    }
  } catch (error) {
    ElMessage.error('查找菜品失败')
  } finally {
    dishesLoading.value = false
  }
}

const isIngredientSelected = (ingredient: string) => {
  return selectedIngredients.value.has(ingredient)
}

const toggleIngredient = (ingredient: string) => {
  if (selectedIngredients.value.has(ingredient)) {
    selectedIngredients.value.delete(ingredient)
  } else {
    selectedIngredients.value.add(ingredient)
  }
  // 触发响应式更新
  selectedIngredients.value = new Set(selectedIngredients.value)
}

const getUnselectedCount = (ingredients: string[]) => {
  return ingredients.filter(ing => !selectedIngredients.value.has(ing)).length
}

const copyIngredientsList = () => {
  let text = ''
  
  // 只复制未选中的食材
  const unselectedMain = categorizedIngredients.value.main.filter(
    ing => !selectedIngredients.value.has(ing)
  )
  const unselectedBasics = categorizedIngredients.value.basics.filter(
    ing => !selectedIngredients.value.has(ing)
  )
  
  if (unselectedMain.length > 0) {
    text += '主要食材：\n'
    text += unselectedMain.join('\n')
    text += '\n\n'
  }
  
  if (unselectedBasics.length > 0) {
    text += '基础调料：\n'
    text += unselectedBasics.join('\n')
  }
  
  if (!text.trim()) {
    ElMessage.info('所有食材都已选中，无需购买')
    return
  }
  
  navigator.clipboard.writeText(text).then(() => {
    const totalCount = unselectedMain.length + unselectedBasics.length
    ElMessage.success(`已复制 ${totalCount} 种待购买食材到剪贴板`)
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const getMatchType = (matchRate: number) => {
  if (matchRate >= 0.8) return 'success'
  if (matchRate >= 0.5) return 'warning'
  return 'info'
}

const goToDishDetail = (id: number) => {
  router.push(`/dish/${id}`)
}
</script>

<style scoped>
.discover-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.discover-header {
  text-align: center;
  margin-bottom: 56px;
  padding: 40px 0;
}

.discover-header h1 {
  font-size: 42px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.discover-header p {
  font-size: 20px;
  color: #666;
  font-weight: 300;
}

.discover-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 32px;
}

.discover-card {
  border-radius: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}

.discover-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
  border-color: #e0e0e0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  font-size: 28px;
  color: #409eff;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.card-content {
  padding: 8px 0;
}

.card-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.input-section {
  margin-bottom: 24px;
}

.input-section :deep(.el-textarea__inner) {
  margin-bottom: 16px;
}

.action-button {
  width: 100%;
}

.result-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.result-section h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.ingredient-category {
  margin-bottom: 24px;
}

.ingredient-category:last-of-type {
  margin-bottom: 16px;
}

.category-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 600;
}

.category-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  transition: all 0.3s;
}

.ingredient-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.ingredient-item.ingredient-selected {
  opacity: 0.6;
}

.ingredient-item.ingredient-selected .ingredient-tag {
  text-decoration: line-through;
  opacity: 0.7;
}

.ingredient-tag {
  font-size: 14px;
  padding: 8px 16px;
  transition: all 0.3s;
}

.main-ingredient {
  background-color: #f0f9ff;
  border-color: #409eff;
  color: #409eff;
}

.basic-ingredient {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.category-count {
  font-size: 14px;
  font-weight: normal;
  color: #666;
  margin-left: 8px;
}

.copy-button {
  margin-top: 16px;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dish-result-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
}

.dish-result-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dish-result-content {
  display: flex;
  gap: 16px;
}

.dish-result-image {
  width: 120px;
  height: 90px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.dish-result-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-result-info {
  flex: 1;
}

.dish-result-info h4 {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.dish-result-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-result-missing {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.match-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.missing-count {
  font-size: 12px;
  color: #999;
  font-style: italic;
  margin-right: 8px;
}

.no-missing {
  font-size: 13px;
  color: #67c23a;
  font-weight: 500;
  padding: 8px 0;
}

.missing-ingredients-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.missing-category {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.missing-category-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  font-weight: 600;
}

.missing-category-label .el-icon {
  font-size: 14px;
  color: #409eff;
}

.missing-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-left: 20px;
}

.missing-item {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.missing-item:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .discover-cards {
    grid-template-columns: 1fr;
  }
}
</style>

