<script setup>
</script>

<template>
  <div>
    <!-- 个人信息显示区域 - 使用路由路径判断 -->
    <div v-if="$route.path === '/IndexA/profile'" class="profile-section">
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

    <!-- 医嘱信息显示区域 -->
    <div v-else-if="$route.path === '/IndexA/medicalOrder'" class="medical-order-section">
      <el-card class="medical-order-card">
        <div slot="header" class="clearfix">
          <span style="font-size: 18px; font-weight: bold;">医嘱信息管理</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="loadMedicalOrders">
            刷新数据
          </el-button>
        </div>

        <el-table
            :data="medicalOrderTableData"
            :header-cell-style="{background:'#868a8c',color:'#333'}"
            border
            style="width: 100%"
            v-loading="medicalOrderLoading">

          <el-table-column prop="orderId" label="医嘱ID" width="80" align="center">
          </el-table-column>

          <el-table-column prop="hospitalizationTimes" label="住院次数" width="100" align="center">
          </el-table-column>

          <el-table-column prop="patientId" label="患者ID" width="100" align="center">
          </el-table-column>

          <el-table-column prop="orderName" label="医嘱项名称" min-width="150">
          </el-table-column>

          <el-table-column prop="startTime" label="开始时间" width="160" :formatter="formatDateColumn">
          </el-table-column>

          <el-table-column prop="endTime" label="结束时间" width="160" :formatter="formatDateColumn">
          </el-table-column>

          <el-table-column prop="orderStatus" label="医嘱状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.orderStatus === '停止' ? 'info' : 'success'">
                {{ scope.row.orderStatus || '进行中' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="orderFrequency" label="医嘱频率" width="100" align="center">
          </el-table-column>

          <el-table-column prop="executeDept" label="执行科室" width="120">
          </el-table-column>

          <el-table-column prop="executeDoc" label="执行医生" width="100">
          </el-table-column>
        </el-table>

        <el-pagination
            @size-change="handleMedicalOrderSizeChange"
            @current-change="handleMedicalOrderCurrentChange"
            :current-page="medicalOrderPageNum"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="medicalOrderPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="medicalOrderTotal"
            style="margin-top: 20px; text-align: center;">
        </el-pagination>
      </el-card>
    </div>

    <!-- 默认显示区域（当没有匹配到任何路由时显示） -->
    <div v-else class="default-section">
      <el-card>
        <div slot="header" class="clearfix">
          <span style="font-size: 18px; font-weight: bold;">欢迎使用系统</span>
        </div>
        <div style="text-align: center; padding: 50px;">
          <p>请从左侧菜单选择功能</p>
        </div>
      </el-card>
    </div>

    <el-dialog
        title="提示"
        :visible.sync="centerDialogVisible"
        width="30%"
        center>
      <el-form ref="form" :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-col :span="20">
            <el-input v-model="form.name"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="centerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="centerDialogVisible = false">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "MainA",
  data() {
    return {
      currentUser: {},
      loginTime: '',
      currentTime: '',
      currentTimeInterval: null,

      // 医嘱信息相关数据
      medicalOrderTableData: [],
      medicalOrderPageSize: 10,
      medicalOrderPageNum: 1,
      medicalOrderTotal: 0,

      // 原有的用户列表相关数据
      tableData: [],
      pageSize: 5,
      pageNum: 1,
      total: 0,
      userId: '',
      userName: '',
      centerDialogVisible: false,
      form: {}
    }
  },
  methods: {
    // 加载医嘱信息
    loadMedicalOrders() {
      this.$axios.post('/medicalOrder/page', {
        pageSize: this.medicalOrderPageSize,
        pageNum: this.medicalOrderPageNum
      }).then(res => res.data).then(res => {
        console.log(res)
        if (res.code == 200) {
          this.medicalOrderTableData = res.data;
          this.medicalOrderTotal = res.total;
          console.log('医嘱信息:', res.data)
        } else {
          alert('获取医嘱信息失败')
        }
      }).catch(error => {
        console.error('加载医嘱信息错误:', error);
        alert('加载医嘱信息出错')
      })
    },

    // 医嘱信息分页大小改变
    handleMedicalOrderSizeChange(val) {
      console.log(`每页 ${val} 条`);
      this.medicalOrderPageNum = 1;
      this.medicalOrderPageSize = val;
      this.loadMedicalOrders();
    },

    // 医嘱信息当前页改变
    handleMedicalOrderCurrentChange(val) {
      console.log(`当前页: ${val}`);
      this.medicalOrderPageNum = val;
      this.loadMedicalOrders();
    },

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

    // 表格列的格式化方法
    formatDateColumn(row, column, cellValue) {
      return this.formatDate(cellValue);
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
    },

    // 其他原有方法保持不变...
    resetQuery() {
      this.userId = '';
      this.userName = '';
      this.loadPost();
    },
    handleSizeChange(val) {
      console.log(`每页 ${val} 条`);
      this.pageNum = 1;
      this.pageSize = val;
      this.loadPost();
    },
    handleCurrentChange(val) {
      console.log(`当前页: ${val}`);
      this.pageNum = val;
      this.loadPost();
    },
    loadGet() {
      this.$axios.get('/user/list').then(res => res.data).then(res => {
        if (res.code == 200) {
          this.tableData = res.data;
          this.total = res.total;
          console.log(res.data)
        } else {
          alert('获取数据失败')
        }
      })
    },
    loadPost() {
      this.$axios.post('/user/page', {
        pageSize: this.pageSize,
        pageNum: this.pageNum,
        param: {
          userId: this.userId,
          userName: this.userName
        },
      }).then(res => res.data).then(res => {
        console.log(res)
        if (res.code == 200) {
          this.tableData = res.data;
          this.total = res.total;
          console.log(res.data)
        } else {
          alert('获取数据失败')
        }
        console.log(res);
      })
    }
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler(newPath) {
        this.clearCurrentTimeInterval();

        if (newPath === '/IndexA/profile') {
          this.loadProfile();
        } else if (newPath === '/IndexA/medicalOrder') {
          this.loadMedicalOrders();
        }
      }
    }
  },
  beforeDestroy() {
    this.clearCurrentTimeInterval();
  }
}
</script>

