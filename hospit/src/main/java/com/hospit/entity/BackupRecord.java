package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// 备份记录表 - 追踪全量和增量备份的binlog位点
@Data
@TableName("backup_record")
@Schema(description = "备份记录表")
public class BackupRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "备份类型：FULL/INCREMENTAL")
    private String backupType;

    @Schema(description = "备份文件名")
    private String fileName;

    @Schema(description = "Binlog文件名")
    private String binlogFile;

    @Schema(description = "Binlog位点")
    private Long binlogPosition;

    @Schema(description = "备份开始时间")
    private LocalDateTime startTime;

    @Schema(description = "备份结束时间")
    private LocalDateTime endTime;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
