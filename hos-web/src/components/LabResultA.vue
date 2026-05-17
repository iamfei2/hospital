<template>
  <div class="lab-result-section">
    <el-card class="lab-result-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold;">检验结果管理</span>
        <el-button style="float: right; margin-left: 10px;" type="success" size="small" @click="showXmlImportDialog">
          XML导入
        </el-button>
        <el-button style="float: right; margin-left: 10px;" type="info" size="small" @click="showJointDetectDialog">
          联合检测
        </el-button>
        <el-button style="float: right; margin-left: 10px;" type="warning" size="small" @click="handleExport">导出Excel</el-button>
        <el-button style="float: right; margin-right: 10px;" type="primary" size="small" @click="showAddDialog">
          新增检验
        </el-button>
      </div>

      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline" @submit.native.prevent>
        <el-form-item label="患者ID">
          <el-input v-model="queryParams.patientId" placeholder="请输入患者ID" clearable @keyup.enter.native="handleQuery"></el-input>
        </el-form-item>
        <el-form-item label="检验项目">
          <el-input v-model="queryParams.testName" placeholder="请输入检验项目" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 分组模式表格 -->
      <el-table
          :data="groupedTableData"
          :header-cell-style="{background:'#868a8c',color:'#333'}"
          border
          style="width: 100%"
          v-loading="loading">

        <el-table-column prop="reportTime" label="检验时间" width="160" sortable>
          <template slot-scope="scope">
            <span>{{ formatDate(scope.row.reportTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="patientId" label="患者ID" width="100" align="center">
        </el-table-column>

        <el-table-column prop="patientName" label="患者姓名" width="100" align="center">
        </el-table-column>

        <el-table-column prop="testDept" label="检验科室" width="120">
        </el-table-column>

        <el-table-column prop="testDoctor" label="检验医生" width="100">
        </el-table-column>

        <el-table-column prop="itemCount" label="检验项目数" width="100" align="center">
          <template slot-scope="scope">
            <el-tag>{{ scope.row.itemCount }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="abnormalCount" label="异常项目数" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.abnormalCount > 0" type="danger">{{ scope.row.abnormalCount }}</el-tag>
            <el-tag v-else type="success">{{ scope.row.abnormalCount }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewGroupDetail(scope.row)">查看详情</el-button>
            <el-button type="text" size="small" @click="handlePreviewPdf(scope.row.reportUrl)" v-if="scope.row.reportUrl">预览PDF</el-button>
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

    <!-- 单条记录详情对话框 -->
    <el-dialog
        title="检验结果详情"
        :visible.sync="detailDialogVisible"
        width="50%">
      <el-descriptions :column="2" border v-if="currentResult">
        <el-descriptions-item label="结果ID">{{ currentResult.resultId }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ currentResult.patientId }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ currentResult.patientName }}</el-descriptions-item>
        <el-descriptions-item label="检验项目">{{ currentResult.testName }}</el-descriptions-item>
        <el-descriptions-item label="检验结果" :span="2">
          <span :style="{color: getResultColor(currentResult)}">{{ currentResult.testResult }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="参考范围">{{ currentResult.referenceRange }}</el-descriptions-item>
        <el-descriptions-item label="单位">
          <!-- 修改单位显示逻辑 -->
          <span v-if="currentResult.unit && currentResult.unit !== '-' && currentResult.unit !== '未知'">
                {{ currentResult.unit }}
            </span>
          <span v-else style="color: #999;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="检验时间">{{ formatDate(currentResult.testTime) }}</el-descriptions-item>
        <el-descriptions-item label="检验科室">{{ currentResult.testDept }}</el-descriptions-item>
        <el-descriptions-item label="检验医生">{{ currentResult.testDoctor }}</el-descriptions-item>
        <el-descriptions-item label="结果状态">
          <el-tag :type="getResultStatusType(currentResult.resultStatus)">
            {{ currentResult.resultStatus }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 分组详情对话框 -->
    <el-dialog
        title="检验结果详情"
        :visible.sync="groupDetailDialogVisible"
        width="70%">
      <div v-if="currentGroupDetail && currentGroupDetail.length > 0">
        <div style="margin-bottom: 15px;">
          <span style="font-weight: bold;">患者ID: {{ currentGroupDetail[0].patientId }}</span>
          <span style="margin-left: 20px; font-weight: bold;">患者姓名: {{ currentGroupDetail[0].patientName }}</span>
          <span style="margin-left: 20px; font-weight: bold;">检验时间: {{ formatDate(currentGroupDetail[0].testTime) }}</span>
        </div>

        <el-table
            :data="currentGroupDetail"
            border
            style="width: 100%">
          <el-table-column prop="testName" label="检验项目" min-width="150">
          </el-table-column>
          <el-table-column prop="testResult" label="检验结果" width="120">
            <template slot-scope="scope">
              <span :style="{color: getResultColor(scope.row)}">{{ scope.row.testResult }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="referenceRange" label="参考范围" width="120">
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center">
          </el-table-column>
          <el-table-column prop="resultStatus" label="结果状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getResultStatusType(scope.row.resultStatus)">
                {{ scope.row.resultStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="handleShowTrend(scope.row)">趋势</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="groupDetailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 新增检验弹窗 -->
    <el-dialog title="新增检验" :visible.sync="addDialogVisible" width="800px" :close-on-click-modal="false">
      <el-form :model="addForm" :rules="addFormRules" ref="addForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者ID" prop="patientId">
              <el-input v-model="addForm.patientId" placeholder="请输入患者ID" @change="handlePatientIdChange">
                <el-button slot="append" icon="el-icon-search" @click="showPatientDialog"></el-button>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者姓名">
              <el-input v-model="addForm.patientName" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验时间" prop="reportTime">
              <el-date-picker v-model="addForm.reportTime" type="datetime" placeholder="选择检验时间" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行科室" prop="executeDept">
              <el-input v-model="addForm.executeDept" placeholder="可选，不填则无"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="执行医生" prop="executeDoc">
              <el-input v-model="addForm.executeDoc" placeholder="可选，不填默认当前用户"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>检验项目</el-divider>

        <div v-for="(item, index) in addForm.items" :key="index" class="lab-item-row">
          <el-row :gutter="10" align="middle">
            <el-col :span="10">
              <el-select
                  v-model="item.itemId"
                  placeholder="选择或输入搜索检验项目"
                  filterable
                  allow-create
                  default-first-option
                  style="width: 100%"
                  @change="handleItemChange(index)">
                <el-option v-for="dict in labItemList" :key="dict.itemId" :label="dict.itemName" :value="dict.itemId">
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-input v-model="item.resultValue" placeholder="结果" style="width: 100%"></el-input>
            </el-col>
            <el-col :span="4">
              <el-input v-model="item.resultUnit" placeholder="单位" style="width: 100%"></el-input>
            </el-col>
            <el-col :span="2">
              <el-button type="danger" icon="el-icon-delete" @click="removeLabItem(index)" :disabled="addForm.items.length <= 1">删除</el-button>
            </el-col>
          </el-row>
        </div>

        <el-button type="primary" plain icon="el-icon-plus" @click="addLabItem" style="margin-bottom: 15px;">添加检验项目</el-button>

        <el-form-item label="检验报告PDF">
          <el-upload
              ref="pdfUpload"
              :auto-upload="false"
              :limit="1"
              :file-list="pdfFileList"
              :on-change="handlePdfChange"
              :on-remove="handlePdfRemove"
              accept=".pdf">
            <el-button size="small" type="primary">选择PDF文件</el-button>
            <div slot="tip" class="el-upload__tip">可选，同一报告时间的结果共享此PDF</div>
          </el-upload>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="addLoading">提交</el-button>
      </div>
    </el-dialog>

    <!-- 患者选择弹窗 -->
    <el-dialog title="选择患者" :visible.sync="patientDialogVisible" width="600px">
      <el-table :data="patientList" border @row-click="selectPatient" height="400">
        <el-table-column prop="patientId" label="患者ID" width="100"></el-table-column>
        <el-table-column prop="patientName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="gender" label="性别" width="80"></el-table-column>
        <el-table-column prop="age" label="年龄" width="80"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 趋势图对话框 -->
    <el-dialog title="检验项目趋势图" :visible.sync="trendDialogVisible" width="800px">
      <div v-if="trendData">
        <div style="margin-bottom: 15px;">
          <span style="font-weight: bold;">检验项目: {{ trendData.itemName }}</span>
          <span style="margin-left: 20px;">单位: {{ trendData.unit || '-' }}</span>
          <span style="margin-left: 20px;">参考范围: {{ trendData.normalRange || '-' }}</span>
        </div>
        <div ref="trendChart" style="width: 100%; height: 400px;"></div>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <p>暂无趋势数据</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="trendDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- XML导入对话框 -->
    <el-dialog title="XML导入检验结果" :visible.sync="xmlImportDialogVisible" width="500px">
      <el-form>
        <el-form-item label="选择XML文件">
          <el-upload
              ref="xmlUpload"
              :auto-upload="false"
              :limit="1"
              :file-list="xmlFileList"
              :on-change="handleXmlFileChange"
              :on-remove="handleXmlFileRemove"
              accept=".xml">
            <el-button size="small" type="primary">选择XML文件</el-button>
            <div slot="tip" class="el-upload__tip">支持.xml格式文件</div>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" :href="xmlTemplateUrl" download="检验结果导入模板.xml">下载XML模板</el-link>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="xmlImportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitXmlImport" :loading="xmlImportLoading">导入</el-button>
      </span>
    </el-dialog>

    <!-- 联合检测对话框 -->
    <el-dialog title="联合检测分析" :visible.sync="jointDetectDialogVisible" width="600px">
      <el-form :model="jointDetectForm" label-width="100px">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="jointDetectForm.patientId" placeholder="请输入患者ID">
            <el-button slot="append" icon="el-icon-search" @click="loadPatientForJoint"></el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="检验项目" prop="itemIds">
          <el-select
              v-model="jointDetectForm.itemIds"
              multiple
              placeholder="请选择至少2个检验项目"
              style="width: 100%">
            <el-option v-for="item in labItemListForJoint" :key="item.itemId" :label="item.itemName" :value="item.itemId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 12px;">提示：联合检测使用Isolation Forest算法分析多个指标的联合异常模式</span>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="jointDetectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitJointDetect" :loading="jointDetectLoading" :disabled="jointDetectForm.itemIds.length < 2">开始检测</el-button>
      </span>
    </el-dialog>

    <!-- 联合检测结果对话框 -->
    <el-dialog title="联合检测结果" :visible.sync="jointDetectResultDialogVisible" width="700px">
      <div v-if="jointDetectResult">
        <el-alert
            :title="jointDetectResult.anomalyLevel === 'ANOMALY' ? '检测到异常' : jointDetectResult.anomalyLevel === 'SUSPICIOUS' ? '可疑' : '正常'"
            :type="jointDetectResult.anomalyLevel === 'ANOMALY' ? 'error' : jointDetectResult.anomalyLevel === 'SUSPICIOUS' ? 'warning' : 'success'"
            :description="jointDetectResult.alertMessage"
            :closable="false"
            show-icon>
        </el-alert>
        <el-divider></el-divider>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <p style="color: #909399; font-size: 12px;">Isolation评分</p>
                <p style="font-size: 24px; font-weight: bold; color: #909399;" v-if="jointDetectResult.isolationScore < 0">-</p>
                <p style="font-size: 24px; font-weight: bold; color: #409EFF;" v-else-if="jointDetectResult.isolationScore !== null">{{ jointDetectResult.isolationScore.toFixed(4) }}</p>
                <p style="font-size: 24px; font-weight: bold; color: #909399;" v-else>-</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <p style="color: #909399; font-size: 12px;">综合评分</p>
                <p style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ jointDetectResult.combinedScore !== null ? jointDetectResult.combinedScore.toFixed(4) : '-' }}</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div style="text-align: center;">
                <p style="color: #909399; font-size: 12px;">异常阈值</p>
                <p style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ jointDetectResult.threshold !== null ? jointDetectResult.threshold : '-' }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-alert v-if="jointDetectResult.isolationScore < 0" 
          title="该指标组合未配置Isolation Forest模型，仅基于Z-score提供参考分析" 
          type="info" 
          :closable="false" 
          show-icon
          style="margin-top: 15px;"></el-alert>
        <el-divider></el-divider>
        <div v-if="jointDetectResult.anomalyItems && jointDetectResult.anomalyItems.length > 0">
          <h4>异常指标详情</h4>
          <el-table :data="jointDetectResult.anomalyItems" border style="margin-top: 10px;">
            <el-table-column prop="itemName" label="项目名称"></el-table-column>
            <el-table-column prop="resultValue" label="结果值">
              <template slot-scope="scope">
                {{ scope.row.resultValue }}
              </template>
            </el-table-column>
            <el-table-column prop="zscore" label="Z-Score">
              <template slot-scope="scope">
                <span :style="{color: Math.abs(scope.row.zscore) > 2 ? '#F56C6C' : '#67C23A'}">
                  {{ scope.row.zscore ? scope.row.zscore.toFixed(2) : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="meanValue" label="均值"></el-table-column>
            <el-table-column prop="stdDeviation" label="标准差"></el-table-column>
          </el-table>
        </div>
        <el-divider></el-divider>
        <div style="background: #f5f7fa; padding: 15px; border-radius: 4px; margin-top: 15px;">
          <h4 style="margin: 0 0 10px 0;">Z-Score 评分说明</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div style="text-align: center;">
                <p style="color: #67C23A; font-weight: bold;">|Z| &lt; 2</p>
                <p style="color: #909399; font-size: 12px;">正常范围</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="text-align: center;">
                <p style="color: #E6A23C; font-weight: bold;">2 ≤ |Z| &lt; 3</p>
                <p style="color: #909399; font-size: 12px;">可疑偏离</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="text-align: center;">
                <p style="color: #F56C6C; font-weight: bold;">|Z| ≥ 3</p>
                <p style="color: #909399; font-size: 12px;">显著异常</p>
              </div>
            </el-col>
          </el-row>
          <p style="color: #909399; font-size: 12px; margin-top: 10px; margin-bottom: 0;">
            注：Z-Score 表示该指标偏离正常均值的标准差倍数。正值表示高于均值，负值表示低于均值。
          </p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="jointDetectResultDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "LabResultA",
  data() {
    return {
      // 查询参数
      queryParams: {
        patientId: '',
        testName: ''
      },
      // 表格数据
      labResultTableData: [],
      groupedTableData: [],
      loading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,

      // 对话框控制
      detailDialogVisible: false,
      groupDetailDialogVisible: false,
      trendDialogVisible: false,
      currentResult: null,
      currentGroupDetail: [],
      trendData: null,
      trendChart: null,

      // 新增检验相关
      addDialogVisible: false,
      addLoading: false,
      pdfFile: null,
      pdfFileList: [],
      addForm: {
        patientId: '',
        patientName: '',
        reportTime: '',
        executeDept: '',
        executeDoc: '',
        items: [{ itemId: null, resultValue: '', resultUnit: '' }]
      },
      addFormRules: {
        patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }],
        reportTime: [{ required: true, message: '请选择检验时间', trigger: 'change' }],
        executeDept: [],
        executeDoc: []
      },
      labItemList: [],
      patientDialogVisible: false,
      patientList: [],

      // XML导入相关
      xmlImportDialogVisible: false,
      xmlImportLoading: false,
      xmlFile: null,
      xmlFileList: [],
      xmlTemplateUrl: '/template/lab_import_template.xml',

      // 联合检测相关
      jointDetectDialogVisible: false,
      jointDetectLoading: false,
      jointDetectForm: {
        patientId: '',
        itemIds: []
      },
      labItemListForJoint: [],
      jointDetectResultDialogVisible: false,
      jointDetectResult: null
    }
  },
  methods: {
    // 加载检验结果数据
    loadLabResults() {
      this.loading = true;

      this.$axios.post('/labResult/page', {
        pageSize: this.pageSize,
        pageNum: this.pageNum,
        param: this.queryParams
      }).then(res => res.data).then(res => {
        console.log(res)
        if (res.code == 200) {
          this.groupedTableData = res.data;
          this.total = res.total;
        } else {
          this.$message.error('获取检验结果失败');
        }
      }).catch(error => {
        console.error('加载检验结果错误:', error);
        this.$message.error('加载检验结果出错');
      }).finally(() => {
        this.loading = false;
      });
    },

    // 查询
    handleQuery() {
      this.pageNum = 1;
      this.loadLabResults();
    },

    // 重置查询
    resetQuery() {
      this.queryParams = {
        patientId: '',
        testName: ''
      };
      this.pageNum = 1;
      this.loadLabResults();
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.pageNum = 1;
      this.pageSize = val;
      this.loadLabResults();
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadLabResults();
    },

    handleExport() {
      const exportParams = {
        pageNum: 1,
        pageSize: 10000,
        param: { ...this.queryParams }
      };
      
      this.$axios.post('/labResult/export', exportParams, {
        responseType: 'blob'
      })
        .then(res => {
          const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = `检验结果数据_${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          URL.revokeObjectURL(link.href);
        })
        .catch(() => {
          this.$message.error('导出失败');
        });
    },

    // 查看单条记录详情
    handleViewDetail(row) {
      this.currentResult = row;
      this.detailDialogVisible = true;
    },

    // 查看分组详情
    handleViewGroupDetail(row) {
      this.loading = true;
      this.$axios.post('/labResult/byTime', {
        pageSize: 100, // 设置较大的pageSize确保获取所有记录
        pageNum: 1,
        param: {
          patientId: row.patientId,
          reportTime: this.formatDateForBackend(row.reportTime)
        }
      }).then(res => res.data).then(res => {
        if (res.code == 200) {
          this.currentGroupDetail = res.data;
          this.groupDetailDialogVisible = true;
        } else {
          this.$message.error('获取检验详情失败');
        }
      }).catch(error => {
        console.error('加载检验详情错误:', error);
        this.$message.error('加载检验详情出错');
      }).finally(() => {
        this.loading = false;
      });
    },

    // 日期格式化（用于显示）
    formatDate(dateValue) {
      if (!dateValue) return '未知';

      let date;
      if (typeof dateValue === 'string') {
        date = new Date(dateValue);
      } else if (dateValue instanceof Date) {
        date = dateValue;
      } else {
        // 处理LocalDateTime对象
        try {
          if (dateValue.year && dateValue.monthValue && dateValue.dayOfMonth) {
            date = new Date(dateValue.year, dateValue.monthValue - 1, dateValue.dayOfMonth,
                dateValue.hour || 0, dateValue.minute || 0, dateValue.second || 0);
          } else {
            return '未知';
          }
        } catch (e) {
          return '未知';
        }
      }

      if (isNaN(date)) return '未知';

      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      const second = date.getSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },

    // 日期格式化（用于后端接口）
    formatDateForBackend(dateValue) {
      if (!dateValue) return '';

      let date;
      if (typeof dateValue === 'string') {
        date = new Date(dateValue);
      } else if (dateValue instanceof Date) {
        date = dateValue;
      } else {
        // 处理LocalDateTime对象
        try {
          if (dateValue.year && dateValue.monthValue && dateValue.dayOfMonth) {
            date = new Date(dateValue.year, dateValue.monthValue - 1, dateValue.dayOfMonth,
                dateValue.hour || 0, dateValue.minute || 0, dateValue.second || 0);
          } else {
            return '';
          }
        } catch (e) {
          return '';
        }
      }

      if (isNaN(date)) return '';

      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      const second = date.getSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },

    // 获取结果状态标签类型
    getResultStatusType(status) {
      const typeMap = {
        '正常': 'success',
        '异常': 'danger',
        '危急': 'warning',
        '待审核': 'info',
        '未知': 'info'
      };
      return typeMap[status] || 'info';
    },

    // 获取结果颜色（根据是否异常）
    getResultColor(result) {
      if (result.resultStatus === '异常' || result.resultStatus === '危急') {
        return '#F56C6C';
      }
      return '#67C23A';
    },

    // 获取结果方向（偏高/偏低）
    getResultDirection(result) {
      if (!result.testResult || !result.referenceRange) return '';
      
      const value = parseFloat(result.testResult);
      if (isNaN(value)) return '';
      
      const range = result.referenceRange;
      const match = range.match(/(\d+\.?\d*)\s*-\s*(\d+\.?\d*)/);
      
      if (match) {
        const min = parseFloat(match[1]);
        const max = parseFloat(match[2]);
        
        if (value < min) return '↓偏低';
        if (value > max) return '↑偏高';
      }
      
      return '';
    },

    // 显示新增对话框
    showAddDialog() {
      this.addDialogVisible = true;
      this.loadLabItemList();
      this.pdfFile = null;
      this.pdfFileList = [];
      this.addForm = {
        patientId: '',
        patientName: '',
        reportTime: '',
        executeDept: '',
        executeDoc: '',
        items: [{ itemId: null, resultValue: '', resultUnit: '' }]
      };
      this.$nextTick(() => {
        if (this.$refs.pdfUpload) {
          this.$refs.pdfUpload.clearFiles();
        }
      });
    },

    // 患者ID输入变化时自动查询患者姓名
    handlePatientIdChange() {
      if (this.addForm.patientId) {
        this.$axios.get('/patient/detail/' + this.addForm.patientId)
          .then(res => {
            if (res.data.code === 200 && res.data.data) {
              this.addForm.patientName = res.data.data.patientName;
            } else {
              this.addForm.patientName = '';
            }
          })
          .catch(() => {
            this.addForm.patientName = '';
          });
      } else {
        this.addForm.patientName = '';
      }
    },

    // 加载检验项目列表
    loadLabItemList() {
      this.$axios.get('/labItemDict/list')
        .then(res => {
          if (res.data.code === 200) {
            this.labItemList = res.data.data || [];
          }
        });
    },

    // 检验项目选择变化
    handleItemChange(index) {
      const item = this.addForm.items[index];
      const dict = this.labItemList.find(d => d.itemId === item.itemId);
      if (dict) {
        item.resultUnit = dict.defaultUnit || '';
      }
    },

    // 添加检验项目
    addLabItem() {
      this.addForm.items.push({ itemId: null, resultValue: '', resultUnit: '' });
    },

    // 删除检验项目
    removeLabItem(index) {
      if (this.addForm.items.length > 1) {
        this.addForm.items.splice(index, 1);
      }
    },

    // 显示患者选择对话框
    showPatientDialog() {
      this.$axios.get('/patient/list')
        .then(res => {
          if (res.data.code === 200) {
            this.patientList = res.data.data || [];
            this.patientDialogVisible = true;
          }
        });
    },

    // 选择患者
    selectPatient(row) {
      this.addForm.patientId = row.patientId;
      this.addForm.patientName = row.patientName;
      this.patientDialogVisible = false;
    },

    // 提交新增
    submitAdd() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          // 检查是否至少选择了PDF或检验项目
          const hasItems = this.addForm.items.some(item => item.itemId);
          if (!hasItems && !this.pdfFile) {
            this.$message.warning('请至少上传PDF或添加一个检验项目');
            return;
          }
          
          this.addLoading = true;
          
          const formData = new FormData();
          formData.append('patientId', this.addForm.patientId);
          formData.append('reportTime', this.formatDateForBackend(this.addForm.reportTime));
          formData.append('executeDept', this.addForm.executeDept || '');
          formData.append('executeDoc', this.addForm.executeDoc || '');
          
          // 过滤掉没有选择检验项目的行
          const validItems = this.addForm.items.filter(item => item.itemId);
          formData.append('items', JSON.stringify(validItems.map(item => ({
            itemId: item.itemId,
            resultValue: item.resultValue,
            resultUnit: item.resultUnit
          }))));

          if (this.pdfFile) {
            formData.append('pdfFile', this.pdfFile);
          }

          this.$axios.post('/labResult/add', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
            .then(res => {
              if (res.data.code === 200) {
                this.$message.success('添加成功');
                this.addDialogVisible = false;
                this.pdfFile = null;
                this.loadLabResults();
              } else {
                this.$message.error(res.data.msg || '添加失败');
              }
            })
            .catch(() => {
              this.$message.error('添加失败');
            })
            .finally(() => {
              this.addLoading = false;
            });
        }
      });
    },

    // PDF文件变化
    handlePdfChange(file, fileList) {
      console.log('PDF文件变化:', file);
      this.pdfFile = file.raw;
      this.pdfFileList = fileList;
      console.log('pdfFile设置为:', this.pdfFile);
    },

    // 移除PDF文件
    handlePdfRemove() {
      this.pdfFile = null;
      this.pdfFileList = [];
    },

    // 预览PDF
    handlePreviewPdf(url) {
      console.log('预览PDF URL:', url);
      if (url) {
        let fullUrl = url;
        if (!url.startsWith('http')) {
          fullUrl = 'http://localhost:8090' + url;
        }
        window.open(fullUrl, '_blank');
      } else {
        this.$message.warning('没有PDF文件');
      }
    },

    // 显示趋势图
    handleShowTrend(row) {
      this.trendDialogVisible = true;
      this.trendData = null;
      
      this.$axios.get('/labResult/trend', {
        params: {
          patientId: row.patientId,
          itemId: row.itemId
        }
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.trendData = res.data;
            this.$nextTick(() => {
              this.initTrendChart();
            });
          } else {
            this.$message.error('获取趋势数据失败');
          }
        })
        .catch(() => {
          this.$message.error('获取趋势数据失败');
        });
    },

    // 初始化趋势图
    initTrendChart() {
      if (!this.trendData || !this.trendData.trendData || this.trendData.trendData.length === 0) {
        return;
      }

      if (this.trendChart) {
        this.trendChart.dispose();
      }

      this.trendChart = echarts.init(this.$refs.trendChart);

      const dates = this.trendData.trendData.map(item => this.formatDate(item.reportTime));
      const values = this.trendData.trendData.map(item => item.resultValue);

      // 解析参考范围
      let minVal = null, maxVal = null;
      if (this.trendData.normalRange) {
        const parts = this.trendData.normalRange.split('-');
        if (parts.length === 2) {
          minVal = parseFloat(parts[0]);
          maxVal = parseFloat(parts[1]);
        }
      }

      // 根据实际数据动态计算Y轴范围
      const validValues = values.filter(v => v != null && !isNaN(v));
      let dataMin = validValues.length > 0 ? Math.min(...validValues) : 0;
      let dataMax = validValues.length > 0 ? Math.max(...validValues) : 100;

      // 如果有参考范围，参考范围也要包含在显示区域内
      if (minVal !== null && minVal < dataMin) {
        dataMin = minVal;
      }
      if (maxVal !== null && maxVal > dataMax) {
        dataMax = maxVal;
      }

      // 添加一些边距
      const padding = (dataMax - dataMin) * 0.1 || 10;
      const yMin = dataMin - padding;
      const yMax = dataMax + padding;

      const unit = this.trendData ? this.trendData.unit : '';

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            let result = params[0].axisValue + '<br/>';
            params.forEach(param => {
              result += param.seriesName + ': ' + param.value + ' ' + unit + '<br/>';
            });
            return result;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: this.trendData.unit || '',
          min: yMin,
          max: yMax
        },
        series: [
          {
            name: this.trendData.itemName,
            type: 'line',
            data: values,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              color: '#165dff',
              width: 2
            },
            itemStyle: {
              color: '#165dff'
            },
            markLine: minVal !== null && maxVal !== null ? {
              silent: true,
              data: [
                { yAxis: minVal, lineStyle: { color: '#00b42a', type: 'dashed' }, label: { formatter: '下限' } },
                { yAxis: maxVal, lineStyle: { color: '#f53f3f', type: 'dashed' }, label: { formatter: '上限' } }
              ]
            } : undefined
          }
        ]
      };

      this.trendChart.setOption(option);

      window.addEventListener('resize', () => {
        if (this.trendChart) {
          this.trendChart.resize();
        }
      });
    },

    // ==================== XML导入相关方法 ====================

    // 显示XML导入对话框
    showXmlImportDialog() {
      this.xmlImportDialogVisible = true;
      this.xmlFile = null;
      this.xmlFileList = [];
      this.$nextTick(() => {
        if (this.$refs.xmlUpload) {
          this.$refs.xmlUpload.clearFiles();
        }
      });
    },

    // XML文件变化
    handleXmlFileChange(file, fileList) {
      console.log('XML文件变化:', file);
      this.xmlFile = file.raw;
      this.xmlFileList = fileList;
    },

    // 移除XML文件
    handleXmlFileRemove() {
      this.xmlFile = null;
      this.xmlFileList = [];
    },

    // 提交XML导入
    submitXmlImport() {
      if (!this.xmlFile) {
        this.$message.warning('请选择XML文件');
        return;
      }

      this.xmlImportLoading = true;
      const formData = new FormData();
      formData.append('file', this.xmlFile);

      this.$axios.post('/import/lab/xml', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
        .then(res => {
          const data = res.data;
          if (data.code === 200) {
            this.$message.success('导入成功！' + (data.data.itemSummary || ''));
            this.xmlImportDialogVisible = false;
            this.loadLabResults();
          } else if (data.code === 417) {
            // 患者不存在，弹出新增患者对话框
            this.$confirm('患者[' + data.data.patientId + ']不存在，是否新增？', '提示', {
              confirmButtonText: '新增患者',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              // 跳转到患者新增页面或弹出患者新增对话框
              this.$message.info('请到患者信息模块新增患者：' + data.data.patientId);
            }).catch(() => {});
          } else {
            this.$message.error(data.msg || '导入失败');
          }
        })
        .catch(err => {
          console.error('XML导入失败:', err);
          this.$message.error('导入失败：' + (err.message || '未知错误'));
        })
        .finally(() => {
          this.xmlImportLoading = false;
        });
    },

    // ==================== 联合检测相关方法 ====================

    // 显示联合检测对话框
    showJointDetectDialog() {
      this.jointDetectDialogVisible = true;
      this.jointDetectForm = {
        patientId: '',
        itemIds: []
      };
      this.jointDetectResult = null;
      // 加载检验项目列表
      this.loadLabItemListForJoint();
    },

    // 加载联合检测用的检验项目列表
    loadLabItemListForJoint() {
      this.$axios.get('/labItemDict/list')
        .then(res => {
          if (res.data.code === 200) {
            this.labItemListForJoint = res.data.data || [];
          }
        });
    },

    // 为联合检测加载患者信息
    loadPatientForJoint() {
      if (!this.jointDetectForm.patientId) {
        this.$message.warning('请输入患者ID');
        return;
      }
      this.$axios.get('/patient/detail/' + this.jointDetectForm.patientId)
        .then(res => {
          if (res.data.code === 200 && res.data.data) {
            this.$message.success('找到患者：' + res.data.data.patientName);
          } else {
            this.$message.warning('未找到患者');
          }
        });
    },

    // 提交联合检测
    submitJointDetect() {
      if (!this.jointDetectForm.patientId) {
        this.$message.warning('请输入患者ID');
        return;
      }
      if (this.jointDetectForm.itemIds.length < 2) {
        this.$message.warning('请至少选择2个检验项目');
        return;
      }

      this.jointDetectLoading = true;
      this.$axios.post('/labResult/jointDetect', {
        patientId: this.jointDetectForm.patientId,
        itemIds: this.jointDetectForm.itemIds
      })
        .then(res => {
          const data = res.data;
          if (data.code === 200) {
            this.jointDetectResult = data.data;
            this.jointDetectResultDialogVisible = true;
          } else {
            this.$message.error(data.msg || '联合检测失败');
          }
        })
        .catch(err => {
          console.error('联合检测失败:', err);
          this.$message.error('联合检测失败');
        })
        .finally(() => {
          this.jointDetectLoading = false;
        });
    }
  },
  mounted() {
    this.loadLabResults();
  },
  beforeDestroy() {
    if (this.trendChart) {
      this.trendChart.dispose();
      this.trendChart = null;
    }
  }
}
</script>

<style scoped>
.lab-result-section {
  padding: 0;
}

.demo-form-inline {
  margin-bottom: 16px;
}

.lab-result-card .el-table {
  max-height: calc(100vh - 320px);
  overflow: auto;
}

.lab-result-card .el-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.el-descriptions {
  margin-top: 10px;
}

.group-detail-header {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f7f8fa;
  border-radius: 6px;
}

.lab-item-row {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f7f8fa;
  border-radius: 6px;
}
</style>