<style scoped>
/* 默认区域样式 */
.default-section {
  padding: 20px;
}

.default-section .el-card {
  max-width: 900px;
  margin: 0 auto;
}

/* 医嘱信息区域样式 */
.medical-order-section {
  padding: 20px;
}

.medical-order-card {
  max-width: 1200px;
  margin: 0 auto;
}

.medical-order-card .el-table {
  margin-top: 10px;
}

.profile-section {
  padding: 20px;
}

.profile-card {
  max-width: 900px;
  margin: 0 auto;
}

.info-item {
  margin-bottom: 15px;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item label {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
  min-width: 80px;
  display: inline-block;
}

.info-item span {
  color: #666;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

/* 当前时间独立显示区域样式 */
.current-time-section {
  margin-top: 30px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.current-time-card {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
}

.current-time-label {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.current-time-display {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}
/* 确保各个内容区域有正确的最小高度 */
.profile-section,
.medical-order-section,
.default-section {
  min-height: calc(100vh - 70px); /* 减去header和padding的高度 */
  box-sizing: border-box;
}

/* 确保卡片容器高度正确 */
.profile-card,
.medical-order-card,
.default-section .el-card {
  min-height: calc(100vh - 80px);
}

/* 表格容器设置最大高度和滚动 */
.medical-order-card .el-table {
  max-height: calc(100vh - 200px);
  overflow: auto;
}

/* 分页器定位在底部 */
.medical-order-card .el-pagination {
  margin-top: 20px;
  position: sticky;
  bottom: 0;
  background: white;
  padding: 10px 0;
  z-index: 10;
}
</style>