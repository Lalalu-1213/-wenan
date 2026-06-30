<template>
  <div class="single-generate">
    <!-- 加载遮罩 -->
    <GeneratingOverlay :visible="loading" />

    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="form-wrapper">
      <div class="form-grid">
        <!-- 左侧输入区 -->
        <div class="form-left">
          <div class="section-label">商品信息</div>

          <el-form-item label="商品名称" prop="productName">
            <el-input
              v-model="form.productName"
              placeholder="如：无线蓝牙耳机"
              size="large"
            />
          </el-form-item>

          <el-form-item label="商品描述">
            <el-input
              v-model="form.productDesc"
              type="textarea"
              :rows="3"
              placeholder="简要描述商品特点..."
            />
          </el-form-item>

          <el-form-item label="核心卖点">
            <el-input
              v-model="form.sellingPoints"
              placeholder="用逗号分隔，如：降噪, 长续航, 舒适"
              size="large"
            />
          </el-form-item>

          <el-form-item label="目标人群">
            <el-input
              v-model="form.targetAudience"
              placeholder="如：年轻上班族、学生党"
              size="large"
            />
          </el-form-item>
        </div>

        <!-- 右侧配置区 -->
        <div class="form-right">
          <div class="section-label">生成配置</div>

          <el-form-item label="文案风格" prop="style">
            <div class="style-options">
              <div
                v-for="style in styleOptions"
                :key="style.value"
                class="style-option"
                :class="{ active: form.style === style.value }"
                @click="form.style = style.value"
              >
                <div class="style-name">{{ style.label }}</div>
                <div class="style-desc">{{ style.desc }}</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="额外要求">
            <el-input
              v-model="form.additionalRequirements"
              type="textarea"
              :rows="3"
              placeholder="其他特殊要求..."
            />
          </el-form-item>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="handleReset">重置</button>
        <button type="button" class="btn btn-primary" @click="handleGenerate">生成文案</button>
      </div>
    </el-form>

    <!-- 生成结果 -->
    <transition name="result">
      <div v-if="result" class="result-section">
        <div class="result-header">
          <span class="result-label">生成结果</span>
          <div class="result-meta">
            <span v-if="result.fromCache" class="cache-badge">缓存</span>
            <button class="copy-btn" @click="handleCopy">复制</button>
          </div>
        </div>
        <div class="result-body">
          <p class="result-text">{{ result.generatedContent }}</p>
        </div>
        <div class="result-footer">
          {{ formatTime(result.generatedAt) }}
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { generateCopywriting } from '../api/copywriting'
import GeneratingOverlay from './GeneratingOverlay.vue'

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const form = reactive({
  productName: '',
  productDesc: '',
  sellingPoints: '',
  targetAudience: '',
  style: 'xiaohongshu',
  additionalRequirements: ''
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  style: [{ required: true, message: '请选择文案风格', trigger: 'change' }]
}

const styleOptions = [
  { value: 'xiaohongshu', label: '小红书', desc: '种草风格，亲切活泼' },
  { value: 'taobao', label: '淘宝', desc: '突出卖点，促进转化' },
  { value: 'wechat', label: '朋友圈', desc: '生活化语气，简短精炼' },
  { value: 'title', label: '标题优化', desc: '提取关键词，提升搜索' }
]

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    result.value = null

    const res = await generateCopywriting({
      product: {
        productName: form.productName,
        productDesc: form.productDesc,
        sellingPoints: form.sellingPoints,
        targetAudience: form.targetAudience
      },
      style: form.style,
      additionalRequirements: form.additionalRequirements
    })

    result.value = res.data
    ElMessage.success('生成完成')
  } catch (error) {
    if (error !== false) {
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
  result.value = null
}

const handleCopy = () => {
  if (result.value?.generatedContent) {
    navigator.clipboard.writeText(result.value.generatedContent)
    ElMessage.success('已复制')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.single-generate {
  padding: 28px 32px;
}

.form-wrapper {
  margin-bottom: 28px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.form-left,
.form-right {
  display: flex;
  flex-direction: column;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin-bottom: 20px;
}

/* 风格选项 */
.style-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.style-option {
  padding: 14px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.style-option:hover {
  border-color: #c0c0c0;
}

.style-option.active {
  border-color: #1d1d1f;
  background: #fafafa;
}

.style-name {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 2px;
}

.style-desc {
  font-size: 12px;
  color: #86868b;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 10px 28px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-family: inherit;
}

.btn-secondary {
  background: #f5f5f5;
  color: #6e6e6e;
}

.btn-secondary:hover {
  background: #ebebeb;
}

.btn-primary {
  background: #1d1d1f;
  color: #fff;
}

.btn-primary:hover {
  background: #333;
}

/* 结果区域 */
.result-section {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.result-label {
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cache-badge {
  font-size: 11px;
  color: #86868b;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 3px;
}

.copy-btn {
  font-size: 13px;
  color: #1d1d1f;
  background: none;
  border: 1px solid #ddd;
  padding: 4px 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.copy-btn:hover {
  background: #1d1d1f;
  color: #fff;
  border-color: #1d1d1f;
}

.result-body {
  padding: 24px 20px;
}

.result-text {
  font-size: 15px;
  line-height: 1.85;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

.result-footer {
  padding: 12px 20px;
  font-size: 12px;
  color: #b0b0b0;
  border-top: 1px solid #f0f0f0;
}

/* 过渡动画 */
.result-enter-active {
  transition: all 0.35s ease;
}

.result-leave-active {
  transition: all 0.25s ease;
}

.result-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.result-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 响应式 */
@media (max-width: 768px) {
  .single-generate {
    padding: 20px 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
