<template>
  <div class="create-note-container">
    <el-card>
      <template #header>
        <h2>发布做菜笔记</h2>
      </template>

      <el-form
        ref="noteFormRef"
        :model="noteForm"
        :rules="rules"
        label-width="120px"
        class="note-form"
      >
        <el-form-item label="菜名" prop="name">
          <el-input
            v-model="noteForm.name"
            placeholder="请输入菜名"
            size="large"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="noteForm.description"
            type="textarea"
            :rows="3"
            placeholder="简单描述一下这道菜..."
          />
        </el-form-item>

        <el-form-item label="图片">
          <div class="image-upload">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <img v-if="noteForm.image" :src="getImageUrl(noteForm.image)" class="preview-image" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">点击上传图片（可选）</p>
          </div>
        </el-form-item>

        <el-form-item label="用料">
          <div class="ingredients-section">
            <div v-for="(ingredient, index) in noteForm.ingredients" :key="index" class="ingredient-item">
              <el-input
                v-model="noteForm.ingredients[index]"
                placeholder="例如：鸡胸肉 300g"
                style="flex: 1"
              />
              <el-button
                type="danger"
                :icon="Delete"
                circle
                @click="removeIngredient(index)"
              />
            </div>
            <el-button
              type="primary"
              :icon="Plus"
              text
              @click="addIngredient"
            >
              添加用料
            </el-button>
            <div class="quick-tags">
              <span class="tag-label">快速选择：</span>
              <el-tag
                v-for="tag in commonIngredients"
                :key="tag"
                class="quick-tag"
                @click="addQuickIngredient(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="做法">
          <div class="steps-section">
            <div v-for="(step, index) in noteForm.steps" :key="index" class="step-item">
              <div class="step-number">{{ index + 1 }}</div>
              <el-input
                v-model="noteForm.steps[index]"
                type="textarea"
                :rows="2"
                placeholder="描述制作步骤..."
                style="flex: 1"
              />
              <el-button
                type="danger"
                :icon="Delete"
                circle
                @click="removeStep(index)"
              />
            </div>
            <el-button
              type="primary"
              :icon="Plus"
              text
              @click="addStep"
            >
              添加步骤
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <div class="tags-section">
            <el-tag
              v-for="(tag, index) in noteForm.tags"
              :key="index"
              closable
              @close="removeTag(index)"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-model="newTag"
              placeholder="输入标签后按回车添加"
              size="small"
              style="width: 200px"
              @keyup.enter="addTag"
            />
            <div class="quick-tags">
              <span class="tag-label">常用标签：</span>
              <el-tag
                v-for="tag in commonTags"
                :key="tag"
                class="quick-tag"
                @click="addQuickTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="可见性">
          <el-radio-group v-model="noteForm.isPublic">
            <el-radio :label="true">公开（其他用户可见）</el-radio>
            <el-radio :label="false">私密（仅自己可见）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="notesStore.loading"
            @click="handleSubmit"
          >
            发布笔记
          </el-button>
          <el-button
            size="large"
            :loading="savingDraft"
            @click="handleSaveDraft"
          >
            保存草稿
          </el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNotesStore } from '@/stores/notes'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const router = useRouter()
const route = useRoute()
const notesStore = useNotesStore()
const noteFormRef = ref<FormInstance>()
const newTag = ref('')
const savingDraft = ref(false)
const draftId = ref<number | null>(null)

const noteForm = reactive({
  name: '',
  description: '',
  image: '',
  ingredients: [''] as string[],
  steps: [''] as string[],
  tags: [] as string[],
  isPublic: true
})

const commonIngredients = [
  '鸡胸肉', '鸡蛋', '番茄', '土豆', '洋葱', '大蒜', '生姜', '大葱',
  '生抽', '老抽', '料酒', '盐', '糖', '醋', '油'
]

const commonTags = [
  '家常菜', '快手菜', '下饭菜', '清淡', '麻辣', '酸甜', '香辣', '营养'
]

