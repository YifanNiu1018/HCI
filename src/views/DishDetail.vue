<template>
  <div v-loading="dishesStore.loading" class="detail-container">
    <div class="detail-wrapper">
      <el-button
        class="back-button"
        @click="$router.back()"
        :icon="ArrowLeft"
      >
        返回
      </el-button>

      <div v-if="dishesStore.currentDish" class="detail-content">
        <div class="detail-header">
        <div class="detail-image-wrapper">
          <img
            :src="getImageUrl(dishesStore.currentDish.image)"
            :alt="dishesStore.currentDish.name"
            class="detail-image"
          />
        </div>
        <div class="detail-info">
          <h1 class="detail-title">{{ dishesStore.currentDish.name }}</h1>
          <p class="detail-description">{{ dishesStore.currentDish.description }}</p>
          <div class="detail-meta">
            <el-tag :type="getDifficultyType(dishesStore.currentDish.difficulty)" size="large">
              {{ dishesStore.currentDish.difficulty }}
            </el-tag>
            <el-tag size="large">
              <el-icon><Clock /></el-icon>
              {{ dishesStore.currentDish.cookingTime }}分钟
            </el-tag>
            <el-tag size="large">
              <el-icon><User /></el-icon>
              {{ dishesStore.currentDish.servings }}人份
            </el-tag>
            <el-tag size="large">
              <el-icon><Menu /></el-icon>
              {{ dishesStore.currentDish.category }}
            </el-tag>
          </div>
          <div class="detail-actions">
            <el-button
              type="danger"
              size="large"
              :icon="dishesStore.currentDish.isFavorite ? StarFilled : Star"
              @click="handleToggleFavorite"
              :loading="favoriteLoading"
            >
              {{ dishesStore.currentDish.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

        <div class="detail-main">
          <div class="detail-main-content">
          <!-- 所需食材 -->
          <div class="content-section">
            <h2 class="section-title">
              <el-icon><ShoppingBag /></el-icon>
              所需食材
            </h2>
            <ul class="ingredients-list">
              <li v-for="(ingredient, index) in dishesStore.currentDish.ingredients" :key="index">
                <el-icon><Check /></el-icon>
                <span>{{ ingredient }}</span>
              </li>
            </ul>
          </div>

          <!-- 制作步骤 -->
          <div class="content-section">
            <h2 class="section-title">
              <el-icon><Document /></el-icon>
              制作步骤
            </h2>
            <ol class="steps-list">
              <li v-for="(step, index) in dishesStore.currentDish.steps" :key="index">
                <div class="step-number">{{ index + 1 }}</div>
                <div class="step-content">{{ step }}</div>
              </li>
            </ol>
          </div>

          <!-- 评论区域 -->
          <div class="content-section comments-section">
            <h2 class="section-title">
              <el-icon><ChatLineRound /></el-icon>
              评论 ({{ commentsStore.comments.length }})
            </h2>

            <!-- 评论表单 -->
            <div v-if="userStore.isLoggedIn" class="comment-form">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="写下你的评论..."
                maxlength="2000"
                show-word-limit
              />
              <div class="comment-form-actions">
                <el-button
                  type="primary"
                  @click="handleSubmitComment"
                  :loading="commentLoading"
                >
                  发表评论
                </el-button>
              </div>
            </div>
            <div v-else class="comment-login-tip">
              <el-button type="primary" @click="$router.push('/login')">
                登录后发表评论
              </el-button>
            </div>

            <!-- 评论列表 -->
            <div v-loading="commentsStore.loading" class="comments-list">
              <div v-if="commentsStore.comments.length === 0" class="no-comments">
                <el-empty description="暂无评论，快来发表第一条评论吧！" :image-size="100" />
              </div>
              <div v-else>
                <div
                  v-for="comment in commentsStore.comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <CommentItem
                    :comment="comment"
                    :dish-id="dishesStore.currentDish?.id || 0"
                    @reply="handleReply"
                    @delete="handleDeleteComment"
                  />
                </div>
              </div>
            </div>
          </div>
          </div>

          <!-- 推荐菜品 - 右侧边栏 -->
          <div v-if="recommendedDishes.length > 0" class="recommendations-sidebar">
          <div class="sidebar-card">
            <h3 class="sidebar-title">
              <el-icon><Star /></el-icon>
              相关推荐
            </h3>
            <div class="recommended-dishes-list">
              <div
                v-for="dish in recommendedDishes"
                :key="dish.id"
                class="recommended-dish-item"
                @click="goToDish(dish.id)"
              >
                <img
                  :src="getImageUrl(dish.image)"
                  :alt="dish.name"
                  class="recommended-dish-image"
                />
                <div class="recommended-dish-info">
                  <h4 class="recommended-dish-name">{{ dish.name }}</h4>
                  <p class="recommended-dish-meta">
                    <span>{{ dish.cookingTime }}分钟</span>
                  </p>
                  <div v-if="dish.tags && dish.tags.length > 0" class="recommended-dish-tags">
                    <el-tag
                      v-for="tag in dish.tags.slice(0, 2)"
                      :key="tag"
                      size="small"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                  <div v-else-if="dish.category" class="recommended-dish-tags">
                    <el-tag size="small">
                      {{ dish.category }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="菜品不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore, type Dish } from '@/stores/dishes'
import { useUserStore } from '@/stores/user'
import { useCommentsStore } from '@/stores/comments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getImageUrl } from '@/utils/image'
import CommentItem from '@/components/CommentItem.vue'
import {
  ArrowLeft,
  Clock,
  User,
  Menu,
  Star,
  StarFilled,
  ShoppingBag,
  Check,
  Document,
  ChatLineRound
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const dishesStore = useDishesStore()
const userStore = useUserStore()
const commentsStore = useCommentsStore()
const favoriteLoading = ref(false)
const commentLoading = ref(false)
const newComment = ref('')
const replyingTo = ref<number | null>(null)
const replyContent = ref('')
const allDishes = ref<Dish[]>([])

// 推荐菜品：同分类的其他菜品，最多显示4个
const recommendedDishes = computed(() => {
  if (!dishesStore.currentDish || allDishes.value.length === 0) {
    return []
  }
  
  const currentCategory = dishesStore.currentDish.category
  const currentId = dishesStore.currentDish.id
  
  // 筛选同分类的菜品，排除当前菜品
  const sameCategoryDishes = allDishes.value.filter(
    dish => dish.category === currentCategory && dish.id !== currentId
  )
  
  // 如果同分类的菜品不足4个，补充其他分类的菜品
  if (sameCategoryDishes.length < 4) {
    const otherDishes = allDishes.value.filter(
      dish => dish.category !== currentCategory && dish.id !== currentId
    )
    const remaining = 4 - sameCategoryDishes.length
    return [...sameCategoryDishes, ...otherDishes.slice(0, remaining)]
  }
  
  return sameCategoryDishes.slice(0, 4)
})

const loadDishData = async (dishId: number) => {
  if (dishId) {
    await dishesStore.fetchDishById(dishId)
    await commentsStore.fetchComments(dishId)
    // 获取所有菜品用于推荐
    await dishesStore.fetchDishes()
    allDishes.value = dishesStore.dishes
  }
}

onMounted(async () => {
  const dishId = parseInt(route.params.id as string)
  await loadDishData(dishId)
})

// 监听路由参数变化，当跳转到不同菜品时重新加载数据
watch(
  () => route.params.id,
  async (newId) => {
    const dishId = parseInt(newId as string)
    await loadDishData(dishId)
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
)

const goToDish = (dishId: number) => {
  router.push(`/dish/${dishId}`)
}

const handleToggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  if (!dishesStore.currentDish) return

  favoriteLoading.value = true
  try {
    await dishesStore.toggleFavorite(dishesStore.currentDish.id)
    ElMessage.success(
      dishesStore.currentDish.isFavorite ? '收藏成功' : '取消收藏成功'
    )
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!dishesStore.currentDish) return

  commentLoading.value = true
  try {
    const result = await commentsStore.createComment(
      dishesStore.currentDish.id,
      newComment.value.trim()
    )
    if (result.success) {
      newComment.value = ''
      await commentsStore.fetchComments(dishesStore.currentDish.id)
      ElMessage.success('评论发表成功')
    } else {
      ElMessage.error(result.message || '评论失败')
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleReply = async (parentId: number, content: string) => {
  if (!dishesStore.currentDish) return

  commentLoading.value = true
  try {
    const result = await commentsStore.createComment(
      dishesStore.currentDish.id,
      content.trim(),
      parentId
    )
    if (result.success) {
      await commentsStore.fetchComments(dishesStore.currentDish.id)
      ElMessage.success('回复成功')
    } else {
      ElMessage.error(result.message || '回复失败')
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (commentId: number) => {
  if (!dishesStore.currentDish) return

  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await commentsStore.deleteComment(dishesStore.currentDish.id, commentId)
    if (result.success) {
      await commentsStore.fetchComments(dishesStore.currentDish.id)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 40px;
  width: 100%;
}

.back-button {
  margin-bottom: 20px;
}

.detail-content {
  background: #fff;
  padding: 24px;
}

.detail-header {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.detail-image-wrapper {
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  aspect-ratio: 16 / 9;
  width: 100%;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  display: flex;
  flex-direction: column;
}

.detail-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.3;
}

.detail-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
}

.detail-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.detail-actions {
  margin-top: auto;
}

.detail-main {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;
  align-items: start;
}

.detail-main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.content-section {
  background: #fff;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.ingredients-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.ingredients-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #333;
  background: #f8f9fa;
  border-radius: 4px;
}

.ingredients-list li .el-icon {
  color: #67c23a;
  font-size: 16px;
  flex-shrink: 0;
}

.steps-list {
  list-style: none;
  padding: 0;
  margin: 0;
  counter-reset: step-counter;
}

.steps-list li {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  counter-increment: step-counter;
}

.steps-list li:last-child {
  border-bottom: none;
}

.step-number {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.step-content {
  flex: 1;
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  padding-top: 2px;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 16px;
  }

  .detail-header {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .detail-image-wrapper {
    height: 250px;
  }

  .detail-title {
    font-size: 22px;
  }

  .detail-description {
    font-size: 14px;
  }

  .detail-meta {
    flex-direction: column;
    gap: 12px;
  }

  .detail-main {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .recommendations-sidebar {
    position: static;
    order: -1;
  }

  .section-title {
    font-size: 18px;
  }

  .ingredients-list li,
  .steps-list li {
    padding: 16px 0;
  }

  .step-content {
    font-size: 14px;
  }

  .detail-actions .el-button {
    width: 100%;
    min-height: 44px;
  }
}

@media (max-width: 480px) {
  .detail-container {
    padding: 12px;
  }

  .detail-image-wrapper {
    height: 200px;
  }

  .detail-title {
    font-size: 20px;
  }

  .section-title {
    font-size: 16px;
  }
}

.recommendations-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.sidebar-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #eee;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.recommended-dishes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommended-dish-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid #f0f0f0;
}

.recommended-dish-item:hover {
  background: #f8f9fa;
  border-color: #409eff;
}

.recommended-dish-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
  aspect-ratio: 4 / 3;
}

.recommended-dish-info {
  flex: 1;
  min-width: 0;
}

.recommended-dish-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommended-dish-meta {
  font-size: 12px;
  color: #999;
  margin: 0 0 4px 0;
}

.recommended-dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.comments-section {
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .detail-main {
    grid-template-columns: 1fr;
  }

  .recommendations-sidebar {
    position: static;
    margin-top: 24px;
  }

  .recommended-dishes-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }
}

.comment-form {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.comment-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comment-login-tip {
  margin-bottom: 32px;
  padding: 24px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
}

.comments-list {
  min-height: 100px;
}

.no-comments {
  padding: 40px 0;
}

.comment-item {
  margin-bottom: 24px;
}

.comment-item:last-child {
  margin-bottom: 0;
}
</style>

