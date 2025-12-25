<template>
  <div class="shop-by-dish-container">
    <el-button class="back-button" @click="$router.push('/discover')" :icon="ArrowLeft">
      返回发现页
    </el-button>

    <div class="page-header">
      <div class="header-icon shop">
        <el-icon><ShoppingCart /></el-icon>
      </div>
      <div class="header-content">
        <h1>根据菜品买菜</h1>
        <p>输入你想要做的菜品，系统会为你整理出需要购买的食材清单，并按超市区域分类</p>
      </div>
    </div>

    <el-card class="main-card" shadow="hover">
      <div class="input-section">
        <el-input
          v-model="dishInput"
          type="textarea"
          :rows="4"
          placeholder="请输入菜品名称，多个菜品用逗号或换行分隔&#10;例如：宫保鸡丁, 番茄鸡蛋, 红烧肉"
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
        <div class="result-header">
          <h3>购物清单</h3>
          <div class="header-actions">
            <el-button
              type="success"
              :icon="DocumentCopy"
              @click="copyIngredientsList"
            >
              复制清单
            </el-button>
            <el-button
              type="primary"
              :icon="Picture"
              @click="showShoppingCard = true"
            >
              生成购物卡片
            </el-button>
          </div>
        </div>

        <!-- 购物提示：按区域购买 -->
        <div class="shopping-tips-section" v-if="Object.keys(ingredientsByZone).length > 0">
          <h4 class="tips-title">
            <el-icon><Goods /></el-icon>
            购物提示：按区域购买
          </h4>
          <div class="zones-grid">
            <div
              v-for="(ingredients, zone) in ingredientsByZone"
              :key="zone"
              class="zone-card"
            >
              <div class="zone-title">{{ zoneNames[zone] || zone }}</div>
              <div class="zone-items">
                <el-tag
                  v-for="(ing, index) in ingredients"
                  :key="'zone-' + zone + '-' + index"
                  :type="getZoneTagType(zone)"
                  size="small"
                  effect="dark"
                  style="margin-right: 8px; margin-bottom: 8px;"
                >
                  {{ ing }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 主要食材和基础调料 -->
        <div class="ingredients-categorized">
          <!-- 提示横幅 -->
          <div class="selection-tip-banner">
            <el-icon class="tip-icon"><InfoFilled /></el-icon>
            <span class="tip-text">💡 点击食材标签可标记为"已有"，已标记的食材将不会出现在购物卡片中</span>
          </div>

          <div v-if="categorizedIngredients.main.length > 0" class="category-section">
            <h4 class="category-title">
              <el-icon><Food /></el-icon>
              主要食材
              <span class="category-hint">
                <el-icon><Pointer /></el-icon>
                点击标记为已有
              </span>
            </h4>
            <div class="ingredients-list">
              <el-tag
                v-for="(ing, index) in categorizedIngredients.main"
                :key="'main-' + index"
                :class="['ingredient-tag', { 'selected': isIngredientSelected(ing), 'clickable': true }]"
                :type="isIngredientSelected(ing) ? 'info' : 'success'"
                :effect="isIngredientSelected(ing) ? 'plain' : 'dark'"
                size="default"
                @click="toggleIngredient(ing)"
              >
                <transition name="check-icon">
                  <el-icon v-if="isIngredientSelected(ing)" class="check-icon"><Check /></el-icon>
                </transition>
                <span class="ingredient-text">{{ ing }}</span>
              </el-tag>
            </div>
          </div>

          <div v-if="categorizedIngredients.basics.length > 0" class="category-section">
            <h4 class="category-title">
              <el-icon><Goods /></el-icon>
              基础调料
              <span class="category-hint">
                <el-icon><Pointer /></el-icon>
                点击标记为已有
              </span>
            </h4>
            <div class="ingredients-list">
              <el-tag
                v-for="(ing, index) in categorizedIngredients.basics"
                :key="'basic-' + index"
                :class="['ingredient-tag', { 'selected': isIngredientSelected(ing), 'clickable': true }]"
                :type="isIngredientSelected(ing) ? 'info' : 'warning'"
                :effect="isIngredientSelected(ing) ? 'plain' : 'dark'"
                size="default"
                @click="toggleIngredient(ing)"
              >
                <transition name="check-icon">
                  <el-icon v-if="isIngredientSelected(ing)" class="check-icon"><Check /></el-icon>
                </transition>
                <span class="ingredient-text">{{ ing }}</span>
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 购物卡片对话框 -->
    <el-dialog
      v-model="showShoppingCard"
      title="购物卡片"
      width="500px"
      class="shopping-card-dialog"
    >
      <div ref="shoppingCardRef" class="shopping-card" v-if="categorizedIngredients.basics.length > 0 || categorizedIngredients.main.length > 0">
        <div class="card-header-section">
          <h2 class="card-title">🛒 购物清单</h2>
        </div>

        <div class="card-section" v-if="Object.keys(getShoppingIngredientsByZoneForCard()).length > 0">
          <div class="shopping-zones">
            <div
              v-for="(ingredients, zone) in getShoppingIngredientsByZoneForCard()"
              :key="zone"
              class="zone-card"
            >
              <div class="zone-title">{{ zoneNames[zone] || zone }}</div>
              <div class="zone-items">
                <span
                  v-for="(ing, index) in ingredients"
                  :key="'shop-' + zone + '-' + index"
                  class="zone-item"
                >
                  {{ ing }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 如果没有按区域分类的，显示主要食材和基础调料 -->
        <template v-else>
          <!-- 主要食材（未选中的） -->
          <div class="card-section" v-if="getUnselectedMainIngredients().length > 0">
            <div class="zone-card">
              <div class="zone-title">🥬 主要食材</div>
              <div class="zone-items">
                <span
                  v-for="(ing, index) in getUnselectedMainIngredients()"
                  :key="'main-' + index"
                  class="zone-item"
                >
                  {{ ing }}
                </span>
              </div>
            </div>
          </div>

          <!-- 基础调料（未选中的） -->
          <div class="card-section" v-if="getUnselectedBasicIngredients().length > 0">
            <div class="zone-card">
              <div class="zone-title">🧂 基础调料</div>
              <div class="zone-items">
                <span
                  v-for="(ing, index) in getUnselectedBasicIngredients()"
                  :key="'basic-' + index"
                  class="zone-item"
                >
                  {{ ing }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <div class="card-footer">
          <p class="footer-text">{{ new Date().toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showShoppingCard = false">关闭</el-button>
          <el-button type="primary" :icon="Download" @click="saveShoppingCardAsImage">
            保存为图片
          </el-button>
          <el-button type="success" :icon="Share" @click="shareShoppingCard">
            分享卡片
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart, Food, Goods, Picture, Download, Share, Check, InfoFilled, Pointer, DocumentCopy } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()
const dishesStore = useDishesStore()

const dishInput = ref('')
const ingredientsLoading = ref(false)
const categorizedIngredients = ref<{
  basics: string[]
  main: string[]
}>({
  basics: [],
  main: []
})
const ingredientsByZone = ref<Record<string, string[]>>({})
const showShoppingCard = ref(false)
const shoppingCardRef = ref<HTMLElement | null>(null)

// 基础调料关键词
const basicIngredientKeywords = [
  '盐', '糖', '油', '酱油', '生抽', '老抽', '醋', '料酒', '蚝油', '豆瓣酱',
  '辣椒', '花椒', '八角', '桂皮', '香叶', '姜', '蒜', '葱', '香菜', '胡椒粉',
  '五香粉', '孜然', '芝麻', '香油', '淀粉', '面粉', '水淀粉'
]

// 区域名称映射
const zoneNames: Record<string, string> = {
  'meat': '🥩 肉类区',
  'seafood': '🐟 海鲜区',
  'vegetable': '🥬 蔬菜区',
  'fruit': '🍎 水果区',
  'dairy': '🥛 乳制品区',
  'grain': '🌾 主食区',
  'seasoning': '🧂 调料区',
  'frozen': '❄️ 冷冻区',
  'other': '📦 其他'
}

// 食材区域分类
const ingredientZones: Record<string, string[]> = {
  'meat': ['鸡', '鸭', '鹅', '猪', '牛', '羊', '肉', '排骨', '里脊', '五花', '腿', '翅', '爪'],
  'seafood': ['鱼', '虾', '蟹', '贝', '鱿鱼', '带鱼', '鲈鱼', '草鱼', '扇贝', '海', '水产品'],
  'vegetable': ['菜', '白菜', '青菜', '萝卜', '土豆', '番茄', '茄子', '豆角', '黄瓜', '胡萝卜', '青椒', '木耳', '豆芽', '豆腐'],
  'fruit': ['苹果', '梨', '橙', '香蕉', '葡萄', '草莓', '西瓜'],
  'dairy': ['牛奶', '酸奶', '奶酪', '黄油'],
  'grain': ['米', '面', '粉', '粉丝', '面条', '馒头', '包子'],
  'seasoning': ['盐', '糖', '油', '酱油', '生抽', '老抽', '醋', '料酒', '蚝油', '豆瓣酱', '辣椒', '花椒', '八角', '桂皮', '香叶', '姜', '蒜', '葱', '香菜', '胡椒粉', '五香粉', '孜然', '芝麻', '香油', '淀粉', '面粉', '水淀粉', '甜面酱', '豆豉', '泡椒', '干辣椒'],
  'frozen': ['速冻', '冷冻', '冰'],
  'other': []
}

const getIngredientZone = (ingredient: string): string | null => {
  const ing = ingredient.toLowerCase()
  for (const [zone, keywords] of Object.entries(ingredientZones)) {
    if (keywords.some(keyword => ing.includes(keyword.toLowerCase()))) {
      return zone
    }
  }
  return 'other'
}

const getZoneTagType = (zone: string): string => {
  const typeMap: Record<string, string> = {
    'meat': 'danger',
    'seafood': 'primary',
    'vegetable': 'success',
    'fruit': 'warning',
    'dairy': 'info',
    'grain': '',
    'seasoning': 'info',
    'frozen': '',
    'other': ''
  }
  return typeMap[zone] || ''
}

const handleGetIngredients = async () => {
  if (!dishInput.value.trim()) {
    ElMessage.warning('请输入菜品名称')
    return
  }

  ingredientsLoading.value = true
  try {
    const dishNames = dishInput.value
      .split(/[,\n，、\s]+/)
      .map(name => name.trim())
      .filter(name => name.length > 0)

    if (dishNames.length === 0) {
      ElMessage.warning('请输入有效的菜品名称')
      return
    }

    await dishesStore.fetchDishes()
    
    const allIngredients = new Set<string>()
    const dishList: any[] = []
    const unmatchedDishes: string[] = []

    for (const dishName of dishNames) {
      // 改进匹配逻辑：更灵活的匹配方式
      const inputNameLower = dishName.toLowerCase().trim()
      let matchedDish = dishesStore.dishes.find(dish => {
        const dishNameLower = dish.name.toLowerCase().trim()
        
        // 完全匹配（去除空格）
        const dishNameNoSpace = dishNameLower.replace(/\s+/g, '')
        const inputNameNoSpace = inputNameLower.replace(/\s+/g, '')
        if (dishNameNoSpace === inputNameNoSpace) return true
        
        // 完全匹配（保留空格）
        if (dishNameLower === inputNameLower) return true
        
        // 双向包含匹配（至少2个字符）
        if (inputNameLower.length >= 2) {
          if (dishNameLower.includes(inputNameLower)) return true
        }
        if (dishNameLower.length >= 2) {
          if (inputNameLower.includes(dishNameLower)) return true
        }
        
        return false
      })

      if (matchedDish) {
        // 避免重复添加同一个菜品
        if (!dishList.find(d => d.id === matchedDish.id)) {
          dishList.push(matchedDish)
          matchedDish.ingredients.forEach(ing => {
            const ingName = ing.split(/\s+/).slice(0, -1).join(' ') || ing
            if (ingName.trim()) {
              allIngredients.add(ingName.trim())
            }
          })
        }
      } else {
        unmatchedDishes.push(dishName)
      }
    }

    // 显示匹配结果提示
    if (dishList.length > 0 && unmatchedDishes.length > 0) {
      ElMessage.warning({
        message: `已找到 ${dishList.length} 道菜品（${dishList.map(d => d.name).join('、')}），但 "${unmatchedDishes.join('、')}" 未找到匹配`,
        duration: 5000
      })
    } else if (dishList.length > 0) {
      ElMessage.success({
        message: `已找到 ${dishList.length} 道菜品（${dishList.map(d => d.name).join('、')}），整理出 ${allIngredients.size} 种食材`,
        duration: 3000
      })
    }

    if (allIngredients.size === 0) {
      ElMessage.warning('未找到匹配的菜品，请检查输入。提示：可以输入菜品全名或部分名称，如"宫保鸡丁"或"宫保"')
      categorizedIngredients.value = { basics: [], main: [] }
      ingredientsByZone.value = {}
      return
    }

    // 分类食材
    const basics: string[] = []
    const main: string[] = []

    allIngredients.forEach(ing => {
      const isBasic = basicIngredientKeywords.some(keyword => ing.includes(keyword))
      if (isBasic) {
        basics.push(ing)
      } else {
        main.push(ing)
      }
    })

    categorizedIngredients.value = {
      basics: basics.sort(),
      main: main.sort()
    }

    // 按区域分类
    const zoneMap: Record<string, string[]> = {}
    allIngredients.forEach(ingredient => {
      const zone = getIngredientZone(ingredient)
      if (zone) {
        if (!zoneMap[zone]) {
          zoneMap[zone] = []
        }
        zoneMap[zone].push(ingredient)
      }
    })

    Object.keys(zoneMap).forEach(zone => {
      zoneMap[zone].sort()
    })

    ingredientsByZone.value = zoneMap

    ElMessage.success(`已找到 ${dishList.length} 道菜品，整理出 ${allIngredients.size} 种食材`)
  } catch (error) {
    ElMessage.error('获取食材失败')
    categorizedIngredients.value = { basics: [], main: [] }
    ingredientsByZone.value = {}
  } finally {
    ingredientsLoading.value = false
  }
}

const getShoppingIngredientsByZoneForCard = (): Record<string, string[]> => {
  const zoneMap: Record<string, string[]> = {}
  
  // 获取未选中的主要食材
  const unselectedMain = categorizedIngredients.value.main.filter(
    ing => !selectedIngredients.value.has(ing)
  )
  
  // 获取未选中的基础调料
  const unselectedBasics = categorizedIngredients.value.basics.filter(
    ing => !selectedIngredients.value.has(ing)
  )
  
  // 合并所有未选中的食材
  const allUnselected = [...unselectedMain, ...unselectedBasics]
  
  // 按区域分类
  allUnselected.forEach(ingredient => {
    const zone = getIngredientZone(ingredient)
    if (zone) {
      if (!zoneMap[zone]) {
        zoneMap[zone] = []
      }
      zoneMap[zone].push(ingredient)
    }
  })
  
  // 排序
  Object.keys(zoneMap).forEach(zone => {
    zoneMap[zone].sort()
  })
  
  return zoneMap
}

const selectedIngredients = ref<Set<string>>(new Set())

const toggleIngredient = (ingredient: string) => {
  if (selectedIngredients.value.has(ingredient)) {
    selectedIngredients.value.delete(ingredient)
  } else {
    selectedIngredients.value.add(ingredient)
  }
}

const isIngredientSelected = (ingredient: string) => {
  return selectedIngredients.value.has(ingredient)
}

// 从路由参数获取菜品名称并填入输入框
onMounted(() => {
  const dishName = route.query.dish as string
  if (dishName) {
    // 如果输入框已有内容，追加菜品名称
    if (dishInput.value.trim()) {
      dishInput.value = `${dishInput.value}, ${dishName}`
    } else {
      dishInput.value = dishName
    }
    // 清除查询参数，避免刷新时重复添加
    router.replace({ query: {} })
  }
})

// 获取未选中的主要食材
const getUnselectedMainIngredients = (): string[] => {
  return categorizedIngredients.value.main.filter(
    ing => !selectedIngredients.value.has(ing)
  )
}

// 获取未选中的基础调料
const getUnselectedBasicIngredients = (): string[] => {
  return categorizedIngredients.value.basics.filter(
    ing => !selectedIngredients.value.has(ing)
  )
}

// 复制购物清单
const copyIngredientsList = async () => {
  try {
    let text = '🛒 购物清单\n\n'
    
    // 按区域购买的食材（只显示未选中的）
    const shoppingZones = getShoppingIngredientsByZoneForCard()
    if (Object.keys(shoppingZones).length > 0) {
      Object.entries(shoppingZones).forEach(([zone, ingredients]) => {
        text += `${zoneNames[zone] || zone}：${ingredients.join('、')}\n`
      })
    } else {
      // 如果没有按区域分类，显示主要食材和基础调料
      const unselectedMain = getUnselectedMainIngredients()
      const unselectedBasics = getUnselectedBasicIngredients()
      
      if (unselectedMain.length > 0) {
        text += `🥬 主要食材：${unselectedMain.join('、')}\n`
      }
      if (unselectedBasics.length > 0) {
        text += `🧂 基础调料：${unselectedBasics.join('、')}\n`
      }
    }
    
    await navigator.clipboard.writeText(text)
    ElMessage.success('购物清单已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const saveShoppingCardAsImage = async () => {
  if (!shoppingCardRef.value) return
  
  try {
    const html2canvas = (await import('html2canvas')).default
    
    const canvas = await html2canvas(shoppingCardRef.value, {
      backgroundColor: '#ffffff',
      scale: 2,
      logging: false
    })
    
    canvas.toBlob((blob) => {
      if (!blob) {
        ElMessage.error('生成图片失败')
        return
      }
      
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `购物清单_${new Date().getTime()}.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      ElMessage.success('图片已保存')
    }, 'image/png')
  } catch (error) {
    console.error('保存图片失败:', error)
    ElMessage.error('保存图片失败')
  }
}

const shareShoppingCard = async () => {
  try {
    if (navigator.share) {
      const canvas = await (await import('html2canvas')).default(shoppingCardRef.value!, {
        backgroundColor: '#ffffff',
        scale: 2,
        logging: false
      })
      
      canvas.toBlob(async (blob) => {
        if (blob) {
          const file = new File([blob], 'shopping-list.png', { type: 'image/png' })
          await navigator.share({
            title: '购物清单',
            files: [file]
          })
        }
      }, 'image/png')
    } else {
      await saveShoppingCardAsImage()
    }
  } catch (error: any) {
    if (error.name !== 'AbortError') {
      ElMessage.error('分享失败')
    }
  }
}
</script>

<style scoped>
.shop-by-dish-container {
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
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
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

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.result-header h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.shopping-tips-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.zones-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.zone-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.zone-title {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 12px;
}

.zone-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ingredients-categorized {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.category-section {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.category-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.selection-tip-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border: 2px solid #ff9800;
  border-radius: 10px;
  margin-bottom: 24px;
  animation: slideIn 0.3s ease-out;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.2);
}

.tip-icon {
  font-size: 22px;
  color: #ff9800;
  flex-shrink: 0;
}

.tip-text {
  font-size: 15px;
  color: #e65100;
  font-weight: 600;
  line-height: 1.6;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.category-hint {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  font-weight: 600;
  color: #ff6b00;
  margin-left: 12px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border: 1.5px solid #ff9800;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.15);
}

.category-hint .el-icon {
  font-size: 14px;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ingredient-tag {
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  margin: 0 !important;
  padding: 8px 14px !important;
  font-size: 14px !important;
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.ingredient-tag.clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.ingredient-tag.selected {
  animation: selectPulse 0.4s ease-out;
  transform: scale(1);
}

@keyframes selectPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.check-icon {
  margin-right: 4px;
  font-size: 14px;
}

.check-icon-enter-active {
  animation: checkBounce 0.5s ease-out;
}

.check-icon-leave-active {
  animation: checkFadeOut 0.3s ease-out;
}

@keyframes checkBounce {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-180deg);
  }
  50% {
    transform: scale(1.2) rotate(10deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

@keyframes checkFadeOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.5);
  }
}

.ingredient-text {
  transition: all 0.3s ease;
}

.ingredient-tag.selected .ingredient-text {
  text-decoration: line-through;
  opacity: 0.7;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 购物卡片样式 */
.shopping-card-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.shopping-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header-section {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #409eff;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
}

.card-section {
  margin-bottom: 16px;
}

.shopping-zones {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.zone-item {
  padding: 4px 10px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  margin-right: 6px;
  margin-bottom: 6px;
  display: inline-block;
}

.card-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
}

.footer-text {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .zones-grid {
    grid-template-columns: 1fr;
  }
}
</style>

