import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export interface Dish {
  id: number
  name: string
  description: string
  image: string
  ingredients: string[]
  steps: string[]
  category: string
  difficulty: string
  cookingTime: number
  servings: number
  isFavorite?: boolean
}

export const useDishesStore = defineStore('dishes', () => {
  const dishes = ref<Dish[]>([])
  const currentDish = ref<Dish | null>(null)
  const loading = ref(false)

  async function fetchDishes(keyword?: string) {
    loading.value = true
    try {
      const params = keyword ? { keyword } : {}
      const response = await api.get('/dishes', { params })
      dishes.value = response.data
    } catch (error) {
      console.error('获取菜品列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  async function fetchDishById(id: number) {
    loading.value = true
    try {
      const response = await api.get(`/dishes/${id}`)
      currentDish.value = response.data
      return response.data
    } catch (error) {
      console.error('获取菜品详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  async function toggleFavorite(dishId: number) {
    try {
      const response = await api.post(`/dishes/${dishId}/favorite`)
      if (currentDish.value && currentDish.value.id === dishId) {
        currentDish.value.isFavorite = response.data.isFavorite
      }
      const dish = dishes.value.find(d => d.id === dishId)
      if (dish) {
        dish.isFavorite = response.data.isFavorite
      }
      return response.data.isFavorite
    } catch (error) {
      console.error('收藏操作失败:', error)
      throw error
    }
  }

  async function fetchFavorites() {
    loading.value = true
    try {
      const response = await api.get('/user/favorites')
      return response.data
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    dishes,
    currentDish,
    loading,
    fetchDishes,
    fetchDishById,
    toggleFavorite,
    fetchFavorites
  }
})

