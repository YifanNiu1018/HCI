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
            :icon="Compass"
            @click="$router.push('/discover')"
            class="discover-button"
          >
            发现
          </el-button>
          <el-button
            v-if="userStore.isLoggedIn"
            text
            :icon="Edit"
            @click="$router.push('/note/create')"
            class="create-note-button"
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
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useDishesStore } from '../stores/dishes'
import { Food, Search, User, ArrowDown, SwitchButton, Edit, Compass, Star, Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const dishesStore = useDishesStore()
const searchKeyword = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Home', query: { keyword: searchKeyword.value } })
  }
}

const goToProfile = () => {
  router.push('/profile')
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'favorites') {
    router.push('/profile?tab=favorites')
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 64px !important;
  line-height: 64px;
  width: 100%;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 300px;
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

.main-content {
  width: 100%;
  padding: 0;
  max-width: none;
}

.discover-button,
.create-note-button {
  background: transparent !important;
  border: none;
  color: #333;
  font-size: 16px;
  padding: 8px 16px;
  transition: all 0.3s;
}

.discover-button:hover,
.create-note-button:hover {
  background: rgba(64, 158, 255, 0.1) !important;
  color: #409eff;
  transform: translateY(-2px);
}

.discover-button:active,
.create-note-button:active {
  transform: translateY(0);
}
</style>

