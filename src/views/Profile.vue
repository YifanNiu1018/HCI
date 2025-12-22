<template>
  <div class="profile-container">
    <el-button
      class="back-button"
      @click="$router.push('/')"
      :icon="ArrowLeft"
    >
      返回主页
    </el-button>
    
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <div class="user-info">
        <el-avatar :size="80" :src="userAvatarUrl" class="user-avatar">
          {{ userStore.user?.username?.charAt(0) }}
        </el-avatar>
        <div class="user-details">
          <h3>{{ userStore.user?.username }}</h3>
        </div>
        <div class="user-stats">
          <div class="stat-item" @click="showFollowingList = true">
            <div class="stat-value">{{ followingCount }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item" @click="showFollowersList = true">
            <div class="stat-value">{{ followersCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ favorites.length + favoriteNotes.length }}</div>
            <div class="stat-label">收藏</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ notes.length }}</div>
            <div class="stat-label">笔记</div>
          </div>
        </div>
      </div>

      <!-- Tab切换栏 -->
      <div class="profile-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="收藏的菜谱" name="favorite-dishes">
            <template #label>
              <span class="tab-label">
                <el-icon><Star /></el-icon>
                收藏的菜谱
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="收藏的笔记" name="favorite-notes">
            <template #label>
              <span class="tab-label">
                <el-icon><Star /></el-icon>
                收藏的笔记
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="我的笔记" name="notes">
            <template #label>
              <span class="tab-label">
                <el-icon><Document /></el-icon>
                我的笔记
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="草稿箱" name="drafts">
            <template #label>
              <span class="tab-label">
                <el-icon><EditPen /></el-icon>
                草稿箱
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="历史记录" name="history">
            <template #label>
              <span class="tab-label">
                <el-icon><Clock /></el-icon>
                历史记录
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="动态" name="following-notes">
            <template #label>
              <span class="tab-label">
                <el-icon><Bell /></el-icon>
                动态
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 收藏的菜谱 -->
    <el-card v-if="activeTab === 'favorite-dishes'" class="content-card">
      <template #header>
        <div class="search-header">
          <h3>
            <el-icon><Star /></el-icon>
            收藏的菜谱
            <span v-if="filteredFavorites.length !== favorites.length" class="count-badge">
              ({{ filteredFavorites.length }}/{{ favorites.length }})
            </span>
          </h3>
          <el-input
            v-model="favoriteDishSearchKeyword"
            placeholder="搜索收藏的菜谱..."
            class="search-input"
            clearable
            :prefix-icon="Search"
          />
        </div>
      </template>
      <div v-loading="loading" class="content-area">
        <div v-if="filteredFavorites.length > 0" class="items-grid">
          <el-card
            v-for="dish in filteredFavorites"
            :key="'dish-' + dish.id"
            class="item-card"
            shadow="hover"
            @click="goToDetail(dish.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(dish.image)" :alt="dish.name" class="item-image" />
              <div class="item-overlay">
                <el-button
                  type="danger"
                  :icon="StarFilled"
                  circle
                  size="small"
                  @click.stop="handleRemoveFavorite(dish.id)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ dish.name }}</h4>
              <p class="item-description">{{ dish.description }}</p>
              <div class="item-meta">
                <el-tag size="small">
                  <el-icon><Clock /></el-icon>
                  {{ dish.cookingTime }}分钟
                </el-tag>
                <el-tag size="small" :type="getDifficultyType(dish.difficulty)">
                  {{ dish.difficulty }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else-if="favorites.length === 0" description="暂无收藏的菜谱" />
        <el-empty v-else description="没有找到匹配的菜谱" />
      </div>
    </el-card>

    <!-- 收藏的笔记 -->
    <el-card v-if="activeTab === 'favorite-notes'" class="content-card">
      <template #header>
        <div class="search-header">
          <h3>
            <el-icon><Star /></el-icon>
            收藏的笔记
            <span v-if="filteredFavoriteNotes.length !== favoriteNotes.length" class="count-badge">
              ({{ filteredFavoriteNotes.length }}/{{ favoriteNotes.length }})
            </span>
          </h3>
          <el-input
            v-model="favoriteNoteSearchKeyword"
            placeholder="搜索收藏的笔记..."
            class="search-input"
            clearable
            :prefix-icon="Search"
          />
        </div>
      </template>
      <div v-loading="favoriteNotesLoading" class="content-area">
        <div v-if="filteredFavoriteNotes.length > 0" class="items-grid">
          <el-card
            v-for="note in filteredFavoriteNotes"
            :key="'note-' + note.id"
            class="item-card note-card"
            shadow="hover"
            @click="goToNoteDetail(note.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(note.image)" :alt="note.name" class="item-image" />
              <div class="item-overlay">
                <el-button
                  type="danger"
                  :icon="StarFilled"
                  circle
                  size="small"
                  @click.stop="handleRemoveFavoriteNote(note.id)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ note.name }}</h4>
              <p class="item-description">{{ note.description }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(note.createdAt) }}</span>
                <span class="note-author-name">by {{ note.author.username }}</span>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else-if="favoriteNotes.length === 0" description="暂无收藏的笔记" />
        <el-empty v-else description="没有找到匹配的笔记" />
      </div>
    </el-card>

    <!-- 我的笔记 -->
    <el-card v-if="activeTab === 'notes'" class="content-card">
      <template #header>
        <div class="notes-header">
          <h3>
            <el-icon><Document /></el-icon>
            我的笔记
          </h3>
          <el-button type="primary" :icon="Edit" @click="$router.push('/note/create')">
            发布笔记
          </el-button>
        </div>
      </template>

      <div v-loading="notesLoading" class="content-area">
        <div v-if="notes.length > 0" class="items-grid">
          <el-card
            v-for="note in notes"
            :key="note.id"
            class="item-card note-card"
            shadow="hover"
            @click="goToNoteDetail(note.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(note.image)" :alt="note.name" class="item-image" />
              <div class="item-overlay">
                <div class="visibility-switch">
                  <el-switch
                    v-model="note.isPublic"
                    active-text="公开"
                    inactive-text="私密"
                    @change="handleToggleVisibility(note.id, note.isPublic)"
                    @click.stop
                  />
                </div>
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click.stop="handleDeleteNote(note.id)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ note.name }}</h4>
              <p class="item-description">{{ note.description }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(note.createdAt) }}</span>
                <div v-if="note.tags && note.tags.length > 0" class="note-tags">
                  <el-tag
                    v-for="tag in note.tags.slice(0, 3)"
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
        <el-empty v-else description="暂无笔记，快去发布你的做菜笔记吧！" />
      </div>
    </el-card>

    <!-- 草稿箱 -->
    <el-card v-if="activeTab === 'drafts'" class="content-card">
      <template #header>
        <div class="notes-header">
          <h3>
            <el-icon><EditPen /></el-icon>
            草稿箱
          </h3>
        </div>
      </template>

      <div v-loading="draftsLoading" class="content-area">
        <div v-if="drafts.length > 0" class="items-grid">
          <el-card
            v-for="draft in drafts"
            :key="draft.id"
            class="item-card note-card draft-card"
            shadow="hover"
            @click="goToEditDraft(draft.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(draft.image)" :alt="draft.name" class="item-image" />
              <div class="item-overlay">
                <el-tag type="warning" size="small" class="draft-tag">草稿</el-tag>
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click.stop="handleDeleteDraft(draft.id)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ draft.name || '未命名草稿' }}</h4>
              <p class="item-description">{{ draft.description || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(draft.createdAt) }}</span>
                <div v-if="draft.tags && draft.tags.length > 0" class="note-tags">
                  <el-tag
                    v-for="tag in draft.tags.slice(0, 3)"
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
        <el-empty v-else description="暂无草稿" />
      </div>
    </el-card>

    <!-- 历史记录 -->
    <el-card v-if="activeTab === 'history'" class="content-card">
      <template #header>
        <div class="notes-header">
          <h3>
            <el-icon><Clock /></el-icon>
            历史记录
          </h3>
          <el-button
            type="danger"
            size="small"
            @click="handleClearHistory"
            :disabled="histories.length === 0"
          >
            清空历史
          </el-button>
        </div>
      </template>

      <div v-loading="historyLoading" class="content-area">
        <div v-if="histories.length > 0" class="items-grid">
          <!-- 菜品历史 -->
          <el-card
            v-for="item in histories.filter(h => h.type === 'dish' && h.dish)"
            :key="'history-dish-' + item.id"
            class="item-card"
            shadow="hover"
            @click="goToDetail(item.dish.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(item.dish.image)" :alt="item.dish.name" class="item-image" />
              <div class="item-overlay">
                <el-tag type="info" size="small">菜品</el-tag>
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ item.dish.name }}</h4>
              <p class="item-description">{{ item.dish.description || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(item.viewedAt) }}</span>
              </div>
            </div>
          </el-card>

          <!-- 笔记历史 -->
          <el-card
            v-for="item in histories.filter(h => h.type === 'note' && h.note)"
            :key="'history-note-' + item.id"
            class="item-card note-card"
            shadow="hover"
            @click="goToNoteDetail(item.note.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(item.note.image)" :alt="item.note.name" class="item-image" />
              <div class="item-overlay">
                <el-tag type="success" size="small">笔记</el-tag>
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ item.note.name }}</h4>
              <p class="item-description">{{ item.note.description || '暂无描述' }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(item.viewedAt) }}</span>
                <div v-if="item.note.tags && item.note.tags.length > 0" class="note-tags">
                  <el-tag
                    v-for="tag in item.note.tags.slice(0, 3)"
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
        <el-empty v-else description="暂无历史记录" />
      </div>
    </el-card>

    <!-- 关注用户动态 -->
    <el-card v-if="activeTab === 'following-notes'" class="content-card">
      <template #header>
        <div class="notes-header">
          <h3>
            <el-icon><Bell /></el-icon>
            关注用户动态
          </h3>
        </div>
      </template>

      <div v-loading="followingNotesLoading" class="content-area">
        <div v-if="followingNotes.length > 0" class="items-grid">
          <el-card
            v-for="note in followingNotes"
            :key="note.id"
            class="item-card note-card"
            shadow="hover"
            @click="goToNoteDetail(note.id)"
          >
            <div class="item-image-wrapper">
              <img :src="getImageUrl(note.image)" :alt="note.name" class="item-image" />
              <div class="item-overlay">
                <el-button
                  :type="note.isFavorite ? 'danger' : 'default'"
                  :icon="note.isFavorite ? StarFilled : Star"
                  circle
                  size="small"
                  @click.stop="handleToggleFavoriteNote(note.id, note.isFavorite || false)"
                />
              </div>
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ note.name }}</h4>
              <p class="item-description">{{ note.description }}</p>
              <div class="item-meta">
                <span class="note-date">{{ formatDate(note.createdAt) }}</span>
                <span 
                  class="note-author-name" 
                  @click.stop="goToUserProfile(note.author.id)" 
                  style="cursor: pointer; color: #409eff;"
                >
                  by {{ note.author.username }}
                </span>
                <div v-if="note.tags && note.tags.length > 0" class="note-tags">
                  <el-tag
                    v-for="tag in note.tags.slice(0, 3)"
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
        <el-empty v-else description="暂无关注用户的动态" />
      </div>
    </el-card>

    <!-- 关注列表对话框 -->
    <el-dialog
      v-model="showFollowingList"
      title="关注列表"
      width="500px"
    >
      <div v-loading="followingLoading" class="user-list">
        <div v-if="following.length > 0">
          <div
            v-for="user in following"
            :key="user.id"
            class="user-list-item"
          >
            <el-avatar :size="40" :src="getImageUrl(user.avatar || '')" @click="goToUserProfile(user.id)" style="cursor: pointer;">
              {{ user.username?.charAt(0) }}
            </el-avatar>
            <div class="user-list-info" @click="goToUserProfile(user.id)" style="cursor: pointer; flex: 1;">
              <div class="user-list-name">{{ user.username }}</div>
            </div>
            <div class="user-list-actions">
              <el-button
                type="success"
                size="small"
                @click.stop="goToMessages(user.id)"
              >
                私信
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click.stop="handleUnfollow(user.id)"
              >
                取消关注
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无关注" />
      </div>
    </el-dialog>

    <!-- 粉丝列表对话框 -->
    <el-dialog
      v-model="showFollowersList"
      title="粉丝列表"
      width="500px"
    >
      <div v-loading="followersLoading" class="user-list">
        <div v-if="followers.length > 0">
          <div
            v-for="user in followers"
            :key="user.id"
            class="user-list-item"
          >
            <el-avatar :size="40" :src="getImageUrl(user.avatar || '')" @click="goToUserProfile(user.id)" style="cursor: pointer;">
              {{ user.username?.charAt(0) }}
            </el-avatar>
            <div class="user-list-info" @click="goToUserProfile(user.id)" style="cursor: pointer; flex: 1;">
              <div class="user-list-name">{{ user.username }}</div>
            </div>
            <div class="user-list-actions">
              <el-button
                type="success"
                size="small"
                @click.stop="goToMessages(user.id)"
              >
                私信
              </el-button>
              <el-button
                v-if="!isFollowingUser(user.id)"
                type="primary"
                size="small"
                @click.stop="handleFollow(user.id)"
              >
                关注
              </el-button>
              <el-button
                v-else
                type="danger"
                size="small"
                @click.stop="handleUnfollow(user.id)"
              >
                取消关注
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无粉丝" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDishesStore } from '@/stores/dishes'
import { useNotesStore } from '@/stores/notes'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Clock, Document, Edit, Delete, ArrowLeft, Search, EditPen, Bell } from '@element-plus/icons-vue'
import type { Dish } from '@/stores/dishes'
import type { Note } from '@/stores/notes'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const route = useRoute()

