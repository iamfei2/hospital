<template>
  <div class="patient-info-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="患者查询" name="query">
        <!-- 搜索区域 -->
        <div class="search-section">
          <el-form :inline="true" :model="searchForm" class="demo-form-inline" @submit.native.prevent>
            <el-form-item label="患者ID">
              <el-input
                  v-model="searchForm.patientId"
                  placeholder="请输入患者ID"
                  clearable
                  style="width: 200px;"
                  @keyup.enter.native="handleSearch">
              </el-input>
            </el-form-item>
            <el-form-item label="姓名">
              <el-input
                  v-model="searchForm.patientName"
                  placeholder="请输入姓名"
                  clearable
                  style="width: 200px;"
                  @keyup.enter.native="handleSearch">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch" :loading="searchLoading">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 患者基本信息 -->
    <div class="info-card" v-if="patientInfo.patientId">
      <div class="card-header">
        <span class="card-title">患者基本信息</span>
      </div>
      <div class="card-content">
        <div class="patient-basic-info">
          <div class="info-item">
            <span class="info-label">患者ID:</span>
            <span class="info-value">{{ patientInfo.patientId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">姓名:</span>
            <span class="info-value">{{ patientInfo.patientName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别:</span>
            <span class="info-value">{{ patientInfo.gender }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">年龄:</span>
            <span class="info-value">{{ patientInfo.age }} 岁</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话:</span>
            <span class="info-value">{{ patientInfo.phone || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态:</span>
            <span class="info-value">
              <el-tag :type="patientInfo.isInvalid ? 'danger' : 'success'" size="small">
                {{ patientInfo.isInvalid ? '作废' : '有效' }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 医嘱信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">医嘱信息</span>
        <span v-if="medicalOrders.length > 0">共 {{ medicalOrders.length }} 条医嘱</span>
      </div>
      <div class="card-content">
        <div v-if="medicalOrders.length === 0" class="empty-state">
          <i class="el-icon-document"></i>
          <p>暂无医嘱信息</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="medicalOrders"
              border
              style="width: 100%"
              :default-sort = "{prop: 'startTime', order: 'descending'}"
              v-loading="medicalOrderLoading">
            <el-table-column
                prop="orderName"
                label="医嘱名称"
                min-width="150">
            </el-table-column>
            <el-table-column
                prop="startTime"
                label="开始时间"
                width="160"
                sortable>
              <template slot-scope="scope">
                {{ formatDate(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column
                prop="endTime"
                label="结束时间"
                width="160"
                sortable>
              <template slot-scope="scope">
                {{ formatDate(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column
                prop="orderStatus"
                label="状态"
                width="100"
                align="center">
              <template slot-scope="scope">
                <el-tag :type="getOrderStatusType(scope.row.orderStatus)" size="small">
                  {{ scope.row.orderStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
                prop="orderFrequency"
                label="频率"
                width="100">
            </el-table-column>
            <el-table-column
                prop="executeDept"
                label="执行科室"
                width="120">
            </el-table-column>
            <el-table-column
                prop="executeDoc"
                label="执行医生"
                width="100">
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 检验结果 - 修改为分组显示 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">检验结果</span>
        <span v-if="groupedLabResults.length > 0">共 {{ groupedLabResults.length }} 次检验</span>
      </div>
      <div class="card-content">
        <div v-if="groupedLabResults.length === 0" class="empty-state">
          <i class="el-icon-document-checked"></i>
          <p>暂无检验结果</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="groupedLabResults"
              border
              style="width: 100%"
              :default-sort = "{prop: 'reportTime', order: 'descending'}"
              v-loading="labResultLoading">

            <el-table-column
                prop="reportTime"
                label="检验时间"
                width="160"
                sortable>
              <template slot-scope="scope">
                {{ formatDate(scope.row.reportTime) }}
              </template>
            </el-table-column>

            <el-table-column
                prop="itemCount"
                label="检验项目数"
                width="100"
                align="center">
              <template slot-scope="scope">
                <el-tag>{{ scope.row.itemCount }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column
                prop="abnormalCount"
                label="异常项目数"
                width="100"
                align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.abnormalCount > 0" type="danger">{{ scope.row.abnormalCount }}</el-tag>
                <el-tag v-else type="success">{{ scope.row.abnormalCount }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column
                prop="testDept"
                label="检验科室"
                width="120">
            </el-table-column>

            <el-table-column
                prop="testDoctor"
                label="检验医生"
                width="100">
            </el-table-column>

            <el-table-column
                label="操作"
                width="180"
                align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleViewGroupDetail(scope.row)">查看详情</el-button>
                <el-button type="text" size="small" @click="handlePreviewPdf(scope.row.reportUrl)" v-if="scope.row.reportUrl">预览PDF</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 患者选择对话框 -->
    <el-dialog title="请选择患者" :visible.sync="patientSelectDialogVisible" width="500px">
      <el-table :data="patientList" border style="width: 100%">
        <el-table-column prop="patientId" label="患者ID" width="120"></el-table-column>
        <el-table-column prop="patientName" label="姓名" width="80"></el-table-column>
        <el-table-column prop="gender" label="性别" width="60"></el-table-column>
        <el-table-column prop="age" label="年龄" width="60"></el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleSelectPatient(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 分组详情对话框 -->
    <el-dialog
        title="检验结果详情"
        :visible.sync="groupDetailDialogVisible"
        width="70%">
      <div v-if="currentGroupDetail && currentGroupDetail.length > 0">
        <div style="margin-bottom: 15px;">
          <span style="font-weight: bold;">检验时间: {{ formatDate(currentGroupDetail[0].reportTime) }}</span>
        </div>

        <el-table
            :data="currentGroupDetail"
            border
            style="width: 100%">
          <el-table-column
              prop="testName"
              label="检验项目"
              min-width="150">
          </el-table-column>
          <el-table-column
              prop="testResult"
              label="检验结果"
              width="120">
            <template slot-scope="scope">
              <span :style="{color: getResultColor(scope.row)}">{{ scope.row.testResult }}</span>
            </template>
          </el-table-column>
          <el-table-column
              prop="referenceRange"
              label="参考范围"
              width="120">
          </el-table-column>
          <el-table-column
              prop="unit"
              label="单位"
              width="80"
              align="center">
            <template slot-scope="scope">
              <span v-if="scope.row.unit && scope.row.unit !== '-' && scope.row.unit !== '未知'">
                {{ scope.row.unit }}
              </span>
              <span v-else style="color: #999;">-</span>
            </template>
          </el-table-column>
          <el-table-column
              prop="resultStatus"
              label="结果状态"
              width="100"
              align="center">
            <template slot-scope="scope">
              <span class="status-tag" :class="getResultStatusClass(scope.row.resultStatus)">
                {{ scope.row.resultStatus || '正常' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
              label="操作"
              width="100"
              align="center">
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

    <!-- CT检查信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">CT检查</span>
        <span v-if="ctResults.length > 0">共 {{ ctResults.length }} 条</span>
      </div>
      <div class="card-content">
        <div v-if="ctResults.length === 0" class="empty-state">
          <i class="el-icon-s-data"></i>
          <p>暂无CT检查信息</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="ctResults"
              border
              style="width: 100%"
              v-loading="ctLoading">
            <el-table-column prop="examinationNo" label="检查编号" width="150"></el-table-column>
            <el-table-column prop="examinationPart" label="检查部位" width="150"></el-table-column>
            <el-table-column prop="examinationTime" label="检查时间" width="160">
              <template slot-scope="scope">{{ formatDate(scope.row.examinationTime) }}</template>
            </el-table-column>
            <el-table-column prop="examineDoctor" label="检查医生" width="100"></el-table-column>
            <el-table-column prop="examineDept" label="检查科室" width="120"></el-table-column>
            <el-table-column prop="reportConclusion" label="报告结论" min-width="150"></el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleViewDetail(scope.row, 'ct')">详情</el-button>
                <el-button type="text" size="small" @click="handlePreview(scope.row)" v-if="scope.row.reportUrl">预览PDF</el-button>
                <el-button type="text" size="small" @click="handleEdit(scope.row, 'ct')">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 核磁检查信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">核磁检查</span>
        <span v-if="mriResults.length > 0">共 {{ mriResults.length }} 条</span>
      </div>
      <div class="card-content">
        <div v-if="mriResults.length === 0" class="empty-state">
          <i class="el-icon-s-data"></i>
          <p>暂无核磁检查信息</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="mriResults"
              border
              style="width: 100%"
              v-loading="mriLoading">
            <el-table-column prop="examinationNo" label="检查编号" width="150"></el-table-column>
            <el-table-column prop="examinationPart" label="检查部位" width="150"></el-table-column>
            <el-table-column prop="examinationTime" label="检查时间" width="160">
              <template slot-scope="scope">{{ formatDate(scope.row.examinationTime) }}</template>
            </el-table-column>
            <el-table-column prop="examineDoctor" label="检查医生" width="100"></el-table-column>
            <el-table-column prop="examineDept" label="检查科室" width="120"></el-table-column>
            <el-table-column prop="reportConclusion" label="报告结论" min-width="150"></el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleViewDetail(scope.row, 'mri')">详情</el-button>
                <el-button type="text" size="small" @click="handlePreview(scope.row)" v-if="scope.row.reportUrl">预览PDF</el-button>
                <el-button type="text" size="small" @click="handleEdit(scope.row, 'mri')">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 肠镜检查信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">肠镜检查</span>
        <span v-if="enteroscopyResults.length > 0">共 {{ enteroscopyResults.length }} 条</span>
      </div>
      <div class="card-content">
        <div v-if="enteroscopyResults.length === 0" class="empty-state">
          <i class="el-icon-s-order"></i>
          <p>暂无肠镜检查信息</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="enteroscopyResults"
              border
              style="width: 100%"
              v-loading="enteroscopyLoading">
            <el-table-column prop="examinationNo" label="检查编号" width="150"></el-table-column>
            <el-table-column prop="enteroscopyType" label="检查类型" width="150"></el-table-column>
            <el-table-column prop="examinationTime" label="检查时间" width="160">
              <template slot-scope="scope">{{ formatDate(scope.row.examinationTime) }}</template>
            </el-table-column>
            <el-table-column prop="examineDoctor" label="检查医生" width="100"></el-table-column>
            <el-table-column prop="examineDept" label="检查科室" width="120"></el-table-column>
            <el-table-column prop="reportConclusion" label="报告结论" min-width="150"></el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleViewDetail(scope.row, 'enteroscopy')">详情</el-button>
                <el-button type="text" size="small" @click="handlePreview(scope.row)" v-if="scope.row.reportUrl">预览PDF</el-button>
                <el-button type="text" size="small" @click="handleEdit(scope.row, 'enteroscopy')">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 病理检查信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="card-title">病理检查</span>
        <span v-if="pathologyResults.length > 0">共 {{ pathologyResults.length }} 条</span>
      </div>
      <div class="card-content">
        <div v-if="pathologyResults.length === 0" class="empty-state">
          <i class="el-icon-document"></i>
          <p>暂无病理检查信息</p>
        </div>
        <div v-else class="table-container">
          <el-table
              :data="pathologyResults"
              border
              style="width: 100%"
              v-loading="pathologyLoading">
            <el-table-column prop="pathologyNo" label="病理号" width="150"></el-table-column>
            <el-table-column prop="specimenType" label="标本类型" width="150"></el-table-column>
            <el-table-column prop="samplingTime" label="取样时间" width="160">
              <template slot-scope="scope">{{ formatDate(scope.row.samplingTime) }}</template>
            </el-table-column>
            <el-table-column prop="reportTime" label="报告时间" width="160">
              <template slot-scope="scope">{{ formatDate(scope.row.reportTime) }}</template>
            </el-table-column>
            <el-table-column prop="pathologyDoctor" label="诊断医生" width="100"></el-table-column>
            <el-table-column prop="pathologyDiagnosis" label="病理诊断" min-width="150"></el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleViewDetail(scope.row, 'pathology')">详情</el-button>
                <el-button type="text" size="small" @click="handlePreview(scope.row)" v-if="scope.row.reportUrl">预览PDF</el-button>
                <el-button type="text" size="small" @click="handleEdit(scope.row, 'pathology')">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
        title="检查详情"
        :visible.sync="detailDialogVisible"
        width="60%">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="患者ID">{{ currentDetail.patientId }}</el-descriptions-item>
        <el-descriptions-item label="检查编号">{{ currentDetail.examinationNo || currentDetail.pathologyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查类型">{{ currentDetail.examinationPart || currentDetail.enteroscopyType || currentDetail.specimenType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查时间">{{ formatDate(currentDetail.examinationTime || currentDetail.samplingTime) }}</el-descriptions-item>
        <el-descriptions-item label="检查医生">{{ currentDetail.examineDoctor || currentDetail.pathologyDoctor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查科室">{{ currentDetail.examineDept || currentDetail.pathologyDept || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告结论/诊断" :span="2">{{ currentDetail.reportConclusion || currentDetail.pathologyDiagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告链接">
          <el-link v-if="currentDetail.reportUrl" :href="getPreviewUrl(currentDetail.reportUrl)" type="primary" target="_blank">查看报告</el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog
        title="编辑检查信息"
        :visible.sync="editDialogVisible"
        width="500px"
        :close-on-click-modal="false">
      <el-form :model="editForm" ref="editForm" label-width="100px">
        <el-form-item label="患者ID">
          <el-input v-model="editForm.patientId" disabled></el-input>
        </el-form-item>
        <el-form-item label="检查编号" v-if="currentEditType !== 'pathology'">
          <el-input v-model="editForm.examinationNo" placeholder="请输入检查编号"></el-input>
        </el-form-item>
        <el-form-item label="病理号" v-if="currentEditType === 'pathology'">
          <el-input v-model="editForm.pathologyNo" placeholder="请输入病理号"></el-input>
        </el-form-item>
        <el-form-item label="检查部位" v-if="currentEditType === 'ct' || currentEditType === 'mri'">
          <el-input v-model="editForm.examinationPart" placeholder="请输入检查部位"></el-input>
        </el-form-item>
        <el-form-item label="检查类型" v-if="currentEditType === 'enteroscopy'">
          <el-input v-model="editForm.enteroscopyType" placeholder="请输入检查类型"></el-input>
        </el-form-item>
        <el-form-item label="标本类型" v-if="currentEditType === 'pathology'">
          <el-input v-model="editForm.specimenType" placeholder="请输入标本类型"></el-input>
        </el-form-item>
        <el-form-item label="检查时间" v-if="currentEditType !== 'pathology'">
          <el-date-picker v-model="editForm.examinationTime" type="datetime" placeholder="选择检查时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="取样时间" v-if="currentEditType === 'pathology'">
          <el-date-picker v-model="editForm.samplingTime" type="datetime" placeholder="选择取样时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="报告时间" v-if="currentEditType === 'pathology'">
          <el-date-picker v-model="editForm.reportTime" type="datetime" placeholder="选择报告时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="检查医生">
          <el-input v-model="editForm.examineDoctor" placeholder="请输入检查医生" v-if="currentEditType !== 'pathology'"></el-input>
          <el-input v-model="editForm.pathologyDoctor" placeholder="请输入检查医生" v-if="currentEditType === 'pathology'"></el-input>
        </el-form-item>
        <el-form-item label="检查科室">
          <el-input v-model="editForm.examineDept" placeholder="请输入检查科室" v-if="currentEditType !== 'pathology'"></el-input>
          <el-input v-model="editForm.pathologyDept" placeholder="请输入检查科室" v-if="currentEditType === 'pathology'"></el-input>
        </el-form-item>
        <el-form-item label="报告结论" v-if="currentEditType !== 'pathology'">
          <el-input v-model="editForm.reportConclusion" type="textarea" :rows="3" placeholder="请输入报告结论"></el-input>
        </el-form-item>
        <el-form-item label="病理诊断" v-if="currentEditType === 'pathology'">
          <el-input v-model="editForm.pathologyDiagnosis" type="textarea" :rows="3" placeholder="请输入病理诊断"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="editLoading">确 定</el-button>
      </div>
    </el-dialog>
    </el-tab-pane>

    <!-- 患者管理Tab -->
      <el-tab-pane label="患者管理" name="manage">
        <div class="search-section">
          <div class="header-row">
            <h2>患者信息管理</h2>
            <div class="header-buttons">
              <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddDialog">新增患者</el-button>
            </div>
          </div>
          <el-form :inline="true" :model="manageQueryForm" class="demo-form-inline" @submit.native.prevent>
            <el-form-item label="患者ID">
              <el-input v-model="manageQueryForm.patientId" placeholder="请输入患者ID" clearable style="width: 180px;"></el-input>
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="manageQueryForm.patientName" placeholder="请输入姓名" clearable style="width: 180px;"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPatientList" :loading="manageLoading">查询</el-button>
              <el-button @click="resetManageQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="info-card">
          <el-table :data="patientTableData" border v-loading="manageLoading" stripe>
            <el-table-column prop="patientId" label="患者ID" width="120" align="center"></el-table-column>
            <el-table-column prop="patientName" label="姓名" width="100" align="center"></el-table-column>
            <el-table-column prop="gender" label="性别" width="80" align="center"></el-table-column>
            <el-table-column prop="age" label="年龄" width="80" align="center"></el-table-column>
            <el-table-column prop="phone" label="联系电话" width="150" align="center">
              <template slot-scope="scope">{{ scope.row.phone || '-' }}</template>
            </el-table-column>
            <el-table-column prop="isInvalid" label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isInvalid ? 'danger' : 'success'" size="small">
                  {{ scope.row.isInvalid ? '作废' : '有效' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleEditPatient(scope.row)">编辑</el-button>
                <el-button type="text" size="small" @click="handleDeletePatient(scope.row)" v-if="!scope.row.isInvalid">作废</el-button>
                <el-button type="text" size="small" @click="handleRestorePatient(scope.row)" v-else>恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              @size-change="handleManageSizeChange"
              @current-change="handleManageCurrentChange"
              :current-page="managePageNum"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="managePageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="manageTotal">
            </el-pagination>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增患者对话框 -->
    <el-dialog title="新增患者" :visible.sync="addDialogVisible" width="520px" :close-on-click-modal="false" class="form-dialog">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="100px" size="small">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="addForm.patientId" placeholder="请输入患者ID" @input="addForm.patientId = addForm.patientId.replace(/\D/g, '')"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="patientName">
          <el-input v-model="addForm.patientName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="addForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="addForm.age" placeholder="请输入年龄"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="addLoading">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑患者对话框 -->
    <el-dialog title="编辑患者" :visible.sync="editPatientDialogVisible" width="520px" :close-on-click-modal="false" class="form-dialog">
      <el-form :model="editPatientForm" :rules="editPatientRules" ref="editPatientForm" label-width="100px" size="small">
        <el-form-item label="患者ID">
          <el-input v-model="editPatientForm.patientId" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="patientName">
          <el-input v-model="editPatientForm.patientName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editPatientForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="editPatientForm.age" placeholder="请输入年龄"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editPatientForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editPatientDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdatePatient" :loading="editPatientLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "PatientInfoA",
  data() {
    return {
      activeTab: 'query',
      searchForm: {
        patientId: '',
        patientName: ''
      },
      searchLoading: false,
      patientInfo: {},
      medicalOrders: [],
      groupedLabResults: [], // 修改：改为分组后的检验结果
      medicalOrderLoading: false,
      labResultLoading: false,

      // 新增：分组详情相关数据
      currentGroupDetail: [],
      groupDetailDialogVisible: false,

      // 检查信息
      ctResults: [],
      mriResults: [],
      enteroscopyResults: [],
      pathologyResults: [],
      ctLoading: false,
      mriLoading: false,
      enteroscopyLoading: false,
      pathologyLoading: false,

      // 患者选择对话框
      patientList: [],
      patientSelectDialogVisible: false,

      // 详情弹窗
      detailDialogVisible: false,
      currentDetail: null,
      currentDetailType: '',

      // 编辑弹窗
      editDialogVisible: false,
      editLoading: false,
      currentEditType: '',
      editForm: {},

      // 趋势图
      trendDialogVisible: false,
      trendData: null,
      trendChart: null,

      // 新增患者对话框
      addDialogVisible: false,
      addLoading: false,
      addForm: {
        patientId: '',
        patientName: '',
        gender: '',
        age: '',
        phone: ''
      },
      addRules: {
        patientId: [
          { required: true, message: '请输入患者ID', trigger: 'blur' },
          { pattern: /^\d+$/, message: '患者ID必须为数字', trigger: 'blur' }
        ],
        patientName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
      },

      // 患者管理相关
      manageQueryForm: {
        patientId: '',
        patientName: ''
      },
      manageLoading: false,
      patientTableData: [],
      managePageNum: 1,
      managePageSize: 10,
      manageTotal: 0,

      // 编辑患者对话框
      editPatientDialogVisible: false,
      editPatientLoading: false,
      editPatientForm: {
        patientId: '',
        patientName: '',
        gender: '',
        age: '',
        phone: ''
      },
      editPatientRules: {
        patientName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
      }
    }
  },
  methods: {
    // 搜索患者信息
    handleSearch() {
      if (!this.searchForm.patientId && !this.searchForm.patientName) {
        this.$message.warning('请输入患者ID或姓名');
        return;
      }

      this.searchLoading = true;

      // 如果有患者ID，直接查询；如果没有患者ID但有姓名，先查询患者ID
      if (this.searchForm.patientId) {
        this.fetchPatientInfo();
        this.fetchMedicalOrders();
        this.fetchLabResults();
        this.fetchCtResults();
        this.fetchMriResults();
        this.fetchEnteroscopyResults();
        this.fetchPathologyResults();
      } else {
        // 按姓名查询获取患者ID列表
        this.$axios.get('/patient/byName', {
          params: { patientName: this.searchForm.patientName }
        })
        .then(res => {
          if (res.data.code === 200) {
            const patients = res.data.data;
            if (patients.length === 1) {
              // 只有一个匹配，直接使用
              this.searchForm.patientId = patients[0].patientId;
              this.fetchPatientInfo();
              this.fetchMedicalOrders();
              this.fetchLabResults();
              this.fetchCtResults();
              this.fetchMriResults();
              this.fetchEnteroscopyResults();
              this.fetchPathologyResults();
            } else {
              // 多个匹配，让用户选择
              this.patientList = patients;
              this.patientSelectDialogVisible = true;
              this.searchLoading = false;
            }
          } else {
            this.$message.error('未找到该患者');
            this.searchLoading = false;
          }
        })
        .catch(() => {
          this.$message.error('查询患者失败');
          this.searchLoading = false;
        });
      }
    },

    // 选择患者
    handleSelectPatient(patient) {
      this.searchForm.patientId = patient.patientId;
      this.patientSelectDialogVisible = false;
      this.searchLoading = true;
      this.fetchPatientInfo();
      this.fetchMedicalOrders();
      this.fetchLabResults();
      this.fetchCtResults();
      this.fetchMriResults();
      this.fetchEnteroscopyResults();
      this.fetchPathologyResults();
    },

// 重置搜索
    resetSearch() {
      this.searchForm.patientId = '';
      this.searchForm.patientName = '';
      this.patientInfo = {};
      this.medicalOrders = [];
      this.groupedLabResults = [];
      this.currentGroupDetail = [];
      this.groupDetailDialogVisible = false;
      this.ctResults = [];
      this.mriResults = [];
      this.enteroscopyResults = [];
      this.pathologyResults = [];
    },

    // 显示新增对话框
    showAddDialog() {
      this.addDialogVisible = true;
      this.addForm = {
        patientId: '',
        patientName: '',
        gender: '',
        age: '',
        phone: ''
      };
      this.$nextTick(() => {
        if (this.$refs.addForm) {
          this.$refs.addForm.resetFields();
        }
      });
    },

    // 新增患者
    handleAdd() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          this.addLoading = true;
          this.$axios.post('/patient/add', this.addForm)
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success('添加成功');
                this.addDialogVisible = false;
                if (this.activeTab === 'manage') {
                  this.loadPatientList();
                } else {
                  this.handleSearch();
                }
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

    // Tab切换
    handleTabChange(tab) {
      if (tab === 'manage' && this.patientTableData.length === 0) {
        this.loadPatientList();
      }
    },

    // 加载患者列表（管理用）
    loadPatientList() {
      this.manageLoading = true;
      this.$axios.post('/patient/page', {
        pageSize: this.managePageSize,
        pageNum: this.managePageNum,
        param: {
          patientId: this.manageQueryForm.patientId,
          patientName: this.manageQueryForm.patientName
        }
      })
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.patientTableData = res.data || [];
            this.manageTotal = res.total || 0;
          } else {
            this.$message.error('获取患者列表失败');
          }
        })
        .catch(() => {
          this.$message.error('获取患者列表失败');
        })
        .finally(() => {
          this.manageLoading = false;
        });
    },

    // 重置管理查询
    resetManageQuery() {
      this.manageQueryForm = {
        patientId: '',
        patientName: ''
      };
      this.managePageNum = 1;
      this.loadPatientList();
    },

    // 编辑患者
    handleEditPatient(row) {
      this.editPatientForm = {
        patientId: row.patientId,
        patientName: row.patientName,
        gender: row.gender,
        age: row.age,
        phone: row.phone || ''
      };
      this.editPatientDialogVisible = true;
    },

    // 更新患者
    handleUpdatePatient() {
      this.$refs.editPatientForm.validate(valid => {
        if (valid) {
          this.editPatientLoading = true;
          this.$axios.post('/patient/update', this.editPatientForm)
            .then(res => res.data)
            .then(res => {
              if (res.code === 200) {
                this.$message.success('修改成功');
                this.editPatientDialogVisible = false;
                this.loadPatientList();
              } else {
                this.$message.error(res.msg || '修改失败');
              }
            })
            .catch(() => {
              this.$message.error('修改失败');
            })
            .finally(() => {
              this.editPatientLoading = false;
            });
        }
      });
    },

    // 作废患者
    handleDeletePatient(row) {
      this.$confirm('确定要作废该患者吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.get('/patient/invalid/' + row.patientId)
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('作废成功');
              this.loadPatientList();
            } else {
              this.$message.error(res.msg || '作废失败');
            }
          });
      }).catch(() => {});
    },

    // 恢复患者
    handleRestorePatient(row) {
      this.$axios.get('/patient/restore/' + row.patientId)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('恢复成功');
            this.loadPatientList();
          } else {
            this.$message.error(res.msg || '恢复失败');
          }
        });
    },

    // 管理分页
    handleManageSizeChange(val) {
      this.managePageSize = val;
      this.loadPatientList();
    },
    handleManageCurrentChange(val) {
      this.managePageNum = val;
      this.loadPatientList();
    },

    // 获取CT检查信息
    fetchCtResults() {
      this.ctLoading = true;
      this.$axios.get('/ctExamination/byPatient/' + this.searchForm.patientId)
        .then(res => {
          if (res.data.code === 200) {
            this.ctResults = res.data.data || [];
          }
        })
        .catch(() => {})
        .finally(() => {
          this.ctLoading = false;
        });
    },

    // 获取核磁检查信息
    fetchMriResults() {
      this.mriLoading = true;
      this.$axios.get('/mriExamination/byPatient/' + this.searchForm.patientId)
        .then(res => {
          if (res.data.code === 200) {
            this.mriResults = res.data.data || [];
          }
        })
        .catch(() => {})
        .finally(() => {
          this.mriLoading = false;
        });
    },

    // 获取肠镜检查信息
    fetchEnteroscopyResults() {
      this.enteroscopyLoading = true;
      this.$axios.get('/enteroscopyExamination/byPatient/' + this.searchForm.patientId)
        .then(res => {
          if (res.data.code === 200) {
            this.enteroscopyResults = res.data.data || [];
          }
        })
        .catch(() => {})
        .finally(() => {
          this.enteroscopyLoading = false;
        });
    },

    // 获取病理检查信息
    fetchPathologyResults() {
      this.pathologyLoading = true;
      this.$axios.get('/pathologyExamination/byPatient/' + this.searchForm.patientId)
        .then(res => {
          if (res.data.code === 200) {
            this.pathologyResults = res.data.data || [];
          }
        })
        .catch(() => {})
        .finally(() => {
          this.pathologyLoading = false;
        });
    },

    // 获取患者基本信息
    fetchPatientInfo() {
      this.$axios.get('/patient/detail/' + this.searchForm.patientId)
          .then(res => {
            if (res.data.code === 200) {
              this.patientInfo = res.data.data;
            } else {
              this.$message.error('获取患者信息失败');
            }
          })
          .catch(error => {
            console.error('获取患者信息错误:', error);
            this.$message.error('获取患者信息出错');
          })
          .finally(() => {
            this.searchLoading = false;
          });
    },

    // 获取医嘱信息
    fetchMedicalOrders() {
      this.medicalOrderLoading = true;
      this.$axios.get('/medicalOrder/byPatient/' + this.searchForm.patientId)
          .then(res => {
            if (res.data.code === 200) {
              this.medicalOrders = res.data.data;
            } else {
              this.$message.error('获取医嘱信息失败');
            }
          })
          .catch(error => {
            console.error('获取医嘱信息错误:', error);
            this.$message.error('获取医嘱信息出错');
          })
          .finally(() => {
            this.medicalOrderLoading = false;
          });
    },

    // 获取检验结果 - 修改为分组查询
    fetchLabResults() {
      this.labResultLoading = true;

      // 使用分组查询接口
      this.$axios.post('/labResult/page', {
        pageSize: 1000, // 设置较大的pageSize获取所有记录
        pageNum: 1,
        param: {
          patientId: this.searchForm.patientId
          // 不传testName，启用分组模式
        }
      }).then(res => {
        if (res.data.code === 200) {
          // 获取分组数据
          this.groupedLabResults = res.data.data;
        } else {
          this.$message.error('获取检验结果失败');
        }
      }).catch(error => {
        console.error('获取检验结果错误:', error);
        this.$message.error('获取检验结果出错');
      }).finally(() => {
        this.labResultLoading = false;
      });
    },

    // 查看分组详情
    handleViewGroupDetail(row) {
      this.labResultLoading = true;

      this.$axios.post('/labResult/byTime', {
        pageSize: 1000,
        pageNum: 1,
        param: {
          patientId: row.patientId,
          reportTime: this.formatDateForBackend(row.reportTime)
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.currentGroupDetail = res.data.data;
          this.groupDetailDialogVisible = true;
        } else {
          this.$message.error('获取检验详情失败');
        }
      }).catch(error => {
        console.error('加载检验详情错误:', error);
        this.$message.error('加载检验详情出错');
      }).finally(() => {
        this.labResultLoading = false;
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

    // 获取医嘱状态标签类型
    getOrderStatusType(status) {
      const typeMap = {
        '执行中': 'success',
        '已停止': 'info',
        '待审核': 'warning',
        '已作废': 'danger'
      };
      return typeMap[status] || 'info';
    },

    // 获取检验结果状态样式类
    getResultStatusClass(status) {
      const classMap = {
        '正常': 'status-normal',
        '异常': 'status-abnormal',
        '危急': 'status-critical'
      };
      return classMap[status] || 'status-normal';
    },

    // 获取检验结果颜色
    getResultColor(result) {
      if (result.resultStatus === '异常' || result.resultStatus === '危急') {
        return '#F56C6C';
      }
      return '#67C23A';
    },

    // 查看详情
    handleViewDetail(row, type) {
      this.currentDetail = row;
      this.currentDetailType = type;
      this.detailDialogVisible = true;
    },

    // 预览PDF
    handlePreview(row) {
      if (row.reportUrl) {
        window.open(this.getPreviewUrl(row.reportUrl), '_blank');
      }
    },

    // 预览PDF（直接传URL）
    handlePreviewPdf(url) {
      if (url) {
        window.open(this.getPreviewUrl(url), '_blank');
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

    // 获取预览URL
    getPreviewUrl(url) {
      if (url) {
        if (url.startsWith('http')) {
          return url;
        }
        return 'http://localhost:8090' + url;
      }
      return url;
    },

    // 编辑
    handleEdit(row, type) {
      this.currentEditType = type;
      this.editForm = { ...row };
      this.editDialogVisible = true;
    },

    // 更新
    handleUpdate() {
      this.editLoading = true;
      let url = '';
      let form = { ...this.editForm };

      switch (this.currentEditType) {
        case 'ct':
          url = '/ctExamination/update';
          break;
        case 'mri':
          url = '/mriExamination/update';
          break;
        case 'enteroscopy':
          url = '/enteroscopyExamination/update';
          break;
        case 'pathology':
          url = '/pathologyExamination/update';
          break;
      }

      this.$axios.post(url, form)
        .then(res => res.data)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('修改成功');
            this.editDialogVisible = false;
            this.handleSearch(); // 刷新数据
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
  },
  mounted() {
    // 组件加载完成后的初始化操作
  }
}
</script>

<style scoped>
.patient-info-container {
  padding: 0;
}

.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.header-row h2 {
  margin: 0;
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.info-card {
  background: white;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  background: #f7f8fa;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
}

.card-content {
  padding: 20px;
}

.patient-basic-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-label {
  width: 100px;
  color: #86909c;
  font-size: 13px;
}

.info-value {
  flex: 1;
  color: #1d2129;
  font-size: 13px;
}

.table-container {
  margin-top: 15px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #86909c;
}

.empty-state i {
  font-size: 40px;
  margin-bottom: 10px;
  color: #c9cdd4;
}

.status-normal {
  background-color: #e8f3ff;
  color: #165dff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-abnormal {
  background-color: #ffece8;
  color: #f53f3f;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-critical {
  background-color: #fff7e8;
  color: #ff7d00;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>