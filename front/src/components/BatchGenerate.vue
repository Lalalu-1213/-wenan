<template>
  <div class="batch-generate">
    <div class="batch-form">
      <div class="form-row">
        <div class="form-field">
          <label class="field-label">文案风格</label>
          <el-select v-model="form.style" placeholder="选择风格" size="large" style="width: 100%">
            <el-option label="小红书风格" value="xiaohongshu" />
            <el-option label="淘宝风格" value="taobao" />
            <el-option label="朋友圈风格" value="wechat" />
            <el-option label="标题优化" value="title" />
          </el-select>
        </div>
        <div class="form-field">
          <label class="field-label">额外要求</label>
          <el-input v-model="form.additionalRequirements" placeholder="可选" size="large" />
        </div>
      </div>

      <div class="product-section">
        <div class="section-header">
          <span class="section-label">商品列表</span>
          <span class="section-hint">{{ form.products.length }} / 100</span>
        </div>

        <div class="product-list">
          <div v-for="(product, index) in form.products" :key="index" class="product-row">
            <el-input v-model="product.productName" placeholder="商品名称" size="large" />
            <el-input v-model="product.productDesc" placeholder="商品描述（可选）" size="large" />
            <button class="remove-btn" @click="removeProduct(index)" v-if="form.products.length > 1">×</button>
          </div>
        </div>

        <button class="add-btn" @click="addProduct" :disabled="form.products.length >= 100">
          + 添加商品
        </button>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="handleReset">重置</button>
        <button type="button" class="btn btn-primary" @click="handleSubmit" :disabled="loading">
          {{ loading ? '提交中...' : '提交任务' }}
        </button>
      </div>
    </div>

    <!-- 任务状态 -->
    <transition name="result">
      <div v-if="taskId" class="task-section">
        <div class="task-header">
          <span class="task-label">任务进度</span>
          <span class="task-status" :class="taskStatus?.status">
            {{ statusText }}
          </span>
        </div>

        <div class="task-stats">
          <div class="stat">
            <span class="stat-value">{{ taskStatus?.totalCount || 0 }}</span>
            <span class="stat-label">总计</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ taskStatus?.completedCount || 0 }}</span>
            <span class="stat-label">完成</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ taskStatus?.failedCount || 0 }}</span>
            <span class="stat-label">失败</span>
          </div>
        </div>

        <div v-if="taskStatus?.results?.length" class="results-list">
          <div v-for="(item, index) in taskStatus.results" :key="index" class="result-card">
            <div class="result-card-header">
              <span class="result-card-title">{{ item.productName }}</span>
              <span class="result-card-style">{{ item.style }}</span>
            </div>
            <p class="result-card-text">{{ item.generatedContent }}</p>
            <button class="copy-btn" @click="handleCopyItem(item)">复制</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { submitBatchTask, getBatchTaskStatus } from '../api/copywriting'

const loading = ref(false)
const taskId = ref(null)
const taskStatus = ref(null)
let pollTimer = null

const form = reactive({
  style: 'xiaohongshu',
  additionalRequirements: '',
  products: [
    { productName: '', productDesc: '' }
  ]
})

const statusText = computed(() => {
  const status = taskStatus.value?.status
  const map = { pending: '等待中', processing: '处理中', completed: '已完成', failed: '失败' }
  return map[status] || status
})

const addProduct = () => {
  if (form.products.length < 100) {
    form.products.push({ productName: '', productDesc: '' })
  }
}

const removeProduct = (index) => {
  form.products.splice(index, 1)
}

const handleSubmit = async () => {
  const validProducts = form.products.filter(p => p.productName.trim())
  if (validProducts.length === 0) {
    ElMessage.warning('请至少添加一个商品')
    return
  }

  loading.value = true
  try {
    const res = await submitBatchTask({
      products: validProducts,
      style: form.style,
      additionalRequirements: form.additionalRequirements
    })

    taskId.value = res.data.taskId
    taskStatus.value = res.data
    ElMessage.success('任务已提交')
    startPolling()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    await refreshStatus()
    if (taskStatus.value?.status === 'completed' || taskStatus.value?.status === 'failed') {
      clearInterval(pollTimer)
    }
  }, 2000)
}

const refreshStatus = async () => {
  if (!taskId.value) return
  try {
    const res = await getBatchTaskStatus(taskId.value)
    taskStatus.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleReset = () => {
  form.products = [{ productName: '', productDesc: '' }]
  form.additionalRequirements = ''
  taskId.value = null
  taskStatus.value = null
  if (pollTimer) clearInterval(pollTimer)
}

const handleCopyItem = (item) => {
  navigator.clipboard.writeText(item.generatedContent)
  ElMessage.success('已复制')
}
</script>

<style scoped>
.batch-generate {
  padding: 28px 32px;
}

.batch-form {
  margin-bottom: 28px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
  color: #1d1d1f;
}

.product-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.section-hint {
  font-size: 12px;
  color: #b0b0b0;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.product-row {
  display: grid;
  grid-template-columns: 1fr 1fr 36px;
  gap: 10px;
  align-items: center;
}

.remove-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  background: #fff;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  border-color: #d00;
  color: #d00;
}

.add-btn {
  padding: 8px 16px;
  font-size: 13px;
  color: #86868b;
  background: none;
  border: 1px dashed #d0d0d0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.add-btn:hover {
  border-color: #1d1d1f;
  color: #1d1d1f;
}

.add-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 任务区域 */
.task-section {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.task-label {
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.task-status {
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 3px;
}

.task-status.pending,
.task-status.processing {
  color: #b07d00;
  background: #fef9e7;
}

.task-status.completed {
  color: #2e7d32;
  background: #e8f5e9;
}

.task-status.failed {
  color: #c62828;
  background: #ffebee;
}

.task-stats {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #eee;
}

.stat {
  flex: 1;
  padding: 16px 20px;
  text-align: center;
  border-right: 1px solid #eee;
}

.stat:last-child {
  border-right: none;
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #86868b;
}

.results-list {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 16px;
}

.result-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.result-card-style {
  font-size: 11px;
  color: #86868b;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
}

.result-card-text {
  font-size: 14px;
  line-height: 1.7;
  color: #444;
  white-space: pre-wrap;
  margin: 0 0 10px 0;
}

.copy-btn {
  font-size: 12px;
  color: #1d1d1f;
  background: none;
  border: 1px solid #ddd;
  padding: 3px 12px;
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
}

/* 响应式 */
@media (max-width: 768px) {
  .batch-generate {
    padding: 20px 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .product-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .task-stats {
    flex-direction: column;
  }

  .stat {
    border-right: none;
    border-bottom: 1px solid #eee;
  }

  .stat:last-child {
    border-bottom: none;
  }
}
</style>
