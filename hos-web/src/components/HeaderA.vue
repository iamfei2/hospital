<template>
  <div class="header-container">
    <div class="header-left">
      <i :class="icon" class="collapse-icon" @click="collapse"></i>
      <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/IndexA' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <div class="notification-bell" @click="showNotificationPanel">
        <i class="el-icon-bell"></i>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="bell-badge" type="danger">
        </el-badge>
      </div>

      <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
        <div class="user-info">
          <div class="avatar">
            <span>{{ getInitial(user.userName) }}</span>
          </div>
          <div class="user-detail">
            <span class="username">{{ user.userName }}</span>
            <span class="role-tag">{{ user.role === 'admin' ? '管理员' : '用户' }}</span>
          </div>
          <i class="el-icon-arrow-down el-icon--right"></i>
        </div>
        <el-dropdown-menu slot="dropdown" class="user-dropdown">
          <el-dropdown-item command="profile">
            <i class="el-icon-user"></i>
            <span>个人中心</span>
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <i class="el-icon-switch-button"></i>
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <el-drawer
      title=" "
      :visible.sync="notificationVisible"
      direction="rtl"
      size="400px"
      :wrapperClosable="true"
      class="notification-drawer">
      <div class="drawer-header">
        <h3>预警通知</h3>
        <el-button type="text" size="small" @click="markAllRead" v-if="recentWarnings.length > 0">全部标为已读</el-button>
      </div>
      <div class="notification-list" v-if="recentWarnings.length > 0">
        <div
          v-for="item in recentWarnings"
          :key="item.warningId"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleWarningClick(item)">
          <div class="item-indicator" :class="'severity-' + item.severity.toLowerCase()"></div>
          <div class="item-content">
            <div class="item-message">{{ item.message }}</div>
            <div class="item-time">{{ formatTime(item.createTime) }}</div>
          </div>
          <div class="item-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <i class="el-icon-bell"></i>
        <p>暂无预警通知</p>
      </div>
      <div class="drawer-footer" v-if="recentWarnings.length > 0">
        <el-button type="text" size="small" @click="$router.push('/IndexA/warningRecord'); notificationVisible = false">
          查看全部预警记录
          <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "HeaderA",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem('CurUser')) || {},
      unreadCount: 0,
      recentWarnings: [],
      notificationVisible: false,
      ws: null,
      wsReconnectTimer: null
    }
  },
  mounted() {
    this.loadUnreadCount();
    this.connectWebSocket();
  },
  beforeDestroy() {
    this.closeWebSocket();
  },
  methods: {
    logOut() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        sessionStorage.clear();
        localStorage.removeItem('token');
        this.closeWebSocket();
        this.$router.push('/');
        this.$message.success('退出成功!');
      }).catch(() => {});
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.logOut();
      } else if (command === 'profile') {
        if (this.$route.path !== '/IndexA/profile') {
          this.$router.push('/IndexA/profile');
        }
      }
    },
    collapse() {
      this.$emit('doCollapse')
    },
    getInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },
    loadUnreadCount() {
      this.$axios.get('/warningRecord/unreadCount')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.unreadCount = res.data || 0;
          }
        })
        .catch(() => {});
    },
    loadRecentWarnings() {
      this.$axios.get('/warningRecord/unreadList')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.recentWarnings = res.data || [];
          }
        })
        .catch(() => {});
    },
    connectWebSocket() {
      const userId = this.user.userId || 'admin';
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = `${protocol}//${window.location.host}/ws/warning/${userId}`;

      try {
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
          this.loadRecentWarnings();
        };

        this.ws.onmessage = (event) => {
          if (event.data === 'pong') return;
          try {
            const data = JSON.parse(event.data);

            this.unreadCount++;
            this.recentWarnings.unshift(data);
            if (this.recentWarnings.length > 50) {
              this.recentWarnings = this.recentWarnings.slice(0, 50);
            }

            this.$notify.warning({
              title: '预警提醒',
              message: data.message || '有新的预警信息',
              duration: 8000,
              position: 'bottom-right'
            });
          } catch (e) {
            console.error('解析WebSocket消息失败', e);
          }
        };

        this.ws.onclose = () => {
          this.wsReconnectTimer = setTimeout(() => {
            this.connectWebSocket();
          }, 3000);
        };

        this.heartbeatTimer = setInterval(() => {
          if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send('ping');
          }
        }, 30000);
      } catch (e) {
        this.wsReconnectTimer = setTimeout(() => {
          this.connectWebSocket();
        }, 3000);
      }
    },
    closeWebSocket() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
      }
      if (this.wsReconnectTimer) {
        clearTimeout(this.wsReconnectTimer);
      }
      if (this.ws) {
        this.ws.close();
        this.ws = null;
      }
    },
    showNotificationPanel() {
      this.notificationVisible = true;
      this.loadRecentWarnings();
    },
    handleWarningClick(item) {
      if (!item.isRead) {
        this.$axios.post('/warningRecord/markRead/' + item.warningId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              item.isRead = true;
              this.unreadCount = Math.max(0, this.unreadCount - 1);
            }
          })
          .catch(() => {});
      }
    },
    markAllRead() {
      this.$axios.post('/warningRecord/markAllRead')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.unreadCount = 0;
            this.recentWarnings.forEach(item => item.isRead = true);
            this.$message.success('已全部标为已读');
          }
        })
        .catch(() => {});
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      if (typeof timeStr === 'string') {
        return timeStr.replace('T', ' ').substring(0, 19);
      }
      return '';
    }
  }
}
</script>