const userAvatarUrl = computed(() => {
  if (!userStore.user?.avatar) return ''
  return getImageUrl(userStore.user.avatar)
})
const router = useRouter()
const userStore = useUserStore()
const dishesStore = useDishesStore()
const notesStore = useNotesStore()
const favorites = ref<Dish[]>([])
const favoriteNotes = ref<Note[]>([])
const notes = ref<Note[]>([])
const drafts = ref<Note[]>([])
const histories = ref<any[]>([])
const followingNotes = ref<Note[]>([])
const following = ref<any[]>([])
const followers = ref<any[]>([])
const loading = ref(false)
const favoriteNotesLoading = ref(false)
const notesLoading = ref(false)
const draftsLoading = ref(false)
const historyLoading = ref(false)
const followingNotesLoading = ref(false)
const followingLoading = ref(false)
const followersLoading = ref(false)
const activeTab = ref<string>('favorite-dishes')
const favoriteDishSearchKeyword = ref('')
const favoriteNoteSearchKeyword = ref('')
const showFollowingList = ref(false)
const showFollowersList = ref(false)

onMounted(async () => {
  // 根据路由参数设置初始tab
  const tab = route.query.tab as string
  if (tab === 'notes' || tab === 'favorite-dishes' || tab === 'favorite-notes' || tab === 'drafts' || tab === 'history' || tab === 'following-notes') {
    activeTab.value = tab
  }
  
  await loadFavorites()
  await loadFavoriteNotes()
  await loadNotes()
  await loadDrafts()
  await loadHistory()
  await loadFollowingNotes()
  await loadFollowing()
  await loadFollowers()
})

