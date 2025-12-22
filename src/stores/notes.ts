import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export interface Note {
  id: number
  name: string
  description: string
  image: string
  ingredients: string[]
  steps: string[]
  tags: string[]
  isPublic: boolean
  createdAt: string
  isFavorite?: boolean
  author: {
    id: number
    username: string
    email: string
    avatar?: string
  }
}

export const useNotesStore = defineStore('notes', () => {
  const notes = ref<Note[]>([])
  const publicNotes = ref<Note[]>([])
  const loading = ref(false)

  async function createNote(noteData: {
    name: string
    description?: string
    image?: string
    ingredients?: string[]
    steps?: string[]
    tags?: string[]
    isPublic?: boolean
  }) {
    loading.value = true
    try {
      const response = await api.post('/notes', noteData)
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '创建笔记失败' }
    } finally {
      loading.value = false
    }
  }

  async function fetchMyNotes() {
    loading.value = true
    try {
      const response = await api.get('/notes/my')
      notes.value = response.data
    } catch (error) {
      console.error('获取我的笔记失败:', error)
    } finally {
      loading.value = false
    }
  }

  async function fetchPublicNotes(keyword?: string) {
    loading.value = true
    try {
      let response
      if (keyword && keyword.trim()) {
        // 使用搜索接口
        response = await api.get('/notes/public/search', { params: { keyword: keyword.trim() } })
      } else {
        // 获取所有公开笔记
        response = await api.get('/notes/public')
      }
      // 确保返回的是数组
      if (Array.isArray(response.data)) {
        publicNotes.value = response.data
      } else {
        publicNotes.value = []
      }
    } catch (error) {
      console.error('获取公开笔记失败:', error)
      publicNotes.value = []
    } finally {
      loading.value = false
    }
  }

  async function updateNoteVisibility(noteId: number, isPublic: boolean) {
    try {
      const response = await api.put(`/notes/${noteId}/visibility`, { isPublic })
      // 更新本地状态
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        note.isPublic = isPublic
      }
      // 如果改为私密，从公开列表中移除；如果改为公开，添加到公开列表
      if (isPublic) {
        if (!publicNotes.value.find(n => n.id === noteId)) {
          publicNotes.value.push(response.data)
        } else {
          const publicNote = publicNotes.value.find(n => n.id === noteId)
          if (publicNote) {
            publicNote.isPublic = true
          }
        }
      } else {
        publicNotes.value = publicNotes.value.filter(n => n.id !== noteId)
      }
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '更新失败' }
    }
  }

  async function deleteNote(noteId: number) {
    try {
      await api.delete(`/notes/${noteId}`)
      notes.value = notes.value.filter(note => note.id !== noteId)
      publicNotes.value = publicNotes.value.filter(note => note.id !== noteId)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '删除失败' }
    }
  }

  async function toggleFavorite(noteId: number) {
    try {
      const response = await api.post(`/notes/${noteId}/favorite`)
      const isFavorite = response.data.isFavorite
      
      // 更新公开笔记列表中的收藏状态
      const publicNote = publicNotes.value.find(n => n.id === noteId)
      if (publicNote) {
        publicNote.isFavorite = isFavorite
      }
      
      // 更新我的笔记列表中的收藏状态（如果是公开的）
      const myNote = notes.value.find(n => n.id === noteId)
      if (myNote) {
        myNote.isFavorite = isFavorite
      }
      
      return { success: true, isFavorite }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '操作失败' }
    }
  }

  async function getNoteById(id: number) {
    loading.value = true
    try {
      const response = await api.get(`/notes/${id}`)
      return response.data
    } catch (error: any) {
      console.error('获取笔记详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    notes,
    publicNotes,
    loading,
    createNote,
    fetchMyNotes,
    fetchPublicNotes,
    updateNoteVisibility,
    deleteNote,
    toggleFavorite,
    getNoteById
  }
})

