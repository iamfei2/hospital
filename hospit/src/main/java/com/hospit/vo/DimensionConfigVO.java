package com.hospit.vo;

import lombok.Data;

@Data
public class DimensionConfigVO {
    private String dimensionName;
    private String dimensionLabel;
    private String groupByField;
    private String tableName;
    private String timeFormat;
    private Integer sortOrder;
}
