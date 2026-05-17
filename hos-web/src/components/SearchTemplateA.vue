<template>
  <div class="search-template-section">
    <el-card class="template-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">查询模板管理</span>
        <el-button style="float: right; margin-left: 10px;" type="primary" size="small" @click="showAddDialog">新增模板</el-button>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadTemplateList">刷新数据</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="模板类型">
          <el-select v-model="queryParams.templateType" placeholder="请选择类型" clearable style="width: 180px;">
            <el-option label="CT检查" value="ct"></el-option>
            <el-option label="核磁检查" value="mri"></el-option>
            <el-option label="病理检查" value="pathology"></el-option>
            <el-option label="肠镜检查" value="enteroscopy"></el-option>
            <el-option label="检验结果" value="lab"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="templateList" :header-cell-style="{background:'#868a8c',color:'#333'}" border style="width: 100%" v-loading="loading">
        <el-table-column prop="templateId" label="ID" width="70" align="center"></el-table-column>
        <el-table-column prop="templateName" label="模板名称" min-width="140"></el-table-column>
        <el-table-column prop="templateType" label="模板类型" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="typeTagMap[scope.row.templateType]" size="small">
              {{ typeNameMap[scope.row.templateType] || scope.row.templateType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="共享状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isShared ? 'success' : 'info'" size="small">
              {{ scope.row.isShared ? '已共享' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleApply(scope.row)">应用</el-button>
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleShare(scope.row)">
              {{ scope.row.isShared ? '取消共享' : '共享' }}
            </el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="formData.templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="formData.templateType" placeholder="请选择模板类型" style="width: 100%;">
            <el-option label="CT检查" value="ct"></el-option>
            <el-option label="核磁检查" value="mri"></el-option>
            <el-option label="病理检查" value="pathology"></el-option>
            <el-option label="肠镜检查" value="enteroscopy"></el-option>
            <el-option label="检验结果" value="lab"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="共享模板">
          <el-switch v-model="formData.isShared" active-text="共享" inactive-text="私有"></el-switch>
        </el-form-item>
        <el-divider content-position="left">查询条件</el-divider>
        <el-form-item label="患者ID">
          <el-input v-model="formData.queryConditions.patientId" placeholder="请输入患者ID"></el-input>
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="formData.queryConditions.patientName" placeholder="请输入患者姓名"></el-input>
        </el-form-item>
        <el-form-item label="检查编号" v-if="formData.templateType !== 'lab'">
          <el-input v-model="formData.queryConditions.examinationNo" placeholder="请输入检查编号"></el-input>
        </el-form-item>
        <el-form-item label="检查部位" v-if="formData.templateType === 'ct' || formData.templateType === 'mri'">
          <el-input v-model="formData.queryConditions.examinationPart" placeholder="请输入检查部位"></el-input>
        </el-form-item>
        <el-form-item label="检查医生" v-if="formData.templateType !== 'lab'">
          <el-input v-model="formData.queryConditions.examineDoctor" placeholder="请输入检查医生"></el-input>
        </el-form-item>
        <el-form-item label="检查科室">
          <el-input v-model="formData.queryConditions.examineDept" placeholder="请输入检查科室"></el-input>
        </el-form-item>
        <el-form-item label="开始时间" v-if="formData.templateType !== 'lab'">
          <el-date-picker v-model="formData.queryConditions.startTime" type="datetime" placeholder="选择开始时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" v-if="formData.templateType !== 'lab'">
          <el-date-picker v-model="formData.queryConditions.endTime" type="datetime" placeholder="选择结束时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;"></el-date-picker>
        </el-form-item>
        <el-form-item label="项目名称" v-if="formData.templateType === 'lab'">
          <el-input v-model="formData.queryConditions.itemName" placeholder="请输入检验项目名称"></el-input>
        </el-form-item>
        <el-form-item label="报告开始" v-if="formData.templateType === 'lab'">
          <el-date-picker v-model="formData.queryConditions.reportStartTime" type="datetime" placeholder="选择报告开始时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;"></el-date-picker>
        </el-form-item>
        <el-form-item label="报告结束" v-if="formData.templateType === 'lab'">
          <el-date-picker v-model="formData.queryConditions.reportEndTime" type="datetime" placeholder="选择报告结束时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;"></el-date-picker>
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
  name: "SearchTemplateA",
  data() {
    return {
      templateList: [],
      loading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,
      queryParams: { templateType: '' },
      dialogVisible: false,
      dialogTitle: '新增查询模板',
      isEdit: false,
      submitLoading: false,
      formData: {
        templateId: null,
        templateName: '',
        templateType: 'ct',
        isShared: false,
        queryConditions: {
          patientId: '',
          patientName: '',
          examinationNo: '',
          examinationPart: '',
          examineDoctor: '',
          examineDept: '',
          startTime: '',
          endTime: '',
          itemName: '',
          reportStartTime: '',
          reportEndTime: ''
        }
      },
      formRules: {
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
      },
      typeTagMap: { ct: '', mri: 'success', pathology: 'warning', enteroscopy: 'danger', lab: 'info' },
      typeNameMap: { ct: 'CT检查', mri: '核磁检查', pathology: '病理检查', enteroscopy: '肠镜检查', lab: '检验结果' },
      allData: []
    }
  },
  methods: {
    loadTemplateList() {
      this.loading = true;
      const params = {};
      if (this.queryParams.templateType) {
        params.templateType = this.queryParams.templateType;
      }
      this.$axios.get('/searchTemplate/list', { params })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.allData = res.data || [];
            this.total = this.allData.length;
            this.templateList = this.allData.slice(
              (this.pageNum - 1) * this.pageSize,
              this.pageNum * this.pageSize
            );
          } else {
            this.$message.error(res.msg || '获取模板列表失败');
          }
        })
        .catch(() => {
          this.$message.error('获取模板列表失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleQuery() {
      this.pageNum = 1;
      this.loadTemplateList();
    },
    resetQuery() {
      this.queryParams = { templateType: '' };
      this.handleQuery();
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadTemplateList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadTemplateList();
    },
    showAddDialog() {
      this.isEdit = false;
      this.dialogTitle = '新增查询模板';
      this.formData = {
        templateId: null,
        templateName: '',
        templateType: 'ct',
        isShared: false,
        queryConditions: {
          patientId: '', patientName: '', examinationNo: '', examinationPart: '',
          examineDoctor: '', examineDept: '', startTime: '', endTime: '',
          itemName: '', reportStartTime: '', reportEndTime: ''
        }
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.formRef) this.$refs.formRef.resetFields();
      });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑查询模板';
      let conditions = {};
      try {
        conditions = JSON.parse(row.queryConditions || '{}');
      } catch (e) {
        conditions = {};
      }
      this.formData = {
        templateId: row.templateId,
        templateName: row.templateName,
        templateType: row.templateType,
        isShared: row.isShared,
        queryConditions: {
          patientId: conditions.patientId || '',
          patientName: conditions.patientName || '',
          examinationNo: conditions.examinationNo || '',
          examinationPart: conditions.examinationPart || '',
          examineDoctor: conditions.examineDoctor || '',
          examineDept: conditions.examineDept || '',
          startTime: conditions.startTime || '',
          endTime: conditions.endTime || '',
          itemName: conditions.itemName || '',
          reportStartTime: conditions.reportStartTime || '',
          reportEndTime: conditions.reportEndTime || ''
        }
      };
      this.dialogVisible = true;
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          this.submitLoading = true;
          const cleanConditions = {};
          Object.keys(this.formData.queryConditions).forEach(key => {
            if (this.formData.queryConditions[key]) {
              cleanConditions[key] = this.formData.queryConditions[key];
            }
          });
          const payload = {
            templateId: this.formData.templateId,
            templateName: this.formData.templateName,
            templateType: this.formData.templateType,
            isShared: this.formData.isShared,
            queryConditions: JSON.stringify(cleanConditions)
          };
          const url = this.isEdit ? '/searchTemplate/update' : '/searchTemplate/save';
          const method = this.isEdit ? 'put' : 'post';
          this.$axios[method](url, payload)
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success(this.isEdit ? '更新成功' : '新增成功');
                this.dialogVisible = false;
                this.loadTemplateList();
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
      this.$confirm('确定删除模板【' + row.templateName + '】吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete('/searchTemplate/' + row.templateId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadTemplateList();
            } else {
              this.$message.error(res.msg || '删除失败');
            }
          })
          .catch(() => this.$message.error('删除失败'));
      }).catch(() => {});
    },
    handleShare(row) {
      const action = row.isShared ? '取消共享' : '共享';
      this.$confirm('确定' + action + '模板【' + row.templateName + '】吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.$axios.put('/searchTemplate/share/' + row.templateId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success(action + '成功');
              this.loadTemplateList();
            } else {
              this.$message.error(res.msg || '操作失败');
            }
          })
          .catch(() => this.$message.error('操作失败'));
      }).catch(() => {});
    },
    handleApply(row) {
      let conditions = {};
      try {
        conditions = JSON.parse(row.queryConditions || '{}');
      } catch (e) {
        conditions = {};
      }
      const routeMap = {
        ct: '/IndexA/check/ct',
        mri: '/IndexA/check/mri',
        pathology: '/IndexA/pathology',
        enteroscopy: '/IndexA/check/colonoscopy',
        lab: '/IndexA/labResult'
      };
      const targetRoute = routeMap[row.templateType];
      if (!targetRoute) {
        this.$message.warning('未知的模板类型');
        return;
      }
      const query = {};
      Object.keys(conditions).forEach(key => {
        if (conditions[key]) {
          query[key] = conditions[key];
        }
      });
      this.$router.push({ path: targetRoute, query: query });
      this.$message.success('已应用模板：' + row.templateName);
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return timeStr.replace('T', ' ').substring(0, 19);
    }
  },
  mounted() {
    this.loadTemplateList();
  }
}
</script>

<style scoped>
.search-template-section {
  padding: 20px;
}

.template-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
