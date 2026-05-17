package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

// 权限表 - 定义操作级权限
@Data
@TableName("sys_permission")
@Schema(description = "权限表")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限编码，如 patient:view")
    private String code;

    @Schema(description = "资源标识")
    private String resource;

    @Schema(description = "操作：view/add/edit/delete")
    private String action;

    @Schema(description = "描述")
    private String description;
}
