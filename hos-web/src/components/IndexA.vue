<template>
  <el-container class="layout-container">
    <el-aside :width="aside_width" class="layout-aside">
      <AsideA :isCollapse="isCollapse"> </AsideA>
    </el-aside>

    <el-container class="layout-main">
      <el-header class="layout-header">
        <HeaderA @doCollapse="doCollapse" :icon="icon"> </HeaderA>
      </el-header>

      <el-main class="layout-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import AsideA from "./AsideA"
import HeaderA from "./HeaderA"

export default {
  components: {AsideA, HeaderA},
  data() {
    return {
      isCollapse: false,
      aside_width: '220px',
      icon: 'el-icon-s-fold'
    }
  },
  methods: {
    handleResize() {
      this.$nextTick(() => {
        // 布局调整逻辑
      });
    },
    doCollapse() {
      this.isCollapse = !this.isCollapse
      if (!this.isCollapse) {
        this.aside_width = '220px'
        this.icon = 'el-icon-s-fold'
      } else {
        this.aside_width = '64px'
        this.icon = 'el-icon-s-unfold'
      }
    }
  },
  mounted() {
    if (this.$route.path !== '/IndexA/profile' &&
        this.$route.path !== '/IndexA/patientInfo') {
      this.$router.push('/IndexA/profile');
    }
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f5f6f8;
}

.layout-aside {
  height: 100vh;
  overflow: hidden;
  transition: width 0.3s;
}

.layout-main {
  height: 100vh;
}

.layout-header {
  padding: 0;
  height: 60px !important;
  line-height: 60px;
}

.layout-content {
  padding: 16px;
  height: calc(100vh - 60px);
  overflow-y: auto;
  background: #f5f6f8;
}
</style>
