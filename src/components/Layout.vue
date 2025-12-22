<template>
  <div class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon><Food /></el-icon>
          <span>美食厨房</span>
        </div>
        <div class="header-center">
          <el-button
            text
            :icon="Food"
            @click="$router.push('/dishes')"
            class="nav-button"
            :class="{ active: $route.path.startsWith('/dishes') || $route.path.startsWith('/category') }"
          >
            菜谱
          </el-button>
          <el-button
            text
            :icon="Document"
            @click="$router.push('/notes')"
            class="nav-button"
            :class="{ active: $route.path.startsWith('/notes') }"
          >
            笔记
          </el-button>
          <el-button
            text
            :icon="Compass"
            @click="$router.push('/discover')"
            class="nav-button"
            :class="{ active: $route.path === '/discover' }"
          >
            发现
          </el-button>
          <el-button
            v-if="userStore.isLoggedIn"
            :icon="Edit"
            @click="$router.push('/note/create')"
            class="publish-button"
            type="primary"
          >
            发布笔记
          </el-button>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜品、食材..."
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button
            v-if="!userStore.isLoggedIn"
            type="primary"
            @click="$router.push('/login')"
          >
            登录
          </el-button>
          <el-dropdown v-else @command="handleCommand" trigger="hover">
            <span class="user-info" @click="goToProfile">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="favorite-dishes">
                  <el-icon><Star /></el-icon>
                  收藏的菜谱
                </el-dropdown-item>
                <el-dropdown-item command="favorite-notes">
                  <el-icon><Star /></el-icon>
                  收藏的笔记
                </el-dropdown-item>
                <el-dropdown-item command="notes">
                  <el-icon><Document /></el-icon>
                  我的笔记
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Food, Search, User, ArrowDown, SwitchButton, Edit, Compass, Star, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')

// 同步路由查询参数到搜索框
onMounted(() => {
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword as string
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  searchKeyword.value = (newKeyword as string) || ''
})

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value.trim() } })
  } else {
    router.push({ path: '/' })
  }
}

const goToProfile = () => {
  router.push('/profile')
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'favorite-dishes') {
    router.push('/profile?tab=favorite-dishes')
  } else if (command === 'favorite-notes') {
    router.push('/profile?tab=favorite-notes')
  } else if (command === 'notes') {
    router.push('/profile?tab=notes')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/')
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 70px !important;
  line-height: 70px;
  width: 100%;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.98);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 12px;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    gap: 8px;
  }

  .header-center {
    gap: 4px;
    order: 3;
    width: 100%;
    justify-content: flex-start;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    margin-top: 8px;
  }

  .header-center:empty {
    display: none;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #ff6b6b;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 12px;
  border-radius: 8px;
  min-height: 44px; /* 触摸友好 */
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .logo {
    font-size: 18px;
    gap: 6px;
    padding: 6px 10px;
  }

  .logo .el-icon {
    font-size: 24px;
  }

  .logo span {
    display: none; /* 移动端隐藏文字，只显示图标 */
  }
}

.logo:hover {
  background: rgba(255, 107, 107, 0.08);
  transform: scale(1.02);
}

.logo .el-icon {
  font-size: 28px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 320px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .search-input {
    width: 100%;
    max-width: 200px;
    flex: 1;
    min-width: 120px;
  }
}

@media (max-width: 480px) {
  .search-input {
    max-width: 150px;
  }
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
}

.dropdown-icon {
  transition: transform 0.3s;
}

.username {
  font-size: 14px;
  color: #333;
}

@media (max-width: 768px) {
  .username {
    display: none; /* 移动端隐藏用户名 */
  }

  .user-info {
    padding: 4px;
  }

  .header-right {
    gap: 8px;
  }
}

.main-content {
  width: 100%;
  padding: 0;
  max-width: none;
}

.nav-button {
  background: transparent !important;
  border: none;
  color: #666;
  font-size: 16px;
  padding: 8px 16px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 44px; /* 触摸友好 */
}

@media (max-width: 768px) {
  .nav-button {
    font-size: 14px;
    padding: 8px 12px;
    min-height: 40px;
  }
}

.nav-button:hover {
  background: rgba(64, 158, 255, 0.1) !important;
  color: #409eff;
  transform: translateY(-2px);
}

.nav-button.active {
  color: #409eff;
  font-weight: 600;
}

.nav-button:active {
  transform: translateY(0);
}

.publish-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%) !important;
  border: none !important;
  color: #fff !important;
  font-size: 16px;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: 20px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  min-height: 44px; /* 触摸友好 */
  white-space: nowrap;
}

@media (max-width: 768px) {
  .publish-button {
    font-size: 14px;
    padding: 8px 16px;
    min-height: 40px;
  }
}

.publish-button:hover {
  background: linear-gradient(135deg, #ff5252 0%, #ff7a2e 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.publish-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}
</style>

