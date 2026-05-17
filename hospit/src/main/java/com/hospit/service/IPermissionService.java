package com.hospit.service;

import java.util.List;

// 权限服务接口
public interface IPermissionService {

    // 获取用户的所有权限编码
    List<String> getUserPermissions(Long userId);

    // 检查用户是否有指定权限
    boolean hasPermission(Long userId, String permissionCode);

    // 检查用户是否有任意一个权限
    boolean hasAnyPermission(Long userId, List<String> permissionCodes);
}
