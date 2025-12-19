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

  async function fetchPublicNotes() {
    loading.value = true
    try {
      const response = await api.get('/notes/public')
      publicNotes.value = response.data
    } catch (error) {
      console.error('获取公开笔记失败:', error)
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

  return {
    notes,
    publicNotes,
    loading,
    createNote,
    fetchMyNotes,
    fetchPublicNotes,
    updateNoteVisibility,
    deleteNote
  }
})

