package com.hospit.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

@ColumnWidth(20)
public class StatisticsCountExportVO {

    @ExcelProperty("检查类型")
    private String typeName;

    @ExcelProperty("数量")
    private Long count;

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
