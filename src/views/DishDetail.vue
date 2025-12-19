<template>
  <div v-loading="dishesStore.loading" class="detail-container">
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

      <div class="detail-sections">
        <el-card class="section-card">
          <template #header>
            <h3>
              <el-icon><ShoppingBag /></el-icon>
              所需食材
            </h3>
          </template>
          <ul class="ingredients-list">
            <li v-for="(ingredient, index) in dishesStore.currentDish.ingredients" :key="index">
              <el-icon><Check /></el-icon>
              {{ ingredient }}
            </li>
          </ul>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <h3>
              <el-icon><Document /></el-icon>
              制作步骤
            </h3>
          </template>
          <ol class="steps-list">
            <li v-for="(step, index) in dishesStore.currentDish.steps" :key="index">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">{{ step }}</div>
            </li>
          </ol>
        </el-card>
      </div>

      <!-- 评论区域 -->
      <el-card class="section-card comments-section">
        <template #header>
          <h3>
            <el-icon><ChatLineRound /></el-icon>
            评论 ({{ commentsStore.comments.length }})
          </h3>
        </template>

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
      </el-card>
    </div>

    <el-empty v-else-if="!dishesStore.loading" description="菜品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDishesStore } from '@/stores/dishes'
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

onMounted(async () => {
  const dishId = parseInt(route.params.id as string)
  if (dishId) {
    await dishesStore.fetchDishById(dishId)
    await commentsStore.fetchComments(dishId)
  }
})

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
  padding: 24px;
  width: 100%;
}

.back-button {
  margin-bottom: 24px;
}

.detail-content {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.detail-header {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.detail-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
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
  font-size: 32px;
  color: #333;
  margin-bottom: 16px;
}

.detail-description {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
  flex: 1;
}

.detail-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.detail-actions {
  margin-top: auto;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  border-radius: 12px;
}

.section-card :deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.section-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: #333;
  margin: 0;
}

.ingredients-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ingredients-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  color: #333;
}

.ingredients-list li:last-child {
  border-bottom: none;
}

.ingredients-list li .el-icon {
  color: #67c23a;
  font-size: 18px;
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
  border-bottom: 1px solid #f0f0f0;
  counter-increment: step-counter;
}

.steps-list li:last-child {
  border-bottom: none;
}

.step-number {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.step-content {
  flex: 1;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  padding-top: 4px;
}

@media (max-width: 768px) {
  .detail-header {
    grid-template-columns: 1fr;
  }

  .detail-image-wrapper {
    height: 300px;
  }
}

.comments-section {
  margin-top: 24px;
}

.comment-form {
  margin-bottom: 32px;
  padding-bottom: 24px;
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

