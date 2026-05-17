<template>
  <div class="operation-log-section">
    <el-card class="operation-log-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">操作日志</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadLogList">
          刷新数据
        </el-button>
      </div>

      <el-table
          :data="logList"
          :header-cell-style="{background:'#868a8c',color:'#333'}"
          border
          style="width: 100%"
          v-loading="loading">
        
        <el-table-column prop="logId" label="日志ID" width="80" align="center">
        </el-table-column>

        <el-table-column prop="userId" label="操作人ID" width="100" align="center">
        </el-table-column>

        <el-table-column label="操作时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.operationTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTypeTag(scope.row.operationType)" size="small">
              {{ scope.row.operationType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="operatedTable" label="操作表" width="180">
        </el-table-column>

        <el-table-column prop="relatedRecordId" label="关联记录ID" width="120">
        </el-table-column>

        <el-table-column prop="remark" label="操作描述" min-width="150">
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: center;">
      </el-pagination>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
        title="操作日志详情"
        :visible.sync="detailDialogVisible"
        width="70%">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ currentLog.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDate(currentLog.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作表">{{ currentLog.operatedTable }}</el-descriptions-item>
        <el-descriptions-item label="关联记录ID">{{ currentLog.relatedRecordId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="white-space: pre-wrap; word-break: break-all;">{{ formatJson(currentLog.beforeContent) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre style="white-space: pre-wrap; word-break: break-all;">{{ formatJson(currentLog.afterContent) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "OperationLogA",
  data() {
    return {
      logList: [],
      loading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,
      detailDialogVisible: false,
      currentLog: null
    }
  },
  methods: {
    loadLogList() {
      this.loading = true;
      this.$axios.post('/operationLog/page', {
        pageSize: this.pageSize,
        pageNum: this.pageNum
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.logList = res.data || [];
            this.total = res.total || 0;
          } else {
            this.$message.error('获取操作日志失败');
          }
        })
        .catch(error => {
          console.error('加载操作日志错误:', error);
          this.$message.error('加载操作日志出错');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadLogList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadLogList();
    },
    handleViewDetail(row) {
      this.currentLog = row;
      this.detailDialogVisible = true;
    },
    getTypeTag(type) {
      const typeMap = {
        '新增': 'success',
        '修改': 'warning',
        '删除': 'danger'
      };
      return typeMap[type] || 'info';
    },
    formatDate(dateValue) {
      if (!dateValue) return '-';
      
      let date;
      if (typeof dateValue === 'string') {
        date = new Date(dateValue);
      } else if (dateValue instanceof Date) {
        date = dateValue;
      } else {
        try {
          if (dateValue.year && dateValue.monthValue && dateValue.dayOfMonth) {
            date = new Date(dateValue.year, dateValue.monthValue - 1, dateValue.dayOfMonth,
                dateValue.hour || 0, dateValue.minute || 0, dateValue.second || 0);
          } else {
            return '-';
          }
        } catch (e) {
          return '-';
        }
      }
      
      if (isNaN(date)) return '-';
      
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      const second = date.getSeconds().toString().padStart(2, '0');
      
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },
    formatJson(str) {
      if (!str) return '-';
      try {
        return JSON.stringify(JSON.parse(str), null, 2);
      } catch (e) {
        return str;
      }
    }
  },
  mounted() {
    this.loadLogList();
  }
}
</script>

<style scoped>
.operation-log-section {
  padding: 0;
}

.operation-log-card .el-table {
  max-height: calc(100vh - 320px);
  overflow: auto;
}

.operation-log-card .el-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

pre {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  background: #f7f8fa;
  padding: 8px;
  border-radius: 4px;
  margin: 0;
}
</style>
