package com.test.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final static String CHAT_ENDPOINT = "/ws/chat";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getChatWebSocketHandler(), CHAT_ENDPOINT)
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOrigins("*");
        log.info("웹소켓 핸들러 등록 완료");
    }

    @Bean
    public WebSocketHandler getChatWebSocketHandler() {
        return new WebSocketHandler(); // 커스텀 웹소켓 핸들러
    }
}
