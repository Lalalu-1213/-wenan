<template>
  <transition name="fade">
    <div v-if="visible" class="overlay">
      <div class="overlay-content">
        <div class="loading-indicator">
          <div class="dot"></div>
          <div class="dot"></div>
          <div class="dot"></div>
        </div>
        <p class="loading-text">{{ statusText }}</p>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const statusText = ref('正在生成...')
let timer = null
let step = 0

const messages = [
  '正在生成...',
  '分析商品特点...',
  '组织文案结构...',
  '优化文字表达...',
  '即将完成...'
]

watch(() => props.visible, (val) => {
  if (val) {
    step = 0
    statusText.value = messages[0]
    timer = setInterval(() => {
      step++
      if (step < messages.length) {
        statusText.value = messages[step]
      }
    }, 1500)
  } else {
    clearInterval(timer)
    timer = null
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.overlay-content {
  text-align: center;
}

.loading-indicator {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-bottom: 16px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1d1d1f;
  animation: blink 1.4s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 80%, 100% {
    opacity: 0.2;
  }
  40% {
    opacity: 1;
  }
}

.loading-text {
  font-size: 14px;
  color: #86868b;
  margin: 0;
  letter-spacing: 0.5px;
}

/* 过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
