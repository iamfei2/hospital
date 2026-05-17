package com.hospit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospit.entity.BackupRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BackupRecordMapper extends BaseMapper<BackupRecord> {

    // 获取最近一次成功备份的binlog位点
    @Select("SELECT * FROM backup_record WHERE success = 1 ORDER BY end_time DESC LIMIT 1")
    BackupRecord selectLatestSuccessful();

    // 获取指定类型的最近备份位点
    @Select("SELECT * FROM backup_record WHERE success = 1 AND backup_type = #{backupType} ORDER BY end_time DESC LIMIT 1")
    BackupRecord selectLatestByType(@Param("backupType") String backupType);
}
