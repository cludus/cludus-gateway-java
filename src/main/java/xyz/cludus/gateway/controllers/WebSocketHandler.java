package xyz.cludus.gateway.controllers;

import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.cludus.gateway.services.LocalSessionRegistry;

import java.util.concurrent.TimeUnit;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private LocalSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Metrics.counter("cludus_gateway_messages_count").increment();
        var latency = Metrics.timer("cludus_gateway_messages_latency");
        var userSession = registry.getSession(session);
        latency.recordCallable(() -> {
            userSession.messageReceived(message);
            return null;
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.sessionClosed(session, status);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void reportCurrentTime() {
        registry.evit();
    }
}
