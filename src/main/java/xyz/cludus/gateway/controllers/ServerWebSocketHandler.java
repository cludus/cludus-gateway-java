package xyz.cludus.gateway.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.cludus.gateway.dtos.ClientMessageDto;
import xyz.cludus.gateway.dtos.ServerMessageDto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    private static final AtomicLong CONNECTIONS = new AtomicLong();

    private static final Gson GSON = new Gson();

    private Map<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CONNECTIONS.incrementAndGet();
        LOG.info("connection count: {}", CONNECTIONS.get());
        Metrics.gauge("cludus_gateway_connections_count", CONNECTIONS);
        sessionMap.put(session.getPrincipal().getName(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Metrics.counter("cludus_gateway_messages_count").increment();
        var latency = Metrics.timer("cludus_gateway_messages_latency");
        latency.recordCallable(() -> {
            try {
                var msg = GSON.fromJson(message.getPayload(), ClientMessageDto.class);
                if(msg.getAction() == ClientMessageDto.Actions.SEND) {
                    var response = ServerMessageDto.message(session.getPrincipal().getName(), msg.getContent());
                    sessionMap.get(msg.getRecipient()).sendMessage(new TextMessage(GSON.toJson(response)));
                }
                var response = ServerMessageDto.ack();
                session.sendMessage(new TextMessage(GSON.toJson(response)));
            }
            catch (JsonSyntaxException ex) {
                var response = ServerMessageDto.error(ex.getMessage());
                session.sendMessage(new TextMessage(GSON.toJson(response)));
            }
            catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
                var response = ServerMessageDto.error(ex.getMessage());
                session.sendMessage(new TextMessage(GSON.toJson(response)));
            }
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
