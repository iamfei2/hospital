<template>
  <div class="profile-section">
    <el-card class="profile-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">您好 , {{ currentUser.userName || '未设置' }}</span>
      </div>

      <div class="user-info">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>用户名：</label>
              <span>{{ currentUser.loginAccount || '未登录' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>用户ID：</label>
              <span>{{ currentUser.id || '未知' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>登录时间：</label>
              <span>{{ formatDate(loginTime) }}</span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <div class="info-item">
              <label>姓名：</label>
              <span>{{ currentUser.userName || '未设置' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>创建时间：</label>
              <span>{{ formatDate(currentUser.createTime) || '未知' }}</span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <div class="info-item">
              <label>账号状态：</label>
              <el-tag :type="currentUser.status ? 'success' : 'danger'">
                {{ currentUser.status ? '启用' : '封禁' }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>角色：</label>
              <el-tag type="primary">{{ currentUser.role || '普通用户' }}</el-tag>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 独立显示当前时间区域 -->
    <div class="current-time-section">
      <el-card class="current-time-card">
        <div class="current-time-label">当前时间</div>
        <div class="current-time-display">{{ currentTime }}</div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProfileA",
  data() {
    return {
      currentUser: {},
      loginTime: '',
      currentTime: '',
      currentTimeInterval: null
    }
  },
  methods: {
    // 日期格式化方法
    formatDate(dateString) {
      if (!dateString) return '未知';

      const date = new Date(dateString);
      if (isNaN(date)) return dateString;

      const year = date.getFullYear();
      const month = date.getMonth() + 1;
      const day = date.getDate();
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      const second = date.getSeconds().toString().padStart(2, '0');

      return `${year}/${month}/${day} ${hour}:${minute}:${second}`;
    },

    // 更新当前时间
    updateCurrentTime() {
      const now = new Date();
      this.currentTime = this.formatDate(now);
    },

    // 加载个人信息
    loadProfile() {
      const userStr = sessionStorage.getItem('CurUser');
      if (userStr) {
        this.currentUser = JSON.parse(userStr);
        this.loginTime = new Date();
        this.updateCurrentTime();

        // 启动当前时间定时器
        if (this.currentTimeInterval) {
          clearInterval(this.currentTimeInterval);
        }
        this.currentTimeInterval = setInterval(() => {
          this.updateCurrentTime();
        }, 1000);
      }
    },

    // 清理定时器
    clearCurrentTimeInterval() {
      if (this.currentTimeInterval) {
        clearInterval(this.currentTimeInterval);
        this.currentTimeInterval = null;
      }
    }
  },
  mounted() {
    this.loadProfile();
  },
  beforeDestroy() {
    this.clearCurrentTimeInterval();
  }
}
</script>

<style scoped>
.profile-section {
  padding: 0;
  min-height: calc(100vh - 70px);
}

.info-item {
  margin-bottom: 15px;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 6px;
}

.info-item label {
  font-weight: 500;
  color: #4e5969;
  margin-right: 10px;
  min-width: 80px;
  display: inline-block;
  font-size: 13px;
}

.info-item span {
  color: #1d2129;
  font-size: 13px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.current-time-section {
  margin-top: 24px;
}

.current-time-card {
  text-align: center;
  padding: 24px;
  background-color: #f7f8fa;
  border-radius: 8px;
}

.current-time-label {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 12px;
}

.current-time-display {
  font-size: 32px;
  font-weight: 600;
  color: #165dff;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
  letter-spacing: 1px;
}
</style>