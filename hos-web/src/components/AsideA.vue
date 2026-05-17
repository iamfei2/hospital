<template>
  <div class="aside-container">
    <div class="logo-area" :class="{ 'logo-collapsed': isCollapse }">
      <div class="logo-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
        </svg>
      </div>
      <transition name="fade">
        <span v-if="!isCollapse" class="logo-text">医院检查管理系统</span>
      </transition>
    </div>

    <el-menu
      background-color="#0f1729"
      text-color="#8b95a5"
      active-text-color="#ffffff"
      :collapse="isCollapse"
      :collapse-transition="false"
      class="main-menu"
      :default-active="$route.path"
      router>

      <el-menu-item index="/IndexA/profile" class="menu-item">
        <i class="el-icon-user"></i>
        <span slot="title">个人信息</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/medicalOrder" class="menu-item">
        <i class="el-icon-document"></i>
        <span slot="title">医嘱信息</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/labResult" class="menu-item">
        <i class="el-icon-document-checked"></i>
        <span slot="title">检验结果</span>
      </el-menu-item>

      <el-submenu index="/IndexA/check" class="submenu-item">
        <template slot="title">
          <i class="el-icon-s-data"></i>
          <span>检查管理</span>
        </template>
        <el-menu-item index="/IndexA/check/ct" class="submenu-child">CT检查</el-menu-item>
        <el-menu-item index="/IndexA/check/mri" class="submenu-child">核磁检查</el-menu-item>
        <el-menu-item index="/IndexA/check/colonoscopy" class="submenu-child">肠镜检查</el-menu-item>
      </el-submenu>

      <el-menu-item index="/IndexA/pathology" class="menu-item">
        <i class="el-icon-s-order"></i>
        <span slot="title">病理检查</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/patientInfo" class="menu-item">
        <i class="el-icon-user-solid"></i>
        <span slot="title">患者信息</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/statistics" class="menu-item">
        <i class="el-icon-data-analysis"></i>
        <span slot="title">统计分析</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/operationLog" class="menu-item">
        <i class="el-icon-tickets"></i>
        <span slot="title">操作日志</span>
      </el-menu-item>

      <el-submenu index="/IndexA/admin" v-if="isAdmin" class="submenu-item">
        <template slot="title">
          <i class="el-icon-setting"></i>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/IndexA/labItemDict" class="submenu-child">参考范围管理</el-menu-item>
        <el-menu-item index="/IndexA/warningRule" class="submenu-child">预警规则配置</el-menu-item>
      </el-submenu>

      <el-menu-item index="/IndexA/warningRecord" class="menu-item">
        <i class="el-icon-bell"></i>
        <span slot="title">预警记录</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/backup" class="menu-item">
        <i class="el-icon-upload2"></i>
        <span slot="title">数据备份</span>
      </el-menu-item>

      <el-menu-item index="/IndexA/searchTemplate" class="menu-item">
        <i class="el-icon-collection-tag"></i>
        <span slot="title">查询模板</span>
      </el-menu-item>

    </el-menu>
  </div>
</template>

<script>
export default {
  name: "AsideA",
  data() {
    return {}
  },
  computed: {
    isAdmin() {
      const userStr = sessionStorage.getItem('CurUser');
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          return user.role === 'admin';
        } catch (e) {
          return false;
        }
      }
      return false;
    }
  },
  props: {
    isCollapse: Boolean
  },
}
</script>

<style scoped>
.aside-container {
  height: 100%;
  background: #0f1729;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  background: linear-gradient(180deg, rgba(255,255,255,0.03) 0%, transparent 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.3s ease;
}

.logo-area.logo-collapsed {
  justify-content: center;
  padding: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: linear-gradient(135deg, #144ddd, #3b6ff5);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(20, 77, 221, 0.4);
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
}

.main-menu {
  flex: 1;
  border-right: none !important;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px;
}

.main-menu::-webkit-scrollbar {
  width: 4px;
}

.main-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: #144ddd;
  border-radius: 0 2px 2px 0;
  transition: height 0.2s ease;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.04) !important;
  color: #fff !important;
}

.menu-item.is-active {
  background: linear-gradient(90deg, rgba(20, 77, 221, 0.2) 0%, rgba(20, 77, 221, 0.1) 100%) !important;
  color: #fff !important;
  font-weight: 500;
}

.menu-item.is-active::before {
  height: 20px;
}

.menu-item i {
  margin-right: 12px;
  font-size: 16px;
  width: 16px;
}

.submenu-item {
  margin: 2px 0;
  border-radius: 8px;
}

.submenu-item >>> .el-submenu__title {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  transition: all 0.2s ease;
  padding-left: 16px !important;
}

.submenu-item >>> .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.04) !important;
  color: #fff !important;
}

.submenu-item >>> .el-submenu__title i {
  margin-right: 12px;
  font-size: 16px;
  width: 16px;
}

.submenu-child {
  height: 38px;
  line-height: 38px;
  margin: 2px 8px;
  padding-left: 44px !important;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s ease;
}

.submenu-child:hover {
  background: rgba(255, 255, 255, 0.04) !important;
  color: #fff !important;
}

.submenu-child.is-active {
  background: linear-gradient(90deg, rgba(20, 77, 221, 0.25) 0%, rgba(20, 77, 221, 0.15) 100%) !important;
  color: #fff !important;
  font-weight: 500;
}

/* 折叠动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* 菜单图标对齐 */
.el-menu-item i,
.el-submenu__title i {
  margin-right: 10px;
  font-size: 16px;
}

.el-menu--collapse .el-menu-item,
.el-menu--collapse .el-submenu__title {
  text-align: center;
  padding-left: 0 !important;
}

.el-menu--collapse .el-menu-item i,
.el-menu--collapse .el-submenu__title i {
  margin: 0;
}
</style>