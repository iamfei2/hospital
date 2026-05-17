-- ============================================
-- 项目改进 SQL：增量备份 + RBAC 权限
-- ============================================

-- 备份记录表
DROP TABLE IF EXISTS `backup_record`;
CREATE TABLE `backup_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `backup_type` varchar(20) NOT NULL COMMENT '备份类型：FULL/INCREMENTAL',
  `file_name` varchar(200) DEFAULT NULL COMMENT '备份文件名',
  `binlog_file` varchar(200) DEFAULT NULL COMMENT 'Binlog文件名',
  `binlog_position` bigint DEFAULT NULL COMMENT 'Binlog位点',
  `start_time` datetime DEFAULT NULL COMMENT '备份开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '备份结束时间',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `success` tinyint(1) DEFAULT '1' COMMENT '是否成功',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_type_time` (`backup_type`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备份记录表';

-- RBAC 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `resource` varchar(100) DEFAULT NULL COMMENT '资源标识',
  `action` varchar(50) DEFAULT NULL COMMENT '操作：view/add/edit/delete',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 初始化默认权限
INSERT INTO `sys_permission` (`name`, `code`, `resource`, `action`, `description`) VALUES
('查看患者', 'patient:view', 'patient', 'view', '查看患者信息'),
('管理患者', 'patient:manage', 'patient', 'manage', '新增/修改/删除患者'),
('查看CT检查', 'ct:view', 'ct', 'view', '查看CT检查数据'),
('管理CT检查', 'ct:manage', 'ct', 'manage', '新增/修改/删除CT检查'),
('查看MRI检查', 'mri:view', 'mri', 'view', '查看MRI检查数据'),
('查看肠镜检查', 'enteroscopy:view', 'enteroscopy', 'view', '查看肠镜检查数据'),
('查看病理检查', 'pathology:view', 'pathology', 'view', '查看病理检查数据'),
('查看检验结果', 'lab:view', 'lab', 'view', '查看检验结果'),
('管理检验结果', 'lab:manage', 'lab', 'manage', '新增/修改/删除检验结果'),
('查看统计', 'statistics:view', 'statistics', 'view', '查看统计报表'),
('管理预警规则', 'warning:manage', 'warning', 'manage', '管理预警规则'),
('查看预警记录', 'warning:view', 'warning', 'view', '查看预警记录'),
('查看审计日志', 'audit:view', 'audit', 'view', '查看操作审计日志'),
('数据导入', 'data:import', 'data', 'import', '批量导入数据'),
('数据导出', 'data:export', 'data', 'export', '导出数据'),
('备份管理', 'backup:manage', 'backup', 'manage', '管理数据库备份');

-- 初始化默认角色
INSERT INTO `sys_role` (`id`, `name`, `description`) VALUES
(1, '系统管理员', '拥有所有权限'),
(2, '医生', '查看检查检验数据，不可删除'),
(3, '检验技师', '管理检验结果和预警规则'),
(4, '审计员', '只读权限+审计日志查看');

-- 系统管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 医生角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission` WHERE code IN (
  'patient:view', 'ct:view', 'mri:view', 'enteroscopy:view',
  'pathology:view', 'lab:view', 'statistics:view', 'warning:view',
  'data:export'
);

-- 检验技师权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission` WHERE code IN (
  'patient:view', 'lab:view', 'lab:manage', 'warning:manage',
  'warning:view', 'data:import', 'data:export'
);

-- 审计员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, id FROM `sys_permission` WHERE code IN (
  'patient:view', 'ct:view', 'mri:view', 'enteroscopy:view',
  'pathology:view', 'lab:view', 'statistics:view', 'warning:view',
  'audit:view', 'backup:manage'
);
