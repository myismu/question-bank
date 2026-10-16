<script lang="ts" setup>
import zhCn from "element-plus/es/locale/lang/zh-cn" // Element Plus 中文包

// 初始化通知
// initStarNotification()
// initStoreNotification()
</script>

<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<style>
html {
  overflow-y: scroll;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
