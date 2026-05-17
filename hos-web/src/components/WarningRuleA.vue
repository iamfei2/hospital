<template>
  <div class="warning-rule-section">
    <el-card>
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">预警规则配置</span>
        <el-button style="float: right; margin-left: 10px;" type="warning" size="small" @click="rescanLab" :loading="scanLoading">扫描检验结果</el-button>
        <el-button style="float: right; margin-left: 10px;" type="warning" size="small" @click="rescanExam" :loading="scanLoading">扫描检查结果</el-button>
        <el-button style="float: right;" type="primary" size="small" @click="showAddDialog">新增规则</el-button>
      </div>

        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="规则类型">
          <el-select v-model="queryParams.ruleType" placeholder="全部" clearable style="width: 150px;">
            <el-option label="指标异常" value="THRESHOLD"></el-option>
            <el-option label="关键词预警" value="KEYWORD"></el-option>
            <el-option label="数据缺失" value="MISSING"></el-option>
            <el-option label="操作异常" value="OPERATION"></el-option>
            <el-option label="多指标联合" value="ISOLATION_FOREST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="严重级别">
          <el-select v-model="queryParams.severity" placeholder="全部" clearable style="width: 120px;">
            <el-option label="提示" value="INFO"></el-option>
            <el-option label="警告" value="WARNING"></el-option>
            <el-option label="危急" value="CRITICAL"></el-option>
            <el-option label="紧急" value="EMERGENCY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.enabled" placeholder="全部" clearable style="width: 100px;">
            <el-option label="启用" :value="true"></el-option>
            <el-option label="禁用" :value="false"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="ruleTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="ruleName" label="规则名称" min-width="150"></el-table-column>
        <el-table-column prop="ruleType" label="规则类型" width="120" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.ruleType === 'THRESHOLD'" type="primary">指标异常</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'KEYWORD'" type="success">关键词预警</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'MISSING'" type="warning">数据缺失</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'OPERATION'" type="danger">操作异常</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'ISOLATION_FOREST'" type="info">多指标联合</el-tag>
            <span v-else>{{ scope.row.ruleType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conditionType" label="条件类型" width="120" align="center"></el-table-column>
        <el-table-column label="阈值范围" width="150" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.thresholdLow != null && scope.row.thresholdHigh != null">
              {{ scope.row.thresholdLow }} ~ {{ scope.row.thresholdHigh }}
            </span>
            <span v-else-if="scope.row.thresholdHigh != null">
              > {{ scope.row.thresholdHigh }}
            </span>
            <span v-else-if="scope.row.thresholdLow != null">
              &lt; {{ scope.row.thresholdLow }}
            </span>
            <span v-else-if="scope.row.missingDays != null">
              {{ scope.row.missingDays }}天
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重级别" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.severity === 'EMERGENCY'" type="danger">紧急</el-tag>
            <el-tag v-else-if="scope.row.severity === 'CRITICAL'" type="danger">危急</el-tag>
            <el-tag v-else-if="scope.row.severity === 'WARNING'" type="warning">警告</el-tag>
            <el-tag v-else type="info">提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enabled" @change="toggleRule(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right;">
      </el-pagination>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" :close-on-click-modal="false">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="editForm.ruleName" placeholder="请输入规则名称"></el-input>
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="editForm.ruleType" placeholder="请选择规则类型" style="width: 100%;" @change="handleRuleTypeChange">
            <el-option label="指标异常" value="THRESHOLD"></el-option>
            <el-option label="关键词预警" value="KEYWORD"></el-option>
            <el-option label="数据缺失" value="MISSING"></el-option>
            <el-option label="操作异常" value="OPERATION"></el-option>
            <el-option label="多指标联合" value="ISOLATION_FOREST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="指标组合" v-if="editForm.ruleType === 'ISOLATION_FOREST'" prop="itemIds">
          <el-select v-model="editForm.itemIds" multiple placeholder="请选择要组合的检验项目（至少选择2个）" style="width: 100%;">
            <el-option v-for="item in labItems" :key="item.itemId" :label="item.itemName" :value="item.itemId"></el-option>
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">选择多个检验项目进行联合异常检测</div>
        </el-form-item>
        <el-form-item label="异常阈值" v-if="editForm.ruleType === 'ISOLATION_FOREST'">
          <el-input-number v-model="editForm.thresholdScore" :min="0" :max="1" :step="0.1" :precision="2" style="width: 100%;"></el-input-number>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">联合异常得分阈值（0-1之间，默认0.5）</div>
        </el-form-item>
        <el-form-item label="条件类型" prop="conditionType" v-if="editForm.ruleType">
          <el-select v-model="editForm.conditionType" placeholder="请选择条件类型" style="width: 100%;" @change="handleConditionTypeChange">
            <template v-if="editForm.ruleType === 'THRESHOLD'">
              <el-option label="高于上限" value="ABOVE"></el-option>
              <el-option label="低于下限" value="BELOW"></el-option>
              <el-option label="范围外" value="RANGE"></el-option>
              <el-option label="骤升（当前vs上次）" value="TREND_UP"></el-option>
              <el-option label="骤降（当前vs上次）" value="TREND_DOWN"></el-option>
              <el-option label="连续上升（近两次）" value="CONTINUE_UP"></el-option>
              <el-option label="连续下降（近两次）" value="CONTINUE_DOWN"></el-option>
              <el-option label="Z-Score偏高" value="ZSCORE_ABOVE"></el-option>
              <el-option label="Z-Score偏低" value="ZSCORE_BELOW"></el-option>
            </template>
            <template v-else-if="editForm.ruleType === 'KEYWORD'">
              <el-option label="关键词匹配" value="KEYWORD"></el-option>
            </template>
            <template v-else-if="editForm.ruleType === 'MISSING'">
              <el-option label="缺失天数" value="MISSING_DAYS"></el-option>
            </template>
            <template v-else-if="editForm.ruleType === 'OPERATION'">
              <el-option label="批量删除" value="ABOVE"></el-option>
              <el-option label="非工作时间" value="BELOW"></el-option>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="检验项目" v-if="editForm.ruleType === 'THRESHOLD'" prop="itemId">
          <el-select v-model="editForm.itemId" placeholder="请选择检验项目" style="width: 100%;" clearable>
            <el-option v-for="item in labItems" :key="item.itemId" :label="item.itemName" :value="item.itemId"></el-option>
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">选择要监控的检验指标项目</div>
        </el-form-item>
        <el-form-item label="关键词" v-if="editForm.ruleType === 'KEYWORD'" prop="description">
          <el-input v-model="editForm.description" placeholder="请输入关键词，多个用逗号分隔，如：恶性,疑似,可疑"></el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">当报告结论包含任意关键词时触发预警</div>
        </el-form-item>
        <el-form-item label="下班时间" v-if="editForm.ruleType === 'OPERATION' && editForm.conditionType === 'BELOW'" prop="offHourStart">
          <el-input-number v-model="editForm.offHourStart" :min="0" :max="23" style="width: 100%;"></el-input-number>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">时（默认22时，22:00后为非工作时间）</span>
        </el-form-item>
        <el-form-item label="上班时间" v-if="editForm.ruleType === 'OPERATION' && editForm.conditionType === 'BELOW'" prop="offHourEnd">
          <el-input-number v-model="editForm.offHourEnd" :min="0" :max="23" style="width: 100%;"></el-input-number>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">时（默认6时，06:00前为非工作时间）</span>
        </el-form-item>
        <el-form-item label="休息日" v-if="editForm.ruleType === 'OPERATION' && editForm.conditionType === 'BELOW'">
          <el-checkbox-group v-model="offDaysSelected">
            <el-checkbox label="0">周日</el-checkbox>
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
          </el-checkbox-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">默认周六、周日为休息日</div>
        </el-form-item>
        <el-form-item label="下限阈值" v-if="showLowThreshold">
          <el-input-number v-model="editForm.thresholdLow" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="上限阈值" v-if="showHighThreshold">
          <el-input-number v-model="editForm.thresholdHigh" :precision="2" style="width: 100%;"></el-input-number>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;" v-if="['TREND_UP', 'TREND_DOWN'].includes(editForm.conditionType)">
            骤升/骤降：当前值与最近一次历史值的变化百分比，如设20表示变化超过20%时触发
          </div>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;" v-if="['CONTINUE_UP', 'CONTINUE_DOWN'].includes(editForm.conditionType)">
            连续上升/下降：最近一次与上两次历史值的变化百分比，如设20表示连续变化超过20%时触发
          </div>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;" v-if="['ZSCORE_ABOVE', 'ZSCORE_BELOW'].includes(editForm.conditionType)">
            Z-Score阈值：如设2表示偏离均值超过2个标准差时触发预警
          </div>
        </el-form-item>
        <el-form-item label="缺失天数" v-if="showMissingDays">
          <el-input-number v-model="editForm.missingDays" :min="1" :max="365" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="严重级别" prop="severity">
          <el-select v-model="editForm.severity" placeholder="请选择严重级别" style="width: 100%;">
            <el-option label="提示" value="INFO"></el-option>
            <el-option label="警告" value="WARNING"></el-option>
            <el-option label="危急" value="CRITICAL"></el-option>
            <el-option label="紧急" value="EMERGENCY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入规则描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "WarningRuleA",
  data() {
    return {
      queryParams: { ruleType: '', severity: '', enabled: '' },
      ruleTableData: [],
      loading: false,
      scanLoading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,
      dialogVisible: false,
      submitLoading: false,
      isEdit: false,
      offDaysSelected: ['0', '6'],
      editForm: {
        ruleId: null, ruleName: '', ruleType: '', conditionType: '', itemId: null,
        itemIds: [], thresholdScore: 0.5,
        thresholdLow: null, thresholdHigh: null, missingDays: null,
        severity: 'WARNING', enabled: true, description: '',
        offHourStart: 22, offHourEnd: 6, offDays: '0,6'
      },
      labItems: [],
      editRules: {
        ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
        conditionType: [{ required: true, message: '请选择条件类型', trigger: 'change' }],
        severity: [{ required: true, message: '请选择严重级别', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑预警规则' : '新增预警规则'; },
    showHighThreshold() {
      return this.editForm.ruleType === 'THRESHOLD' && ['ABOVE', 'RANGE', 'TREND_UP', 'TREND_DOWN', 'CONTINUE_UP', 'CONTINUE_DOWN', 'ZSCORE_ABOVE', 'ZSCORE_BELOW'].includes(this.editForm.conditionType);
    },
    showLowThreshold() {
      return this.editForm.ruleType === 'THRESHOLD' && ['BELOW', 'RANGE', 'ZSCORE_BELOW'].includes(this.editForm.conditionType);
    },
    showMissingDays() {
      return this.editForm.ruleType === 'MISSING' && this.editForm.conditionType === 'MISSING_DAYS';
    },
    showThresholdFields() {
      return this.editForm.ruleType === 'THRESHOLD';
    },
    showItemSelector() {
      return this.editForm.ruleType === 'THRESHOLD';
    }
  },
  methods: {
    handleRuleTypeChange() {
      this.editForm.conditionType = '';
      this.editForm.itemId = null;
      this.editForm.itemIds = [];
      this.editForm.thresholdScore = 0.5;
      this.editForm.thresholdLow = null;
      this.editForm.thresholdHigh = null;
      this.editForm.missingDays = null;
      this.editForm.offHourStart = 22;
      this.editForm.offHourEnd = 6;
      this.offDaysSelected = ['0', '6'];
    },
    handleConditionTypeChange() {
      if (this.editForm.ruleType === 'THRESHOLD') {
        this.editForm.thresholdLow = null;
        this.editForm.thresholdHigh = null;
        if (['ZSCORE_ABOVE', 'ZSCORE_BELOW'].includes(this.editForm.conditionType)) {
          this.editForm.thresholdHigh = 2.0;
        }
      }
    },
    loadLabItems() {
      this.$axios.get('/labItemDict/list').then(res => res.data).then(res => {
        if (res.code === 200) {
          this.labItems = res.data || [];
        }
      }).catch(() => {});
    },
    loadRules() {
      this.loading = true;
      this.$axios.post('/warningRule/page', {
        pageNum: this.pageNum, pageSize: this.pageSize, param: this.queryParams
      }).then(res => res.data).then(res => {
        if (res.code === 200) {
          this.ruleTableData = res.data || [];
          this.total = res.total || 0;
        } else {
          this.$message.error('查询失败');
        }
      }).catch(() => { this.$message.error('查询出错'); })
        .finally(() => { this.loading = false; });
    },
    resetQuery() {
      this.queryParams = { ruleType: '', severity: '', enabled: '' };
      this.pageNum = 1;
      this.loadRules();
    },
    handleSizeChange(val) { this.pageSize = val; this.loadRules(); },
    handleCurrentChange(val) { this.pageNum = val; this.loadRules(); },
    showAddDialog() {
      this.isEdit = false;
      this.offDaysSelected = ['0', '6'];
      this.editForm = { ruleId: null, ruleName: '', ruleType: '', conditionType: '', itemId: null,
        itemIds: [], thresholdScore: 0.5,
        thresholdLow: null, thresholdHigh: null, missingDays: null,
        severity: 'WARNING', enabled: true, description: '',
        offHourStart: 22, offHourEnd: 6, offDays: '0,6' };
      this.dialogVisible = true;
    },
    showEditDialog(row) {
      this.isEdit = true;
      this.editForm = { ...row };
      if (row.offDays) {
        this.offDaysSelected = row.offDays.split(',');
      } else {
        this.offDaysSelected = ['0', '6'];
      }
      if (row.ruleType === 'ISOLATION_FOREST' && row.itemIds) {
        this.editForm.itemIds = row.itemIds.split(',').map(id => parseInt(id.trim()));
        this.editForm.thresholdScore = row.thresholdScore || 0.5;
      }
      this.dialogVisible = true;
    },
    handleSubmit() {
      this.$refs.editForm.validate(valid => {
        if (!valid) return;
        if (this.editForm.ruleType === 'ISOLATION_FOREST' && (!this.editForm.itemIds || this.editForm.itemIds.length < 2)) {
          this.$message.error('请至少选择2个检验项目进行联合检测');
          return;
        }
        this.submitLoading = true;
        const submitData = { ...this.editForm };
        if (submitData.ruleType === 'OPERATION' && submitData.conditionType === 'BELOW') {
          submitData.offDays = this.offDaysSelected.join(',');
        }
        if (submitData.ruleType === 'ISOLATION_FOREST') {
          submitData.itemIds = submitData.itemIds.join(',');
          submitData.conditionType = 'ISOLATION_FOREST';
          const url = this.isEdit ? '/isolationForest/rule/' + submitData.ruleId : '/isolationForest/rule';
          this.$axios.post(url, submitData).then(res => res.data).then(res => {
            if (res.code === 200) {
              this.$message.success(this.isEdit ? '修改成功' : '添加成功');
              this.dialogVisible = false;
              this.loadRules();
            } else {
              this.$message.error(res.msg || '操作失败');
            }
          }).catch(() => { this.$message.error('操作失败'); })
            .finally(() => { this.submitLoading = false; });
          return;
        }
        const url = this.isEdit ? '/warningRule/update' : '/warningRule/add';
        this.$axios.post(url, submitData).then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success(this.isEdit ? '修改成功' : '添加成功');
            this.dialogVisible = false;
            this.loadRules();
          } else {
            this.$message.error(res.msg || '操作失败');
          }
        }).catch(() => { this.$message.error('操作失败'); })
          .finally(() => { this.submitLoading = false; });
      });
    },
    handleDelete(row) {
      this.$confirm('确定要删除该规则吗?', '提示', { type: 'warning' }).then(() => {
        const url = row.ruleType === 'ISOLATION_FOREST' 
            ? '/isolationForest/rule/' + row.ruleId 
            : '/warningRule/' + row.ruleId;
        this.$axios.delete(url).then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadRules();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        });
      });
    },
    toggleRule(row) {
      this.$axios.post('/warningRule/toggle/' + row.ruleId).then(res => res.data).then(res => {
        if (res.code === 200) {
          this.$message.success('操作成功');
        } else {
          row.enabled = !row.enabled;
          this.$message.error('操作失败');
        }
      });
    },
    rescanLab() {
      this.$confirm('将扫描所有历史检验结果，触发超过阈值的预警记录。是否继续？', '提示', { type: 'warning' }).then(() => {
        this.scanLoading = true;
        this.$axios.post('/warningRule/rescanLab').then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success(res.msg || '扫描完成');
          } else {
            this.$message.error(res.msg || '扫描失败');
          }
        }).catch(() => { this.$message.error('扫描失败'); })
          .finally(() => { this.scanLoading = false; });
      });
    },
    rescanExam() {
      this.$confirm('将扫描所有历史检查结果，触发包含关键词的预警记录。是否继续？', '提示', { type: 'warning' }).then(() => {
        this.scanLoading = true;
        this.$axios.post('/warningRule/rescanExam').then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success(res.msg || '扫描完成');
          } else {
            this.$message.error(res.msg || '扫描失败');
          }
        }).catch(() => { this.$message.error('扫描失败'); })
          .finally(() => { this.scanLoading = false; });
      });
    }
  },
  mounted() { 
    this.loadRules();
    this.loadLabItems();
  }
}
</script>

<style scoped>
.warning-rule-section { padding: 0; }
.demo-form-inline { margin-bottom: 16px; }
</style>
