<template>
  <div class="lab-item-section">
    <el-card class="lab-item-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">检验项目管理</span>
        <el-button style="float: right; margin-left: 10px;" type="primary" size="small" @click="showAddDialog">新增项目</el-button>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadLabItemList">
          刷新数据
        </el-button>
      </div>

      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="项目名称">
          <el-input v-model="queryParams.itemName" placeholder="请输入项目名称" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
          :data="labItemList"
          :header-cell-style="{background:'#868a8c',color:'#333'}"
          border
          style="width: 100%"
          v-loading="loading">
        
        <el-table-column prop="itemId" label="ID" width="80" align="center">
        </el-table-column>

        <el-table-column prop="itemName" label="项目名称" min-width="150">
        </el-table-column>

        <el-table-column prop="itemCode" label="项目编码" width="150">
        </el-table-column>

        <el-table-column prop="defaultUnit" label="默认单位" width="100">
        </el-table-column>

        <el-table-column prop="normalRange" label="参考范围" width="120">
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="150">
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleDelete(scope.row)" style="color: #f56c6c;">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formData" label-width="100px">
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="formData.itemName" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="formData.itemCode" placeholder="请输入项目编码"></el-input>
        </el-form-item>
        <el-form-item label="默认单位" prop="defaultUnit">
          <el-input v-model="formData.defaultUnit" placeholder="请输入默认单位"></el-input>
        </el-form-item>
        <el-form-item label="参考范围" prop="normalRange">
          <el-input v-model="formData.normalRange" placeholder="例如：0-40"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "LabItemDictA",
  data() {
    return {
      labItemList: [],
      loading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,
      queryParams: {
        itemName: ''
      },
      dialogVisible: false,
      dialogTitle: '新增检验项目',
      isEdit: false,
      submitLoading: false,
      formData: {
        itemId: null,
        itemName: '',
        itemCode: '',
        defaultUnit: '',
        normalRange: '',
        remark: ''
      },
      formRules: {
        itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        itemCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    loadLabItemList() {
      this.loading = true;
      this.$axios.get('/labItemDict/list')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            let list = res.data || [];
            
            // 按项目名称过滤
            if (this.queryParams.itemName) {
              list = list.filter(item => 
                item.itemName && item.itemName.includes(this.queryParams.itemName)
              );
            }
            
            this.total = list.length;
            this.labItemList = list.slice((this.pageNum - 1) * this.pageSize, this.pageNum * this.pageSize);
          } else {
            this.$message.error('获取检验项目列表失败');
          }
        })
        .catch(() => {
          this.$message.error('获取检验项目列表失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleQuery() {
      this.pageNum = 1;
      this.loadLabItemList();
    },
    resetQuery() {
      this.queryParams = { itemName: '' };
      this.handleQuery();
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadLabItemList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadLabItemList();
    },
    showAddDialog() {
      this.isEdit = false;
      this.dialogTitle = '新增检验项目';
      this.formData = {
        itemId: null,
        itemName: '',
        itemCode: '',
        defaultUnit: '',
        normalRange: '',
        remark: ''
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.formData) {
          this.$refs.formData.resetFields();
        }
      });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑检验项目';
      this.formData = { ...row };
      this.dialogVisible = true;
    },
    handleSubmit() {
      this.$refs.formData.validate(valid => {
        if (valid) {
          this.submitLoading = true;
          const url = this.isEdit ? '/labItemDict/update' : '/labItemDict/add';
          
          this.$axios.post(url, this.formData)
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success(this.isEdit ? '修改成功' : '添加成功');
                this.dialogVisible = false;
                this.loadLabItemList();
              } else {
                this.$message.error(res.msg || '操作失败');
              }
            })
            .catch(() => {
              this.$message.error('操作失败');
            })
            .finally(() => {
              this.submitLoading = false;
            });
        }
      });
    },
    handleDelete(row) {
      this.$confirm('确定要删除该检验项目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete('/labItemDict/' + row.itemId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadLabItemList();
            } else {
              this.$message.error(res.msg || '删除失败');
            }
          })
          .catch(() => {
            this.$message.error('删除失败');
          });
      }).catch(() => {});
    }
  },
  mounted() {
    this.loadLabItemList();
  }
}
</script>

<style scoped>
.lab-item-section {
  padding: 0;
}

.lab-item-card .el-table {
  max-height: calc(100vh - 320px);
  overflow: auto;
}

.lab-item-card .el-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.demo-form-inline {
  margin-bottom: 16px;
}
</style>
