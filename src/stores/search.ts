import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'
import type { Dish } from './dishes'
import type { Note } from './notes'

export interface SearchResult {
  dishes: Dish[]
  notes: Note[]
}

export const useSearchStore = defineStore('search', () => {
  const results = ref<SearchResult>({ dishes: [], notes: [] })
  const loading = ref(false)

  async function search(keyword?: string) {
    loading.value = true
    try {
      const params = keyword ? { keyword } : {}
      const response = await api.get('/search', { params })
      results.value = response.data
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    results,
    loading,
    search
  }
})

