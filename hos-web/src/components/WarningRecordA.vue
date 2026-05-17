<template>
  <div class="warning-record-section">
    <el-card>
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">预警记录查询</span>
        <el-button style="float: right; margin-left: 10px;" type="primary" size="small" @click="markAllRead">全部标为已读</el-button>
        <el-button style="float: right;" type="info" size="small" @click="loadStats">刷新统计</el-button>
      </div>

      <div class="stats-bar" v-if="stats">
        <el-tag type="danger" size="medium">未读: {{ stats.unread }}</el-tag>
        <el-tag type="danger" size="medium" style="margin-left: 10px;">危急: {{ stats.critical }}</el-tag>
        <el-tag size="medium" style="margin-left: 10px;">今日: {{ stats.today }}</el-tag>
        <el-tag type="info" size="medium" style="margin-left: 10px;">总计: {{ stats.total }}</el-tag>
      </div>

      <el-form :inline="true" :model="queryParams" class="demo-form-inline" style="margin-top: 16px;">
        <el-form-item label="患者ID">
          <el-input v-model="queryParams.patientId" placeholder="请输入患者ID" clearable style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="严重级别">
          <el-select v-model="queryParams.severity" placeholder="全部" clearable style="width: 120px;">
            <el-option label="提示" value="INFO"></el-option>
            <el-option label="警告" value="WARNING"></el-option>
            <el-option label="危急" value="CRITICAL"></el-option>
            <el-option label="紧急" value="EMERGENCY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="queryParams.ruleType" placeholder="全部" clearable style="width: 130px;">
            <el-option label="指标异常" value="THRESHOLD"></el-option>
            <el-option label="关键词预警" value="KEYWORD"></el-option>
            <el-option label="数据缺失" value="MISSING"></el-option>
            <el-option label="操作异常" value="OPERATION"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isRead" placeholder="全部" clearable style="width: 100px;">
            <el-option label="未读" :value="false"></el-option>
            <el-option label="已读" :value="true"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecords">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="recordTableData" border style="width: 100%" v-loading="loading"
                :row-style="row => ({ background: !row.isRead ? '#fff7e6' : '' })">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="patientName" label="患者" width="100" align="center"></el-table-column>
        <el-table-column prop="itemName" label="项目/类型" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.ruleType === 'KEYWORD'">{{ getExamTypeName(scope.row.examinationType) }}</span>
            <span v-else>{{ scope.row.itemName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="检验值" width="100" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.resultValue != null">{{ scope.row.resultValue }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="级别" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.severity === 'EMERGENCY'" type="danger" size="mini">紧急</el-tag>
            <el-tag v-else-if="scope.row.severity === 'CRITICAL'" type="danger" size="mini">危急</el-tag>
            <el-tag v-else-if="scope.row.severity === 'WARNING'" type="warning" size="mini">警告</el-tag>
            <el-tag v-else type="info" size="mini">提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警消息" min-width="250">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.message" placement="top">
              <span class="message-text">{{ scope.row.message }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isRead" type="success" size="mini">已读</el-tag>
            <el-tag v-else type="warning" size="mini">未读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" width="170" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button v-if="!scope.row.isRead" type="text" size="small" @click="markRead(scope.row)">标为已读</el-button>
            <el-button type="text" size="small" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
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
        style="margin-top: 20px; text-align: right;">
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "WarningRecordA",
  data() {
    return {
      queryParams: { patientId: '', severity: '', ruleType: '', isRead: '' },
      recordTableData: [],
      loading: false,
      pageSize: 20,
      pageNum: 1,
      total: 0,
      stats: null
    }
  },
  methods: {
    loadRecords() {
      this.loading = true;
      this.$axios.post('/warningRecord/page', {
        pageNum: this.pageNum, pageSize: this.pageSize, param: this.queryParams
      }).then(res => res.data).then(res => {
        if (res.code === 200) {
          this.recordTableData = res.data || [];
          this.total = res.total || 0;
        } else {
          this.$message.error('查询失败');
        }
      }).catch(() => { this.$message.error('查询出错'); })
        .finally(() => { this.loading = false; });
    },
    loadStats() {
      this.$axios.get('/warningRecord/stats').then(res => res.data).then(res => {
        if (res.code === 200) { this.stats = res.data; }
      });
    },
    resetQuery() {
      this.queryParams = { patientId: '', severity: '', ruleType: '', isRead: '' };
      this.pageNum = 1;
      this.loadRecords();
    },
    handleSizeChange(val) { this.pageSize = val; this.loadRecords(); },
    handleCurrentChange(val) { this.pageNum = val; this.loadRecords(); },
    markRead(row) {
      this.$axios.post('/warningRecord/markRead/' + row.warningId).then(res => res.data).then(res => {
        if (res.code === 200) {
          row.isRead = true;
          this.$message.success('已标为已读');
        }
      });
    },
    markAllRead() {
      this.$confirm('确定要将所有预警标记为已读吗?', '提示', { type: 'warning' }).then(() => {
        this.$axios.post('/warningRecord/markAllRead').then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success('已全部标为已读');
            this.recordTableData.forEach(r => r.isRead = true);
            this.loadStats();
          }
        });
      });
    },
    handleDelete(row) {
      this.$confirm('确定要删除该预警记录吗?', '提示', { type: 'warning' }).then(() => {
        this.$axios.delete('/warningRecord/' + row.warningId).then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadRecords();
            this.loadStats();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        }).catch(() => { this.$message.error('删除失败'); });
      });
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      if (typeof timeStr === 'string') return timeStr.replace('T', ' ').substring(0, 19);
      return '';
    },
    getExamTypeName(type) {
      const map = { 'CT': 'CT检查', 'MRI': 'MRI检查', 'PATHOLOGY': '病理检查', 'ENTEROSCOPY': '肠镜检查' };
      return map[type] || type || '-';
    }
  },
  mounted() { this.loadRecords(); this.loadStats(); }
}
</script>

<style scoped>
.warning-record-section { padding: 0; }
.demo-form-inline { margin-bottom: 16px; }
.stats-bar { display: flex; align-items: center; padding: 12px 0; }
.message-text { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
