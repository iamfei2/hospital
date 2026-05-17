<template>
  <div class="page-wrapper">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">检查统计分析</h1>
        <p class="page-desc">按时间周期统计各类检查数据</p>
      </div>
    </div>

    <!-- 统计Tab -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="检查数量统计" name="count">
        <!-- 查询区域 -->
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">统计周期</span>
            <el-radio-group v-model="periodType" size="medium" @change="handlePeriodChange">
              <el-radio-button label="day">日</el-radio-button>
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-item">
            <span class="filter-label">时间范围</span>
            <el-date-picker
                v-model="dateRange"
                :type="datePickerType"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                size="medium"
                style="width: 340px;">
            </el-date-picker>
          </div>
          <div class="filter-item">
            <span class="filter-label">检查类型</span>
            <el-checkbox-group v-model="searchForm.types" class="type-checkboxes">
              <el-checkbox label="ct">CT</el-checkbox>
              <el-checkbox label="mri">核磁</el-checkbox>
              <el-checkbox label="enteroscopy">肠镜</el-checkbox>
              <el-checkbox label="pathology">病理</el-checkbox>
            </el-checkbox-group>
          </div>
          <el-button type="primary" @click="handleSearch" :loading="loading" size="medium">开始统计</el-button>
          <el-button @click="handleExport" :disabled="!statData" size="medium">导出Excel</el-button>
        </div>

        <!-- 统计结果 -->
        <div class="result-area" v-if="statData">
          <div class="count-row">
            <div class="count-item" v-for="(item, index) in (statData.typeLabels || statData.labels)" :key="index">
              <div class="count-num">{{ (statData.typeTotals || statData.values)[index] }}</div>
              <div class="count-name">{{ item }}</div>
            </div>
            <div class="count-item total-item">
              <div class="count-num">{{ statData.typeTotals ? statData.typeTotals.reduce((a,b)=>a+b,0) : statData.total }}</div>
              <div class="count-name">合计</div>
            </div>
          </div>

          <div class="charts-row">
            <div class="chart-box">
              <div class="chart-title">{{ statData.periods ? '时间趋势统计' : '数量对比' }}</div>
              <div ref="barChart" class="chart-body"></div>
            </div>
            <div class="chart-box">
              <div class="chart-title">占比分布</div>
              <div ref="pieChart" class="chart-body"></div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="科室月度检查量" name="monthly">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">时间范围</span>
            <el-date-picker
                v-model="monthlyRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                value-format="yyyy-MM"
                size="medium"
                style="width: 280px;">
            </el-date-picker>
          </div>
          <el-button type="primary" @click="loadMonthlyStats" :loading="loading" size="medium">查询</el-button>
        </div>

        <div class="result-area" v-if="monthlyData">
          <div class="chart-box" style="width: 100%;">
            <div class="chart-title">科室月度检查量趋势</div>
            <div ref="monthlyChart" class="chart-body" style="height: 400px;"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="医生工作量统计" name="doctor">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">时间范围</span>
            <el-date-picker
                v-model="doctorRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                value-format="yyyy-MM"
                size="medium"
                style="width: 280px;">
            </el-date-picker>
          </div>
          <el-button type="primary" @click="loadDoctorStats" :loading="loading" size="medium">查询</el-button>
        </div>

        <div class="result-area" v-if="doctorData">
          <div class="chart-box" style="width: 100%;">
            <div class="chart-title">医生工作量排名（Top 10）</div>
            <div ref="doctorChart" class="chart-body" style="height: 400px;"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="患者指标趋势" name="patient">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">患者ID</span>
            <el-input v-model="patientForm.patientId" placeholder="请输入患者ID" style="width: 150px;" size="medium"></el-input>
          </div>
          <div class="filter-item">
            <span class="filter-label">检验项目</span>
            <el-select v-model="patientForm.itemId" placeholder="请选择检验项目" style="width: 200px;" size="medium">
              <el-option v-for="item in labItems" :key="item.itemId" :label="item.itemName" :value="item.itemId"></el-option>
            </el-select>
          </div>
          <el-button type="primary" @click="loadPatientTrend" :loading="loading" size="medium">查询趋势</el-button>
          <el-button @click="handlePatientExport" :disabled="!patientData" size="medium">导出Excel</el-button>
        </div>

        <div class="result-area" v-if="patientData">
          <div class="chart-box" style="width: 100%;">
            <div class="chart-title">{{ patientData.itemName }} - 患者指标趋势</div>
            <div ref="patientChart" class="chart-body" style="height: 400px;"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="自定义统计" name="custom">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">主维度</span>
            <el-select v-model="customForm.primaryDim" placeholder="请选择主维度" style="width: 160px;" size="medium" @change="handlePrimaryDimChange">
              <el-option label="按科室" value="byDept"></el-option>
              <el-option label="按医生" value="byDoctor"></el-option>
              <el-option label="按月份" value="byMonth"></el-option>
              <el-option label="按周" value="byWeek"></el-option>
              <el-option label="按日期" value="byDay"></el-option>
              <el-option label="按检查部位" value="byExaminationPart"></el-option>
              <el-option label="按标本类型" value="bySpecimenType"></el-option>
              <el-option label="按肠镜类型" value="byEnteroscopyType"></el-option>
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">次维度</span>
            <el-select v-model="customForm.secondaryDim" placeholder="可选" style="width: 160px;" size="medium" clearable>
              <el-option label="无" value=""></el-option>
              <el-option label="按科室" value="byDept"></el-option>
              <el-option label="按医生" value="byDoctor"></el-option>
              <el-option label="按月份" value="byMonth"></el-option>
              <el-option label="按周" value="byWeek"></el-option>
              <el-option label="按日期" value="byDay"></el-option>
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">检查类型</span>
            <el-checkbox-group v-model="customForm.tables" class="type-checkboxes">
              <el-checkbox label="ct">CT</el-checkbox>
              <el-checkbox label="mri">核磁</el-checkbox>
              <el-checkbox label="enteroscopy">肠镜</el-checkbox>
              <el-checkbox label="pathology">病理</el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="filter-item">
            <span class="filter-label">时间范围</span>
            <el-date-picker
                v-model="customForm.dateRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                value-format="yyyy-MM"
                size="medium"
                style="width: 260px;">
            </el-date-picker>
          </div>
          <el-button type="primary" @click="handleCustomStat" :loading="loading" size="medium">查询</el-button>
          <el-button @click="handleSaveTemplate" size="medium">保存模板</el-button>
          <el-button @click="showTemplateDialog = true" size="medium">加载模板</el-button>
        </div>

        <div class="result-area" v-if="customData">
          <div class="count-row" v-if="customData.xAxis && customData.xAxis.length > 0">
            <div class="count-item" v-for="(item, index) in customData.xAxis.slice(0, 6)" :key="index">
              <div class="count-num">{{ getCustomTotal(customData.series, index) }}</div>
              <div class="count-name">{{ item }}</div>
            </div>
          </div>

          <div class="charts-row">
            <div class="chart-box" style="width: 100%;">
              <div class="chart-title">自定义统计结果</div>
              <div ref="customChart" class="chart-body" style="height: 400px;"></div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="统计模板" :visible.sync="showTemplateDialog" width="500px">
      <el-table :data="templateList" @row-click="loadTemplate" highlight-current-row border>
        <el-table-column prop="templateName" label="模板名称"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click.stop="deleteTemplate(scope.row.templateId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showTemplateDialog = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="保存模板" :visible.sync="showSaveDialog" width="400px">
      <el-form label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveTemplate">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "StatisticsA",
  data() {
    return {
      loading: false,
      activeTab: 'count',
      periodType: 'month',
      dateRange: null,
      monthlyRange: null,
      doctorRange: null,
      searchForm: {
        startTime: '',
        endTime: '',
        types: ['ct', 'mri', 'enteroscopy', 'pathology']
      },
      statData: null,
      monthlyData: null,
      doctorData: null,
      patientData: null,
      labItems: [],
      patientForm: {
        patientId: '',
        itemId: null
      },
      barChart: null,
      pieChart: null,
      monthlyChart: null,
      doctorChart: null,
      patientChart: null,
      customChart: null,
      customForm: {
        primaryDim: 'byDept',
        secondaryDim: '',
        tables: ['ct', 'mri', 'enteroscopy', 'pathology'],
        dateRange: null
      },
      customData: null,
      showTemplateDialog: false,
      showSaveDialog: false,
      templateList: [],
      templateName: ''
    }
  },
  computed: {
    datePickerType() {
      switch (this.periodType) {
        case 'day': return 'daterange';
        case 'week': return 'daterange';
        case 'month': return 'monthrange';
        case 'year': return 'monthrange';
        default: return 'daterange';
      }
    }
  },
  mounted() {
    this.loadLabItems();
    this.setDefaultDateRange();
  },
  methods: {
    setDefaultDateRange() {
      const now = new Date();
      let start, end;
      if (this.periodType === 'day' || this.periodType === 'week') {
        start = new Date(now); start.setDate(now.getDate() - 7);
        end = new Date(now);
        this.dateRange = [this.formatDate(start), this.formatDate(end)];
      } else if (this.periodType === 'month' || this.periodType === 'year') {
        start = new Date(now); start.setMonth(now.getMonth() - 3);
        end = new Date(now);
        this.dateRange = [this.formatMonth(start), this.formatMonth(end)];
      }
    },
    handlePeriodChange() {
      this.setDefaultDateRange();
    },
    formatDate(date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    },
    formatMonth(date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      return `${y}-${m}`;
    },
    getEndOfMonth(yearMonth) {
      const [year, month] = yearMonth.split('-').map(Number);
      const lastDay = new Date(year, month, 0).getDate();
      return `${yearMonth}-${String(lastDay).padStart(2, '0')} 23:59:59`;
    },
    handleTabChange() {
      if (this.activeTab === 'patient' && this.labItems.length === 0) {
        this.loadLabItems();
      }
      if (this.activeTab === 'custom') {
        this.loadTemplateList();
        this.initCustomChart();
      }
    },
    handlePrimaryDimChange() {
      this.customForm.secondaryDim = '';
    },
    handleCustomStat() {
      if (!this.customForm.dateRange || this.customForm.dateRange.length !== 2) {
        this.$message.warning('请选择时间范围');
        return;
      }
      if (this.customForm.tables.length === 0) {
        this.$message.warning('请至少选择一个检查类型');
        return;
      }
      if (!this.customForm.primaryDim) {
        this.$message.warning('请选择主维度');
        return;
      }

      this.loading = true;
      const dimensions = [this.customForm.primaryDim];
      if (this.customForm.secondaryDim) {
        dimensions.push(this.customForm.secondaryDim);
      }

      const params = {
        dimensions: dimensions,
        tables: this.customForm.tables,
        startTime: this.customForm.dateRange[0] + '-01',
        endTime: this.getEndOfMonth(this.customForm.dateRange[1])
      };

      this.$axios.post('/statistics/dynamic', params)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.customData = res.data;
            this.$nextTick(() => {
              this.initCustomChart();
            });
          } else {
            this.$message.error(res.msg || '统计失败');
          }
        })
        .catch(() => {
          this.$message.error('统计失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getCustomTotal(series, index) {
      if (!series) return 0;
      return series.reduce((sum, s) => sum + (s.data[index] || 0), 0);
    },
    initCustomChart() {
      if (this.customChart) this.customChart.dispose();
      if (!this.$refs.customChart) return;
      this.customChart = echarts.init(this.$refs.customChart);

      if (!this.customData || !this.customData.xAxis || this.customData.xAxis.length === 0) {
        this.customChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center' }
        });
        return;
      }

      const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585', '#00ced1'];

      if (this.customData.series && this.customData.series.length > 1) {
        const seriesData = this.customData.series.map((s, i) => ({
          name: s.name,
          type: 'bar',
          data: s.data,
          itemStyle: { color: colors[i % colors.length] }
        }));

        this.customChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          legend: { bottom: 0 },
          grid: { left: 50, right: 30, top: 30, bottom: 60 },
          xAxis: { type: 'category', data: this.customData.xAxis, axisLine: { lineStyle: { color: '#ddd' } }, axisLabel: { color: '#666', rotate: 30 } },
          yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
          series: seriesData
        });
      } else {
        const barData = this.customData.series && this.customData.series.length === 1
          ? this.customData.series[0].data
          : [];

        this.customChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: 50, right: 30, top: 30, bottom: 40 },
          xAxis: { type: 'category', data: this.customData.xAxis, axisLine: { lineStyle: { color: '#ddd' } }, axisLabel: { color: '#666', rotate: 30 } },
          yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
          series: [{ type: 'bar', data: barData, barWidth: 40, itemStyle: { color: '#409eff' }, label: { show: true, position: 'top' } }]
        });
      }
    },
    loadTemplateList() {
      this.$axios.get('/searchTemplate/list', { params: { templateType: 'STATISTICS' } })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.templateList = res.data || [];
          }
        });
    },
    handleSaveTemplate() {
      if (!this.customForm.primaryDim) {
        this.$message.warning('请先进行统计查询');
        return;
      }
      this.templateName = '';
      this.showSaveDialog = true;
    },
    confirmSaveTemplate() {
      if (!this.templateName || !this.templateName.trim()) {
        this.$message.warning('请输入模板名称');
        return;
      }
      const dimensions = [this.customForm.primaryDim];
      if (this.customForm.secondaryDim) {
        dimensions.push(this.customForm.secondaryDim);
      }
      const templateData = {
        templateName: this.templateName.trim(),
        templateType: 'STATISTICS',
        queryConditions: JSON.stringify({
          dimensions: dimensions,
          tables: this.customForm.tables,
          primaryDim: this.customForm.primaryDim,
          secondaryDim: this.customForm.secondaryDim
        })
      };
      this.$axios.post('/searchTemplate/save', templateData)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('模板保存成功');
            this.showSaveDialog = false;
            this.loadTemplateList();
          } else {
            this.$message.error(res.msg || '保存失败');
          }
        });
    },
    loadTemplate(row) {
      try {
        const conditions = JSON.parse(row.queryConditions);
        this.customForm.primaryDim = conditions.primaryDim || 'byDept';
        this.customForm.secondaryDim = conditions.secondaryDim || '';
        this.customForm.tables = conditions.tables || ['ct', 'mri', 'enteroscopy', 'pathology'];
        this.showTemplateDialog = false;
        this.$message.success('模板已加载，请选择时间范围后查询');
      } catch (e) {
        this.$message.error('模板格式错误');
      }
    },
    deleteTemplate(templateId) {
      this.$axios.delete('/searchTemplate/' + templateId)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadTemplateList();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        });
    },
    loadLabItems() {
      this.$axios.get('/labItemDict/list')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.labItems = res.data || [];
          }
        });
    },
    handleSearch() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        this.$message.warning('请选择时间范围');
        return;
      }
      if (this.searchForm.types.length === 0) {
        this.$message.warning('请至少选择一个检查类型');
        return;
      }

      this.loading = true;
      this.searchForm.startTime = this.dateRange[0];
      this.searchForm.endTime = this.dateRange[1];
      this.searchForm.periodType = this.periodType;

      this.$axios.post('/statistics/count', this.searchForm)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.statData = res.data;
            this.$nextTick(() => {
              this.initCharts();
            });
          } else {
            this.$message.error(res.msg || '统计失败');
          }
        })
        .catch(() => {
          this.$message.error('统计失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadMonthlyStats() {
      if (!this.monthlyRange || this.monthlyRange.length !== 2) {
        this.$message.warning('请选择时间范围');
        return;
      }
      this.loading = true;
      this.$axios.get('/statistics/monthlyByDept', {
        params: {
          startDate: this.monthlyRange[0],
          endDate: this.monthlyRange[1]
        }
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.monthlyData = res.data;
            this.$nextTick(() => {
              this.initMonthlyChart();
            });
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadDoctorStats() {
      if (!this.doctorRange || this.doctorRange.length !== 2) {
        this.$message.warning('请选择时间范围');
        return;
      }
      this.loading = true;
      this.$axios.get('/statistics/doctorWorkload', {
        params: {
          startDate: this.doctorRange[0],
          endDate: this.doctorRange[1]
        }
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.doctorData = res.data;
            this.$nextTick(() => {
              this.initDoctorChart();
            });
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadPatientTrend() {
      if (!this.patientForm.patientId) {
        this.$message.warning('请输入患者ID');
        return;
      }
      if (!this.patientForm.itemId) {
        this.$message.warning('请选择检验项目');
        return;
      }
      this.loading = true;
      this.$axios.get('/statistics/patientTrend', {
        params: this.patientForm
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.patientData = res.data;
            this.$nextTick(() => {
              this.initPatientChart();
            });
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleExport() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        this.$message.warning('请选择时间范围');
        return;
      }
      
      const exportParams = {
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
        types: this.searchForm.types
      };
      
      this.$axios.post('/statistics/exportCount', exportParams, {
        responseType: 'blob'
      })
        .then(res => {
          const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = `检查统计_${this.dateRange[0]}_${this.dateRange[1]}.xlsx`;
          link.click();
          URL.revokeObjectURL(link.href);
        })
        .catch(() => {
          this.$message.error('导出失败');
        });
    },
    handlePatientExport() {
      if (!this.patientData) return;
      
      this.$axios.get('/statistics/exportPatientTrend', {
        params: this.patientForm,
        responseType: 'blob'
      })
        .then(res => {
          const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = `${this.patientData.itemName}_趋势_${this.patientForm.patientId}.xlsx`;
          link.click();
          URL.revokeObjectURL(link.href);
        })
        .catch(() => {
          this.$message.error('导出失败');
        });
    },
    handleReset() {
      this.dateRange = null;
      this.searchForm = {
        startTime: '',
        endTime: '',
        types: ['ct', 'mri', 'enteroscopy', 'pathology']
      };
      this.statData = null;
      this.destroyCharts();
    },
    destroyCharts() {
      if (this.barChart) { this.barChart.dispose(); this.barChart = null; }
      if (this.pieChart) { this.pieChart.dispose(); this.pieChart = null; }
      if (this.monthlyChart) { this.monthlyChart.dispose(); this.monthlyChart = null; }
      if (this.doctorChart) { this.doctorChart.dispose(); this.doctorChart = null; }
      if (this.patientChart) { this.patientChart.dispose(); this.patientChart = null; }
      if (this.customChart) { this.customChart.dispose(); this.customChart = null; }
    },
    initCharts() {
      this.initBarChart();
      this.initPieChart();
    },
    initBarChart() {
      if (this.barChart) this.barChart.dispose();
      this.barChart = echarts.init(this.$refs.barChart);

      if (this.statData.periods && this.statData.series) {
        const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585'];
        const seriesData = this.statData.series.map((s, i) => ({
          name: s.name,
          type: 'bar',
          data: s.data,
          itemStyle: { color: colors[i % colors.length], borderRadius: [4, 4, 0, 0] },
          label: { show: true, position: 'top' }
        }));

        this.barChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          legend: { bottom: 0 },
          grid: { left: 50, right: 30, top: 30, bottom: 60 },
          xAxis: { type: 'category', data: this.statData.periods, axisLine: { lineStyle: { color: '#ddd' } }, axisLabel: { color: '#666' } },
          yAxis: { type: 'value', minInterval: 1, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
          series: seriesData
        });
      } else {
        this.barChart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: 50, right: 30, top: 30, bottom: 40 },
          xAxis: { type: 'category', data: this.statData.labels, axisLine: { lineStyle: { color: '#ddd' } }, axisLabel: { color: '#666' } },
          yAxis: { type: 'value', minInterval: 1, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
          series: [{ type: 'bar', data: this.statData.values, barWidth: 40, itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }, label: { show: true, position: 'top' } }]
        });
      }
    },
    initPieChart() {
      if (this.pieChart) this.pieChart.dispose();
      this.pieChart = echarts.init(this.$refs.pieChart);

      if (this.statData.typeLabels && this.statData.typeTotals) {
        const pieData = this.statData.typeLabels.map((label, index) => ({ name: label, value: this.statData.typeTotals[index] }));
        this.pieChart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', right: 20, top: 'center' },
          series: [{ type: 'pie', radius: ['45%', '70%'], center: ['40%', '50%'], data: pieData, color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'] }]
        });
      } else {
        const pieData = this.statData.labels.map((label, index) => ({ name: label, value: this.statData.values[index] }));
        this.pieChart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', right: 20, top: 'center' },
          series: [{ type: 'pie', radius: ['45%', '70%'], center: ['40%', '50%'], data: pieData, color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'] }]
        });
      }
    },
    initMonthlyChart() {
      if (this.monthlyChart) this.monthlyChart.dispose();
      this.monthlyChart = echarts.init(this.$refs.monthlyChart);
      
      const series = [];
      const deptNames = Object.keys(this.monthlyData.deptData || {});
      const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585', '#00ced1'];
      
      deptNames.forEach((dept, index) => {
        const data = this.monthlyData.months.map(month => (this.monthlyData.deptData[dept] && this.monthlyData.deptData[dept][month]) || 0);
        series.push({
          name: dept,
          type: 'line',
          data: data,
          smooth: true,
          itemStyle: { color: colors[index % colors.length] }
        });
      });
      
      this.monthlyChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { type: 'scroll', bottom: 0 },
        grid: { left: 50, right: 30, top: 30, bottom: 60 },
        xAxis: { type: 'category', data: this.monthlyData.months, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', axisLabel: { color: '#666' } },
        series: series
      });
    },
    initDoctorChart() {
      if (this.doctorChart) this.doctorChart.dispose();
      this.doctorChart = echarts.init(this.$refs.doctorChart);
      this.doctorChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 120, right: 30, top: 30, bottom: 40 },
        xAxis: { type: 'value', axisLabel: { color: '#666' } },
        yAxis: { type: 'category', data: this.doctorData.labels, axisLabel: { color: '#666' } },
        series: [{ type: 'bar', data: this.doctorData.values, itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] }, label: { show: true, position: 'right' } }]
      });
    },
    initPatientChart() {
      if (this.patientChart) this.patientChart.dispose();
      this.patientChart = echarts.init(this.$refs.patientChart);
      
      const refLine = this.patientData.normalRange ? parseFloat(this.patientData.normalRange.split('-')[1]) : null;
      
      this.patientChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 30, top: 30, bottom: 40 },
        xAxis: { type: 'category', data: this.patientData.dates, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', axisLabel: { color: '#666' } },
        series: [
          {
            name: '检验值',
            type: 'line',
            data: this.patientData.values,
            smooth: true,
            itemStyle: { color: '#409eff' },
            markLine: refLine ? {
              silent: true,
              data: [{ yAxis: refLine, name: '正常上限' }],
              lineStyle: { color: '#f56c6c', type: 'dashed' }
            } : undefined
          }
        ]
      });
    }
  },
  beforeDestroy() {
    this.destroyCharts();
  }
}
</script>

