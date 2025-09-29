// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.socket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * websocket服务端
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Component
public class WebSocketServerHandler extends TextWebSocketHandler {

    // 存储 userId -> 该用户的多个 WebSocketSession（支持 PC & 移动端）
    private static final Map<String, Set<WebSocketSession>> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 解析 WebSocket URL 参数
        Map<String, String> queryParams = parseQueryParams(session.getUri());

        // 获取 token & platform
        String token = queryParams.get("token");
        String platform = queryParams.getOrDefault("platform", "unknown");

        if (token == null || token.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            System.out.println("❌ 连接拒绝，缺少 token");
            return;
        }

        // 解析 userId
        String userId = parseUserIdFromToken(token);

        // 存储用户 WebSocket 连接（支持多个设备同时在线）
        USER_SESSIONS.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("✅ WebSocket 连接成功: userId=" + userId + ", platform=" + platform + "，当前在线用户: " + USER_SESSIONS.size());

        session.sendMessage(new TextMessage("连接成功！你的 userId 是：" + userId + "，设备：" + platform));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    USER_SESSIONS.remove(userId);
                }
            }
        }
        System.out.println("🔴 WebSocket 关闭: userId=" + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 处理心跳消息
        if (StrUtil.isNotEmpty(payload)) {
            // 更新会话活跃时间
            session.sendMessage(new TextMessage(payload));
            System.out.println("❤️ 收到心跳: sessionId=" + session.getId());
        } else {
            // 其他业务消息处理
            super.handleTextMessage(session, message);
        }
    }

    /**
     * 服务器主动发送消息给指定用户
     */
    public void sendMessageToUser(String userId, String message) {
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("📩 发送消息成功: userId=" + userId + ", message=" + message);
        } else {
            System.out.println("⚠️ 用户未在线: userId=" + userId);
        }
    }

    /**
     * 服务器主动发送消息给所有在线用户
     */
    public void sendMessageToAll(String message) throws IOException {
        for (Set<WebSocketSession> sessions : USER_SESSIONS.values()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
        System.out.println("📢 发送广播消息：" + message);
    }

    /**
     * 解析 WebSocket URL 的查询参数
     */
    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> queryMap = new HashMap<>();
        if (uri == null || uri.getQuery() == null) {
            // 防止 NullPointerException
            return queryMap;
        }

        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                queryMap.put(keyValue[0], keyValue[1]);
            }
        }
        return queryMap;
    }

    /**
     * 解析 JWT Token 获取 userId
     */
    private String parseUserIdFromToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        JWTPayload payload = jwt.getPayload();
        String isAdmin = payload.getClaim("isAdmin").toString();
        String userId = payload.getClaim("userId").toString();
        return StrUtil.format("{}::{}", isAdmin, userId);
    }

    /**
     * 获取 session 对应的 userId（通过 token 解析）
     */
    private String getUserIdFromSession(WebSocketSession session) {
        Map<String, String> queryParams = parseQueryParams(session.getUri());
        String token = queryParams.get("token");
        return token != null ? parseUserIdFromToken(token) : null;
    }
}
