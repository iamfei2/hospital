<template>
  <div class="default-section">
    <el-card>
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">欢迎使用系统</span>
      </div>
      <div style="text-align: center; padding: 50px;">
        <p>请从左侧菜单选择功能</p>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "DefaultA"
}
</script>

<style scoped>
.default-section {
  padding: 20px;
  min-height: calc(100vh - 70px);
  box-sizing: border-box;
}

.default-section .el-card {
  max-width: 900px;
  margin: 0 auto;
  min-height: calc(100vh - 150px);
}
</style>