<style scoped>
.page-wrapper {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.page-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.filter-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: visible;
}

.filter-label {
  font-size: 13px;
  color: #4e5969;
  white-space: nowrap;
}

.type-checkboxes {
  display: flex;
  gap: 12px;
}

.filter-item .el-date-editor .el-range-separator {
  min-width: 48px !important;
  overflow: visible !important;
  display: inline-block !important;
  white-space: nowrap !important;
  padding: 0 4px !important;
}

.filter-item .el-date-editor.el-range-editor {
  overflow: visible !important;
}

::v-deep .filter-item .el-date-editor .el-range-separator {
  min-width: 48px !important;
  overflow: visible !important;
  display: inline-block !important;
}

::v-deep .filter-item .el-date-editor.el-range-editor {
  overflow: visible !important;
}

.result-area {
  margin-top: 16px;
}

.count-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.count-item {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.count-num {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 6px;
}

.count-name {
  font-size: 13px;
  color: #86909c;
}

.total-item {
  background: #165dff;
}

.total-item .count-num,
.total-item .count-name {
  color: #fff;
}

.charts-row {
  display: flex;
  gap: 16px;
}

.chart-box {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.chart-title {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-body {
  height: 350px;
}

.empty-area {
  text-align: center;
  padding: 80px 20px;
}

.empty-img {
  width: 160px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 13px;
  color: #86909c;
  margin: 0;
}

@media (max-width: 900px) {
  .charts-row {
    flex-direction: column;
  }
  .count-row {
    flex-wrap: wrap;
  }
  .count-item {
    min-width: calc(50% - 12px);
  }
}
</style>
