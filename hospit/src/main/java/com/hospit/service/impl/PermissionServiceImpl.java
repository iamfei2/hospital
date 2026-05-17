package com.hospit.service.impl;

import com.hospit.mapper.PermissionMapper;
import com.hospit.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 权限服务实现 - 支持细粒度权限检查
@Service
public class PermissionServiceImpl implements IPermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    // 权限缓存，减少数据库查询
    private final Map<Long, List<String>> permissionCache = new ConcurrentHashMap<>();

    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) return Collections.emptyList();

        // 先查缓存
        List<String> cached = permissionCache.get(userId);
        if (cached != null) return cached;

        // 查数据库
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        if (permissions != null) {
            permissionCache.put(userId, permissions);
            return permissions;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(Long userId, List<String> permissionCodes) {
        List<String> permissions = getUserPermissions(userId);
        for (String code : permissionCodes) {
            if (permissions.contains(code)) return true;
        }
        return false;
    }

    // 清除用户权限缓存（权限变更时调用）
    public void clearCache(Long userId) {
        permissionCache.remove(userId);
    }

    // 清除所有缓存
    public void clearAllCache() {
        permissionCache.clear();
    }
}
