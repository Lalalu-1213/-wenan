<template>
  <div class="history-list">
    <div class="history-header">
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索商品名称"
          size="large"
          @keyup.enter="handleSearch"
          clearable
          @clear="handleSearch"
        >
          <template #suffix>
            <el-icon style="cursor: pointer" @click="handleSearch"><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <div class="history-table" v-loading="loading">
      <div v-if="historyList.length === 0 && !loading" class="empty-state">
        <p>暂无生成记录</p>
      </div>

      <div v-for="(item, index) in historyList" :key="index" class="history-item">
        <div class="item-header">
          <span class="item-name">{{ item.productName }}</span>
          <div class="item-tags">
            <span class="style-tag">{{ styleMap[item.style] || item.style }}</span>
            <span v-if="item.fromCache" class="source-tag">缓存</span>
          </div>
        </div>
        <p class="item-content">{{ item.generatedContent }}</p>
        <div class="item-footer">
          <span class="item-time">{{ formatTime(item.createdAt) }}</span>
          <button class="copy-btn" @click="handleCopy(item)">复制</button>
        </div>
      </div>
    </div>

    <div class="history-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="prev, pager, next, sizes"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :pager-count="5"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHistory } from '../api/copywriting'

const styleMap = {
  xiaohongshu: '小红书',
  taobao: '淘宝',
  wechat: '朋友圈',
  title: '标题优化'
}

const loading = ref(false)
const historyList = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getHistory({
      keyword: keyword.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    historyList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchHistory()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchHistory()
}

const handlePageChange = () => {
  fetchHistory()
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handleCopy = (item) => {
  navigator.clipboard.writeText(item.generatedContent)
  ElMessage.success('已复制')
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-list {
  padding: 28px 32px;
}

.history-header {
  margin-bottom: 24px;
}

.search-box {
  max-width: 320px;
}

/* 列表项 */
.history-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-item:first-child {
  padding-top: 0;
}

.history-item:last-child {
  border-bottom: none;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.item-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.style-tag {
  font-size: 11px;
  color: #86868b;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
}

.source-tag {
  font-size: 11px;
  color: #2e7d32;
  background: #e8f5e9;
  padding: 2px 8px;
  border-radius: 3px;
}

.item-content {
  font-size: 14px;
  line-height: 1.7;
  color: #555;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-time {
  font-size: 12px;
  color: #b0b0b0;
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
  opacity: 0;
  transition: opacity 0.2s ease;
}

.history-item:hover .copy-btn {
  opacity: 1;
}

.copy-btn:hover {
  background: #1d1d1f;
  color: #fff;
  border-color: #1d1d1f;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #b0b0b0;
  font-size: 14px;
}

/* 分页 */
.history-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .history-list {
    padding: 20px 16px;
  }

  .search-box {
    max-width: 100%;
  }

  .copy-btn {
    opacity: 1;
  }
}
</style>
