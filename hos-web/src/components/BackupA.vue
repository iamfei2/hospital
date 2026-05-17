<template>
  <div class="backup-section">
    <el-card>
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">数据备份与恢复</span>
        <el-button style="float: right;" type="primary" size="small" @click="handleBackup" :loading="backupLoading">手动备份</el-button>
      </div>

      <el-alert title="备份说明" type="info" :closable="false" style="margin-bottom: 16px;">
        <template slot>
          <div style="font-size: 13px;">
            <p>系统每日凌晨2:00自动备份数据库，保留最近30天的备份文件。</p>
            <p>您也可以点击"手动备份"按钮立即创建备份。选择备份文件后可进行恢复操作。</p>
          </div>
        </template>
      </el-alert>

      <el-table :data="backupList" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="fileName" label="备份文件名" min-width="250">
          <template slot-scope="scope">
            <i class="el-icon-document" style="margin-right: 5px; color: #67C23A;"></i>
            {{ scope.row.fileName }}
          </template>
        </el-table-column>
        <el-table-column label="备份大小" width="120" align="center">
          <template slot-scope="scope">
            {{ formatSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="backupTime" label="备份时间" width="180" align="center"></el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button type="warning" size="small" @click="handleRestore(scope.row)" :loading="restoreLoading" v-if="isAdmin">恢复</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "BackupA",
  data() {
    return {
      backupList: [],
      loading: false,
      backupLoading: false,
      restoreLoading: false,
      currentUser: {}
    }
  },
  computed: {
    isAdmin() {
      return this.currentUser.role === 'admin';
    }
  },
  methods: {
    loadBackups() {
      this.loading = true;
      this.$axios.get('/backup/list')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.backupList = res.data || [];
          } else {
            this.$message.error('查询备份列表失败');
          }
        })
        .catch(() => { this.$message.error('查询出错'); })
        .finally(() => { this.loading = false; });
    },
    handleBackup() {
      this.backupLoading = true;
      this.$axios.post('/backup/manual')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('备份成功');
            this.loadBackups();
          } else {
            this.$message.error(res.msg || '备份失败');
          }
        })
        .catch(() => { this.$message.error('备份出错'); })
        .finally(() => { this.backupLoading = false; });
    },
    handleRestore(row) {
      this.$confirm(`确定要从备份 "${row.fileName}" 恢复数据库吗？此操作将覆盖当前数据！`, '警告', {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.restoreLoading = true;
        this.$axios.post('/backup/restore', null, {
          params: { 
            fileName: row.fileName,
            encrypted: row.encrypted,
            compressed: row.compressed
          }
        }).then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success('数据库恢复成功');
          } else {
            this.$message.error(res.msg || '恢复失败');
          }
        }).catch(() => { 
          this.$message.error('恢复出错'); 
        }).finally(() => {
          this.restoreLoading = false;
        });
      }).catch(() => {});
    },
    handleDelete(row) {
      this.$confirm(`确定要删除备份 "${row.fileName}" 吗？`, '提示', { type: 'warning' }).then(() => {
        this.$axios.delete('/backup/' + encodeURIComponent(row.fileName))
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadBackups();
            } else {
              this.$message.error(res.msg || '删除失败');
            }
          })
          .catch(() => { this.$message.error('删除出错'); });
      }).catch(() => {});
    },
    formatSize(bytes) {
      if (!bytes) return '-';
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
  },
  mounted() { 
    this.loadBackups(); 
    const userStr = sessionStorage.getItem('CurUser');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      console.log('当前用户:', this.currentUser);
    }
  }
}
</script>

<style scoped>
.backup-section { padding: 0; }
</style>
