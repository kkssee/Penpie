package com.test.chat;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class HttpHandshakeInterceptor implements HandshakeInterceptor
{
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler, Map<String, Object> attributes)
            throws Exception {
        if (request instanceof ServletServerHttpRequest) {

            // http://localhost/ws/chat?userid=smith 형식으로 웹소켓에 접속할 때, 파라미터를 처리하는 예
            // 아래의 방법을 사용하여 웹사이트에 로그인한 이용자의 ID를 웹소켓핸들러 안으로 전달할 수 있다
            String userno = ((ServletServerHttpRequest) request).getServletRequest().getParameter("userno");
            attributes.put("userno", userno);  // 웹소켓접속시 사용된 파라미터에서 추출된 userid를 웹소켓핸들러에게 전달함

            String chatRoomId = ((ServletServerHttpRequest) request).getServletRequest().getParameter("chatRoomId");
            if(chatRoomId != null) {
                attributes.put("chatRoomId", chatRoomId);
            }

            // ServletContext 의 참조를 구할 때는 아래처럼...
            ServletContext ctx = ((ServletServerHttpRequest) request).getServletRequest().getServletContext();
            // ctx 사용...
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
        // TODO Auto-generated method stub
    }
}