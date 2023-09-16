package xyz.cludus.gateway.controllers;

import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    private static final AtomicLong CONNECTIONS = new AtomicLong();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CONNECTIONS.incrementAndGet();
        LOG.info("connection count: {}", CONNECTIONS.get());
        Metrics.gauge("cludus_gateway_connections_count", CONNECTIONS);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Metrics.counter("cludus_gateway_messages_count").increment();
        var latency = Metrics.timer("cludus_gateway_messages_latency");
        latency.recordCallable(() -> {
            String request = message.getPayload();
            String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
            session.sendMessage(new TextMessage(response));
            return null;
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CONNECTIONS.decrementAndGet();
        LOG.info("connection count: {}", CONNECTIONS.get());
        Metrics.gauge("cludus_gateway_connections_count", CONNECTIONS);
    }
}
