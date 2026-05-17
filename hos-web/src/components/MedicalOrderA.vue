<template>
  <div class="medical-order-section">
    <el-card class="medical-order-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">医嘱信息管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadMedicalOrders">
          刷新数据
        </el-button>
      </div>

      <!-- 查询条件区域 -->
      <div class="query-form">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="患者ID">
            <el-input
                v-model="queryParams.patientId"
                placeholder="请输入患者ID"
                clearable
                style="width: 200px;"
                @keyup.enter.native="handleQuery">
            </el-input>
          </el-form-item>


          <el-form-item label="医嘱时间">
            <el-date-picker
                v-model="queryParams.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm"
                :default-time="['00:00:00', '23:59:59']"
                style="width: 380px;"
                clearable>
            </el-date-picker>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleQuery" icon="el-icon-search">查询</el-button>
            <el-button @click="handleReset" icon="el-icon-refresh">重置</el-button>
            <el-button type="success" @click="handleAdd" icon="el-icon-plus">新增医嘱</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
          :data="medicalOrderTableData"
          :header-cell-style="{background:'#868a8c',color:'#333'}"
          border
          style="width: 100%"
          v-loading="medicalOrderLoading">

        <!-- 已去掉医嘱ID列 -->

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
            <el-tag :type="getStatusType(scope.row.orderStatus)" effect="dark">
              {{ getStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="orderFrequency" label="医嘱频率" width="100" align="center">
        </el-table-column>

        <el-table-column prop="executeDept" label="执行科室" width="120">
        </el-table-column>

        <el-table-column prop="executeDoc" label="执行医生" width="100">
        </el-table-column>

        <!-- 修复后的操作列 -->
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <div v-if="scope.row && scope.row.orderStatus === '执行'">
              <el-button
                  type="primary"
                  size="mini"
                  @click="handleStopOrder(scope.row)"
                  :loading="getRowLoadingState(scope.row)">
                停止医嘱
              </el-button>
            </div>
            <div v-else-if="scope.row">
              <span style="color: #909399;">不可操作</span>
            </div>
            <div v-else>
              <span style="color: #909399;">数据错误</span>
            </div>
          </template>
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

      <!-- 新增医嘱对话框 -->
      <el-dialog
          title="新增医嘱"
          :visible.sync="addDialogVisible"
          width="600px"
          @close="handleAddDialogClose">
        <el-form :model="addForm" :rules="addFormRules" ref="addFormRef" label-width="100px">
          <el-form-item label="患者ID" prop="patientId">
            <el-input v-model="addForm.patientId" placeholder="请输入患者ID"></el-input>
          </el-form-item>
          <el-form-item label="住院次数" prop="hospitalizationTimes">
            <el-input-number v-model="addForm.hospitalizationTimes" :min="1" :max="999"></el-input-number>
          </el-form-item>
          <el-form-item label="医嘱名称" prop="orderName">
            <el-input v-model="addForm.orderName" placeholder="请输入医嘱项名称"></el-input>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
                v-model="addForm.startTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择开始时间"
                style="width: 100%;">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
                v-model="addForm.endTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择结束时间"
                style="width: 100%;">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="医嘱状态" prop="orderStatus">
            <el-select v-model="addForm.orderStatus" placeholder="请选择状态" style="width: 100%;">
              <el-option label="执行" value="执行"></el-option>
              <el-option label="终止" value="终止"></el-option>
              <el-option label="撤销" value="撤销"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="医嘱频率" prop="orderFrequency">
            <el-input v-model="addForm.orderFrequency" placeholder="如：一次、每日一次等"></el-input>
          </el-form-item>
          <el-form-item label="执行科室" prop="executeDept">
            <el-input v-model="addForm.executeDept" placeholder="请输入执行科室"></el-input>
          </el-form-item>
          <el-form-item label="执行医生" prop="executeDoc">
            <el-input v-model="addForm.executeDoc" placeholder="请输入执行医生"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitAddForm" :loading="addLoading">确 定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "MedicalOrderA",
  data() {
    return {
      medicalOrderTableData: [],
      medicalOrderPageSize: 10,
      medicalOrderPageNum: 1,
      medicalOrderTotal: 0,
      medicalOrderLoading: false,

      // 使用简单的对象存储loading状态
      rowLoadingStates: {},

      // 查询参数
      queryParams: {
        patientId: '',
        timeRange: [],
        startTime: '',
        endTime: ''
      },
      addDialogVisible: false,
      addLoading: false,
      addForm: {
        patientId: '',
        hospitalizationTimes: 1,
        orderName: '',
        startTime: '',
        endTime: '',
        orderStatus: '执行',
        orderFrequency: '',
        executeDept: '',
        executeDoc: ''
      },
      addFormRules: {
        patientId: [
          { required: true, message: '患者ID不能为空', trigger: 'blur' }
        ],
        orderName: [
          { required: true, message: '医嘱名称不能为空', trigger: 'blur' }
        ],
        startTime: [
          { required: true, message: '开始时间不能为空', trigger: 'change' }
        ],
      }
    }
  },
  methods: {
    // 新增医嘱对话框打开
    handleAdd() {
      this.addDialogVisible = true;
    },

    // 新增对话框关闭
    handleAddDialogClose() {
      this.$refs.addFormRef.resetFields();
      this.addForm = {
        patientId: '',
        hospitalizationTimes: 1,
        orderName: '',
        startTime: '',
        endTime: '',
        orderStatus: '执行',
        orderFrequency: '',
        executeDept: '',
        executeDoc: ''
      };
    },

    // 提交新增表单
    submitAddForm() {
      this.$refs.addFormRef.validate((valid) => {
        if (valid) {
          this.addLoading = true;

          this.$axios.post('/medicalOrder/add', this.addForm)
              .then(res => res.data)
              .then(res => {
                if (res.code == 200) {
                  this.$message.success('新增医嘱成功');
                  this.addDialogVisible = false;
                  this.loadMedicalOrders(); // 刷新列表
                } else {
                  this.$message.error('新增医嘱失败: ' + (res.msg || '未知错误'));
                }
                this.addLoading = false;
              })
              .catch(error => {
                console.error('新增医嘱错误:', error);
                this.$message.error('新增医嘱出错');
                this.addLoading = false;
              });
        }
      });
    },
    // 加载医嘱信息
    loadMedicalOrders() {
      this.medicalOrderLoading = true;

      // 处理时间范围参数
      if (this.queryParams.timeRange && this.queryParams.timeRange.length === 2) {
        this.queryParams.startTime = this.queryParams.timeRange[0];
        this.queryParams.endTime = this.queryParams.timeRange[1];
      } else {
        this.queryParams.startTime = '';
        this.queryParams.endTime = '';
      }

      this.$axios.post('/medicalOrder/page', {
        pageSize: this.medicalOrderPageSize,
        pageNum: this.medicalOrderPageNum,
        param: {
          patientId: this.queryParams.patientId,
          startTime: this.queryParams.startTime,
          endTime: this.queryParams.endTime
        }
      }).then(res => res.data).then(res => {
        console.log('查询结果:', res)
        if (res.code == 200) {
          // 直接使用返回的数据，不添加任何额外属性
          this.medicalOrderTableData = res.data || [];
          this.medicalOrderTotal = res.total;

          console.log('医嘱信息:', this.medicalOrderTableData)
        } else {
          this.$message.error('获取医嘱信息失败');
        }
        this.medicalOrderLoading = false;
      }).catch(error => {
        console.error('加载医嘱信息错误:', error);
        this.$message.error('加载医嘱信息出错');
        this.medicalOrderLoading = false;
      })
    },

    // 获取行的loading状态
    getRowLoadingState(row) {
      if (!row || !row.orderId) return false;
      return this.rowLoadingStates[row.orderId] || false;
    },

    // 查询按钮处理
    handleQuery() {
      this.medicalOrderPageNum = 1;
      this.loadMedicalOrders();
    },

    // 重置查询条件
    handleReset() {
      this.queryParams = {
        patientId: '',
        timeRange: [],
        startTime: '',
        endTime: ''
      };
      this.medicalOrderPageNum = 1;
      this.loadMedicalOrders();
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

    // 获取医嘱状态对应的标签类型
    getStatusType(status) {
      const statusMap = {
        '终止': 'info',
        '撤销': 'warning',
        '执行': 'success'
      };
      return statusMap[status] || 'info';
    },

    // 获取医嘱状态显示文本
    getStatusText(status) {
      return status || '未知状态';
    },

    // 停止医嘱操作
    handleStopOrder(row) {
      this.$confirm('确定要停止这条医嘱吗？停止后状态将变为"终止"，结束时间将设置为当前时间。', '确认停止', {
        confirmButtonText: '确定停止',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.updateOrderStatus(row);
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },

    // 更新医嘱状态
    updateOrderStatus(row) {
      // 设置当前行的loading状态
      this.$set(this.rowLoadingStates, row.orderId, true);

      // 获取当前时间
      const now = new Date();
      const endTime = this.formatDateTime(now);

      this.$axios.put('/medicalOrder/stop', {
        orderId: row.orderId,
        endTime: endTime
      }).then(res => res.data).then(res => {
        if (res.code == 200) {
          this.$message.success('医嘱已成功停止');
          // 刷新数据
          this.loadMedicalOrders();
        } else {
          this.$message.error('停止医嘱失败: ' + (res.msg || '未知错误'));
        }
        this.$set(this.rowLoadingStates, row.orderId, false);
      }).catch(error => {
        console.error('停止医嘱错误:', error);
        this.$message.error('停止医嘱出错');
        this.$set(this.rowLoadingStates, row.orderId, false);
      });
    },

    // 格式化日期时间为后端需要的格式
    formatDateTime(date) {
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      const second = date.getSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    }
  },
  mounted() {
    this.loadMedicalOrders();
  }
}
</script>

<style scoped>
.medical-order-section {
  padding: 0;
  min-height: calc(100vh - 70px);
}

.medical-order-card {
  min-height: calc(100vh - 150px);
}

.medical-order-card .el-table {
  margin-top: 10px;
  max-height: calc(100vh - 250px);
  overflow: auto;
}

.query-form {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f7f8fa;
  border-radius: 8px;
}

.demo-form-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 768px) {
  .demo-form-inline {
    flex-direction: column;
    align-items: stretch;
  }

  .query-form .el-form-item {
    margin-bottom: 10px;
  }
}

.el-table .cell {
  word-break: break-word;
}

.el-table .el-button--mini {
  padding: 5px 10px;
  font-size: 12px;
}
</style>