// 监听路由变化
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'notes' || newTab === 'favorite-dishes' || newTab === 'favorite-notes' || newTab === 'drafts' || newTab === 'history' || newTab === 'following-notes') {
    activeTab.value = newTab as string
    if (newTab === 'drafts') {
      loadDrafts()
    } else if (newTab === 'history') {
      loadHistory()
    } else if (newTab === 'following-notes') {
      loadFollowingNotes()
    }
  }
})

const handleTabChange = (tabName: string) => {
  // 更新URL参数但不刷新页面
  router.replace({ query: { ...route.query, tab: tabName } })
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const data = await dishesStore.fetchFavorites()
    favorites.value = data
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/dish/${id}`)
}

const loadFavoriteNotes = async () => {
  favoriteNotesLoading.value = true
  try {
    const response = await api.get('/user/favorite-notes')
    favoriteNotes.value = response.data
  } catch (error) {
    ElMessage.error('加载收藏笔记失败')
  } finally {
    favoriteNotesLoading.value = false
  }
}

const handleRemoveFavorite = async (dishId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await dishesStore.toggleFavorite(dishId)
    favorites.value = favorites.value.filter(dish => dish.id !== dishId)
    ElMessage.success('取消收藏成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleToggleFavoriteNote = async (noteId: number, isFavorite: boolean) => {
  try {
    const result = await notesStore.toggleFavorite(noteId)
    if (result.success) {
      // 更新本地状态
      const note = followingNotes.value.find(n => n.id === noteId)
      if (note) {
        note.isFavorite = !isFavorite
      }
      ElMessage.success(isFavorite ? '已取消收藏' : '收藏成功')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error('操作失败')
  }
}

const handleRemoveFavoriteNote = async (noteId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await notesStore.toggleFavorite(noteId)
    favoriteNotes.value = favoriteNotes.value.filter(note => note.id !== noteId)
    ElMessage.success('取消收藏成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadNotes = async () => {
  notesLoading.value = true
  try {
    await notesStore.fetchMyNotes()
    notes.value = notesStore.notes
  } catch (error) {
    ElMessage.error('加载笔记列表失败')
  } finally {
    notesLoading.value = false
  }
}

const goToNoteDetail = (id: number) => {
  router.push(`/note/${id}`)
}

const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
}

const goToMessages = (userId: number) => {
  router.push(`/messages?userId=${userId}`)
}

const loadDrafts = async () => {
  draftsLoading.value = true
  try {
    drafts.value = await notesStore.fetchDrafts()
  } catch (error) {
    ElMessage.error('加载草稿列表失败')
  } finally {
    draftsLoading.value = false
  }
}

const goToEditDraft = (draftId: number) => {
  router.push(`/note/create?draftId=${draftId}`)
}

const handleDeleteDraft = async (draftId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个草稿吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await notesStore.deleteNote(draftId)
    if (result.success) {
      drafts.value = drafts.value.filter(draft => draft.id !== draftId)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const response = await api.get('/history')
    histories.value = response.data
  } catch (error) {
    console.error('加载历史记录失败:', error)
    histories.value = []
  } finally {
    historyLoading.value = false
  }
}

const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有历史记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.delete('/history')
    histories.value = []
    ElMessage.success('清空成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const followingCount = computed(() => following.value.length)
const followersCount = computed(() => followers.value.length)

const loadFollowingNotes = async () => {
  followingNotesLoading.value = true
  try {
    const response = await api.get('/notes/following')
    followingNotes.value = response.data
  } catch (error) {
    console.error('加载关注动态失败:', error)
    followingNotes.value = []
  } finally {
    followingNotesLoading.value = false
  }
}

const loadFollowing = async () => {
  followingLoading.value = true
  try {
    const response = await api.get('/follow/following')
    following.value = response.data
  } catch (error) {
    console.error('加载关注列表失败:', error)
    following.value = []
  } finally {
    followingLoading.value = false
  }
}

const loadFollowers = async () => {
  followersLoading.value = true
  try {
    const response = await api.get('/follow/followers')
    followers.value = response.data
  } catch (error) {
    console.error('加载粉丝列表失败:', error)
    followers.value = []
  } finally {
    followersLoading.value = false
  }
}

const handleFollow = async (userId: number) => {
  try {
    await api.post(`/follow/${userId}`)
    ElMessage.success('关注成功')
    await loadFollowing()
    await loadFollowers()
    await loadFollowingNotes()
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.response?.data?.error || '操作失败'
    ElMessage.error(errorMessage)
  }
}

const handleUnfollow = async (userId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消关注吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.delete(`/follow/${userId}`)
    ElMessage.success('取消关注成功')
    await loadFollowing()
    await loadFollowers()
    await loadFollowingNotes()
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMessage = error.response?.data?.message || error.response?.data?.error || '操作失败'
      ElMessage.error(errorMessage)
    }
  }
}

const isFollowingUser = (userId: number) => {
  return following.value.some(u => u.id === userId)
}

const handleToggleVisibility = async (noteId: number, isPublic: boolean) => {
  try {
    const result = await notesStore.updateNoteVisibility(noteId, isPublic)
    if (result.success) {
      ElMessage.success(isPublic ? '已设置为公开' : '已设置为私密')
      // 更新本地状态
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isPublic = isPublic
      }
    } else {
      // 如果失败，恢复原状态
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isPublic = !isPublic
      }
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    // 如果失败，恢复原状态
    const note = notes.value.find(n => n.id === noteId)
    if (note) {
      note.isPublic = !isPublic
    }
    ElMessage.error('操作失败')
  }
}

const handleDeleteNote = async (noteId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await notesStore.deleteNote(noteId)
    if (result.success) {
      notes.value = notes.value.filter(note => note.id !== noteId)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

// 过滤收藏的菜谱
const filteredFavorites = computed(() => {
  if (!favoriteDishSearchKeyword.value.trim()) {
    return favorites.value
  }
  const keyword = favoriteDishSearchKeyword.value.toLowerCase().trim()
  return favorites.value.filter(dish => {
    return (
      dish.name.toLowerCase().includes(keyword) ||
      dish.description?.toLowerCase().includes(keyword) ||
      dish.category?.toLowerCase().includes(keyword) ||
      dish.tags?.some(tag => tag.toLowerCase().includes(keyword)) ||
      dish.ingredients?.some(ing => ing.toLowerCase().includes(keyword))
    )
  })
})

// 过滤收藏的笔记
const filteredFavoriteNotes = computed(() => {
  if (!favoriteNoteSearchKeyword.value.trim()) {
    return favoriteNotes.value
  }
  const keyword = favoriteNoteSearchKeyword.value.toLowerCase().trim()
  return favoriteNotes.value.filter(note => {
    return (
      note.name.toLowerCase().includes(keyword) ||
      note.description?.toLowerCase().includes(keyword) ||
      note.tags?.some(tag => tag.toLowerCase().includes(keyword)) ||
      note.author.username.toLowerCase().includes(keyword)
    )
  })
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 32px 24px;
}

.back-button {
  margin-bottom: 24px;
}

.profile-card {
  margin-bottom: 32px;
  border-radius: 20px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.profile-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #e9ecef;
  padding: 24px 32px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}

.profile-tabs {
  margin-top: 24px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.content-card {
  border-radius: 20px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.content-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #e9ecef;
  padding: 20px 24px;
}

.content-area {
  min-height: 200px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  background: #fff;
}

.item-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: #e0e0e0;
}

.item-card.note-card {
  border-left: 3px solid #409eff;
}

.item-image-wrapper {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f5f5;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.item-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.visibility-switch {
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 8px;
  border-radius: 4px;
  backdrop-filter: blur(10px);
}

.item-info {
  padding: 12px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-description {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.note-date {
  font-size: 12px;
  color: #999;
}

.note-author-name {
  font-size: 12px;
  color: #999;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.draft-card {
  border-left: 3px solid #ff9800;
}

.draft-tag {
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
  justify-content: space-between;
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.user-details h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.user-details p {
  color: #666;
  font-size: 14px;
}

.user-stats {
  display: flex;
  gap: 32px;
  align-items: center;
  padding-left: 32px;
  border-left: 1px solid #e0e0e0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notes-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  color: #333;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
}

.user-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.user-list-item:last-child {
  border-bottom: none;
}

.user-list-info {
  flex: 1;
}

.user-list-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.user-list-email {
  font-size: 12px;
  color: #999;
}

.user-list-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.search-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  color: #333;
}

.count-badge {
  font-size: 14px;
  font-weight: normal;
  color: #666;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

@media (max-width: 768px) {
  .profile-container {
    padding: 20px 16px;
  }

  .profile-card :deep(.el-card__header) {
    padding: 20px;
  }

  .profile-header h2 {
    font-size: 20px;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .user-details h3 {
    font-size: 20px;
  }

  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-header h3 {
    font-size: 18px;
  }

  .search-input {
    width: 100%;
  }

  .notes-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .notes-header h3 {
    font-size: 18px;
  }

  .notes-header .el-button {
    width: 100%;
    min-height: 44px;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .user-details h3 {
    font-size: 20px;
  }

  .user-stats {
    flex-direction: row;
    justify-content: center;
    padding-left: 0;
    border-left: none;
    border-top: 1px solid #e0e0e0;
    padding-top: 16px;
    width: 100%;
    gap: 24px;
  }

  .stat-value {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .items-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .item-name {
    font-size: 14px;
  }

  .item-description {
    font-size: 12px;
  }
}

</style>

