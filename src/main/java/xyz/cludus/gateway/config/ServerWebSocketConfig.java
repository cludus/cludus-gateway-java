package xyz.cludus.gateway.config;

import io.micrometer.core.instrument.Metrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import xyz.cludus.gateway.controllers.ServerWebSocketHandler;

import java.net.UnknownHostException;

@Configuration
@EnableWebSocket
public class ServerWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/websocket");
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new ServerWebSocketHandler();
    }
}