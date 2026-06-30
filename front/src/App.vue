<template>
  <el-config-provider :locale="zhCn">
    <Home />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import Home from './views/Home.vue'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  background-color: #fafafa;
  font-family: 'PingFang SC', 'Helvetica Neue', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #1d1d1f;
}
</style>
