import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export interface Comment {
  id: number
  content: string
  createdAt: string
  author: {
    id: number
    username: string
    email: string
    avatar?: string
  }
  parentId: number | null
  replies: Comment[]
}

export const useCommentsStore = defineStore('comments', () => {
  const comments = ref<Comment[]>([])
  const loading = ref(false)

  async function fetchComments(dishId: number) {
    loading.value = true
    try {
      const response = await api.get(`/dishes/${dishId}/comments`)
      comments.value = response.data
    } catch (error) {
      console.error('获取评论失败:', error)
    } finally {
      loading.value = false
    }
  }

  async function fetchNoteComments(noteId: number) {
    loading.value = true
    try {
      const response = await api.get(`/notes/${noteId}/comments`)
      comments.value = response.data
    } catch (error) {
      console.error('获取评论失败:', error)
    } finally {
      loading.value = false
    }
  }

  async function createComment(dishId: number, content: string, parentId?: number) {
    try {
      const response = await api.post(`/dishes/${dishId}/comments`, {
        content,
        parentId: parentId || null
      })
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '评论失败' }
    }
  }

  async function createNoteComment(noteId: number, content: string, parentId?: number) {
    try {
      const response = await api.post(`/notes/${noteId}/comments`, {
        content,
        parentId: parentId || null
      })
      return { success: true, data: response.data }
    } catch (error: any) {
      console.error('创建笔记评论失败:', error)
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          '评论失败'
      return { success: false, message: errorMessage }
    }
  }

  async function deleteComment(dishId: number, commentId: number) {
    try {
      await api.delete(`/dishes/${dishId}/comments/${commentId}`)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '删除失败' }
    }
  }

  async function deleteNoteComment(noteId: number, commentId: number) {
    try {
      await api.delete(`/notes/${noteId}/comments/${commentId}`)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.response?.data?.message || '删除失败' }
    }
  }

  return {
    comments,
    loading,
    fetchComments,
    fetchNoteComments,
    createComment,
    createNoteComment,
    deleteComment,
    deleteNoteComment
  }
})

