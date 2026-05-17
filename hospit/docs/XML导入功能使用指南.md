# XML导入功能使用指南

## 一、功能概述

XML导入功能支持将检验结果和检查数据以XML格式批量导入系统。用户只需按照规定的XML格式准备文件，即可快速完成数据批量录入。

---

## 二、支持的数据类型

| 类型 | 接口路径 | 说明 |
|------|----------|------|
| 检验结果 | `/import/lab/xml` | 检验项目结果（如肝功能、血糖等） |
| CT检查 | `/import/ct/xml` | 电子计算机断层扫描 |
| MRI检查 | `/import/mri/xml` | 磁共振成像 |
| 病理检查 | `/import/pathology/xml` | 病理切片分析 |
| 肠镜检查 | `/import/enteroscopy/xml` | 电子肠镜检查 |

---

## 三、XML格式说明

### 3.1 检验结果XML格式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<LabImport>
  <Records>
    <Record>
      <Patient>
        <ID>患者ID</ID>
        <Name>患者姓名（选填）</Name>
      </Patient>
      <LabReport>
        <ReportTime>报告时间</ReportTime>
        <ExecuteDept>执行科室</ExecuteDept>
        <ExecuteDoc>执行医生</ExecuteDoc>
      </LabReport>
      <LabItems>
        <Item>
          <ItemCode>项目代码</ItemCode>
          <ItemName>项目名称（选填）</ItemName>
          <ResultValue>结果值</ResultValue>
          <ResultUnit>单位（选填）</ResultUnit>
        </Item>
        <Item>
          <ItemCode>ALT</ItemCode>
          <ResultValue>32.5</ResultValue>
          <ResultUnit>U/L</ResultUnit>
        </Item>
      </LabItems>
    </Record>
  </Records>
</LabImport>
```

**字段说明：**

| 字段 | 必填 | 说明 |
|------|------|------|
| Patient/ID | ✅ | 患者ID，必须在系统中存在 |
| LabReport/ReportTime | ✅ | 报告时间，格式：`yyyy-MM-dd HH:mm:ss` |
| LabReport/ExecuteDept | ✅ | 执行科室 |
| LabReport/ExecuteDoc | ✅ | 执行医生 |
| ItemCode | ✅ | 检验项目代码，必须在字典中存在 |
| ItemName | 选填 | 项目名称（当ItemCode无法匹配时，可使用名称匹配） |
| ResultValue | ✅ | 检验结果数值 |
| ResultUnit | 选填 | 结果单位，不填则使用字典默认值 |

**示例：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<LabImport>
  <Records>
    <Record>
      <Patient>
        <ID>P001</ID>
        <Name>张三</Name>
      </Patient>
      <LabReport>
        <ReportTime>2026-03-01 10:00:00</ReportTime>
        <ExecuteDept>检验科</ExecuteDept>
        <ExecuteDoc>王医生</ExecuteDoc>
      </LabReport>
      <LabItems>
        <Item>
          <ItemCode>GAMMA_GT</ItemCode>
          <ItemName>γ-谷氨酰转移酶</ItemName>
          <ResultValue>45.6</ResultValue>
          <ResultUnit>U/L</ResultUnit>
        </Item>
        <Item>
          <ItemCode>ALT</ItemCode>
          <ItemName>谷丙转氨酶</ItemName>
          <ResultValue>32.5</ResultValue>
          <ResultUnit>U/L</ResultUnit>
        </Item>
      </LabItems>
    </Record>
  </Records>
</LabImport>
```

---

### 3.2 CT/MRI检查XML格式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ExaminationImport>
  <Records>
    <Record>
      <Patient>
        <ID>患者ID</ID>
        <Name>患者姓名（选填）</Name>
      </Patient>
      <ExaminationInfo>
        <ExamNo>检查编号</ExamNo>
        <ExamTime>检查时间</ExamTime>
        <Part>检查部位</Part>
      </ExaminationInfo>
      <Report>
        <Doctor>报告医生</Doctor>
        <Dept>科室</Dept>
        <Conclusion>报告结论</Conclusion>
      </Report>
    </Record>
  </Records>
