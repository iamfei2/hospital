<template>
  <div class="page-wrapper">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">病理检查管理</h1>
        <p class="page-desc">管理和查看病理检查记录</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-refresh" @click="loadPathologyList" circle size="small" title="刷新数据"></el-button>
      </div>
    </div>

    <el-card class="main-card">
      <div slot="header" class="card-header">
        <div class="header-title">
          <span>检查列表</span>
        </div>
        <div class="header-buttons">
          <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddDialog">新增检查</el-button>
          <el-button type="primary" size="small" icon="el-icon-upload2" @click="showImportDialog">批量导入</el-button>
          <el-button type="primary" size="small" icon="el-icon-document" @click="showXmlImportDialog">XML导入</el-button>
          <el-button type="primary" size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
        </div>
      </div>

      <div class="filter-section">
        <el-form :inline="true" :model="queryParams" class="filter-form" @submit.native.prevent>
          <el-form-item label="患者ID" class="filter-item">
            <el-input v-model="queryParams.patientId" placeholder="输入患者ID" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <el-form-item label="患者姓名" class="filter-item">
            <el-input v-model="queryParams.patientName" placeholder="输入患者姓名" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <el-form-item label="病理号" class="filter-item">
            <el-input v-model="queryParams.pathologyNo" placeholder="输入病理号" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <el-form-item label="标本类型" class="filter-item">
            <el-input v-model="queryParams.specimenType" placeholder="输入标本类型" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <el-form-item label="病理医生" class="filter-item">
            <el-input v-model="queryParams.pathologyDoctor" placeholder="输入医生姓名" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <el-form-item label="病理科" class="filter-item">
            <el-input v-model="queryParams.pathologyDept" placeholder="输入科室名称" clearable @keyup.enter.native="handleQuery" size="small"></el-input>
          </el-form-item>
          <div class="filter-item filter-item-wide">
            <span class="filter-label">检查时间</span>
            <el-date-picker v-model="queryTimeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" size="small" @change="handleTimeRangeChange"></el-date-picker>
          </div>
          <el-form-item class="filter-actions">
            <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">查询</el-button>
            <el-button type="primary" icon="el-icon-refresh-left" size="small" @click="resetQuery">重置</el-button>
            <el-button type="primary" icon="el-icon-document" size="small" @click="showSaveTemplateDialog">保存模板</el-button>
            <el-button type="primary" icon="el-icon-folder-opened" size="small" @click="showLoadTemplateDialog">加载模板</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="pathologyTableData" stripe border v-loading="loading" class="data-table">
        <el-table-column prop="patientId" label="患者ID" width="120" align="center" fixed></el-table-column>
        <el-table-column prop="specimenType" label="标本类型" min-width="120"></el-table-column>
        <el-table-column prop="samplingTime" label="取样时间" width="160" align="center">
          <template slot-scope="scope"><span class="time-cell">{{ formatDate(scope.row.samplingTime) }}</span></template>
        </el-table-column>
        <el-table-column prop="reportTime" label="报告时间" width="160" align="center">
          <template slot-scope="scope"><span class="time-cell">{{ formatDate(scope.row.reportTime) }}</span></template>
        </el-table-column>
        <el-table-column prop="pathologyDoctor" label="诊断医生" width="100" align="center"></el-table-column>
        <el-table-column prop="pathologyDept" label="病理科" width="120" align="center"></el-table-column>
        <el-table-column prop="pathologyDiagnosis" label="病理诊断" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button type="text" size="small" @click="handlePreview(scope.row)" v-if="scope.row.reportUrl">PDF</el-button>
            <el-button type="text" size="small" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pageNum" :page-sizes="[10, 20, 50, 100]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
      </div>
    </el-card>

    <el-dialog
      title="病理检查详情"
      :visible.sync="detailDialogVisible"
      width="600px"
      class="detail-dialog">
      <el-descriptions :column="2" border size="medium" v-if="currentPathology">
        <el-descriptions-item label="病理ID">{{ currentPathology.pathologyId }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ currentPathology.patientId }}</el-descriptions-item>
        <el-descriptions-item label="病理号">{{ currentPathology.pathologyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="标本类型">{{ currentPathology.specimenType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取样时间">{{ formatDate(currentPathology.samplingTime) }}</el-descriptions-item>
        <el-descriptions-item label="报告时间">{{ formatDate(currentPathology.reportTime) }}</el-descriptions-item>
        <el-descriptions-item label="诊断医生">{{ currentPathology.pathologyDoctor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="病理科">{{ currentPathology.pathologyDept || '-' }}</el-descriptions-item>
        <el-descriptions-item label="病理诊断" :span="2">{{ currentPathology.pathologyDiagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告文件" :span="2">
          <el-link v-if="currentPathology.reportUrl" :href="getPreviewUrl(currentPathology.reportUrl)" type="primary" target="_blank" icon="el-icon-view">查看报告</el-link>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDate(currentPathology.uploadTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentPathology.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <el-dialog title="新增病理检查" :visible.sync="addDialogVisible" width="520px" :close-on-click-modal="false" class="form-dialog">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="100px" size="small">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="addForm.patientId" placeholder="请输入患者ID"></el-input>
        </el-form-item>
        <el-form-item label="病理号">
          <el-input v-model="addForm.pathologyNo" placeholder="请输入病理号"></el-input>
        </el-form-item>
        <el-form-item label="标本类型">
          <el-input v-model="addForm.specimenType" placeholder="请输入标本类型"></el-input>
        </el-form-item>
        <el-form-item label="取样时间">
          <el-date-picker v-model="addForm.samplingTime" type="datetime" placeholder="选择取样时间" style="width: 100%" size="small"></el-date-picker>
        </el-form-item>
        <el-form-item label="报告时间">
          <el-date-picker v-model="addForm.reportTime" type="datetime" placeholder="选择报告时间" style="width: 100%" size="small"></el-date-picker>
        </el-form-item>
        <el-form-item label="诊断医生">
          <el-input v-model="addForm.pathologyDoctor" placeholder="请输入诊断医生"></el-input>
        </el-form-item>
        <el-form-item label="病理科">
          <el-input v-model="addForm.pathologyDept" placeholder="请输入病理科"></el-input>
        </el-form-item>
        <el-form-item label="病理诊断">
          <el-input v-model="addForm.pathologyDiagnosis" type="textarea" :rows="3" placeholder="请输入病理诊断"></el-input>
        </el-form-item>
        <el-form-item label="PDF文件">
          <el-upload ref="pdfUpload" :auto-upload="false" :limit="1" :on-change="handleFileChange" :on-remove="handleFileRemove" accept=".pdf,.jpg,.jpeg,.png">
            <el-button size="small" type="primary">选择PDF文件</el-button>
            <div slot="tip" class="el-upload__tip">支持pdf/jpg/jpeg/png格式文件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button type="primary" @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="addLoading">确认添加</el-button>
      </span>
    </el-dialog>

    <el-dialog title="编辑病理检查" :visible.sync="editDialogVisible" width="520px" :close-on-click-modal="false" class="form-dialog">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="100px" size="small">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="editForm.patientId" placeholder="请输入患者ID"></el-input>
        </el-form-item>
        <el-form-item label="病理号">
          <el-input v-model="editForm.pathologyNo" placeholder="请输入病理号"></el-input>
        </el-form-item>
        <el-form-item label="标本类型">
          <el-input v-model="editForm.specimenType" placeholder="请输入标本类型"></el-input>
        </el-form-item>
        <el-form-item label="取样时间">
          <el-date-picker v-model="editForm.samplingTime" type="datetime" placeholder="选择取样时间" style="width: 100%" size="small"></el-date-picker>
        </el-form-item>
        <el-form-item label="报告时间">
          <el-date-picker v-model="editForm.reportTime" type="datetime" placeholder="选择报告时间" style="width: 100%" size="small"></el-date-picker>
        </el-form-item>
        <el-form-item label="诊断医生">
          <el-input v-model="editForm.pathologyDoctor" placeholder="请输入诊断医生"></el-input>
        </el-form-item>
        <el-form-item label="病理科">
          <el-input v-model="editForm.pathologyDept" placeholder="请输入病理科"></el-input>
        </el-form-item>
        <el-form-item label="病理诊断">
          <el-input v-model="editForm.pathologyDiagnosis" type="textarea" :rows="3" placeholder="请输入病理诊断"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button type="primary" @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="editLoading">保存修改</el-button>
      </span>
    </el-dialog>

    <el-dialog title="批量导入病理检查数据" :visible.sync="importDialogVisible" width="680px" :close-on-click-modal="false" class="form-dialog">
      <div class="import-tips">
        <el-alert title="Excel文件格式说明" type="info" :closable="false" show-icon>
          <template>
            <div class="tip-content">
              <p>请确保Excel文件包含以下列（表头行将被忽略）：</p>
              <p class="tip-columns">患者ID, 病理号, 标本类型, 标本取样时间, 病理诊断医生, 病理科, 病理诊断结论</p>
              <p class="tip-warning"><i class="el-icon-warning"></i> 患者ID为必填项</p>
            </div>
          </template>
        </el-alert>
      </div>

      <div class="import-mode">
        <el-form label-width="80px" size="small">
          <el-form-item label="导入模式">
            <el-radio-group v-model="importMode">
              <el-radio label="strict">
                <span class="radio-label">严格模式</span>
                <span class="radio-desc">发现失败行则全部回滚</span>
              </el-radio>
              <el-radio label="lenient">
                <span class="radio-label">宽容模式</span>
                <span class="radio-desc">跳过失败行，继续导入</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <el-upload
        ref="importUpload"
        :auto-upload="false"
        :limit="1"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :file-list="importFileList"
        accept=".xls,.xlsx"
        class="import-upload">
        <el-button slot="trigger" size="small" type="primary">
          <i class="el-icon-upload2"></i> 选择Excel文件
        </el-button>
        <div slot="tip" class="el-upload__tip">支持xls/xlsx格式，建议单次导入不超过5万条</div>
      </el-upload>
      
      <el-divider v-if="importResult"></el-divider>
      
      <div v-if="importResult" class="import-result">
        <el-alert 
          :title="'导入完成：共 ' + importResult.total + ' 条，成功 ' + importResult.success + ' 条，失败 ' + importResult.fail + ' 条，耗时 ' + importResult.costTime + 'ms'" 
          :type="importResult.fail > 0 ? 'warning' : 'success'" 
          :closable="false"
          show-icon>
        </el-alert>

        <div v-if="importResult.fail > 0" class="error-summary">
          <p class="summary-title">错误摘要：</p>
          <el-table :data="errorSummaryData" size="small" border max-height="140">
            <el-table-column prop="errorType" label="错误类型"></el-table-column>
            <el-table-column prop="count" label="数量" width="80" align="center"></el-table-column>
          </el-table>
        </div>

        <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list">
          <p class="list-title">错误信息（仅显示前5条）：</p>
          <div v-for="(error, index) in importResult.errors.slice(0, 5)" :key="index" class="error-item">
            <i class="el-icon-close-circle"></i> {{ error }}
          </div>
          <el-button v-if="importResult.fail > 5" type="text" size="small" @click="showAllErrors" class="view-all">
            查看全部{{ importResult.fail }}条错误 <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
      </div>
      
      <span slot="footer">
        <el-button type="primary" @click="handleDownloadErrorReport" v-if="importResult && importResult.fail > 0" :loading="downloadLoading" size="small">下载错误报告</el-button>
        <el-button type="primary" @click="importDialogVisible = false" size="small">关 闭</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading" :disabled="!importFile" size="small">开始导入</el-button>
      </span>
    </el-dialog>

    <el-dialog title="导入错误列表" :visible.sync="allErrorsDialogVisible" width="850px" class="form-dialog">
      <div class="export-actions">
        <el-button type="primary" size="small" icon="el-icon-download" @click="handleDownloadErrorReport">下载错误报告</el-button>
      </div>
      <el-table :data="importResult ? importResult.failedRows : []" border max-height="380" size="small" class="error-table">
        <el-table-column prop="rowIndex" label="Excel行号" width="100" align="center"></el-table-column>
        <el-table-column prop="rawData" label="原始数据" min-width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="errors" label="失败原因" min-width="200">
          <template slot-scope="scope">
            <div v-for="(err, idx) in scope.row.errors" :key="idx" class="error-cell">
              <i class="el-icon-warning"></i> {{ err }}
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="保存查询模板" :visible.sync="saveTemplateDialogVisible" width="420px" class="form-dialog">
      <el-form size="small">
        <el-form-item label="模板名称">
          <el-input v-model="templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button type="primary" @click="saveTemplateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTemplate">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="选择查询模板" :visible.sync="loadTemplateDialogVisible" width="520px" class="form-dialog">
      <el-table :data="templateList" border size="small">
        <el-table-column prop="templateName" label="模板名称"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="small" icon="el-icon-check" @click="handleLoadTemplate(scope.row)">加载</el-button>
            <el-button type="text" size="small" icon="el-icon-delete" style="color: #f56c6c;" @click="handleDeleteTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer">
        <el-button type="primary" @click="loadTemplateDialogVisible = false" size="small">关闭</el-button>
      </span>
    </el-dialog>

    <!-- XML导入对话框 -->
    <el-dialog title="XML导入病理检查数据" :visible.sync="xmlImportDialogVisible" width="500px" :close-on-click-modal="false">
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
          <el-link type="primary" :href="pathologyXmlTemplateUrl" download="病理检查导入模板.xml">下载XML模板</el-link>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="xmlImportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitXmlImport" :loading="xmlImportLoading">导入</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>

export default {
  name: "PathologyA",
  data() {
    return {
      queryParams: {
        patientId: '',
        patientName: '',
        pathologyNo: '',
        specimenType: '',
        pathologyDoctor: '',
        pathologyDept: '',
        startTime: '',
        endTime: ''
      },
      queryTimeRange: [],
      pathologyTableData: [],
      loading: false,
      pageSize: 10,
      pageNum: 1,
      total: 0,
      detailDialogVisible: false,
      currentPathology: null,
      addDialogVisible: false,
      addLoading: false,
      addForm: {
        patientId: '',
        pathologyNo: '',
        specimenType: '',
        samplingTime: '',
        reportTime: '',
        pathologyDoctor: '',
        pathologyDept: '',
        pathologyDiagnosis: '',
        pdfFile: null
      },
      addRules: {
        patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }]
      },
      editDialogVisible: false,
      editLoading: false,
      editForm: {
        pathologyId: null,
        patientId: '',
        pathologyNo: '',
        specimenType: '',
        samplingTime: '',
        reportTime: '',
        pathologyDoctor: '',
        pathologyDept: '',
        pathologyDiagnosis: ''
      },
      editRules: {
        patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }]
      },
      importDialogVisible: false,
      importLoading: false,
      importFile: null,
      importFileList: [],
      importResult: null,
      importMode: 'strict',
      errorSummaryData: [],
      allErrorsDialogVisible: false,
      downloadLoading: false,
      saveTemplateDialogVisible: false,
      templateName: '',
      loadTemplateDialogVisible: false,
      templateList: [],
      selectedTemplate: null,
      xmlImportDialogVisible: false,
      xmlImportLoading: false,
      xmlFile: null,
      xmlFileList: [],
      pathologyXmlTemplateUrl: '/template/pathology_import_template.xml'
    }
  },
  methods: {
    loadPathologyList() {
      this.loading = true;
      this.$axios.post('/pathologyExamination/page', {
        pageSize: this.pageSize,
        pageNum: this.pageNum,
        param: this.queryParams
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.pathologyTableData = res.data || [];
            this.total = res.total || 0;
          } else {
            this.$message.error('获取病理检查失败');
          }
        })
        .catch(error => {
          console.error('加载病理检查错误:', error);
          this.$message.error('加载病理检查出错');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleQuery() {
      this.pageNum = 1;
      this.loadPathologyList();
    },
    resetQuery() {
      this.queryParams = {
        patientId: '',
        patientName: '',
        pathologyNo: '',
        specimenType: '',
        pathologyDoctor: '',
        pathologyDept: '',
        startTime: '',
        endTime: ''
      };
      this.queryTimeRange = [];
      this.pageNum = 1;
      this.loadPathologyList();
    },
    handleTimeRangeChange(val) {
      if (val && val.length === 2) {
        this.queryParams.startTime = val[0];
        this.queryParams.endTime = val[1];
      } else {
        this.queryParams.startTime = '';
        this.queryParams.endTime = '';
      }
    },
    showSaveTemplateDialog() {
      this.templateName = '';
      this.saveTemplateDialogVisible = true;
    },
    handleSaveTemplate() {
      if (!this.templateName || !this.templateName.trim()) {
        this.$message.warning('请输入模板名称');
        return;
      }
      this.$axios.post('/searchTemplate/save', {
        templateName: this.templateName.trim(),
        templateType: 'pathology',
        queryConditions: JSON.stringify(this.queryParams)
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('模板保存成功');
            this.saveTemplateDialogVisible = false;
          } else {
            this.$message.error(res.msg || '保存失败');
          }
        });
    },
    showLoadTemplateDialog() {
      this.$axios.get('/searchTemplate/list?templateType=pathology')
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.templateList = res.data || [];
            this.loadTemplateDialogVisible = true;
          }
        });
    },
    handleLoadTemplate(row) {
      try {
        const conditions = JSON.parse(row.queryConditions);
        this.queryParams = { ...this.queryParams, ...conditions };
        if (conditions.startTime && conditions.endTime) {
          this.queryTimeRange = [conditions.startTime, conditions.endTime];
        }
        this.loadTemplateDialogVisible = false;
        this.$message.success('模板加载成功');
        this.handleQuery();
      } catch (e) {
        this.$message.error('模板加载失败');
      }
    },
    handleDeleteTemplate(row) {
      this.$confirm('确定要删除该模板吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete('/searchTemplate/' + row.templateId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.showLoadTemplateDialog();
            }
          });
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadPathologyList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadPathologyList();
    },
    handleExport() {
      const exportParams = {
        pageNum: 1,
        pageSize: 10000,
        param: { ...this.queryParams }
      };
      
      this.$axios.post('/pathologyExamination/export', exportParams, {
        responseType: 'blob'
      })
        .then(res => {
          const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = `病理检查数据_${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          URL.revokeObjectURL(link.href);
        })
        .catch(() => {
          this.$message.error('导出失败');
        });
    },
    handleViewDetail(row) {
      this.currentPathology = row;
      this.detailDialogVisible = true;
    },
    handleEdit(row) {
      this.editForm = {
        pathologyId: row.pathologyId,
        patientId: row.patientId,
        pathologyNo: row.pathologyNo || '',
        specimenType: row.specimenType || '',
        samplingTime: row.samplingTime || '',
        reportTime: row.reportTime || '',
        pathologyDoctor: row.pathologyDoctor || '',
        pathologyDept: row.pathologyDept || '',
        pathologyDiagnosis: row.pathologyDiagnosis || ''
      };
      this.editDialogVisible = true;
    },
    handleUpdate() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.editLoading = true;
          this.$axios.post('/pathologyExamination/update', this.editForm)
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success('修改成功');
                this.editDialogVisible = false;
                this.loadPathologyList();
              } else {
                this.$message.error(res.msg || '修改失败');
              }
            })
            .catch(() => {
              this.$message.error('修改失败');
            })
            .finally(() => {
              this.editLoading = false;
            });
        }
      });
    },
    handlePreview(row) {
      if (row.reportUrl) {
        window.open(this.getPreviewUrl(row.reportUrl), '_blank');
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该病理检查记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete('/pathologyExamination/' + row.pathologyId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadPathologyList();
            } else {
              this.$message.error(res.msg || '删除失败');
            }
          });
      }).catch(() => {});
    },
    getPreviewUrl(url) {
      if (url) {
        if (url.startsWith('http')) {
          return url;
        }
        return (process.env.VUE_APP_API_BASE_URL || 'http://localhost:8090') + url;
      }
      return url;
    },
    showAddDialog() {
      this.addDialogVisible = true;
      this.addForm = {
        patientId: '',
        pathologyNo: '',
        specimenType: '',
        samplingTime: '',
        reportTime: '',
        pathologyDoctor: '',
        pathologyDept: '',
        pathologyDiagnosis: '',
        pdfFile: null
      };
      this.$nextTick(() => {
        if (this.$refs.addForm) {
          this.$refs.addForm.resetFields();
        }
        if (this.$refs.pdfUpload) {
          this.$refs.pdfUpload.clearFiles();
        }
      });
    },
    handleFileChange(file) {
      this.addForm.pdfFile = file.raw;
    },
    handleFileRemove() {
      this.addForm.pdfFile = null;
    },
    handleAdd() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          this.addLoading = true;

          const formData = new FormData();
          formData.append('patientId', this.addForm.patientId);
          if (this.addForm.pathologyNo) formData.append('pathologyNo', this.addForm.pathologyNo);
          if (this.addForm.specimenType) formData.append('specimenType', this.addForm.specimenType);
          if (this.addForm.samplingTime) formData.append('samplingTime', this.formatDateForBackend(this.addForm.samplingTime));
          if (this.addForm.reportTime) formData.append('reportTime', this.formatDateForBackend(this.addForm.reportTime));
          if (this.addForm.pathologyDoctor) formData.append('pathologyDoctor', this.addForm.pathologyDoctor);
          if (this.addForm.pathologyDept) formData.append('pathologyDept', this.addForm.pathologyDept);
          if (this.addForm.pathologyDiagnosis) formData.append('pathologyDiagnosis', this.addForm.pathologyDiagnosis);
          if (this.addForm.pdfFile) formData.append('pdfFile', this.addForm.pdfFile);

          this.$axios.post('/pathologyExamination/add', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success('添加成功');
                this.addDialogVisible = false;
                this.loadPathologyList();
              } else {
                this.$message.error(res.msg || '添加失败');
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
    showImportDialog() {
      this.importDialogVisible = true;
      this.importFile = null;
      this.importFileList = [];
      this.importResult = null;
      this.importMode = 'strict';
      this.errorSummaryData = [];
    },
    handleImportFileChange(file) {
      this.importFile = file.raw;
    },
    handleImportFileRemove() {
      this.importFile = null;
    },
    handleImport() {
      if (!this.importFile) {
        this.$message.warning('请选择要导入的Excel文件');
        return;
      }
      
      this.importLoading = true;
      this.importResult = null;
      const formData = new FormData();
      formData.append('file', this.importFile);
      formData.append('mode', this.importMode);
      
      this.$axios.post('/import/pathologyExamination', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.importResult = res.data;
            if (res.data.importMode === 'strict' && res.data.fail > 0) {
              this.$alert('共' + res.data.total + '条，发现' + res.data.fail + '条数据校验失败，已全部回滚。请查看错误信息并修正后重新导入。', '导入失败', {
                confirmButtonText: '确定',
                type: 'warning'
              });
            } else if (res.data.fail > 0) {
              this.buildErrorSummary();
              this.$message.warning('导入完成，部分数据失败');
              this.loadPathologyList();
            } else {
              this.$message.success('导入完成');
              this.loadPathologyList();
            }
          } else {
            this.$message.error(res.msg || '导入失败');
          }
        })
        .catch(() => {
          this.$message.error('导入失败');
        })
        .finally(() => {
          this.importLoading = false;
        });
    },
    buildErrorSummary() {
      if (!this.importResult || !this.importResult.errorSummary) return;
      this.errorSummaryData = Object.keys(this.importResult.errorSummary).map(key => ({
        errorType: key,
        count: this.importResult.errorSummary[key]
      }));
    },
    showAllErrors() {
      this.allErrorsDialogVisible = true;
    },
    handleDownloadErrorReport() {
      if (!this.importResult || !this.importResult.failedRows || this.importResult.failedRows.length === 0) {
        this.$message.warning('没有可导出的错误数据');
        return;
      }
      this.downloadLoading = true;
      import('@/components/ExportErrorReport').then(module => {
        module.default.download(this.importResult.failedRows, '病理检查导入错误报告');
        this.downloadLoading = false;
      }).catch(() => {
        this.$message.error('导出失败');
        this.downloadLoading = false;
      });
    },
    formatDateForBackend(dateValue) {
      if (!dateValue) return '';
      
      let date;
      if (typeof dateValue === 'string') {
        date = new Date(dateValue);
      } else if (dateValue instanceof Date) {
        date = dateValue;
      } else {
        return '';
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
    handleXmlFileChange(file) {
      this.xmlFile = file.raw;
    },
    handleXmlFileRemove() {
      this.xmlFile = null;
    },
    submitXmlImport() {
      if (!this.xmlFile) {
        this.$message.warning('请选择XML文件');
        return;
      }
      this.xmlImportLoading = true;
      const formData = new FormData();
      formData.append('file', this.xmlFile);
      this.$axios.post('/import/pathology/xml', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
        .then(res => {
          const data = res.data;
          if (data.code === 200) {
            this.$message.success('导入成功！');
            this.xmlImportDialogVisible = false;
            this.loadPathologyList();
          } else if (data.code === 417) {
            this.$confirm('患者[' + data.data.patientId + ']不存在，是否新增？', '提示', {
              confirmButtonText: '新增患者',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.$message.info('请到患者信息模块新增患者：' + data.data.patientId);
            }).catch(() => {});
          } else {
            this.$message.error(data.msg || '导入失败');
          }
        })
        .catch(err => {
          console.error('XML导入失败:', err);
          this.$message.error('导入失败');
        })
        .finally(() => {
          this.xmlImportLoading = false;
        });
    }
  },
  mounted() {
    this.loadPathologyList();
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

.header-actions {
  display: flex;
  gap: 8px;
}

.main-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.filter-section {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-item {
  margin-bottom: 0;
}

.filter-item-wide {
  display: flex;
  align-items: center;
  width: 550px;
  margin-bottom: 0;
}

.filter-label {
  flex-shrink: 0;
  margin-right: 8px;
  color: #606266;
  font-size: 14px;
}

.filter-item-wide .el-date-editor.el-range-editor {
  width: 420px;
}

.filter-item-wide .el-date-editor .el-range-separator {
  min-width: 40px !important;
  overflow: visible !important;
  display: inline-block !important;
}

.filter-actions {
  margin-left: auto;
  margin-bottom: 0;
  display: flex;
  gap: 8px;
}

.data-table {
  margin-top: 0;
  border-radius: 8px;
  overflow: hidden;
}

.time-cell {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #606266;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

/* 导入相关样式 */
.import-tips {
  margin-bottom: 16px;
}

.tip-content p {
  margin: 4px 0;
  font-size: 13px;
}

.tip-columns {
  color: #606266;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.tip-warning {
  color: #e6a23c;
  font-size: 12px;
}

.tip-warning i {
  margin-right: 4px;
}

.import-mode {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.radio-label {
  font-weight: 500;
  color: #303133;
}

.radio-desc {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}

.import-upload {
  padding: 16px 0;
}

.import-result {
  margin-top: 16px;
}

.error-summary {
  margin-top: 12px;
}

.summary-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.error-list {
  margin-top: 12px;
}

.list-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.error-item {
  font-size: 12px;
  color: #f56c6c;
  padding: 4px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.error-item i {
  color: #f56c6c;
}

.view-all {
  margin-top: 8px;
  color: #144ddd;
}

.export-actions {
  margin-bottom: 12px;
}

.error-table .error-cell {
  color: #f56c6c;
  font-size: 12px;
  padding: 2px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-table .error-cell i {
  color: #f56c6c;
}

.text-muted {
  color: #c0c4cc;
}

/* 表格按钮样式 */
.data-table >>> .el-button--text {
  padding: 4px 8px;
  font-size: 13px;
}

</style>
