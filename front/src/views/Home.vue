<template>
  <div class="home-container">
    <!-- 头部 -->
    <header class="header">
      <h1 class="title">文案工坊</h1>
      <p class="subtitle">多风格商品文案，一键生成</p>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="tab-content">
        <SingleGenerate v-if="activeTab === 'single'" />
        <BatchGenerate v-if="activeTab === 'batch'" />
        <HistoryList v-if="activeTab === 'history'" />
      </div>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <span>文案工坊 · Copywriting Studio</span>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SingleGenerate from '../components/SingleGenerate.vue'
import BatchGenerate from '../components/BatchGenerate.vue'
import HistoryList from '../components/HistoryList.vue'

const activeTab = ref('single')

const tabs = [
  { key: 'single', label: '单条生成' },
  { key: 'batch', label: '批量生成' },
  { key: 'history', label: '生成记录' }
]
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.header {
  text-align: center;
  padding: 60px 20px 40px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: 2px;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 15px;
  color: #86868b;
  margin: 0;
  letter-spacing: 1px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 自定义 Tab 栏 */
.tab-bar {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 0;
}

.tab-btn {
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 500;
  color: #86868b;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
  font-family: inherit;
}

.tab-btn:hover {
  color: #1d1d1f;
}

.tab-btn.active {
  color: #1d1d1f;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 1.5px;
  background: #1d1d1f;
}

.tab-content {
  background: #fff;
  border: 1px solid #eee;
  border-top: none;
  border-radius: 0 0 8px 8px;
  min-height: 400px;
}

/* 底部 */
.footer {
  text-align: center;
  padding: 32px 20px;
  color: #86868b;
  font-size: 13px;
  letter-spacing: 0.5px;
}

/* 响应式 */
@media (max-width: 768px) {
  .header {
    padding: 40px 16px 28px;
  }

  .title {
    font-size: 26px;
  }

  .main-content {
    padding: 0 16px 32px;
  }

  .tab-btn {
    padding: 10px 16px;
    font-size: 13px;
  }
}
</style>
