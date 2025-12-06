<script setup lang="ts">
</script>
<template>
  <el-container>
    <el-aside class="aside-area">
      <el-scrollbar>
        <el-menu
            :router="true"
            :ellipsis="false"
            mode="vertical"
            style="height: 100%;"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/message-encoding">消息编码</el-menu-item>
          <el-menu-item index="/message-decoding">消息解码</el-menu-item>
          <el-menu-item index="/dynamic-encoding">动态编码</el-menu-item>
          <el-menu-item index="/field-test">field-test</el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header class="header-area">
        <h3>Xtream-Codec JT/T 808 服务端调试</h3>
      </el-header>
      <el-main class="main-area">
        <RouterView></RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
$aside-width: 130px;
$header-height: 50px;
::v-deep(.aside-area) {
  .el-scrollbar__view {
    height: 100%;
  }

  position: fixed;
  top: 0;
  left: 0;
  height: 100%;
  width: $aside-width;
}

.header-area {
  position: fixed;
  top: 0;
  left: $aside-width;
  width: 100%;
  height: $header-height;

  border-bottom: 1px solid var(--el-border-color);
  display: flex;
  justify-content: flex-start;
  align-items: center;
  z-index: 999999;
}

.main-area {
  z-index: 10;
  padding: 10px 10px;
  margin: $header-height 0 0 $aside-width;
  width: calc(100vw - #{$aside-width});
  height: calc(100vh - #{$header-height});
  overflow-y: scroll;
}
</style>
