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
        label-width="100px"
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
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useNotesStore } from '@/stores/notes'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import api from '@/api'

const router = useRouter()
const notesStore = useNotesStore()
const noteFormRef = ref<FormInstance>()
const newTag = ref('')

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
  if (typeof response === 'string') {
    noteForm.image = response
  } else if (response && response.body) {
    noteForm.image = response.body
  }
  ElMessage.success('图片上传成功')
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
        isPublic: noteForm.isPublic
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
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.create-note-container h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
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
  background: #409eff;
  color: #fff;
}
</style>

