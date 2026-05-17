package com.hospit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospit.annotation.OperateLog;
import com.hospit.common.QueryPageParam;
import com.hospit.common.Result;
import com.hospit.entity.User;
import com.hospit.service.IUserService;
import com.hospit.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.NumberUtils;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mindrot.jbcrypt.BCrypt;

/**
 * <p>
 * 科室用户登录信息表 前端控制器
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    // 登录失败次数限制配置
    private static final int MAX_LOGIN_ATTEMPTS = 5;  // 最大失败次数
    private static final int LOCK_TIME_MINUTES = 15;  // 锁定时间（分钟）

    // 存储登录失败次数：key=账号, value=失败信息
    private static final ConcurrentHashMap<String, LoginFailInfo> LOGIN_FAIL_MAP = new ConcurrentHashMap<>();

    // 获取用户列表
    @GetMapping("/list")
    public Result list() {
        return Result.success(userService.list());
    }

    // 登录失败信息类
    static class LoginFailInfo {
        private int failCount;          // 失败次数
        private LocalDateTime lockUntil; // 锁定截止时间

        public LoginFailInfo(int failCount, LocalDateTime lockUntil) {
            this.failCount = failCount;
            this.lockUntil = lockUntil;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public LocalDateTime getLockUntil() {
            return lockUntil;
        }

        public void setLockUntil(LocalDateTime lockUntil) {
            this.lockUntil = lockUntil;
        }
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //注册
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, Object> userMap) {
        String loginAccount = (String) userMap.get("loginAccount");
        
        Long count = userService.lambdaQuery()
                .eq(User::getLoginAccount, loginAccount)
                .count();
        
        if (count > 0) {
            return Result.fail("账号已存在");
        }
        
        if (loginAccount == null || loginAccount.trim().isEmpty()) {
            return Result.fail("请输入账号");
        }
        
        String loginPassword = (String) userMap.get("loginPassword");
        if (loginPassword == null || loginPassword.trim().isEmpty()) {
            return Result.fail("请输入密码");
        }
        
        String userName = (String) userMap.get("userName");
        if (userName == null || userName.trim().isEmpty()) {
            return Result.fail("请输入姓名");
        }
        
        User user = new User();
        user.setLoginAccount(loginAccount.trim());
        user.setLoginPassword(BCrypt.hashpw(loginPassword, BCrypt.gensalt()));
        user.setUserName(userName.trim());
        user.setStatus(true);
        user.setCreateTime(LocalDateTime.now());
        
        boolean success = userService.save(user);
        return success ? Result.success(null, "注册成功") : Result.fail("注册失败");
    }

    //新增
    @OperateLog(operationType = "新增", operatedTable = "user", description = "新增用户")
    @PostMapping("/add")
    public boolean add(@RequestBody Map<String, Object> userMap) {
        User user = new User();

        // 手动设置字段
        if (userMap.get("userId") != null) {
            user.setUserId(Integer.valueOf(userMap.get("userId").toString()));
        }
        user.setLoginAccount((String) userMap.get("loginAccount"));
        
        // 密码使用 BCrypt 加密存储
        String rawPassword = (String) userMap.get("loginPassword");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setLoginPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        }
        
        user.setUserName((String) userMap.get("userName"));

        if (userMap.get("status") != null) {
            user.setStatus(Boolean.valueOf(userMap.get("status").toString()));
        }

        // 手动转换日期时间
        if (userMap.get("createTime") != null) {
            user.setCreateTime(LocalDateTime.parse((String) userMap.get("createTime"), FORMATTER));
        }
        if (userMap.get("updateTime") != null) {
            user.setUpdateTime(LocalDateTime.parse((String) userMap.get("updateTime"), FORMATTER));
        }

        return userService.save(user);
    }

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> userMap) {
        String loginAccount = (String) userMap.get("loginAccount");
        String loginPassword = (String) userMap.get("loginPassword");

        // 检查登录失败次数限制
        LoginFailInfo failInfo = LOGIN_FAIL_MAP.get(loginAccount);
        if (failInfo != null) {
            LocalDateTime now = LocalDateTime.now();
            if (failInfo.getLockUntil() != null && now.isBefore(failInfo.getLockUntil())) {
                // 账号被锁定
                long minutesLeft = ChronoUnit.MINUTES.between(now, failInfo.getLockUntil());
                return Result.fail("账号已被锁定，请" + minutesLeft + "分钟后重试");
            }
            // 锁定时间已过，清除记录
            if (failInfo.getLockUntil() != null && now.isAfter(failInfo.getLockUntil())) {
                LOGIN_FAIL_MAP.remove(loginAccount);
            }
        }

        User user = userService.lambdaQuery()
                .eq(User::getLoginAccount, loginAccount)
                .one();

        if (user == null) {
            // 记录失败次数
            recordLoginFail(loginAccount);
            return Result.fail("用户名或密码错误");
        }

        String storedPassword = user.getLoginPassword();
        boolean passwordValid = false;

        // 密码迁移：兼容旧明文密码
        if (storedPassword != null && !storedPassword.isEmpty()) {
            // 尝试 BCrypt 验证
            try {
                passwordValid = BCrypt.checkpw(loginPassword, storedPassword);
            } catch (Exception e) {
                passwordValid = false;
            }

            // 如果 BCrypt 验证失败，尝试明文验证（兼容旧数据）
            if (!passwordValid && storedPassword.equals(loginPassword)) {
                passwordValid = true;
                // 迁移密码：将明文密码更新为 BCrypt 格式
                String hashedPassword = BCrypt.hashpw(loginPassword, BCrypt.gensalt());
                user.setLoginPassword(hashedPassword);
                userService.updateById(user);
            }
        }

        if (!passwordValid) {
            // 记录失败次数
            recordLoginFail(loginAccount);
            int remainingAttempts = getRemainingAttempts(loginAccount);
            if (remainingAttempts > 0) {
                return Result.fail("用户名或密码错误，还有" + remainingAttempts + "次尝试机会");
            } else {
                return Result.fail("账号已被锁定，请" + LOCK_TIME_MINUTES + "分钟后重试");
            }
        }

        // 登录成功，清除失败记录
        LOGIN_FAIL_MAP.remove(loginAccount);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getUserId(), user.getLoginAccount());
        
        // 返回用户信息和Token
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        
        return Result.success(result);
    }

    // 记录登录失败
    private void recordLoginFail(String loginAccount) {
        LoginFailInfo failInfo = LOGIN_FAIL_MAP.get(loginAccount);
        if (failInfo == null) {
            failInfo = new LoginFailInfo(1, null);
        } else {
            failInfo.setFailCount(failInfo.getFailCount() + 1);
        }

        // 达到最大失败次数，设置锁定时间
        if (failInfo.getFailCount() >= MAX_LOGIN_ATTEMPTS) {
            failInfo.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
        }

        LOGIN_FAIL_MAP.put(loginAccount, failInfo);
    }

    // 获取剩余尝试次数
    private int getRemainingAttempts(String loginAccount) {
        LoginFailInfo failInfo = LOGIN_FAIL_MAP.get(loginAccount);
        if (failInfo == null) {
            return MAX_LOGIN_ATTEMPTS;
        }
        return Math.max(0, MAX_LOGIN_ATTEMPTS - failInfo.getFailCount());
    }
    // 分页查询用户
    @PostMapping("/page")
    public Result page(@RequestBody QueryPageParam query) {
//        System.out.println( query);
        HashMap param = query.getParam();
        System.out.println(param);
        System.out.println(param.get("userId"));
        System.out.println(1);
        Object userIdObj = param.get("userId");
        int userId = (userIdObj == null || userIdObj.toString().isEmpty()) ? 0 : Integer.parseInt(userIdObj.toString());

        String userName= param.get("userName") == null ? "" : param.get("userName").toString();
        System.out.println(userId);
        //        System.out.println( param.get("userId"));
        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());
        System.out.println(query.getPageNum());
        System.out.println(query.getPageSize());
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(userId != 0){

            lambdaQueryWrapper.like(User::getUserId, userId);
        }

        if(userName != null){
            lambdaQueryWrapper.like(User::getUserName, userName);
        }

        IPage result = userService.page(page, lambdaQueryWrapper);
        System.out.println(result.getTotal());
        System.out.println(result.getRecords());
        return Result.success(result.getRecords(),result.getTotal() );
    }

    @OperateLog(operationType = "修改", operatedTable = "user", description = "修改用户")
    @PostMapping("/update")
    public Result updateUser(@RequestBody User user) {
        try {
            user.setUpdateTime(LocalDateTime.now());
            // 如果密码不为空且不是BCrypt格式，则加密
            if (user.getLoginPassword() != null && !user.getLoginPassword().startsWith("$2a$")) {
                user.setLoginPassword(BCrypt.hashpw(user.getLoginPassword(), BCrypt.gensalt()));
            }
            boolean success = userService.updateById(user);
            return success ? Result.success(null, "修改成功") : Result.fail("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @OperateLog(operationType = "删除", operatedTable = "user", description = "删除用户")
    @DeleteMapping("/{userId}")
    public Result deleteUser(@PathVariable Integer userId) {
        try {
            boolean success = userService.removeById(userId);
            return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
}