</ExaminationImport>
```

**字段说明：**

| 字段 | 必填 | 说明 |
|------|------|------|
| Patient/ID | ✅ | 患者ID |
| ExaminationInfo/ExamNo | 选填 | 检查编号 |
| ExaminationInfo/ExamTime | 选填 | 检查时间 |
| ExaminationInfo/Part | 选填 | 检查部位 |
| Report/Doctor | 选填 | 检查医生 |
| Report/Dept | 选填 | 检查科室 |
| Report/Conclusion | 选填 | 报告结论 |

---

### 3.3 病理检查XML格式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ExaminationImport>
  <Records>
    <Record>
      <Patient>
        <ID>患者ID</ID>
      </Patient>
      <Specimen>
        <PathologyNo>病理号</PathologyNo>
        <Type>标本类型</Type>
        <SamplingTime>采样时间</SamplingTime>
      </Specimen>
      <Diagnosis>
        <Doctor>诊断医生</Doctor>
        <Dept>科室</Dept>
        <Conclusion>诊断结论</Conclusion>
      </Diagnosis>
    </Record>
  </Records>
</ExaminationImport>
```

---

## 四、操作步骤

### 4.1 导入检验结果

1. **准备XML文件**
   - 按照格式要求编辑XML文件
   - 确保患者ID在系统中存在
   - 确保检验项目代码在字典中存在

2. **上传文件**
   - 进入系统「检验结果管理」页面
   - 点击「XML导入」按钮
   - 选择准备好的XML文件

3. **确认导入**
   - 系统显示预览信息
   - 确认无误后点击「确认导入」
   - 导入成功后显示结果

### 4.2 导入检查数据（CT/MRI/病理/肠镜）

操作步骤与检验结果导入相同，仅需在对应的功能模块中选择XML导入。

---

## 五、常见错误及处理

### 5.1 患者不存在

**错误提示：** `患者[P001]不存在`

**原因：** XML中填写的患者ID在系统中不存在

**处理方式：**
1. 系统弹出「新增患者」提示框
2. 填写患者信息并保存
3. 重新上传XML文件

### 5.2 项目代码无效

**错误提示：** `无效的检验项目代码[XXX]`

**原因：** 填写的项目代码在检验项目字典中不存在

**处理方式：**
1. 检查项目代码是否正确
2. 或改用项目名称（系统会尝试按名称匹配）

### 5.3 日期格式错误

**错误提示：** `日期格式错误`

**原因：** 日期格式不符合要求

**支持的日期格式：**
- `yyyy-MM-dd HH:mm:ss`（推荐）
- `yyyy/MM/dd HH:mm:ss`
- `yyyy-MM-dd`
- `yyyy/MM/dd`

---

## 六、注意事项

1. **患者ID必须存在**
   - 导入前请先确认患者已在系统中创建
   - 系统不支持自动创建新患者

2. **项目代码优先匹配**
   - 系统优先使用ItemCode匹配项目
   - ItemCode匹配失败时，会尝试用ItemName匹配

3. **批量导入数量**
   - 建议单次导入不超过1000条记录
   - 大批量导入建议分批进行

4. **XML编码**
   - 文件编码必须为UTF-8
   - 请勿使用Windows记事本编辑（建议使用Notepad++或VS Code）

---

## 七、检验项目代码参考

常用检验项目代码（具体以系统字典为准）：

| 项目代码 | 项目名称 | 默认单位 |
|----------|----------|----------|
| GAMMA_GT | γ-谷氨酰转移酶 | U/L |
| ALT | 谷丙转氨酶 | U/L |
| AST | 谷草转氨酶 | U/L |
| TP | 总蛋白 | g/L |
| ALB | 白蛋白 | g/L |
| TBIL | 总胆红素 | μmol/L |
| DBIL | 直接胆红素 | μmol/L |
| CREA | 肌酐 | μmol/L |
| BUN | 尿素氮 | mmol/L |
| GLU | 空腹血糖 | mmol/L |
| CHO | 总胆固醇 | mmol/L |
| TG | 甘油三酯 | mmol/L |
| HDL | 高密度脂蛋白 | mmol/L |
| LDL | 低密度脂蛋白 | mmol/L |

---

## 八、联系支持

如在使用过程中遇到问题，请联系系统管理员。
