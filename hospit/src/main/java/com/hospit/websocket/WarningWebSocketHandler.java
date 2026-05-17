package com.hospit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket预警处理器 - 实时推送预警消息
 * 端点：/ws/warning/{userId}
 * 支持心跳保活、消息广播、单用户推送
 */
@ServerEndpoint("/ws/warning/{userId}")
@Component
public class WarningWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WarningWebSocketHandler.class);

    private static final Map<String, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        ONLINE_USERS.put(userId, session);
        log.info("WebSocket连接建立: userId={}", userId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        ONLINE_USERS.remove(userId);
        log.info("WebSocket连接关闭: userId={}", userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳处理
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("发送心跳响应失败", e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket异常", error);
    }

    public static void pushToUser(String userId, Object message) {
        Session session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(MAPPER.writeValueAsString(message));
            } catch (IOException e) {
                log.error("推送消息失败: userId={}", userId, e);
            }
        }
    }

    public static void broadcast(Object message) {
        try {
            String json = MAPPER.writeValueAsString(message);
            ONLINE_USERS.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        log.error("广播消息失败", e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("序列化消息失败", e);
        }
    }

    public static int getOnlineCount() {
        return ONLINE_USERS.size();
    }
}
