package com.hospit.config;

import com.hospit.entity.User;
import com.hospit.service.IPermissionService;
import com.hospit.service.IUserService;
import com.hospit.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * JWT认证拦截器 - 验证Token并做权限控制
 * admin角色放行所有接口，其他角色基于RBAC权限编码检查
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private IUserService userService;

    @Autowired
    private IPermissionService permissionService;

    // 角色兜底权限：admin=全部，user=受限（权限表未配置时生效）
    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            "admin", Arrays.asList("/**"),
            "user", Arrays.asList(
                    "/user/**", "/patient/**",
                    "/ctExamination/**", "/mriExamination/**",
                    "/enteroscopyExamination/**", "/pathologyExamination/**",
                    "/labResult/**", "/labItemDict/**",
                    "/medicalOrder/**", "/statistics/**",
                    "/operationLog/**", "/warningRecord/**",
                    "/backup/**", "/attachment/**",
                    "/searchTemplate/**", "/ws/**"
            )
    );

    // 请求路径到权限编码的映射
    private static final Map<String, String> PATH_PERMISSION_MAP = Map.ofEntries(
            Map.entry("/patient", "patient:view"),
            Map.entry("/ctExamination", "ct:view"),
            Map.entry("/mriExamination", "mri:view"),
            Map.entry("/enteroscopyExamination", "enteroscopy:view"),
            Map.entry("/pathologyExamination", "pathology:view"),
            Map.entry("/labResult", "lab:view"),
            Map.entry("/labItemDict", "lab:dict"),
            Map.entry("/statistics", "statistics:view"),
            Map.entry("/warningRule", "warning:manage"),
            Map.entry("/warningRecord", "warning:view"),
            Map.entry("/backup", "backup:manage"),
            Map.entry("/import", "data:import"),
            Map.entry("/operationLog", "audit:view"),
            Map.entry("/searchTemplate", "template:view")
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || !JwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\"}");
            return false;
        }
        
        Integer userId = JwtUtil.getUserId(token);
        String loginAccount = JwtUtil.getLoginAccount(token);
        request.setAttribute("userId", userId);
        request.setAttribute("loginAccount", loginAccount);

        User user = userService.getById(userId);
        if (user != null) {
            String role = user.getRole() != null ? user.getRole() : "user";
            request.setAttribute("userRole", role);

            // admin admin
            if ("admin".equals(role)) return true;

            String requestUri = request.getRequestURI();

            // RBAC
            String permissionCode = resolvePermissionCode(requestUri);
            if (permissionCode != null && permissionService.hasPermission(userId.longValue(), permissionCode)) {
                return true;
            }

            //  path
            List<String> allowedPaths = ROLE_PERMISSIONS.getOrDefault(role, ROLE_PERMISSIONS.get("user"));
            boolean hasPermission = allowedPaths.stream().anyMatch(pattern -> matchPath(pattern, requestUri));

            if (!hasPermission) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"权限不足\"}");
                return false;
            }
        }
        
        return true;
    }

    //  URI 
    private String resolvePermissionCode(String requestUri) {
        for (Map.Entry<String, String> entry : PATH_PERMISSION_MAP.entrySet()) {
            if (requestUri.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean matchPath(String pattern, String path) {
        if (pattern.equals("/**")) return true;
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }
}
