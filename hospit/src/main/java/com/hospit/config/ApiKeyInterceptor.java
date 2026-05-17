package com.hospit.config;

import com.hospit.entity.ApiAccess;
import com.hospit.service.IApiAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyInterceptor.class);

    private static final String RATE_KEY_PREFIX = "api:rate:";
    private static final String CIRCUIT_KEY_PREFIX = "api:circuit:";
    private static final String CIRCUIT_BREAKER_WINDOW = "circuit:window";
    private static final long CIRCUIT_FAILURE_THRESHOLD = 10;
    private static final long CIRCUIT_BREAK_DURATION_SECONDS = 30;

    @Autowired(required = false)
    private IApiAccessService apiAccessService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // API Key拦截器预处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isEmpty()) {
            writeJson(response, 401, "缺少API Key");
            return false;
        }

        if (apiAccessService == null) {
            writeJson(response, 500, "API认证服务未配置");
            return false;
        }

        if (redisTemplate == null) {
            writeJson(response, 500, "Redis服务未配置");
            return false;
        }

        ApiAccess access = apiAccessService.lambdaQuery()
                .eq(ApiAccess::getApiKey, apiKey)
                .eq(ApiAccess::getEnabled, true)
                .one();

        if (access == null) {
            writeJson(response, 401, "无效的API Key");
            return false;
        }

        if (checkCircuitBreaker(apiKey)) {
            response.setHeader("Retry-After", String.valueOf(CIRCUIT_BREAK_DURATION_SECONDS));
            writeJson(response, 503, "服务暂时不可用，请稍后重试");
            return false;
        }

        int rateLimit = access.getRateLimit() != null ? access.getRateLimit() : 100;

        if (!checkRateLimit(apiKey, rateLimit)) {
            redisTemplate.opsForZSet().incrementScore(
                    CIRCUIT_KEY_PREFIX + apiKey + ":" + CIRCUIT_BREAKER_WINDOW,
                    "failures",
                    1
            );
            response.setHeader("Retry-After", "60");
            writeJson(response, 429, "请求频率超限，请稍后重试");
            return false;
        }

        recordSuccess(apiKey);

        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(getRemaining(apiKey, rateLimit)));

        request.setAttribute("apiAppName", access.getAppName());
        request.setAttribute("apiAccessId", access.getAccessId());

        return true;
    }

    // 检查API频率限制
    private boolean checkRateLimit(String apiKey, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - 60000;

        String rateKey = RATE_KEY_PREFIX + apiKey;

        redisTemplate.opsForZSet().removeRangeByScore(rateKey, 0, windowStart);

        Long count = redisTemplate.opsForZSet().zCard(rateKey);

        if (count != null && count >= limit) {
            return false;
        }

        redisTemplate.opsForZSet().add(rateKey, String.valueOf(now), now);
        redisTemplate.expire(rateKey, 2, TimeUnit.MINUTES);

        return true;
    }

    // 获取剩余请求次数
    private int getRemaining(String apiKey, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - 60000;
        String rateKey = RATE_KEY_PREFIX + apiKey;
        redisTemplate.opsForZSet().removeRangeByScore(rateKey, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(rateKey);
        int used = count != null ? count.intValue() : 0;
        return Math.max(0, limit - used);
    }

    // 检查熔断器状态
    private boolean checkCircuitBreaker(String apiKey) {
        String circuitKey = CIRCUIT_KEY_PREFIX + apiKey + ":" + CIRCUIT_BREAKER_WINDOW;

        Long ttl = redisTemplate.getExpire(circuitKey, TimeUnit.SECONDS);
        if (ttl != null && ttl < 0) {
            redisTemplate.delete(circuitKey);
            return false;
        }

        Double failures = redisTemplate.opsForZSet().score(circuitKey, "failures");
        if (failures != null && failures >= CIRCUIT_FAILURE_THRESHOLD) {
            redisTemplate.expire(circuitKey, CIRCUIT_BREAK_DURATION_SECONDS, TimeUnit.SECONDS);
            return true;
        }

        return false;
    }

    // 记录成功请求
    private void recordSuccess(String apiKey) {
        String circuitKey = CIRCUIT_KEY_PREFIX + apiKey + ":" + CIRCUIT_BREAKER_WINDOW;
        redisTemplate.opsForZSet().remove(circuitKey, "failures");
    }

    // 写入JSON响应
    private void writeJson(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\"}");
    }
}
