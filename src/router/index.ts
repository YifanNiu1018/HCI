import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dish/:id',
    name: 'DishDetail',
    component: () => import('@/views/DishDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/note/create',
    name: 'CreateNote',
    component: () => import('@/views/CreateNote.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/note/:id',
    name: 'NoteDetail',
    component: () => import('@/views/NoteDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/discover',
    name: 'Discover',
    component: () => import('@/views/Discover.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router

