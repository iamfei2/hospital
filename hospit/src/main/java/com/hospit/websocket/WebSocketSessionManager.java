package com.hospit.websocket;

import org.springframework.stereotype.Component;

/**
 * WebSocket会话管理器 - 统一消息推送入口
 * 支持单用户推送和广播模式
 */
@Component
public class WebSocketSessionManager {

    /** 向指定用户推送消息 */
    public void pushToUser(String userId, Object message) {
        WarningWebSocketHandler.pushToUser(userId, message);
    }

    /** 广播消息给所有在线用户 */
    public void broadcast(Object message) {
        WarningWebSocketHandler.broadcast(message);
    }

    /** 获取当前在线人数 */
    public int getOnlineCount() {
        return WarningWebSocketHandler.getOnlineCount();
    }
}
