package com.hospit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RestoreConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(RestoreConfirmationService.class);

    public static final class PendingRestore {
        private final String fileName;
        private final boolean encrypted;
        private final boolean compressed;
        private final String operatorIp;
        private final LocalDateTime createTime;
        private final int confirmCode;

        public PendingRestore(String fileName, boolean encrypted, boolean compressed, String operatorIp, int confirmCode) {
            this.fileName = fileName;
            this.encrypted = encrypted;
            this.compressed = compressed;
            this.operatorIp = operatorIp;
            this.createTime = LocalDateTime.now();
            this.confirmCode = confirmCode;
        }

        public String getFileName() { return fileName; }
        public boolean isEncrypted() { return encrypted; }
        public boolean isCompressed() { return compressed; }
        public String getOperatorIp() { return operatorIp; }
        public LocalDateTime getCreateTime() { return createTime; }
        public int getConfirmCode() { return confirmCode; }
    }

    private final Map<String, PendingRestore> pendingRestores = new ConcurrentHashMap<>();
    private static final long CONFIRM_TIMEOUT_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();

    public record ConfirmInitResult(String token, int confirmCode, LocalDateTime expiresAt) {}
    public record ConfirmCheckResult(boolean valid, String message) {}

    public ConfirmInitResult initRestore(String fileName, boolean encrypted, boolean compressed, String operatorIp) {
        String token = generateToken();
        int confirmCode = generateConfirmCode();

        PendingRestore pending = new PendingRestore(fileName, encrypted, compressed, operatorIp, confirmCode);
        pendingRestores.put(token, pending);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CONFIRM_TIMEOUT_MINUTES);

        log.info("数据库恢复待确认: token={}, fileName={}, operatorIp={}, expiresAt={}",
                token, fileName, operatorIp, expiresAt);

        return new ConfirmInitResult(token, confirmCode, expiresAt);
    }

    public ConfirmCheckResult checkConfirmation(String token, int confirmCode, String clientIp) {
        PendingRestore pending = pendingRestores.get(token);

        if (pending == null) {
            log.warn("恢复确认失败: token不存在, clientIp={}", clientIp);
            return new ConfirmCheckResult(false, "恢复确认令牌不存在或已过期，请重新发起恢复请求");
        }

        if (!isIpMatching(pending.getOperatorIp(), clientIp)) {
            log.warn("恢复确认失败: IP不匹配, expected={}, actual={}", pending.getOperatorIp(), clientIp);
            return new ConfirmCheckResult(false, "操作来源IP不一致，请使用原发起设备确认");
        }

        if (isExpired(pending.getCreateTime())) {
            pendingRestores.remove(token);
            log.warn("恢复确认失败: 令牌已过期, token={}", token);
            return new ConfirmCheckResult(false, "恢复确认已超时（5分钟），请重新发起恢复请求");
        }

        if (pending.getConfirmCode() != confirmCode) {
            log.warn("恢复确认失败: 确认码错误, token={}", token);
            return new ConfirmCheckResult(false, "确认码错误，请查看正确的确认码后重新输入");
        }

        pendingRestores.remove(token);
        log.info("恢复确认成功: token={}, fileName={}", token, pending.getFileName());
        return new ConfirmCheckResult(true, pending.getFileName());
    }

    public PendingRestore getAndRemovePending(String token) {
        return pendingRestores.remove(token);
    }

    public void cleanupExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CONFIRM_TIMEOUT_MINUTES);
        pendingRestores.entrySet().removeIf(entry -> entry.getValue().getCreateTime().isBefore(cutoff));
    }

    private boolean isIpMatching(String expected, String actual) {
        if (expected == null || actual == null) {
            return true;
        }
        if (expected.equals(actual)) {
            return true;
        }
        if (expected.startsWith("127.") && actual.equals("127.0.0.1")) {
            return true;
        }
        if (actual.startsWith("127.")) {
            return true;
        }
        return false;
    }

    private boolean isExpired(LocalDateTime createTime) {
        return createTime.plusMinutes(CONFIRM_TIMEOUT_MINUTES).isBefore(LocalDateTime.now());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private int generateConfirmCode() {
        return 100000 + random.nextInt(900000);
    }
}