const rules: FormRules = {
  name: [
    { required: true, message: '请输入菜名', trigger: 'blur' }
  ]
}

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/upload/image`
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const handleUploadSuccess = (response: any) => {
  // 后端返回格式: { url: "/uploads/xxx.jpg" }
  // Element Plus upload 组件的 on-success 回调接收的 response 是服务器返回的响应体
  if (response && response.url) {
    noteForm.image = response.url
    ElMessage.success('图片上传成功')
  } else if (typeof response === 'string') {
    noteForm.image = response
    ElMessage.success('图片上传成功')
  } else if (response && response.body) {
    noteForm.image = response.body
    ElMessage.success('图片上传成功')
  } else {
    console.error('Upload response format error:', response)
    ElMessage.error('图片上传成功，但无法获取图片路径，请重试')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const addIngredient = () => {
  noteForm.ingredients.push('')
}

const removeIngredient = (index: number) => {
  noteForm.ingredients.splice(index, 1)
}

const addQuickIngredient = (ingredient: string) => {
  if (!noteForm.ingredients.includes(ingredient)) {
    noteForm.ingredients.push(ingredient)
  }
}

const addStep = () => {
  noteForm.steps.push('')
}

const removeStep = (index: number) => {
  noteForm.steps.splice(index, 1)
}

const addTag = () => {
  if (newTag.value.trim() && !noteForm.tags.includes(newTag.value.trim())) {
    noteForm.tags.push(newTag.value.trim())
    newTag.value = ''
  }
}

const addQuickTag = (tag: string) => {
  if (!noteForm.tags.includes(tag)) {
    noteForm.tags.push(tag)
  }
}

const removeTag = (index: number) => {
  noteForm.tags.splice(index, 1)
}

// 加载草稿数据
onMounted(async () => {
  const draftIdParam = route.query.draftId as string
  if (draftIdParam) {
    try {
      const draft = await notesStore.getNoteById(parseInt(draftIdParam))
      if (draft && draft.isDraft) {
        draftId.value = draft.id
        noteForm.name = draft.name || ''
        noteForm.description = draft.description || ''
        noteForm.image = draft.image || ''
        noteForm.ingredients = draft.ingredients && draft.ingredients.length > 0 
          ? draft.ingredients 
          : ['']
        noteForm.steps = draft.steps && draft.steps.length > 0 
          ? draft.steps 
          : ['']
        noteForm.tags = draft.tags || []
        noteForm.isPublic = draft.isPublic ?? true
        ElMessage.info('已加载草稿内容')
      }
    } catch (error) {
      console.error('加载草稿失败:', error)
    }
  }
})

const handleSaveDraft = async () => {
  savingDraft.value = true
  try {
    const data = {
      name: noteForm.name || undefined,
      description: noteForm.description || undefined,
      image: noteForm.image || undefined,
      ingredients: noteForm.ingredients.filter(i => i.trim()),
      steps: noteForm.steps.filter(s => s.trim()),
      tags: noteForm.tags,
      isPublic: noteForm.isPublic
    }

    let result
    if (draftId.value) {
      // 更新现有草稿
      result = await notesStore.updateDraft(draftId.value, { ...data, isDraft: true })
    } else {
      // 创建新草稿
      result = await notesStore.saveDraft(data)
      if (result.success && result.data) {
        draftId.value = result.data.id
      }
    }

    if (result.success) {
      ElMessage.success('草稿保存成功')
    } else {
      ElMessage.error(result.message || '保存草稿失败')
    }
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const handleSubmit = async () => {
  if (!noteFormRef.value) return

  await noteFormRef.value.validate(async (valid) => {
    if (valid) {
      const data = {
        name: noteForm.name,
        description: noteForm.description || undefined,
        image: noteForm.image || undefined,
        ingredients: noteForm.ingredients.filter(i => i.trim()),
        steps: noteForm.steps.filter(s => s.trim()),
        tags: noteForm.tags,
        isPublic: noteForm.isPublic,
        isDraft: false // 发布时设置为非草稿
      }

      // 如果是更新草稿，先更新再发布
      if (draftId.value) {
        const updateResult = await notesStore.updateDraft(draftId.value, { ...data, isDraft: false })
        if (updateResult.success) {
          ElMessage.success('笔记发布成功')
          router.push('/profile')
          return
        } else {
          ElMessage.error(updateResult.message || '发布失败')
          return
        }
      }

      const result = await notesStore.createNote(data)
      if (result.success) {
        ElMessage.success('笔记发布成功')
        router.push('/profile')
      } else {
        ElMessage.error(result.message || '发布失败')
      }
    }
  })
}
</script>

<style scoped>
.create-note-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: calc(100vh - 200px);
}

.create-note-container :deep(.el-card) {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: none;
  transition: none;
}

.create-note-container :deep(.el-card:hover) {
  box-shadow: none;
  transform: none;
}

.create-note-container :deep(.el-card__body) {
  padding: 32px 40px;
}

.create-note-container :deep(.el-card__header) {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 24px 40px;
}

.create-note-container h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: -0.3px;
}

.image-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.preview-image {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.upload-icon {
  font-size: 48px;
  color: #ccc;
  cursor: pointer;
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  transition: all 0.3s;
}

.upload-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.ingredients-section,
.steps-section,
.tags-section {
  width: 100%;
}

.ingredient-item,
.step-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.step-item {
  align-items: center;
}

.step-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.quick-tags {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.tag-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.quick-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  background: #ff6b6b;
  color: #fff;
  border-color: #ff6b6b;
}

.note-form :deep(.el-form-item) {
  margin-bottom: 28px;
}

.note-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-right: 20px;
}

.note-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.note-form :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.note-form :deep(.el-radio-group) {
  display: flex;
  gap: 24px;
}

/* 主要按钮样式 */
.note-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8c42 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  transition: all 0.3s;
}

.note-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #ff5252 0%, #ff7a2e 100%);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.note-form :deep(.el-button--primary:active) {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b35 100%);
}

.note-form :deep(.el-button--primary.is-disabled) {
  background: #ccc;
  color: #fff;
}

/* 取消按钮样式 */
.note-form :deep(.el-button:not(.el-button--primary):not(.el-button--danger)) {
  border-color: #d1d5db;
  color: #6b7280;
}

.note-form :deep(.el-button:not(.el-button--primary):not(.el-button--danger):hover) {
  border-color: #9ca3af;
  color: #4b5563;
  background: #f9fafb;
}

/* 文本按钮样式 */
.note-form :deep(.el-button--text.el-button--primary) {
  color: #ff6b6b;
  padding: 8px 12px;
}

.note-form :deep(.el-button--text.el-button--primary:hover) {
  color: #ff5252;
  background: rgba(255, 107, 107, 0.1);
}

@media (max-width: 768px) {
  .create-note-container {
    padding: 20px 16px;
  }

  .create-note-container :deep(.el-card__body) {
    padding: 24px 20px;
  }

  .create-note-container :deep(.el-card__header) {
    padding: 20px;
  }

  .create-note-container h2 {
    font-size: 20px;
  }

  .note-form :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  .note-form :deep(.el-form-item__label) {
    font-size: 14px;
    padding-bottom: 8px;
  }

  .note-form :deep(.el-input),
  .note-form :deep(.el-textarea) {
    font-size: 16px; /* 防止iOS自动缩放 */
  }

  .note-form :deep(.el-button) {
    min-height: 44px; /* 触摸友好 */
    width: 100%;
    margin-bottom: 12px;
  }

  .note-form :deep(.el-button + .el-button) {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .create-note-container {
    padding: 16px 12px;
  }

  .create-note-container :deep(.el-card__body) {
    padding: 20px 16px;
  }

  .create-note-container :deep(.el-card__header) {
    padding: 16px;
  }

  .note-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left !important;
    padding-bottom: 8px;
  }
}
</style>