<style scoped>
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.collapse-icon:hover {
  background: #f5f7fa;
  color: #144ddd;
}

.breadcrumb >>> .el-breadcrumb__inner {
  color: #909399;
  font-size: 13px;
}

.breadcrumb >>> .el-breadcrumb__inner.is-link:hover {
  color: #144ddd;
}

.breadcrumb >>> .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #303133;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-bell {
  position: relative;
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.notification-bell:hover {
  background: #f5f7fa;
}

.notification-bell i {
  font-size: 20px;
  color: #606266;
}

.bell-badge {
  position: absolute;
  top: 2px;
  right: 2px;
}

.bell-badge >>> .el-badge__content {
  background: #f53f3f;
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.2s;
  height: 36px;
  box-sizing: border-box;
}

.user-info:hover {
  background: #f5f7fa;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #144ddd, #3b6ff5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(20, 77, 221, 0.3);
}

.user-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.3;
}

.role-tag {
  font-size: 11px;
  color: #909399;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 4px;
  line-height: 1.5;
}

.user-info .el-icon-arrow-down {
  font-size: 12px;
  color: #c0c4cc;
  transition: transform 0.2s;
}

.user-dropdown >>> .el-dropdown-menu__item {
  padding: 8px 20px !important;
  font-size: 13px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 0;
}

.user-dropdown >>> .el-dropdown-menu__item i {
  font-size: 14px;
  order: 1;
  margin-left: 0;
}

.user-dropdown >>> .el-dropdown-menu__item span {
  order: 2;
  margin-right: 8px;
}

/* 抽屉样式 */
.notification-drawer >>> .el-drawer__header {
  padding: 0;
  margin: 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-item.unread:hover {
  background: #e6f0ff;
}

.item-indicator {
  width: 4px;
  height: 32px;
  border-radius: 2px;
  flex-shrink: 0;
}

.severity-emergency {
  background: #f53f3f;
}

.severity-critical {
  background: #ff7d00;
}

.severity-warning {
  background: #ff9800;
}

.severity-info {
  background: #909399;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-message {
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

.item-arrow {
  color: #c0c4cc;
  font-size: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 14px;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  text-align: center;
}

.drawer-footer >>> .el-button--text {
  color: #144ddd;
  font-size: 13px;
}

.drawer-footer >>> .el-button--text:hover {
  color: #3b6ff5;
}
</style>